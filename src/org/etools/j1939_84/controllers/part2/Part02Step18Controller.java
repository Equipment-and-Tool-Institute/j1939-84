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
 * 6.2.18 Part 2 to Part 3 transition
 */
public class Part02Step18Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    Part02Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part02Step18Controller(Executor executor,
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
        // 6.2.18.1.a. Turn Engine Off and keep the ignition key in the off position.
        // 6.2.18.1.b. Implant Fault A according to engine manufacturer’s instruction. (See section 5 for additional discussion).
        // 6.2.18.1.c. Turn ignition key to the ON position.
        // 6.2.18.1.d. Observe MIL and Wait to Start Lamps in Instrument Cluster
        // 6.2.18.1.e. Start Engine after MIL and Wait to Start Lamp (if equipped) have extinguished.
    }
}
