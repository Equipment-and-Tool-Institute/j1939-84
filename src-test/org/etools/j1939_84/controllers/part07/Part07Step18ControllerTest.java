/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_ON;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part07Step18ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 18;

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

        instance = new Part07Step18Controller(executor,
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
    // FIXME: we need a good way to test the repeated key state changes.
    public void testHappyPathNoFailuresOneFaultB() {

        // ensureKeyOffEngineOff()
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "500.0 RPMs",
                                                                    "500.0 RPMs");

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setNumberOfTripsForFaultBImplant(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // ensureKeyOffEngineOff()
        String urgentMessages_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages_5), eq(expectedTitle_5), eq(WARNING));

        String urgentMessages = "Implant Fault B according to engine manufacturer’s instruction"
                + NL;
        urgentMessages += "Press OK to continue the testing";
        String expectedTitle = "Step 6.2.18.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.d.
        String urgentMessages1 = "Please turn the Key ON with Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        // 6.7.18.1.e.
        String urgentMessages1_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle1_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5), eq(expectedTitle1_5), eq(WARNING));

        // 6.7.18.1.f.
        String urgentMessages2 = "Wait for manufacturer’s recommended time for Fault B to be detected as failed."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.7.18.1.f";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.g.
        String urgentMessages2_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle2_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages2_5), eq(expectedTitle2_5), eq(WARNING));

        // 6.7.18.1.h.
        String urgentMessages3 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages3 += "Press OK to continue the testing.";
        String expectedTitle3 = "Step 6.7.18.1.h";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages3),
                                                            eq(expectedTitle3),
                                                            eq(WARNING),
                                                            questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle3_5 = "Adjust Key Switch";
        verify(mockListener, times(2)).onUrgentMessage(eq(urgentMessages3_5), eq(expectedTitle3_5), eq(WARNING));

        String expectedMessages = "Step 6.7.18.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.b & c - Implant Fault B according to engine manufacturer’s instruction" + NL;
        expectedMessages += "Step 6.7.18.1.d - Turn key to on with the with the engine off" + NL;
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.e - Turn Engine On and keep the ignition key in the on position" + NL;
        expectedMessages += "Step 6.7.18.1.f - Waiting for manufacturer’s recommended time for Fault B to be detected as failed"
                + NL;
        expectedMessages += "Step 6.7.18.1.g - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.i - Turn Engine on with the ignition key in the on position" + NL;
        expectedMessages += "Step 6.7.18.1.j - Fault B is a single trip fault; proceeding with part 8 immediately";
        assertEquals(expectedMessages, listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
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
    public void testHappyPathNoFailuresTwoFaultB() {

        // ensureKeyOffEngineOff()
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    // ensureKeyOnEngineOff();
                                                                    "0.0 RPMs",
                                                                    "500.0 RPMs",
                                                                    // ensureKeyOnEngineOn();
                                                                    "500.0 RPMs",
                                                                    "500.0 RPMs",
                                                                    // ensureKeyOffEngineOff();
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    // ensureKeyOnEngineOn();
                                                                    "500.0 RPMs",
                                                                    "500.0 RPMs",
                                                                    // ensureKeyOffEngineOff();
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    // ensureKeyOnEngineOn();
                                                                    "500.0 RPMs",
                                                                    "500.0 RPMs");

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setNumberOfTripsForFaultBImplant(2);
        dataRepository.setVehicleInformation(vehicleInformation);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        assertEquals(List.of(), listener.getOutcomes());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // ensureKeyOffEngineOff()
        String urgentMessages_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages_5), eq(expectedTitle_5), eq(WARNING));

        String urgentMessages = "Implant Fault B according to engine manufacturer’s instruction"
                + NL;
        urgentMessages += "Press OK to continue the testing";
        String expectedTitle = "Step 6.2.18.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.e.
        String urgentMessages1 = "Please turn the Key ON with Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        // 6.7.18.1.f.
        String urgentMessages2 = "Wait for manufacturer’s recommended time for Fault B to be detected as failed."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.7.18.1.f";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.g.
        String urgentMessages2_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle2_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages2_5), eq(expectedTitle2_5), eq(WARNING));

        // 6.7.18.1.h.
        String urgentMessages3 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages3 += "Press OK to continue the testing.";
        String expectedTitle3 = "Step 6.7.18.1.h";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages3),
                                                            eq(expectedTitle3),
                                                            eq(WARNING),
                                                            questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle3_5 = "Adjust Key Switch";
        verify(mockListener, times(2)).onUrgentMessage(eq(urgentMessages3_5), eq(expectedTitle3_5), eq(WARNING));

        String urgentMessages4 = "Wait for manufacturer’s recommended time for Fault B to be detected as failed."
                + NL;
        urgentMessages4 += "Press OK to continue the testing.";
        String expectedTitle4 = "Step 6.7.18.1.k";
        verify(mockListener).onUrgentMessage(eq(urgentMessages4),
                                             eq(expectedTitle4),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages5 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages5 += "Press OK to continue the testing.";
        String expectedTitle5 = "Step 6.7.18.1.m";
        verify(mockListener).onUrgentMessage(eq(urgentMessages5),
                                             eq(expectedTitle5),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages4_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle4_5 = "Adjust Key Switch";
        verify(mockListener, times(2)).onUrgentMessage(eq(urgentMessages4_5), eq(expectedTitle4_5), eq(WARNING));

        String expectedMessages = "Step 6.7.18.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.b & c - Implant Fault B according to engine manufacturer’s instruction" + NL;
        expectedMessages += "Step 6.7.18.1.d - Turn key to on with the with the engine off" + NL;
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.e - Turn Engine On and keep the ignition key in the on position" + NL;
        expectedMessages += "Step 6.7.18.1.f - Waiting for manufacturer’s recommended time for Fault B to be detected as failed"
                + NL;
        expectedMessages += "Step 6.7.18.1.g - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.i - Turn Engine on with the ignition key in the on position" + NL;
        expectedMessages += "Step 6.7.18.1.j - Running fault B trip #2 of 2 total fault trips" + NL;
        expectedMessages += "Step 6.7.18.1.k - Waiting for manufacturer’s recommended time for Fault B to be detected as failed"
                + NL;
        expectedMessages += "Step 6.7.18.1.l - Turn Engine Off and keep the ignition key in the off position." + NL;
        expectedMessages += "Step 6.7.18.1.n & o - With the ignition key on and engine on proceeding to Part 8";
        assertEquals(expectedMessages, listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMilestones());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }

    @Test
    @Ignore
    public void testUserAbortForFail() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF_ENGINE_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs", "500.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_OFF);
            }
        }, 750);
        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        String urgentMessages = "Please wait for the manufacturer's recommended interval with the key in off position"
                + NL;
        urgentMessages += "Press OK to continue the testing" + NL;
        String expectedTitle = "Step 6.6.11.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages1 = "Please turn the Key ON with Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        String urgentMessages2 = "If required by engine manufacturer, start the engine for start to start operating cycle effects"
                + NL;
        urgentMessages2 += "Press OK when when ready to continue testing" + NL;
        String expectedTitle2 = "Step 6.6.11.d & e";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(NO);

        String urgentMessages4 = "Please wait for the manufacturer's recommended interval with the key in off position"
                + NL;
        urgentMessages4 += "Press OK to continue the testing" + NL;
        String expectedTitle4 = "Step 6.6.11.1.g";
        verify(mockListener).onUrgentMessage(eq(urgentMessages4),
                                             eq(expectedTitle4),
                                             eq(WARNING),
                                             questionCaptor.capture());

        String urgentMessages5 = "Turn the key to the on position" + NL;
        urgentMessages5 += "Proceeding with Part 7" + NL;
        urgentMessages5 += "Press OK when ready to continue testing" + NL;
        String expectedTitle5 = "Step 6.6.11.1.h-i";
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
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
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
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_ON,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_OFF_ENGINE_OFF,
                                                         KEY_ON_ENGINE_OFF,
                                                         KEY_ON_ENGINE_ON);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "0.0 RPMs",
                                                                    "500.0 RPMs",
                                                                    "500.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 2250);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setNumberOfTripsForFaultBImplant(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        // ensureKeyOffEngineOff()
        String urgentMessages_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages_5), eq(expectedTitle_5), eq(WARNING));

        String urgentMessages = "Implant Fault B according to engine manufacturer’s instruction"
                + NL;
        urgentMessages += "Press OK to continue the testing";
        String expectedTitle = "Step 6.2.18.1.b";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq(expectedTitle),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.d.
        String urgentMessages1 = "Please turn the Key ON with Engine OFF";
        String expectedTitle1 = "Adjust Key Switch";
        verify(mockListener).onUrgentMessage(eq(urgentMessages1), eq(expectedTitle1), eq(WARNING));

        // 6.7.18.1.e.
        String urgentMessages1_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle1_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages1_5), eq(expectedTitle1_5), eq(WARNING));

        // 6.7.18.1.f.
        String urgentMessages2 = "Wait for manufacturer’s recommended time for Fault B to be detected as failed."
                + NL;
        urgentMessages2 += "Press OK to continue the testing.";
        String expectedTitle2 = "Step 6.7.18.1.f";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq(expectedTitle2),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        // 6.7.18.1.g.
        String urgentMessages2_5 = "Please turn the Key OFF with Engine OFF";
        String expectedTitle2_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages2_5), eq(expectedTitle2_5), eq(WARNING));

        // 6.7.18.1.h.
        String urgentMessages3 = "Wait for the manufacturer's recommended interval with the key in off position."
                + NL;
        urgentMessages3 += "Press OK to continue the testing.";
        String expectedTitle3 = "Step 6.7.18.1.h";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages3),
                                                            eq(expectedTitle3),
                                                            eq(WARNING),
                                                            questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages3_5 = "Please turn the Key ON with Engine ON";
        String expectedTitle3_5 = "Adjust Key Switch";
        verify(mockListener, atLeastOnce()).onUrgentMessage(eq(urgentMessages3_5), eq(expectedTitle3_5), eq(WARNING));

        String expectedMessages = "Step 6.7.18.1.a - Turn Engine Off and keep the ignition key in the off position"
                + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.b & c - Implant Fault B according to engine manufacturer’s instruction" + NL;
        expectedMessages += "Step 6.7.18.1.d - Turn key to on with the with the engine off" + NL;
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.e - Turn Engine On and keep the ignition key in the on position" + NL;
        expectedMessages += "Step 6.7.18.1.f - Waiting for manufacturer’s recommended time for Fault B to be detected as failed"
                + NL;
        expectedMessages += "Step 6.7.18.1.g - Turn Engine Off and keep the ignition key in the off position" + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Waiting for Key OFF, Engine OFF..." + NL;
        expectedMessages += "Step 6.7.18.1.i - Turn Engine on with the ignition key in the on position";
        assertEquals(expectedMessages, listener.getMessages());

        assertEquals("", listener.getMilestones());
        String expected = "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;
        expected += "Initial Engine Speed = 500.0 RPMs" + NL;
        expected += "Final Engine Speed = 500.0 RPMs" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMilestones());

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
    }

}
