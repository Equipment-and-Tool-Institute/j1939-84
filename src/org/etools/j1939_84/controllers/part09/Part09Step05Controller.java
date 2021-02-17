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
 * 6.9.5 DM21: Diagnostic Readiness 2
 */
public class Part09Step05Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part09Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step05Controller(Executor executor,
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
        // 6.9.5.1.a. DS DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3294, 3296)]) to each OBD ECU.
        // 6.9.5.2.a. Fail if any ECU reports distance SCC (SPN 3294) > 0.
        // 6.9.5.2.b. Fail if any ECU reports time SCC (SPN 3296) is < 1 minute (if SPN 3296 is supported).
        // 6.9.5.2.c. Fail if no OBD ECU provides a DM21 message
        // 6.9.5.2.d. Fail if NACK not received from OBD ECUs that did not support a DM21 message.
        // 6.9.5.3.a. Warn if more than one ECU reports time SCC > 0 and times reported differ by > 1 minute
    }

}