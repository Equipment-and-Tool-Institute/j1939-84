/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

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
 * 6.6.7 DM28: Permanent DTCs
 */
public class Part06Step07Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part06Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step07Controller(Executor executor,
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
        // 6.6.7.1.a DS DM28 [(send Request (PGN 59904) for PGN 64896 (SPNS 1213-1215, 3038, 1706))] to each OBD ECU.
        // 6.6.7.2.a. Fail if no ECU reports permanent DTC present.
        // 6.6.7.2.b. Fail if permanent DTC provided does not match DM12 active DTC.
        // 6.6.7.2.c. Fail if no ECU reports MIL on.
        // 6.6.7.2.d. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
    }

}
