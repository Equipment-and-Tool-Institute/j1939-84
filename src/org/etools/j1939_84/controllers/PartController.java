/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public abstract class PartController extends Controller {

    protected PartController(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, VehicleInformationModule vehicleInformationModule) {
        super(executor, engineSpeedModule, bannerModule, vehicleInformationModule);
    }

    @Override
    protected void run() throws Throwable {
        List<StepController> stepControllers = getStepControllers();
        int totalSteps = stepControllers.stream()
                .mapToInt(StepController::getTotalSteps)
                .sum() + stepControllers.size();
        setupProgress(totalSteps);

        PartResult partResult = getPartResult();
        getListener().beginPart(partResult);
        getListener().onResult("Start " + partResult);

        for (StepController controller : getStepControllers()) {
            StepResult stepResult = getPartResult().getStepResult(controller.getStepNumber());

            getListener().beginStep(stepResult);
            getListener().onResult(NL);
            getListener().onResult("Start " + stepResult);

            incrementProgress(stepResult.toString());
            controller.run(getListener(), getJ1939());

            getListener().endStep(stepResult);
            getListener().onResult("End " + stepResult);
        }
        getListener().onResult("End " + partResult);
        getListener().onResult(NL);
        getListener().endPart(partResult);
    }

    protected abstract PartResult getPartResult();

    protected abstract List<StepController> getStepControllers();

    /**
     * TODO Remove this
     * This is a temporary method until the project is finished
     */
    protected List<StepController> getStepControllers(int partNumber, int steps) {
        List<StepController> stepControllers = new ArrayList<>();
        for (int i = 1; i <= steps; i++) {
            stepControllers.add(new StepController(Executors.newSingleThreadExecutor(),
                    new EngineSpeedModule(),
                    new BannerModule(),
                    new VehicleInformationModule(),
                    partNumber,
                    i,
                    1) {
                @Override
                protected void run() {
                    getListener().onResult("Do Testing;\nWait for Responses;\nWrite Messages, etc");
                }
            });
        }
        return stepControllers;
    }

}
