/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_OFF;
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
 * 6.7.18 Complete Part 7 Operating Cycle and Implant Fault B
 */
public class Part07Step18Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    Part07Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step18Controller(Executor executor,
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
        // 6.7.18.1.a. Turn the engine off.
        // 6.2.18.1.a. Turn Engine Off and keep the ignition key in the off position.
        incrementProgress("Step 6.7.18.1.a - Turn Engine Off and keep the ignition key in the off position");
        ensureKeyStateIs(KEY_OFF);

        // 6.7.18.1.b. Keep the ignition key in the off position.
        // 6.7.18.1.c. Implant Fault B according to engine manufacturer’s instruction. (See Section 5 for additional
        // discussion.)
        incrementProgress("Step 6.7.18.1.b & c - Implant Fault B according to engine manufacturer’s instruction");
        implantFaultB();

        // 6.7.18.1.d. Turn ignition key to the ON position.
        incrementProgress("Step 6.7.18.1.d - Turn key to on with the with the engine off");
        ensureKeyStateIs(KEY_ON_ENGINE_OFF);

        // 6.7.18.1.e. Start the engine for cycle 8a.
        incrementProgress("Step 6.7.18.1.e - Turn Engine On and keep the ignition key in the on position");
        ensureKeyStateIs(KEY_ON_ENGINE_RUNNING);

        // 6.7.18.1.f. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
        waitForFault("Step 6.7.18.1.f");

        // 6.7.18.1.g. Turn engine off.
        incrementProgress("Step 6.7.18.1.g - Turn Engine Off and keep the ignition key in the off position");
        ensureKeyStateIs(KEY_OFF);

        // 6.7.18.1.h. Wait engine manufacturer’s recommended interval for permanent fault recording.
        waitForManufacturerInterval("Step 6.7.18.1.h", "off");

        // 6.7.18.1.i. Start Engine.
        incrementProgress("Step 6.7.18.1.i - Turn Engine on with the ignition key in the on position");
        ensureKeyStateIs(KEY_ON_ENGINE_RUNNING);

        // 6.7.18.1.j. If Fault B is a single trip fault proceed with part 8 immediately.
        int numberOfTripsForFaultBImplant = getDataRepository().getVehicleInformation()
                                                               .getNumberOfTripsForFaultBImplant();
        if (numberOfTripsForFaultBImplant != 1) {
            // We already handle the first fault B, so only iterate over rest
            for (int i = 2; i <= numberOfTripsForFaultBImplant; i++) {
                incrementProgress(format("Step 6.7.18.1.j - Running fault B trip #%d of %d total fault trips",
                                         i,
                                         numberOfTripsForFaultBImplant));
                // 6.7.18.1.k. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
                waitForFault("Step 6.7.18.1.k");

                // 6.7.18.1.l. Turn engine off.
                incrementProgress("Step 6.7.18.1.l - Turn Engine Off and keep the ignition key in the off position.");
                ensureKeyStateIs(KEY_OFF);

                // 6.7.18.1.m. Wait engine manufacturer’s recommended interval for permanent fault recording.
                waitForManufacturerInterval("Step 6.7.18.1.m", "off");

                // 6.7.18.1.n. Start Engine.
                // 6.7.18.1.o. Proceed with part 8 (cycle 8b).
                incrementProgress("Step 6.7.18.1.n & o - With the ignition key on and engine on proceeding to Part 8");
                ensureKeyStateIs(KEY_ON_ENGINE_RUNNING);
            }
        } else {
            // 6.7.18.1.j. If Fault B is a single trip fault proceed with part 8 immediately.
            incrementProgress("Step 6.7.18.1.j - Fault B is a single trip fault; proceeding with part 8 immediately");
        }
    }

    private void waitForFault(String stepId) throws InterruptedException {
        incrementProgress(format("%s - Waiting for manufacturer’s recommended time for Fault B to be detected as failed",
                                 stepId));
        String message = "Wait for manufacturer’s recommended time for Fault B to be detected as failed." + NL;
        message += "Press OK to continue the testing.";
        displayInstructionAndWait(message, stepId, WARNING);

    }

    private void implantFaultB() {
        String message = "Implant Fault B according to engine manufacturer’s instruction" + NL;
        message += "Press OK to continue the testing";
        displayInstructionAndWait(message, "Step 6.7.18.1.b", WARNING);
    }

}
