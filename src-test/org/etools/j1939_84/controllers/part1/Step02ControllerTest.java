/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
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
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step02Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 2", description = "Verify engine operation"))
public class Step02ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step02Controller instance;

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

        instance = new Step02Controller(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule);
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
    @TestDoc(value = @TestItem(verifies = "6.1.2", description = "Verifies part and step name for report."))
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 2", instance.getDisplayName());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.2", description = "Verifies that there is a single 6.1.2 step."))
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.2.1.a",
                               description = "Verify if the engine is running that there are no messages when already KOEO.",
                               dependsOn = { "EngineSpeedModuleTest" }))
    public void testRun() {
        when(engineSpeedModule.isEngineNotRunning()).thenReturn(true);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule).isEngineNotRunning();
        verify(vehicleInformationModule).setJ1939(j1939);

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.2.1.a",
                               description = "Verify user is requested to turn KOEO when engine is not KOEO.",
                               dependsOn = { "EngineSpeedModuleTest" }))
    public void testWaitForKeyOn() {
        when(engineSpeedModule.isEngineNotRunning()).thenReturn(false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 750);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineNotRunning();
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(mockListener).onUrgentMessage("Please turn the Engine OFF with Key ON.", "Adjust Key Switch", WARNING);

        String expectedMessages = "";
        expectedMessages += "Waiting for Key ON, Engine OFF..." + NL;
        expectedMessages += "Waiting for Key ON, Engine OFF...";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }

}
