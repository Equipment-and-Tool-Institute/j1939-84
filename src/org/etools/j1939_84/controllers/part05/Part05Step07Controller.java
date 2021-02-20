/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isDevEnv;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Part05Step07Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part05Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part05Step07Controller(Executor executor,
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
        // 6.5.7.1.a Turn the engine off to complete the first1st cycle.
        incrementProgress("Step 5.7.1.a - Turn Engine Off and keep the ignition key in the off position");
        ensureKeyOffEngineOff();

        // 6.5.7.1.b Wait manufacturer’s recommended interval with the key in the off position.
        incrementProgress("Step 5.7.1.b - Waiting manufacturer’s recommended interval with the key in the off position");
        waitForManufacturerInterval("Part 6.5.7.1.b");

        // 6.5.7.1.c Start Engine for second cycle.
        incrementProgress("6.5.7.1.c Turn Engine on and keep the ignition key in the on position");
        ensureKeyOnEngineOn();

        // 6.5.7.1.d Wait for manufacturer’s recommended time for Fault A to be detected as passed.
        incrementProgress("Waiting manufacturer’s recommended time for Fault A to be detected as passed");
        waitForManufacturerInterval("Part 6.5.7.1.d");

        // 6.5.7.1.e Turn the engine off to complete the second cycle.
        // 6.5.7.1.f Wait manufacturer’s recommended interval with the key in the off position.
        // 6.5.7.1.g Start the engine for part 6.
        // 6.5.7.1.h Wait for manufacturer’s recommended time for Fault A to be detected as passed
        waitForEngineStart();
    }
    private void waitForManufacturerInterval(String boxTitle) {
        if (!isDevEnv()) {
            String message = "Please wait for the manufacturer's recommended interval with the key in off position" + NL;
            message += "Press OK to continue the testing" + NL;
            displayInstructionAndWait(message, boxTitle, WARNING);
        }
    }
    private void waitForEngineStart() {
        if (!isDevEnv()) {
            String message = "Turn the engine off to complete the second cycle" + NL;
            message += "Wait manufacturer’s recommended interval with the key in the off position" + NL;
            message += "Start the engine for part 6" + NL;
            message += "Wait for manufacturer’s recommended time for Fault A to be detected as passed" + NL;
            message += "Press OK when ready to continue testing" + NL;
            displayInstructionAndWait(message, "Part 6.5.7.1.e-g", WARNING);
        }
    }
}