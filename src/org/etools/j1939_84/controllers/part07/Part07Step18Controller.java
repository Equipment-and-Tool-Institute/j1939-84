/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

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
        // 6.7.18.1.b. Keep the ignition key in the off position.
        // 6.7.18.1.c. Implant Fault B according to engine manufacturer’s instruction. (See Section 5 for additional
        // discussion.)
        // 6.7.18.1.d. Turn ignition key to the ON position.
        // 6.7.18.1.e. Start the engine for cycle 8a.
        // 6.7.18.1.f. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
        // 6.7.18.1.g. Turn engine off.
        // 6.7.18.1.h. Wait engine manufacturer’s recommended interval for permanent fault recording.
        // 6.7.18.1.i. Start Engine.
        // 6.7.18.1.j. If Fault B is a single trip fault proceed with part 8 immediately.
        // 6.7.18.1.k. Wait for manufacturer’s recommended time for Fault B to be detected as failed.
        // 6.7.18.1.l. Turn engine off.
        // 6.7.18.1.m. Wait engine manufacturer’s recommended interval for permanent fault recording.
        // 6.7.18.1.n. Start Engine.
        // 6.7.18.1.o. Proceed with part 8 (cycle 8b).
    }

}
