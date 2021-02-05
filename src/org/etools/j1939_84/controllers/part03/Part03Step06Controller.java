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
 * 6.3.6 DM1: Active diagnostic trouble codes (DTCs)
 */
public class Part03Step06Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part03Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step06Controller(Executor executor,
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
        // 6.3.6.1.a Receive DM1 broadcast info (PGN 65226 (SPNs 1213-1215, 1706, and 3038)).
        // 6.3.6.2.a Fail if no OBD ECU supports DM1.
        // 6.3.6.2.b Fail if any OBD ECU reports an active DTC.
        // 6.3.6.2.c Fail if any OBD ECU does not report MIL off. See section A.8 for allowed values.
        // 6.3.6.2.d Fail if any non-OBD ECU does not report MIL off or not supported.
    }

}
