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
 * 6.7.8 DM27: All Pending DTCs
 */
public class Part07Step08Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part07Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step08Controller(Executor executor,
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
        // 6.7.8.1.a Global DM27 [(send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.7.8.2.a (if supported) Fail if any OBD ECU reports a pending DTC.
        // 6.7.8.2.b (if supported)Fail if any ECU does not report MIL off.
        // 6.7.8.3.a DS DM27 to each OBD ECU.
        // 6.7.8.4.a (if supported) Fail if any difference compared to data received for global request.
        // 6.7.8.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query
    }

}
