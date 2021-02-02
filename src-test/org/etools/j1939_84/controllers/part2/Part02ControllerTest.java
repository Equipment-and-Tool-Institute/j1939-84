/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part02Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private Part02Step01Controller step01Controller;

    @Mock
    private Part02Step02Controller step02Controller;

    @Mock
    private Part02Step03Controller step03Controller;

    @Mock
    private Part02Step03Controller step04Controller;

    @Mock
    private Part02Step03Controller step05Controller;

    @Mock
    private Part02Step03Controller step06Controller;

    @Mock
    private Part02Step03Controller step07Controller;

    @Mock
    private Part02Step03Controller step08Controller;

    @Mock
    private Part02Step03Controller step09Controller;

    @Mock
    private Part02Step03Controller step10Controller;

    @Mock
    private Part02Step03Controller step11Controller;

    @Mock
    private Part02Step03Controller step12Controller;

    @Mock
    private Part02Step03Controller step13Controller;

    @Mock
    private Part02Step03Controller step14Controller;

    @Mock
    private Part02Step03Controller step15Controller;

    @Mock
    private Part02Step03Controller step16Controller;

    @Mock
    private Part02Step03Controller step17Controller;

    @Mock
    private Part02Step03Controller step18Controller;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part02Controller(executor,
                                        bannerModule,
                                        DateTimeModule.getInstance(),
                                        DataRepository.newInstance(),
                                        engineSpeedModule,
                                        vehicleInformationModule,
                                        diagnosticMessageModule,
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
                                        step18Controller);
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
                                 step18Controller);
    }

    /**
     * Test method for {@link Part02Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Test", instance.getDisplayName());
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testPart02Controller() {
        List<StepController> stepControllers = List.of(step01Controller,
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
                                                       step18Controller);

        for (int i = 0; i < stepControllers.size(); i++) {
            when(stepControllers.get(i).getStepNumber()).thenReturn(i + 1);
        }

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        InOrder inOrder = inOrder(
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
                step18Controller);

        for (StepController StepController : stepControllers) {
            inOrder.verify(StepController).run(any(ResultsListener.class), eq(j1939));
        }

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);

        for (StepController stepController : stepControllers) {
            verify(stepController).getTotalSteps();
            verify(stepController).getStepNumber();
        }

        StringBuilder expectedMilestones = new StringBuilder("Begin Part: " + Lookup.getPartName(2) + NL);
        for (int i = 1; i <= stepControllers.size(); i++) {
            expectedMilestones.append("Begin Step: Step 2.").append(i).append(". ").append(Lookup.getStepName(2, i))
                    .append(NL);
            expectedMilestones.append("End Step: Step 2.").append(i).append(". ").append(Lookup.getStepName(2, i))
                    .append(NL);
        }
        expectedMilestones.append("End Part: ").append(Lookup.getPartName(2));
        assertEquals(expectedMilestones.toString(), listener.getMilestones());

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= stepControllers.size(); i++) {
            expectedMessages.append(NL).append("Step 2.").append(i).append(". ").append(Lookup.getStepName(2, i));
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder(NL + "Start " + Lookup.getPartName(2) + NL);
        for (int i = 1; i <= stepControllers.size(); i++) {
            expectedResults.append(NL)
                    .append("Start Step 2.").append(i).append(". ").append(Lookup.getStepName(2, i))
                    .append(NL)
                    .append(NL)
                    .append(NL);
            expectedResults.append("End Step 2.").append(i).append(". ").append(Lookup.getStepName(2, i))
                    .append(NL);
        }
        expectedResults.append(NL).append(NL);
        expectedResults.append("End ").append(Lookup.getPartName(2)).append(NL).append(NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }

}
