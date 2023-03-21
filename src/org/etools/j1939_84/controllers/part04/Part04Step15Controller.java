/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
 * 6.4.15 Part 4 to Part 5 Transition - Complete Fault A First Trip
 */
public class Part04Step15Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part04Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step15Controller(Executor executor,
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

            // 6.4.15.1.a Turn the engine off.
            ensureKeyStateIs(KEY_OFF, "6.4.15.1.a");

            // 6.4.15.1.b Wait engine manufacturer’s recommended interval.
            waitMfgIntervalWithKeyOff("Step 6.4.15.1.b");

            // 6.4.15.1.c With the key in the off position remove the implanted Fault A according to the
            // manufacturer’s instructions for restoring the system to a fault- free operating condition.
            updateProgress("Step 6.4.15.1.c - Waiting for implanted Fault A to be removed");
            String message = "With the key in the off position, remove the implanted Fault A according to the"
                    + NL + "manufacturer’s instructions for restoring the system to a fault-free operating condition"
                    + NL
                    + NL;
            message += "Press OK to continue";
            displayInstructionAndWait(message, "Step 6.4.15.1.c", WARNING);

            // 6.4.15.1.d Turn ignition key to the ON position.
            // 6.4.15.1.e Observe MIL in Instrument Cluster.
            // 6.4.15.1.f Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.4.15.1.f");

            // 6.4.15.1.g Wait for manufacturer’s recommended time for Fault A to be detected (as passed).
            updateProgress("Step 6.4.15.1.g - Waiting for manufacturer’s recommended time for Fault A to be detected (as passed)");
            String message1 = "Wait for manufacturer’s recommended time for Fault A to be detected (as passed)" + NL
                    + NL;
            message1 += "Press OK to continue";
            displayInstructionAndWait(message1, "Step 6.4.15.1.g", WARNING);
        }
    }

}
