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
 * 6.11.11 DM26: Diagnostic Readiness 3
 */
public class Part11Step11Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part11Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step11Controller(Executor executor,
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
        // 6.11.11.1.a. DS DM26 [(send Request (PGN 59904) for PGN 64952 (SPNs 3301-3305)]) to each OBD ECU.
        // 6.11.11.1.b. Record all monitor readiness this trip data (i.e., which supported monitors are complete this
        // trip or supported and not complete this trip).
        // 6.11.11.2.a. Fail if response indicates time since engine start (SPN 3301) differs by more than ±10 seconds
        // from expected value (calculated by software using original DM26 response in this part plus accumulated time
        // since then);.
        // i.e., Fail if ABS[(Time Since Engine StartB - Time Since Engine StartA) - Delta Time] > 10 seconds.
        // 6.11.11.2.b. Fail if NACK not received from OBD ECUs that did not provide a DM26 message.
    }

}
