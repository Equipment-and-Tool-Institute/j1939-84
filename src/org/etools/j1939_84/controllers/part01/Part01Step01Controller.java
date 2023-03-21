/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.model.VehicleInformationListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.DM5Heartbeat;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.1 Test Vehicle Data Collection
 */
public class Part01Step01Controller extends StepController {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 3;

    Part01Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             DataRepository.getInstance(),
             DateTimeModule.getInstance());
    }

    Part01Step01Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    /**
     * Sends the request to the UI to gather vehicle information from the user.
     *
     * @throws InterruptedException
     *                                  if the cancelled the operation
     */
    private void collectVehicleInformation() throws InterruptedException {

        getListener().onVehicleInformationNeeded(new VehicleInformationListener() {

            @Override
            public void onResult(VehicleInformation vehInfo) {
                if (vehInfo == null) {
                    try {
                        getListener().onResult("User cancelled testing at Part " + getPartNumber() + " Test "
                                + getStepNumber());
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

            @Override
            public ResultsListener getResultsListener() {
                return getListener();
            }
        });

        while (getDataRepository().getVehicleInformation() == null) {
            getDateTimeModule().pauseFor(500);
            updateProgress(getDisplayName() + " - Collecting Vehicle Information");
        }
        getListener().onVehicleInformationReceived(getDataRepository().getVehicleInformation());
    }

    @Override
    protected void run() throws Throwable {
        try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {

            incrementProgress("Step 6.1.1.1.a - Vehicle Data Collection");
            // 6.1.1.1.a. Confirm the vehicle is in a safe location and condition for the test.
            String message = "a. Confirm the vehicle is in a safe location and condition for the test" + NL;

            // 6.1.1.1.b. Confirm that the vehicle battery is well charged. ([Battery voltage >> 12 V].)
            message += "b. Confirm the vehicle battery is well charged. (Battery voltage >> 12 volts)" + NL;

            // 6.1.1.1.c. Confirm the vehicle condition and operator control settings according to the engine
            // manufacturer’s
            // instructions.
            message += "c. Confirm the vehicle condition and operator control settings according to the engine manufacturer’s instructions"
                    + NL + NL;
            message += "Please press OK to continue";
            displayInstructionAndWait(message, "Step 6.1.1.1.a, b & c", WARNING);

            // 6.1.1.1.d. Turn the ignition key to on.
            ensureKeyStateIs(KEY_ON_ENGINE_OFF, "6.1.1.1.d");
        }
        incrementProgress("Step 6.1.1.1.e - Collecting Vehicle Information");
        // 6.1.1.1.e. Record vehicle data base entries including:
        // 6.1.1.1.e.i. VIN of vehicle,
        // 6.1.1.1.e.ii. MY of vehicle,
        // 6.1.1.1.e.ii.1. Warn the user if the MY character of the VIN does not match the data entered by the user for
        // the vehicle model year.
        // 6.1.1.1.e.iii. MY of engine,
        // 6.1.1.1.e.iv. Fuel type,
        // 6.1.1.1.e.v. Number of emission or diagnostic-critical control units on vehicle (i.e., number that are
        // required to support CAL ID and CVN),3 and
        // 6.1.1.1.e.vi. Certification intent (U.S., Euro, etc.).
        collectVehicleInformation();
    }

}
