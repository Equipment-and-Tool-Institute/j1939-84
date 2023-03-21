/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

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
 * 6.2.18 Part 2 to Part 3 transition
 */
public class Part02Step18Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;
    private final FaultModule faultModule;

    Part02Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new FaultModule());
    }

    Part02Step18Controller(Executor executor,
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

            // 6.2.18.1.a. Turn Engine Off and keep the ignition key in the off position.
            ensureKeyStateIs(KEY_OFF, "6.2.18.1.a");
            faultModule.setJ1939(getJ1939());

            // 6.2.18.1.b. Implant Fault A according to engine manufacturer’s instruction (See section 5 for additional
            // discussion).
            waitForFaultA();
            if (isTesting()) {
                faultModule.implantFaultA(getListener());
            }

            // 6.2.18.1.c. Turn ignition key to the ON position.
            // 6.2.18.1.d. Observe MIL and Wait to Start Lamps in Instrument Cluster
            // 6.2.18.1.e. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
            ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.2.18.1.c");
        }
    }

    private void waitForFaultA() throws InterruptedException {
        updateProgress("Step 6.2.18.1.b - Waiting for implant of Fault A according to the engine manufacturer's instruction");

        String message = "Implant Fault A according to engine manufacturer’s instruction" + NL + NL;
        message += "Press OK to continue";
        displayInstructionAndWait(message, "Step 6.2.18.1.b", WARNING);
    }

}
