/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
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
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
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

/**
 * The unit test for {@link Step27Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Step27ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 27;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step27Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);

        instance = new Step27Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                mockListener);
    }

    /**
     * Test method for {@link Step27Controller#run()}.
     */
    // FIXME - this needs to be fixed when we figure out how to throw the
    // InterruptedException.
    @Test
    public void testEngineThrowInterruptedException() {
        String expectedTitle = "Start Part 2";
        ResultsListener.MessageType expectedType = QUESTION;
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);
        // doThrow(InterruptedException.class).when(engineSpeedModule).isEngineRunning();
        // when(engineSpeedModule.isEngineRunning()).thenThrow(new
        // InterruptedException("Engine failed to start"));

        // ArgumentCaptor<QuestionListener> questionCaptor =
        // ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1."
                + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?"
                + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                eq(expectedTitle),
                eq(expectedType),
                any());
        // questionCaptor.getValue().answered(NO);
        String urgentMessages2 = "Please turn the Engine ON with Key ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        String expectedMessages = NL;
        expectedMessages += "Part 1, Step 27 Part 1 to Part 2 Transition" + NL;
        expectedMessages += "Part 1, Step 27 b.i Ensuring Key On, Engine On" + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Part 1, Step 27 b.iii Allowing engine to idle one minute";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for {@link StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for {@link StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for {@link StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Step27Controller#run()}.
     */
    @Test
    public void testRun() {
        String expectedTitle = "Start Part 2";
        ResultsListener.MessageType expectedType = QUESTION;
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1."
                + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?"
                + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                eq(expectedTitle),
                eq(expectedType),
                questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "Please turn the Engine ON with Key ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        String expectedMessages = NL;
        expectedMessages += "Part 1, Step 27 Part 1 to Part 2 Transition" + NL;
        expectedMessages += "Part 1, Step 27 b.i Ensuring Key On, Engine On" + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Part 1, Step 27 b.iii Allowing engine to idle one minute";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";// "User provided ";
        // expectedResults += "\n";
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Step27Controller#run()}.
     */
    @Test
    public void testUserAbortForFail() {
        String expectedTitle = "Start Part 2";
        ResultsListener.MessageType expectedType = QUESTION;
        PartResultRepository partResultRepository = PartResultRepository.getInstance();
        StepResult stepResult = new StepResult(1, 3, "Testing Result");
        stepResult.addResult(new ActionOutcome(PASS, "6.1.2.1.a - Pass for testing"));
        stepResult.addResult(new ActionOutcome(FAIL, "6.1.2.1.b - Fail for testing"));

        partResultRepository.setStepResult(1, stepResult);

        when(engineSpeedModule.isEngineRunning()).thenReturn(false, false, false, true);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineRunning();

        String urgentMessages = "";
        urgentMessages += "Ready to transition from Part 1 to Part 2 of the test" + NL;
        urgentMessages += "a. Testing may be stopped for vehicles with failed tests and for vehicles with the MIL on or a non-emissions related fault displayed in DM1."
                + NL;
        urgentMessages += "   Vehicles with the MIL on will fail subsequent tests." + NL + NL;
        urgentMessages += "This vehicle has had failures and will likely fail subsequent tests.  Would you still like to continue?"
                + NL;
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                eq(expectedTitle),
                eq(expectedType),
                questionCaptor.capture());
        questionCaptor.getValue().answered(NO);
        String urgentMessages2 = "Please turn the Engine ON with Key ON";
        String expectedTitle2 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2), eq(expectedTitle2), eq(WARNING));

        String outcomeMessage = "Aborting - user ended test";
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, Outcome.ABORT, outcomeMessage);

        String expectedMessages = NL;
        expectedMessages += "Part 1, Step 27 Part 1 to Part 2 Transition" + NL;
        expectedMessages += "Part 1, Step 27 b.i Ensuring Key On, Engine On" + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Part 1, Step 27 b.iii Allowing engine to idle one minute";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMilestones());
    }

}
