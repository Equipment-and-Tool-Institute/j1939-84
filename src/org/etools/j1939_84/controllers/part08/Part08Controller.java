/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
 * 6.8 Part 8 Verify Fault B for General Denominator Demonstration
 */
public class Part08Controller extends PartController {

    public Part08Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new Part08Step01Controller(),
             new Part08Step02Controller(),
             new Part08Step03Controller(),
             new Part08Step04Controller(),
             new Part08Step05Controller(),
             new Part08Step06Controller(),
             new Part08Step07Controller(),
             new Part08Step08Controller(),
             new Part08Step09Controller(),
             new Part08Step10Controller(),
             new Part08Step11Controller(),
             new Part08Step12Controller(),
             new Part08Step13Controller(),
             new Part08Step14Controller(),
             new Part08Step15Controller(),
             new Part08Step16Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part08Controller(Executor executor,
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
              8,
              stepControllers);
    }

}
