/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

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
 * 6.2.17 KOER Data stream verification
 */
public class Part02Step17Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    Part02Step17Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part02Step17Controller(Executor executor,
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
        // 6.2.17.1.a. Gather broadcast data for all SPNs that are supported for data stream in the OBD ECU responses.

        // 6.2.17.2.a. Fail if no response/no valid data for any broadcast SPN indicated as supported in DM24.
        // 6.2.17.2.b. Fail/warn if any broadcast data is not valid for KOER conditions as per Table A-1, Minimum Data Stream Support.
        // 6.2.17.2.c. Fail/warn per Table A-1 if an expected SPN from the DM24 support list is provided by a non-OBD ECU.
        // 6.2.17.2.d. Fail/warn per Table A-1, if two or more ECUs provide an SPN listed.

        // 6.2.17.3.a. Identify SPNs provided in the data stream that are listed in Table A-1 but not supported by any OBD ECU in its DM24 response.
        // 6.2.17.4.a. Fail/warn per Table A-1 column, “Action if SPN provided but not included in DM24”.

        // 6.2.17.5.a. DS messages to ECU that indicated support in DM24 for upon request SPNs and SPNs not observed in step 1.
        // 6.2.17.5.b. If no response/no valid data for any SPN requested in 6.2.16.3.a, send global message to request that SPN(s).

        // 6.2.17.6.a. Fail if no response/no valid data for any upon request SPN indicated as supported in DM24, per Table A-1.
        // 6.2.17.6.b. Fail/warn if any upon request data is not valid for KOER conditions as per section A.1.
        // 6.2.17.6.c. Warn when global request was required for “broadcast” SPN
    }
}
