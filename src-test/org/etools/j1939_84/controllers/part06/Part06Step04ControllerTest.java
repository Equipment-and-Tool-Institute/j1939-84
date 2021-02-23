/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
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
public class Part06Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
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

        instance = new Part06Step04Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc));
        obdModuleInformation.set(DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22));
        dataRepository.putObdModule(obdModuleInformation);

        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, NOT_SUPPORTED, NOT_SUPPORTED, NOT_SUPPORTED, NOT_SUPPORTED);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNoMILOn() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        obdModuleInformation.set(DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22));
        dataRepository.putObdModule(obdModuleInformation);

        var dm1_0 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, ON, ON, ON, ON);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.6.4.2.a - No OBD ECU reported MIL on");
    }

    @Test
    public void testFailureDifferentDTCs() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 9);
        var dtc2 = DiagnosticTroubleCode.create(463, 12, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1));
        obdModuleInformation.set(DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22));
        dataRepository.putObdModule(obdModuleInformation);

        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc2);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, ON, ON, ON, ON);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.4.2.b - The DTC (123:12) provided by Engine #1 (0) in DM12 is not included in its DM1 display");
    }

    @Test
    public void testFailureForDifferentCount() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 9);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc));
        obdModuleInformation.set(DM5DiagnosticReadinessPacket.create(0, 2, 0, 0x22));
        dataRepository.putObdModule(obdModuleInformation);

        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, ON, ON, ON, ON);
        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1));

        runTest();

        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.4.2.c - Engine #1 (0) reported a different number of active DTCs than what it reported in DM5 for number of active DTCs");
    }
}
