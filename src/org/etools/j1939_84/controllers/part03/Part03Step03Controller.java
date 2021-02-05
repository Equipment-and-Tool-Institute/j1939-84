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
 * 6.3.3 DM27: All pending DTCs
 */
public class Part03Step03Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part03Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step03Controller(Executor executor,
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
        // 6.3.3.1.a. Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)).
        // 6.3.3.2.a. Fail if (if supported) no ECU reports the same DTC observed in step 6.3 in a positive DM27 response.
        // 6.3.3.3.a. Warn if (if supported) any ECU additional DTCs are provided than the DTC observed in step 6.3 in a positive DM27 response.

        // 6.3.3.4.a. DS DM27 to each OBD ECU.
        // 6.3.3.5.a Fail if (if supported) any difference compared to data received with global request.
        // 6.3.3.5.a Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}
