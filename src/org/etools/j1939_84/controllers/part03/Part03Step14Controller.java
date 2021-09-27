/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.14 DM20: Monitor performance ratio
 */
public class Part03Step14Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part03Step14Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step14Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.3.14.1.a. DS DM20 (send Request (PGN 59904) for PGN 49664 (SPNs 3048)) to ECU(s) that responded in part 1
        // with DM20 data.
        // 6.3.14.1.b. Store ignition cycle counter value (SPN 3048).
        getDataRepository().getObdModuleAddresses()
                           .stream()
                           .sorted()
                           .map(address -> getCommunicationsModule().requestDM20(getListener(), address))
                           .map(BusResult::requestResult)
                           .map(RequestResult::getPackets)
                           .flatMap(Collection::stream)
                           .forEach(this::save);
    }
}
