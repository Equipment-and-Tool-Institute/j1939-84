/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.ABORT;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.2.1 Part 2 Key On Engine Running Data Collection
 * </p>
 * <p>
 * Part 2 Purpose: Verify data in Key-on, engine running (KOER) operation
 * with no implanted faults.
 * </p>
 */
public class Step01Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 0;
    private final DateTimeModule dateTimeModule;

    Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                DateTimeModule.getInstance());
    }

    Step01Controller(Executor executor,
                     EngineSpeedModule engineSpeedModule,
                     BannerModule bannerModule,
                     VehicleInformationModule vehicleInformationModule,
                     DateTimeModule dateTimeModule) {
        super(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dateTimeModule,
                PART_NUMBER,
                STEP_NUMBER,
                TOTAL_STEPS);
        this.dateTimeModule = dateTimeModule;
    }

    @Override
    protected void run() throws Throwable {

        try {
            /*  6.2.1 Verify engine running
             *  6.2.1.1 Actions:
             *    a. Gather broadcast data for engine speed (e.g., SPN 190).
             */
            if (getEngineSpeedModule().isEngineNotRunning()) {
                getListener().onResult("Initial Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");
                /* 6.2.1.2 Warn criteria:
                *    a. If engine speed is < 400 rpm, prompt/warn operator to confirm engine is running and then press enter.
                */
                getListener().onUrgentMessage("Please turn the Engine ON with Key ON.", "Adjust Key Switch", WARNING);

                while (getEngineSpeedModule().isEngineNotRunning()) {
                    updateProgress("Waiting for Key ON, Engine ON...");
                    dateTimeModule.pauseFor(500);
                }
            }
            getListener().onResult("Final Engine Speed = " + getEngineSpeedModule().getEngineSpeed() + " RPMs");
        } catch (InterruptedException e) {
            getListener().addOutcome(getPartNumber(), getStepNumber(), ABORT, "User cancelled operation");
            throw e;
        }
    }
}
