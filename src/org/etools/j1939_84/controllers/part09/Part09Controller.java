/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9 Verify Deletion of Fault B with DM11
 */
public class Part09Controller extends PartController {

    public Part09Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part09Step01Controller(),
             new Part09Step02Controller(),
             new Part09Step03Controller(),
             new Part09Step04Controller(),
             new Part09Step05Controller(),
             new Part09Step06Controller(),
             new Part09Step07Controller(),
             new Part09Step08Controller(),
             new Part09Step09Controller(),
             new Part09Step10Controller(),
             new Part09Step11Controller(),
             new Part09Step12Controller(),
             new Part09Step13Controller(),
             new Part09Step14Controller(),
             new Part09Step15Controller(),
             new Part09Step16Controller(),
             new Part09Step17Controller(),
             new Part09Step18Controller(),
             new Part09Step19Controller(),
             new Part09Step20Controller(),
             new Part09Step21Controller(),
             new Part09Step22Controller(),
             new Part09Step23Controller(),
             new Part09Step24Controller(),
             new Part09Step25Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part09Controller(Executor executor,
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
              9,
              stepControllers);
    }

}
