/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

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
 * 6.12.7 DM21: Diagnostic Readiness 2
 */
public class Part12Step07Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part12Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part12Step07Controller(Executor executor,
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
        // 6.12.7.1.a. Global DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3294, 3296)]).
        // 6.12.7.2.a. Fail if any ECU reports distance SCC (SPN 3294) > 0.
        // 6.12.7.2.b. Fail if any ECU reports < 10 minutes for time SCC (SPN 3296), if supported.
        // 6.12.7.2.c. If more than one ECU responds, fail if values reported for time SCC differ by > 1 minute.
        // 6.12.7.2.d. Fail if no OBD ECU provides a DM21 message.
    }

}