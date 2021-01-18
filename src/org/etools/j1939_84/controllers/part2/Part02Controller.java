/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

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
 * The {@link Controller} for the Part 2 Tests
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class Part02Controller extends PartController {

    private final List<StepController> stepControllers = new ArrayList<>();

    public Part02Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new VehicleInformationModule(),DateTimeModule.getInstance(), dataRepository);
    }

    private Part02Controller(Executor executor, EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule, VehicleInformationModule vehicleInformationModule,
                             DateTimeModule dateTimeModule, DataRepository dataRepository) {
        this(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule,
             new Part02Step01Controller(), new Part02Step02Controller(dataRepository));

    }
    /**
     * Constructor exposed for testing
     */
    public Part02Controller(Executor executor,
                            EngineSpeedModule engineSpeedModule,
                            BannerModule bannerModule,
                            VehicleInformationModule vehicleInformationModule,
                            DateTimeModule dateTimeModule,
                            Part02Step01Controller part02Step01Controller, Part02Step02Controller part02Step02Controller) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule, dateTimeModule);

        stepControllers.add(part02Step01Controller);
        stepControllers.add(part02Step02Controller);
    }

    @Override
    public String getDisplayName() {
        return "Part 2 Test";
    }

    @Override
    protected PartResult getPartResult() {
        return getPartResult(2);
    }

    @Override
    public List<StepController> getStepControllers() { return stepControllers;}
}
