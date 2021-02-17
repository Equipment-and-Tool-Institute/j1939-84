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
 * 6.8.10 DM25: Expanded Freeze Frame
 */
public class Part08Step10Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part08Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step10Controller(Executor executor,
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
        // 6.8.10.1.a. DS DM25 [(send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214, 1215)]) to each OBD ECU.
        // 6.8.10.2.a. Fail if DTC(s) reported in the freeze frame does not include either the DTC reported in DM12 or the DTC reported in DM23 earlier in this part
        // 6.8.10.2.b. Fail if no freeze frame data (i.e. an empty freeze frame) is provided.
        // 6.8.10.2.c. Fail if NACK not received from OBD that did not provide an DM25 message.
        // 6.8.10.3.a. Warn if DTC reported by DM23 earlier in this part is not present in the freeze frame data.
    }

}