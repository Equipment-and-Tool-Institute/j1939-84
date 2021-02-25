/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
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
public class Part07Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 10;

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

        instance = new Part07Step10Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
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
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoPacket() {
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of());

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.b - No ECU reported > 0 previous MIL on");
    }

    @Test
    public void testFailureForPending() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 1, 0, 0, 1, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.a - Engine #1 (0) reported > 0 for pending");
    }

    @Test
    public void testFailureForAllPending() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 1, 0, 1, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.a - Engine #1 (0) reported > 0 for all pending");
    }

    @Test
    public void testFailureForMILOn() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 1, 1, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.a - Engine #1 (0) reported > 0 for MIL on");
    }

    @Test
    public void testFailureForPermanent() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 1, 1);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.a - Engine #1 (0) reported > 0 for permanent");
    }

    @Test
    public void testFailureForNoPrevious() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 0, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.10.2.b - No ECU reported > 0 previous MIL on");
    }

    @Test
    public void testWarningForMoreThanOnePrevious() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var dtc2 = DiagnosticTroubleCode.create(234, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2));
        dataRepository.putObdModule(obdModuleInformation);

        var dm29 = DM29DtcCounts.create(0, 0, 0, 0, 2, 0);
        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.7.10.3.a - Engine #1 (0) reported > 1 for previous MIL on");
    }

    @Test
    public void testWarningForMoreThanOnePreviousModule() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc0 = DiagnosticTroubleCode.create(123, 1, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc0));
        dataRepository.putObdModule(obdModuleInformation);
        var dm29_0 = DM29DtcCounts.create(0, 0, 0, 0, 1, 0);

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        var dtc1 = DiagnosticTroubleCode.create(234, 1, 1, 1);
        obdModuleInformation1.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(obdModuleInformation1);
        var dm29_1 = DM29DtcCounts.create(1, 0, 0, 0, 1, 0);

        when(diagnosticMessageModule.requestDM29(any())).thenReturn(RequestResult.of(dm29_0, dm29_1));

        runTest();

        verify(diagnosticMessageModule).requestDM29(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.7.10.3.b - More than one ECU reported > 0 for previous MIL on");
    }

}
