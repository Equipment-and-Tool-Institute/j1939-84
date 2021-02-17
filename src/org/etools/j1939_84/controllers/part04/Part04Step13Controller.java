/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
 * 6.4.13 DM3: Diagnostic Data Clear/Reset for Previously Active DTCs
 */
public class Part04Step13Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part04Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step13Controller(Executor executor,
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
        // 6.4.13.1.a. DS DM3 [(send Request (PGN 59904) for PGN 65228)] to each OBD ECU
        // 6.4.13.1.b. Wait 5 seconds before checking for erased information.
        // 6.4.13.2.a. Fail if any OBD ECU does not NACK with control byte = 1 or 2 or 3, or if any ECU erases any diagnostic information. See Section A.5 for more information.
        // 6.4.13.2.b. Warn if any OBD ECU NACKs with control byte = 3.
        // 6.4.13.3.a. Global DM3
        // 6.4.13.3.b. Wait 5 seconds before checking for erased information.
        // 6.4.13.4.a. Fail if any OBD ECU erases OBD diagnostic information.
    }

}