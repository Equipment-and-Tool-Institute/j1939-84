/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.11 Part 11 Exercise General Denominator
 */
public class Part11Controller extends PartController {

    public Part11Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part11Step01Controller(),
             new Part11Step02Controller(),
             new Part11Step03Controller(),
             new Part11Step04Controller(),
             new Part11Step05Controller(),
             new Part11Step06Controller(),
             new Part11Step07Controller(),
             new Part11Step08Controller(),
             new Part11Step09Controller(),
             new Part11Step10Controller(),
             new Part11Step11Controller(),
             new Part11Step12Controller(),
             new Part11Step13Controller(),
             new Part11Step14Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part11Controller(Executor executor,
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
              11,
              stepControllers);
    }

}
