/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
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
             new CommunicationsModule());
    }

    Part11Step13Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
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

    @Override
    protected void run() throws Throwable {
        // 6.11.13.1.a. Turn Engine Off.
        ensureKeyStateIs(KEY_OFF, "6.11.13.1.a");

        // 6.11.13.1.b. Wait manufacturer's recommended interval.
        waitMfgIntervalWithKeyOff("Step 6.11.13.1.b");

        // 6.11.13.1.c. Turn Key On.
        // 6.11.13.1.d. Start Engine Immediately.
        ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.11.13.1.d");

        // 6.11.13.1.e. Wait 60 seconds.
        pause("Step 6.11.13.1.e - Waiting %1$d seconds", 60);

        // 6.11.13.1.f. Turn engine off.
        ensureKeyStateIs(KEY_OFF, "6.11.13.1.f");

        // 6.11.13.1.g. Wait manufacturer's recommended interval.
        waitMfgIntervalWithKeyOff("Step 6.11.13.1.g");

        // 6.11.13.1.h. Turn Key On.
        ensureKeyStateIs(KEY_ON_ENGINE_OFF, "6.11.13.1.h");

        // 6.11.13.1.i. Proceed to Part 12.
    }

}
