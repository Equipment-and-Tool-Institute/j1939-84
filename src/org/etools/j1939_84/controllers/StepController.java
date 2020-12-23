/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.concurrent.Executor;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public abstract class StepController extends Controller {

    private final int partNumber;
    private final int stepNumber;
    private final int totalSteps;

    protected StepController(Executor executor,
                             EngineSpeedModule engineSpeedModule,
                             BannerModule bannerModule,
                             VehicleInformationModule vehicleInformationModule,
                             int partNumber,
                             int stepNumber,
                             int totalSteps) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule);
        this.partNumber = partNumber;
        this.stepNumber = stepNumber;
        this.totalSteps = totalSteps;
    }

    protected void addFailure(String message) {
        addFailure(getPartNumber(), getStepNumber(), message);
    }

    protected void addWarning(String message) {
        addWarning(getPartNumber(), getStepNumber(), message);
    }

    @Override
    public String getDisplayName() {
        return "Part " + getPartNumber() + " Step " + getStepNumber();
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    @Override
    public int getTotalSteps() {
        return totalSteps;
    }
}
