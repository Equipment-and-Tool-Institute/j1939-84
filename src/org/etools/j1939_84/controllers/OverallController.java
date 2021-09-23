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

import org.etools.j1939_84.controllers.part01.Part01Controller;
import org.etools.j1939_84.controllers.part02.Part02Controller;
import org.etools.j1939_84.controllers.part03.Part03Controller;
import org.etools.j1939_84.controllers.part04.Part04Controller;
import org.etools.j1939_84.controllers.part05.Part05Controller;
import org.etools.j1939_84.controllers.part06.Part06Controller;
import org.etools.j1939_84.controllers.part07.Part07Controller;
import org.etools.j1939_84.controllers.part08.Part08Controller;
import org.etools.j1939_84.controllers.part09.Part09Controller;
import org.etools.j1939_84.controllers.part10.Part10Controller;
import org.etools.j1939_84.controllers.part11.Part11Controller;
import org.etools.j1939_84.controllers.part12.Part12Controller;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The Controller that manages the other Controllers each of which is
 * responsible for one part only.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class OverallController extends Controller {
    private final List<PartController> partControllers = new ArrayList<>();
    private PartController activeController;

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
             new CommunicationsModule(),
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
                              CommunicationsModule communicationsModule,
                              PartController... partControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
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
