/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

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
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *         6.2.18 Part 2 to Part 3 transition
 *         <p>
 *         This step is similar to Part 01 Step 27 & Part 02 Step 01
 */
public class Part02Step18Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    Part02Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part02Step18Controller(Executor executor,
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
        // 6.2.18.1.a. Turn Engine Off and keep the ignition key in the off position.
        incrementProgress("Part 2, Step 18 Turn Engine Off and keep the ignition key in the off position");
        ensureKeyOffEngineOff();

        // 6.2.18.1.b. Implant Fault A according to engine manufacturer’s instruction (See section 5 for additional
        // discussion).
        incrementProgress("Waiting for implant of Fault A according to the engine manufacturer's instruction");
        waitForFaultA();

        // 6.2.18.1.c. Turn ignition key to the ON position.
        // 6.2.18.1.d. Observe MIL and Wait to Start Lamps in Instrument Cluster
        // 6.2.18.1.e. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        incrementProgress("Part 2, Step 18 Turn ignition key to the ON position after MIL & WSL have cleared");
        waitForEngineStart();

    }

    private void waitForFaultA() {
        if (!isDevEnv()) {
            String message = "Implant Fault A according to engine manufacturer’s instruction" + NL;
            message += "Press OK to continue testing" + NL;
            waitForFault("Part 6.2.18.1.b", message);
        }
    }

    private void waitForEngineStart() {
        if (!isDevEnv()) {
            String message = "Turn ignition key to the ON position" + NL;
            message += "Please observe the MIL and Wait to Start Lamp (if equipped) in the Instrument Cluster" + NL;
            message += "Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished" + NL;
            message += "Press OK to continue testing" + NL;
            displayInstructionAndWait(message, "Part 6.2.18.1.c-e", WARNING);
        }
    }

}
