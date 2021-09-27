/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part06Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 8;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part06Step08Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM29(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));
        verify(communicationsModule).requestDM29(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNonZeroEmissionRelatedPending() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 1, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.a - Engine #1 (0) reports > 0 for emission-related pending");
    }

    @Test
    public void testFailureForNonZeroPreviousMILOn() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 1, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.a - Engine #1 (0) reports > 0 for previous MIL on");
    }

    @Test
    public void testFailureForNoMILOn() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 0, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.6.8.2.b - No ECU reported > 0 for MIL on");
    }

    @Test
    public void testFailureForDifferentDM12() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc1), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.c - Engine #1 (0) reported a different number for MIL on than what it reported in DM12");
    }

    @Test
    public void testFailureForNoPermanent() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 0);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.d - No ECU reported > 0 for permanent");
    }

    @Test
    public void testFailureForDifferentDM28() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc1), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.e - Engine #1 (0) reported a different number for MIL on than what it reported in DM28");
    }

    @Test
    public void testFailureForDifferentDM6() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc1), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.f - Engine #1 (0) reported an for all pending DTC count that is less than its pending DTC (DM6) count");
    }

    @Test
    public void testFailureForIncorrectAllPending() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.g - Engine #1 (0) did not report number of all pending DTCs = 0xFF");
    }

    @Test
    public void testFailureForNoNACK() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM29(any(), eq(1))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));
        verify(communicationsModule).requestDM29(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.8.2.h - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testWarningForMoreThanOneMILOn() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc1), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 2, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.8.3.a - Engine #1 (0) reported > 1 for MIL on");
    }

    @Test
    public void testWarningForMoreThanOneMILOnModule() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation1.set(DM27AllPendingDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation1.set(DM6PendingEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm29_1 = DM29DtcCounts.create(1, 0, 0, 1, 1, 0, 0);
        when(communicationsModule.requestDM29(any(), eq(1))).thenReturn(BusResult.of(dm29_1));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));
        verify(communicationsModule).requestDM29(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.8.3.b - More than one ECU reported > 0 for MIL on");
    }

    @Test
    public void testWarningForMoreThanOnePermanent() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc1), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 2);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.8.3.c - Engine #1 (0) reported > 1 for permanent");
    }

    @Test
    public void testWarningForMoreThanOnePermanentModule() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation0.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(dm29));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM27AllPendingDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation1.set(DM28PermanentEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation1.set(DM6PendingEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm29_1 = DM29DtcCounts.create(1, 0, 0, 1, 0, 0, 1);
        when(communicationsModule.requestDM29(any(), eq(1))).thenReturn(BusResult.of(dm29_1));

        runTest();

        verify(communicationsModule).requestDM29(any(), eq(0));
        verify(communicationsModule).requestDM29(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.8.3.d - More than one ECU reported > 0 for permanent");
    }

}
