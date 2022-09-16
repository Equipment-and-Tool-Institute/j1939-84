/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.5 DM1: Active Diagnostic Trouble Codes (DTCs) Actions
 */
public class Part07Step05Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part07Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step05Controller(Executor executor,
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
        // 6.7.5.1.a Receive broadcast data [(PGN 65226 (SPNs 1213-1215, 1706, and 3038)]).
        var packets = read(DM1ActiveDTCsPacket.class,
                           3,
                           SECONDS).stream()
                                   .map(p -> new DM1ActiveDTCsPacket(p.getPacket()))
                                   .filter(p -> isObdModule(p.getSourceAddress()))
                                   .collect(
                                            Collectors.toList());

        // 6.7.5.2.a Fail if any OBD ECU reports an active DTC.
        packets.stream()
               .filter(p -> !p.getDtcs().isEmpty())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.7.5.2.a - " + moduleName + " reported an active DTC"));

        // 6.7.5.2.b Fail if any OBD ECU does not report MIL off.
        packets.stream()
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.7.5.2.b - " + moduleName + " did not report MIL off"));

        // 6.7.5.2.c Fail if no OBD ECU provides DM1.
        if (packets.isEmpty()) {
            addFailure("6.7.5.2.c - No OBD ECU provided a DM1");
        }
    }

}
