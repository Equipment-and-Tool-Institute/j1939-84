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
 * 6.12.3 DM5: Diagnostic Readiness 1
 */
public class Part12Step03Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part12Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part12Step03Controller(Executor executor,
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
        // 6.12.3.1.a. DS DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1221-1223)]) to each OBD ECU.
        // 6.12.3.1.b. Display monitor readiness composite value in log.
        // 6.12.3.2.a. Fail if any supported monitor (except CCM) that was “0 = complete” in part 11 is now reporting “1 = not complete.”.
        // 6.12.3.3.a. Warn if DM5 reports fewer completed monitors than DM26 in step 6.12.2.1.
    }

}