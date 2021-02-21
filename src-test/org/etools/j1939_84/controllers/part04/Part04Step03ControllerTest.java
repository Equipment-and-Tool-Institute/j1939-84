/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
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
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part04Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 3;

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

        instance = new Part04Step03Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 1);

        OBDModuleInformation moduleInformation = new OBDModuleInformation(0);
        moduleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(moduleInformation);

        var dm1 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(0, listener.getOutcomes().size());
    }

    @Test
    public void testNoActiveDTCFailure() {
        var dm1 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(1, listener.getOutcomes().size());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.3.2.a - No ECU reported an active DTC and MIL on");
    }

    @Test
    public void testDifferentDTCsFailure() {
        // Module 0 Different DTCs
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 1, 1);
        OBDModuleInformation moduleInfo0 = new OBDModuleInformation(0);
        moduleInfo0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo0);
        var dtc2 = DiagnosticTroubleCode.create(456, 9, 1, 1);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc2);

        // Module 1 No active DTC in DM1
        OBDModuleInformation moduleInfo1 = new OBDModuleInformation(1);
        moduleInfo1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo1);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(3, listener.getOutcomes().size());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.3.2.b - Engine #1 (0) did not include its DM12 DTCs in the list of active DTCs");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.3.2.b - Engine #2 (1) did not include its DM12 DTCs in the list of active DTCs");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.3.2.c - Engine #2 (1) reported fewer active DTCs in its DM1 response than its DM12 response");
    }

    @Test
    public void testNonOBDActiveDTCWarning() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 1);

        var dm1 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(1, listener.getOutcomes().size());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.3.2.d - Non-OBD ECU Engine #1 (0) reported an active DTC");
    }

    @Test
    public void testMultipleActiveDTCWarningFromOneModule() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 1, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 9, 1, 1);

        OBDModuleInformation moduleInfo0 = new OBDModuleInformation(0);
        moduleInfo0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2));
        dataRepository.putObdModule(moduleInfo0);

        var dm1 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(1, listener.getOutcomes().size());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.3.2.e - More than 1 active DTC is reported by the vehicle");
    }

    @Test
    public void testMultipleActiveDTCWarningFromMultipleModules() {
        var dtc0 = DiagnosticTroubleCode.create(123, 12, 1, 1);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc0);
        OBDModuleInformation moduleInfo0 = new OBDModuleInformation(0);
        moduleInfo0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0));
        dataRepository.putObdModule(moduleInfo0);

        var dtc1 = DiagnosticTroubleCode.create(456, 9, 1, 1);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc1);
        OBDModuleInformation moduleInfo1 = new OBDModuleInformation(1);
        moduleInfo1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1));
        dataRepository.putObdModule(moduleInfo1);

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(1, listener.getOutcomes().size());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.3.2.e - More than 1 active DTC is reported by the vehicle");
    }

}
