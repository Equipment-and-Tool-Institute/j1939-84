/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC.PGN;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Garrison Garland (garrison@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step16ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step16Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private RequestResult<DM2PreviouslyActiveDTC> requestResult(DM2PreviouslyActiveDTC packet1) {
        return new RequestResult<>(false, List.of(new Either<>(packet1, null)));
    }

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step16Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dtcModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 dtcModule);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testDTCsNotEmpty() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        Packet packet = mock(Packet.class);
        when(packet.getBytes()).thenReturn(new byte[0]);
        when(packet1.getPacket()).thenReturn(packet);
        DiagnosticTroubleCode dtc1 = mock(DiagnosticTroubleCode.class);
        when(packet1.getDtcs()).thenReturn(List.of(dtc1));
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1));
        when(dtcModule.requestDM2(any(), eq(true), eq(0))).thenReturn(new BusResult<>(false, packet1));

        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU reported a previously active DTC");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.16.2.a - OBD ECU reported a previously active DTC" + NL, listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 16", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILNotSupported() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OTHER);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of());

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU does not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.16.2.b - OBD ECU does not report MIL off" + NL, listener.getResults());
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testMILOff() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(
                new RequestResult<>(false, List.of(packet1), List.of(packet3)));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of());

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILStatusNotOFF() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        Packet packet = mock(Packet.class);
        when(packet.getBytes()).thenReturn(new byte[0]);
        when(packet1.getPacket()).thenReturn(packet);
        DiagnosticTroubleCode dtc = mock(DiagnosticTroubleCode.class);
        when(packet1.getDtcs()).thenReturn(List.of(dtc));
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.SLOW_FLASH);

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1));
        when(dtcModule.requestDM2(any(), eq(true), eq(0))).thenReturn(new BusResult<>(false, packet1));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU reported a previously active DTC");
        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU does not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expected = "";
        expected += "FAIL: 6.1.16.2.a - OBD ECU reported a previously active DTC" + NL;
        expected += "FAIL: 6.1.16.2.b - OBD ECU does not report MIL off" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b,c") })
    public void testNonOBDMilOn() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getDtcs()).thenReturn(List.of());
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of());

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.c - Non-OBD ECU does not report MIL off or not supported");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expected = "";
        expected += "FAIL: 6.1.16.2.b - OBD ECU does not report MIL off" + NL;
        expected += "FAIL: 6.1.16.2.c - Non-OBD ECU does not report MIL off or not supported" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b") })
    public void testResponseNotNACK() {

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                Packet.create(PGN, 0, new byte[] { 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(requestResult(packet1));

        // Set up the destination specific packets we will be returning when
        // requested
        DM2PreviouslyActiveDTC packet2 = new DM2PreviouslyActiveDTC(
                Packet.create(PGN, 0, new byte[] { 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));

        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet2));

        // add ACK/NACK packets to the listing for complete reality testing
        DM2PreviouslyActiveDTC packet4 = new DM2PreviouslyActiveDTC(
                Packet.create(PGN, 3, new byte[] { 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new BusResult<>(false, packet4));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.b - OBD module Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.16.4.b - OBD module Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query" + NL,
                listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a") })
    public void testResponsesAreDifferent() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        Packet packet1Packet = mock(Packet.class);
        when(packet1Packet.getBytes()).thenReturn(new byte[0]);
        when(packet1.getPacket()).thenReturn(packet1Packet);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM2PreviouslyActiveDTC packet3 = mock(DM2PreviouslyActiveDTC.class);
        when(packet3.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
        when(packet3.getSourceAddress()).thenReturn(3);
        // global response
        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1, packet3));

        // Set up the destination specific packets we will be returning when requested
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        Packet packet2Packet = mock(Packet.class);
        when(packet2Packet.getBytes()).thenReturn(new byte[] { 1 });
        when(packet2.getPacket()).thenReturn(packet2Packet);

        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet2));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new BusResult<>(false, packet4));

        // Return the modules address so that we can do the destination specific calls
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.a - Difference compared to data received during global request");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.16.4.a - Difference compared to data received during global request" + NL,
                     listener.getResults());
    }

    @Test
    public void testTwoObdModulesOneWithResponseOneWithNack2() {

        DM2PreviouslyActiveDTC packet1 = new DM2PreviouslyActiveDTC(
                Packet.create(PGN, 0, new byte[] { 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));

        // Set up the destination specific packets we will be returning when requested
        DM2PreviouslyActiveDTC packet2 = new DM2PreviouslyActiveDTC(
                Packet.create(PGN, 0, new byte[] { 0x00, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 }));
        when(dtcModule.requestDM2(any(), eq(true), eq(0))).thenReturn(new BusResult<>(false, packet2));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        when(packet4.getResponse()).thenReturn(Response.NACK);
        when(packet4.getSourceAddress()).thenReturn(3);

        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new BusResult<>(false, packet4));

        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
