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
 * 6.4.2 DM12: Emissions Related Active DTCs
 */
public class Part04Step02Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part04Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step02Controller(Executor executor,
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
        // 6.4.2.1.a. Global DM12 ([send Request (PGN 59904) for PGN 65236 (SPN 1213-1215, 1706, and 3038)]) to retrieve confirmed and active DTCs.
        // 6.4.2.1.a.i. Repeat request no more frequently than once per second until one or more ECUs reports a confirmed and active DTC.
        // 6.4.2.1.a.ii. Time-out after every 5 minutes and ask user “‘yes/no”’ to continue if there is still no confirmed and active DTC; fail if user says “'no”' and no ECU reports a confirmed and active DTC.
        // 6.4.2.2.a. Fail if no ECU reports MIL on. See Section A.8 for allowed values.
        // 6.4.2.2.b. Fail if DM12 DTC(s) is (are) not the same SPN+FMI(s) as DM6 pending DTC in part 3.
        // 6.4.2.3.a. Warn if any ECU reports > 1 confirmed and active DTC.
        // 6.4.2.3 b. Warn if more than one ECU reports a confirmed and active DTC.
        // 6.4.2.4.a. DS DM12 to each OBD ECU.
        // 6.4.2.5.a. Fail if any difference compared to data received from global request.
        // 6.4.2.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
    }

}