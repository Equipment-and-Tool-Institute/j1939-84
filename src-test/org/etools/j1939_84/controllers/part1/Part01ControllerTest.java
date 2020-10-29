/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Part01Controller}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01ControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DateTimeModule dateTimeModule;

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
    private PartResultFactory partResultFactory;

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
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Part01Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
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
                step17Controller);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
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
                step17Controller);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Part01Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Test", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Part01Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 28, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Part01Controller#Part01Controller()}.
     */
    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testPart01Controller() {
        PartResult partResult = mock(PartResult.class);
        when(partResult.toString()).thenReturn("Part 1");
        when(partResultFactory.create(1)).thenReturn(partResult);

        int[] steps = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 };

        for (int i : steps) {
            StepResult stepResult = mock(StepResult.class);
            when(stepResult.toString()).thenReturn("Step " + i);
            when(partResult.getStepResult(i)).thenReturn(stepResult);
        }

        StringBuffer expectedMessages = new StringBuffer();
        for (int i : steps) {
            expectedMessages.append("\nStep ").append(i);
        }

        StringBuffer expectedMilestones = new StringBuffer("Begin Part: Part 1\n");
        for (int i : steps) {
            expectedMilestones.append("Begin Step: Step ").append(i).append("\n");
            expectedMilestones.append("End Step: Step ").append(i).append("\n");
        }
        expectedMilestones.append("End Part: Part 1");

        StringBuffer expectedResults = new StringBuffer("Start Part 1\n");

        for (int i : steps) {
            expectedResults.append("\n\nStart Step ").append(i).append("\n");
            expectedResults.append("End Step ").append(i).append("\n");
        }
        expectedResults.append("End Part 1\n");

        when(step01Controller.getStepNumber()).thenReturn(1);
        when(step02Controller.getStepNumber()).thenReturn(2);
        when(step03Controller.getStepNumber()).thenReturn(3);
        when(step04Controller.getStepNumber()).thenReturn(4);
        when(step05Controller.getStepNumber()).thenReturn(5);
        when(step06Controller.getStepNumber()).thenReturn(6);
        when(step07Controller.getStepNumber()).thenReturn(7);
        when(step08Controller.getStepNumber()).thenReturn(8);
        when(step09Controller.getStepNumber()).thenReturn(9);
        when(step10Controller.getStepNumber()).thenReturn(10);
        when(step11Controller.getStepNumber()).thenReturn(11);
        when(step12Controller.getStepNumber()).thenReturn(12);
        when(step13Controller.getStepNumber()).thenReturn(13);
        when(step14Controller.getStepNumber()).thenReturn(14);
        when(step15Controller.getStepNumber()).thenReturn(15);
        when(step16Controller.getStepNumber()).thenReturn(16);
        when(step17Controller.getStepNumber()).thenReturn(17);

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        InOrder inOrder = inOrder(step01Controller,
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
                step17Controller);
        inOrder.verify(step01Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step02Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step03Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step04Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step05Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step06Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step07Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step08Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step09Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step10Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step11Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step12Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step13Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step14Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step15Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step16Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step17Controller).run(any(ResultsListener.class), eq(j1939));

        verify(partResultFactory).create(1);
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);

        verify(step01Controller).getStepNumber();
        verify(step02Controller).getStepNumber();
        verify(step03Controller).getStepNumber();
        verify(step04Controller).getStepNumber();
        verify(step05Controller).getStepNumber();
        verify(step06Controller).getStepNumber();
        verify(step07Controller).getStepNumber();
        verify(step08Controller).getStepNumber();
        verify(step09Controller).getStepNumber();
        verify(step10Controller).getStepNumber();
        verify(step11Controller).getStepNumber();
        verify(step12Controller).getStepNumber();
        verify(step13Controller).getStepNumber();
        verify(step14Controller).getStepNumber();
        verify(step15Controller).getStepNumber();
        verify(step16Controller).getStepNumber();
        verify(step17Controller).getStepNumber();

        assertEquals(expectedMilestones.toString(), listener.getMilestones());
        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals(expectedResults.toString(), listener.getResults());
    }

}
