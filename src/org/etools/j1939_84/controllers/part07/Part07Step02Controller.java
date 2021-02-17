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
 * 6.7.2 DM23: Emission Related Previously Active DTCs
 */
public class Part07Step02Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part07Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step02Controller(Executor executor,
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
        // 6.7.2.1.a DS DM23 ([send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)]) to each OBD ECU.
        // 6.7.2.2.a Fail if no OBD ECU reports previously active DTC.
        // 6.7.2.2.b Fail if reported previously active DTC does not match DM12 active DTC from part 6.
        // 6.7.2.2.c Fail if any ECU does not report MIL off and not flashing.
        // 6.7.2.2.d Fail if NACK not received from OBD ECUs that did not provide a DM23 message.
        // 6.7.2.3.a Warn if any ECU reports > 1 previously active DTC.
        // 6.7.2.3.b Warn if more than one ECU reports a previously active DTC.
    }

}