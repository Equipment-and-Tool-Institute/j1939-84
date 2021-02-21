/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
 * 6.9.15 DM29: Regulated DTC Counts
 */
public class Part09Step15Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part09Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step15Controller(Executor executor,
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
        // 6.9.15.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.9.15.2.a. Fail if any ECU reports > 0 for emission-related pending, MIL-on, or previous MIL on.
        // 6.9.15.2.b. Fail if no ECU reports > 0 for permanent DTC.
        // 6.9.15.2.c. Fail if any ECU reports a different number for permanent DTC than what that ECU reported in DM28.
        // 6.9.15.2.d. For OBD ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        // 6.9.15.2.e. For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs
        // = 0xFF.
        // 6.9.15.3.a. Warn if any ECU reports > 1 for permanent DTC.
        // 6.9.15.3.b. Warn if more than one ECU reports > 0 for permanent DTC.
    }

}
