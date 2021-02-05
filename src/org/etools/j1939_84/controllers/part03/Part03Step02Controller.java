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
 * 6.3.2 DM6: Emission related pending DTCs
 */
public class Part03Step02Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part03Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step02Controller(Executor executor,
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
        // 6.3.2.1.a. Global DM6 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)).
        // 6.3.2.1.a.i. Repeat request for DM6 no more frequently than once per s until one or more ECUs reports a pending DTC.
        // 6.3.2.1.a.ii. Time-out after every 5 minutes and ask user ‘yes/no’ to continue if still no pending DTC; and fail if user says 'no' and no ECU reports a pending DTC.
        // 6.3.2.2.a. Fail if no OBD ECU supports DM6.
        // 6.3.2.3.a Warn if any ECU reports > 1 pending DTC
        // 6.3.2.3.b Warn if more than one ECU reports a pending DTC.

        // 6.3.2.4 DS DM6 to each OBD ECU.
        // 6.3.2.5.a Fail if any difference compared to data received with global request.
        // 6.3.2.5.b Fail if all [OBD] ECUs do not report MIL off. See section A.8 for allowed values.
        // 6.3.2.5.c Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}
