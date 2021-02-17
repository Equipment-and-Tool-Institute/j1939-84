/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

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
 * 6.6.8 DM29: Regulated DTC Counts
 */
public class Part06Step08Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part06Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step08Controller(Executor executor,
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
        // 6.6.8.1.a DS DM29 ([send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]) to each OBD ECU.
        // 6.6.8.2.a. Fail if any ECU reports > 0 for emission-related pending or previous MIL on.
        // 6.6.8.2.b. Fail if no ECU reports > 0 for MIL on.
        // 6.6.8.2.c. Fail if any ECU reports a different number for MIL on than what that ECU reported in DM12.
        // 6.6.8.2.d. Fail if no ECU reports > 0 for permanent.
        // 6.6.8.2.e. Fail if any ECU reports a different number for permanent than what that ECU reported in DM28.
        // 6.6.8.2.f. For ECUs that support DM27, fail if any ECU reports an all pending DTC (DM27) (SPNSP 4105) count that is less than its pending DTC (DM6) count.
        // 6.6.8.2.g. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs = 0xFF.
        // 6.6.8.2.h. Fail if NACK not received from OBD ECUs that did not provide a DM29 message.
        // 6.6.8.3.a. Warn if any ECU reports > 1 for MIL on.
        // 6.6.8.3.b. Warn if more than one ECU reports > 0 for MIL on.
        // 6.6.8.3.c. Warn if any ECU reports > 1 for permanent.
        // 6.6.8.3.d. Warn if more than one ECU reports > 0 for permanent
    }

}