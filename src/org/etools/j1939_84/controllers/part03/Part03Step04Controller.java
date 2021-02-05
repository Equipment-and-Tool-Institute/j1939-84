/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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
 * 6.3.4 DM29: Regulated DTC counts
 */
public class Part03Step04Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part03Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step04Controller(Executor executor,
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
        // 6.3.4.1.a Global DM29 (send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)).
        // 6.3.4.2.a Fail if any ECU reports > 0 for MIL on, previous MIL on, or permanent fault counts.
        // 6.3.4.2.b Fail if no ECU reports > 0 emission-related pending (SPN 4104).
        // 6.3.4.2.c Fail if any ECU reports a different number of emission-related pending DTCs than what that ECU reported in DM6 earlier in this part.
        // 6.3.4.2.d For OBD ECUs that support DM27, fail if any ECU reports a lower number of all pending DTCs (SPN 4105) than the number of emission-related pending DTCs.
        // 6.3.4.2.e For OBD ECUs that support DM27, fail if any ECU reports a lower number of all pending DTCs than what that ECU reported in DM27 earlier in this part.
        // 6.3.4.2.f For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs = 0xFF.
        // 6.3.4.2.g For non-OBD ECUs, fail if any ECU reports pending, MIL-on, previously MIL-on or permanent DTC count greater than 0.
        // 6.3.4.3.a Warn if any ECU reports > 1 for pending or all pending.
        // 6.3.4.3.b Warn if more than one ECU reports > 0 for pending or all pending.
    }

}
