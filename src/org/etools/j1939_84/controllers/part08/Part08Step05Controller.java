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
 * 6.8.5 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part08Step05Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part08Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step05Controller(Executor executor,
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
        // 6.8.5.1.a Global DM2 ([send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)]).
        // 6.8.5.2.a (if supported) Fail if any OBD ECU does not include all DTCs from its DM23 response in its DM2
        // response.
        // 6.8.5.2.b (if supported) Fail if any OBD ECU reporting a different MIL status than DM12 response earlier in
        // this part.
    }

}
