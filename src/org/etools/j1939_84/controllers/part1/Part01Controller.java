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
    public Part01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
             new VehicleInformationModule(), new DataRepository());
    }

    private Part01Controller(Executor executor, EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule, VehicleInformationModule vehicleInformationModule,
                             DataRepository dataRepository) {
        this(executor, engineSpeedModule, bannerModule, vehicleInformationModule,
             new Step01Controller(dataRepository), new Step02Controller(), new Step03Controller(dataRepository),
             new Step04Controller(dataRepository), new Step05Controller(dataRepository),
             new Step06Controller(dataRepository), new Step07Controller(dataRepository),
             new Step08Controller(dataRepository), new Step09Controller(dataRepository),
             new Step10Controller(dataRepository), new Step11Controller(dataRepository),
             new Step12Controller(dataRepository), new Step13Controller(dataRepository),
             new Step14Controller(dataRepository), new Step15Controller(dataRepository),
             new Step16Controller(dataRepository), new Step17Controller(dataRepository),
             new Step18Controller(dataRepository), new Step19Controller(dataRepository),
             new Step20Controller(dataRepository), new Step21Controller(dataRepository),
             new Step22Controller(dataRepository), new Step23Controller(),
             new Step24Controller(dataRepository), new Step25Controller(dataRepository),
             new Step26Controller(dataRepository), new Step27Controller());
    }

    /**
     * Constructor exposed for testing
     */
    public Part01Controller(Executor executor,
                            EngineSpeedModule engineSpeedModule,
                            BannerModule bannerModule,
                            VehicleInformationModule vehicleInformationModule,
                            Step01Controller step01Controller,
                            Step02Controller step02Controller,
                            Step03Controller step03Controller,
                            Step04Controller step04Controller,
                            Step05Controller step05Controller,
                            Step06Controller step06Controller,
                            Step07Controller step07Controller,
                            Step08Controller step08Controller,
                            Step09Controller step09Controller,
                            Step10Controller step10Controller,
                            Step11Controller step11Controller,
                            Step12Controller step12Controller,
                            Step13Controller step13Controller,
                            Step14Controller step14Controller,
                            Step15Controller step15Controller,
                            Step16Controller step16Controller,
                            Step17Controller step17Controller,
                            Step18Controller step18Controller,
                            Step19Controller step19Controller,
                            Step20Controller step20Controller,
                            Step21Controller step21Controller,
                            Step22Controller step22Controller,
                            Step23Controller step23Controller,
                            Step24Controller step24Controller,
                            Step25Controller step25Controller,
                            Step26Controller step26Controller,
                            Step27Controller step27Controller) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule);

        stepControllers.add(step01Controller);
        stepControllers.add(step02Controller);
        stepControllers.add(step03Controller);
        stepControllers.add(step04Controller);
        stepControllers.add(step05Controller);
        stepControllers.add(step06Controller);
        stepControllers.add(step07Controller);
        stepControllers.add(step08Controller);
        stepControllers.add(step09Controller);
        stepControllers.add(step10Controller);
        stepControllers.add(step11Controller);
        stepControllers.add(step12Controller);
        stepControllers.add(step13Controller);
        stepControllers.add(step14Controller);
        stepControllers.add(step15Controller);
        stepControllers.add(step16Controller);
        stepControllers.add(step17Controller);
        stepControllers.add(step18Controller);
        stepControllers.add(step19Controller);
        stepControllers.add(step20Controller);
        stepControllers.add(step21Controller);
        stepControllers.add(step22Controller);
        stepControllers.add(step23Controller);
        stepControllers.add(step24Controller);
        stepControllers.add(step25Controller);
        stepControllers.add(step26Controller);
        stepControllers.add(step27Controller);
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
