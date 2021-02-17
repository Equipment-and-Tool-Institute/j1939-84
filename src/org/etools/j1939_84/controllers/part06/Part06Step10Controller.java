/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

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
 * 6.6.10 DM21: Diagnostic Readiness 2
 */
public class Part06Step10Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part06Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step10Controller(Executor executor,
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
        // 6.6.10.1.a. DS DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3069, 3295)]) to each OBD ECU.
        // 6.6.10.2.a. Fail if any ECU reports distance with MIL on (SPN 3069) is > 0 or reports not supported.
        // 6.6.10.2.b. Fail if any ECU reports time with MIL on greater than 0 minute, and did not report a DTC in its DM12 response.
        // 6.6.10.2.c. Fail if no ECU supports DM21.
        // 6.6.10.2.d. Fail if NACK not received from OBD ECUs that did not provide a DM21 message.
        // 6.6.10.3.a. Warn if no ECU reports time with MIL on (SPN 3295) greater than 0 minute.
        // 6.6.10.3.b. Warn if more than one ECU reports time with MIL on > 0 and difference between times reported is > 1 minute.
    }

}