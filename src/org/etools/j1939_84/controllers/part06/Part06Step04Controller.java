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
 * 6.6.4 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part06Step04Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part06Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step04Controller(Executor executor,
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
        // 6.6.4.1.a Receive broadcast DM1 [(PGN 65226 (SPNs 1213-1215, 3038, 1706)]).
        // 6.6.4.2.a Fail if no OBD ECU reports MIL on.
        // 6.6.4.2.b Fail the DTC provided by the OBD ECU in DM12 is not included in its DM1 display.
        // 6.6.4.2.c Fail if any OBD ECU reports a different number of active DTCs than what that ECU reported in DM5 for number of active DTCs.
    }

}