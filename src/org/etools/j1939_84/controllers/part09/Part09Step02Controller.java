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
 * DM12: Emissions Related Active DTCs
 */
public class Part09Step02Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part09Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part09Step02Controller(Executor executor,
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
        // 6.9.2.1.a Global DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.9.2.1.b Create list of which OBD ECU(s) have a DM12 active MIL on DTC and which do not. This list will be used for test 6.9.8.
        // 6.9.2.2.a Fail if no OBD ECU reporting one or more active MIL on DTCs.
        // 6.9.2.2.b Fail if no OBD ECUs reporting MIL commanded on. See Section A.8 for allowed values.
        // 6.9.2.2.c Fail if any ECU reports a different active MIL on DTC(s) than what that ECU reported in part 8 DM12 response.
        // 6.9.2.3.a Warn if any ECU reports > 1 active DTC.
        // 6.9.2.3.b Warn if more than one ECU reports an active DTC
    }

}