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
 * 6.9.24 DM33: Emission Increasing Auxiliary Emission Control Device Active Time
 */
public class Part09Step24Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 24;
    private static final int TOTAL_STEPS = 0;

    Part09Step24Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step24Controller(Executor executor,
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
        // 6.9.24.1.a. DS DM33 [(send Request (PGN 59904) for PGN 41216 (SPNs 4124-4126))] to each OBD ECU.
        // 6.9.24.2.a. Fail if any ECU reports a different number EI-AECD than was reported in part 2.
        // [Engines using SI technology need not respond until the 2024 engine model year].
        // 6.9.24.2.b. Compare to list of ECU address + EI-AECD number + actual time (for Timer 1 and/or Timer 2)
        // for any with non- zero timer values created earlier in step 6.9.6.1 and fail if any timer value is less
        // than the value it was earlier in this part.
        // 6.9.24.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM33 message.
    }

}
