/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.FAST_FLASH;
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
public class Part05Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 3;

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

        instance = new Part05Step03Controller(executor,
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
        // Module 0 responds properly
        var dtc0 = DiagnosticTroubleCode.create(123, 14, 1, 1);
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc0);

        // Module 1 is an OBD Module without a DM12 from the previous step
        var dtc1 = DiagnosticTroubleCode.create(456, 14, 1, 1);
        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm1_1 = DM1ActiveDTCsPacket.create(1, FAST_FLASH, OFF, OFF, OFF, dtc1);

        // Module 2 is a non-OBD Module
        var dm1_2 = DM1ActiveDTCsPacket.create(2, OFF, OFF, OFF, OFF);

        when(communicationsModule.readDM1(any())).thenReturn(List.of(dm1_0, dm1_1, dm1_2));

        runTest();

        verify(communicationsModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForDifferentDTCs() {
        var dtc0 = DiagnosticTroubleCode.create(123, 14, 1, 1);
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc1 = DiagnosticTroubleCode.create(456, 14, 1, 1);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, dtc1);

        when(communicationsModule.readDM1(any())).thenReturn(List.of(dm1_0));

        runTest();

        verify(communicationsModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.3.2.a - Engine #1 (0) DM1 response does not include SPN = 123, FMI = 14 in the previous DM12 response");
    }

    @Test
    public void testFailureForDifferentMILStatus() {
        var dtc0 = DiagnosticTroubleCode.create(123, 14, 1, 1);
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 5);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, FAST_FLASH, OFF, OFF, OFF, dtc0);

        when(communicationsModule.readDM1(any())).thenReturn(List.of(dm1_0));

        runTest();

        verify(communicationsModule).readDM1(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.5.3.2.b - Engine #1 (0) DM1 response has a different MIL status than the previous DM12 response");
    }

}
