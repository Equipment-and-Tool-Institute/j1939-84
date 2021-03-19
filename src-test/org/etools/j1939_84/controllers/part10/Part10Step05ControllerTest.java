/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.VehicleInformation;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part10Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 5;

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

        instance = new Part10Step05Controller(executor,
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

        // ensureKeyOffEngineOff()
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "500.0 RPMs");

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setNumberOfTripsForFaultBImplant(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Step 6.10.5.1.a - Wait for manufacturer’s recommended time for Fault B to be detected as passed"
                + NL + NL;
        urgentMessages += "Press OK to continue";
        String expectedTitle = "Step 6.10.5.1.a";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.10.5.1.c. Turn engine off.
        String urgentMessages1 = "Please turn key off";
        String expectedTitle1 = "Step 6.10.5.1.c";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.10.5.1.e. Start engine.
        String urgentMessages1_5 = "Please start the engine";
        String expectedTitle1_5 = "Step 6.10.5.1.e";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5),
                                                            eq(expectedTitle1_5),
                                                            eq(WARNING),
                                                            any());

        StringBuilder expectedMessages = new StringBuilder(
                                                           "Step 6.10.5.1.a - Waiting for manufacturer’s recommended time for Fault B to be detected as passed"
                                                                   + NL);
        for (int i = 120; i > 0; i--) {
            expectedMessages.append(format("Step 6.10.5.1.b - Waiting %1$d seconds to establish second cycle", i))
                            .append(NL);
        }
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off").append(NL);
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off...").append(NL);
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off...").append(NL);
        for (int i = 60; i > 0; i--) {
            expectedMessages.append(format("Step 6.10.5.1.d - Waiting %1$d seconds", i)).append(NL);
        }
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;

        assertEquals(expected, listener.getResults());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }

    @Test
    public void testAnsweredNoToQuestion() {

        // ensureKeyOffEngineOff()
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "500.0 RPMs");

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setNumberOfTripsForFaultBImplant(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Step 6.10.5.1.a - Wait for manufacturer’s recommended time for Fault B to be detected as passed"
                + NL + NL;
        urgentMessages += "Press OK to continue";
        String expectedTitle = "Step 6.10.5.1.a";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.10.5.1.c. Turn engine off.
        String urgentMessages1 = "Please turn key off";
        String expectedTitle1 = "Step 6.10.5.1.c";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        // 6.10.5.1.e. Start engine.
        String urgentMessages1_5 = "Please start the engine";
        String expectedTitle1_5 = "Step 6.10.5.1.e";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5),
                                                            eq(expectedTitle1_5),
                                                            eq(WARNING),
                                                            any());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 10 Step 5");

        StringBuilder expectedMessages = new StringBuilder("Step 6.10.5.1.a - Waiting for manufacturer’s recommended time for Fault B to be detected as passed"
                                                                   + NL);
        for (int i = 120; i > 0; i--) {
            expectedMessages.append(format("Step 6.10.5.1.b - Waiting %1$d seconds to establish second cycle", i))
                            .append(NL);
        }
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off").append(NL);
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off...").append(NL);
        expectedMessages.append("Step 6.10.5.1.c - Waiting for key off...").append(NL);
        for (int i = 60; i > 0; i--) {
            expectedMessages.append(format("Step 6.10.5.1.d - Waiting %1$d seconds", i)).append(NL);
        }
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.10.5.1.e - Waiting for engine start...").append(NL);
        expectedMessages.append("User cancelled testing at Part 10 Step 5");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;

        assertEquals(expected, listener.getResults());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }
}
