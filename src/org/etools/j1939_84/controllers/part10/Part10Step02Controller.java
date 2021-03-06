/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

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
 * 6.10.2 Complete Cycle 10a
 */
public class Part10Step02Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part10Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part10Step02Controller(Executor executor,
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
        // 6.10.2.1.a. Wait for manufacturer’s recommended time for Fault B to be detected as passed.
        waitForFault("Step 6.10.2.1.a");
        // 6.10.2.1.b. Wait a total of at least 2 minutes to establish cycle.
        pause("Step 6.10.2.1.b Waiting %1$d seconds", 120L);
        // 6.10.2.1.c. Turn engine off.
        ensureKeyStateIs(KEY_OFF);
        // 6.10.2.1.d. Wait 1 minute.
        pause("Step 6.10.2.1.d Waiting %1$d seconds", 60L);
        // 6.10.2.1.e. Start engine.
        ensureKeyStateIs(KEY_ON_ENGINE_RUNNING);
    }

    private void waitForFault(String stepId) throws InterruptedException {
        incrementProgress(format("%s - Waiting for manufacturer’s recommended time for Fault B to be detected as passed",
                                 stepId));
        String message = "Wait for manufacturer’s recommended time for Fault B to be detected as passed." + NL;
        message += "Press OK to continue the testing.";
        displayInstructionAndWait(message, stepId, WARNING);
    }

}
