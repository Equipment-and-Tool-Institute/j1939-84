/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
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
 * The unit test for {@link Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Step11ControllerTest extends AbstractControllerTest {

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

    private Step11Controller instance;

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

        instance = new Step11Controller(executor,
                engineSpeedModule,
                bannerModule,
                diagnosticReadinessModule,
                vehicleInformationModule,
                dataRepository);
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
                reportFileModule,
                dataRepository);
    }

    @Test
    public void testEmptyDM21PacketsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> emptyPackets = new ArrayList<>();

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, emptyPackets, Collections.emptyList()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
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
                Outcome.FAIL,
                "6.1.11.1.e - No OBD ECU provided a DM21 message");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.e - No OBD ECU provided a DM21 message");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.e - No OBD ECU provided a DM21 message");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.e - DS responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.e - No OBD ECU provided a DM21 message" + NL
                + "FAIL: 6.1.11.4.e - DS responses differ from global responses" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step11Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part 1 Step 11", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step11Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals(1, instance.getTotalSteps());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDiffer() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, Collections.emptyList()));
        // return the set of OBD module addresses when requested

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
                .thenReturn(new BusResult<>(false, new Either<>(packet3, null
                        /* packet3 - this didn't make sense */)));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.e - DS responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.e - DS responses differ from global responses" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDifferAtNackPacket() {
        List<Integer> obdAddressSet = List.of(0, 9, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, Collections.emptyList()));
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
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.e - DS responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.e - DS responses differ from global responses" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testGlobalAndAddressSpecificResponsesDifferAtSourceAddress() {
        List<Integer> obdAddressSet = List.of(0, 9, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> globalPackets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, globalPackets, Collections.emptyList()));
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

        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.f - NACK not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.e - DS responses differ from global responses");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.f - NACK not received from OBD ECUs that did not respond to global query");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.e - DS responses differ from global responses");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.f - NACK not received from OBD ECUs that did not respond to global query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.4.e - DS responses differ from global responses" + NL
                + "FAIL: 6.1.11.4.f - NACK not received from OBD ECUs that did not respond to global query" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step11Controller#run()}.
     */
    @Test
    public void testHappyPath() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));
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
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));

        verify(reportFileModule).onProgress(0, 1, "");

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
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));
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
                Outcome.FAIL,
                "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero"
                + NL
                + "FAIL: 6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testKmWhileMILIsActivated() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 10.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
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
                Outcome.FAIL,
                "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL
                + "FAIL: 6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMilesSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        // return the set of OBD module addresses when requested
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 15.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
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
                Outcome.FAIL,
                "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.a - An ECU reported distance with MIL on (SPN 3069) is not zero");

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
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 10.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
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
                Outcome.FAIL,
                "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.b - An ECU reported distance SCC (SPN 3294) is not zero" + NL
                + "FAIL: 6.1.11.4.b. - An ECU reported distance SCC (SPN 3294) is not zero" + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    @Test
    public void testMinutesSinceDTCsCleared() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);

        // return the set of OBD module addresses when requested
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();

        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
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
                Outcome.FAIL,
                "6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute");

        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.d - An ECU reported time SCC (SPN 3296) > 1 minute"
                + NL
                + "FAIL: 6.1.11.4.d - An ECU reported time SCC (SPN 3296) > 1 minute" + NL;
        assertEquals(expectedResult, listener.getResults());

    }

    @Test
    public void testMinutesWhileMILIsActivated() {
        List<Integer> obdAddressSet = List.of(0, 17, 21);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        List<DM21DiagnosticReadinessPacket> packets = new ArrayList<>();
        
        when(diagnosticReadinessModule.requestDM21Packets(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, packets, Collections.emptyList()));
        // return the set of OBD module addresses when requested

        DM21DiagnosticReadinessPacket packet4 = createDM21Packet(0, 0.0, 0.0, 0.0, 0.0, 0.0, 25.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(0)))
                .thenReturn(new BusResult<>(false, packet4));
        packets.add(packet4);

        DM21DiagnosticReadinessPacket packet5 = createDM21Packet(17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(17)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        createDM21Packet(21, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        when(diagnosticReadinessModule.getDM21Packets(any(), eq(true), eq(21)))
                .thenReturn(new BusResult<>(false, packet5));
        packets.add(packet5);

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(0));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(17));
        verify(diagnosticReadinessModule).getDM21Packets(any(), eq(true), eq(21));
        verify(diagnosticReadinessModule).requestDM21Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero");
        verify(mockListener).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero");
        verify(reportFileModule).addOutcome(1,
                11,
                Outcome.FAIL,
                "6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResult = "FAIL: 6.1.11.1.c - An ECU reported time with MIL on (SPN 3295) is not zero"
                + NL
                + "FAIL: 6.1.11.4.c - An ECU reported time with MIL on (SPN 3295) is not zero"
                + NL;
        assertEquals(expectedResult, listener.getResults());

    }

}
