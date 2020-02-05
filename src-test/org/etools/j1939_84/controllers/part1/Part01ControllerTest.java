/**
 * Unit test for Part01ControllerTest
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
 * The {@link Part01Controller} for the class under test
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
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
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener();
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
                step07Controller);
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
                step07Controller);
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
    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION", justification = "Matt thinks he knows what I'm doing")
    public void testPart01Controller() {
        PartResult partResult = mock(PartResult.class);
        when(partResult.toString()).thenReturn("Part 1");
        when(partResultFactory.create(1)).thenReturn(partResult);

        for (int i = 1; i < 27; i++) {
            StepResult stepResult = mock(StepResult.class);
            when(stepResult.toString()).thenReturn("Step " + i);
            when(partResult.getStepResult(i)).thenReturn(stepResult);
        }

        String expectedMessages = "";
        for (int i = 1; i < 27; i++) {
            expectedMessages += "\nStep " + i;
        }

        String expectedMilestones = "";
        expectedMilestones += "Begin Part: Part 1\n";
        for (int i = 1; i < 27; i++) {
            expectedMilestones += "Begin Step: Step " + i + "\n";
            expectedMilestones += "End Step: Step " + i + "\n";
        }
        expectedMilestones += "End Part: Part 1";

        String expectedResults = "Start Part 1\n";
        for (int i = 1; i < 27; i++) {
            expectedResults += "\n\nStart Step " + i + "\n";
            expectedResults += "End Step " + i + "\n";
        }
        expectedResults += "End Part 1\n";

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
                step07Controller);
        inOrder.verify(step01Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step02Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step03Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step04Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step05Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step06Controller).run(any(ResultsListener.class), eq(j1939));
        inOrder.verify(step07Controller).run(any(ResultsListener.class), eq(j1939));

        verify(partResultFactory).create(1);
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);

        assertEquals(expectedMilestones, listener.getMilestones());
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals(expectedResults, listener.getResults());
    }

}
