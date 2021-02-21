/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

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
 * 6.11.4 DM29: Regulated DTC Counts
 */
public class Part11Step04Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part11Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step04Controller(Executor executor,
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
        // 6.11.4.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.11.4.2.a. Fail if any ECU reports > 0 for emission-related pending, MIL-on, or previous MIL on.
        // 6.11.4.2.b. Fail if no ECU reports > 0 for permanent DTC.
        // 6.11.4.2.c. For ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        // 6.11.4.2.d. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs =
        // 0xFF.
        // 6.11.4.3.a. Warn if any ECU reports > 1 for permanent DTC.
        // 6.11.4.3.b. Warn if more than one ECU reports > 0 for permanent DTC.
    }

}
