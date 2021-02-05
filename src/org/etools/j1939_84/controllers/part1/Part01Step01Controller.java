/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part01Step01Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 3;

    Part01Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             DataRepository.getInstance(),
             DateTimeModule.getInstance());
    }

    Part01Step01Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    /**
     * Sends the request to the UI to gather vehicle information from the user.
     *
     * @throws InterruptedException
     *         if the cancelled the operation
     */
    private void collectVehicleInformation() throws InterruptedException {

        getListener().onVehicleInformationNeeded(new VehicleInformationListener() {

            @Override public void onResult(VehicleInformation vehInfo) {
                if (vehInfo == null) {
                    try {
                        getListener().onResult("User cancelled the test at Part " + getPartNumber() + " Step " + getStepNumber());
                        setEnding(Ending.STOPPED);
                        incrementProgress("User cancelled testing");
                    } catch (InterruptedException e) {
                        // This will be caught later.
                    }
                } else {
                    getDataRepository().setVehicleInformation(vehInfo);
                    getListener().onResult(getDataRepository().getVehicleInformation().toString());
                }
            }

            @Override public ResultsListener getResultsListener() {
                return getListener();
            }
        });

        while (getDataRepository().getVehicleInformation() == null) {
            getDateTimeModule().pauseFor(500);
            updateProgress("Part 1, Step 1 e Collecting Vehicle Information");
        }
        getListener().onVehicleInformationReceived(getDataRepository().getVehicleInformation());
    }

    /**
     * Displays a warning message to the user.
     */
    private void displayWarningMessage() {
        String message = "Ready to begin Part 1" + NL;
        message += "a. Confirm the vehicle is in a safe location and condition for the test" + NL;
        message += "b. Confirm that the vehicle battery is well charged. (Battery voltage >> 12 volts)" + NL;
        message += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions"
                + NL;
        String boxTitle = "Start Part 1";
        displayInstructionAndWait(message, boxTitle, WARNING);
    }

    @Override
    protected void run() throws Throwable {
        incrementProgress("Part 1, Step 1 a-c Displaying Warning Message");
        //if (!J1939_84.isTesting()) {
        displayWarningMessage();
        //}

        incrementProgress("Part 1, Step 1 d Ensuring Key On, Engine Off");
        //if (!J1939_84.isTesting()) {
        ensureKeyOnEngineOff();
        //}

        incrementProgress("Part 1, Step 1 e Collecting Vehicle Information");
        collectVehicleInformation();
    }

}
