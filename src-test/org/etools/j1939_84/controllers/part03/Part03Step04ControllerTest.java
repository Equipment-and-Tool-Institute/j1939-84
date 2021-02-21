/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
public class Part03Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 4;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step04Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
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
    public void testNoMessages() {
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.empty());

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.b - No ECU reported > 0 emission-related pending count");
    }

    @Test
    public void testFailureForMILCountGreaterThanZero() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 1, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for MIL on count");
    }

    @Test
    public void testFailureForPreviousMILCountGreaterThanZero() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 1, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for previous MIL on count");
    }

    @Test
    public void testFailureForPermanentDTCCountGreaterThanZero() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 1);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for permanent DTC count");
    }

    @Test
    public void testFailureForNoEmissionPendingCount() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        moduleInfo.set(dm27);
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.b - No ECU reported > 0 emission-related pending count");
    }

    @Test
    public void testFailureForDifferenceFromDM6WithMore() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1, dtc2));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.c - Engine #1 (0) reported a different number of emission-related pending DTCs than what it reported in the previous DM6");
    }

    @Test
    public void testFailureForDifferenceFromDM6WithLess() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        moduleInfo.set(dm27);
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.c - Engine #1 (0) reported a different number of emission-related pending DTCs than what it reported in the previous DM6");
    }

    @Test
    public void testFailureForDifferencePendingCounts() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.d - Engine #1 (0) reported a lower number of all pending DTCs than the number of emission-related pending DTCs");
    }

    @Test
    public void testFailureForLessThanDM27DTCs() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(435, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.e - Engine #1 (0) reported a lower number of all pending DTCs than what it reported in DM27 earlier");
    }

    @Test
    public void testFailureForWrongNotSupportedValue() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.f - Engine #1 (0) does not support DM27 and did not report all pending DTCs = 0xFF");
    }

    @Test
    public void testFailureForNonOBD() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 1, 1, 1);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for MIL on count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for previous MIL on count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.a - Engine #1 (0) reported > 0 for permanent DTC count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.g - Non-OBD ECU Engine #1 (0) reported > 0 for pending DTC count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.g - Non-OBD ECU Engine #1 (0) reported > 0 for MIL-on count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.g - Non-OBD ECU Engine #1 (0) reported > 0 for previous MIL-on count");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.4.2.g - Non-OBD ECU Engine #1 (0) reported > 0 for permanent DTC count");
    }

    @Test
    public void testFailureForMoreThanOnePendingCounts() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 2, 2, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        var dm27 = DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2);
        moduleInfo.set(dm27);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1, dtc2));
        dataRepository.putObdModule(moduleInfo);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.4.3.a - Engine #1 (0) reported > 1 for pending DTC count");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.4.3.a - Engine #1 (0) reported > 1 for all pending DTC count");
    }

    @Test
    public void testFailureForMoreThanOneModuleReportingPendingCounts() {
        DM29DtcCounts dm29_0 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        DM29DtcCounts dm29_1 = DM29DtcCounts.create(1, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29_0, dm29_1));

        OBDModuleInformation moduleInfo0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        moduleInfo0.set(DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1));
        moduleInfo0.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo0);

        OBDModuleInformation moduleInfo1 = new OBDModuleInformation(1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        moduleInfo1.set(DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc2));
        moduleInfo1.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc2));
        dataRepository.putObdModule(moduleInfo1);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.4.3.b - More than one ECU reported > 0 for pending DTC count");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.4.3.b - More than one ECU reported > 0 for all pending DTC count");
    }

    @Test
    public void testNoFailures() {
        DM29DtcCounts dm29 = DM29DtcCounts.create(0, 1, 1, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        OBDModuleInformation moduleInfo0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        moduleInfo0.set(DM27AllPendingDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1));
        moduleInfo0.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo0);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).requestDM29(any());

    }
}
