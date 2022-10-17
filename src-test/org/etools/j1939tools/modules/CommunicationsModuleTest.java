/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.modules;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939tools.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.bus.TestResultsListener;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for the {@link CommunicationsModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class CommunicationsModuleTest {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static final int REQUEST_PGN = 0xEA00;
    public static final int ACK_PGN = AcknowledgmentPacket.PGN;
    /**
     * The Bus address of the tool for testing purposes
     */
    private static final int BUS_ADDR = 0xA5;
    private CommunicationsModule instance;

    private static final CommunicationsListener NOOP = x -> {
    };

    private static final long TIMEOUT = 750;

    private TestResultsListener listener;

    @Spy
    private J1939 j1939;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener();
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new CommunicationsModule();
        instance.setJ1939(j1939);
    }

    private static MonitoredSystemStatus getStatus(boolean enabled, boolean complete) {
        return findStatus(true, enabled, complete);
    }

    @Test
    public void testReportCalibrationInformation() throws BusException {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);
        byte[] calBytes2 = "EFGH1234567890123456".getBytes(UTF8);
        byte[] calBytes3 = "IJKL1234567890123456".getBytes(UTF8);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                                                                                        Packet.create(pgn,
                                                                                                      0x00,
                                                                                                      calBytes1));
        DM19CalibrationInformationPacket packet2 = new DM19CalibrationInformationPacket(
                                                                                        Packet.create(pgn,
                                                                                                      0x17,
                                                                                                      calBytes2));
        DM19CalibrationInformationPacket packet3 = new DM19CalibrationInformationPacket(
                                                                                        Packet.create(pgn,
                                                                                                      0x21,
                                                                                                      calBytes3));

        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket()),
                 Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket()),
                 Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(eq(TIMEOUT),
                                                                                                eq(MILLISECONDS));

        RequestResult<DM19CalibrationInformationPacket> expected = RequestResult.of(packet1, packet2, packet3);
        RequestResult<DM19CalibrationInformationPacket> actual = instance.requestDM19(NOOP);
        assertEquals(expected, actual);

        verify(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));
    }

    @Test
    public void testReadDM1() throws BusException {
        DM1ActiveDTCsPacket packet = new DM1ActiveDTCsPacket(
                                                             Packet.create(65226,
                                                                           0x00,
                                                                           0x11,
                                                                           0x01,
                                                                           0x61,
                                                                           0x02,
                                                                           0x13,
                                                                           0x00,
                                                                           0x21,
                                                                           0x06,
                                                                           0x1F,
                                                                           0x00,
                                                                           0xEE,
                                                                           0x10,
                                                                           0x04,
                                                                           0x00));

        doReturn(Stream.of(packet.getPacket(), packet.getPacket(), packet.getPacket())).when(j1939)
                                                                                       .read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();

        assertEquals(List.of(packet), instance.read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS, listener));

        String expected = NL;
        expected += "10:15:30.0000 Reading the bus for published DM1 messages" + NL;
        expected += "10:15:30.0000 18FECA00 [14] 11 01 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #1 (0): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;

        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.PGN, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReadDM1WithEmptyResponse() throws BusException {
        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();

        assertEquals(List.of(), instance.read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS, listener));

        String expected = NL + "10:15:30.0000 Reading the bus for published DM1 messages" + NL;

        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.PGN, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReadDM1WithTwoModules() throws BusException {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                                                              Packet.create(65226,
                                                                            0x00,
                                                                            0x11,
                                                                            0x01,
                                                                            0x61,
                                                                            0x02,
                                                                            0x13,
                                                                            0x00,
                                                                            0x21,
                                                                            0x06,
                                                                            0x1F,
                                                                            0x00,
                                                                            0xEE,
                                                                            0x10,
                                                                            0x04,
                                                                            0x00));

        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                                                              Packet.create(65226,
                                                                            0x01,
                                                                            0x11,
                                                                            0x01,
                                                                            0x61,
                                                                            0x02,
                                                                            0x13,
                                                                            0x00,
                                                                            0x21,
                                                                            0x06,
                                                                            0x1F,
                                                                            0x00,
                                                                            0xEE,
                                                                            0x10,
                                                                            0x04,
                                                                            0x00));

        doReturn(Stream.of(packet1.getPacket(),
                           packet2.getPacket(),
                           packet1.getPacket(),
                           packet2.getPacket(),
                           packet1.getPacket(),
                           packet2.getPacket())).when(j1939)
                                                .read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();

        assertEquals(List.of(packet1, packet2),
                     instance.read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS, listener));

        String expected = NL + "10:15:30.0000 Reading the bus for published DM1 messages" + NL;
        expected += "10:15:30.0000 18FECA00 [14] 11 01 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #1 (0): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += NL;
        expected += "10:15:30.0000 18FECA01 [14] 11 01 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #2 (1): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.PGN, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReportCalibrationInformationWithAddress() throws BusException {
        final int pgn = DM19CalibrationInformationPacket.PGN;
        byte[] calBytes1 = "ABCD1234567890123456".getBytes(UTF8);

        DM19CalibrationInformationPacket packet1 = new DM19CalibrationInformationPacket(
                                                                                        Packet.create(pgn,
                                                                                                      0x00,
                                                                                                      calBytes1));

        doReturn(Stream.of(packet1.getPacket()),
                 Stream.of((packet1.getPacket()),
                           Stream.of(packet1.getPacket()))).when(j1939)
                                                           .read(eq(TIMEOUT), eq(MILLISECONDS));

        BusResult<DM19CalibrationInformationPacket> expected = new BusResult<>(false, packet1);
        BusResult<DM19CalibrationInformationPacket> actual = instance.requestDM19(NOOP, 0x00);
        assertEquals(expected, actual);

        verify(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));
    }

    @Test
    public void testDsReportCalibrationInformationNoResponse() throws BusException {
        doReturn(Stream.empty(),
                 Stream.empty(),
                 Stream.empty()).when(j1939)
                                .read(eq(TIMEOUT), eq(MILLISECONDS));

        BusResult<DM19CalibrationInformationPacket> expected = new BusResult<>(false);
        BusResult<DM19CalibrationInformationPacket> actual = instance.requestDM19(NOOP, 0x00);
        assertEquals(expected, actual);

        verify(j1939).read(eq(TIMEOUT),
                           eq(MILLISECONDS));
    }

    @Test
    public void testGlobalReportCalibrationInformationNoResponses() throws BusException {
        doReturn(Stream.empty(),
                 Stream.empty(),
                 Stream.empty()).when(j1939)
                                .read(eq(TIMEOUT), eq(MILLISECONDS));

        RequestResult<DM19CalibrationInformationPacket> expected = new RequestResult<>(false);
        RequestResult<DM19CalibrationInformationPacket> actual = instance.requestDM19(NOOP);
        assertEquals(expected, actual);

        verify(j1939).read(eq(TIMEOUT),
                           eq(MILLISECONDS));
    }

    @Test
    public void testReportComponentIdentification() {
        final int pgn = ComponentIdentificationPacket.PGN;
        byte[] bytes1 = "Make1*Model1*SerialNumber1**".getBytes(UTF8);
        byte[] bytes2 = "****".getBytes(UTF8);
        byte[] bytes3 = "Make3*Model3***".getBytes(UTF8);

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);

        ComponentIdentificationPacket packet1 = new ComponentIdentificationPacket(Packet.create(pgn, 0x00, bytes1));
        ComponentIdentificationPacket packet2 = new ComponentIdentificationPacket(Packet.create(pgn, 0x17, bytes2));
        ComponentIdentificationPacket packet3 = new ComponentIdentificationPacket(Packet.create(pgn, 0x21, bytes3));
        doReturn(new RequestResult<>(false,
                                     packet1,
                                     packet2,
                                     packet3))
                                              .when(j1939)
                                              .requestGlobal(anyString(), eq(pgn), eq(requestPacket), eq(NOOP));

        instance.request(ComponentIdentificationPacket.class, NOOP);

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).requestGlobal(eq("Component Identification"),
                                    eq(ComponentIdentificationPacket.PGN),
                                    any(),
                                    eq(NOOP));
    }

    @Test
    public void testReportVin() {
        final int pgn = VehicleIdentificationPacket.PGN;
        byte[] vinBytes = "12345678901234567890*".getBytes(UTF8);

        VehicleIdentificationPacket packet1 = new VehicleIdentificationPacket(Packet.create(pgn, 0x00, vinBytes));
        VehicleIdentificationPacket packet2 = new VehicleIdentificationPacket(Packet.create(pgn, 0x17, vinBytes));
        VehicleIdentificationPacket packet3 = new VehicleIdentificationPacket(Packet.create(pgn, 0x21, vinBytes));
        doReturn(new RequestResult<>(false, packet1, packet2, packet3))
                                                                       .when(j1939)
                                                                       .requestGlobal(anyString(),
                                                                                      eq(VehicleIdentificationPacket.PGN),
                                                                                      Mockito.any(),
                                                                                      eq(NOOP));

        List<VehicleIdentificationPacket> packets = instance.request(VehicleIdentificationPacket.class,
                                                                     NOOP)
                                                            .stream()
                                                            .map(p -> new VehicleIdentificationPacket(p.getPacket()))
                                                            .collect(Collectors.toList());
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));

        verify(j1939).requestGlobal(eq("Vehicle Identification"), eq(VehicleIdentificationPacket.PGN), any(), eq(NOOP));
    }

    @Test
    public void testReportComponentIdentificationWithNoResponse() throws BusException {
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));

        List<ComponentIdentificationPacket> expected = Collections.emptyList();
        List<? extends ComponentIdentificationPacket> actual = instance.request(ComponentIdentificationPacket.class,
                                                                                NOOP);
        assertEquals(expected, actual);

        verify(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));
    }

    @Test
    public void testReportVinWithNoResponses() throws BusException {

        final int pgn = VehicleIdentificationPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Global Vehicle Identification Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] EC FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        List<VehicleIdentificationPacket> packets = instance.request(VehicleIdentificationPacket.class,
                                                                     listener)
                                                            .stream()
                                                            .map(p -> {
                                                                return new VehicleIdentificationPacket(p.getPacket());
                                                            })
                                                            .collect(
                                                                     Collectors.toList());
        assertEquals(0, packets.size());
        assertEquals(expected, listener.getResults());
        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket1 = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket1).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                                                                Packet.create(ACK_PGN | BUS_ADDR,
                                                                              0x00,
                                                                              0x00,
                                                                              0xFF,
                                                                              0xFF,
                                                                              0xFF,
                                                                              BUS_ADDR,
                                                                              0xD3,
                                                                              0xFE,
                                                                              0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8A500 [8] 00 FF FF FF A5 D3 FE 00" + NL;
        expected += "Acknowledgment from Engine #1 (0): Response: ACK, Group Function: 255, Address Acknowledged: 165, PGN Requested: 65235"
                + NL + NL;

        TestResultsListener listener = new TestResultsListener();

        assertEquals(List.of(packet1), instance.requestDM11(listener, 600, MILLISECONDS));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11DS() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket1 = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket1).when(j1939).createRequestPacket(pgn, 0);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                                                                Packet.create(ACK_PGN | BUS_ADDR,
                                                                              0x00,
                                                                              0x00,
                                                                              0xFF,
                                                                              0xFF,
                                                                              0xFF,
                                                                              BUS_ADDR,
                                                                              0xD3,
                                                                              0xFE,
                                                                              0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = NL;
        expected += "10:15:30.0000 Destination Specific DM11 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8A500 [8] 00 FF FF FF A5 D3 FE 00" + NL;
        expected += "Acknowledgment from Engine #1 (0): Response: ACK, Group Function: 255, Address Acknowledged: 165, PGN Requested: 65235"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(List.of(packet1), instance.requestDM11(listener, 0));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecific() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(pgn,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM12 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();

        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM12(listener, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(Packet.create(pgn,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x61,
                                                                                          0x02,
                                                                                          0x13,
                                                                                          0x00,
                                                                                          0x21,
                                                                                          0x06,
                                                                                          0x1F,
                                                                                          0x00,
                                                                                          0xEE,
                                                                                          0x10,
                                                                                          0x04,
                                                                                          0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM12 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);

        assertEquals(expectedResult, instance.requestDM12(listener, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM12 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM12(listener, 0x17));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12Global() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(pgn,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        DM12MILOnEmissionDTCPacket packet2 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(pgn,
                                                                                          0x17,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(pgn,
                                                                                          0x21,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        TestResultsListener listener = new TestResultsListener();
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM12 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FED417 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FED421 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;

        List<DM12MILOnEmissionDTCPacket> expectedPackets = List.of(packet1, packet2, packet3);
        RequestResult<DM12MILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                                                                                       expectedPackets,
                                                                                       List.of());
        assertEquals(expectedResult, instance.requestDM12(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12GlobalWithDTCs() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(Packet.create(pgn,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x61,
                                                                                          0x02,
                                                                                          0x13,
                                                                                          0x00,
                                                                                          0x21,
                                                                                          0x06,
                                                                                          0x1F,
                                                                                          0x00,
                                                                                          0xEE,
                                                                                          0x10,
                                                                                          0x04,
                                                                                          0x00));
        TestResultsListener listener = new TestResultsListener();
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM12 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;

        RequestResult<DM12MILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                                                                                       List.of(packet1),
                                                                                       List.of());

        assertEquals(expectedResult, instance.requestDM12(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12GlobalWithNoResponses() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        TestResultsListener listener = new TestResultsListener();
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM12 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        assertEquals(RequestResult.empty(false), instance.requestDM12(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM21 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM21DiagnosticReadinessPacket> result = new BusResult<>(false);

        assertEquals(result, instance.requestDM21(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21DestinationSpecificResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM21DiagnosticReadinessPacket> result = new BusResult<>(false, packet1);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM21 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 [8] 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL;

        assertEquals(result, instance.requestDM21(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21GlobalNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                                                                                  List.of(packet1),
                                                                                  List.of());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 [8] 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL + NL;

        assertEquals(result, instance.requestDM21(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21GlobalResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                                                                                  List.of(packet1),
                                                                                  List.of());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 [8] 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL;
        expected += NL;

        assertEquals(result, instance.requestDM21(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecific() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(
                                                                                                Packet.create(pgn,
                                                                                                              0x21,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM23 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB521 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false,
                                                                                         packet1);
        assertEquals(expectedResult, instance.requestDM23(listener, 0x21));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(Packet.create(pgn,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x61,
                                                                                                              0x02,
                                                                                                              0x13,
                                                                                                              0x00,
                                                                                                              0x21,
                                                                                                              0x06,
                                                                                                              0x1F,
                                                                                                              0x00,
                                                                                                              0xEE,
                                                                                                              0x10,
                                                                                                              0x04,
                                                                                                              0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM23 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM23(listener, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);
        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM23 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM23(listener, 0x17));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23Global() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(
                                                                                                Packet.create(pgn,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        DM23PreviouslyMILOnEmissionDTCPacket packet2 = new DM23PreviouslyMILOnEmissionDTCPacket(
                                                                                                Packet.create(pgn,
                                                                                                              0x17,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        DM23PreviouslyMILOnEmissionDTCPacket packet3 = new DM23PreviouslyMILOnEmissionDTCPacket(
                                                                                                Packet.create(pgn,
                                                                                                              0x21,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM23 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FDB517 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FDB521 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                                                                                                 List.of(packet1,
                                                                                                         packet2,
                                                                                                         packet3),
                                                                                                 List.of());
        assertEquals(expectedResult, instance.requestDM23(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23GlobalWithDTCs() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(Packet.create(pgn,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x61,
                                                                                                              0x02,
                                                                                                              0x13,
                                                                                                              0x00,
                                                                                                              0x21,
                                                                                                              0x06,
                                                                                                              0x1F,
                                                                                                              0x00,
                                                                                                              0xEE,
                                                                                                              0x10,
                                                                                                              0x04,
                                                                                                              0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM23 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                                                                                                 List.of(packet1),
                                                                                                 List.of());
        assertEquals(expectedResult, instance.requestDM23(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23GlobalWithNoResponses() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM23 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM23(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificNackOnly() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                                                                Packet.create(ACK_PGN | GLOBAL_ADDR,
                                                                              0x00,
                                                                              0x01,
                                                                              0xFF,
                                                                              0xFF,
                                                                              0xFF,
                                                                              BUS_ADDR,
                                                                              0xB7,
                                                                              0xFD,
                                                                              0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] B7 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8FF00 [8] 01 FF FF FF A5 B7 FD 00" + NL;
        expected += "Acknowledgment from Engine #1 (0): Response: NACK, Group Function: 255, Address Acknowledged: 165, PGN Requested: 64951"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());
        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] B7 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificWithResponse() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        int[] realData = new int[] {
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA };

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(pgn, 0x00, realData));
        doReturn(Stream.of(packet.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] B7 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB700 [87] 56 9D 00 07 7F 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "DM25 from Engine #1 (0): " + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "]" + NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, packet);
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecific() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(pgn,
                                                                                                        0x21,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM26 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB821 [8] 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not enabled,     complete" + NL;
        expected += "    Fuel System                not enabled,     complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not enabled,     complete" + NL;
        expected += "    Boost pressure control sys not enabled,     complete" + NL;
        expected += "    Catalyst                   not enabled,     complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expected += "    Heated catalyst            not enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled,     complete" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(false,
                                                                                              List.of(packet1),
                                                                                              List.of());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x21));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(Packet.create(pgn,
                                                                                                        0x00,
                                                                                                        0x00,
                                                                                                        0xFF,
                                                                                                        0x61,
                                                                                                        0x02,
                                                                                                        0x13,
                                                                                                        0x00,
                                                                                                        0x21,
                                                                                                        0x06,
                                                                                                        0x1F,
                                                                                                        0x00,
                                                                                                        0xEE,
                                                                                                        0x10,
                                                                                                        0x04,
                                                                                                        0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM26 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB800 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 97, Time Since Engine Start: not available" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not enabled,     complete" + NL;
        expected += "    Fuel System                    enabled,     complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         enabled,     complete" + NL;
        expected += "    Boost pressure control sys not enabled, not complete" + NL;
        expected += "    Catalyst                       enabled, not complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled, not complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled,     complete" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(
                                                                                              false,
                                                                                              List.of(packet1),
                                                                                              List.of());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM26 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(false,
                                                                                              List.of(),
                                                                                              List.of());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x17));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26Global() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(pgn,
                                                                                                        0x00,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(pgn,
                                                                                                        0x17,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(pgn,
                                                                                                        0x21,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0,
                                                                                                        0));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB800 [8] 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not enabled,     complete" + NL;
        expected += "    Fuel System                not enabled,     complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not enabled,     complete" + NL;
        expected += "    Boost pressure control sys not enabled,     complete" + NL;
        expected += "    Catalyst                   not enabled,     complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expected += "    Heated catalyst            not enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled,     complete" + NL + NL;

        expected += "10:15:30.0000 18FDB817 [8] 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Instrument Cluster #1 (23): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not enabled,     complete" + NL;
        expected += "    Fuel System                not enabled,     complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not enabled,     complete" + NL;
        expected += "    Boost pressure control sys not enabled,     complete" + NL;
        expected += "    Catalyst                   not enabled,     complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expected += "    Heated catalyst            not enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled,     complete" + NL + NL;

        expected += "10:15:30.0000 18FDB821 [8] 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not enabled,     complete" + NL;
        expected += "    Fuel System                not enabled,     complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not enabled,     complete" + NL;
        expected += "    Boost pressure control sys not enabled,     complete" + NL;
        expected += "    Catalyst                   not enabled,     complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expected += "    Heated catalyst            not enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled,     complete" + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(
                                                                                              false,
                                                                                              List.of(packet1,
                                                                                                      packet2,
                                                                                                      packet3),
                                                                                              List.of());
        assertEquals(expectedResult, instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26GlobalWithDTCs() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(Packet.create(pgn,
                                                                                                        0x11,
                                                                                                        0x22,
                                                                                                        0x33,
                                                                                                        0x44,
                                                                                                        0x55,
                                                                                                        0x66,
                                                                                                        0x77,
                                                                                                        0x88,
                                                                                                        0xFF));

        doReturn(Stream.of(packet1.getPacket()), Stream.of(packet1.getPacket()), Stream.of(packet1.getPacket())).when(
                                                                                                                      j1939)
                                                                                                                .read(anyLong(),
                                                                                                                      any());

        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB811 [8] 22 33 44 55 66 77 88 FF" + NL;
        expected += "DM26 from Cruise Control (17): Warm-ups: 68, Time Since Engine Start: 13,090 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        enabled, not complete" + NL;
        expected += "    Fuel System                not enabled,     complete" + NL;
        expected += "    Misfire                        enabled, not complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not enabled,     complete" + NL;
        expected += "    Boost pressure control sys     enabled, not complete" + NL;
        expected += "    Catalyst                   not enabled,     complete" + NL;
        expected += "    Cold start aid system          enabled, not complete" + NL;
        expected += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expected += "    EGR/VVT system             not enabled, not complete" + NL;
        expected += "    Evaporative system             enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater      enabled,     complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    NMHC converting catalyst       enabled, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "    Secondary air system       not enabled, not complete" + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26GlobalWithNoResponses() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());
        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecific() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(pgn,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM27 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();

        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM27(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(pgn,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x61,
                                                                                      0x02,
                                                                                      0x13,
                                                                                      0x00,
                                                                                      0x21,
                                                                                      0x06,
                                                                                      0x1F,
                                                                                      0x00,
                                                                                      0xEE,
                                                                                      0x10,
                                                                                      0x04,
                                                                                      0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM27 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, packet1);

        assertEquals(expectedResult, instance.requestDM27(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM27 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM27(listener, 0x17));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27Global() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(pgn,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00));
        DM27AllPendingDTCsPacket packet2 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(pgn,
                                                                                      0x17,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00));
        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(pgn,
                                                                                      0x21,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM27 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += NL;
        expected += "10:15:30.0000 18FD8217 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += NL;
        expected += "10:15:30.0000 18FD8221 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM27AllPendingDTCsPacket> expectedPackets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        RequestResult<DM27AllPendingDTCsPacket> expectedResult = new RequestResult<>(false,
                                                                                     expectedPackets,
                                                                                     List.of());
        assertEquals(expectedResult, instance.requestDM27(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27GlobalWithDTCs() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(pgn,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x61,
                                                                                      0x02,
                                                                                      0x13,
                                                                                      0x00,
                                                                                      0x21,
                                                                                      0x06,
                                                                                      0x1F,
                                                                                      0x00,
                                                                                      0xEE,
                                                                                      0x10,
                                                                                      0x04,
                                                                                      0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM27 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM27AllPendingDTCsPacket> expectedResult = new RequestResult<>(false,
                                                                                     List.of(packet1),
                                                                                     List.of());

        assertEquals(expectedResult, instance.requestDM27(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27GlobalWithNoResponses() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM27 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM27(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM28GlobalWithDTCs() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM28PermanentEmissionDTCPacket packet1 = new DM28PermanentEmissionDTCPacket(Packet.create(pgn,
                                                                                                  0x00,
                                                                                                  0x00,
                                                                                                  0xFF,
                                                                                                  0x61,
                                                                                                  0x02,
                                                                                                  0x13,
                                                                                                  0x00,
                                                                                                  0x21,
                                                                                                  0x06,
                                                                                                  0x1F,
                                                                                                  0x00,
                                                                                                  0xEE,
                                                                                                  0x10,
                                                                                                  0x04,
                                                                                                  0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM28 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 80 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8000 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM28 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                                                                                           List.of(packet1),
                                                                                           List.of());
        assertEquals(expectedResult, instance.requestDM28(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM29 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response"
                + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(BusResult.empty(), instance.requestDM29(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29DestinationSpecificResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM29 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 189EA500 [8] 09 20 47 31 01 FF FF FF" + NL;
        expected += "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                               9" + NL;
        expected += "All Pending DTC Count                                           32" + NL;
        expected += "Emission-Related MIL-On DTC Count                               71" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count                    49" + NL;
        expected += "Emission-Related Permanent DTC Count                             1" + NL;

        DM29DtcCounts packet1 = new DM29DtcCounts(
                                                  Packet.create(pgn | BUS_ADDR,
                                                                0x00,
                                                                0x09,
                                                                0x20,
                                                                0x47,
                                                                0x31,
                                                                0x01,
                                                                0xFF,
                                                                0xFF,
                                                                0xFF));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM29DtcCounts> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM29(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29GlobalNoResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;

        String expected = "";
        expected += "10:15:30.0000 Global DM29 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM29(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29GlobalResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;

        String expected = "";
        expected += "10:15:30.0000 Global DM29 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 189EFF00 [8] 09 20 47 31 01 FF FF FF" + NL;
        expected += "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                               9" + NL;
        expected += "All Pending DTC Count                                           32" + NL;
        expected += "Emission-Related MIL-On DTC Count                               71" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count                    49" + NL;
        expected += "Emission-Related Permanent DTC Count                             1" + NL;
        expected += NL;

        DM29DtcCounts packet1 = new DM29DtcCounts(
                                                  Packet.create(pgn | GLOBAL_ADDR,
                                                                0x00,
                                                                0x09,
                                                                0x20,
                                                                0x47,
                                                                0x31,
                                                                0x01,
                                                                0xFF,
                                                                0xFF,
                                                                0xFF));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM29DtcCounts> expectedResult = new RequestResult<>(false,
                                                                          List.of(packet1),
                                                                          List.of());
        assertEquals(expectedResult, instance.requestDM29(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM2 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertTrue(instance.requestDM2(listener, 0x17).getPacket().isEmpty());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testRequestDM2DestinationSpecificWithEngine1Response() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | 0x01, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x01);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                                                                    Packet.create(pgn,
                                                                                  0x01,
                                                                                  0x22,
                                                                                  0xDD,
                                                                                  0x33,
                                                                                  0x44,
                                                                                  0x55,
                                                                                  0x66,
                                                                                  0x77,
                                                                                  0x88));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM2 Request to Engine #2 (1)" + NL;
        expected += "10:15:30.0000 18EA01A5 [3] CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB01 [8] 22 DD 33 44 55 66 77 88" + NL;
        expected += "DM2 from Engine #2 (1): MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 148531:21 - Unknown, Data Drifted Low - 102 times" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM2PreviouslyActiveDTC> busResult = instance.requestDM2(listener, 0x01);
        assertTrue(busResult.getPacket().isPresent());
        assertTrue(busResult.getPacket().get().left.isPresent());
        assertEquals(packet1, busResult.getPacket().get().left.get());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x01);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalFullStringTrue() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                                                                    Packet.create(pgn,
                                                                                  0x00,
                                                                                  0x22,
                                                                                  0xDD,
                                                                                  0x33,
                                                                                  0x44,
                                                                                  0x55,
                                                                                  0x66,
                                                                                  0x77,
                                                                                  0x88));
        DM2PreviouslyActiveDTC packet2 = new DM2PreviouslyActiveDTC(
                                                                    Packet.create(pgn,
                                                                                  0x17,
                                                                                  0x02,
                                                                                  0xFD,
                                                                                  0x03,
                                                                                  0x04,
                                                                                  0x05,
                                                                                  0x06,
                                                                                  0x07,
                                                                                  0x08));
        DM2PreviouslyActiveDTC packet3 = new DM2PreviouslyActiveDTC(
                                                                    Packet.create(pgn,
                                                                                  0x21,
                                                                                  0x20,
                                                                                  0xDF,
                                                                                  0x30,
                                                                                  0x40,
                                                                                  0x50,
                                                                                  0x60,
                                                                                  0x70,
                                                                                  0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB00 [8] 22 DD 33 44 55 66 77 88" + NL;
        expected += "DM2 from Engine #1 (0): MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 148531:21 - Unknown, Data Drifted Low - 102 times" + NL + NL;
        expected += "10:15:30.0000 18FECB17 [8] 02 FD 03 04 05 06 07 08" + NL;
        expected += "DM2 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: other" + NL;
        expected += "DTC 1027:5 - Trip Time in Derate by Engine, Current Below Normal Or Open Circuit - 6 times" + NL
                + NL;
        expected += "10:15:30.0000 18FECB21 [8] 20 DF 30 40 50 60 70 80" + NL;
        expected += "DM2 from Body Controller (33): MIL: off, RSL: other, AWL: off, PL: off" + NL;
        expected += "DTC 147504:16 - Unknown, Data Valid But Above Normal Operating Range - Moderately Severe Level - 96 times"
                + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM2PreviouslyActiveDTC> expectedPackets = List.of(packet1, packet2, packet3);
        assertEquals(expectedPackets, instance.requestDM2(listener).getPackets());

        assertEquals("", listener.getMessages());
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalWithDTCs() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(Packet.create(pgn,
                                                                                  0x00,
                                                                                  0x00,
                                                                                  0xFF,
                                                                                  0x61,
                                                                                  0x02,
                                                                                  0x13,
                                                                                  0x00,
                                                                                  0x21,
                                                                                  0x06,
                                                                                  0x1F,
                                                                                  0x00,
                                                                                  0xEE,
                                                                                  0x10,
                                                                                  0x04,
                                                                                  0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB00 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM2 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times";
        expected += NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();

        RequestResult<DM2PreviouslyActiveDTC> expectedResult = new RequestResult<>(false,
                                                                                   List.of(packet1),
                                                                                   List.of());
        assertEquals(expectedResult, instance.requestDM2(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalWithNoResponses() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(new ArrayList<DM2PreviouslyActiveDTC>(), instance.requestDM2(listener).getPackets());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM31 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM31DtcToLampAssociation> expectedResult = new RequestResult<>(false,
                                                                                     List.of(),
                                                                                     List.of());
        assertEquals(expectedResult, instance.requestDM31(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31DestinationSpecificResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM31 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 [3] 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 18A3A521 [18] 61 02 13 81 62 1D 21 06 1F 23 22 DD EE 10 04 00 AA 55" + NL;
        expected += "DM31 from Body Controller (33): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "]" + NL;

        DM31DtcToLampAssociation packet1 = new DM31DtcToLampAssociation(
                                                                        Packet.create(pgn | BUS_ADDR,
                                                                                      0x21,
                                                                                      0x61, // SPN least significant bit
                                                                                      0x02, // SPN most significant bit
                                                                                      0x13, // Failure mode indicator
                                                                                      0x81, // SPN Conversion Occurrence
                                                                                      // Count
                                                                                      0x62, // Lamp Status/Support
                                                                                      0x1D, // Lamp Status/State

                                                                                      0x21, // SPN least significant bit
                                                                                      0x06, // SPN most significant bit
                                                                                      0x1F, // Failure mode indicator
                                                                                      0x23, // SPN Conversion Occurrence
                                                                                      // Count
                                                                                      0x22, // Lamp Status/Support
                                                                                      0xDD, // Lamp Status/State

                                                                                      0xEE, // SPN least significant bit
                                                                                      0x10, // SPN most significant bit
                                                                                      0x04, // Failure mode indicator
                                                                                      0x00, // SPN Conversion Occurrence
                                                                                      // Count
                                                                                      0xAA, // Lamp Status/Support
                                                                                      0x55));// Lamp Status/State
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM31DtcToLampAssociation> expectedResult = new RequestResult<>(false,
                                                                                     List.of(packet1),
                                                                                     List.of());
        assertEquals(expectedResult, instance.requestDM31(listener, 0x21));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31GlobalNoResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;

        String expected = "";
        expected += "10:15:30.0000 Global DM31 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM31(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31GlobalResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;

        String expected = "";
        expected += "10:15:30.0000 Global DM31 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 18A30021 [18] 61 02 13 81 62 1D 21 06 1F 23 22 DD EE 10 04 00 AA 55" + NL;
        expected += "DM31 from Body Controller (33): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "]" + NL;
        expected += NL;

        DM31DtcToLampAssociation packet1 = new DM31DtcToLampAssociation(
                                                                        Packet.create(pgn,
                                                                                      0x21,
                                                                                      0x61, // SPN least significant bit
                                                                                      0x02, // SPN most significant bit
                                                                                      0x13, // Failure mode indicator
                                                                                      0x81,
                                                                                      0x62,
                                                                                      0x1D,

                                                                                      0x21, // SPN least significant bit
                                                                                      0x06, // SPN most significant bit
                                                                                      0x1F, // Failure mode indicator
                                                                                      0x23,
                                                                                      0x22,
                                                                                      0xDD,

                                                                                      0xEE, // SPN least significant bit
                                                                                      0x10, // SPN most significant bit
                                                                                      0x04, // Failure mode indicator
                                                                                      0x00,
                                                                                      0xAA,
                                                                                      0x55));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM31DtcToLampAssociation> expectedResult = new RequestResult<>(false,
                                                                                     List.of(packet1),
                                                                                     List.of());
        assertEquals(expectedResult, instance.requestDM31(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33DestinationSpecificEmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime()
                                                                                                               throws BusException {
        final int pgn = DM33EmissionIncreasingAECDActiveTime.PGN;

        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        var packet1 = new DM33EmissionIncreasingAECDActiveTime(Packet.create(pgn, 0x00, data));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(new RequestResult<>(false, packet1), instance.requestDM33(listener));

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 18A10000 [36] 01 2B 0B 01 00 2B C4 0B 00 02 FE FE FE FE FF FF FF FF 03 FE FE FE FE 2C 0B 03 00 04 FF FF FF FE FE FE FE FF"
                + NL;
        expected += "DM33 Emission Increasing AECD Active Time from Engine #1 (0): [" + NL;
        expected += "  EI-AECD Number = 1: Timer 1 = 68395 minutes; Timer 2 = 771115 minutes" + NL;
        expected += "  EI-AECD Number = 2: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "  EI-AECD Number = 3: Timer 1 = errored; Timer 2 = 199468 minutes" + NL;
        expected += "  EI-AECD Number = 4: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM33EmissionIncreasingAECDActiveTime.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM33 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        var expectedResult = new RequestResult<>(false);
        assertEquals(expectedResult, instance.requestDM33(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33GlobalEmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime() throws BusException {
        final int pgn = DM33EmissionIncreasingAECDActiveTime.PGN;

        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        var packet1 = new DM33EmissionIncreasingAECDActiveTime(Packet.create(pgn, 0, data));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 18A10000 [36] 01 2B 0B 01 00 2B C4 0B 00 02 FE FE FE FE FF FF FF FF 03 FE FE FE FE 2C 0B 03 00 04 FF FF FF FE FE FE FE FF"
                + NL;
        expected += "DM33 Emission Increasing AECD Active Time from Engine #1 (0): [" + NL;
        expected += "  EI-AECD Number = 1: Timer 1 = 68395 minutes; Timer 2 = 771115 minutes" + NL;
        expected += "  EI-AECD Number = 2: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "  EI-AECD Number = 3: Timer 1 = errored; Timer 2 = 199468 minutes" + NL;
        expected += "  EI-AECD Number = 4: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += NL;

        TestResultsListener listener = new TestResultsListener();
        var expectedResult = new RequestResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM33(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33GlobalNoResponse() throws BusException {
        final int pgn = DM33EmissionIncreasingAECDActiveTime.PGN;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM33(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecific() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(
                                                                              Packet.create(pgn,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM6 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, packet1);

        assertEquals(result, instance.requestDM6(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(Packet.create(pgn,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x61,
                                                                                            0x02,
                                                                                            0x13,
                                                                                            0x00,
                                                                                            0x21,
                                                                                            0x06,
                                                                                            0x1F,
                                                                                            0x00,
                                                                                            0xEE,
                                                                                            0x10,
                                                                                            0x04,
                                                                                            0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM6 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                                                                                List.of(packet1),
                                                                                List.of());
        assertEquals(result, instance.requestDM6(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;
        Packet requestPacket = Packet.create(REQUEST_PGN | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM6 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                                                                                List.of(),
                                                                                List.of());
        assertEquals(result, instance.requestDM6(listener, 0x21));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6Global() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(
                                                                              Packet.create(pgn,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x61));
        DM6PendingEmissionDTCPacket packet2 = new DM6PendingEmissionDTCPacket(
                                                                              Packet.create(pgn,
                                                                                            0x17,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00));
        DM6PendingEmissionDTCPacket packet3 = new DM6PendingEmissionDTCPacket(
                                                                              Packet.create(pgn,
                                                                                            0x21,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM6 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 [3] 00 FF 61" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FECF17 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;
        expected += "10:15:30.0000 18FECF21 [8] 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM6PendingEmissionDTCPacket> dm6Packets = List.of(packet1, packet2, packet3);
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, dm6Packets, List.of());

        assertEquals(result, instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6GlobalWithDTCs() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(Packet.create(pgn,
                                                                                            0x00,
                                                                                            0x00,
                                                                                            0xFF,
                                                                                            0x61,
                                                                                            0x02,
                                                                                            0x13,
                                                                                            0x00,
                                                                                            0x21,
                                                                                            0x06,
                                                                                            0x1F,
                                                                                            0x00,
                                                                                            0xEE,
                                                                                            0x10,
                                                                                            0x04,
                                                                                            0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM6 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                                                                                List.of(packet1),
                                                                                List.of());
        assertEquals(result, instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6GlobalWithNoResponses() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM6 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(false), instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGlobalRequestDm56() throws BusException {
        final int pgn = DM56EngineFamilyPacket.PGN;
        byte[] bytes = "2015MY-EUS HD ODB   *".getBytes(UTF_8);

        TestResultsListener listener = new TestResultsListener();
        DM56EngineFamilyPacket packet1 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x00, bytes));
        DM56EngineFamilyPacket packet2 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x17, bytes));
        DM56EngineFamilyPacket packet3 = new DM56EngineFamilyPacket(Packet.create(pgn, 0x21, bytes));

        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket()),
                 Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket()),
                 Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(eq(TIMEOUT),
                                                                                                eq(MILLISECONDS));

        List<DM56EngineFamilyPacket> packets = instance.requestDM56(listener);
        assertEquals(3, packets.size());
        assertEquals(packet1, packets.get(0));
        assertEquals(packet2, packets.get(1));
        assertEquals(packet3, packets.get(2));

        String expectedResults = "10:15:30.0000 Global DM56 Request";
        expectedResults += NL + "10:15:30.0000 18EAFFA5 [3] C7 FC 00 (TX)";
        expectedResults += NL
                + "10:15:30.0000 18FCC700 [21] 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A";
        expectedResults += NL + "Model Year and Certification Engine Family from Engine #1 (0): ";
        expectedResults += NL + "Model Year: 2015MY-E";
        expectedResults += NL + "Family Name: US HD ODB   ";
        expectedResults += NL;
        expectedResults += NL
                + "10:15:30.0000 18FCC717 [21] 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A";
        expectedResults += NL + "Model Year and Certification Engine Family from Instrument Cluster #1 (23): ";
        expectedResults += NL + "Model Year: 2015MY-E";
        expectedResults += NL + "Family Name: US HD ODB   ";
        expectedResults += NL;
        expectedResults += NL
                + "10:15:30.0000 18FCC721 [21] 32 30 31 35 4D 59 2D 45 55 53 20 48 44 20 4F 44 42 20 20 20 2A";
        expectedResults += NL + "Model Year and Certification Engine Family from Body Controller (33): ";
        expectedResults += NL + "Model Year: 2015MY-E";
        expectedResults += NL + "Family Name: US HD ODB   ";
        expectedResults += NL;
        expectedResults += NL;

        String actualResults = listener.getResults();
        assertEquals(expectedResults, actualResults);

        verify(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));
    }

    @Test
    public void testRequestDm58Global() {
        TestResultsListener listener = new TestResultsListener();
        try {
            instance.requestDM58(listener, GLOBAL_ADDR, 1224);
            fail("Exception for request sent to global should have been thrown.");
        } catch (IllegalArgumentException e) {
            assertEquals("DM7 request to global.", e.getMessage());
            assertEquals("DM7 request to global." + NL, listener.getResults());
        }
    }

    @Test
    public void testRequestDm58Ds() throws BusException {
        TestResultsListener listener = new TestResultsListener();
        int moduleAddress = 0x17;
        byte[] data = { (byte) 0xF5, (byte) 0xC8, 0x04, 0x00, (byte) 0xFF, (byte) 0x88, (byte) 0xFF, (byte) 0xFF };
        DM58RationalityFaultSpData packet = new DM58RationalityFaultSpData(Packet.create(DM58RationalityFaultSpData.PGN,

                                                                                         moduleAddress,
                                                                                         data));

        doReturn(Stream.of(packet.getPacket()),
                 Stream.of(packet.getPacket()),
                 Stream.of(packet.getPacket())).when(j1939)
                                               .read(anyLong(),
                                                     any());

        BusResult<DM58RationalityFaultSpData> expected = new BusResult<>(false, packet);
        BusResult<DM58RationalityFaultSpData> actual = instance.requestDM58(listener,
                                                                            moduleAddress,
                                                                            190);
        assertEquals(expected, actual);

        String expectedResults = "10:15:30.0000 Sending DM7 for DM58 to Instrument Cluster #1 (23) for SPN 190" + NL;
        expectedResults += "10:15:30.0000 18E317A5 [8] F5 BE 00 1F FF FF FF FF (TX)" + NL;
        expectedResults += "10:15:30.0000 18FBDB17 [8] F5 C8 04 00 FF 88 FF FF" + NL;
        expectedResults += "DM58 from Instrument Cluster #1 (23): " + NL;
        expectedResults += "  Test Identifier: 245" + NL;
        expectedResults += "  Rationality Fault SPN: 1224" + NL;
        expectedResults += "  Rationality Fault SPN Data Value: [FF]" + NL;
        expectedResults += "  SPN  1224, Test Identifier: Not Available" + NL + NL;
        String actualResults = listener.getResults();
        assertEquals(expectedResults, actualResults);

    }

    @Test
    public void testRequestGlobalDm56NoResponse() throws BusException {

        doReturn(Stream.empty(), Stream.empty(), Stream.empty())
                                                                .when(j1939)
                                                                .read(eq(TIMEOUT),
                                                                      eq(MILLISECONDS));

        TestResultsListener listener = new TestResultsListener();
        List<DM56EngineFamilyPacket> actual = instance.requestDM56(listener);
        List<DM56EngineFamilyPacket> expected = new ArrayList<>();
        assertEquals(expected, actual);

        String expectedResults = "10:15:30.0000 Global DM56 Request" + NL;
        expectedResults += "10:15:30.0000 18EAFFA5 [3] C7 FC 00 (TX)" + NL;
        expectedResults += "10:15:30.0000 Timeout - No Response" + NL;
        String actualResults = listener.getResults();
        assertEquals(expectedResults, actualResults);

        verify(j1939).read(eq(TIMEOUT), eq(MILLISECONDS));
    }

    @Test
    public void testGetCompositeSystems() {
        Set<MonitoredSystem> monitoredSystems = new ConcurrentSkipListSet<>();
        monitoredSystems.add(new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(true, true), 1, true));
        monitoredSystems.add(new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(true, true), 2, true));
        monitoredSystems.add(new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(true, true), 3, true));
        monitoredSystems.add(new MonitoredSystem(
                                                 BOOST_PRESSURE_CONTROL_SYS,
                                                 getStatus(true, true),
                                                 1,
                                                 true));
        monitoredSystems.add(new MonitoredSystem(
                                                 BOOST_PRESSURE_CONTROL_SYS,
                                                 getStatus(true, false),
                                                 2,
                                                 true));
        monitoredSystems.add(new MonitoredSystem(
                                                 BOOST_PRESSURE_CONTROL_SYS,
                                                 getStatus(false, false),
                                                 3,
                                                 true));
        monitoredSystems.add(new MonitoredSystem(CATALYST, getStatus(false, false), 1, true));
        monitoredSystems.add(new MonitoredSystem(CATALYST, getStatus(false, false), 2, true));
        monitoredSystems.add(new MonitoredSystem(CATALYST, getStatus(false, false), 3, true));

        List<CompositeMonitoredSystem> expected = new ArrayList<>();
        expected.add(new CompositeMonitoredSystem(
                                                  new MonitoredSystem(
                                                                      AC_SYSTEM_REFRIGERANT,
                                                                      getStatus(true, true),
                                                                      -1,
                                                                      true),
                                                  true));
        expected.add(new CompositeMonitoredSystem(
                                                  new MonitoredSystem(
                                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                                      getStatus(true, false),
                                                                      -1,
                                                                      true),
                                                  true));
        expected.add(new CompositeMonitoredSystem(
                                                  new MonitoredSystem(
                                                                      CATALYST,
                                                                      getStatus(false, false),
                                                                      -1,
                                                                      true),
                                                  true));

        List<CompositeMonitoredSystem> actual = getCompositeSystems(monitoredSystems, true);
        assertEquals(expected, actual);
    }

    @Test
    public void testGetDM20PacketsNoResponse() throws BusException {
        TestResultsListener listener = new TestResultsListener();

        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;
        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM20PacketsTrue() throws BusException {
        TestResultsListener listener = new TestResultsListener();
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x00,
                                                                                                        0x11,
                                                                                                        0x22,
                                                                                                        0x33,
                                                                                                        0x44,
                                                                                                        0x55,
                                                                                                        0x66,
                                                                                                        0x77,
                                                                                                        0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x17,
                                                                                                        0x01,
                                                                                                        0x02,
                                                                                                        0x03,
                                                                                                        0x04,
                                                                                                        0x05,
                                                                                                        0x06,
                                                                                                        0x07,
                                                                                                        0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x21,
                                                                                                        0x10,
                                                                                                        0x20,
                                                                                                        0x30,
                                                                                                        0x40,
                                                                                                        0x50,
                                                                                                        0x60,
                                                                                                        0x70,
                                                                                                        0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 18C2A500 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                               8,721" + NL;
        expected += "  OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += "10:15:30.0000 18C2A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                                 513" + NL;
        expected += "  OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += "10:15:30.0000 18C2A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                               8,208" + NL;
        expected += "  OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;
        expected += NL;

        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM20PacketsWithEngine1Response() throws BusException {
        TestResultsListener listener = new TestResultsListener();
        final int pgn = DM20MonitorPerformanceRatioPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x01,
                                                                                                        0x11,
                                                                                                        0x22,
                                                                                                        0x33,
                                                                                                        0x44,
                                                                                                        0x55,
                                                                                                        0x66,
                                                                                                        0x77,
                                                                                                        0x88));
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x17,
                                                                                                        0x01,
                                                                                                        0x02,
                                                                                                        0x03,
                                                                                                        0x04,
                                                                                                        0x05,
                                                                                                        0x06,
                                                                                                        0x07,
                                                                                                        0x08));
        DM20MonitorPerformanceRatioPacket packet3 = new DM20MonitorPerformanceRatioPacket(
                                                                                          Packet.create(pgn | BUS_ADDR,
                                                                                                        0x21,
                                                                                                        0x10,
                                                                                                        0x20,
                                                                                                        0x30,
                                                                                                        0x40,
                                                                                                        0x50,
                                                                                                        0x60,
                                                                                                        0x70,
                                                                                                        0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
                                                                                                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM20 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C2 00 (TX)" + NL;
        expected += "10:15:30.0000 18C2A501 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM20 from Engine #2 (1):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                               8,721" + NL;
        expected += "  OBD Monitoring Conditions Encountered                        17,459" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += "10:15:30.0000 18C2A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM20 from Instrument Cluster #1 (23):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                                 513" + NL;
        expected += "  OBD Monitoring Conditions Encountered                         1,027" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += "10:15:30.0000 18C2A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM20 from Body Controller (33):  [" + NL;
        expected += "                                                       Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                               8,208" + NL;
        expected += "  OBD Monitoring Conditions Encountered                        16,432" + NL;
        expected += "]" + NL;
        expected += NL;
        instance.requestDM20(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM21PacketsNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM21 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;

        instance.requestDM21(listener, 0x17);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testGetDM21PacketsTrue() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x21,
                                                                                                0x10,
                                                                                                0x20,
                                                                                                0x30,
                                                                                                0x40,
                                                                                                0x50,
                                                                                                0x60,
                                                                                                0x70,
                                                                                                0x80));
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
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty())
                                                                                .when(j1939)
                                                                                .read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;
        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21PacketsTrue() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x00,
                                                                                                0x11,
                                                                                                0x22,
                                                                                                0x33,
                                                                                                0x44,
                                                                                                0x55,
                                                                                                0x66,
                                                                                                0x77,
                                                                                                0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x17,
                                                                                                0x01,
                                                                                                0x02,
                                                                                                0x03,
                                                                                                0x04,
                                                                                                0x05,
                                                                                                0x06,
                                                                                                0x07,
                                                                                                0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x21,
                                                                                                0x10,
                                                                                                0x20,
                                                                                                0x30,
                                                                                                0x40,
                                                                                                0x50,
                                                                                                0x60,
                                                                                                0x70,
                                                                                                0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
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
        expected += "]" + NL + NL;
        expected += "10:15:30.0000 18C1A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL + NL;
        expected += "10:15:30.0000 18C1A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL + NL;

        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21PacketsWithEngine1Response() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x01,
                                                                                                0x11,
                                                                                                0x22,
                                                                                                0x33,
                                                                                                0x44,
                                                                                                0x55,
                                                                                                0x66,
                                                                                                0x77,
                                                                                                0x88));
        DM21DiagnosticReadinessPacket packet2 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x17,
                                                                                                0x01,
                                                                                                0x02,
                                                                                                0x03,
                                                                                                0x04,
                                                                                                0x05,
                                                                                                0x06,
                                                                                                0x07,
                                                                                                0x08));
        DM21DiagnosticReadinessPacket packet3 = new DM21DiagnosticReadinessPacket(
                                                                                  Packet.create(pgn | BUS_ADDR,
                                                                                                0x21,
                                                                                                0x10,
                                                                                                0x20,
                                                                                                0x30,
                                                                                                0x40,
                                                                                                0x50,
                                                                                                0x60,
                                                                                                0x70,
                                                                                                0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939)
                                                                                          .read(anyLong(),
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
        expected += NL;
        expected += "10:15:30.0000 18C1A517 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM21 from Instrument Cluster #1 (23): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     513 km (318.763 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    1,541 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  1,027 km (638.148 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      2,055 minutes" + NL;
        expected += "]" + NL;
        expected += NL;
        expected += "10:15:30.0000 18C1A521 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM21 from Body Controller (33): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     8,208 km (5,100.215 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    24,656 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  16,432 km (10,210.371 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      32,880 minutes" + NL;
        expected += "]" + NL;
        expected += NL;

        instance.requestDM21(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM5() throws BusException {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM5DiagnosticReadinessPacket packet1 = new DM5DiagnosticReadinessPacket(Packet.create(pgn,
                                                                                              0x00,
                                                                                              0x11,
                                                                                              0x22,
                                                                                              0x33,
                                                                                              0x44,
                                                                                              0x55,
                                                                                              0x66,
                                                                                              0x77,
                                                                                              0x88));
        DM5DiagnosticReadinessPacket packet2 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(pgn,
                                                                                              0x17,
                                                                                              0x01,
                                                                                              0x02,
                                                                                              0x03,
                                                                                              0x04,
                                                                                              0x05,
                                                                                              0x06,
                                                                                              0x07,
                                                                                              0x08));
        DM5DiagnosticReadinessPacket packet3 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(pgn,
                                                                                              0x21,
                                                                                              0x10,
                                                                                              0x20,
                                                                                              0x30,
                                                                                              0x40,
                                                                                              0x50,
                                                                                              0x60,
                                                                                              0x70,
                                                                                              0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket()))
                                                                                          .when(j1939)
                                                                                          .read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM5 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CE FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECE00 [8] 11 22 33 44 55 66 77 88" + NL;
        expected += "DM5 from Engine #1 (0): OBD Compliance: Reserved for SAE/Unknown (51), Active Codes: 17, Previously Active Codes: 34"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        supported, not complete" + NL;
        expected += "    Fuel System                not supported,     complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         supported, not complete" + NL;
        expected += "    Boost pressure control sys     supported,     complete" + NL;
        expected += "    Catalyst                       supported, not complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        expected += "    Diesel Particulate Filter      supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Evaporative system             supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor         not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor heater      supported, not complete" + NL;
        expected += "    Heated catalyst            not supported, not complete" + NL;
        expected += "    NMHC converting catalyst   not supported,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Secondary air system       not supported,     complete" + NL;
        expected += NL;

        expected += "10:15:30.0000 18FECE17 [8] 01 02 03 04 05 06 07 08" + NL;
        expected += "DM5 from Instrument Cluster #1 (23): OBD Compliance: OBD and OBD II (3), Active Codes: 1, Previously Active Codes: 2"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        supported,     complete" + NL;
        expected += "    Fuel System                not supported,     complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not supported,     complete" + NL;
        expected += "    Boost pressure control sys     supported,     complete" + NL;
        expected += "    Catalyst                       supported, not complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        expected += "    Diesel Particulate Filter      supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Evaporative system             supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor         not supported,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not supported,     complete" + NL;
        expected += "    Heated catalyst            not supported, not complete" + NL;
        expected += "    NMHC converting catalyst   not supported,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Secondary air system       not supported,     complete" + NL;
        expected += NL;

        expected += "10:15:30.0000 18FECE21 [8] 10 20 30 40 50 60 70 80" + NL;
        expected += "DM5 from Body Controller (33): OBD Compliance: Reserved for SAE/Unknown (48), Active Codes: 16, Previously Active Codes: 32"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not supported, not complete" + NL;
        expected += "    Fuel System                not supported,     complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         supported, not complete" + NL;
        expected += "    Boost pressure control sys not supported,     complete" + NL;
        expected += "    Catalyst                   not supported,     complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        expected += "    Diesel Particulate Filter  not supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Evaporative system         not supported,     complete" + NL;
        expected += "    Exhaust Gas Sensor         not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor heater      supported, not complete" + NL;
        expected += "    Heated catalyst            not supported,     complete" + NL;
        expected += "    NMHC converting catalyst   not supported,     complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported,     complete" + NL;
        expected += "    Secondary air system       not supported,     complete" + NL;
        expected += NL;

        instance.requestDM5(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM5WithNoResponses() throws BusException {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;
        TestResultsListener listener = new TestResultsListener();

        Packet requestPacket = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM5 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 [3] CE FE 00 (TX)" + NL;
        expected += "10:15:30.0000 Timeout - No Response" + NL;
        instance.requestDM5(listener);
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM57DS() throws BusException {

        final int pgn = 64710;  // DM57 (PG 0xFCC6);
        int moduleAddress = 0x17;

        listener = new TestResultsListener();

        Packet requestPacket = j1939.createRequestPacket(pgn, moduleAddress);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, moduleAddress);

        GenericPacket packet = new GenericPacket(Packet.create(pgn,
                                                               moduleAddress,
                                                               false,
                                                               0xFD,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF,
                                                               0xFF));

        doReturn(Stream.of(packet.getPacket()), Stream.of(packet.getPacket()), Stream.of(packet.getPacket()))
                                                                                                             .when(j1939)
                                                                                                             .read(anyLong(),
                                                                                                                   any());

        BusResult<? extends GenericPacket> actual = instance.requestDM57(listener, moduleAddress);

        assertEquals(BusResult.of(packet), actual);

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM57 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 [3] C6 FC 00 (TX)" + NL;
        expected += "10:15:30.0000 18FCC617 [9] FD FF FF FF FF FF FF FF FF" + NL;
        expected += "OBD Information from Instrument Cluster #1 (23): " + NL;
        expected += "  SPN  5843, Engine Warm-up Sequence: A warm-up cycle has occurred on this engine start"
                + NL + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939, times(2)).createRequestPacket(pgn, moduleAddress);
        verify(j1939).read(anyLong(), eq(MILLISECONDS));
    }

}
