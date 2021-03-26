/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
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
public class Part08Step06ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 6;

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

        instance = new Part08Step06Controller(executor,
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
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        obdModuleInformation.set(DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2), 8);
        var dtc3 = DiagnosticTroubleCode.create(1098, 1, 0, 1);
        obdModuleInformation.set(DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc3), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm5_0 = DM5DiagnosticReadinessPacket.create(0, 2, 1, 0x22);
        when(diagnosticMessageModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5_0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm5_1 = DM5DiagnosticReadinessPacket.create(1, 0xFF, 0, 0x22);
        when(diagnosticMessageModule.requestDM5(any(), eq(1))).thenReturn(BusResult.of(dm5_1));

        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5_0, dm5_1));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM5(any(), eq(0));
        verify(diagnosticMessageModule).requestDM5(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForDifferentActiveCodes() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        obdModuleInformation.set(DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2), 8);
        var dtc3 = DiagnosticTroubleCode.create(1098, 1, 0, 1);
        obdModuleInformation.set(DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc3), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));
        when(diagnosticMessageModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM5(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.6.2.a - Engine #1 (0) reported different number of DTCs than correspond DM1 response earlier in this part");
    }

    @Test
    public void testFailureForDifferentPrevActiveCount() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        obdModuleInformation.set(DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2), 8);
        var dtc3 = DiagnosticTroubleCode.create(1098, 1, 0, 1);
        obdModuleInformation.set(DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc3), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 2, 2, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));
        when(diagnosticMessageModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM5(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.6.2.a - Engine #1 (0) reported different number of DTCs than correspond DM2 response earlier in this part");
    }

    @Test
    public void testFailureForDifferentGlobalVsDS() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        obdModuleInformation.set(DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2), 8);
        var dtc3 = DiagnosticTroubleCode.create(1098, 1, 0, 1);
        obdModuleInformation.set(DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc3), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var dm5_1 = DM5DiagnosticReadinessPacket.create(0, 2, 1, 0x22);

        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5_1));
        var dm5_2 = DM5DiagnosticReadinessPacket.create(0, 1, 2, 0x22);
        when(diagnosticMessageModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5_2));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM5(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.6.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }
}
