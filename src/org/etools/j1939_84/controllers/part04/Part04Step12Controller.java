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
 * 6.4.12 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part04Step12Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 12;
    private static final int TOTAL_STEPS = 0;

    Part04Step12Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step12Controller(Executor executor,
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
        // 6.4.12.1.a. DS DM7 to each OBD ECU that provided test results in part 1 using TID 246, SPN 5846, and FMI 31.
        // 6.4.12.1.a.i. (If TID 246 method not supported, use DS DM7 with TID 247 + each DM24 SPN+ FMI 31.).
        // 6.4.12.1.b. Create list of any ECU address+SPN+FMI combination with non-initialized test results,
        // noting the number of initialized test results for each SPN+FMI combination that has non-initialized test results.
        // 6.4.12.2.a. Fail if there is any difference in each ECU’s provided test result labels (SPN and FMI combinations)
        // from the test results received in part 1 test 11, paragraph 6.1.11.39
    }

}