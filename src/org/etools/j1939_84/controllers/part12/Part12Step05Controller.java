/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

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
 * 6.12.5 DM29: Regulated DTC Counts
 */
public class Part12Step05Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part12Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part12Step05Controller(Executor executor,
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
        // 6.12.5.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        // 6.12.5.2.a. Fail if any ECU reports > 0 for emission-related pending, MIL-on, previous MIL on, or permanent DTC.
        // 6.12.5.2.b. For OBD ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        // 6.12.5.2.c. For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs = 0xFF.
        // 6.12.5.2.d. Fail if no OBD ECU provides a DM29 message.
    }

}