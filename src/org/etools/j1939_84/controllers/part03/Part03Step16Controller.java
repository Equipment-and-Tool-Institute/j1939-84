/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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
 * 6.3.16 Part 3 to Part 4 Transition - Complete Fault A First Trip
 */
public class Part03Step16Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part03Step16Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step16Controller(Executor executor,
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

            // 6.3.16.1.a. Turn the engine off.
            ensureKeyStateIs(KEY_OFF, "6.3.16.1.a");

            // 6.3.16.1.b. Confirm Fault A is still implanted according to the manufacturer’s instruction.
            updateProgress("Step 6.3.16.1.b - Confirming Fault A is still implanted according to the manufacturer's instruction");
            String message = "Confirm Fault A is still implanted according to the manufacturer's instruction" + NL + NL;
            message += "Press OK to continue";
            displayInstructionAndWait(message, "Step 6.3.16.1.b", WARNING);

            // 6.3.16.1.c. Wait manufacturer’s recommended interval with the key in the off position.
            waitMfgIntervalWithKeyOff("Step 6.3.16.1.c");

            // 6.3.16.1.d. Turn ignition key to the ON position.
            // 6.3.16.1.e. Observe MIL and Wait to Start Lamp in Instrument Cluster
            // 6.3.16.1.f. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.3.16.1.f");

            // 6.3.16.1.g. Wait as indicated by the engine manufacturer’s recommendations for Fault A.
            updateProgress("Step 6.3.16.1.g - Waiting as indicated by the engine manufacturer’s recommendations for Fault A");
            String message1 = "Wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL + NL;
            message1 += "Press OK to continue";
            displayInstructionAndWait(message1, "Step 6.3.16.1.g", WARNING);
        }
    }

}
