/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

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
 * 6.7 Verify DM23 Transition
 */
public class Part07Controller extends PartController {

    public Part07Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part07Step01Controller(),
             new Part07Step02Controller(),
             new Part07Step03Controller(),
             new Part07Step04Controller(),
             new Part07Step05Controller(),
             new Part07Step06Controller(),
             new Part07Step07Controller(),
             new Part07Step08Controller(),
             new Part07Step09Controller(),
             new Part07Step10Controller(),
             new Part07Step11Controller(),
             new Part07Step12Controller(),
             new Part07Step13Controller(),
             new Part07Step14Controller(),
             new Part07Step15Controller(),
             new Part07Step16Controller(),
             new Part07Step17Controller(),
             new Part07Step18Controller());
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
                            CommunicationsModule communicationsModule,
                            StepController... stepControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              7,
              stepControllers);
    }
}
