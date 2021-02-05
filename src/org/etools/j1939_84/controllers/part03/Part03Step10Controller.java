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
 * 6.3.10 DM23: Emission related previously active DTCs
 */
public class Part03Step10Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part03Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step10Controller(Executor executor,
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
        // 6.3.10.1.a. Global DM23 (send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 3038, 1706)).
        // 6.3.10.2.a. Fail if any ECU reports a previously active DTC.
        // 6.3.10.2.b. Fail if any OBD ECU does not report MIL off.
        // 6.3.10.2.c. Fail if any non- OBD ECU does not report MIL off or not supported.
        // 6.3.10.2.d. Fail if no OBD ECU provides DM23

        // 6.3.10.3.a. DS DM23 to each OBD ECU.
        // 6.3.10.4.a. Fail if any difference compared to data received from global request.
        // 6.3.10.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
    }

}
