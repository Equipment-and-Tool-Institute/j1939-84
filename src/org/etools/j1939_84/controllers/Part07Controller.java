/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.List;
import java.util.concurrent.Executors;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 7 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Part07Controller extends PartController {

    public Part07Controller() {
        super(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule());
    }

    @Override
    public String getDisplayName() {
        return "Part 7 Test";
    }

    @Override
    protected PartResult getPartResult() {
        return getPartResult(7);
    }

    @Override
    protected List<StepController> getStepControllers() {
        return getStepControllers(7, 17);
    }
}
