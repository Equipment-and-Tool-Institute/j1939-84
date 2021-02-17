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
 * 6.7.11 DM31: DTC to Lamp Association
 */
public class Part07Step11Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part07Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step11Controller(Executor executor,
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
        // 6.7.11.1.a. DS DM31 [(send Request (PGN 59904) for PGN 41728 (SPNs 1214-1215, 4113, 4117))] to each OBD ECU.
        // 6.7.11.2.a. (if supported) Fail if any ECU response includes the same DTC as it reported by DM23 earlier in this part.
        // 6.7.11.2.b. (if supported) Fail if any ECU does not report MIL off for all DTCs reported.
        // 6.7.11.2.c. (if supported) Fail if NACK not received from OBD ECUs that did not provide DM31 message.
    }

}