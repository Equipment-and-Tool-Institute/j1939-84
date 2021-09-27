/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.11.4 DM29: Regulated DTC Counts
 */
public class Part11Step04Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part11Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part11Step04Controller(Executor executor,
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
        // 6.11.4.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.11.4.2.a. Fail if any ECU reports > 0 for emission-related pending
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.4.2.a - " + moduleName + " reported > 0 for emission-related pending");
               });

        // 6.11.4.2.a. Fail if any ECU reports > 0 for MIL-on
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.4.2.a - " + moduleName + " reported > 0 for MIL-on");
               });

        // 6.11.4.2.a. Fail if any ECU reports > 0 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.4.2.a - " + moduleName + " reported > 0 for previous MIL on");
               });

        // 6.11.4.2.b. Fail if no ECU reports > 0 for permanent DTC.
        boolean noPermanent = packets.stream().noneMatch(p -> p.getEmissionRelatedPermanentDTCCount() > 0);
        if (noPermanent) {
            addFailure("6.11.4.2.b - No ECU reported > 0 for permanent DTC");
        }

        // 6.11.4.2.c. For ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.4.2.c - " + moduleName + " reported > 0 for all pending DTCs");
               });

        // 6.11.4.2.d. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs =
        // 0xFF.
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> !supportsDM27(p.getSourceAddress()))
               .filter(p -> (byte) p.getAllPendingDTCCount() != (byte) 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.4.2.d - " + moduleName + " did not report all pending DTCs = 0xFF");
               });

        // 6.11.4.3.a. Warn if any ECU reports > 1 for permanent DTC.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.11.4.3.a - " + moduleName + " reported > 1 for permanent DTC");
               });

        // 6.11.4.3.b. Warn if more than one ECU reports > 0 for permanent DTC.
        long count = packets.stream().filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0).count();
        if (count > 1) {
            addWarning("6.11.4.3.b - More than one ECU reported > 0 for permanent DTC");
        }

    }

}
