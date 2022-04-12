/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

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

;

/**
 * 6.3 Test Pending Fault A
 */
public class Part03Controller extends PartController {

    public Part03Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             new Part03Step01Controller(),
             new Part03Step02Controller(),
             new Part03Step03Controller(),
             new Part03Step04Controller(),
             new Part03Step05Controller(),
             new Part03Step06Controller(),
             new Part03Step07Controller(),
             new Part03Step08Controller(),
             new Part03Step09Controller(),
             new Part03Step10Controller(),
             new Part03Step11Controller(),
             new Part03Step12Controller(),
             new Part03Step13Controller(),
             new Part03Step14Controller(),
             new Part03Step15Controller(),
             new Part03Step16Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part03Controller(Executor executor,
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
              3,
              stepControllers);
    }

}
