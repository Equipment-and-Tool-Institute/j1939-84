/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.11.3 DM21: Diagnostic Readiness 2
 */
public class Part11Step03Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part11Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part11Step03Controller(Executor executor,
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
        // 6.11.3.1.a. DS DM21 ([send Request (PGN 59904) for PGN 49408 (SPNs 3294, 3296)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM21(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // 6.11.3.1.b. Record distance (SPN 3294) and time (SPN 3296) SCC (if supported) to compare later in test
        // 6.11.12.
        packets.forEach(this::save);

        // 6.11.3.2.a. Fail if NACK not received from OBD ECUs that did not provide a DM21 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.11.3.2.a");
    }

}
