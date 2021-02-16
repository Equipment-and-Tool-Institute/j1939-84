/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
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

import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartResultRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.StepResult;
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
    private DiagnosticMessageModule diagnosticMessageModule;

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
                                              diagnosticMessageModule,
                                              dateTimeModule,
                                              DataRepository.newInstance());

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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    @Test
    public void testUserAbortForFail() {
        String expectedTitle = "Start Part 2";
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeed();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages), eq("Start Part 2"), eq(QUESTION), any());

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(QUESTION),
                                             questionCaptor.capture());

        questionCaptor.getValue().answered(NO);

        String urgentMessages2 = "Please turn the Key ON with Engine ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        String outcomeMessage = "User cancelled testing at Part 1 Step 27";
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, outcomeMessage);

        StringBuilder expectedMessages = new StringBuilder("Part 1, Step 27 - Part 1 to Part 2 Transition" + NL);
        expectedMessages.append("Part 1, Step 27 b.i - Ensuring Key On, Engine On").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Part 1, Step 27 b.iii - Allowing engine to idle one minute");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL).append("Allowing engine to idle for ").append(i).append(" seconds");
        }
        expectedMessages.append(NL).append("User cancelled testing at Part 1 Step 27");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "ABORT: User cancelled testing at Part 1 Step 27" + NL;
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMilestones());
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
        String expectedTitle = "Start Part 2";
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeed();
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(QUESTION),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "Please turn the Key ON with Engine ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        StringBuilder expectedMessages = new StringBuilder("Part 1, Step 27 - Part 1 to Part 2 Transition" + NL);
        expectedMessages.append("Part 1, Step 27 b.i - Ensuring Key On, Engine On").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Part 1, Step 27 b.iii - Allowing engine to idle one minute");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL).append("Allowing engine to idle for ").append(i).append(" seconds");
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "";
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMilestones());
    }

    //FIXME - this needs to be fixed when we figure out how to throw the InterruptedException.
    @Test
    public void testEngineThrowInterruptedException() {
        String expectedTitle = "Start Part 2";
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(PART_NUMBER, STEP_NUMBER, "Testing Result");
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(PART_NUMBER, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeed();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1." + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?" + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(QUESTION),
                                             any());
        String urgentMessages2 = "Please turn the Key ON with Engine ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        StringBuilder expectedMessages = new StringBuilder("Part 1, Step 27 - Part 1 to Part 2 Transition" + NL);
        expectedMessages.append("Part 1, Step 27 b.i - Ensuring Key On, Engine On").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Waiting for Key ON, Engine ON...").append(NL);
        expectedMessages.append("Part 1, Step 27 b.iii - Allowing engine to idle one minute");
        int minuteCounter = 60;
        for (int i = minuteCounter; i > 0; i--) {
            expectedMessages.append(NL).append("Allowing engine to idle for ").append(i).append(" seconds");
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "";
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMilestones());
    }

}
