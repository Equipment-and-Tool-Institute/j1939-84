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
 * 6.9.19 DM12: Emissions Related Active DTCs
 */
public class Part09Step19Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 19;
    private static final int TOTAL_STEPS = 0;

    Part09Step19Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step19Controller(Executor executor,
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
        // 6.9.19.1.a. DS DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]) to each OBD ECU.
        // 6.9.19.2.a. Fail if any ECU reports an active MIL DTC.
        // 6.9.19.2.b. Fail if any ECU does not report MIL off.
        // 6.9.19.2.c. Fail if no DM12 message is received from any OBD ECU.
        // 6.9.19.2.d. Fail if NACK not received from OBD ECUs that did not support DM12 message.
    }

}