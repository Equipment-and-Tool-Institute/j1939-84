/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.etools.j1939_84.controllers.part1.Part01Controller;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * The Controller that manages the other Controllers each of which is
 * responsible for one part only.
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class OverallController extends Controller {
    private Controller activeController;

    /**
     * The other Controllers in the order they will be executed.
     */

    private final List<Controller> controllers = new ArrayList<>();

    public OverallController() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new Part01Controller(), new Part02Controller(),
                new Part03Controller(), new Part04Controller(), new Part05Controller(), new Part06Controller(),
                new Part07Controller(), new Part08Controller(), new Part09Controller(), new Part10Controller(),
                new Part11Controller(), new Part12Controller());
    }

    /**
     * Constructor expose for testing
     *
     * @param executor                 {@link ScheduledExecutorService}
     * @param engineSpeedModule        the {@link EngineSpeedModule} used to request
     *                                 engine speed
     * @param bannerModule             the {@link BannerModule} used to display
     *                                 headers and footers on the report
     * @param dateTimeModule           the {@link DateTimeModule} used to determine
     *                                 the date and time
     * @param vehicleInformationModule the {@link VehicleInformationModule} used to
     *                                 gather information about the vehicle
     * @param part1Controller          the {@link Controller} for Part1
     */
    public OverallController(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule,
            Part01Controller part1Controller, Part02Controller part2Controller, Part03Controller part3Controller,
            Part04Controller part4Controller, Part05Controller part5Controller, Part06Controller part6Controller,
            Part07Controller part7Controller, Part08Controller part8Controller, Part09Controller part9Controller,
            Part10Controller part10Controller, Part11Controller part11Controller, Part12Controller part12Controller) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);
        controllers.add(part1Controller);
        controllers.add(part2Controller);
        controllers.add(part3Controller);
        controllers.add(part4Controller);
        controllers.add(part5Controller);
        controllers.add(part6Controller);
        controllers.add(part7Controller);
        controllers.add(part8Controller);
        controllers.add(part9Controller);
        controllers.add(part10Controller);
        controllers.add(part11Controller);
        controllers.add(part12Controller);
    }

    @Override
    public String getDisplayName() {
        return "Overall Controller";
    }

    @Override
    protected int getTotalSteps() {
        return controllers.size();
    }

    @Override
    protected void run() throws Throwable {
        try {
            getBannerModule().reportHeader(getListener());

            for (int i = 0; i < controllers.size(); i++) {
                activeController = controllers.get(i);
                getListener().onProgress(i, getTotalSteps(), activeController.getDisplayName());
                activeController.run(getListener(), getJ1939());
                activeController = null;
                if (getEnding() != null) {
                    break;
                }
            }
            setEnding(Ending.COMPLETED);
        } finally {
            finished();
        }
    }

    @Override
    public void stop() {
        if (activeController != null) {
            activeController.stop();
        }
        super.stop();
    }

}
