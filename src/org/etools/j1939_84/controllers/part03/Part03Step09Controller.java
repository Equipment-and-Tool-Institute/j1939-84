/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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
 * 6.3.9 DM12: Emissions related active DTCs
 */
public class Part03Step09Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part03Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step09Controller(Executor executor,
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
        // 6.3.9.1.a Global DM12 (send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)).
        // 6.3.9.2.a Fail if any ECU reports an active DTC.
        // 6.3.9.2.b Fail if any OBD ECU does not report MIL off. See section A.8 for allowed values
        // 6.3.9.2.c Fail if any non-OBD ECU does not report MIL off or not supported.
        // 6.3.9.2.d Fail if no OBD ECU provides DM12

        // 6.3.9.3.a DS DM12 to each OBD ECU.
        // 6.3.9.4.a Fail if any difference compared to data received from global request.
        // 6.3.9.4.b Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}
