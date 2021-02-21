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
 * 6.9.22 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part09Step22Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 22;
    private static final int TOTAL_STEPS = 0;

    Part09Step22Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step22Controller(Executor executor,
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
        // 6.9.22.1.a. Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.9.22.2.a. (if supported) Fail if any ECU does not report MIL off or MIL not supported. See Section A.8 for
        // allowed values.
        // 6.9.22.2.b. (if supported) Fail if any OBD ECU reports a previously active DTC.
        // 6.9.22.3.a. DS DM2 to each OBD ECU.
        // 6.9.22.4.a. (if supported) Fail if any difference compared to data received during global request.
        // 6.9.22.4.b. (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}
