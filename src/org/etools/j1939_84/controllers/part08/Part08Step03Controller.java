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
 * 6.8.3 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part08Step03Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part08Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step03Controller(Executor executor,
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
        // 6.8.3.1.a Receive broadcast data [(PGN 65226 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.8.3.2.a Fail if no ECU reporting MIL on.
        // 6.8.3.2.b Fail if any OBD ECU does not include all DTCs from its DM12 response in its DM1 response.
        // 6.8.3.2.c Fail if any OBD ECU reporting different MIL status than DM12 response earlier in this part.
    }

}