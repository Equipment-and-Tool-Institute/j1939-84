/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
 * 6.8.4 DM23: Emission Related Previously Active DTCs
 */
public class Part08Step04Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part08Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step04Controller(Executor executor,
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
        // 6.8.4.1.a Global DM23 [(send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)]).
        // 6.8.4.2.a Fail if no OBD ECU reports a previously active DTC.
        // 6.8.4.2.b Fail if previously active DTC reported is not the same as previously active DTC from part 7.
        // 6.8.4.2.c Fail if any ECU reporting different MIL status than DM12 response earlier in this part.
    }

}