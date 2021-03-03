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
 * 6.9.11 DM20: Monitor Performance Ratio
 */
public class Part09Step11Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part09Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step11Controller(Executor executor,
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
        // 6.9.11.1.a. DS DM20 [(send Request (PGN 59904) for PGN 49664 (SPNs 3048-3049, 3066-3068)]) to ECUs that
        // responded earlier in this part with DM20 data.
        // 6.9.11.2.a. Fail if any value (ignition cycle, numerator, or denominator) is not equal to the value that it
        // was earlier in Step 6.9.4.1.b (before DM11).
        // 6.9.11.2.b. Fail if any ECU now NACKs DM20 requests after previously providing data in 6.9.4.1.
        // 6.9.11.2.c. Fail if any NACK not received from an OBD ECU that did not provide a DM20 message.
    }

}
