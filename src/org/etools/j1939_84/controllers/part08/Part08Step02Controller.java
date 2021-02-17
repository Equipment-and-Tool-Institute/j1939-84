/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
 * 6.8.2 DM12: Emissions Related Active DTCs
 */
public class Part08Step02Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part08Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step02Controller(Executor executor,
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
        // 6.8.2.1.a. Global DM12 [(send Request (PGNPG 59904) for PGNPG 65236 (SPNSPs 1213-1215, 1706, and 3038)]).
        // 6.8.2.1.b. Repeat request until one or more ECUs reports an active DTC.
        // 6.8.2.1.b.i. Time-out after 5 minutes and ask user yes/no to continue if there is still no active DTC.
        // 6.8.2.1.b.ii. Fail if user says “no” and no ECU reports an active DTC.
        // 6.8.2.2.a. Warn if any ECU reports > 1 active DTC.
        // 6.8.2.2.b. Warn if more than one ECU reports an active DTC.
        // 6.8.2.3.a. DS DM12 to each OBD ECU.
        // 6.8.2.4.a. Fail if any difference compared to data received with global request.
        // 6.8.2.4.b. Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        // 6.8.2.4.c. Fail if NACK not received from OBD ECUs that did not respond to global query.
        // 6.8.2.5.a. Warn if ECU reporting active DTC does not report MIL on.
        // 6.8.2.5.b. Warn if an ECU not reporting an active DTC reports MIL on.
    }

}