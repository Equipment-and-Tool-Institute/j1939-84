/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
 * 6.4.3 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part04Step03Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part04Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step03Controller(Executor executor,
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
        // 6.4.3.1.a Receive broadcast data ([PGN 65226 (SPNs 1213-1215, 3038, 1706)]).
        // 6.4.3.2.a Fail if no ECU reports an active DTC and MIL on.
        // 6.4.3.2.b Fail if any OBD ECU report does not include its DM12 DTCs in the list of active DTCs.
        // 6.4.3.2.c Fail if any OBD ECU reports fewer active DTCs in its DM1 response than its DM12 response.
        // 6.4.3.2.d Warn if any non-OBD ECU reports an Active DTC.
        // 6.4.3.2.e Warn if more than 1 active DTC is reported by the vehicle.
    }

}