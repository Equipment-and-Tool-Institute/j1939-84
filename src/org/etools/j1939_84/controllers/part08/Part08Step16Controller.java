/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;

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
 * 6.8.16 Complete Part 8b Operating Cycle and Repair Fault B for Part 9
 */
public class Part08Step16Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part08Step16Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step16Controller(Executor executor,
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
        // 6.8.16.1.a Turn the engine off.
        ensureKeyStateIs(KEY_OFF);

        // 6.8.16.1.b Wait manufacturer's recommended interval.
        waitForManufacturerInterval("Step 6.8.16.1.b", KEY_OFF);

        // 6.8.16.1.c With the key in the off position remove the implanted Fault B, according to the manufacturer’s
        // instructions for restoring the system to a fault- free operating condition.
        waitForFaultRemoval();

        // 6.8.16.1.d Turn the ignition key to the ON position.
        ensureKeyStateIs(KEY_ON_ENGINE_OFF);

        // 6.8.16.1.e Do not start engine.
        // 6.8.16.1.f Proceed with part 9.
        displayDoNotStartEngine();
    }

    private void waitForFaultRemoval() throws InterruptedException {
        incrementProgress("Step 6.8.16.1.c - With Key OFF, remove the implanted Fault B");
        String message = "Step 6.8.16.1.c - With the key in the off position remove the implanted Fault B, according to the manufacturer’s"
                + NL + "instructions for restoring the system to a fault- free operating condition." + NL;
        message += "Press OK to continue the testing.";
        displayInstructionAndWait(message, "Test 6.8.16", WARNING);
    }

    private void displayDoNotStartEngine() throws InterruptedException {
        incrementProgress("Step 6.8.16.1.e & f - Do Not Start Engine - proceeding with part 9");
        String message = "Step 6.8.16.1.e - Do Not Start Engine." + NL;
        message += "Step 6.8.16.1.f - Proceeding with part 9." + NL;
        message += "Press OK to continue the testing.";
        displayInstructionAndWait(message, "Test 6.8.16", WARNING);
    }
}
