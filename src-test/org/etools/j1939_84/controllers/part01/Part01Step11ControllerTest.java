/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket.create;
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
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
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
 * The unit test for {@link Part01Step11Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step11ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

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
                                              diagnosticMessageModule,
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
                                 diagnosticMessageModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 dataRepository);
    }

    @Test
    public void testEmptyDM21PacketsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

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
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        DM21DiagnosticReadinessPacket packet3 = create(21, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

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
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));
        // return the set of OBD module addresses when requested
        DM21DiagnosticReadinessPacket packet1 = create(9, 0, 0, 0, 0);
        globalPackets.add(packet1);

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(diagnosticMessageModule.requestDM21(any(), eq(9)))
                .thenReturn(new BusResult<>(false, packet2));
        when(packet2.getResponse()).thenReturn(Response.NACK);
        when(packet2.getSourceAddress()).thenReturn(9);

        DM21DiagnosticReadinessPacket packet3 = create(21, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(9));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

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
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, globalPackets, List.of()));
        // return the set of OBD module addresses when requested
        DM21DiagnosticReadinessPacket packet1 = create(9, 0, 0, 0, 0);
        globalPackets.add(packet1);

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        globalPackets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        globalPackets.add(packet5);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(diagnosticMessageModule.requestDM21(any(), eq(9)))
                .thenReturn(new BusResult<>(false, packet2));
        when(packet2.getResponse()).thenReturn(Response.NACK);
        when(packet2.getSourceAddress()).thenReturn(16);

        DM21DiagnosticReadinessPacket packet3 = create(21, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(9));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

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
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testKmSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);
        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));

        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 15, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));
        verify(diagnosticMessageModule).requestDM21(any());

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.a - Cruise Control (17) reported distance with MIL on (SPN 3069) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.a - Cruise Control (17) reported distance with MIL on (SPN 3069) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.a - Cruise Control (17) reported distance with MIL on (SPN 3069) is not zero"
                + NL
                + "FAIL: 6.1.11.4.a - Cruise Control (17) reported distance with MIL on (SPN 3069) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testKmWhileMILIsActivated() {
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 17, 21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 10, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.b - Cruise Control (17) reported distance SCC (SPN 3294) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.b - Cruise Control (17) reported distance SCC (SPN 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.b - Cruise Control (17) reported distance SCC (SPN 3294) is not zero" + NL
                + "FAIL: 6.1.11.4.b - Cruise Control (17) reported distance SCC (SPN 3294) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMinutesSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);

        // return the set of OBD module addresses when requested
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, packets, List.of()));

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 20);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        when(packet3.getSourceAddress()).thenReturn(21);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.d - Cruise Control (17) reported time SCC (SPN 3296) > 1 minute");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.d - Cruise Control (17) reported time SCC (SPN 3296) > 1 minute");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.d - Cruise Control (17) reported time SCC (SPN 3296) > 1 minute"
                + NL
                + "FAIL: 6.1.11.4.d - Cruise Control (17) reported time SCC (SPN 3296) > 1 minute" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    @Test
    public void testMinutesWhileMILIsActivated() {
        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0, 17, 21));

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, packets, List.of()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = create(0, 0, 0, 25, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = create(17, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        DM21DiagnosticReadinessPacket packet6 = create(21, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM21(any(), eq(21)))
                .thenReturn(new BusResult<>(false, packet6));
        packets.add(packet6);

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any(), eq(0));
        verify(diagnosticMessageModule).requestDM21(any(), eq(17));
        verify(diagnosticMessageModule).requestDM21(any(), eq(21));
        verify(diagnosticMessageModule).requestDM21(any());

        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.1.c - Engine #1 (0) reported time with MIL on (SPN 3295) is not zero");
        verify(mockListener).addOutcome(1,
                                        11,
                                        FAIL,
                                        "6.1.11.4.c - Engine #1 (0) reported time with MIL on (SPN 3295) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.c - Engine #1 (0) reported time with MIL on (SPN 3295) is not zero"
                + NL
                + "FAIL: 6.1.11.4.c - Engine #1 (0) reported time with MIL on (SPN 3295) is not zero"
                + NL;
        assertEquals(expectedResult, listener.getResults());

    }

}
