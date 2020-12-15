/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.bus.j1939.J1939.REQUEST_PGN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Unit tests for the {@link DTCModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                    justification = "The values returned are properly ignored on verify statements.")
@RunWith(MockitoJUnitRunner.class)
public class DTCModuleTest {

    /**
     * The Bus address of the tool for testing purposes
     */
    private static final int BUS_ADDR = 0xA5;

    private DTCModule instance;

    @Spy
    private J1939 j1939;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new DTCModule();
        instance.setJ1939(j1939);
        doReturn(BUS_ADDR).when(j1939).getBusAddress();
    }

    @After
    public void tearDown() {
        // this enforces that we test implementation details of J1939 - Joe
        // 11/19/2020
        // verifyNoMoreInteractions(j1939);
    }

    @Test
    public void testReadDM1() throws BusException {
        DM1ActiveDTCsPacket packet = new DM1ActiveDTCsPacket(
                Packet.create(65226, 0x00, 0x11, 0x01, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        doReturn(Stream.of(packet.getPacket(), packet.getPacket(), packet.getPacket())).when(j1939).read(anyLong(),
                any());

        TestResultsListener listener = new TestResultsListener();

        RequestResult<DM1ActiveDTCsPacket> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet),
                Collections.emptyList());
        assertEquals(expectedResult, instance.readDM1(listener));

        String expected = "10:15:30.0000 Reading the bus for published DM1 messages" + NL;
        expected += "DM1 from Engine #1 (0): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReadDM1WithEmptyResponse() throws BusException {
        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();

        RequestResult<DM1ActiveDTCsPacket> expectedResult = new RequestResult<>(false,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(expectedResult, instance.readDM1(listener));

        String expected = "10:15:30.0000 Reading the bus for published DM1 messages" + NL;
        expected += "10:15:30.0000 No published DM1 messages were identified" + NL;
        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReadDM1WithTwoModules() throws BusException {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                Packet.create(65226, 0x00, 0x11, 0x01, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                Packet.create(65226, 0x01, 0x11, 0x01, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet1.getPacket(), packet2.getPacket(),
                packet1.getPacket(), packet2.getPacket())).when(j1939)
                        .read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();

        RequestResult<DM1ActiveDTCsPacket> expectedResult = new RequestResult<>(false,
                Arrays.asList(packet1, packet2),
                Collections.emptyList());
        assertEquals(expectedResult, instance.readDM1(listener));

        String expected = "10:15:30.0000 Reading the bus for published DM1 messages" + NL;
        expected += "DM1 from Engine #1 (0): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;
        expected += "DM1 from Engine #2 (1): MIL: alternate off, RSL: slow flash, AWL: alternate off, PL: fast flash"
                + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        assertEquals(expected, listener.getResults());
        verify(j1939).read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReportDM28DestinationSpecific() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM28PermanentEmissionDTCPacket packet1 = new DM28PermanentEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM28 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 80 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8021 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM28 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                packet1);
        assertEquals(expectedResult, instance.reportDM28(listener, 0x21));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReportDM28DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

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
        expected += "10:15:30.0000 Destination Specific DM28 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 80 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8000 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM28 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(
                false, Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.reportDM28(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testReportDM28Global() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM28PermanentEmissionDTCPacket packet1 = new DM28PermanentEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM28PermanentEmissionDTCPacket packet2 = new DM28PermanentEmissionDTCPacket(
                Packet.create(pgn, 0x17, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM28PermanentEmissionDTCPacket packet3 = new DM28PermanentEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM28 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 80 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8000 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM28 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FD8017 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM28 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FD8021 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM28 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(
                false, Arrays.asList(packet1, packet2, packet3), Collections.emptyList());
        assertEquals(expectedResult, instance.reportDM28(listener, GLOBAL_ADDR));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    /* FIXME what is this supposed to be testing? It has one response. */
    public void testRequestDM11GlobalNoResponseWithManyModules() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE8A5, 0x00, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8A500 00 FF FF FF A5 D3 FE 00" + NL;
        expected += "Diagnostic Trouble Codes were successfully cleared." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<AcknowledgmentPacket> expectedResult = new RequestResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11GlobalWithNoResponsesOneModule() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "ERROR: Clearing Diagnostic Trouble Codes failed." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11GlobalWithOneModule() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket1 = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket1).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE800 | BUS_ADDR, 0x00, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8A500 00 FF FF FF A5 D3 FE 00" + NL;
        // why is thi smissing FIXME expected += "DM11 from Engine #1 (0):
        // Response is Acknowledged" + NL;
        expected += "Diagnostic Trouble Codes were successfully cleared." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM11ClearActiveDTCsPacket> expectedResult = new RequestResult<>(false, Collections.emptyList(),
                Collections.singletonList(packet1));
        assertEquals(expectedResult, instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11GlobalWithOneModuleWithNack() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE800, 0x00, 0x01, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E80000 01 FF FF FF F9 D3 FE 00" + NL;
        expected += "Acknowledgment from Engine #1 (0): Response: NACK, Group Function: 255, Address Acknowledged: 249, PGN Requested: 65235"
                + NL;
        expected += "ERROR: Clearing Diagnostic Trouble Codes failed." + NL;
        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM11ClearActiveDTCsPacket> expectedResult = new RequestResult<>(false, Collections.emptyList(),
                Collections.singletonList(packet1));
        assertEquals(expectedResult, instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11WithManyModules() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE8FF, 0x00, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        AcknowledgmentPacket packet2 = new AcknowledgmentPacket(
                Packet.create(0xE8FF, 0x17, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        AcknowledgmentPacket packet3 = new AcknowledgmentPacket(
                Packet.create(0xE8FF, 0x21, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E80000 00 FF FF FF F9 D3 FE 00" + NL;
        expected += "DM11 from Engine #1 (0): Response is Acknowledged" + NL;
        expected += "10:15:30.0000 18E80017 00 FF FF FF F9 D3 FE 00" + NL;
        expected += "DM11 from Instrument Cluster #1 (23): Response is Acknowledged" + NL;
        expected += "10:15:30.0000 18E80021 00 FF FF FF F9 D3 FE 00" + NL;
        expected += "DM11 from Body Controller (33): Response is Acknowledged" + NL;
        expected += "Diagnostic Trouble Codes were successfully cleared." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM11ClearActiveDTCsPacket> expectedResult = new RequestResult<>(false, Collections.emptyList(),
                Arrays.asList(new AcknowledgmentPacket[] { packet1, packet2, packet3 }));
        assertEquals(expectedResult, instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM11WithManyModulesWithNack() throws BusException {
        final int pgn = DM11ClearActiveDTCsPacket.PGN;

        Packet requestPacket = Packet.create(REQUEST_PGN | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE800 | BUS_ADDR, 0x00, 0x01, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        AcknowledgmentPacket packet2 = new AcknowledgmentPacket(
                Packet.create(0xE800 | BUS_ADDR, 0x17, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        AcknowledgmentPacket packet3 = new AcknowledgmentPacket(
                Packet.create(0xE800 | BUS_ADDR, 0x21, 0x00, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xD3, 0xFE, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Clearing Diagnostic Trouble Codes" + NL;
        expected += "10:15:30.0000 Global DM11 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D3 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8A500 01 FF FF FF A5 D3 FE 00" + NL;
        // FIXME expected += "Acknowledgment from Engine #1 (0): Response: NACK,
        // Group Function: 255, Address Acknowledged: 249, PGN Requested: 65235"
        // + NL;
        expected += "10:15:30.0000 18E8A517 00 FF FF FF A5 D3 FE 00" + NL;
        // FIXME expected += "Acknowledgment from Instrument Cluster #1 (23):
        // Response: ACK, Group Function: 255, Address Acknowledged: 249, PGN
        // Requested: 65235"
        // + NL;
        expected += "10:15:30.0000 18E8A521 00 FF FF FF A5 D3 FE 00" + NL;
        // FIXME expected += "Acknowledgment from Body Controller (33):
        // Response: ACK, Group Function: 255, Address Acknowledged: 249, PGN
        // Requested: 65235"
        // + NL;
        expected += "ERROR: Clearing Diagnostic Trouble Codes failed." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM11ClearActiveDTCsPacket> expectedResult = new RequestResult<>(false, Collections.emptyList(),
                Arrays.asList(packet1, packet2, packet3));
        assertEquals(expectedResult, instance.requestDM11(listener));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecific() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM12 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();

        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM12(listener, true, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EA00A5 D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);

        assertEquals(expectedResult, instance.requestDM12(listener, true, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM12 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 D4 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM12MILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM12(listener, true, 0x17));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12Global() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM12MILOnEmissionDTCPacket packet2 = new DM12MILOnEmissionDTCPacket(
                Packet.create(pgn, 0x17, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        TestResultsListener listener = new TestResultsListener();
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM12 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FED417 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FED421 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM12 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        List<DM12MILOnEmissionDTCPacket> expectedPackets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        RequestResult<DM12MILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false, expectedPackets,
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM12(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12GlobalWithDTCs() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EAFFA5 D4 FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FED400 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM12 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        RequestResult<DM12MILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1), Collections.emptyList());

        assertEquals(expectedResult, instance.requestDM12(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM12GlobalWithNoResponses() throws BusException {
        final int pgn = DM12MILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        TestResultsListener listener = new TestResultsListener();
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM12 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 D4 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 D4 FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        assertEquals(RequestResult.empty(), instance.requestDM12(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Destination Specific DM21 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 C1 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                Collections.emptyList(),
                Collections.emptyList());

        assertEquals(result, instance.requestDM21(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21DestinationSpecificResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());

        String expected = "10:15:30.0000 Destination Specific DM21 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL;

        assertEquals(result, instance.requestDM21(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21GlobalNoResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());

        String expected = "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL;

        assertEquals(result, instance.requestDM21(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM21GlobalResponse() throws BusException {
        final int pgn = DM21DiagnosticReadinessPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        Packet packet = Packet.create(pgn | BUS_ADDR, 0, 0x10, 0x27, 0x20, 0x4E, 0x30, 0x75, 0x40, 0x9C);
        DM21DiagnosticReadinessPacket packet1 = new DM21DiagnosticReadinessPacket(packet);

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM21DiagnosticReadinessPacket> result = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());

        String expected = "10:15:30.0000 Global DM21 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 C1 00 (TX)" + NL;
        expected += "10:15:30.0000 18C1A500 10 27 20 4E 30 75 40 9C" + NL;
        expected += "DM21 from Engine #1 (0): [" + NL;
        expected += "  Distance Traveled While MIL is Activated:     10,000 km (6,213.712 mi)" + NL;
        expected += "  Time Run by Engine While MIL is Activated:    30,000 minutes" + NL;
        expected += "  Distance Since DTCs Cleared:                  20,000 km (12,427.424 mi)" + NL;
        expected += "  Time Since DTCs Cleared:                      40,000 minutes" + NL + "]" + NL;

        assertEquals(result, instance.requestDM21(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecific() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM23 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB521 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false,
                packet1);
        assertEquals(expectedResult, instance.requestDM23(listener, true, 0x21));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EA00A5 B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM23(listener, true, 0x00));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);
        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM23 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 B5 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM23(listener, true, 0x17));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23Global() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM23PreviouslyMILOnEmissionDTCPacket packet2 = new DM23PreviouslyMILOnEmissionDTCPacket(
                Packet.create(pgn, 0x17, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM23PreviouslyMILOnEmissionDTCPacket packet3 = new DM23PreviouslyMILOnEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM23 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FDB517 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FDB521 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM23 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                Arrays.asList(packet1, packet2, packet3), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM23(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23GlobalWithDTCs() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

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
        expected += "10:15:30.0000 18EAFFA5 B5 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB500 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM23 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM23(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM23GlobalWithNoResponses() throws BusException {
        final int pgn = DM23PreviouslyMILOnEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM23 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 B5 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 B5 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM23(listener, true));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificNackOnly() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        AcknowledgmentPacket packet1 = new AcknowledgmentPacket(
                Packet.create(0xE800 | GLOBAL_ADDR, 0x00, 0x01, 0xFF, 0xFF, 0xFF, BUS_ADDR, 0xB7, 0xFD, 0x00));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 B7 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18E8FF00 01 FF FF FF A5 B7 FD 00" + NL;
        expected += "Acknowledgment from Engine #1 (0): Response: NACK, Group Function: 255, Address Acknowledged: 165, PGN Requested: 64951"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());
        String expected = "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 B7 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM25DestinationSpecificWithResponse() throws BusException {
        final int pgn = DM25ExpandedFreezeFrame.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        int[] realData = new int[] {
                0x56,
                0x9D,
                0x00,
                0x07,
                0x7F,
                0x00,
                0x01,
                0x7B,
                0x00,
                0x00,
                0x39,
                0x3A,
                0x5C,
                0x0F,
                0xC4,
                0xFB,
                0x00,
                0x00,
                0x00,
                0xF1,
                0x26,
                0x00,
                0x00,
                0x00,
                0x12,
                0x7A,
                0x7D,
                0x80,
                0x65,
                0x00,
                0x00,
                0x32,
                0x00,
                0x00,
                0x00,
                0x00,
                0x84,
                0xAD,
                0x00,
                0x39,
                0x2C,
                0x30,
                0x39,
                0xFC,
                0x38,
                0xC6,
                0x35,
                0xE0,
                0x34,
                0x2C,
                0x2F,
                0x00,
                0x00,
                0x7D,
                0x7D,
                0x8A,
                0x28,
                0xA0,
                0x0F,
                0xA0,
                0x0F,
                0xD1,
                0x37,
                0x00,
                0xCA,
                0x28,
                0x01,
                0xA4,
                0x0D,
                0x00,
                0xA8,
                0xC3,
                0xB2,
                0xC2,
                0xC3,
                0x00,
                0x00,
                0x00,
                0x00,
                0x7E,
                0xD0,
                0x07,
                0x00,
                0x7D,
                0x04,
                0xFF,
                0xFA };

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(pgn, 0x00, realData));
        doReturn(Stream.of(packet.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Destination Specific DM25 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 B7 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB700 56 9D 00 07 7F 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "DM25 from Engine #1 (0): " + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "DTC:  (157) Engine Fuel 1 Injector Metering Rail 1 Pressure Mechanical System Not Responding Or Out Of Adjustment (7)"
                + NL;
        expected += "SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF"
                + NL;
        expected += "]" + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM25ExpandedFreezeFrame> expectedResult = new BusResult<>(false, packet);
        assertEquals(expectedResult, instance.requestDM25(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecific() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0, 0, 0, 0, 0, 0, 0, 0));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM26 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB821 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x21));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EA00A5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB800 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 97, Time Since Engine Start: not available" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(
                false, Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM26 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 B8 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(false,
                Collections.emptyList(), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM26(listener, 0x17));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26Global() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0, 0, 0, 0, 0, 0, 0, 0));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0, 0, 0, 0, 0, 0, 0, 0));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0, 0, 0, 0, 0, 0, 0, 0));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB800 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Engine #1 (0): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "10:15:30.0000 18FDB817 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Instrument Cluster #1 (23): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;
        expected += "10:15:30.0000 18FDB821 00 00 00 00 00 00 00 00" + NL;
        expected += "DM26 from Body Controller (33): Warm-ups: 0, Time Since Engine Start: 0 seconds" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(
                false, Arrays.asList(packet1, packet2, packet3), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26GlobalWithDTCs() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FDB811 22 33 44 55 66 77 88 FF" + NL;
        expected += "DM26 from Cruise Control (17): Warm-ups: 68, Time Since Engine Start: 13,090 seconds" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM26TripDiagnosticReadinessPacket> expectedResult = new RequestResult<>(
                false, List.of(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM26GlobalWithNoResponses() throws BusException {
        final int pgn = DM26TripDiagnosticReadinessPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0xFF);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());
        String expected = "";
        expected += "10:15:30.0000 Global DM26 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 B8 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM26(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecific() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM27 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();

        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, packet1);
        assertEquals(expectedResult, instance.requestDM27(listener, true, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EA00A5 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, packet1);

        assertEquals(expectedResult, instance.requestDM27(listener, true, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM27 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 82 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        BusResult<DM27AllPendingDTCsPacket> expectedResult = new BusResult<>(false, Optional.empty());
        assertEquals(expectedResult, instance.requestDM27(listener, true, 0x17));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27Global() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM27AllPendingDTCsPacket packet2 = new DM27AllPendingDTCsPacket(
                Packet.create(pgn, 0x17, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM27 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FD8217 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FD8221 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM27 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM27AllPendingDTCsPacket> expectedPackets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        RequestResult<DM27AllPendingDTCsPacket> expectedResult = new RequestResult<>(false, expectedPackets,
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM27(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27GlobalWithDTCs() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EAFFA5 82 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8200 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM27AllPendingDTCsPacket> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1), Collections.emptyList());

        assertEquals(expectedResult, instance.requestDM27(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM27GlobalWithNoResponses() throws BusException {
        final int pgn = DM27AllPendingDTCsPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM27 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 82 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 82 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM27(listener, true));
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM28DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());
        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM28 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 80 FD 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(false,
                Collections.emptyList(), Collections.emptyList());
        assertEquals(expectedResult, instance.reportDM28(listener, 0x17));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM28GlobalWithDTCs() throws BusException {
        final int pgn = DM28PermanentEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EAFFA5 80 FD 00 (TX)" + NL;
        expected += "10:15:30.0000 18FD8000 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM28 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM28PermanentEmissionDTCPacket> expectedResult = new RequestResult<>(false, List.of(packet1),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM28(listener, true));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "10:15:30.0000 Destination Specific DM29 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 9E 00 (TX)" + NL;
        expected += "Error: Timeout - No Response."
                + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(BusResult.empty(), instance.requestDM29(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29DestinationSpecificResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "10:15:30.0000 Destination Specific DM29 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 189EA500 09 20 47 31 01 FF FF FF" + NL;
        expected += "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                               9" + NL;
        expected += "All Pending DTC Count                                           32" + NL;
        expected += "Emission-Related MIL-On DTC Count                               71" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count                    49" + NL;
        expected += "Emission-Related Permanent DTC Count                             1" + NL;

        DM29DtcCounts packet1 = new DM29DtcCounts(
                Packet.create(pgn | BUS_ADDR, 0x00,
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
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29GlobalNoResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        String expected = "10:15:30.0000 Global DM29 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 9E 00 (TX)" + NL;
        expected += "Error: Timeout - No Response."
                + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM29(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM29GlobalResponse() throws BusException {
        final int pgn = DM29DtcCounts.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        String expected = "10:15:30.0000 Global DM29 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 9E 00 (TX)" + NL;
        expected += "10:15:30.0000 189EFF00 09 20 47 31 01 FF FF FF" + NL;
        expected += "DM29 from Engine #1 (0): " + NL;
        expected += "Emission-Related Pending DTC Count                               9" + NL;
        expected += "All Pending DTC Count                                           32" + NL;
        expected += "Emission-Related MIL-On DTC Count                               71" + NL;
        expected += "Emission-Related Previously MIL-On DTC Count                    49" + NL;
        expected += "Emission-Related Permanent DTC Count                             1" + NL;

        DM29DtcCounts packet1 = new DM29DtcCounts(
                Packet.create(pgn | GLOBAL_ADDR, 0x00,
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
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM29(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x17, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x17);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM2 Request to Instrument Cluster #1 (23)" + NL;
        expected += "10:15:30.0000 18EA17A5 CB FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertTrue(instance.requestDM2(listener, true, 0x17).getPacket().isEmpty());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x17);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2DestinationSpecificWithEngine1Response() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | 0x01, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x01);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x01, 0x22, 0xDD, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM2 Request to Engine #2 (1)" + NL;
        expected += "10:15:30.0000 18EA01A5 CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB01 22 DD 33 44 55 66 77 88" + NL;
        expected += "DM2 from Engine #2 (1): MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (148531) Unknown Data Drifted Low (21) 102 times" + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(packet1, instance.requestDM2(listener, true, 0x01).getPacket().get().left.get());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x01);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalFullStringTrue() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x00, 0x22, 0xDD, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM2PreviouslyActiveDTC packet2 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x17, 0x02, 0xFD, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM2PreviouslyActiveDTC packet3 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x21, 0x20, 0xDF, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB00 22 DD 33 44 55 66 77 88" + NL;
        expected += "DM2 from Engine #1 (0): MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (148531) Unknown Data Drifted Low (21) 102 times" + NL;
        expected += "10:15:30.0000 18FECB17 02 FD 03 04 05 06 07 08" + NL;
        expected += "DM2 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: other" + NL;
        expected += "DTC:  (1027) Trip Time in Derate by Engine Current Below Normal Or Open Circuit (5) 6 times" + NL;
        expected += "10:15:30.0000 18FECB21 20 DF 30 40 50 60 70 80" + NL;
        expected += "DM2 from Body Controller (33): MIL: off, RSL: other, AWL: off, PL: off" + NL;
        expected += "DTC:  (147504) Unknown Data Valid But Above Normal Operating Range - Moderately Severe Level (16) 96 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM2PreviouslyActiveDTC> expectedPackets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        assertEquals(expectedPackets, instance.requestDM2(listener, true).getPackets());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(expected, listener.getResults());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalPacketsFullStringFalse() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, false, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM2PreviouslyActiveDTC packet2 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x17, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM2PreviouslyActiveDTC packet3 = new DM2PreviouslyActiveDTC(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 CB FE 00" + NL;
        expected += "10:15:30.0000 18FECB00 11 22 33 44 55 66 77 88" + NL;
        expected += "10:15:30.0000 18FECB17 01 02 03 04 05 06 07 08" + NL;
        expected += "10:15:30.0000 18FECB21 10 20 30 40 50 60 70 80" + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM2PreviouslyActiveDTC> expectedPackets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        assertEquals(expectedPackets, instance.requestDM2(listener, false).getPackets());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalWithDTCs() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EAFFA5 CB FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECB00 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM2 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();

        RequestResult<DM2PreviouslyActiveDTC> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM2(listener, true));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0xFF);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM2GlobalWithNoResponses() throws BusException {
        final int pgn = DM2PreviouslyActiveDTC.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM2 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 CB FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 CB FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(new ArrayList<DM2PreviouslyActiveDTC>(), instance.requestDM2(listener, true).getPackets());
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        String expected = "10:15:30.0000 Destination Specific DM31 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 A3 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM25ExpandedFreezeFrame> expectedResult = new RequestResult<>(false,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM31(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31DestinationSpecificResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        String expected = "10:15:30.0000 Destination Specific DM31 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 18A3A521 61 02 13 81 62 1D 21 06 1F 23 22 DD EE 10 04 00 AA 55"
                + NL;
        expected += "DM31 from Body Controller (33): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;
        expected += "]" + NL;

        DM31DtcToLampAssociation packet1 = new DM31DtcToLampAssociation(
                Packet.create(pgn | BUS_ADDR, 0x21,
                        0x61, // SPN least significant bit
                        0x02, // SPN most significant bit
                        0x13, // Failure mode indicator
                        0x81, // SPN Conversion Occurrence Count
                        0x62, // Lamp Status/Support
                        0x1D, // Lamp Status/State

                        0x21, // SPN least significant bit
                        0x06, // SPN most significant bit
                        0x1F, // Failure mode indicator
                        0x23, // SPN Conversion Occurrence Count
                        0x22, // Lamp Status/Support
                        0xDD, // Lamp Status/State

                        0xEE, // SPN least significant bit
                        0x10, // SPN most significant bit
                        0x04, // Failure mode indicator
                        0x00, // SPN Conversion Occurrence Count
                        0xAA, // Lamp Status/Support
                        0x55));// Lamp Status/State
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM31DtcToLampAssociation> expectedResult = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM31(listener, 0x21));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31GlobalNoResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        String expected = "10:15:30.0000 Global DM31 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A3 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A3 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM31(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }

    @Test
    public void testRequestDM31GlobalResponse() throws BusException {
        final int pgn = DM31DtcToLampAssociation.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        String expected = "10:15:30.0000 Global DM31 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A3 00 (TX)" + NL;
        expected += "10:15:30.0000 18A30021 61 02 13 81 62 1D 21 06 1F 23 22 DD EE 10 04 00 AA 55"
                + NL;
        expected += "DM31 from Body Controller (33): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;
        expected += "]" + NL;

        DM31DtcToLampAssociation packet1 = new DM31DtcToLampAssociation(
                Packet.create(pgn, 0x21,
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
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM31(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33DestinationSpecificEmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime()
            throws BusException {
        final int pgn = DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime packet1 = new DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(
                Packet.create(pgn,
                        0x00,
                        data));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 18A10000 01 2B 0B 01 00 2B C4 0B 00 02 FE FE FE FE FF FF FF FF 03 FE FE FE FE 2C 0B 03 00 04 FF FF FF FE FE FE FE FF"
                + NL;
        expected += "DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 1" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = 68395 minutes" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 771115 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 2" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 3" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 199468 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 4" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> expectedResult = new RequestResult<>(
                false,
                Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM33(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33DestinationSpecificNoResponse() throws BusException {
        final int pgn = DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM33 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 00 A1 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> expectedResult = new RequestResult<>(
                false,
                Collections.emptyList(), Collections.emptyList());

        assertEquals(expectedResult, instance.requestDM33(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33GlobalEmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime() throws BusException {
        final int pgn = DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime packet1 = new DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(
                Packet.create(pgn,
                        GLOBAL_ADDR,
                        data));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 18A100FF 01 2B 0B 01 00 2B C4 0B 00 02 FE FE FE FE FF FF FF FF 03 FE FE FE FE 2C 0B 03 00 04 FF FF FF FE FE FE FE FF"
                + NL;
        expected += "DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 1" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = 68395 minutes" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 771115 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 2" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 3" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 199468 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 4" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> expectedResult = new RequestResult<>(
                false,
                Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM33(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM33GlobalNoResponse() throws BusException {
        final int pgn = DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime packet1 = new DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(
                Packet.create(pgn,
                        0x00,
                        data));

        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Global DM33 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 00 A1 00 (TX)" + NL;
        expected += "10:15:30.0000 18A10000 01 2B 0B 01 00 2B C4 0B 00 02 FE FE FE FE FF FF FF FF 03 FE FE FE FE 2C 0B 03 00 04 FF FF FF FE FE FE FE FF"
                + NL;
        expected += "DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 1" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = 68395 minutes" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 771115 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 2" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 3" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = 199468 minutes" + NL;
        expected += "EngineHoursTimer" + NL;
        expected += "  EI-AECD Number = 4" + NL;
        expected += "  EI-AECD Engine Hours Timer 1 = errored" + NL;
        expected += "  EI-AECD Engine Hours Timer 2 = n/a" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> expectedResult = new RequestResult<>(
                false,
                Collections.singletonList(packet1), Collections.emptyList());
        assertEquals(expectedResult, instance.requestDM33(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecific() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x00);

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket())).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM6 Request to Engine #1 (0)" + NL;
        expected += "10:15:30.0000 18EA00A5 CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, packet1);

        assertEquals(result, instance.requestDM6(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecificWithDTCs() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
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
        expected += "10:15:30.0000 18EA00A5 CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(result, instance.requestDM6(listener, 0x00));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x00);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6DestinationSpecificWithNoResponses() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | 0x21, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0x21);

        doReturn(Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "";
        expected += "10:15:30.0000 Destination Specific DM6 Request to Body Controller (33)" + NL;
        expected += "10:15:30.0000 18EA21A5 CF FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                Collections.emptyList(),
                Collections.emptyList());
        assertEquals(result, instance.requestDM6(listener, 0x21));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, 0x21);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6Global() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(
                Packet.create(pgn, 0x00, 0x00, 0xFF, 0x61));
        DM6PendingEmissionDTCPacket packet2 = new DM6PendingEmissionDTCPacket(
                Packet.create(pgn, 0x17, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM6PendingEmissionDTCPacket packet3 = new DM6PendingEmissionDTCPacket(
                Packet.create(pgn, 0x21, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));
        doReturn(Stream.of(packet1.getPacket(), packet2.getPacket(), packet3.getPacket())).when(j1939).read(anyLong(),
                any());

        String expected = "";
        expected += "10:15:30.0000 Global DM6 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 00 FF 61" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FECF17 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "10:15:30.0000 18FECF21 00 FF 00 00 00 00 00 00" + NL;
        expected += "DM6 from Body Controller (33): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;

        TestResultsListener listener = new TestResultsListener();
        List<DM6PendingEmissionDTCPacket> dm6Packets = new ArrayList<>() {
            {
                add(packet1);
                add(packet2);
                add(packet3);
            }
        };
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, dm6Packets,
                Collections.emptyList());

        assertEquals(result, instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6GlobalWithDTCs() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;

        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

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
        expected += "10:15:30.0000 18EAFFA5 CF FE 00 (TX)" + NL;
        expected += "10:15:30.0000 18FECF00 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC:  (609) Controller #2 Received Network Data In Error (19) 0 times" + NL;
        expected += "DTC:  (1569) Engine Protection Torque Derate Condition Exists (31) 0 times" + NL;
        expected += "DTC:  (4334) Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;

        TestResultsListener listener = new TestResultsListener();
        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false,
                Collections.singletonList(packet1),
                Collections.emptyList());
        assertEquals(result, instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939).read(anyLong(), any());
    }

    @Test
    public void testRequestDM6GlobalWithNoResponses() throws BusException {
        final int pgn = DM6PendingEmissionDTCPacket.PGN;
        Packet requestPacket = Packet.create(0xEA00 | GLOBAL_ADDR, BUS_ADDR, true, pgn, pgn >> 8, pgn >> 16);
        doReturn(requestPacket).when(j1939).createRequestPacket(pgn, 0);

        doReturn(Stream.empty(), Stream.empty(), Stream.empty()).when(j1939).read(anyLong(), any());

        String expected = "10:15:30.0000 Global DM6 Request" + NL;
        expected += "10:15:30.0000 18EAFFA5 CF FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;
        expected += "10:15:30.0000 18EAFFA5 CF FE 00 (TX)" + NL;
        expected += "Error: Timeout - No Response." + NL;

        TestResultsListener listener = new TestResultsListener();
        assertEquals(RequestResult.empty(), instance.requestDM6(listener));
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(j1939).createRequestPacket(pgn, GLOBAL_ADDR);
        verify(j1939, times(2)).read(anyLong(), any());
    }
}
