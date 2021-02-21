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
 * 6.7.12 DM25: Expanded Freeze Frame
 */
public class Part07Step12Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part07Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step12Controller(Executor executor,
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
        // 6.7.12.1.a. DS DM25 ([send Request (PGNPG 59904) for PGNPG 64951 (SPNSPs 3300, 1214-1215)]) to each OBD ECU.
        // 6.7.12.2.a. Fail if no ECU reports Freeze Frame data.
        // 6.7.12.2.b. Fail if DTC in reported Freeze Frame data does not include the DTC provided by DM23 earlier in
        // this part.
        // 6.7.12.2.c. Fail if NACK not received from OBD ECUs that did not provide DM25 message.
        // 6.7.12.3.a. Warn if more than one Freeze Frame is provided
    }

}
