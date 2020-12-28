/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

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
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.BannerModule;
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
 * The unit test for {@link Part01Controller}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private Step01Controller step01Controller;

    @Mock
    private Step02Controller step02Controller;

    @Mock
    private Step03Controller step03Controller;

    @Mock
    private Step04Controller step04Controller;

    @Mock
    private Step05Controller step05Controller;

    @Mock
    private Step06Controller step06Controller;

    @Mock
    private Step07Controller step07Controller;

    @Mock
    private Step08Controller step08Controller;

    @Mock
    private Step09Controller step09Controller;

    @Mock
    private Step10Controller step10Controller;

    @Mock
    private Step11Controller step11Controller;

    @Mock
    private Step12Controller step12Controller;

    @Mock
    private Step13Controller step13Controller;

    @Mock
    private Step14Controller step14Controller;

    @Mock
    private Step15Controller step15Controller;

    @Mock
    private Step16Controller step16Controller;

    @Mock
    private Step17Controller step17Controller;

    @Mock
    private Step18Controller step18Controller;

    @Mock
    private Step19Controller step19Controller;

    @Mock
    private Step20Controller step20Controller;

    @Mock
    private Step21Controller step21Controller;

    @Mock
    private Step22Controller step22Controller;

    @Mock
    private Step23Controller step23Controller;

    @Mock
    private Step24Controller step24Controller;

    @Mock
    private Step25Controller step25Controller;

    @Mock
    private Step26Controller step26Controller;

    @Mock
    private Step27Controller step27Controller;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);

        instance = new Part01Controller(executor,
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

    /**
     * Test method for {@link Part01Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Test", instance.getDisplayName());
    }

    /**
     * Test method for {@link Part01Controller#Part01Controller()}.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testPart01Controller() {
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

        for (int i = 1; i <= 27; i++) {
            when(stepControllers.get(i - 1).getStepNumber()).thenReturn(i);
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

        for (StepController StepController : stepControllers) {
            inOrder.verify(StepController).run(any(ResultsListener.class), eq(j1939));
        }

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);

        for (StepController stepController : stepControllers) {
            verify(stepController).getTotalSteps();
            verify(stepController).getStepNumber();
        }

        StringBuilder expectedMilestones = new StringBuilder("Begin Part: " + Lookup.getPartName(1) + NL);
        for (int i = 1; i <= 27; i++) {
            expectedMilestones.append("Begin Step: Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i))
                    .append(NL);
            expectedMilestones.append("End Step: Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i))
                    .append(NL);
        }
        expectedMilestones.append("End Part: ").append(Lookup.getPartName(1));
        assertEquals(expectedMilestones.toString(), listener.getMilestones());

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= 27; i++) {
            expectedMessages.append(NL).append("Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i));
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder("Start " + Lookup.getPartName(1) + NL);
        for (int i = 1; i <= 27; i++) {
            expectedResults.append(NL)
                    .append(NL)
                    .append("Start Step 1.")
                    .append(i).append(". ").append(Lookup.getStepName(1, i))
                    .append(NL);
            expectedResults.append("End Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i)).append(NL);
        }
        expectedResults.append("End ").append(Lookup.getPartName(1)).append(NL);
        assertEquals(expectedResults.toString(), listener.getResults());

    }

}
