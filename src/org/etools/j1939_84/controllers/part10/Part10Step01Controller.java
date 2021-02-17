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
 * 6.10.1 Verify Engine Running Operation for Cycle 10a
 */
public class Part10Step01Controller extends StepController {
    private static final int PART_NUMBER = 10;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 0;

    Part10Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part10Step01Controller(Executor executor,
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
        // 6.10.1.1.a. Gather broadcast data for engine speed (e.g., SPN 190).
        // 6.10.1.2.a. If engine speed is < 400 rpm, prompt/warn operator to confirm engine is running and then press enter.
    }

}