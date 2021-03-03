/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.DataRepository;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part02Step01Controller}
 * This step is similar to Part 01 Step 27 & Part 02 Step 18
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step01ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 1;

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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(null);
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        DataRepository dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part02Step01Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
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
    public void testRun() {
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("0.0 RPMs");

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();
        verify(engineSpeedModule, atLeastOnce()).getKeyState();

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 0.0 RPMs" + NL;
        expectedResults += "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testWaitForKeyOn() {
        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("148.6 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.getKeyState()).thenReturn(KEY_ON_ENGINE_RUNNING);
            }
        }, 750);

        runTest();

        verify(engineSpeedModule, atLeastOnce()).getKeyState();
        verify(engineSpeedModule, atLeastOnce()).getEngineSpeedAsString();
        verify(mockListener).onUrgentMessage("Please turn Key ON/Engine RUNNING", "Adjust Key Switch", WARNING);

        // String expectedMessages = "Waiting for Key ON/Engine RUNNING..." + NL;
        // expectedMessages += "Waiting for Key ON/Engine RUNNING...";
        // assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "Initial Engine Speed = 148.6 RPMs" + NL;
        expectedResults += "Final Engine Speed = 148.6 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testEngineThrowInterruptedException() {

        when(engineSpeedModule.getKeyState()).thenReturn(KEY_OFF);
        when(engineSpeedModule.getEngineSpeedAsString()).thenReturn("300.0 RPMs");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 750);

        runTest();

        verify(engineSpeedModule).getEngineSpeedAsString();
        verify(engineSpeedModule, atLeastOnce()).getKeyState();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, ABORT, "User cancelled testing at Part 2 Step 1");
        verify(mockListener).onUrgentMessage("Please turn Key ON/Engine RUNNING", "Adjust Key Switch", WARNING);

        String expectedMessages = "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "Waiting for Key ON/Engine RUNNING..." + NL;
        expectedMessages += "User cancelled testing at Part 2 Step 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "";
        expectedResults += "Initial Engine Speed = 300.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
