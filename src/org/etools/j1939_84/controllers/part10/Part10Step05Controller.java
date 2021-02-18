/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

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
 * 6.10.5 Part 10 to Part 11 Transition
 */
public class Part10Step05Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part10Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part10Step05Controller(Executor executor,
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
        // 6.10.5.1.a. Wait for manufacturer’s recommended time for Fault B to be detected as passed.
        // 6.10.5.1.b. Wait a total of at least 2 minutes to establish second cycle.
        // 6.10.5.1.c. Turn engine off.
        // 6.10.5.1.d. Wait 1 minute.
        // 6.10.5.1.e. Start engine.
        // 6.10.5.1.f. Proceed with part 11, General Denominator Demonstration.
    }

}