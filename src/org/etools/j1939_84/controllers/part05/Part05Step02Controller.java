/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

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
 * 6.5.2 DM12: Emission-Related Active DTCs
 */
public class Part05Step02Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part05Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part05Step02Controller(Executor executor,
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
        // 6.5.2.1.a Global DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.5.2.2.a Fail if no OBD ECU reporting MIL on. See Section A.8 for allowed values.
        // 6.5.2.2.b Fail if all OBD ECUs report no DM12 DTC set.
        // 6.5.2.2.c Fail if DM12 DTC reported does not match the DM6 DTC SPN and FMI reported from step 6.4.2.
        // 6.5.2.2.d Fail if any ECU reporting MIL as ON, flashing. See Section A.8 for allowed values.
    }

}