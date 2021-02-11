/*
 * Copyright 2017 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.etools.j1939_84.modules.DiagnosticMessageModule.getCompositeSystems;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for the {@link DiagnosticMessageModule}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
        justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReadinessModuleTest {

    private static final int BUS_ADDR = 0xA5;

    private static MonitoredSystemStatus getStatus(boolean enabled, boolean complete) {
        return findStatus(true, enabled, complete);
    }

    private DiagnosticMessageModule instance;

    @Spy
    private J1939 j1939;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener();
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new DiagnosticMessageModule();
        instance.setJ1939(j1939);
        DataRepository.clearInstance();
    }

    @Test
    public void testGetCompositeSystems() {
        Set<MonitoredSystem> monitoredSystems = new ConcurrentSkipListSet<>();
        monitoredSystems.add(new MonitoredSystem("System123", getStatus(true, true), 1, AC_SYSTEM_REFRIGERANT, true));
        monitoredSystems.add(new MonitoredSystem("System123", getStatus(true, true), 2, AC_SYSTEM_REFRIGERANT, true));
        monitoredSystems.add(new MonitoredSystem("System123", getStatus(true, true), 3, AC_SYSTEM_REFRIGERANT, true));
        monitoredSystems.add(new MonitoredSystem("System456",
                                                 getStatus(true, true),
                                                 1,
                                                 BOOST_PRESSURE_CONTROL_SYS,
                                                 true));
        monitoredSystems.add(new MonitoredSystem("System456",
                                                 getStatus(true, false), 2, BOOST_PRESSURE_CONTROL_SYS, true));
        monitoredSystems.add(new MonitoredSystem("System456",
                                                 getStatus(false, false), 3, BOOST_PRESSURE_CONTROL_SYS, true));
        monitoredSystems.add(new MonitoredSystem("System789", getStatus(false, false), 1, CATALYST, true));
        monitoredSystems.add(new MonitoredSystem("System789", getStatus(false, false), 2, CATALYST, true));
        monitoredSystems.add(new MonitoredSystem("System789", getStatus(false, false), 3, CATALYST, true));

        List<CompositeMonitoredSystem> expected = new ArrayList<>();
        expected.add(new CompositeMonitoredSystem(
                new MonitoredSystem("System123", getStatus(true, true), -1, AC_SYSTEM_REFRIGERANT, true), true));
        expected.add(new CompositeMonitoredSystem(
                new MonitoredSystem("System456", getStatus(true, false), -1, BOOST_PRESSURE_CONTROL_SYS, true), true));
        expected.add(new CompositeMonitoredSystem(
                new MonitoredSystem("System789", getStatus(false, false), -1, CATALYST, true), true));

        List<CompositeMonitoredSystem> actual = getCompositeSystems(monitoredSystems, true);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDM20PacketsNoResponse() throws BusException {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 Error: Timeout - No Response." + NL;
        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM20PacketsTrue() throws BusException {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                                                                                                            any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 18C2A500 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,721" + NL;
        expected += "OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C2A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                 513" + NL;
        expected += "OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C2A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,208" + NL;
        expected += "OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;

        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM20PacketsWithEngine1Response() throws BusException {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn | BUS_ADDR, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                                                                                                            any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 18C2A501 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #2 (1):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,721" + NL;
        expected += "OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C2A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                 513" + NL;
        expected += "OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C2A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,208" + NL;
        expected += "OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;

        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM21PacketsNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM21 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 Error: Timeout - No Response." + NL;

        instance.requestDM21(listener, 0x17);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM21PacketsTrue() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet3.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM21 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.requestDM21(listener, 0x21);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21PacketsNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty())
                .when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 Error: Timeout - No Response." + NL;
        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21PacketsTrue() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                                                                                                            any());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C1A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C1A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21PacketsWithEngine1Response() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn | BUS_ADDR, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                                                                                                            any());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A501 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #2 (1): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C1A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.0000 18C1A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM5() throws BusException {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                                                                                                            any());

        String expected = "";
        expected += "10:15:30.0000 Global DM5 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CE FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECE00 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #1 (0): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "10:15:30.0000 18FECE17 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "10:15:30.0000 18FECE21 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;

        instance.requestDM5(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM5WithNoResponses() throws BusException {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM5 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CE FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Error: Timeout - No Response." + NL;
        instance.requestDM5(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }
}
