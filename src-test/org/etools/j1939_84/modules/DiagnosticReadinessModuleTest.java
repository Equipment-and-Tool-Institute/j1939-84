/**
 * Copyright 2017 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for the {@link DiagnosticReadinessModule}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                    justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class DiagnosticReadinessModuleTest {

    private static final int BUS_ADDR = 0xA5;

    private DiagnosticReadinessModule instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener();
        instance = new DiagnosticReadinessModule(new TestDateTimeModule());
        instance.setJ1939(j1939);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(j1939);
    }

    @Test
    public void testGetCompositeSystems() {
        Set<MonitoredSystem> monitoredSystems = new ConcurrentSkipListSet<>();
        monitoredSystems.add(new MonitoredSystem("System123", findStatus(false, true, true), 1,
                CompositeSystem.AC_SYSTEM_REFRIGERANT));
        monitoredSystems.add(new MonitoredSystem("System123", findStatus(false, true, true), 2,
                CompositeSystem.AC_SYSTEM_REFRIGERANT));
        monitoredSystems.add(new MonitoredSystem("System123", findStatus(false, true, true), 3,
                CompositeSystem.AC_SYSTEM_REFRIGERANT));
        monitoredSystems.add(new MonitoredSystem("System456", findStatus(false, true, true), 1,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS));
        monitoredSystems.add(new MonitoredSystem("System456", findStatus(false, true, false), 2,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS));
        monitoredSystems.add(new MonitoredSystem("System456", findStatus(false, false, false), 3,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS));
        monitoredSystems
                .add(new MonitoredSystem("System789", findStatus(false, false, false), 1, CompositeSystem.CATALYST));
        monitoredSystems
                .add(new MonitoredSystem("System789", findStatus(false, false, false), 2, CompositeSystem.CATALYST));
        monitoredSystems
                .add(new MonitoredSystem("System789", findStatus(false, false, false), 3, CompositeSystem.CATALYST));

        List<MonitoredSystem> expected = new ArrayList<>();
        expected.add(new MonitoredSystem("System123", findStatus(false, true, true), -1,
                CompositeSystem.AC_SYSTEM_REFRIGERANT));
        expected.add(new MonitoredSystem("System456", findStatus(false, true, false), -1,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS));
        expected.add(new MonitoredSystem("System789", findStatus(false, false, false), -1, CompositeSystem.CATALYST));

        // FIXME
        // List<CompositeMonitoredSystem> actual =
        // DiagnosticReadinessModule.getCompositeSystems(monitoredSystems,
        // false);
        // assertEquals(expected, actual);
    }

    @Test
    public void testGetDM20PacketsFalse() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "10:15:30.000 18C20000 11 22 33 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18C20017 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18C20021 10 20 30 40 50 60 70 80" + NL;

        instance.getDM20Packets(listener, false);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testGetDM20PacketsNoResponse() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.getDM20Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(3))
                .requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testGetDM20PacketsTrue() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "10:15:30.000 18C20000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,721" + NL;
        expected += "OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20017 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                 513" + NL;
        expected += "OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,208" + NL;
        expected += "OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;

        instance.getDM20Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testGetDM20PacketsWithEngine1Response() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "10:15:30.000 18C20001 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #2 (1):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,721" + NL;
        expected += "OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20017 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                 513" + NL;
        expected += "OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,208" + NL;
        expected += "OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;

        instance.getDM20Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testGetDM20PacketWithNoListener() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        List<DM20MonitorPerformanceRatioPacket> packets = instance.getDM20Packets(null, false);
        assertEquals(3, packets.size());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testGetDM21PacketsFalse() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0x21)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Destination Specific DM21 Request" + NL;
        expected += "10:15:30.000 18EA21A5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 70 80" + NL;

        instance.getDM21Packets(listener, false, 0x21);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM21PacketsNoResponse() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0x17)).thenReturn(requestPacket);

        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Destination Specific DM21 Request" + NL;
        expected += "10:15:30.000 18EA17A5 00 C1 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.getDM21Packets(listener, true, 0x17);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939, times(3))
                .requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM21PacketsTrue() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0x21)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Destination Specific DM21 Request" + NL;
        expected += "10:15:30.000 18EA21A5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.getDM21Packets(listener, true, 0x21);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM26PacketsFalse() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.000 18FDB800 11 22 33 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18FDB817 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18FDB821 10 20 30 40 50 60 70 80" + NL;
        instance.getDM26Packets(listener, false);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM26PacketsNoResponse() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.getDM26Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(3))
                .requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM26PacketsTrue() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.000 18FDB800 11 22 33 44 55 66 77 88" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 51, Time Since Engine Start: 8,721 seconds" + NL;
        expected += "10:15:30.000 18FDB817 01 02 03 04 05 06 07 08" + NL;
        expected += "DM26 from Instrument Cluster #1 (23): Warm-ups: 3, Time Since Engine Start: 513 seconds" + NL;
        expected += "10:15:30.000 18FDB821 10 20 30 40 50 60 70 80" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 48, Time Since Engine Start: 8,208 seconds" + NL;

        instance.getDM26Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM26PacketsWithEngine1Response() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.000 18FDB801 11 22 33 44 55 66 77 88" + NL;
        expected += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds" + NL;
        expected += "10:15:30.000 18FDB817 01 02 03 04 05 06 07 08" + NL;
        expected += "DM26 from Instrument Cluster #1 (23): Warm-ups: 3, Time Since Engine Start: 513 seconds" + NL;
        expected += "10:15:30.000 18FDB821 10 20 30 40 50 60 70 80" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 48, Time Since Engine Start: 8,208 seconds" + NL;

        instance.getDM26Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM26PacketsWithNoListener() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        List<DM26TripDiagnosticReadinessPacket> packets = instance.getDM26Packets(null, false);
        assertEquals(3, packets.size());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM5PacketsEngine1Response() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE01 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #2 (1): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "10:15:30.000 18FECE21 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;

        instance.getDM5Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM5PacketsFalse() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE00 11 22 33 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18FECE21 10 20 30 40 50 60 70 80" + NL;

        instance.getDM5Packets(listener, false);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM5PacketsNoResponse() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.getDM5Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(3))
                .requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM5PacketsTrue() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE00 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #1 (0): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "10:15:30.000 18FECE21 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;

        instance.getDM5Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetDM5PacketsWithNoListener() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        List<DM5DiagnosticReadinessPacket> packets = instance.getDM5Packets(null, false);
        assertEquals(3, packets.size());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetIgnitionCycles() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x00, 0x00, 0x00, 0x00, 0x55, 0x66, 0x77, 0x88)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x33, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x39, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE)));
        assertEquals(8208, DiagnosticReadinessModule.getIgnitionCycles(packets));
    }

    @Test
    public void testGetIgnitionCyclesWithoutEngine() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80)));

        assertEquals(8208, DiagnosticReadinessModule.getIgnitionCycles(packets));
    }

    @Test
    public void testGetIgnitionCyclesWithoutPackets() {
        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        assertEquals(-1, DiagnosticReadinessModule.getIgnitionCycles(packets));
    }

    @Test
    public void testGetOBDCounts() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x00, 0x00, 0x00, 0x0, 0x55, 0x66, 0x77, 0x88)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x33, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x39, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE, 0xFE)));
        assertEquals(16432, DiagnosticReadinessModule.getOBDCounts(packets));
    }

    @Test
    public void testGetOBDCountsWithoutEngine() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08)));
        packets.add(new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80)));

        assertEquals(16432, DiagnosticReadinessModule.getOBDCounts(packets));
    }

    @Test
    public void testGetOBDCountsWithoutPackets() {
        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        assertEquals(-1, DiagnosticReadinessModule.getOBDCounts(packets));
    }

    @Test
    public void testGetOBDModules() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 20, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet11 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 20, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet22 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 19, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet11, packet2, packet22, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE00 11 22 14 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18FECE00 11 22 14 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18FECE21 10 20 13 40 50 60 70 80" + NL;
        expected += "Engine #1 (0) reported as an HD-OBD Module." + NL;
        expected += "Body Controller (33) reported as an HD-OBD Module." + NL;

        List<Integer> results = instance.getOBDModules(listener);
        assertEquals(2, results.size());
        assertTrue(results.contains(0x00));
        assertTrue(results.contains(0x21));

        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testGetRatios() {
        PerformanceRatio ratio01 = new PerformanceRatio(1, 1, 1, 0);
        PerformanceRatio ratio02 = new PerformanceRatio(2, 1, 1, 0);
        PerformanceRatio ratio03 = new PerformanceRatio(3, 1, 1, 0);
        List<PerformanceRatio> ratios0 = new ArrayList<>();
        ratios0.add(ratio01);
        ratios0.add(ratio02);
        ratios0.add(ratio03);

        PerformanceRatio ratio11 = new PerformanceRatio(1, 1, 1, 1);
        PerformanceRatio ratio12 = new PerformanceRatio(2, 1, 1, 1);
        PerformanceRatio ratio13 = new PerformanceRatio(3, 1, 1, 1);
        List<PerformanceRatio> ratios1 = new ArrayList<>();
        ratios1.add(ratio11);
        ratios1.add(ratio12);
        ratios1.add(ratio13);

        Set<PerformanceRatio> expectedRatios = new HashSet<>();
        expectedRatios.addAll(ratios0);
        expectedRatios.addAll(ratios1);

        List<DM20MonitorPerformanceRatioPacket> packets = new ArrayList<>();
        DM20MonitorPerformanceRatioPacket packet0 = mock(DM20MonitorPerformanceRatioPacket.class);
        when(packet0.getRatios()).thenReturn(ratios0);
        packets.add(packet0);
        DM20MonitorPerformanceRatioPacket packet1 = mock(DM20MonitorPerformanceRatioPacket.class);
        when(packet1.getRatios()).thenReturn(ratios1);
        packets.add(packet1);

        Set<PerformanceRatio> actualRatios = DiagnosticReadinessModule.getRatios(packets);
        assertEquals(expectedRatios, actualRatios);
    }

    @Test
    public void testGetSystems() {
        MonitoredSystem system01 = new MonitoredSystem("system1", findStatus(false, true, true), 0,
                CompositeSystem.EGR_VVT_SYSTEM);
        MonitoredSystem system02 = new MonitoredSystem("system2", findStatus(false, false, false), 0,
                CompositeSystem.EXHAUST_GAS_SENSOR);
        MonitoredSystem system03 = new MonitoredSystem("system3", findStatus(false, false, false), 0,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        Set<MonitoredSystem> systems0 = new HashSet<>();
        systems0.add(system01);
        systems0.add(system02);
        systems0.add(system03);

        MonitoredSystem system11 = new MonitoredSystem("system1", findStatus(false, false, false), 1,
                CompositeSystem.EGR_VVT_SYSTEM);
        MonitoredSystem system12 = new MonitoredSystem("system2", findStatus(false, true, true), 1,
                CompositeSystem.EXHAUST_GAS_SENSOR);
        MonitoredSystem system13 = new MonitoredSystem("system3", findStatus(false, true, false), 1,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        Set<MonitoredSystem> systems1 = new HashSet<>();
        systems1.add(system11);
        systems1.add(system12);
        systems1.add(system13);

        Set<MonitoredSystem> expectedSystems = new HashSet<>();
        expectedSystems.addAll(systems0);
        expectedSystems.addAll(systems1);

        List<DM5DiagnosticReadinessPacket> packets = new ArrayList<>();
        DM5DiagnosticReadinessPacket packet0 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet0.getMonitoredSystems()).thenReturn(systems0);
        packets.add(packet0);
        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems1);
        packets.add(packet1);

        Set<MonitoredSystem> actualSystems = DiagnosticReadinessModule.getSystems(packets);
        assertEquals(expectedSystems, actualSystems);
    }

    @Test
    public void testReportDM20() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "10:15:30.000 18C20000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,721" + NL;
        expected += "OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20017 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                 513" + NL;
        expected += "OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C20021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                     Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               8,208" + NL;
        expected += "OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;

        assertEquals(true, instance.reportDM20(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testReportDM20WithNoResponses() {
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM20 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C2 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        assertEquals(false, instance.reportDM20(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(3))
                .requestRaw(DM20MonitorPerformanceRatioPacket.class, requestPacket);
    }

    @Test
    public void testReportDM21() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x77, 0x88));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10017 01 02 03 04 05 06 77 88" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 77 88" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;

        instance.reportDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM21WithGap() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;

        instance.reportDM21(listener);
        String message = listener.getResults();
        assertEquals(expected, message);

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class,
                requestPacket);
    }

    @Test
    public void testReportDM21WithNewFile() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;

        instance.reportDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM21WithNoResponses() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.reportDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(3))
                .requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM21WithReset() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x00, 0x00));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1).map(p -> new Either<>(p, null)));

        instance.reportDM21(listener);

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM26() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.000 18FDB800 11 22 33 44 55 66 77 88" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 51, Time Since Engine Start: 8,721 seconds" + NL;
        expected += "10:15:30.000 18FDB817 01 02 03 04 05 06 07 08" + NL;
        expected += "DM26 from Instrument Cluster #1 (23): Warm-ups: 3, Time Since Engine Start: 513 seconds" + NL;
        expected += "10:15:30.000 18FDB821 10 20 30 40 50 60 70 80" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 48, Time Since Engine Start: 8,208 seconds" + NL;
        expected += NL;
        expected += "Vehicle Composite of DM26:" + NL;
        expected += "A/C system refrigerant         enabled, not complete" + NL;
        expected += "Boost pressure control sys     enabled,     complete" + NL;
        expected += "Catalyst                       enabled, not complete" + NL;
        expected += "Cold start aid system      not enabled,     complete" + NL;
        expected += "Comprehensive component        enabled, not complete" + NL;
        expected += "Diesel Particulate Filter      enabled,     complete" + NL;
        expected += "EGR/VVT system             not enabled,     complete" + NL;
        expected += "Evaporative system             enabled, not complete" + NL;
        expected += "Exhaust Gas Sensor         not enabled, not complete" + NL;
        expected += "Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expected += "Fuel System                not enabled,     complete" + NL;
        expected += "Heated catalyst            not enabled, not complete" + NL;
        expected += "Misfire                    not enabled,     complete" + NL;
        expected += "NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "Secondary air system       not enabled,     complete" + NL;

        assertEquals(true, instance.reportDM26(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM26WithNoResponses() {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM26 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        assertEquals(false, instance.reportDM26(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(3))
                .requestRaw(DM26TripDiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM5() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE00 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #1 (0): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "10:15:30.000 18FECE21 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;
        expected += NL;
        expected += "Vehicle Composite of DM5:" + NL;
        expected += "A/C system refrigerant         supported, not complete" + NL;
        expected += "Boost pressure control sys     supported,     complete" + NL;
        expected += "Catalyst                       supported, not complete" + NL;
        expected += "Cold start aid system      not supported,     complete" + NL;
        expected += "Comprehensive component        supported, not complete" + NL;
        expected += "Diesel Particulate Filter      supported,     complete" + NL;
        expected += "EGR/VVT system             not supported,     complete" + NL;
        expected += "Evaporative system             supported, not complete" + NL;
        expected += "Exhaust Gas Sensor         not supported, not complete" + NL;
        expected += "Exhaust Gas Sensor heater      supported, not complete" + NL;
        expected += "Fuel System                not supported,     complete" + NL;
        expected += "Heated catalyst            not supported, not complete" + NL;
        expected += "Misfire                    not supported,     complete" + NL;
        expected += "NMHC converting catalyst   not supported,     complete" + NL;
        expected += "NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "Secondary air system       not supported,     complete" + NL;

        assertEquals(true, instance.reportDM5(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportDM5WithNoResponses() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        assertEquals(false, instance.reportDM5(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(3))
                .requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testReportMonitoredSystems() {
        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(0, 0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        instance.reportMonitoredSystems(listener,
                packet1.getMonitoredSystems(),
                packet2.getMonitoredSystems(),
                "2017-02-25T14:56:50.053",
                "2017-02-25T14:56:52.513");

        String expected = "";
        expected += "10:15:30.000 Vehicle Composite Results of DM5:" + NL;
        expected += "+----------------------------+----------------+----------------+" + NL;
        expected += "|           Monitor          | Initial Status |  Last Status   |" + NL;
        expected += "|                            |   2017-02-25   |   2017-02-25   |" + NL;
        expected += "|                            |  14:56:50.053  |  14:56:52.513  |" + NL;
        expected += "+----------------------------+----------------+----------------+" + NL;
        expected += "|*A/C system refrigerant     |  Not Complete  |  Unsupported  *|" + NL;
        expected += "| Boost pressure control sys |      Complete  |      Complete  |" + NL;
        expected += "| Catalyst                   |  Not Complete  |  Not Complete  |" + NL;
        expected += "| Cold start aid system      |  Unsupported   |  Unsupported   |" + NL;
        expected += "|*Comprehensive component    |  Not Complete  |      Complete *|" + NL;
        expected += "| Diesel Particulate Filter  |      Complete  |      Complete  |" + NL;
        expected += "| EGR/VVT system             |  Unsupported   |  Unsupported   |" + NL;
        expected += "| Evaporative system         |  Not Complete  |  Not Complete  |" + NL;
        expected += "|*Exhaust Gas Sensor         |  Unsupported   |  Unsupported  *|" + NL;
        expected += "|*Exhaust Gas Sensor heater  |  Not Complete  |  Unsupported  *|" + NL;
        expected += "| Fuel System                |  Unsupported   |  Unsupported   |" + NL;
        expected += "| Heated catalyst            |  Unsupported   |  Unsupported   |" + NL;
        expected += "| Misfire                    |  Unsupported   |  Unsupported   |" + NL;
        expected += "| NMHC converting catalyst   |  Unsupported   |  Unsupported   |" + NL;
        expected += "| NOx catalyst/adsorber      |  Unsupported   |  Unsupported   |" + NL;
        expected += "| Secondary air system       |  Unsupported   |  Unsupported   |" + NL;
        expected += "+----------------------------+----------------+----------------+" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testReportPerformanceRatios() {
        int[] data1 = new int[] { 0x03, 0x01, 0x0C, 0x00, 0xC9, 0x14, 0xF8, 0x00, 0x00, 0x0C, 0x00, 0xF2, 0x0B, 0xF8,
                0x00, 0x00, 0x0C, 0x00, 0xC6, 0x14, 0xF8, 0x00, 0x00, 0x0C, 0x00, 0xEF, 0x0B, 0xF8, 0x00, 0x00, 0x0C,
                0x00, 0x81, 0x02, 0xF8, 0x01, 0x00, 0x02, 0x00, 0xB8, 0x12, 0xF8, 0x00, 0x00, 0x0B, 0x00, 0xF8, 0x0B,
                0xF8, 0x00, 0x00, 0x0C, 0x00, 0xF0, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xF5, 0x0B, 0xF8, 0x00, 0x00,
                0x00, 0x00, 0xEA, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xEE, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xF3,
                0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, };
        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(0xC200, 0x00, data1));
        int[] data2 = new int[] { 0x03, 0x01, 0x0D, 0x00, 0xC9, 0x14, 0xF8, 0x00, 0x00, 0x0D, 0x00, 0xF2, 0x0B, 0xF8,
                0x00, 0x00, 0x0D, 0x00, 0xC6, 0x14, 0xF8, 0x00, 0x00, 0x0D, 0x00, 0xEF, 0x0B, 0xF8, 0x00, 0x00, 0x0D,
                0x00, 0x81, 0x02, 0xF8, 0x01, 0x00, 0x02, 0x00, 0xB8, 0x12, 0xF8, 0x00, 0x00, 0x0B, 0x00, 0xF8, 0x0B,
                0xF8, 0x00, 0x00, 0x0D, 0x00, 0xF0, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xF5, 0x0B, 0xF8, 0x00, 0x00,
                0x00, 0x00, 0xEA, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xEE, 0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, 0xF3,
                0x0B, 0xF8, 0x00, 0x00, 0x00, 0x00, };

        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(0xC200, 0x00, data2));
        instance.reportPerformanceRatios(listener,
                packet1.getRatios(),
                packet2.getRatios(),
                1,
                1,
                3,
                4,
                "2017-02-25T14:56:50.053",
                "2017-02-25T14:56:52.513");
        String expected = "";
        expected += "10:15:30.000 Vehicle Composite Results of DM20:" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     |                                  |  Initial Status |   Last Status   |" + NL;
        expected += "|     |                                  |    2017-02-25   |    2017-02-25   |" + NL;
        expected += "|     |                                  |   14:56:50.053  |   14:56:52.513  |" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     | Ignition Cycles                  |               1 |              1  |" + NL;
        expected += "|*    | OBD Monitoring Conditions Count  |               3 |              4 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "| Src | Monitor                          |  Num'r |  Den'r |  Num'r |  Den'r |" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "|   0 |  641 Variable Geometry Turbocha..|      1 |      2 |     1  |     2  |" + NL;
        expected += "|   0 | 3050 Catalyst 1 Sys Mon          |      0 |      0 |     0  |     0  |" + NL;
        expected += "|   0 | 3054 2ndary Air Sys Mon          |      0 |      0 |     0  |     0  |" + NL;
        expected += "|*  0 | 3055 Fuel Sys Mon                |      0 |     12 |     0  |    13 *|" + NL;
        expected += "|   0 | 3056 N/O2 Exh Gas Snsr 1 Mon     |      0 |      0 |     0  |     0  |" + NL;
        expected += "|*  0 | 3058 EGR Sys Mon                 |      0 |     12 |     0  |    13 *|" + NL;
        expected += "|   0 | 3059 +Crankcase Vent Sys Mon     |      0 |      0 |     0  |     0  |" + NL;
        expected += "|   0 | 3061 Cold Start Strategy Sys Mon |      0 |      0 |     0  |     0  |" + NL;
        expected += "|*  0 | 3064 AFT DPF Sys Mon             |      0 |     12 |     0  |    13 *|" + NL;
        expected += "|   0 | 4792 AFT 1 SCR Sys               |      0 |     11 |     0  |    11  |" + NL;
        expected += "|*  0 | 5318 AFT Exh Gas Snsr Sys Mon    |      0 |     12 |     0  |    13 *|" + NL;
        expected += "|*  0 | 5321 Intake Manifold Press Sys ..|      0 |     12 |     0  |    13 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testReportPerformanceRatiosWithFewerInitialValues() {
        int[] data1 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x01, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00 };
        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(Packet.create(0, 0, data1));

        int[] data2 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x02, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00,
                // Three
                0xBC, 0x14, 0xF8, 0x06, 0x00, 0x06, 0x00 };
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(Packet.create(0, 0, data2));
        instance.reportPerformanceRatios(listener,
                packet1.getRatios(),
                packet2.getRatios(),
                1,
                1,
                3,
                4,
                "2017-02-25T14:56:50.053",
                "2017-02-25T14:56:52.513");

        String expected = "";
        expected += "10:15:30.000 Vehicle Composite Results of DM20:" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     |                                  |  Initial Status |   Last Status   |" + NL;
        expected += "|     |                                  |    2017-02-25   |    2017-02-25   |" + NL;
        expected += "|     |                                  |   14:56:50.053  |   14:56:52.513  |" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     | Ignition Cycles                  |               1 |              1  |" + NL;
        expected += "|*    | OBD Monitoring Conditions Count  |               3 |              4 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "| Src | Monitor                          |  Num'r |  Den'r |  Num'r |  Den'r |" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "|   0 | 4792 AFT 1 SCR Sys               |      3 |      4 |     3  |     4  |" + NL;
        expected += "|*  0 | 5322 AFT NMHC Converting Cataly..|      0 |      1 |     0  |     2 *|" + NL;
        expected += "|*  0 | 5308 AFT 1 NOx Adsorber Catalys..|     -1 |     -1 |     6* |     6 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;

        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testReportPerformanceRatiosWithMoreInitialValues() {
        int[] data1 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x01, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00,
                // Three
                0xBC, 0x14, 0xF8, 0x05, 0x00, 0x06, 0x00 };
        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(Packet.create(0, 0, data1));

        int[] data2 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x02, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00 };
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(Packet.create(0, 0, data2));
        instance.reportPerformanceRatios(listener,
                packet1.getRatios(),
                packet2.getRatios(),
                1,
                1,
                3,
                4,
                "2017-02-25T14:56:50.053",
                "2017-02-25T14:56:52.513");

        String expected = "";
        expected += "10:15:30.000 Vehicle Composite Results of DM20:" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     |                                  |  Initial Status |   Last Status   |" + NL;
        expected += "|     |                                  |    2017-02-25   |    2017-02-25   |" + NL;
        expected += "|     |                                  |   14:56:50.053  |   14:56:52.513  |" + NL;
        expected += "+-----+----------------------------------+-----------------+-----------------+" + NL;
        expected += "|     | Ignition Cycles                  |               1 |              1  |" + NL;
        expected += "|*    | OBD Monitoring Conditions Count  |               3 |              4 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "| Src | Monitor                          |  Num'r |  Den'r |  Num'r |  Den'r |" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;
        expected += "|   0 | 4792 AFT 1 SCR Sys               |      3 |      4 |     3  |     4  |" + NL;
        expected += "|*  0 | 5308 AFT 1 NOx Adsorber Catalys..|      5 |      6 |    -1* |    -1 *|" + NL;
        expected += "|*  0 | 5322 AFT NMHC Converting Cataly..|      0 |      1 |     0  |     2 *|" + NL;
        expected += "+-----+----------------------------------+--------+--------+--------+--------+" + NL;

        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testRequestDM21PacketsFalse() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10000 11 22 33 44 55 66 77 88" + NL;
        expected += "10:15:30.000 18C10017 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 70 80" + NL;

        instance.requestDM21Packets(listener, false);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testRequestDM21PacketsNoResponse() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.empty())
                .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.requestDM21Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(3))
                .requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testRequestDM21PacketsTrue() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10000 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10017 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.requestDM21Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testRequestDM21PacketsWithEngine1Response() {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM21 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.000 18C10001 11 22 33 44 55 66 77 88" + NL;
        expected += "DM21 from Engine #2 (1): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,721 km (5,418.978 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    26,197 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  17,459 km (10,848.52 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      34,935 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10017 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL;
        expected += "10:15:30.000 18C10021 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;

        instance.requestDM21Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestRaw(DM21DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testRequestDM5() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, GLOBAL_ADDR)).thenReturn(requestPacket);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket))
                .thenReturn(Stream.of(packet1, packet2, packet3).map(p -> new Either<>(p, null)));

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "10:15:30.000 18FECE00 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #1 (0): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "10:15:30.000 18FECE17 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "10:15:30.000 18FECE21 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;
        expected += NL;
        expected += "Vehicle Composite of DM5:" + NL;
        expected += "A/C system refrigerant         supported, not complete" + NL;
        expected += "Boost pressure control sys     supported,     complete" + NL;
        expected += "Catalyst                       supported, not complete" + NL;
        expected += "Cold start aid system      not supported,     complete" + NL;
        expected += "Comprehensive component        supported, not complete" + NL;
        expected += "Diesel Particulate Filter      supported,     complete" + NL;
        expected += "EGR/VVT system             not supported,     complete" + NL;
        expected += "Evaporative system             supported, not complete" + NL;
        expected += "Exhaust Gas Sensor         not supported, not complete" + NL;
        expected += "Exhaust Gas Sensor heater      supported, not complete" + NL;
        expected += "Fuel System                not supported,     complete" + NL;
        expected += "Heated catalyst            not supported, not complete" + NL;
        expected += "Misfire                    not supported,     complete" + NL;
        expected += "NMHC converting catalyst   not supported,     complete" + NL;
        expected += "NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "Secondary air system       not supported,     complete" + NL;

        instance.requestDM5Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).requestRaw(DM5DiagnosticReadinessPacket.class, requestPacket);
    }

    @Test
    public void testRequestDM5WithNoResponses() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        when(j1939.createRequestPacket(pgn, 0xFF)).thenReturn(requestPacket);
        when(j1939.requestRaw(DM5DiagnosticReadinessPacket.class,
                requestPacket))
                        .thenReturn(Stream.empty())
                        .thenReturn(Stream.empty()).thenReturn(Stream.empty());

        String expected = "";
        expected += "10:15:30.000 Global DM5 Request" + NL;
        expected += "10:15:30.000 18EAFFA5 CE FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        instance.requestDM5Packets(listener, true);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(3))
                .requestRaw(DM5DiagnosticReadinessPacket.class,
                        requestPacket);
    }
}
