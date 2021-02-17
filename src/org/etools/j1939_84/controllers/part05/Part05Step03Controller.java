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
 * 6.5.3 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part05Step03Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part05Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part05Step03Controller(Executor executor,
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
        // 6.5.3.1.a Receive DM1 broadcast data ([PGN 65226 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.5.3.2.a For every [OBD] ECU that responded to the DM12 request in step 6.5.2.1, Fail if the DM1 response for the same ECU does not include the SPN(s) and associated FMI as given in the DM12 response.
        // 6.5.3.2.b For every ([OBD]) ECU that responded to the DM12 request in step 6.5.2.1, Fail if the DM1 response for the same ECU has a different MIL status than given in its DM12 response.
    }

}