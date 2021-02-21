/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public abstract class PartController extends Controller {

    private final List<StepController> stepControllers = new ArrayList<>();
    private final int partNumber;

    protected PartController(Executor executor,
                             BannerModule bannerModule,
                             DateTimeModule dateTimeModule,
                             DataRepository dataRepository,
                             EngineSpeedModule engineSpeedModule,
                             VehicleInformationModule vehicleInformationModule,
                             DiagnosticMessageModule diagnosticMessageModule,
                             int partNumber,
                             StepController... stepControllers) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
        this.partNumber = partNumber;
        this.stepControllers.addAll(Arrays.asList(stepControllers));
    }

    @Override
    public String getDisplayName() {
        return "Part " + partNumber + " Test";
    }

    @Override
    protected void run() throws Throwable {
        List<StepController> stepControllers = getStepControllers();
        int totalSteps = stepControllers.stream()
                                        .mapToInt(StepController::getTotalSteps)
                                        .sum()
                + stepControllers.size();
        setupProgress(totalSteps);

        PartResult partResult = getPartResult();
        getListener().onResult("");
        getListener().beginPart(partResult);
        getListener().onResult("Start " + partResult);
        getListener().onResult("");

        for (StepController controller : getStepControllers()) {
            StepResult stepResult = getPartResult().getStepResult(controller.getStepNumber());

            getListener().beginStep(stepResult);
            getListener().onResult("Start " + stepResult);
            getListener().onResult("");

            incrementProgress(stepResult.toString());
            controller.run(getListener(), getJ1939());

            getListener().endStep(stepResult);
            getListener().onResult("");
            getListener().onResult("End " + stepResult);
            getListener().onResult("");
        }
        getListener().onResult("");
        getListener().onResult("End " + partResult);
        getListener().onResult("");
        getListener().endPart(partResult);
    }

    protected PartResult getPartResult() {
        return getPartResult(partNumber);
    }

    protected List<StepController> getStepControllers() {
        return stepControllers;
    }

}
