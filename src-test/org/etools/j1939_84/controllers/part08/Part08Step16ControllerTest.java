/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
public class Part08Step16ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 16;

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

        instance = new Part08Step16Controller(executor,
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
                                                                    "250.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // 6.8.16.1.a Turn the engine off.
        String urgentMessages1 = "Please turn Key OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.b Wait manufacturer's recommended interval.
        String urgentMessages2 = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.8.16.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.c With the key in the off position remove the implanted Fault B, according to the manufacturer’s
        // instructions for restoring the system to a fault- free operating condition.
        String urgentMessages3 = "Step 6.8.16.1.c - With the key in the off position remove the implanted Fault B, according to the manufacturer’s"
                + NL;
        urgentMessages3 += "instructions for restoring the system to a fault- free operating condition." + NL;
        urgentMessages3 += "Press OK to continue the testing.";
        String expectedTitle3 = "Test 8.16";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3),
                                             eq(expectedTitle3),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.d Turn the ignition key to the ON position.
        String urgentMessages4 = "Please turn Key ON/Engine OFF";
        String expectedTitle4 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages4),
                                                            eq(expectedTitle4),
                                                            eq(WARNING),
                                                            any());

        String urgentMessages5 = "Step 6.8.16.1.e - Do Not Start Engine." + NL;
        urgentMessages5 += "Step 6.8.16.1.f - Proceeding with part 9." + NL;
        urgentMessages5 += "Press OK to continue the testing.";
        String expectedTitle5 = "Test 8.16";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages5),
                                                            eq(expectedTitle5),
                                                            eq(WARNING),
                                                            questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String expectedMessages = "Waiting for Key OFF..." + NL;
        expectedMessages += "Waiting for Key OFF..." + NL;
        expectedMessages += "Step 6.8.16.1.c - With Key OFF, remove the implanted Fault B"
                + NL;
        expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        expectedMessages += "Step 6.8.16.1.e & f - Do Not Start Engine - proceeding with part 9";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 250.0 RPMs" + NL;
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
                                                                    "250.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // 6.8.16.1.a Turn the engine off.
        String urgentMessages1 = "Please turn Key OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.b Wait manufacturer's recommended interval.
        String urgentMessages2 = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.8.16.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.c With the key in the off position remove the implanted Fault B, according to the manufacturer’s
        // instructions for restoring the system to a fault- free operating condition.
        String urgentMessages3 = "Step 6.8.16.1.c - With the key in the off position remove the implanted Fault B, according to the manufacturer’s"
                + NL;
        urgentMessages3 += "instructions for restoring the system to a fault- free operating condition." + NL;
        urgentMessages3 += "Press OK to continue the testing.";
        String expectedTitle3 = "Test 8.16";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3),
                                             eq(expectedTitle3),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.8.16.1.d Turn the ignition key to the ON position.
        String urgentMessages4 = "Please turn Key ON/Engine OFF";
        String expectedTitle4 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages4),
                                                            eq(expectedTitle4),
                                                            eq(WARNING),
                                                            any());

        String urgentMessages5 = "Step 6.8.16.1.e - Do Not Start Engine." + NL;
        urgentMessages5 += "Step 6.8.16.1.f - Proceeding with part 9." + NL;
        urgentMessages5 += "Press OK to continue the testing.";
        String expectedTitle5 = "Test 8.16";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages5),
                                                            eq(expectedTitle5),
                                                            eq(WARNING),
                                                            questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 8 Step 16");

        String expectedMessages = "Waiting for Key OFF..." + NL;
        expectedMessages += "Waiting for Key OFF..." + NL;
        expectedMessages += "Step 6.8.16.1.c - With Key OFF, remove the implanted Fault B"
                + NL;
        expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        expectedMessages += "Step 6.8.16.1.e & f - Do Not Start Engine - proceeding with part 9" + NL;
        expectedMessages += "User cancelled testing at Part 8 Step 16";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 250.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }
}
