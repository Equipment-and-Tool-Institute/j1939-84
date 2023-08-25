/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.11.10 DM5: Diagnostic Readiness 1
 */
public class Part11Step10Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part11Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part11Step10Controller(Executor executor,
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
        // 6.11.10.1.a. DS DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1223)]) to each OBD ECU.
        var packets = filterPackets(getDataRepository().getObdModuleAddresses()
                                                       .stream()
                                                       .map(a -> getCommunicationsModule().requestDM5(getListener(),
                                                                                                      a))
                                                       .collect(Collectors.toList()));

        // 6.11.10.1.b. Record all data (i.e., which monitors are supported and complete or supported and incomplete).
        packets.forEach(this::save);

        // 6.11.10.1.c. Display monitor readiness composite value in log
        if (packets.size() > 1) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM5:");
            getCompositeSystems(packets, true).stream()
                                              .map(MonitoredSystem::toString)
                                              .forEach(string -> getListener().onResult(string));
        }

    }

}
