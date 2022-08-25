/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.8.8 DM29: Regulated DTC Counts
 */
public class Part08Step08Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part08Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step08Controller(Executor executor,
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
        // 6.8.8.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.8.8.2.a. Fail if any ECU reports > 0 for emission-related pending.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.8.2.a - " + moduleName + " reported > 0 for emissions-related pending");
               });

        // 6.8.8.2.b. Fail if no ECU reports > 0 for MIL on.
        boolean isMilOn = packets.stream().anyMatch(p -> p.getEmissionRelatedMILOnDTCCount() > 0);
        if (!isMilOn) {
            addFailure("6.8.8.2.b - No ECU reported > 0 for MIL on");
        }

        // 6.8.8.2.c. Fail if any ECU reports a different number for MIL on than what that ECU reported in DM12 earlier
        // in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != getDM12Count(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.8.2.c - " + moduleName
                           + " reported a different number for MIL on than what it reported in DM12 earlier in this part");
               });

        // 6.8.8.2.d. Fail if no ECU reports > 0 for previous MIL on.
        boolean isPrevMilOn = packets.stream().anyMatch(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0);
        if (!isPrevMilOn) {
            addFailure("6.8.8.2.d - No ECU reported > 0 for previous MIL on");
        }

        // 6.8.8.2.e. Fail if any ECU reports a different number for previous MIL on than what that ECU reported in DM23
        // earlier in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != getDM23Count(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.8.2.e - " + moduleName
                           + " reported a different number for previous MIL on than what it reported in DM23 earlier in this part");
               });

        // 6.8.8.2.f. Fail if no ECU reports > 0 for permanent.
        boolean isPermanent = packets.stream().anyMatch(p -> p.getEmissionRelatedPermanentDTCCount() > 0);
        if (!isPermanent) {
            addFailure("6.8.8.2.f - No ECU reported > 0 for permanent");
        }

        // 6.8.8.2.g. Fail if any ECU reports a different number for permanent than what that ECU reported in DM28
        // earlier in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != getDM28Count(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.8.2.g - " + moduleName
                           + " reported a different number for permanent than what it reported in DM28 earlier in this part");
               });

        // 6.8.8.2.h. For ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        getDataRepository().getObdModules()
                           .stream()
                           .filter(OBDModuleInformation::supportsDM27)
                           .map(OBDModuleInformation::getSourceAddress)
                           .flatMap(a -> packets.stream().filter(p -> p.getSourceAddress() == a))
                           .filter(p -> p.getAllPendingDTCCount() > 0)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.8.8.2.h - " + moduleName + " reported > 0 for all pending DTCs");
                           });

        // 6.8.8.2.i. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs =
        // 0xFF.
        getDataRepository().getObdModules()
                           .stream()
                           .filter(m -> !m.supportsDM27())
                           .map(OBDModuleInformation::getSourceAddress)
                           .flatMap(a -> packets.stream().filter(p -> p.getSourceAddress() == a))
                           .filter(p -> p.getAllPendingDTCCount() != 0xFF)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.8.8.2.i - " + moduleName
                                       + " did not report number of all pending DTCs = 0xFF");
                           });

        // 6.8.8.3.a. Warn if any ECU reports > 1 for MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.8.8.3.a - " + moduleName + " reported > 1 for MIL on");
               });

        // 6.8.8.3.b. Warn if more than one ECU reports > 0 for MIL on.
        long milOnCount = packets.stream()
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
                                 .count();
        if (milOnCount > 1) {
            addWarning("6.8.8.3.b - More than one ECU reported > 0 for MIL on");
        }

        // 6.8.8.3.c. Warn if any ECU reports > 1 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.8.8.3.c - " + moduleName + " reported > 1 for previous MIL on");
               });

        // 6.8.8.3.d. Warn if more than one ECU reports > 0 for previous MIL on.
        long prevMilOnCount = packets.stream()
                                     .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
                                     .count();
        if (prevMilOnCount > 1) {
            addWarning("6.8.8.3.d - More than one ECU reported > 0 for previous MIL on");
        }

        // 6.8.8.3.e. Warn if any ECU report > 1 for permanent.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.8.8.3.e - " + moduleName + " reported > 1 for permanent");
               });

        // 6.8.8.3.f. Warn if more than one ECU reports > 0 for permanent.
        long permanentCount = packets.stream()
                                     .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
                                     .count();
        if (permanentCount > 1) {
            addWarning("6.8.8.3.f - More than one ECU reported > 0 for permanent");
        }
    }

    private int getDM12Count(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 8).size();
    }

    private int getDM23Count(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 8).size();
    }

    private int getDM28Count(int address) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, address, 8).size();
    }
}
