/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step02Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 1;

    Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule());
    }

    Step02Controller(Executor executor,
            EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule,
            VehicleInformationModule vehicleInformationModule) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        try {
            if (!getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onUrgentMessage("Please turn the Engine OFF with Key ON.", "Adjust Key Switch", WARNING);

                while (!getEngineSpeedModule().isEngineNotRunning() && getEnding() == null) {
                    updateProgress("Waiting for Key ON, Engine OFF...");
                    Thread.sleep(500);
                }
            }
        } catch (InterruptedException e) {
            getListener().addOutcome(1, 2, ABORT, "User cancelled operation");
            throw e;
        }
    }
}
