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
 * 6.7.10 DM29: Regulated DTC Counts
 */
public class Part07Step10Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part07Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step10Controller(Executor executor,
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
        // 6.7.10.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.7.10.2.a. Fail if any ECU reports > 0 for pending, all pending, MIL on, or permanent.
        // 6.7.10.2.b. Fail if no ECU reports > 0 previous MIL on.
        // 6.7.10.2.c. Fail if any ECU reports a different number of previous MIL on DTCs than what that ECU reported in
        // DM23 earlier in this part.
        // 6.7.10.3.a. Warn if any ECU reports > 1 for previous MIL on.
        // 6.7.10.3.b. Warn if more than one ECU reports > 0 for previous MIL on.
    }

}
