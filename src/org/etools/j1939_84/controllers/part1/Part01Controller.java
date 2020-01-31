/**
 * private final IndexGenerator indexGenerator = new IndexGenerator(new
 * DateTimeModule());
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.StepResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The {@link Controller} for the Part 1 Tests
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class Part01Controller extends Controller {

    private final List<Controller> stepControllers = new ArrayList<>();

    /**
     * Constructor
     */
    public Part01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DataRepository());
    }

    private Part01Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            DataRepository dataRepository) {
        this(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule,
                new Step01Controller(dataRepository), new Step02Controller(), new Step03Controller(dataRepository),
                new Step04Controller(dataRepository), new Step05Controller(dataRepository),
                new Step06Controller(dataRepository), new Step07Controller());
    }

    /** Constructor exposed for testing */
    public Part01Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            Step01Controller step01Controller,
            Step02Controller step02Controller, Step03Controller step03Controller, Step04Controller step04Controller,
            Step05Controller step05Controller, Step06Controller step06Controller, Step07Controller step07Controller) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);

        stepControllers.add(step01Controller);
        stepControllers.add(step02Controller);
        stepControllers.add(step03Controller);
        stepControllers.add(step04Controller);
        stepControllers.add(step05Controller);
        stepControllers.add(step06Controller);
        stepControllers.add(step07Controller);
    }

    private void executeStep(int stepNumber) throws InterruptedException {
        StepResult stepResult = getPartResult(1).getStepResult(stepNumber);

        getListener().beginStep(stepResult);
        getListener().onResult(NL);
        getListener().onResult("Start " + stepResult);

        incrementProgress(stepResult.toString());
        executeStepTest(stepNumber);

        getListener().endStep(stepResult);
        getListener().onResult("End " + stepResult);
    }

    private void executeStepTest(int stepNumber) throws InterruptedException {
        if (stepNumber < stepControllers.size()) {
            stepControllers.get(stepNumber - 1).run(getListener(), getJ1939());
        }
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Test";
    }

    @Override
    protected int getTotalSteps() {
        return 28;
    }

    @Override
    protected void run() throws Throwable {
        PartResult partResult = getPartResult(1);
        getListener().beginPart(partResult);
        getListener().onResult("Start " + partResult);

        for (int i = 1; i < 27; i++) {
            executeStep(i);
        }

        getListener().onResult("End " + partResult);
        getListener().endPart(partResult);
    }

}
