/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
 * 6.8.8 DM29: Regulated DTC Counts
 */
public class Part08Step08Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part08Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step08Controller(Executor executor,
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
        // 6.8.8.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.8.8.2.a. Fail if any ECU reports > 0 for emission-related pending.
        // 6.8.8.2.b. Fail if no ECU reports > 0 for MIL on.
        // 6.8.8.2.c. Fail if any ECU reports a different number for MIL on than what that ECU reported in DM12 earlier in this part.
        // 6.8.8.2.d. Fail if no ECU reports > 0 for previous MIL on.
        // 6.8.8.2.e. Fail if any ECU reports a different number for previous MIL on than what that ECU reported in DM23 earlier in this part.
        // 6.8.8.2.f. Fail if no ECU reports > 0 for permanent.
        // 6.8.8.2.g. Fail if any ECU reports a different number for permanent than what that ECU reported in DM28 earlier in this part.
        // 6.8.8.2.h. For ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPNSP 4105).
        // 6.8.8.2.i. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs = 0xFF.
        // 6.8.8.3.a. Warn if any ECU reports > 1 for MIL on.
        // 6.8.8.3.b. Warn if more than one ECU reports > 0 for MIL on.
        // 6.8.8.3.c. Warn if any ECU reports > 1 for previous MIL on.
        // 6.8.8.3.d. Warn if more than one ECU reports > 0 for previous MIL on.
        // 6.8.8.3.e. Warn if any ECU report > 1 for permanent.
        // 6.8.8.3.f. Warn if more than one ECU reports > 0 for permanent.
    }

}