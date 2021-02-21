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
 * 6.7.15 DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results
 */
public class Part07Step15Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part07Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step15Controller(Executor executor,
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
        // 6.7.15.1.a DS DM7 with TID 246 + SPN 5846 + FMI 31.
        // 6.7.15.1.a.i. If TID 246 method not supported, use DS DM7 with TID 247 + each DM24 SPNSP+ FMI 31. b. Create
        // list of any ECU address+SPN+FMI combination with non-initialized test results.
        // 6.7.15.2.a. Fail if any difference in the ECU address+SPN+FMI combinations that report test results compared
        // to list created in part 1.
        // 6.7.15.2.b. Fail if NACK received from OBD ECUs that did not support an SPNSP listed in its DM24 response
    }

}
