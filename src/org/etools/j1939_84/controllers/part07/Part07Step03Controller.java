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
 * 6.7.3 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part07Step03Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part07Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step03Controller(Executor executor,
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
        // 6.7.3.1.a Global DM2 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.7.3.2.a (if supported) Fail if no OBD ECU reports any previously active DTC(s).
        // 6.7.3.2.b (if supported) Fail if any OBD ECU reports a fewer previously active DTCs than in DM23 response
        // earlier in this part.
        // 6.7.3.2.c (if supported) Fail if any OBD ECU fails to provide its DTC from its DM12 response in part 6 as a
        // previously active DTC in its DM2 response.
        // 6.7.3.2.d (if supported) Fail if any OBD ECU does not report MIL off. See Section A.8 for allowed values.
        // Fail if any non-OBD ECU does not report MIL off or not supported.
        // 6.7.3.3.a DS DM2 to each OBD ECU.
        // 6.7.3.4.a (if supported) Fail if any difference compared to data received for global request.
        // 6.7.3.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}
