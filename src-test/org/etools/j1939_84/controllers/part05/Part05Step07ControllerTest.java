/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Timer;
import java.util.TimerTask;
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
public class Part05Step07ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 7;

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

        instance = new Part05Step07Controller(executor,
                                              bannerModule,
                                              DateTimeModule.getInstance(),
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

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs", "500.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING);
            }
        }, 750);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.5.7.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1 = "Please turn Key ON/Engine RUNNING";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING), any());

        String urgentMessages2 = "Wait for the manufacturer's recommended interval with the Key ON/Engine RUNNING."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.5.7.1.d";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3 = "Turn the engine off to complete the second cycle" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended interval with the key in the off position" + NL;
        urgentMessages3 += "Start the engine for part 6" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended time for Fault A to be detected as passed" + NL;
        urgentMessages3 += "Press OK to continue testing";
        String expectedTitle3 = "Step 6.5.7.1.e - g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3),
                                             eq(expectedTitle3),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String expectedMessages = "Step 6.5.7.1.a - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Step 6.5.7.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "Step 6.5.7.1.c Turn Engine on and keep the ignition key in the on position" + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting manufacturer’s recommended time for Fault A to be detected as passed";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }

    @Test
    public void testUserAbortForFail() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs", "500.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING);
            }
        }, 750);
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.5.7.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1 = "Please turn Key ON/Engine RUNNING";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING), any());

        String urgentMessages2 = "Wait for the manufacturer's recommended interval with the Key ON/Engine RUNNING."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.5.7.1.d";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        String urgentMessages3 = "Turn the engine off to complete the second cycle" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended interval with the key in the off position" + NL;
        urgentMessages3 += "Start the engine for part 6" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended time for Fault A to be detected as passed" + NL;
        urgentMessages3 += "Press OK to continue testing";
        String expectedTitle3 = "Step 6.5.7.1.e - g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3),
                                             eq(expectedTitle3),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(ABORT),
                                        eq("User cancelled testing at Part 5 Step 7"));

        String expectedMessages = "Step 6.5.7.1.a - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Step 6.5.7.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "Step 6.5.7.1.c Turn Engine on and keep the ignition key in the on position" + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting manufacturer’s recommended time for Fault A to be detected as passed" + NL;
        expectedMessages += "User cancelled testing at Part 5 Step 7";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());

    }

    @Test
    public void testEngineThrowInterruptedException() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING);
            }
        }, 750);
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.5.7.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1 = "Please turn Key ON/Engine RUNNING";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING), any());

        String urgentMessages2 = "Wait for the manufacturer's recommended interval with the Key ON/Engine RUNNING."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.5.7.1.d";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        String urgentMessages3 = "Turn the engine off to complete the second cycle" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended interval with the key in the off position" + NL;
        urgentMessages3 += "Start the engine for part 6" + NL;
        urgentMessages3 += "Wait for manufacturer’s recommended time for Fault A to be detected as passed" + NL;
        urgentMessages3 += "Press OK to continue testing";
        String expectedTitle3 = "Step 6.5.7.1.e - g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3), eq(expectedTitle3), eq(WARNING), any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 5 Step 7");

        String expectedMessages = "Step 6.5.7.1.a - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Step 6.5.7.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "Step 6.5.7.1.c Turn Engine on and keep the ignition key in the on position" + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting manufacturer’s recommended time for Fault A to be detected as passed" + NL;
        expectedMessages += "User cancelled testing at Part 5 Step 7";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

}
