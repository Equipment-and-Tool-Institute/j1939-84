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
 * 6.11.12 DM21: Diagnostic Readiness 2
 */
public class Part11Step12Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part11Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step12Controller(Executor executor,
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
        // 6.11.12.1.a. DS DM21 ([send Request (PGN 59904) for PGN 49408 (SPNs 3294, 3296))] to each OBD ECU.
        // 6.11.12.2.a. If Time SCC (SPN 3296) is supported, Fail if Time SCC differs by more than ±1 minute from the
        // expected value (calculated using the original DM26 response in this part from 6.11.3 plus the accumulated
        // time since then);.
        // [i.e., Fail if ABS[(Time SCCB - Time SCCA) (minutes) - Truncate((Time Since Engine StartB - Time Since Engine
        // StartA) / 60 (seconds/minute)) > 1 minute].
        // 6.11.12.2.b. Fail if distance SCC (SPN 3294) is > 0.
        // 6.11.12.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM21 message.
    }

}
