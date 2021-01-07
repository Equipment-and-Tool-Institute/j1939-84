/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.List;
import java.util.concurrent.Executors;
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
public class Part04Controller extends PartController {

    public Part04Controller() {
        super(Executors.newSingleThreadScheduledExecutor(),
                new EngineSpeedModule(),
                new BannerModule(),
                new VehicleInformationModule(),
                DateTimeModule.getInstance());
    }

    @Override
    public String getDisplayName() {
        return "Part 4 Test";
    }

    @Override
    protected PartResult getPartResult() {
        return getPartResult(4);
    }

    @Override
    protected List<StepController> getStepControllers() {
        return getStepControllers(4, 15);
    }

}
