/*
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.PartController;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 1 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Part01Controller extends PartController {

    private final List<StepController> stepControllers = new ArrayList<>();

    /**
     * Constructor
     */
    public Part01Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
             new VehicleInformationModule(), dataRepository, DateTimeModule.getInstance());
    }

    private Part01Controller(Executor executor, EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule, VehicleInformationModule vehicleInformationModule,
                             DataRepository dataRepository, DateTimeModule dateTimeModule) {
        this(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule,
             new Part01Step01Controller(dataRepository), new Part01Step02Controller(), new Part01Step03Controller(dataRepository),
             new Part01Step04Controller(dataRepository), new Part01Step05Controller(dataRepository),
             new Part01Step06Controller(dataRepository), new Part01Step07Controller(dataRepository),
             new Part01Step08Controller(dataRepository), new Part01Step09Controller(dataRepository),
             new Part01Step10Controller(dataRepository), new Part01Step11Controller(dataRepository),
             new Part01Step12Controller(dataRepository), new Part01Step13Controller(dataRepository),
             new Part01Step14Controller(dataRepository), new Part01Step15Controller(dataRepository),
             new Part01Step16Controller(dataRepository), new Part01Step17Controller(dataRepository),
             new Part01Step18Controller(dataRepository), new Part01Step19Controller(dataRepository),
             new Part01Step20Controller(dataRepository), new Part01Step21Controller(dataRepository),
             new Part01Step22Controller(dataRepository), new Part01Step23Controller(),
             new Part01Step24Controller(dataRepository), new Part01Step25Controller(dataRepository),
             new Part01Step26Controller(dataRepository), new Part01Step27Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part01Controller(Executor executor,
                            EngineSpeedModule engineSpeedModule,
                            BannerModule bannerModule,
                            VehicleInformationModule vehicleInformationModule,
                            DateTimeModule dateTimeModule,
                            Part01Step01Controller part01Step01Controller,
                            Part01Step02Controller part01Step02Controller,
                            Part01Step03Controller part01Step03Controller,
                            Part01Step04Controller part01Step04Controller,
                            Part01Step05Controller part01Step05Controller,
                            Part01Step06Controller part01Step06Controller,
                            Part01Step07Controller part01Step07Controller,
                            Part01Step08Controller part01Step08Controller,
                            Part01Step09Controller part01Step09Controller,
                            Part01Step10Controller part01Step10Controller,
                            Part01Step11Controller part01Step11Controller,
                            Part01Step12Controller part01Step12Controller,
                            Part01Step13Controller part01Step13Controller,
                            Part01Step14Controller part01Step14Controller,
                            Part01Step15Controller part01Step15Controller,
                            Part01Step16Controller part01Step16Controller,
                            Part01Step17Controller part01Step17Controller,
                            Part01Step18Controller part01Step18Controller,
                            Part01Step19Controller part01Step19Controller,
                            Part01Step20Controller part01Step20Controller,
                            Part01Step21Controller part01Step21Controller,
                            Part01Step22Controller part01Step22Controller,
                            Part01Step23Controller part01Step23Controller,
                            Part01Step24Controller part01Step24Controller,
                            Part01Step25Controller part01Step25Controller,
                            Part01Step26Controller part01Step26Controller,
                            Part01Step27Controller part01Step27Controller) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule);

        stepControllers.add(part01Step01Controller);
        stepControllers.add(part01Step02Controller);
        stepControllers.add(part01Step03Controller);
        stepControllers.add(part01Step04Controller);
        stepControllers.add(part01Step05Controller);
        stepControllers.add(part01Step06Controller);
        stepControllers.add(part01Step07Controller);
        stepControllers.add(part01Step08Controller);
        stepControllers.add(part01Step09Controller);
        stepControllers.add(part01Step10Controller);
        stepControllers.add(part01Step11Controller);
        stepControllers.add(part01Step12Controller);
        stepControllers.add(part01Step13Controller);
        stepControllers.add(part01Step14Controller);
        stepControllers.add(part01Step15Controller);
        stepControllers.add(part01Step16Controller);
        stepControllers.add(part01Step17Controller);
        stepControllers.add(part01Step18Controller);
        stepControllers.add(part01Step19Controller);
        stepControllers.add(part01Step20Controller);
        stepControllers.add(part01Step21Controller);
        stepControllers.add(part01Step22Controller);
        stepControllers.add(part01Step23Controller);
        stepControllers.add(part01Step24Controller);
        stepControllers.add(part01Step25Controller);
        stepControllers.add(part01Step26Controller);
        stepControllers.add(part01Step27Controller);
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Test";
    }

    @Override
    protected PartResult getPartResult() {
        return getPartResult(1);
    }

    @Override
    public List<StepController> getStepControllers() {
        return stepControllers;
    }
}
