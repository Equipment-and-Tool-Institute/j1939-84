package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step01Controller extends Controller {

    private final DataRepository dataRepository;

    Step01Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(), dataRepository);
    }

    Step01Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dataRepository = dataRepository;
    }

    /**
     * Sends the request to the UI to gather vehicle information from the user.
     *
     * @throws InterruptedException if the cancelled the operation
     */
    private void collectVehicleInformation() throws InterruptedException {
        getListener().onVehicleInformationNeeded(vehInfo -> {
            if (vehInfo == null) {
                try {
                    setEnding(Ending.ABORTED);
                } catch (InterruptedException e) {
                    // This will be caught later.
                }
            } else {
                dataRepository.setVehicleInformation(vehInfo);
            }
        });

        while (dataRepository.getVehicleInformation() == null) {
            Thread.sleep(500);
            updateProgress("Part 1, Step 1 e Collecting Vehicle Information"); // To check for test aborted
        }

        getListener().onResult("User provided " + dataRepository.getVehicleInformation());
        getListener().onVehicleInformationReceived(dataRepository.getVehicleInformation());
    }

    /**
     * Displays a warning message to the user.
     */
    private void displayWarningMessage() {
        String message = "Ready to begin Part 1" + NL;
        message += "a. Confirm the vehicle is in a safe location and condition for the test." + NL;
        message += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts)." + NL;
        message += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions."
                + NL;

        getListener().onUrgentMessage(message, "Start Part 1", MessageType.WARNING);
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make the
     * proper adjustments.
     *
     * @throws InterruptedException if the user cancels the operation
     */
    private void ensureKeyOnEngineOff() throws InterruptedException {
        try {
            if (!getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onUrgentMessage("Please turn the Engine OFF with Key ON.", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(1, 2, Outcome.ABORT, "User cancelled operation");
            throw e;
        }
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 1 Test";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        incrementProgress("Part 1, Step 1 a-c Displaying Warning Message");
        displayWarningMessage();

        incrementProgress("Part 1, Step 1 d Ensuring Key On, Engine Off");
        ensureKeyOnEngineOff();

        incrementProgress("Part 1, Step 1 e Collecting Vehicle Information");
        collectVehicleInformation();
    }

    @Override
    public void stop() {
        System.out.println("Stop");
        super.stop();
    }
}
