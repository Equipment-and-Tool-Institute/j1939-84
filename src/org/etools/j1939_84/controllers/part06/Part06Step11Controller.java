/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isTesting;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
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
 * 6.6.11 Complete Fault A Three Trip Countdown Cycle 3
 */
public class Part06Step11Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part06Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step11Controller(Executor executor,
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

            // 6.6.11.1.a Turn engine off.
            ensureKeyStateIs(KEY_OFF, "6.6.11.1.a");

            // 6.6.11.1.b Wait engine manufacturer’s recommended interval.
            waitMfgIntervalWithKeyOff("Step 6.6.11.1.b");

            // 6.6.11.1.c Turn key to on position.
            ensureKeyStateIs(KEY_ON_ENGINE_OFF, "6.6.11.1.c");

            // 6.6.11.1.d If required by engine manufacturer, start the engine for start to start operating cycle
            // effects.
            String message = "If required by engine manufacturer, start the engine for start-to-start operating cycle effects";
            message += NL + NL + "Press OK to continue";
            displayInstructionAndWait(message, "Step 6.6.11.1.d", WARNING);
            if (isTesting()) {
                // The Simulated engine requires an engine start
                ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.6.11.1.d");
            }

            // 6.6.11.1.e Otherwise, Proceed with part 7.
            if (getEngineSpeedModule().getKeyState() != KEY_ON_ENGINE_RUNNING) {
                return;
            }

            // 6.6.11.1.f Turn engine off.
            ensureKeyStateIs(KEY_OFF, "6.6.11.1.f");

            // 6.6.11.1.g Wait engine manufacturer’s recommended interval.
            waitMfgIntervalWithKeyOff("Step 6.6.11.1.g");

            // 6.6.11.1.h Turn the key to the on position.
            // 6.6.11.1.i Proceed with part 7.
            ensureKeyStateIs(KEY_ON_ENGINE_OFF, "6.6.11.1.h");
        }
    }

}
