/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step02Controller extends Controller {

    Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory());
    }

    Step02Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 2";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
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
            getListener().addOutcome(1, 2, Outcome.ABORT, "User cancelled operation");
            throw e;
        }
    }
}
