/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.PASS;
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

import net.solidDesign.j1939.J1939;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartResultRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
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

/**
 * The unit test for {@link Part01Step27Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step27ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 27;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step27Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();

        listener = new TestResultsListener(mockListener);

        instance = new Part01Step27Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dateTimeModule,
                                              DataRepository.newInstance());

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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
    }

    @Test
    public void testUserAbortForFail() {
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Testing may be stopped for vehicles with failed tests " + NL +
                "and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL +
                "Vehicles with the MIL on will fail subsequent tests." + NL +
                "" + NL +
                "This vehicle has had failures and will likely fail subsequent tests." + NL +
                "" + NL +
                "Would you like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages), eq("Step 6.1.27.1.a"), eq(QUESTION), any());

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq("Step 6.1.27.1.a"),
                                             eq(QUESTION),
                                             questionCaptor.capture());

        questionCaptor.getValue().answered(NO);

        String urgentMessages2 = "Please start the engine";
        String expectedTitle2 = "Step 6.1.27.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING), any());

        String outcomeMessage = "User cancelled testing at Part 1 Step 27";
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, outcomeMessage);

        StringBuilder expectedMessages = new StringBuilder("Step 6.1.27.1.b - Waiting for engine start" + NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL)
                            .append("Step 6.1.27.b.iii - Allowing engine to idle for ")
                            .append(i)
                            .append(" seconds");
        }
        expectedMessages.append(NL).append("User cancelled testing at Part 1 Step 27");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 3, instance.getTotalSteps());
    }

    @Test
    public void testRun() throws InterruptedException {
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();
        verify(engineSpeedModule, atLeastOnce()).getKeyState();

        String urgentMessages = "Testing may be stopped for vehicles with failed tests " + NL +
                "and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL +
                "Vehicles with the MIL on will fail subsequent tests." + NL +
                "" + NL +
                "This vehicle has had failures and will likely fail subsequent tests." + NL +
                "" + NL +
                "Would you like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq("Step 6.1.27.1.a"),
                                             eq(QUESTION),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "Please start the engine";
        String expectedTitle2 = "Step 6.1.27.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING), any());

        StringBuilder expectedMessages = new StringBuilder("Step 6.1.27.1.b - Waiting for engine start" + NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL)
                            .append("Step 6.1.27.b.iii - Allowing engine to idle for ")
                            .append(i)
                            .append(" seconds");
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expected = "";
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());
    }

    // FIXME - this needs to be fixed when we figure out how to throw the InterruptedException.
    @Test
    public void testEngineThrowInterruptedException() {
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(PART_NUMBER, STEP_NUMBER, "Testing Result");
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(PART_NUMBER, stepResult);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 750);

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Testing may be stopped for vehicles with failed tests " + NL +
                "and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL +
                "Vehicles with the MIL on will fail subsequent tests." + NL +
                "" + NL +
                "This vehicle has had failures and will likely fail subsequent tests." + NL +
                "" + NL +
                "Would you like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq("Step 6.1.27.1.a"),
                                             eq(QUESTION),
                                             any());
        String urgentMessages2 = "Please start the engine";
        String expectedTitle2 = "Step 6.1.27.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING), any());

        StringBuilder expectedMessages = new StringBuilder("Step 6.1.27.1.b - Waiting for engine start" + NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...").append(NL);
        expectedMessages.append("Step 6.1.27.1.b - Waiting for engine start...");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL)
                            .append("Step 6.1.27.b.iii - Allowing engine to idle for ")
                            .append(i)
                            .append(" seconds");
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expectedResults = "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

}
