/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.CANCEL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.INCOMPLETE;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part01Step02Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part01Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             DateTimeModule.getInstance());
    }

    Part01Step02Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
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
    }

    @Override
    protected void run() throws Throwable {
        try {
            if (!getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onResult("Initial Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");

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
                getListener().onUrgentMessage("Please turn the Engine OFF with Key ON.",
                                              "Adjust Key Switch",
                                              WARNING,
                                              questionListener);

                while (!getEngineSpeedModule().isEngineNotRunning() && getEnding() == null) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    getDateTimeModule().pauseFor(500);
                }
            }
            getListener().onResult("Final Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");
        } catch (InterruptedException e) {
            getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, "User cancelled operation");
            getListener().onResult("User cancelled the test at Part " + getPartNumber() + " Step " + getStepNumber());
            throw e;
        }
    }
}
