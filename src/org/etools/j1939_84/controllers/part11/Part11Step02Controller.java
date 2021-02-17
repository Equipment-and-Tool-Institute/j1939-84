/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

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
 * 6.11.2 DM26: Diagnostic Readiness 3
 */
public class Part11Step02Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part11Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step02Controller(Executor executor,
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
        // 6.11.2.1.a. Global DM26 ([send Request (PGN 59904) for PGN 64952 (SPN 3301)]).
        // 6.11.2.1.a.i. Record time since engine start.
        // 6.11.2.1.a.ii. Separately start tracking time in software to compare to reported values later in part 11.
        // 6.11.2.2.a. If more than one ECU responds, fail if times (since engine start) differ by > 2 seconds.
        // 6.11.2.2.b. Fail if no OBD ECU provides a DM26 message
    }

}