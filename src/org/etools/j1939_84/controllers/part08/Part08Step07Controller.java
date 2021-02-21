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
 * 6.8.7 DM28: Permanent DTCs
 */
public class Part08Step07Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part08Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step07Controller(Executor executor,
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
        // 6.8.7.1.a. Global DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.8.7.2.a. Fail if no OBD ECU reports a permanent DTC.
        // 6.8.7.2.b. Fail if permanent DTC does not match DM12 DTC from earlier in test 6.8.2.
        // 6.8.7.2.c. Fail if any ECU reporting different MIL status than DM12 response earlier in test 6.8.2.
        // 6.8.7.3.a. Warn if more than one ECU reports a permanent DTC.
        // 6.8.7.3.b. Warn if any ECU reports more than one permanent DTC.
        // 6.8.7.4.a. DS DM28 to each OBD ECU.
        // 6.8.7.5.a. Fail if any difference in data compared to global response.
        // 6.8.7.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query
    }

}
