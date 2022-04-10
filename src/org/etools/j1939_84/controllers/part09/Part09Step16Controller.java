/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.16 DM26: Diagnostic Readiness 3
 */
public class Part09Step16Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part09Step16Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step16Controller(Executor executor,
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
        // 6.9.16.1.a. DS DM26 [(send Request (PGN 59904) for PGN 64952 (SPN 3302)]) to each OBD ECU.
        var results = getDataRepository().getObdModuleAddresses()
                                         .stream()
                                         .map(a -> getCommunicationsModule().requestDM26(getListener(), a))
                                         .collect(Collectors.toList());

        var packets = filterRequestResultPackets(results);

        // 6.9.16.2.a. Fail if any ECU that was reporting a non-zero value of number of WU-SCC (SPN 3302) in test
        // 6.8.16.1.a is still reporting > 0.
        packets.stream()
               .filter(p -> getWarmUpsSCC(p.getSourceAddress()) > 0)
               .filter(p -> p.getWarmUpsSinceClear() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.16.2.a - " + moduleName
                           + " reported a non-zero value of number of WU-SCC in test 6.8.16.1.a and is still reporting > 0");
               });

        // 6.9.16.2.b. Fail if NACK not received from OBD ECUs that did not provide a DM26 message.
        checkForNACKsDS(packets, filterRequestResultAcks(results), "6.9.16.2.b");
    }

    private int getWarmUpsSCC(int address) {
        var dm26 = get(DM26TripDiagnosticReadinessPacket.class, address, 8);
        return dm26 == null ? 0 : dm26.getWarmUpsSinceClear();
    }
}
