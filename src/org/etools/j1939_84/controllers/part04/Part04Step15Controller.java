/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isDevEnv;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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
             new DiagnosticMessageModule());
    }

    Part04Step15Controller(Executor executor,
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
        // 6.4.15.1.a Turn the engine off.
        incrementProgress("Step 6.4.15.1.a - Turn Engine Off and keep the ignition key in the off position");
        ensureKeyStateIs(KEY_OFF);

        // 6.4.15.1.b Wait engine manufacturer’s recommended interval.
        waitForManufacturerInterval("Step 6.4.15.1.b", KEY_OFF);

        // 6.4.15.1.c With the key in the off position remove the implanted Fault A according to the
        // manufacturer’s instructions for restoring the system to a fault- free operating condition.
        incrementProgress("Step 6.4.15.1.c - Remove implanted fault per manufacturer's instructions");
        confirmFaultRemoved();

        // 6.4.15.1.d Turn ignition key to the ON position.
        // 6.4.15.1.e Observe MIL in Instrument Cluster.
        // 6.4.15.1.f Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        // 6.4.15.1.g Wait for manufacturer’s recommended time for Fault A to be detected (as passed).
        waitForEngineStart();
    }

    private void confirmFaultRemoved() {
        String message = "Step 6.4.15.1.c - With Key OFF, remove the implanted Fault A according to the"
                + NL;
        message += "manufacturer’s instructions for restoring the system to a fault- free operating condition" + NL;
        message += "Press OK to continue testing.";
        displayInstructionAndWait(message, "Test 6.4.15", WARNING);
    }

    private void waitForEngineStart() {
        if (!isDevEnv()) {
            String message = "Step 6.4.15.1.d - Turn ignition key to the ON position." + NL;
            message += "Step 6.4.15.1.e - Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster."
                    + NL;
            message += "Step 6.4.15.1.f - Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished."
                    + NL;
            message += "Step 6.4.15.1.g - Please wait as indicated by the engine manufacturer’s recommendations for Fault A."
                    + NL;
            message += "Press OK to continue testing.";
            displayInstructionAndWait(message, "Test 6.4.15", WARNING);
        }
    }

}
