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
 * 6.8.9 DM31: DTC to Lamp Association
 */
public class Part08Step09Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part08Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part08Step09Controller(Executor executor,
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
        // 6.8.9.1.a. Global DM31 [(send Request (PGN 59904) for PGN 41728 (SPNs 1214, 1215, 4113, 4117)]).
        // 6.8.9.2.a. (if supported) Fail if no ECU reports same DTC as MIL on for as was reported in DM12 earlier in this part.
        // See Section A.8 for allowed values of SPN 4113 and 4117.
        // 6.8.9.2.b. (if supported) Fail if any ECU reports additional or fewer DTCs than those reported in DM12
        // and DM23 responses earlier in this part.
        // 6.8.9.2.c. (if supported) Fail if no ECU reports the same DTC as MIL off for the previous active DTC reported in DM23 earlier in this part.
    }

}