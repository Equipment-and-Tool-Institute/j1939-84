/**
 * private final IndexGenerator indexGenerator = new IndexGenerator(new
 * DateTimeModule()); Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.PartResult;
import org.etools.j1939_84.model.PartResultFactory;
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

    private final List<StepController> stepControllers = new ArrayList<>();

    /**
     * Constructor
     */
    public Part01Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new PartResultFactory(), new DataRepository());
    }

    private Part01Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        this(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory,
                new Step01Controller(dataRepository), new Step02Controller(), new Step03Controller(dataRepository),
                new Step04Controller(dataRepository), new Step05Controller(dataRepository),
                new Step06Controller(dataRepository), new Step07Controller(dataRepository),
                new Step08Controller(dataRepository), new Step09Controller(dataRepository),
                new Step10Controller(dataRepository), new Step11Controller(dataRepository),
                new Step12Controller(dataRepository), new Step13Controller(dataRepository),
                new Step14Controller(dataRepository), new Step16Controller(dataRepository));
    }

    /**
     * Constructor exposed for testing
     *
     * @param executor
     *            the {@link Executor}
     * @param engineSpeedModule
     *            the {@link EngineSpeedModule}
     * @param bannerModule
     *            the {@link BannerModule}
     * @param dateTimeModule
     *            the {@link DateTimeModule}
     * @param vehicleInformationModule
     *            the {@link VehicleInformationModule}
     * @param partResultFactory
     *            the {@link PartResultFactory}
     * @param step01Controller
     *            the {@link Step01Controller} for Part1Controller
     * @param step02Controller
     *            the {@link Step02Controller} for Part1Controller
     * @param step03Controller
     *            the {@link Step03Controller} for Part1Controller
     * @param step04Controller
     *            the {@link Step04Controller} for Part1Controller
     * @param step05Controller
     *            the {@link Step05Controller} for Part1Controller
     * @param step06Controller
     *            the {@link Step06Controller} for Part1Controller
     * @param step07Controller
     *            the {@link Step07Controller} for Part1Controller
     * @param step08Controller
     *            the {@link Step08Controller} for Part1Controller
     * @param step09Controller
     *            the {@link Step09Controller} for Part1Controller
     * @param step10Controller
     *            the {@link Step10Controller} for Part1Controller
     * @param step11Controller
     *            the {@link Step11Controller} for Part1Controller
     * @param step12Controller
     *            the {@link Step12Controller} for Part1Controller
     * @param step13Controller
     *            the {@link step13Controller} for Part1Controller
     * @param step14Controller
     *            the {@link step14Controller} for Part1Controller
     * @param step16Controller
     *            the {@link step16Controller} for Part1Controller
     */
    public Part01Controller(Executor executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            PartResultFactory partResultFactory, Step01Controller step01Controller, Step02Controller step02Controller,
            Step03Controller step03Controller, Step04Controller step04Controller, Step05Controller step05Controller,
            Step06Controller step06Controller, Step07Controller step07Controller, Step08Controller step08Controller,
            Step09Controller step09Controller, Step10Controller step10Controller, Step11Controller step11Controller,
            Step12Controller step12Controller, Step13Controller step13Controller, Step14Controller step14Controller,
            Step16Controller step16Controller) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);

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
        stepControllers.add(step16Controller);
    }

    private void executeStep(StepController controller) throws InterruptedException {
        StepResult stepResult = getPartResult(1).getStepResult(controller.getStepNumber());

        getListener().beginStep(stepResult);
        getListener().onResult(NL);
        getListener().onResult("Start " + stepResult);

        incrementProgress(stepResult.toString());
        controller.run(getListener(), getJ1939());

        getListener().endStep(stepResult);
        getListener().onResult("End " + stepResult);
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

        for (StepController controller : stepControllers) {
            executeStep(controller);
        }

        getListener().onResult("End " + partResult);
        getListener().endPart(partResult);
    }

}
