/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.4 DM12: Emissions Related Active DTCs
 */
public class Part07Step04Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part07Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step04Controller(Executor executor,
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
        // 6.7.4.1.a DS DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]) to each OBD ECU
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM12(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        packets.forEach(this::save);

        // 6.7.4.2.a Fail if any OBD ECU reports an active DTC.
        packets.stream()
               .filter(p -> !p.getDtcs().isEmpty())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.4.2.a - " + moduleName + " reported an active DTC");
               });

        // 6.7.4.2.b Fail if any OBD ECU does not report MIL off.
        packets.stream()
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.7.4.2.b - " + moduleName + " did not report MIL off."));

        // 6.7.4.2.c Fail if no OBD ECU supports DM12.
        if (packets.isEmpty()) {
            addFailure("6.7.4.2.c - No OBD ECU supports DM12");
        }

        // 6.7.4.1.d Fail if NACK not received from OBD ECUs that did not provide a DM12 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.7.4.1.d");
    }

}
