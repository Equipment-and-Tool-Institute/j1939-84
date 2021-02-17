/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
 * 6.4.7 DM31: DTC to Lamp Association
 */
public class Part04Step07Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part04Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step07Controller(Executor executor,
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
        // 6.4.7.1.a DS DM31 [(send Request (PGN 59904) for PGN 47128 (SPN 1214-1215, 4113, 4117)]) to each ECU supporting DM12.
        // 6.4.7.2.a Fail if an OBD ECU does not include the same SPN and FMI from its DM12 response earlier in this part and report MIL on Status for that SPN and FMI in its DM31 response (if DM31 is supported).
        // 6.4.7.2.b Fail if NACK not received from OBD ECU that did not provide DM31 response.
    }

}