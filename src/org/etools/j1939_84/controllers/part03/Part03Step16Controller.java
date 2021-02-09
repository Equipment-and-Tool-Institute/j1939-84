/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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
        // 6.3.16.1.b. Confirm Fault A is still implanted according to the manufacturer’s instruction.
        // 6.3.16.1.c. Wait manufacturer’s recommended interval with the key in the off position.
        // 6.3.16.1.d. Turn ignition key to the ON position.
        // 6.3.16.1.e. Observe MIL and Wait to Start Lamp in Instrument Cluster
        // 6.3.16.1.f. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        // 6.3.16.1.g. Wait as indicated by the engine manufacturer’s recommendations for Fault A.
    }

}