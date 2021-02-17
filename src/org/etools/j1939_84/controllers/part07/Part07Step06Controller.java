/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

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
 * 6.7.6 DM5: Diagnostic Readiness 1
 */
public class Part07Step06Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part07Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step06Controller(Executor executor,
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
        // 6.7.6.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNSPs 1218-1219)]).
        // 6.7.6.2.a Fail if any OBD ECU reports > 0 for active DTCs.
        // 6.7.6.2.b Fail if no ECU reports > 0 for previously active DTCs.
        // 6.7.6.2.c Fail if any OBD ECU reports a different number of previously active DTCs than in DM2 response earlier in this Part.
    }

}