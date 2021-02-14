/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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
             new DiagnosticMessageModule());
    }

    Part03Step16Controller(Executor executor,
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
        // 6.3.16.1.a. Turn the engine off.
        incrementProgress("Part 3, Step 16 Turn Engine Off and keep the ignition key in the off position");
        ensureKeyOffEngineOff();

        // 6.3.16.1.b. Confirm Fault A is still implanted according to the manufacturer’s instruction.
        incrementProgress("Confirming Fault A is still implanted according to the manufacturer's instruction");
        confirmFault();

        // 6.3.16.1.c. Wait manufacturer’s recommended interval with the key in the off position.
        incrementProgress("Waiting for manufacturer's recommended interval with the key in off position");
        waitForManufacturerInterval();

        // 6.3.16.1.d. Turn ignition key to the ON position.
        // 6.3.16.1.e. Observe MIL and Wait to Start Lamp in Instrument Cluster
        // 6.3.16.1.f. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        // 6.3.16.1.g. Wait as indicated by the engine manufacturer’s recommendations for Fault A.
        waitForEngineStart();
    }

    private void confirmFault(){
        String message = "Please confirm Fault A is still implanted according to the manufacturer's instruction" + NL;
        message += "Press OK when ready to continue testing" + NL;
        String boxTitle = "Part 6.3.16.1.b";
        if(!isDevEnv()) {
            displayInstructionAndWait(message, boxTitle, WARNING);
        }
    }

    private void waitForManufacturerInterval() {
        if (!isDevEnv()) {
            String message = "Please wait for the manufacturer's recommended interval with the key in off position" + NL;
            message += "Press OK to continue the testing" + NL;
            displayInstructionAndWait(message, "Part 6.3.16.1.c", WARNING);
        }
    }

    private void waitForEngineStart() {
        if (!isDevEnv()) {
            String message = "Turn ignition key to the ON position" + NL;
            message += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
            message += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
            message += "Please wait as indicated by the engine manufacturer’s recommendations for Fault A" + NL;
            message += "Press OK when ready to continue testing" + NL;
            displayInstructionAndWait(message, "Part 6.3.16.1.d-g", WARNING);
        }
    }

}
