/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
        // 6.4.15.1.b Wait engine manufacturer’s recommended interval.
        // 6.4.15.1.c With the key in the off position remove the implanted Fault A according to the manufacturer’s instructions for restoring the system to a fault- free operating condition.
        // 6.4.15.1.d Turn ignition key to the ON position.
        // 6.4.15.1.e Observe MIL in Instrument Cluster.
        // 6.4.15.1.f Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
        // 6.4.15.1.g Wait for manufacturer’s recommended time for Fault A to be detected (as passed).
    }

}