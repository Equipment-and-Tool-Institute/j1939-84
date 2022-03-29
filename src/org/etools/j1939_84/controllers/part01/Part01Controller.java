/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.1 KOEO Data Collection
 */
public class Part01Controller extends PartController {

    public Part01Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part01Step01Controller(),
             new Part01Step02Controller(),
             new Part01Step03Controller(dataRepository),
             new Part01Step04Controller(dataRepository, DateTimeModule.getInstance()),
             new Part01Step05Controller(dataRepository),
             new Part01Step06Controller(dataRepository),
             new Part01Step07Controller(dataRepository),
             new Part01Step08Controller(dataRepository),
             new Part01Step09Controller(dataRepository),
             new Part01Step10Controller(dataRepository),
             new Part01Step11Controller(dataRepository),
             new Part01Step12Controller(dataRepository),
             new Part01Step13Controller(dataRepository),
             new Part01Step14Controller(dataRepository),
             new Part01Step15Controller(dataRepository),
             new Part01Step16Controller(dataRepository),
             new Part01Step17Controller(dataRepository),
             new Part01Step18Controller(dataRepository),
             new Part01Step19Controller(dataRepository),
             new Part01Step20Controller(dataRepository),
             new Part01Step21Controller(dataRepository),
             new Part01Step22Controller(dataRepository),
             new Part01Step23Controller(),
             new Part01Step24Controller(dataRepository),
             new Part01Step25Controller(dataRepository),
             new Part01Step26Controller(),
             new Part01Step27Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part01Controller(Executor executor,
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
              1,
              stepControllers);
    }
}
