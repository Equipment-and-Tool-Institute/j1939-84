/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
 * 6.9.9 DM21: Diagnostic Readiness 2
 */
public class Part09Step09Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part09Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step09Controller(Executor executor,
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
        // 6.9.9.1.a. DS DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3295-3296)]) to each OBD ECU.
        // 6.9.9.2.a. Fail if any report time SCC (SPN 3296) > 0 (if supported).
        // 6.9.9.2.b. Fail if any report time with MIL on (SPN 3295) > 0 (if supported).
        // 6.9.9.2.c. Fail if no OBD ECU supports DM21.
        // 6.9.9.2.d. Fail if NACK not received from OBD ECUs that did not provide DM21 message.
    }

}
