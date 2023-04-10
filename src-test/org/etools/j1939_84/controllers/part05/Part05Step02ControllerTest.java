/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
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
public class Part05Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 2;

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

        instance = new Part05Step02Controller(executor,
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
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dtc_1 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 3);
        dataRepository.putObdModule(obdModule0);

        var obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1), 3);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12, dm12_1));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailures() {
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dtc_1 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        var dtc_2 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, SLOW_FLASH, OFF, OFF, OFF, dtc_1, dtc_2);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12, dm12_1));

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 3);
        dataRepository.putObdModule(obdModule0);

        var obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1), 3);
        dataRepository.putObdModule(obdModule1);

        runTest();

        verify(communicationsModule).requestDM12(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.a - No OBD ECU reported MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.c - OBD ECU Engine #2 (1) had a discrepancy between reported DM12 DTCs and DM6 DTCs reported in 6.3.2");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.d - OBD ECU Engine #2 (1) reported a MIL as slow flash");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testEmptyFailures() {
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 3);
        dataRepository.putObdModule(obdModule0);

        var obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, SLOW_FLASH, OFF, OFF, OFF), 4);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12, dm12_1));

        runTest();

        verify(communicationsModule).requestDM12(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.a - No OBD ECU reported MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.b - All OBD ECUs report no DM12 DTCs");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testLampStateFailures() {
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        var dtc_1 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        var dtc_2 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, FAST_FLASH, OFF, OFF, OFF);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 4);
        dataRepository.putObdModule(obdModule0);

        var obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc_1, dtc_2), 3);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12, dm12_1));

        runTest();

        verify(communicationsModule).requestDM12(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.b - All OBD ECUs report no DM12 DTCs");
        // clarified with ticket #1227
        // verify(mockListener).addOutcome(PART_NUMBER,
        // STEP_NUMBER,
        // FAIL,
        // "6.5.2.2.c - OBD ECU Engine #2 (1) had a discrepancy between reported DM12 DTCs and DM6 DTCs reported in
        // 6.3.2");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.2.2.d - OBD ECU Engine #2 (1) reported a MIL as fast flash");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
