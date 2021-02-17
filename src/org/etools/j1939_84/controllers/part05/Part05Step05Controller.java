/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

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
 * 6.5.5 DM29: Regulated DTC Counts
 */
public class Part05Step05Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part05Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part05Step05Controller(Executor executor,
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
        // 6.5.5.1.a Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.5.5.2.a Fail if any ECU reports > 0 for emission-related pending or previous MIL on.
        // 6.5.5.2.b Fail if no ECU reports > 0 MIL on DTCs where the same ECU provides one or more permanent DTCs.
        // 6.5.5.2.c Fail if any ECU reports a different number of MIL on DTCs than what that ECU reported in DM12 earlier in this part.
        // 6.5.5.2.d Fail if any ECU reports a different number of permanent DTCs than what that ECU reported in DM28 earlier in this part.
        // 6.5.5.2.e For OBD ECUs that support DM27,
        // 6.5.5.2.e.i. Fail if any ECU reports > 0; for all pending DTCs (SPNSP 4105).
        // 6.5.5.2.e.ii. Fail if any ECU reports 0xFF, for all pending DTCs.
        // 6.5.5.2.f. For ECUs that do not support DM27,
        // 6.5.5.2.f.i Fail if any ECU does not report number of all pending DTCs (SPNSP 4105) = 0xFF.
        // 6.5.5.3.a Warn if any ECU reports > 1 for MIL on or permanent.
        // 6.5.5.3.b Warn if more than one ECU reports > 0 for MIL on or permanent.
    }

}