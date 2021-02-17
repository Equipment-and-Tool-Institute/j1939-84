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
 * 6.4.4 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part04Step04Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part04Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step04Controller(Executor executor,
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
        // 6.4.4.1.a Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.4.4.2.a (if supported) Fail if any OBD ECU reports > 0 previously active DTCs.
        // 6.4.4.2.b (if supported) Fail if any OBD ECU reports a different MIL status (e.g., on and flashing, or off) than it did in DM12 response earlier in this part.
        // 6.4.4.3.a DS DM2 to each OBD ECU.
        // 6.4.4.4.a (if supported) Fail if any difference compared to data received from global request.
        // 6.4.4.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}