/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
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
 * The unit test for {@link Step01Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(description = "Part 1 Step 1 KOEO Data Collection")
public class Step01ControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 1;
    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

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
    public void setUp() {
        listener = new TestResultsListener(mockListener);

        instance = new Step01Controller(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                mockListener);
    }

    /**
     * Test method for {@link Step01Controller#getDisplayName()}.
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.1", description = "Verifies part and step name for report"))
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 1", instance.getDisplayName());
    }

    /**
     * Test method for {@link StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for {@link StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for {@link Step01Controller#getTotalSteps()}.
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.1", description = "Verifies that there is a single 6.1.1 step"))
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Step01Controller#run()}.
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.1.1.a,b,c", dependsOn = "UserInterfacePresenterTest",
                    description = "Verify that the UI presents the data."),
            @TestItem(verifies = "6.1.1.1.d", dependsOn = "EngineSpeedModuleTest"),
            @TestItem(verifies = "6.1.1.1", dependsOn = { "VehicleInformationTest", "VehicleInformationModuleTest" }),
            @TestItem(verifies = "6.1.1.1.e.i",
                    dependsOn = { "VehicleInformationTest", "VehicleInformationModuleTest.testGetVin",
                            "VehicleInformationModuleTest.testGetVinNoResponse",
                            "VehicleInformationModuleTest.testGetVinWithDifferentResponses",
                            "VehicleInformationModuleTest.testReportVin",
                            "VehicleInformationModuleTest.testReportVinWithNoResponses", "VinDecoderTest" }),
            @TestItem(verifies = "6.1.1.1.e.ii",
                    dependsOn = { "VinDecoderTest" }),
            @TestItem(verifies = "6.1.1.1.e.iv",
                    dependsOn = { "FuelTypeTest" }),
            @TestItem(verifies = "6.1.1.1.e.v",
                    dependsOn = { "VehicleInformationTest",
                            "VehicleInformationModuleTest.testReportCalibrationInformation",
                            "VehicleInformationModuleTest.testReportCalibrationInformationWithNoResponses",
                            "VehicleInformationModuleTest.testRunHappyPath",
                            "VehicleInformationModuleTest.testRunNoModulesRespond",
                            "VehicleInformationModuleTest.testRunWithWarningsAndFailures" }),
    },
            description = "Verify vehicle data collection and that the correct instructions to the user are transmistted to the UI presenter.")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testRun() {

        String expectedTitle = "Start Part 1";
        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(vehicleInfo.toString()).thenReturn("VehicleInfo");

        when(engineSpeedModule.isEngineNotRunning()).thenReturn(true);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        String urgentMessages = "";
        urgentMessages += "Ready to begin Part 1\n";
        urgentMessages += "a. Confirm the vehicle is in a safe location and condition for the test.\n";
        urgentMessages += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts).\n";
        urgentMessages += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions.\n";

        verify(dataRepository, atLeastOnce()).getVehicleInformation();
        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule).isEngineNotRunning();
        verify(mockListener).onUrgentMessage(urgentMessages, expectedTitle, WARNING);
        verify(mockListener).onVehicleInformationReceived(vehicleInfo);
        verify(vehicleInformationModule).setJ1939(j1939);

        ArgumentCaptor<VehicleInformationListener> vehicleInfoCaptor = ArgumentCaptor
                .forClass(VehicleInformationListener.class);
        verify(mockListener).onVehicleInformationNeeded(vehicleInfoCaptor.capture());
        vehicleInfoCaptor.getValue().onResult(vehicleInfo);
        verify(dataRepository).setVehicleInformation(vehicleInfo);

        String expectedMessages = "\n";
        expectedMessages += "Part 1, Step 1 a-c Displaying Warning Message\n";
        expectedMessages += "Part 1, Step 1 d Ensuring Key On, Engine Off\n";
        expectedMessages += "Part 1, Step 1 e Collecting Vehicle Information";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = vehicleInfo + "\n";
        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for {@link Step01Controller#run()}.
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.1.1.a,b,c", dependsOn = "UserInterfacePresenterTest"),
            @TestItem(verifies = "6.1.1.1.d", dependsOn = "EngineSpeedModuleTest"),
            @TestItem(verifies = "6.1.1.1.e",
                    dependsOn = { "VehicleInformationTest", "VehicleInformationModuleTest" }),
    },
            description = "Verify vehicle data collection is empty when the engine is not running and no data is collected.")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testRunVehicleInfoNull() {

        when(engineSpeedModule.isEngineNotRunning()).thenReturn(true);
        when(dataRepository.getVehicleInformation()).thenReturn(null);

        ArgumentCaptor<VehicleInformationListener> vehicleInfoCaptor = ArgumentCaptor
                .forClass(VehicleInformationListener.class);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    verify(mockListener).onVehicleInformationNeeded(vehicleInfoCaptor.capture());
                    vehicleInfoCaptor.getValue().onResult(null);
                    timer.cancel();
                } catch (Throwable t) {
                    // Expected
                }
            }
        }, 10, 10);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        String urgentMessages = "";
        urgentMessages += "Ready to begin Part 1\n";
        urgentMessages += "a. Confirm the vehicle is in a safe location and condition for the test.\n";
        urgentMessages += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts).\n";
        urgentMessages += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions.\n";

        verify(dataRepository, atLeastOnce()).getVehicleInformation();
        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule).isEngineNotRunning();
        verify(mockListener).onUrgentMessage(urgentMessages, "Start Part 1", WARNING);
        verify(vehicleInformationModule).setJ1939(j1939);

        String expectedMessages = "\n";
        expectedMessages += "Part 1, Step 1 a-c Displaying Warning Message\n";
        expectedMessages += "Part 1, Step 1 d Ensuring Key On, Engine Off\n";
        expectedMessages += "Part 1, Step 1 e Collecting Vehicle Information\n";
        expectedMessages += "Part 1, Step 1 e Collecting Vehicle Information";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.1.1.a,b,c", dependsOn = "UserInterfacePresenterTest"),
            @TestItem(verifies = "6.1.1.1.d", dependsOn = "EngineSpeedModuleTest"),
            @TestItem(verifies = "6.1.1.1.e",
                    dependsOn = { "VehicleInformationTest", "VehicleInformationModuleTest" }) },
            description = "After the key was detected off, notify user to 'Please turn the Engine OFF with Key ON.', then continue with data collection.")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    public void testWaitForKey() {
        when(engineSpeedModule.isEngineNotRunning()).thenReturn(false);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                instance.stop();
            }
        }, 500);

        try {
            instance.execute(listener, j1939, reportFileModule);
            ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
            verify(executor).execute(runnableCaptor.capture());
            runnableCaptor.getValue().run();
        } catch (Throwable e) {
            e.printStackTrace();

        }
        verify(engineSpeedModule).setJ1939(j1939);
        verify(engineSpeedModule, atLeastOnce()).isEngineNotRunning();
        verify(vehicleInformationModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 2, ABORT, "User cancelled operation");

        String urgentMessages = "";
        urgentMessages += "Ready to begin Part 1\n";
        urgentMessages += "a. Confirm the vehicle is in a safe location and condition for the test.\n";
        urgentMessages += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts).\n";
        urgentMessages += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions.\n";
        verify(mockListener).onUrgentMessage(urgentMessages, "Start Part 1", WARNING);

        verify(mockListener).onUrgentMessage("Please turn the Engine OFF with Key ON", "Adjust Key Switch", WARNING);

        String expectedMessages = "\n";
        expectedMessages += "Part 1, Step 1 a-c Displaying Warning Message\n";
        expectedMessages += "Part 1, Step 1 d Ensuring Key On, Engine Off\n";
        expectedMessages += "Waiting for Key ON, Engine OFF...\n";
        expectedMessages += "Waiting for Key ON, Engine OFF...";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }

}
