/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
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
    private CommunicationsModule communicationsModule;

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
                                              communicationsModule);

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
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
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

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        ArgumentCaptor<QuestionListener> questionCaptor = ArgumentCaptor.forClass(QuestionListener.class);
        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();

        verify(mockListener).onUrgentMessage(eq("Please turn the key off"),
                                             eq("Step 6.4.15.1.a"),
                                             eq(WARNING),
                                             any());

        String urgentMessages = "Wait for the manufacturer's recommended interval with the key off" + NL
                + NL;
        urgentMessages += "Press OK to continue";
        verify(mockListener).onUrgentMessage(eq(urgentMessages),
                                             eq("Step 6.4.15.1.b"),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String urgentMessages2 = "With the key in the off position, remove the implanted Fault A according to the"
                + NL;
        urgentMessages2 += "manufacturer’s instructions for restoring the system to a fault-free operating condition"
                + NL + NL;
        urgentMessages2 += "Press OK to continue";
        verify(mockListener).onUrgentMessage(eq(urgentMessages2),
                                             eq("Step 6.4.15.1.c"),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        verify(mockListener).onUrgentMessage(eq("Please start the engine"),
                                             eq("Step 6.4.15.1.f"),
                                             eq(WARNING),
                                             any());

        String urgentMessages3 = "Wait for manufacturer’s recommended time for Fault A to be detected (as passed)"
                + NL + NL;
        urgentMessages3 += "Press OK to continue";
        verify(mockListener).onUrgentMessage(eq(urgentMessages3),
                                             eq("Step 6.4.15.1.g"),
                                             eq(WARNING),
                                             questionCaptor.capture());
        questionCaptor.getValue().answered(YES);

        String expectedMessages = "Step 6.4.15.1.a - Waiting for key off" + NL +
                "Step 6.4.15.1.a - Waiting for key off..." + NL +
                "Step 6.4.15.1.b - Waiting manufacturer’s recommended interval with the key off" + NL +
                "Step 6.4.15.1.c - Waiting for implanted Fault A to be removed" + NL +
                "Step 6.4.15.1.f - Waiting for engine start" + NL +
                "Step 6.4.15.1.f - Waiting for engine start..." + NL +
                "Step 6.4.15.1.g - Waiting for manufacturer’s recommended time for Fault A to be detected (as passed)";
        assertEquals(expectedMessages, listener.getMessages());

        String expected = "";
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        expected += "Initial Engine Speed = 0.0 RPMs" + NL;
        expected += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expected, listener.getResults());
    }

}
