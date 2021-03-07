/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.model.KeyState.KEY_OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.KeyState;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.11.13 Part 11 to Part 12 Transition
 */
public class Part11Step13Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part11Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step13Controller(Executor executor,
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
        // 6.11.13.1.a. Turn Engine Off.
        ensureKeyStateIs(KEY_OFF);

        // 6.11.13.1.b. Wait manufacturer's recommended interval.
        waitForManufacturerInterval("Step 6.11.31.1.b", KEY_OFF);

        // 6.11.13.1.c. Turn Key On.
        // 6.11.13.1.d. Start Engine Immediately.
        ensureKeyStateIs(KeyState.KEY_ON_ENGINE_RUNNING);

        // 6.11.13.1.e. Wait 60 seconds.
        pause("Step 6.11.13.1.e - Waiting %1$d seconds", 60);

        // 6.11.13.1.f. Turn engine off.
        ensureKeyStateIs(KEY_OFF);

        // 6.11.13.1.g. Wait manufacturer's recommended interval.
        waitForManufacturerInterval("Step 6.11.31.1.g", KEY_OFF);

        // 6.11.13.1.h. Turn Key On.
        ensureKeyStateIs(KeyState.KEY_ON_ENGINE_OFF);

        // 6.11.13.1.i. Proceed to Part 12.
    }

}
