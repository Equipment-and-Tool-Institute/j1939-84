/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
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
public class Part04Step06ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
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

        instance = new Part04Step06Controller(executor,
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
        OBDModuleInformation module0 = new OBDModuleInformation(0);
        module0.set(DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, DiagnosticTroubleCode.create(123, 3, 0, 1)), 4);
        dataRepository.putObdModule(module0);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoActiveDTCs() {
        OBDModuleInformation module0 = new OBDModuleInformation(0);
        module0.set(DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF), 4);
        dataRepository.putObdModule(module0);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.6.2.a - No OBD ECU reported number of active DTCs as > 0");
    }

    @Test
    public void testFailureForDifferentNumberDTCs() {
        OBDModuleInformation module0 = new OBDModuleInformation(0);
        module0.set(DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, DiagnosticTroubleCode.create(123, 3, 0, 1)), 4);
        dataRepository.putObdModule(module0);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 2, 0, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.6.2.b - Engine #1 (0) reported a different number of active DTCs than it did in DM1 response earlier in this part.");
    }

    @Test
    public void testFailureForPreviouslyActiveDTCs() {
        OBDModuleInformation module0 = new OBDModuleInformation(0);
        module0.set(DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF, DiagnosticTroubleCode.create(123, 3, 0, 1)), 4);
        dataRepository.putObdModule(module0);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(RequestResult.of(dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.6.2.c - Engine #1 (0) reported > 0 previously active DTCs");
    }

}
