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
 * 6.4.9 DM27: All Pending DTCs
 */
public class Part04Step09Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part04Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step09Controller(Executor executor,
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
        // 6.4.9.1.a Global DM27 [(send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)]).
        // 6.4.9.2.a (if supported) Fail if any ECU reports a pending DTC.
        // 6.4.9.2.b (if supported) Fail if any ([OBD)] ECU reports a different MIL status than it did for DM12 response earlier in this part.
        // 6.4.9.3.a DS DM27 to each OBD ECU.
        // 6.4.9.4.a (if supported) Fail if any difference compared to data received from global request.
        // 6.4.9.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}