/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step01Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Step01ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step01Controller instance;

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
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Step01Controller(executor,
                                        engineSpeedModule,
                                        bannerModule,
                                        vehicleInformationModule,
                                        DateTimeModule.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Step 1", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testRun() {
        when(engineSpeedModule.isEngineNotRunning()).thenReturn(false);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule).getEngineSpeed();
        verify(engineSpeedModule).isEngineNotRunning();
        verify(vehicleInformationModule).setJ1939(j1939);

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "Final Engine Speed = 0.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testWaitForKeyOn() {
        when(engineSpeedModule.isEngineNotRunning()).thenReturn(true);
        when(engineSpeedModule.getEngineSpeed()).thenReturn(148.6);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                when(engineSpeedModule.isEngineNotRunning()).thenReturn(false);
            }
        }, 750);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineNotRunning();
        verify(engineSpeedModule, times(2)).getEngineSpeed();
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(mockListener).onUrgentMessage("Please turn the Engine ON with Key ON.", "Adjust Key Switch", WARNING);

        String expectedMessages = "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON...";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "Initial Engine Speed = 148.6 RPMs" + NL;
        expectedResults += "Final Engine Speed = 148.6 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testEngineThrowInterruptedException() {

        when(engineSpeedModule.isEngineNotRunning()).thenReturn(true);
        when(engineSpeedModule.getEngineSpeed()).thenReturn(300.0);
        instance.execute(listener, j1939, reportFileModule);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 750);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule).getEngineSpeed();
        verify(engineSpeedModule, atLeastOnce()).isEngineNotRunning();

        verify(mockListener).addOutcome(2, 1, ABORT, "User cancelled operation");
        verify(mockListener).onUrgentMessage("Please turn the Engine ON with Key ON.", "Adjust Key Switch", WARNING);

        verify(vehicleInformationModule).setJ1939(j1939);

        String expectedMessages = "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON..." + NL;
        expectedMessages += "Waiting for Key ON, Engine ON...";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "Initial Engine Speed = 300.0 RPMs" + NL;
        assertEquals(expectedResults, listener.getResults());
    }
}