/**
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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Step01Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(verifies = "Part 1 Step 1", description = "KOEO Data Collection")
public class Step01ControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

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
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step01Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository,
                mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step01Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 1", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step01Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step01Controller#run()}.
     */
    @Test
    @TestDoc(verifies = {
            "6.1.1.1.a",
            "6.1.1.1.b",
            "6.1.1.1.c",
            "6.1.1.1.d",
            "6.1.1.1.e",
            "6.1.1.1.e.i",
            "6.1.1.1.e.ii",
            "6.1.1.1.2.a",
            "6.1.1.1.2.b",
            "6.1.1.1.2.c",
            "6.1.1.1.2.d" }, description = "Confirm the vehicle is in a safe location and condition for the test"
                    + "<br>"
                    + "Confirm that the vehicle battery is well charged. [Battery voltage >> 12 volts"
                    + "<br>"
                    + "Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions"
                    + "<br>"
                    + "Turn the ignition key to on"
                    + "<br>"
                    + "Record vehicle data base entries including: VIN of vehicle"
                    + "<br>"
                    + "Record vehicle data base entries including: MY of vehicle"
                    + "<br>"
                    + "Warn the user if the MY character of the VIN does not match the data entered by the user for the vehicle model year"
                    + "<br>"
                    + "MY of engine"
                    + "<br>"
                    + "Fuel type"
                    + "<br>"
                    + "Number of emission or diagnostic-critical control units on vehicle (i.e., number that are required to support CAL ID and CVN), 3 and"
                    + "<br>"
                    + "Certification intent (US, Euro, etc.)")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testRun() {

        String expectedTitle = "Start Part 1";
        MessageType expectedType = WARNING;
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
        verify(mockListener).onUrgentMessage(urgentMessages, expectedTitle, expectedType);
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

        String expectedResults = "User provided ";
        expectedResults += vehicleInfo;
        expectedResults += "\n";
        assertEquals(expectedResults, listener.getResults());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step01Controller#run()}.
     */
    @Test
    @TestDoc(verifies = {
            "6.1.1.1.a",
            "6.1.1.1.b",
            "6.1.1.1.c",
            "6.1.1.1.d",
            "6.1.1.1.e",
            "6.1.1.1.e.i",
            "6.1.1.1.e.ii",
            "6.1.1.1.2.a",
            "6.1.1.1.2.b",
            "6.1.1.1.2.c",
            "6.1.1.1.2.d" }, description = "Confirm the vehicle is in a safe location and condition for the test"
                    + "<br>"
                    + "Confirm that the vehicle battery is well charged. [Battery voltage >> 12 volts"
                    + "<br>"
                    + "Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions"
                    + "<br>"
                    + "Turn the ignition key to on"
                    + "<br>"
                    + "Record vehicle data base entries including: VIN of vehicle"
                    + "<br>"
                    + "Record vehicle data base entries including: MY of vehicle"
                    + "<br>"
                    + "Warn the user if the MY character of the VIN does not match the data entered by the user for the vehicle model year"
                    + "<br>"
                    + "MY of engine"
                    + "<br>"
                    + "Fuel type"
                    + "<br>"
                    + "Number of emission or diagnostic-critical control units on vehicle (i.e., number that are required to support CAL ID and CVN), 3 and"
                    + "<br>"
                    + "Certification intent (US, Euro, etc.)")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
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
    @TestDoc(verifies = {
            "6.1.1.1.a",
            "6.1.1.1.b",
            "6.1.1.1.c",
            "6.1.1.1.d",
            "6.1.1.1.e",
            "6.1.1.1.e.i",
            "6.1.1.1.e.ii",
            "6.1.1.1.2.a",
            "6.1.1.1.2.b",
            "6.1.1.1.2.c",
            "6.1.1.1.2.d" }, description = "Confirm the vehicle is in a safe location and condition for the test"
                    + "<br>"
                    + "Confirm that the vehicle battery is well charged. [Battery voltage >> 12 volts"
                    + "<br>"
                    + "Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions"
                    + "<br>"
                    + "Turn the ignition key to on but engine off"
                    + "<br>"
                    + "Record vehicle data base entries including: VIN of vehicle"
                    + "<br>"
                    + "Record vehicle data base entries including: MY of vehicle"
                    + "<br>"
                    + "Warn the user if the MY character of the VIN does not match the data entered by the user for the vehicle model year"
                    + "<br>"
                    + "MY of engine"
                    + "<br>"
                    + "Fuel type"
                    + "<br>"
                    + "Number of emission or diagnostic-critical control units on vehicle (i.e., number that are required to support CAL ID and CVN), 3 and"
                    + "<br>"
                    + "Certification intent (US, Euro, etc.)")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testWaitForKey() {
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

        verify(mockListener).addOutcome(1, 2, ABORT, "User cancelled operation");

        String urgentMessages = "";
        urgentMessages += "Ready to begin Part 1\n";
        urgentMessages += "a. Confirm the vehicle is in a safe location and condition for the test.\n";
        urgentMessages += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts).\n";
        urgentMessages += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions.\n";
        verify(mockListener).onUrgentMessage(urgentMessages, "Start Part 1", WARNING);

        verify(mockListener).onUrgentMessage("Please turn the Engine OFF with Key ON.", "Adjust Key Switch", WARNING);

        String expectedMessages = "\n";
        expectedMessages += "Part 1, Step 1 a-c Displaying Warning Message\n";
        expectedMessages += "Part 1, Step 1 d Ensuring Key On, Engine Off\n";
        expectedMessages += "Waiting for Key ON, Engine OFF...\n";
        expectedMessages += "Waiting for Key ON, Engine OFF...\n";
        expectedMessages += "Waiting for Key ON, Engine OFF...";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedMilestones = "";
        assertEquals(expectedMilestones, listener.getMilestones());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }

}
