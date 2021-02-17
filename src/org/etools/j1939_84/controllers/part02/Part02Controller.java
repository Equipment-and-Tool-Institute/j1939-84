/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.2 Key On Engine Running Data Collection
 */
public class Part02Controller extends PartController {

    public Part02Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new Part02Step01Controller(),
             new Part02Step02Controller(dataRepository),
             new Part02Step03Controller(dataRepository),
             new Part02Step04Controller(dataRepository),
             new Part02Step05Controller(dataRepository),
             new Part02Step06Controller(dataRepository),
             new Part02Step07Controller(dataRepository),
             new Part02Step08Controller(dataRepository),
             new Part02Step09Controller(dataRepository),
             new Part02Step10Controller(dataRepository),
             new Part02Step11Controller(dataRepository),
             new Part02Step12Controller(dataRepository),
             new Part02Step13Controller(dataRepository),
             new Part02Step14Controller(dataRepository),
             new Part02Step15Controller(dataRepository),
             new Part02Step16Controller(dataRepository),
             new Part02Step17Controller(),
             new Part02Step18Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part02Controller(Executor executor,
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
              2,
              stepControllers);
    }

}
