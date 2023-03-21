/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static java.lang.String.format;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.J1939_84.isTesting;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.KeyState.KEY_OFF;
import static org.etools.j1939_84.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.FaultModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.DM5Heartbeat;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.18 Complete Part 7 Operating Cycle and Implant Fault B
 */
public class Part07Step18Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    private final FaultModule faultModule;

    Part07Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new FaultModule());
    }

    Part07Step18Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           FaultModule faultModule) {
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

        this.faultModule = faultModule;
    }

    @Override
    protected void run() throws Throwable {
        try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {
            // 6.7.18.1.a. Turn the engine off.
            // 6.2.18.1.a. Turn Engine Off and keep the ignition key in the off position.
            ensureKeyStateIs(KEY_OFF, "6.7.18.1.a");
            faultModule.setJ1939(getJ1939());

            // 6.7.18.1.b. Keep the ignition key in the off position.
            // 6.7.18.1.c. Implant Fault B according to engine manufacturer’s instruction.
            updateProgress("Step 6.7.18.1.c - Waiting for Fault B to be implanted");
            String message = "Implant Fault B according to engine manufacturer’s instruction";
            message += NL + NL + "Press OK to continue";
            displayInstructionAndWait(message, "Step 6.7.18.1.b", WARNING);
            if (isTesting()) {
                faultModule.implantFaultB(getListener());
            }

            // 6.7.18.1.d. Turn ignition key to the ON position.
            // 6.7.18.1.e. Start the engine for cycle 8a.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.7.18.1.e");

            // 6.7.18.1.f. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
            waitForFault("Step 6.7.18.1.f");

            // 6.7.18.1.g. Turn engine off.
            ensureKeyStateIs(KEY_OFF, "6.7.18.1.g");

            // 6.7.18.1.h. Wait engine manufacturer’s recommended interval for permanent fault recording.
            waitMfgIntervalWithKeyOff("Step 6.7.18.1.h");

            // 6.7.18.1.i. Start Engine.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.7.18.1.i");

            // 6.7.18.1.j. If Fault B is a single trip fault proceed with part 8 immediately.
            if (getTripsRequired() != 1) {
                // We already handle the first fault B, so only iterate over rest
                for (int i = 2; i <= getTripsRequired(); i++) {
                    updateProgress(format("Step 6.7.18.1.j - Running fault B trip #%d of %d total fault trips",
                                          i,
                                          getTripsRequired()));
                    // 6.7.18.1.k. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
                    waitForFault("Step 6.7.18.1.k");

                    // 6.7.18.1.l. Turn engine off.
                    ensureKeyStateIs(KEY_OFF, "6.7.18.1.l");

                    // 6.7.18.1.m. Wait engine manufacturer’s recommended interval for permanent fault recording.
                    waitMfgIntervalWithKeyOff("Step 6.7.18.1.m");

                    // 6.7.18.1.n. Start Engine.
                    // 6.7.18.1.o. Proceed with part 8 (cycle 8b).
                    ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.7.18.1.n");
                }
            } else {
                // 6.7.18.1.j. If Fault B is a single trip fault proceed with part 8 immediately.
                updateProgress("Step 6.7.18.1.j - Fault B is a single trip fault; proceeding with part 8 immediately");
            }
        }
    }

    private int getTripsRequired() {
        return getDataRepository().getVehicleInformation().getNumberOfTripsForFaultBImplant();
    }

    private void waitForFault(String stepId) throws InterruptedException {
        updateProgress(stepId + " - Waiting for manufacturer’s recommended time for Fault B to be detected as failed");
        String message = "Wait for manufacturer’s recommended time for Fault B to be detected as failed";
        message += NL + NL + "Press OK to continue";
        displayInstructionAndWait(message, stepId, WARNING);

    }

}
