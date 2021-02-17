/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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
 * 6.4 Test Confirmed Fault A
 */
public class Part04Controller extends PartController {

    public Part04Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new Part04Step01Controller(),
             new Part04Step02Controller(),
             new Part04Step03Controller(),
             new Part04Step04Controller(),
             new Part04Step05Controller(),
             new Part04Step06Controller(),
             new Part04Step07Controller(),
             new Part04Step08Controller(),
             new Part04Step09Controller(),
             new Part04Step10Controller(),
             new Part04Step11Controller(),
             new Part04Step12Controller(),
             new Part04Step13Controller(),
             new Part04Step14Controller(),
             new Part04Step15Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part04Controller(Executor executor,
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
              4,
              stepControllers);
    }

}
