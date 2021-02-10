/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

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
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * The controller for 6.2.1 Part 2 Key On Engine Running Data Collection
 * </p>
 * <p>
 * Part 2 Purpose: Verify data in Key-on, engine running (KOER) operation
 * with no implanted faults.
 * </p>
 */
public class Part02Step01Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 1;
    private static final int TOTAL_STEPS = 0;

    Part02Step01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             DateTimeModule.getInstance());
    }

    Part02Step01Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DateTimeModule dateTimeModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              DataRepository.getInstance(),
              engineSpeedModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(),
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.2.1.1.a. Gather broadcast data for engine speed (e.g., SPN 190).
        ensureKeyOnEngineOn();
    }

}
