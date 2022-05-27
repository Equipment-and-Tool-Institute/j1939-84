/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.9.23 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part09Step23Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 23;
    private static final int TOTAL_STEPS = 0;

    Part09Step23Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step23Controller(Executor executor,
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
        // 6.9.23.1.a. Receive DM1 broadcast [(PGN 65226 (SPNs 1213-1215, 1706, and 3038)]).
        List<DM1ActiveDTCsPacket> packets = read(DM1ActiveDTCsPacket.class,
                                                 3,
                                                 SECONDS).stream()
                                                         .map(p -> new DM1ActiveDTCsPacket(p.getPacket()))
                                                         .peek(this::save)
                                                         .peek(p -> {
                                                             // 6.9.23.2.a. Fail if any ECU does not
                                                             // report MIL off or MIL not supported.
                                                             if (p.getMalfunctionIndicatorLampStatus() != OFF
                                                                     &&
                                                                     p.getMalfunctionIndicatorLampStatus() != NOT_SUPPORTED) {
                                                                 addFailure(format("6.9.23.2.a - ECU %s reported MIL status of %s",
                                                                                   p.getModuleName(),
                                                                                   p.getMalfunctionIndicatorLampStatus()));
                                                             }
                                                             // 6.9.23.2.b. Fail if any ECU reports an
                                                             // active DTC.
                                                             if (p.hasDTCs()) {
                                                                 addFailure(format("6.9.23.2.b - ECU %s reported a active DTC",
                                                                                   p.getModuleName()));
                                                             }
                                                         })
                                                         .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                                         .collect(Collectors.toList());

        // 6.9.23.2.c. Fail if no OBD ECU provides DM1.
        if (packets.isEmpty()) {
            addFailure("6.9.23.2.c - No OBD ECU provided a DM1");
        }
    }

}
