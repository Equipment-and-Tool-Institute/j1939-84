/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
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
public class Part04Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 15;

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

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);

        instance = new Part04Step15Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              DataRepository.newInstance(),
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
    public void testUserAbortForFail() {

        when(engineSpeedModule.isEngineCommunicating()).thenReturn(false, false, false, false);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        runTest();

        verify(engineSpeedModule, atLeastOnce()).isEngineCommunicating();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Please wait for the manufacturer's recommended interval with the key in off position" + NL;
        urgentMessages += "Press OK to continue the testing" + NL;
        String expectedTitle = "Part 6.4.15.1.b";
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());

        questionCaptor.getValue().answered(NO);

        String urgentMessages2 = "With the key in the off position remove the implanted Fault A according to the" + NL;
        urgentMessages2 += "manufacturer’s instructions for restoring the system to a fault- free operating condition" + NL;
        urgentMessages2 += "Press OK when ready to continue testing" + NL;
        String expectedTitle2 = "Part 6.4.15.1.c";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             any());

        String urgentMessages3 = "Turn ignition key to the ON position" + NL;
        urgentMessages3 += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
        urgentMessages3 += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
        urgentMessages3 += "Please wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL;
        urgentMessages3 += "Press OK when ready to continue testing" + NL;
        String expectedTitle3 = "Part 6.4.15.1.d-g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3), eq(expectedTitle3), eq(WARNING), any());

        String outcomeMessage = "User cancelled testing at Part 4 Step 15";
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, outcomeMessage);

        String expectedMessages = "Part 4, Step 15 Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for manufacturer's recommended interval with the key in off position" + NL;
        expectedMessages += "Part 4, Step 15 Remove implanted fault per manufacturer's instructions" + NL;
        expectedMessages += "User cancelled testing at Part 4 Step 15";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testRun() throws InterruptedException {

        when(engineSpeedModule.isEngineCommunicating()).thenReturn(false);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).isEngineCommunicating();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Please wait for the manufacturer's recommended interval with the key in off position" + NL;
        urgentMessages += "Press OK to continue the testing" + NL;
        String expectedTitle = "Part 6.4.15.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "With the key in the off position remove the implanted Fault A according to the" + NL;
        urgentMessages2 += "manufacturer’s instructions for restoring the system to a fault- free operating condition" + NL;
        urgentMessages2 += "Press OK when ready to continue testing" + NL;
        String expectedTitle2 = "Part 6.4.15.1.c";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3 = "Turn ignition key to the ON position" + NL;
        urgentMessages3 += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
        urgentMessages3 += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
        urgentMessages3 += "Please wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL;
        urgentMessages3 += "Press OK when ready to continue testing" + NL;
        String expectedTitle3 = "Part 6.4.15.1.d-g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3), eq(expectedTitle3), eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String expectedMessages = "Part 4, Step 15 Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for manufacturer's recommended interval with the key in off position" + NL;
        expectedMessages += "Part 4, Step 15 Remove implanted fault per manufacturer's instructions";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testEngineThrowInterruptedException() {

        when(engineSpeedModule.isEngineCommunicating()).thenReturn(false, false, false, false);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineCommunicating();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Please wait for the manufacturer's recommended interval with the key in off position" + NL;
        urgentMessages += "Press OK to continue the testing" + NL;
        String expectedTitle = "Part 6.4.15.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "With the key in the off position remove the implanted Fault A according to the" + NL;
        urgentMessages2 += "manufacturer’s instructions for restoring the system to a fault- free operating condition" + NL;
        urgentMessages2 += "Press OK when ready to continue testing" + NL;
        String expectedTitle2 = "Part 6.4.15.1.c";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        String urgentMessages3 = "Turn ignition key to the ON position" + NL;
        urgentMessages3 += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
        urgentMessages3 += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
        urgentMessages3 += "Please wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL;
        urgentMessages3 += "Press OK when ready to continue testing" + NL;
        String expectedTitle3 = "Part 6.4.15.1.d-g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3), eq(expectedTitle3), eq(WARNING), any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 4 Step 15");

        String expectedMessages = "Part 4, Step 15 Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for manufacturer's recommended interval with the key in off position" + NL;
        expectedMessages += "Part 4, Step 15 Remove implanted fault per manufacturer's instructions" + NL;
        expectedMessages += "User cancelled testing at Part 4 Step 15";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

}
