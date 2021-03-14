/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
public class Part09Step25ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 25;

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

        instance = new Part09Step25Controller(executor,
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

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // 6.9.25.1.a. Turn Key Off.
        String urgentMessages1 = "Please turn Key OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.9.25.1.b. Wait manufacturer’s recommended interval.
        String urgentMessages = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "6.9.25.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.9.25.1.c. Turn ignition key to on position.
        String urgentMessages2 = "Please turn Key ON/Engine OFF";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.9.25.1.d. Start engine.
        String urgentMessages1_5 = "Please turn Key ON/Engine RUNNING";
        String expectedTitle1_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5),
                                                            eq(expectedTitle1_5),
                                                            eq(WARNING),
                                                            any());

        String expectedMessages = "Waiting for Key OFF..." + NL
                + "Waiting for Key OFF..." + NL
                + "6.9.25.1.b - Waiting manufacturer’s recommended interval with the Key OFF" + NL
                + "Waiting for Key ON/Engine OFF...";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
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

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // 6.9.25.1.a. Turn Key Off.
        String urgentMessages1 = "Please turn Key OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1),
                                             eq(expectedTitle1),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.9.25.1.b. Wait manufacturer’s recommended interval
        String urgentMessages = "Wait for the manufacturer's recommended interval with the Key OFF."
                + NL;
        urgentMessages += "Press OK to continue the testing.";
        String expectedTitle = "6.9.25.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        // 6.9.25.1.c. Turn ignition key to on position.
        String urgentMessages2 = "Please turn Key ON/Engine OFF";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             any());

        // 6.9.25.1.d. Start engine.
        String urgentMessages1_5 = "Please turn Key ON/Engine RUNNING";
        String expectedTitle1_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5),
                                                            eq(expectedTitle1_5),
                                                            eq(WARNING),
                                                            any());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 9 Step 25");

        String expectedMessages = "Waiting for Key OFF..." + NL
                + "Waiting for Key OFF..." + NL
                + "6.9.25.1.b - Waiting manufacturer’s recommended interval with the Key OFF" + NL
                + "Waiting for Key ON/Engine OFF..." + NL
                + "User cancelled testing at Part 9 Step 25";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }
}
