/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

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
 * 6.11.7 DM20/DM28/Broadcast data: Waiting until General Denominator Is Met
 */
public class Part11Step07Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part11Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part11Step07Controller(Executor executor,
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
        // 6.11.7.1.a. Broadcast data received shall comply with the values defined in Section A.1.
        // 6.11.7.1.b. Wait 3 minutes.
        // 6.11.7.1.c. Increase engine speed over 1150 rpm (a minimum of 300 seconds at this speed is required).
        // 6.11.7.1.d. Periodic DS DM20 to ECUs that reported data earlier in this part and DS DM28s to ECU that
        // reported permanent DTC earlier in this part (no more than once every 1 second) while timing engine operation
        // versus the general denominator timing requirement.
        // 6.11.7.1.e. [Every 10th query set may be reported in the log unless the failure criteria for DM20 or DM28
        // were met].
        // 6.11.7.1.f. After 300 seconds have been exceeded, reduce the engine speed back to idle.
        // 6.11.7.2.a. Fail if there is any DM20 response that indicates any denominator is greater than the value it
        // was earlier in this part before general denominator timing has elapsed.
        // 6.11.7.2.b. Fail if there is any DM28 response that indicates the permanent DTC is no longer present before
        // general denominator timing has elapsed.
        // 6.11.7.2.c. Fail if any broadcast data is missing according to Table A1, or otherwise meets failure criteria
        // during engine idle speed periods.
        // 6.11.7.3.a. Identify any broadcast data meeting warning criteria in Table A1 during engine idle periods.
        // 6.11.7.4.a. Once 620 seconds of engine operation overall in part 11 have elapsed (including over 300 seconds
        // of engine operation over 1150 rpm), end periodic DM20 and DM28 and continue with test 6.11.8.
    }

}
