/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

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
 * 6.12.9 DM11: Diagnostic Data Clear/Reset for Active DTCs
 */
public class Part12Step09Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part12Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part12Step09Controller(Executor executor,
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
        // 6.12.9.1.a. DS DM11 [(send Request (PGN 59904) for PGN 65235]) to each OBD ECU.
        // 6.12.9.1.b. Wait 5 seconds before checking for erased data.
        // 6.12.9.2.a. Fail if any OBD ECU does not respond with a NACK.
        // 6.12.9.2.b. Check diagnostic information as described in Section A.5 and fail if any ECU partially erases
        // diagnostic information (pass if it erases either all or none).
        // 6.12.9.2.c. For systems with multiple ECUs, fail if one OBD ECU or more than one OBD ECU erases diagnostic
        // information and one or more other OBD ECUs do not erase diagnostic information.
        // 6.12.9.3.a. Global DM11.
        // 6.12.9.3.b. Wait 5 seconds before checking for erased data.
        // 6.12.9.4.a. Fail if any OBD ECU responds with a NACK.
        // 6.12.9.4.b. Warn if any OBD ECU responds with an ACK.
        // 6.12.9.4.c. Check diagnostic information and fail if any ECU partially erases diagnostic information
        // (pass if it erases either all or none).
        // 6.12.9.4.d. Fail if one OBD ECU or more than one OBD ECU erases diagnostic information and one or more
        // other ECUs do not erase diagnostic information. See Section A.5 for the methods to check for erasure of
        // diagnostic information.
    }

}
