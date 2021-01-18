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
import org.etools.j1939_84.modules.DateTimeModule;
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
    private Part01Step01Controller part01Step01Controller;

    @Mock
    private Part01Step02Controller part01Step02Controller;

    @Mock
    private Part01Step03Controller part01Step03Controller;

    @Mock
    private Part01Step04Controller part01Step04Controller;

    @Mock
    private Part01Step05Controller part01Step05Controller;

    @Mock
    private Part01Step06Controller part01Step06Controller;

    @Mock
    private Part01Step07Controller part01Step07Controller;

    @Mock
    private Part01Step08Controller part01Step08Controller;

    @Mock
    private Part01Step09Controller part01Step09Controller;

    @Mock
    private Part01Step10Controller part01Step10Controller;

    @Mock
    private Part01Step11Controller part01Step11Controller;

    @Mock
    private Part01Step12Controller part01Step12Controller;

    @Mock
    private Part01Step13Controller part01Step13Controller;

    @Mock
    private Part01Step14Controller part01Step14Controller;

    @Mock
    private Part01Step15Controller part01Step15Controller;

    @Mock
    private Part01Step16Controller part01Step16Controller;

    @Mock
    private Part01Step17Controller part01Step17Controller;

    @Mock
    private Part01Step18Controller part01Step18Controller;

    @Mock
    private Part01Step19Controller part01Step19Controller;

    @Mock
    private Part01Step20Controller part01Step20Controller;

    @Mock
    private Part01Step21Controller part01Step21Controller;

    @Mock
    private Part01Step22Controller part01Step22Controller;

    @Mock
    private Part01Step23Controller part01Step23Controller;

    @Mock
    private Part01Step24Controller part01Step24Controller;

    @Mock
    private Part01Step25Controller part01Step25Controller;

    @Mock
    private Part01Step26Controller part01Step26Controller;

    @Mock
    private Part01Step27Controller part01Step27Controller;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Controller(executor,
                                        engineSpeedModule,
                                        bannerModule,
                                        vehicleInformationModule,
                                        DateTimeModule.getInstance(),
                                        part01Step01Controller,
                                        part01Step02Controller,
                                        part01Step03Controller,
                                        part01Step04Controller,
                                        part01Step05Controller,
                                        part01Step06Controller,
                                        part01Step07Controller,
                                        part01Step08Controller,
                                        part01Step09Controller,
                                        part01Step10Controller,
                                        part01Step11Controller,
                                        part01Step12Controller,
                                        part01Step13Controller,
                                        part01Step14Controller,
                                        part01Step15Controller,
                                        part01Step16Controller,
                                        part01Step17Controller,
                                        part01Step18Controller,
                                        part01Step19Controller,
                                        part01Step20Controller,
                                        part01Step21Controller,
                                        part01Step22Controller,
                                        part01Step23Controller,
                                        part01Step24Controller,
                                        part01Step25Controller,
                                        part01Step26Controller,
                                        part01Step27Controller);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 part01Step01Controller,
                                 part01Step02Controller,
                                 part01Step03Controller,
                                 part01Step04Controller,
                                 part01Step05Controller,
                                 part01Step06Controller,
                                 part01Step07Controller,
                                 part01Step08Controller,
                                 part01Step09Controller,
                                 part01Step10Controller,
                                 part01Step11Controller,
                                 part01Step12Controller,
                                 part01Step13Controller,
                                 part01Step14Controller,
                                 part01Step15Controller,
                                 part01Step16Controller,
                                 part01Step17Controller,
                                 part01Step18Controller,
                                 part01Step19Controller,
                                 part01Step20Controller,
                                 part01Step21Controller,
                                 part01Step22Controller,
                                 part01Step23Controller,
                                 part01Step24Controller,
                                 part01Step25Controller,
                                 part01Step26Controller,
                                 part01Step27Controller);
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
        List<StepController> stepControllers = List.of(part01Step01Controller,
                                                       part01Step02Controller,
                                                       part01Step03Controller,
                                                       part01Step04Controller,
                                                       part01Step05Controller,
                                                       part01Step06Controller,
                                                       part01Step07Controller,
                                                       part01Step08Controller,
                                                       part01Step09Controller,
                                                       part01Step10Controller,
                                                       part01Step11Controller,
                                                       part01Step12Controller,
                                                       part01Step13Controller,
                                                       part01Step14Controller,
                                                       part01Step15Controller,
                                                       part01Step16Controller,
                                                       part01Step17Controller,
                                                       part01Step18Controller,
                                                       part01Step19Controller,
                                                       part01Step20Controller,
                                                       part01Step21Controller,
                                                       part01Step22Controller,
                                                       part01Step23Controller,
                                                       part01Step24Controller,
                                                       part01Step25Controller,
                                                       part01Step26Controller,
                                                       part01Step27Controller);

        for (int i = 1; i <= 27; i++) {
            when(stepControllers.get(i - 1).getStepNumber()).thenReturn(i);
        }

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        InOrder inOrder = inOrder(
                part01Step01Controller,
                part01Step02Controller,
                part01Step03Controller,
                part01Step04Controller,
                part01Step05Controller,
                part01Step06Controller,
                part01Step07Controller,
                part01Step08Controller,
                part01Step09Controller,
                part01Step10Controller,
                part01Step11Controller,
                part01Step12Controller,
                part01Step13Controller,
                part01Step14Controller,
                part01Step15Controller,
                part01Step16Controller,
                part01Step17Controller,
                part01Step18Controller,
                part01Step19Controller,
                part01Step20Controller,
                part01Step21Controller,
                part01Step22Controller,
                part01Step23Controller,
                part01Step24Controller,
                part01Step25Controller,
                part01Step26Controller,
                part01Step27Controller);

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

        StringBuilder expectedResults = new StringBuilder(NL + "Start " + Lookup.getPartName(1) + NL);
        for (int i = 1; i <= 27; i++) {
            expectedResults.append(NL)
                    .append("Start Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i))
                    .append(NL)
                    .append(NL)
                    .append(NL)
                    .append("End Step 1.").append(i).append(". ").append(Lookup.getStepName(1, i))
                    .append(NL);
        }
        expectedResults.append(NL)
                .append(NL)
                .append("End ").append(Lookup.getPartName(1))
                .append(NL)
                .append(NL);
        assertEquals(expectedResults.toString(), listener.getResults());

    }

}
