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
 * 6.12.10 DM7/ DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part12Step10Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part12Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part12Step10Controller(Executor executor,
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
        // 6.12.10.1.a. DS DM7 with TID 250 for specific ECU address + SPN + FMI that had non-initialized values earlier in part 12 test 8.
        // 6.12.10.1.b. Use responses to help verify coordinated DM11 code clear in this part (i.e., all or no ECUs clear).
        // For example, this will be evident in the increased count of initialized test results for the SPN and FMI
        // pairs that were listed with non-initialized values.
    }

}