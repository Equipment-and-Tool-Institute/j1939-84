/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
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
public class Part06Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 11;

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

        instance = new Part06Step11Controller(executor,
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

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs", "500.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.6.11.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1 = "Please turn Key ON/Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        String urgentMessages2 = "If required by engine manufacturer, start the engine for start to start operating cycle effects"
                + NL;
        urgentMessages2 += "Press OK when ready to continue testing";
        String expectedTitle2 = "Step 6.6.11.d & e";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3 = "Please turn Key OFF";
        String expectedTitle3 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3), eq(expectedTitle3), eq(WARNING));

        String urgentMessages4 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages4 += "Press OK to continue the testing.";
        String expectedTitle4 = "Step 6.6.11.1.g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages4),
                                             eq(expectedTitle4),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages5 = "Turn the key to the on position" + NL;
        urgentMessages5 += "Proceeding with Part 7" + NL;
        urgentMessages5 += "Press OK when ready to continue testing";
        String expectedTitle5 = "Step 6.6.11.1.h - i";
        verify(mockListener).onUrgentMessage(eq(urgentMessages5),
                                             eq(expectedTitle5),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String expectedMessages = "Step 6.6.11.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Step 6.6.11.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "6.6.11.1.c Turn the ignition key in the on position" + NL;
        // expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        // expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        expectedMessages += "Step 6.6.11.g - Waiting manufacturer’s recommended interval with the key in the off position";
        assertEquals(expectedMessages, listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMilestones());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }

    @Test
    public void testUserAbortForFail() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs", "500.0 RPMs");

        // new Timer().schedule(new TimerTask() {
        // @Override
        // public void run() {
        // when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_ON);
        // }
        // }, 750);
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessage0 = "Please turn Key OFF";
        String expectedTitle0 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessage0), eq(expectedTitle0), eq(WARNING));

        String urgentMessages = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.6.11.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "If required by engine manufacturer, start the engine for start to start operating cycle effects"
                + NL;
        urgentMessages2 += "Press OK when ready to continue testing";
        String expectedTitle2 = "Step 6.6.11.d & e";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        String urgentMessages1 = "Please turn Key ON/Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        String urgentMessages4 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages4 += "Press OK to continue the testing.";
        String expectedTitle4 = "Step 6.6.11.1.g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages4),
                                             eq(expectedTitle4),
                                             eq(WARNING),
                                             questionCaptor.capture());

        String urgentMessages5 = "Turn the key to the on position" + NL;
        urgentMessages5 += "Proceeding with Part 7" + NL;
        urgentMessages5 += "Press OK when ready to continue testing";
        String expectedTitle5 = "Step 6.6.11.1.h - i";
        verify(mockListener).onUrgentMessage(eq(urgentMessages5),
                                             eq(expectedTitle5),
                                             eq(WARNING),
                                             questionCaptor.capture());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 6 Step 11");

        String expectedMessages = "Step 6.6.11.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Step 6.6.11.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "6.6.11.1.c Turn the ignition key in the on position" + NL;
        // expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        // expectedMessages += "Waiting for Key ON/Engine OFF..." + NL;
        expectedMessages += "Step 6.6.11.g - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "User cancelled testing at Part 6 Step 11";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());

    }

    @Test
    public void testEngineThrowInterruptedException() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 750);
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages1 = "Please turn Key OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        String urgentMessages = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "Step 6.6.11.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1_5 = "Please turn Key ON/Engine OFF";
        String expectedTitle1_5 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1_5), eq(expectedTitle1_5), eq(WARNING));

        String urgentMessages2 = "If required by engine manufacturer, start the engine for start to start operating cycle effects"
                + NL;
        urgentMessages2 += "Press OK when ready to continue testing";
        String expectedTitle2 = "Step 6.6.11.d & e";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                               STEP_NUMBER,
                                               ABORT,
                                               "User cancelled testing at Part 6 Step 11");

        String expectedMessages = "Step 6.6.11.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Step 6.6.11.1.b - Waiting manufacturer’s recommended interval with the key in the off position"
                + NL;
        expectedMessages += "6.6.11.1.c Turn the ignition key in the on position" + NL;
        expectedMessages += "Waiting for Key OFF..." + NL;
        expectedMessages += "Waiting for Key OFF..." + NL;
        expectedMessages += "Waiting for Key OFF..." + NL;
        expectedMessages += "User cancelled testing at Part 6 Step 11" + NL;
        expectedMessages += "User cancelled testing at Part 6 Step 11";
        assertEquals(expectedMessages, listener.getMessages());

        assertEquals("", listener.getMilestones());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMilestones());
    }
}
