/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.10 DM29: Regulated DTC Counts
 */
public class Part07Step10Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part07Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step10Controller(Executor executor,
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
        // 6.7.10.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.7.10.2.a. Fail if any ECU reports > 0 for pending
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.10.2.a - " + moduleName + " reported > 0 for pending");
               });

        // 6.7.10.2.a. Fail if any ECU reports > 0 for all pending
        packets.stream()
               .filter(p -> p.getAllPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.10.2.a - " + moduleName + " reported > 0 for all pending");
               });

        // 6.7.10.2.a. Fail if any ECU reports > 0 for MIL on
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.10.2.a - " + moduleName + " reported > 0 for MIL on");
               });

        // 6.7.10.2.a. Fail if any ECU reports > 0 for permanent
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.10.2.a - " + moduleName + " reported > 0 for permanent");
               });

        // 6.7.10.2.b. Fail if no ECU reports > 0 previous MIL on.
        boolean noPrev = packets.stream()
                                .map(DM29DtcCounts::getEmissionRelatedPreviouslyMILOnDTCCount)
                                .noneMatch(c -> c > 0);
        if (noPrev) {
            addFailure("6.7.10.2.b - No ECU reported > 0 previous MIL on");
        }

        // 6.7.10.2.c. Fail if any ECU reports a different number of previous MIL on DTCs than what that ECU reported in
        // DM23 earlier in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class,
                                                                                     p.getSourceAddress(),
                                                                                     7).size())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.7.10.2.c - " + moduleName
                           + " reported a different number of previous MIL on DTCs that what it reported in DM23 earlier in this part");
               });

        // 6.7.10.3.a. Warn if any ECU reports > 1 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.7.10.3.a - " + moduleName + " reported > 1 for previous MIL on");
               });

        // 6.7.10.3.b. Warn if more than one ECU reports > 0 for previous MIL on.
        long count = packets.stream()
                            .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
                            .count();
        if (count > 1) {
            addWarning("6.7.10.3.b - More than one ECU reported > 0 for previous MIL on");
        }
    }

}
