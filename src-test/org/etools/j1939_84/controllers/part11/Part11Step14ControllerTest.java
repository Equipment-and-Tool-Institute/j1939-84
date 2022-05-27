/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 14;

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

    private TestDateTimeModule dateTimeModule;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);

        dateTimeModule = new TestDateTimeModule();

        instance = new Part11Step14Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
        String OFF_MSG = "Wait for the manufacturer's recommended interval with the key off" + NL
                + NL + "Press OK to continue";

        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("500.0 RPMs",
                                                                    "---- RPMs",
                                                                    // Engine Off
                                                                    "---- RPMs",
                                                                    "500.0 RPMs",
                                                                    // Engine On
                                                                    "500.0 RPMs",
                                                                    "---- RPMs",
                                                                    // Engine off
                                                                    "---- RPMs",
                                                                    "0.0 RPMs");
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_OFF,
                                                         // Key is now off
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         // Engine is now running
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_ON_ENGINE_RUNNING,
                                                         KEY_OFF,
                                                         // Engine is now off
                                                         KEY_OFF,
                                                         KEY_OFF,
                                                         KEY_ON_ENGINE_OFF);

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();
        verify(engineSpeedModule, atLeastOnce()).getKeyState();

        verify(mockListener).onUrgentMessage(eq("Please turn the key off"),
                                             eq("Step 6.11.14.1.a"),
                                             eq(WARNING),
                                             any());
        verify(mockListener).onUrgentMessage(eq("Please start the engine"),
                                             eq("Step 6.11.14.1.d"),
                                             eq(WARNING),
                                             any());
        verify(mockListener).onUrgentMessage(eq("Please turn the key off"),
                                             eq("Step 6.11.14.1.f"),
                                             eq(WARNING),
                                             any());
        verify(mockListener).onUrgentMessage(eq("Please turn the key on with the engine off"),
                                             eq("Step 6.11.14.1.h"),
                                             eq(WARNING),
                                             any());

        verify(mockListener).onUrgentMessage(eq(OFF_MSG), eq("Step 6.11.14.1.b"), eq(WARNING), any());
        verify(mockListener).onUrgentMessage(eq(OFF_MSG), eq("Step 6.11.14.1.g"), eq(WARNING), any());

        assertEquals(62000, dateTimeModule.getTimeAsLong());

        StringBuilder expectedMessages = new StringBuilder();
        expectedMessages.append("Step 6.11.14.1.a - Waiting for key off").append(NL);
        expectedMessages.append("Step 6.11.14.1.a - Waiting for key off...").append(NL);
        expectedMessages.append("Step 6.11.14.1.b - Waiting manufacturer’s recommended interval with the key off")
                        .append(NL);
        expectedMessages.append("Step 6.11.14.1.d - Waiting for engine start").append(NL);
        expectedMessages.append("Step 6.11.14.1.d - Waiting for engine start...").append(NL);
        for (int i = 60; i > 0; i--) {
            expectedMessages.append("Step 6.11.14.1.e - Waiting ").append(i).append(" seconds").append(NL);
        }
        expectedMessages.append("Step 6.11.14.1.f - Waiting for key off").append(NL);
        expectedMessages.append("Step 6.11.14.1.f - Waiting for key off...").append(NL);
        expectedMessages.append("Step 6.11.14.1.g - Waiting manufacturer’s recommended interval with the key off")
                        .append(NL);
        expectedMessages.append("Step 6.11.14.1.h - Waiting for key on with engine off").append(NL);
        expectedMessages.append("Step 6.11.14.1.h - Waiting for key on with engine off...");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = ---- RPMs" + NL;
        expectedResults += "Initial Engine Speed = ---- RPMs" + NL;
        expectedResults += "Final Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Initial Engine Speed = 500.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = ---- RPMs" + NL;
        expectedResults += "Initial Engine Speed = ---- RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;

        assertEquals(expectedResults, listener.getResults());
    }

}
