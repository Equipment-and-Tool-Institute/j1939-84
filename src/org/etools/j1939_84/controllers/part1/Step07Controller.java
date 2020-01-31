package org.etools.j1939_84.controllers.part1;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

public class Step07Controller extends Controller {

    Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule());
    }

    Step07Controller(ScheduledExecutorService executor, EngineSpeedModule engineSpeedModule,
            BannerModule bannerModule, DateTimeModule dateTimeModule,
            VehicleInformationModule vehicleInformationModule) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule);
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 7";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        getVehicleInformationModule().reportCalibrationInformation(getListener());

    }

}
