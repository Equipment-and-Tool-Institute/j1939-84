/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractPartControllerTest {

    @Mock
    protected BannerModule bannerModule;

    @Mock
    protected EngineSpeedModule engineSpeedModule;

    @Mock
    protected Executor executor;

    protected PartController instance;

    @Mock
    protected J1939 j1939;

    protected TestResultsListener listener;

    @Mock
    protected ResultsListener mockListener;

    @Mock
    protected ReportFileModule reportFileModule;

    @Mock
    protected StepController step01Controller;

    @Mock
    protected StepController step02Controller;

    @Mock
    protected StepController step03Controller;

    @Mock
    protected StepController step04Controller;

    @Mock
    protected StepController step05Controller;

    @Mock
    protected StepController step06Controller;

    @Mock
    protected StepController step07Controller;

    @Mock
    protected StepController step08Controller;

    @Mock
    protected StepController step09Controller;

    @Mock
    protected StepController step10Controller;

    @Mock
    protected StepController step11Controller;

    @Mock
    protected StepController step12Controller;

    @Mock
    protected StepController step13Controller;

    @Mock
    protected StepController step14Controller;

    @Mock
    protected StepController step15Controller;

    @Mock
    protected StepController step16Controller;

    @Mock
    protected StepController step17Controller;

    @Mock
    protected StepController step18Controller;

    @Mock
    protected StepController step19Controller;

    @Mock
    protected StepController step20Controller;

    @Mock
    protected StepController step21Controller;

    @Mock
    protected StepController step22Controller;

    @Mock
    protected StepController step23Controller;

    @Mock
    protected StepController step24Controller;

    @Mock
    protected StepController step25Controller;

    @Mock
    protected StepController step26Controller;

    @Mock
    protected StepController step27Controller;

    @Mock
    protected VehicleInformationModule vehicleInformationModule;

    @Mock
    protected DiagnosticMessageModule diagnosticMessageModule;

    protected int partNumber;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 step01Controller,
                                 step02Controller,
                                 step03Controller,
                                 step04Controller,
                                 step05Controller,
                                 step06Controller,
                                 step07Controller,
                                 step08Controller,
                                 step09Controller,
                                 step10Controller,
                                 step11Controller,
                                 step12Controller,
                                 step13Controller,
                                 step14Controller,
                                 step15Controller,
                                 step16Controller,
                                 step17Controller,
                                 step18Controller,
                                 step19Controller,
                                 step20Controller,
                                 step21Controller,
                                 step22Controller,
                                 step23Controller,
                                 step24Controller,
                                 step25Controller,
                                 step26Controller,
                                 step27Controller);
    }

    @Test
    public void testGetDisplayName() {
        if (instance == null) {
            return;
        }

        assertEquals("Part " + partNumber + " Test", instance.getDisplayName());
    }

    @Test
    public void testRun() {
        if (instance == null) {
            return;
        }

        List<StepController> stepControllers = instance.getStepControllers();

        for (int i = 0; i < stepControllers.size(); i++) {
            when(stepControllers.get(i).getStepNumber()).thenReturn(i + 1);
        }

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        InOrder inOrder = inOrder(stepControllers.toArray());

        for (StepController StepController : stepControllers) {
            inOrder.verify(StepController).run(any(ResultsListener.class), eq(j1939));
        }

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);

        for (StepController stepController : stepControllers) {
            verify(stepController).getTotalSteps();
            verify(stepController).getStepNumber();
        }

        String partName = Lookup.getPartName(partNumber);

        StringBuilder expectedMilestones = new StringBuilder("Begin Part: " + partName + NL);
        for (int i = 1; i <= stepControllers.size(); i++) {
            String stepName = Lookup.getStepName(partNumber, i);
            expectedMilestones.append("Begin Step: Step ")
                              .append(partNumber)
                              .append(".")
                              .append(i)
                              .append(". ")
                              .append(stepName)
                              .append(NL);
            expectedMilestones.append("End Step: Step ")
                              .append(partNumber)
                              .append(".")
                              .append(i)
                              .append(". ")
                              .append(stepName)
                              .append(NL);
        }
        expectedMilestones.append("End Part: ").append(partName);
        assertEquals(expectedMilestones.toString(), listener.getMilestones());

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= stepControllers.size(); i++) {
            expectedMessages.append(NL)
                            .append("Step ")
                            .append(partNumber)
                            .append(".")
                            .append(i)
                            .append(". ")
                            .append(Lookup.getStepName(partNumber, i));
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder(NL + "Start " + partName + NL);
        for (int i = 1; i <= stepControllers.size(); i++) {
            String stepName = Lookup.getStepName(partNumber, i);
            expectedResults.append(NL)
                           .append("Start Step ")
                           .append(partNumber)
                           .append(".")
                           .append(i)
                           .append(". ")
                           .append(stepName)
                           .append(NL)
                           .append(NL)
                           .append(NL);
            expectedResults.append("End Step ")
                           .append(partNumber)
                           .append(".")
                           .append(i)
                           .append(". ")
                           .append(stepName)
                           .append(NL);
        }
        expectedResults.append(NL).append(NL);
        expectedResults.append("End ").append(partName).append(NL).append(NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }
}
