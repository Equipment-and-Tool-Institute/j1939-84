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
 * 6.3.11 DM28: permanent DTCs
 */
public class Part03Step11Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part03Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step11Controller(Executor executor,
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
        // 6.3.11.1.a. Global DM28 (send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 3038, 1706)).
        // 6.3.11.2.a. Fail if any ECU reports a permanent DTC.
        // 6.3.11.2.b. Fail if any OBD ECU does not report MIL off.
        // 6.3.11.2.c. Fail if any non-OBD ECU does not report MIL off or not supported.
        // 6.3.11.2.d. Fail if no OBD ECU provides DM28

        // 6.3.11.3.a. DS DM28 to each OBD ECU.
        // 6.3.11.4.a. Fail if any difference compared to data received from global request.
        // 6.3.11.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
    }

}
