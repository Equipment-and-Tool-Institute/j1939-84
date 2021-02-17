/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

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
 * 6.6.11 Complete Fault A Three Trip Countdown Cycle 3
 */
public class Part06Step11Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part06Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step11Controller(Executor executor,
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
        // 6.6.11.1.a Turn engine off.
        // 6.6.11.1.b Wait engine manufacturer’s recommended interval.
        // 6.6.11.1.c Turn key to on position.
        // 6.6.11.1.d If required by engine manufacturer, start the engine for start to start operating cycle effects.
        // 6.6.11.1.e Otherwise, Proceed with part 7.
        // 6.6.11.1.f Turn engine off.
        // 6.6.11.1.g Wait engine manufacturer’s recommended interval.
        // 6.6.11.1.h Turn the key to the on position.
        // 6.6.11.1.i Proceed with part 7.
    }

}