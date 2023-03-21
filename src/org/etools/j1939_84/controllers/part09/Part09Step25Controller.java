/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.DM5Heartbeat;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.9.25 Part 9 to Part 10 Transition
 */
public class Part09Step25Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 25;
    private static final int TOTAL_STEPS = 0;

    Part09Step25Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step25Controller(Executor executor,
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
        try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {

            // 6.9.25.1.a. Turn Key Off.
            ensureKeyStateIs(KEY_OFF, "6.9.25.1.a");

            // 6.9.25.1.b. Wait manufacturer’s recommended interval.
            waitMfgIntervalWithKeyOff("Step 6.9.25.1.b");

            // 6.9.25.1.c. Turn ignition key to on position.
            // 6.9.25.1.d. Start engine.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.9.25.1.d");

            // 6.9.25.1.e. Proceed with part 10
        }
    }
}
