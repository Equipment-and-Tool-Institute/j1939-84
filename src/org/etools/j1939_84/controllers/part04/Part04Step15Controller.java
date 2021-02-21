/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
        incrementProgress("Part 4, Step 15 Turn Engine Off and keep the ignition key in the off position");
        ensureKeyOffEngineOff();

        // 6.4.15.1.b Wait engine manufacturer’s recommended interval.
        incrementProgress("Waiting for manufacturer's recommended interval with the key in off position");
        waitForManufacturerInterval();

        // 6.4.15.1.c With the key in the off position remove the implanted Fault A according to the
        // manufacturer’s instructions for restoring the system to a fault- free operating condition.
        incrementProgress("Part 4, Step 15 Remove implanted fault per manufacturer's instructions");
        confirmFaultRemoved();

        // 6.4.15.1.d Turn ignition key to the ON position.
        // 6.4.15.1.e Observe MIL in Instrument Cluster.
        // 6.4.15.1.f Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        // 6.4.15.1.g Wait for manufacturer’s recommended time for Fault A to be detected (as passed).
        waitForEngineStart();
    }

    private void waitForManufacturerInterval() {
        if (!isDevEnv()) {
            String message = "Please wait for the manufacturer's recommended interval with the key in off position"
                    + NL;
            message += "Press OK to continue the testing" + NL;
            displayInstructionAndWait(message, "Part 6.4.15.1.b", WARNING);
        }
    }

    private void confirmFaultRemoved() {
        String message = "With the key in the off position remove the implanted Fault A according to the" + NL;
        message += "manufacturer’s instructions for restoring the system to a fault- free operating condition" + NL;
        message += "Press OK when ready to continue testing" + NL;
        String boxTitle = "Part 6.4.15.1.c";
        if (!isDevEnv()) {
            displayInstructionAndWait(message, boxTitle, WARNING);
        }
    }

    private void waitForEngineStart() {
        if (!isDevEnv()) {
            String message = "Turn ignition key to the ON position" + NL;
            message += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
            message += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
            message += "Please wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL;
            message += "Press OK when ready to continue testing" + NL;
            displayInstructionAndWait(message, "Part 6.4.15.1.d-g", WARNING);
        }
    }

}
