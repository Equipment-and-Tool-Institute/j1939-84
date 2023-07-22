/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.5 DM29: Regulated DTC Counts
 */
public class Part12Step05Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part12Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step05Controller(Executor executor,
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
        // 6.12.5.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        // 6.12.5.2.a. Fail if any ECU reports > 0 for emission-related pending
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.a - " + moduleName + " reported > 0 for emission-related pending");
               });

        // 6.12.5.2.a. Fail if any ECU reports > 0 for MIL-on
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.a - " + moduleName + " reported > 0 for MIL-on");
               });

        // 6.12.5.2.a. Fail if any ECU reports > 0 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.a - " + moduleName + " reported > 0 for previous MIL on");
               });

        // 6.12.5.2.a. Fail if any ECU reports > 0 for permanent.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.a - " + moduleName + " reported > 0 for permanent");
               });

        // 6.12.5.2.b. For OBD ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .filter(p -> p.getAllPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.b - " + moduleName + " reported > 0 for all pending DTCs");
               });

        // 6.12.5.2.c. For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs
        // = 0xFF.
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> !supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.12.5.2.c - " + moduleName + " did not report all pending DTCs = 0xFF");
               });

        // 6.12.5.2.d. Fail if no OBD ECU provides a DM29 message.
        boolean noOBDResponse = packets.stream().noneMatch(p -> isObdModule(p.getSourceAddress()));
        if (noOBDResponse) {
            addFailure("6.12.5.2.d - No OBD ECU provided a DM29 message");
        }

    }

}
