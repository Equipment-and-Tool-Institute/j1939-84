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
 * 6.6.3 DM12: Emissions Related Active DTCs
 */
public class Part06Step03Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part06Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step03Controller(Executor executor,
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

        // 6.6.3.1.a DS DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 3038, 1706))] to each OBD ECU.
        // 6.6.3.2.a Fail if no ([OBD]) ECU reports an MIL-on active DTC.
        // 6.6.3.2.b Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        // 6.6.3.2.c Fail if NACK not received from OBD ECUs that did not provide a DM12 message.
    }

}
