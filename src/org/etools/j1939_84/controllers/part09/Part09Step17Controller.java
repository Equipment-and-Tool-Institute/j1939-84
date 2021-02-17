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
 * 6.9.17 DM25: Expanded Freeze Frame
 */
public class Part09Step17Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    Part09Step17Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step17Controller(Executor executor,
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
        // 6.9.17.1.a. DS DM25 [(send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)]) to each OBD ECU.
        // 6.9.17.2.a. Fail if any OBD ECU reports other than no Freeze Frame data stored (bytes 1-5 = 0x00, 6-8= 0xFF).
        // 6.9.17.2.b. Fail if NACK now received from OBD ECUs that previously provided a DM25 message
    }

}