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
 * 6.7.16 DM3: Diagnostic Data Clear/Reset for Previously Active DTCs
 */
public class Part07Step16Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part07Step16Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step16Controller(Executor executor,
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
        // 6.7.16.1.a. Global DM3 [(send Request (PGN 59904) for PGN 65228]).
        // 6.7.16.1.b. Wait 5 seconds before checking for erased information.
        // 6.7.16.2.a. Fail if any OBD ECU erases any diagnostic information as discussed in Section A.5.
        // 6.7.16.3.a. DS DM3 to each OBD ECU.
        // 6.7.16.3.b. Wait 5 seconds before checking for erased information.
        // 6.7.16.4.a. Fail if any ECU does not NACK, or if any OBD ECU erases any diagnostic information. See Section A.5 for more information.
    }

}