/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part05Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 5;

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

        instance = new Part05Step05Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForPendingDTCsCount() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 1, 0, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.a - Engine #1 (0) reported > 0 for emissions-related pending");
    }

    @Test
    public void testFailureForPreviousMILOnCount() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 1, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.a - Engine #1 (0) reported > 0 for previous MIL on");
    }

    @Test
    public void testFailureForNoDTCs() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.b - No ECU reported > 0 MIL on DTCs and > 0 permanent DTCs");
    }

    @Test
    public void testFailureForDifferentDM12DTCs() {
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.c - Engine #1 (0) reported a different number of MIL on DTCs than what it reported in DM12 earlier in this part");
    }

    @Test
    public void testFailureForDifferentDM28DTCs() {
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.d - Engine #1 (0) reported a different number of permanent DTCs than what it reported in DM28 earlier in this part");
    }

    @Test
    public void testFailureForAllPendingDTCs() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.e.i - Engine #1 (0) reported > 0 for all pending DTCs");
    }

    @Test
    public void testFailureForAllPendingDTCs0xFF() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0xFF, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.e.ii - Engine #1 (0) reported 0xFF for all pending DTCs");
    }

    @Test
    public void testFailureForAllPendingDTCsNot0xFF() {
        var dtc = DiagnosticTroubleCode.create(123, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.5.2.f.i - Engine #1 (0) did not report all pending DTCs = 0xFF");
    }

    @Test
    public void testFailureForMoreThanOneMILOn() {
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 2, 0, 1);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.5.5.3.a - Engine #1 (0) reported > 1 for MIL on");
    }

    @Test
    public void testFailureForMoreThanOnePermanent() {
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 5);
        obdModuleInformation.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 2);
        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.5.5.3.a - Engine #1 (0) reported > 1 for permanent");
    }

    @Test
    public void testWarningForMoreThanOneModuleWithMILOn() {

        var dtc0 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm29_0 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);

        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation1.set(DM27AllPendingDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm29_1 = DM29DtcCounts.create(1, 0, 0, 0, 1, 0, 0);

        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29_0, dm29_1));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.5.5.3.b - More than one ECU reported > 0 for MIL on");
    }

    @Test
    public void testWarningForMoreThanOneModuleWithPermanent() {
        var dtc0 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        obdModuleInformation0.set(DM27AllPendingDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm29_0 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 1);

        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 9);
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM28PermanentEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1), 5);
        obdModuleInformation1.set(DM27AllPendingDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc1), 5);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm29_1 = DM29DtcCounts.create(1, 0, 0, 0, 0, 0, 1);

        when(communicationsModule.requestDM29(any())).thenReturn(new RequestResult<>(false, dm29_0, dm29_1));

        runTest();

        verify(communicationsModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.5.5.3.b - More than one ECU reported > 0 for permanent");
    }
}
