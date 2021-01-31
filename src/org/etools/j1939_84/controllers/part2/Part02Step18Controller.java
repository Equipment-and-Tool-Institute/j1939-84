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

public class Part02Step18Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    private final DataRepository dataRepository;

    Part02Step18Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part02Step18Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
        super(executor,
              engineSpeedModule,
              bannerModule,
              vehicleInformationModule,
              new DiagnosticMessageModule(), dateTimeModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
        this.dataRepository = dataRepository;
    }

    @Override
    protected void run() throws Throwable {
       getListener().onResult("There are " + dataRepository.getObdModuleAddresses() + " OBD Modules");
    }
}
