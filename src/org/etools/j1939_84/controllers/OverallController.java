/*
 * Copyright (c) 2002. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.part1.Part01Controller;
import org.etools.j1939_84.controllers.part2.Part02Controller;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The Controller that manages the other Controllers each of which is
 * responsible for one part only.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OverallController extends Controller {
    private PartController activeController;

    private final List<PartController> partControllers = new ArrayList<>();

    public OverallController() {
        this(DataRepository.getInstance());
    }

    private OverallController(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             dataRepository,
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule(),
             new Part01Controller(dataRepository),
             new Part02Controller(dataRepository),
             new Part03Controller(dataRepository),
             new Part04Controller(dataRepository),
             new Part05Controller(dataRepository),
             new Part06Controller(dataRepository),
             new Part07Controller(dataRepository),
             new Part08Controller(dataRepository),
             new Part09Controller(dataRepository),
             new Part10Controller(dataRepository),
             new Part11Controller(dataRepository),
             new Part12Controller(dataRepository));
    }

    private OverallController(Executor executor,
                              BannerModule bannerModule,
                              DateTimeModule dateTimeModule,
                              DataRepository dataRepository,
                              EngineSpeedModule engineSpeedModule,
                              VehicleInformationModule vehicleInformationModule,
                              DiagnosticMessageModule diagnosticMessageModule,
                              PartController... partControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
        this.partControllers.addAll(Arrays.asList(partControllers));
    }

    @Override
    public String getDisplayName() {
        return "Overall Controller";
    }

    @Override
    protected void run() throws Throwable {
        try {
            getBannerModule().reportHeader(getListener());

            for (PartController controller : partControllers) {
                activeController = controller;
                activeController.run(getListener(), getJ1939());
                activeController = null;
                if (getEnding() != null) {
                    break;
                }
            }
            setEnding(Ending.COMPLETED);
        } finally {
            finished();
        }
    }

    @Override
    public void stop() {
        Optional.ofNullable(activeController).ifPresent(Controller::stop);
        super.stop();
    }
}
