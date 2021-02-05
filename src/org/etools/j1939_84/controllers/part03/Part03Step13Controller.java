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
 * 6.3.13 DM25: Expanded freeze frame
 */
public class Part03Step13Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 13;
    private static final int TOTAL_STEPS = 0;

    Part03Step13Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step13Controller(Executor executor,
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
        // 6.3.13.1.a. DS DM25 (send Request (PGN 59904) for PGN 64951 (SPNs 3300, 1214-1215)) to each OBD ECU.
        // 6.3.13.1.b. If no response (transport protocol RTS or NACK(Busy) in 220 ms), then retry DS DM25 request to the OBD ECU. [Do not attempt retry for NACKS that indicate not supported]
        // 6.3.13.1.c. Translate and print in log file all received freeze frame data with data labels assuming data received in order expected by DM24 response for visual check by test log reviewer.

        // 6.3.13.2.a. Fail if retry was required to obtain DM25 response.
        // 6.3.13.2.b. Fail if no ECU has freeze frame data to report.
        // 6.3.13.2.c. Fail if received data does not match expected number of bytes based on DM24 supported SPN list for that ECU.
        // 6.3.13.2.d. Fail if freeze frame data does not include the same SPN+FMI as DM6 pending DTC earlier in this part.37
        // 6.3.13.2.e. Fail/warn per section A.2, Criteria for Freeze Frame Evaluation.

        // 6.3.13.2.f. Warn if more than 1 freeze frame data set is included in the response.
        // 6.3.13.2.g. Fail if NACK not received from OBD ECUs that did not provide DM25 response to query.
    }

}
