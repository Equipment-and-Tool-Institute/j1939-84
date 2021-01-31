/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.INCOMPLETE;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
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

    private final DataRepository dataRepository;

    Part01Step01Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step01Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(),
              dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
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
                    dataRepository.setVehicleInformation(vehInfo);
                }
            }

            @Override public ResultsListener getResultsListener() {
                return getListener();
            }
        });

        while (dataRepository.getVehicleInformation() == null) {
            getDateTimeModule().pauseFor(500);
            updateProgress("Part 1, Step 1 e Collecting Vehicle Information");
        }

        getListener().onResult(dataRepository.getVehicleInformation().toString());
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
        QuestionListener questionListener = answerType -> {
            //end test if user hits cancel button
            if (answerType == CANCEL) {
                getListener().addOutcome(getPartNumber(),
                                         getStepNumber(),
                                         INCOMPLETE,
                                         "Stopping test - user ended test");
                try {
                    getListener().onResult("User cancelled the test at Part " + getPartNumber() + " Step " + getStepNumber());
                    setEnding(Ending.STOPPED);
                    incrementProgress("User cancelled testing");
                } catch (InterruptedException ignored) {
                }
            }
        };
        getListener().onUrgentMessage(message, "Start Part 1", WARNING, questionListener);
    }

    /**
     * Ensures the Key is on with the Engine Off and prompts the user to make
     * the proper adjustments.
     *
     * @throws InterruptedException
     *         if the user cancels the operation
     */
    private void ensureKeyOnEngineOff() throws InterruptedException {
        try {
            if (!getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onUrgentMessage("Please turn the Engine OFF with Key ON", "Adjust Key Switch", WARNING);
                while (!getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(1, 2, ABORT, "User cancelled operation");
            setEnding(Ending.STOPPED);
            incrementProgress("User Aborted");
        }
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

}
