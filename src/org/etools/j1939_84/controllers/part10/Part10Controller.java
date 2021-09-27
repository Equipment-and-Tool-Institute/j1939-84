/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part10;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.10 Part 10 Prime Diagnostic Executive for General Denominator Demonstration
 */
public class Part10Controller extends PartController {

    public Part10Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part10Step01Controller(),
             new Part10Step02Controller(),
             new Part10Step03Controller(),
             new Part10Step04Controller(),
             new Part10Step05Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part10Controller(Executor executor,
                            BannerModule bannerModule,
                            DateTimeModule dateTimeModule,
                            DataRepository dataRepository,
                            EngineSpeedModule engineSpeedModule,
                            VehicleInformationModule vehicleInformationModule,
                            CommunicationsModule communicationsModule,
                            StepController... stepControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              10,
              stepControllers);
    }

}
