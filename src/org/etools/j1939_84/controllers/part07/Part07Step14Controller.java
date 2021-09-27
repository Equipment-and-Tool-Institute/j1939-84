/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.7.14 DM21: Diagnostic Readiness 2
 */
public class Part07Step14Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part07Step14Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step14Controller(Executor executor,
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
        // 6.7.14.1.a. DS DM21 ([send Request (PGN 59904) for PGN 49408 (SPN 3295)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM21(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.7.14.2.a. Fail if no ECU reports time with MIL on (SPN 3295) greater than or equal to 1 minute.
        long milOn1MinCount = packets.stream()
                                     .filter(p -> p.getMinutesWhileMILIsActivated() >= 1)
                                     .map(ParsedPacket::getModuleName)
                                     .count();
        if (milOn1MinCount == 0) {
            addFailure("6.7.14.2.a - No ECU reported time with MIL on greater than or equal to 1 minute");
        }

        // 6.7.14.2.b. Fail if NACK not received from OBD ECUs that did not provide DM21 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.7.14.2.b");
    }

}
