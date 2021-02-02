/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 7 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Part07Controller extends PartController {

    public Part07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
              new BannerModule(),
              DateTimeModule.getInstance(),
              dataRepository,
              new EngineSpeedModule(),
              new VehicleInformationModule(),
              new DiagnosticMessageModule());
    }

    /**
     * Constructor exposed for testing
     */
    public Part07Controller(Executor executor,
                            BannerModule bannerModule,
                            DateTimeModule dateTimeModule,
                            DataRepository dataRepository,
                            EngineSpeedModule engineSpeedModule,
                            VehicleInformationModule vehicleInformationModule,
                            DiagnosticMessageModule diagnosticMessageModule,
                            StepController... stepControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              7,
              stepControllers);
    }

    @Override
    protected List<StepController> getStepControllers() {
        return getStepControllers(7, 17);
    }
}
