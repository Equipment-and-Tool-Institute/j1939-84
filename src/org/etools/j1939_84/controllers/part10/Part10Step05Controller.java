/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
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
 * 6.10.5 Part 10 to Part 11 Transition
 */
public class Part10Step05Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part10Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part10Step05Controller(Executor executor,
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

            // 6.10.5.1.a. Wait for manufacturer’s recommended time for Fault B to be detected as passed.
            updateProgress("Step 6.10.5.1.a - Waiting for manufacturer’s recommended time for Fault B to be detected as passed");
            String message = "Wait for manufacturer’s recommended time for Fault B to be detected as passed";
            message += NL + NL + "Press OK to continue";
            displayInstructionAndWait(message, "Step 6.10.5.1.a", WARNING);

            // 6.10.5.1.b. Wait a total of at least 2 minutes to establish second cycle.
            pause("Step 6.10.5.1.b - Waiting %1$d seconds to establish second cycle", 120L);

            // 6.10.5.1.c. Turn engine off.
            ensureKeyStateIs(KEY_OFF, "6.10.5.1.c");

            // 6.10.5.1.d. Wait 1 minute.
            pause("Step 6.10.5.1.d - Waiting %1$d seconds", 60L);

            // 6.10.5.1.e. Start engine.
            // 6.10.5.1.f. Proceed with part 11, General Denominator Demonstration.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.10.5.1.e");
        }
    }

}
