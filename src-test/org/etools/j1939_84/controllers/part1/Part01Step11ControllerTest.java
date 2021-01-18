/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step11ControllerTest extends AbstractControllerTest {

    /*
     * All values must be checked prior to mocking so that we are not creating
     * unnecessary mocks.
     */
    private static DM21DiagnosticReadinessPacket createDM21Packet(Integer sourceAddress,
                                                                  Double kmSinceDtcCleared,
                                                                  Double kmWhileMILIsActivated,
                                                                  Double milesSinceDTCsCleared,
                                                                  Double milesWhileMILIsActivated,
                                                                  Double minutesSinceDTCsCleared,
                                                                  Double minutesWhileMILIsActivated) {
        DM21DiagnosticReadinessPacket packet = mock(DM21DiagnosticReadinessPacket.class);
        if (sourceAddress != null) {
            when(packet.getSourceAddress()).thenReturn(sourceAddress);
        }
        if (kmSinceDtcCleared != null) {
            when(packet.getKmSinceDTCsCleared()).thenReturn(kmSinceDtcCleared);
        }
        if (kmWhileMILIsActivated != null) {
            when(packet.getKmWhileMILIsActivated()).thenReturn(kmWhileMILIsActivated);
        }
        if (milesSinceDTCsCleared != null) {
            when(packet.getMilesSinceDTCsCleared()).thenReturn(milesSinceDTCsCleared);
        }
        if (milesWhileMILIsActivated != null) {
            when(packet.getMilesWhileMILIsActivated()).thenReturn(milesWhileMILIsActivated);
        }
        if (minutesSinceDTCsCleared != null) {
            when(packet.getMinutesSinceDTCsCleared()).thenReturn(minutesSinceDTCsCleared);
        }
        if (minutesWhileMILIsActivated != null) {
            when(packet.getMinutesWhileMILIsActivated()).thenReturn(minutesWhileMILIsActivated);
        }

        Packet packetPacket = mock(Packet.class);
        when(packet.getPacket()).thenReturn(packetPacket);
        when(packetPacket.getBytes()).thenReturn(new byte[0]);

        return packet;
    }

    @Mock
    private BannerModule bannerModule;
    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step11Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step11Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              diagnosticReadinessModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());
        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 diagnosticReadinessModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 dataRepository);
    }

    @Test
    public void testEmptyDM21PacketsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.e - No OBD ECU provided a DM21 message");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD module Cruise Control (17) did not provide a response to Global query and did not provide a NACK for the DS query");
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.e - No OBD ECU provided a DM21 message" + NL
                + "FAIL: 6.1.11.4.f - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL
                + "FAIL: 6.1.11.4.f - OBD module Cruise Control (17) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part 1 Step 11", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDiffer() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        DM21DiagnosticReadinessPacket packet3 = createDM21Packet(21, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDifferAtNackPacket() {
        List<Integer> obdAddressSet = List.of(0, 9, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));
        // return the set of OBD module addresses when requested
        DM21DiagnosticReadinessPacket packet1 = createDM21Packet(9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        globalPackets.add(packet1);

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(9)))
                .thenReturn(new BusResult<>(false, packet2));
        when(packet2.getResponse()).thenReturn(Response.NACK);
        when(packet2.getSourceAddress()).thenReturn(9);

        DM21DiagnosticReadinessPacket packet3 = createDM21Packet(21, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(9));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDifferAtSourceAddress() {
        List<Integer> obdAddressSet = List.of(0, 9, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));
        // return the set of OBD module addresses when requested
        DM21DiagnosticReadinessPacket packet1 = createDM21Packet(9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        globalPackets.add(packet1);

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(9)))
                .thenReturn(new BusResult<>(false, packet2));
        when(packet2.getResponse()).thenReturn(Response.NACK);
        when(packet2.getSourceAddress()).thenReturn(16);

        DM21DiagnosticReadinessPacket packet3 = createDM21Packet(21, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(9));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

//        verify(mockListener).addOutcome(1,
//                                        11,
//                                        FAIL,
//                                        "6.1.11.4.e - DS responses differ from global responses");

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.f - OBD module Suspension - Drive Axle #1 (21) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    /**
     * Test method for
     * {@link Part01Step11Controller#run()}.
     */
    @Test
    public void testHappyPath() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testKmSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);
        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));

        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero"
                + NL
                + "FAIL: 6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testKmWhileMILIsActivated() {
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 17, 21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.b - An ECU reported distance SCC (SPN 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL
                + "FAIL: 6.1.11.4.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMilesSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        // return the set of OBD module addresses when requested
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero"
                + NL
                + "FAIL: 6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMilesWhileMILIsActivated() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        Packet ackPacket = Packet.create(AcknowledgmentPacket.PGN, 21, new byte[] { 1, 0, 0, 0, 0, 0, 0, 0 });
        AcknowledgmentPacket packet3 = new AcknowledgmentPacket(ackPacket); //mock(AcknowledgmentPacket.class);
        //        when(packet3.getResponse()).thenReturn(Response.NACK);
        //        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.b - An ECU reported distance SCC (SPN 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL
                + "FAIL: 6.1.11.4.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMinutesSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);

        // return the set of OBD module addresses when requested
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute"
                + NL
                + "FAIL: 6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    @Test
    public void testMinutesWhileMILIsActivated() {
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 17, 21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 25.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        DM21DiagnosticReadinessPacket packet6 = createDM21Packet(21, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet6));
        packets.add(packet6);

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero"
                + NL
                + "FAIL: 6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero"
                + NL;
        assertEquals(expectedResult, listener.getResults());

    }

}
