/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.Executor;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    private Step01Controller step01Controller;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part02Controller(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                DateTimeModule.getInstance(),
                step01Controller);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                step01Controller);
    }

    /**
     * Test method for {@link Part02Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Test", instance.getDisplayName());
    }

    /**
     * Test method for {@link Part02Controller#Part02Controller()}.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testPart01Controller() {
        List<StepController> stepControllers = List.of(step01Controller);

        for (int i = 0; i < stepControllers.size(); i++) {
            when(stepControllers.get(i).getStepNumber()).thenReturn(i + 1);
        }

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        InOrder inOrder = inOrder(
                step01Controller);

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
                    .append(NL)
                    .append(NL)
                    .append(NL);
        }
        expectedResults.append("End ").append(Lookup.getPartName(2)).append(NL).append(NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }

}
