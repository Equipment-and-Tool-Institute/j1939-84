/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939tools.j1939.model.KeyState.KEY_ON_ENGINE_RUNNING;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.4.1 Confirm Engine Running Status
 */
public class Part04Step01Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 0;

    Part04Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step01Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.4.1.1.a. Gather broadcast data for engine speed (e.g., SPN 190).
        // 6.4.1.2.a. If engine speed is <400 rpm, prompt/warn operator to confirm engine is running and then press
        // enter.
        ensureKeyStateIs(KEY_ON_ENGINE_RUNNING, "6.4.1.2.a");
    }

}
