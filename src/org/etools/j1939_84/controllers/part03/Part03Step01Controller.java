/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.isDevEnv;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.1 Confirm engine running status
 */
public class Part03Step01Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 0;

    Part03Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step01Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
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

    @Override
    protected void run() throws Throwable {
        try {
            // 6.3.1.1.a Gather broadcast data for engine speed (e.g., SPN 190).
            if (getEngineSpeedModule().isEngineNotRunning() && !isDevEnv()) {
                getListener().onResult("Initial Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");

                // 6.3.1.2.a Warn If engine speed is < 400 rpm, prompt/warn operator to confirm engine is running and then press enter
                getListener().onUrgentMessage("Please turn the Engine ON with Key ON.", "Adjust Key Switch", WARNING);

                while (getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine ON...");
                    getDateTimeModule().pauseFor(500);
                }
            }
            getListener().onResult("Final Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");
        } catch (InterruptedException e) {
            getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, "User cancelled operation");
            throw e;
        }
    }

}
