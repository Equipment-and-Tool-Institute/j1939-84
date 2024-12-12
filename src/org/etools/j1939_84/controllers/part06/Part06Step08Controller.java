/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.6.8 DM29: Regulated DTC Counts
 */
public class Part06Step08Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part06Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step08Controller(Executor executor,
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
        // 6.6.8.1.a DS DM29 ([send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM29(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.6.8.2.a. Fail if any ECU reports > 0 for emission-related pending
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.8.2.a - " + moduleName
                       + " reports > 0 for emission-related pending"));

        // 6.6.8.2.a. Fail if any ECU reports > 0 for previous MIL on
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.8.2.a - " + moduleName
                       + " reports > 0 for previous MIL on"));

        // 6.6.8.2.b. Fail if no ECU reports > 0 for MIL on.
        boolean noMIL = packets.stream().map(DM29DtcCounts::getEmissionRelatedMILOnDTCCount).noneMatch(mil -> mil > 0);
        if (noMIL) {
            addFailure("6.6.8.2.b - No ECU reported > 0 for MIL on");
        }

        // 6.6.8.2.c. Fail if any OBD System reports a different sum MIL on counts than what that OBD System reported in DM12 (from 6.5.2).
        int sumMil = packets.stream()
                .mapToInt(p -> p.getEmissionRelatedMILOnDTCCount())
                .sum();
        int dm12Count = getDataRepository().getObdModuleAddresses().stream()
                .mapToInt(addr -> getDM12DTCs(addr).size()).sum();
        if (sumMil != dm12Count){
            addFailure("6.6.8.2.c - OBD System reported a different sum of MIL on counts than what it reported in DM12");
        }

        // 6.6.8.2.d. Fail if no ECU reports > 0 for permanent DTC counts.
        boolean noPerm = packets.stream()
                                .map(DM29DtcCounts::getEmissionRelatedPermanentDTCCount)
                                .noneMatch(perm -> perm > 0);
        if (noPerm) {
            addFailure("6.6.8.2.d - No ECU reported > 0 for permanent DTC counts");
        }

        // 6.6.8.2.e. Fail if OBD System reports a different sum of permanent DTC counts in DM29 than the sum of
        // the DTCs counted in OBD system’s DM28 responses, where the sum of the DM29 permanent DTC counts
        // is 4 or less.
        int permanentDTCCount = packets.stream()
                .mapToInt(p -> p.getEmissionRelatedPermanentDTCCount())
                .sum();
        int dm28Count = getDataRepository().getObdModuleAddresses().stream()
                .mapToInt(addr -> getDM28DTCs(addr).size()).sum();
        if (permanentDTCCount <= 4 && permanentDTCCount != dm28Count){
                addFailure("6.6.8.2.e - OBD System reported a different sum of permanent DTC counts than what it reported in DM28 responses");
        }

        // 6.6.8.2.f. For ECUs that support DM27, fail if any ECU reports an all pending DTC (DM27) (SPN 4105) count
        // that is less than its pending DTC (DM6) count.
        packets.stream()
               .filter(p -> supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .filter(p -> p.getAllPendingDTCCount() < getDM6DTCs(p.getSourceAddress()).size())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.6.8.2.f - " + moduleName
                           + " reported an for all pending DTC count that is less than its pending DTC (DM6) count");
               });

        // 6.6.8.2.g. For ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs =
        // 0xFF.
        packets.stream()
               .filter(p -> !supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.6.8.2.g - " + moduleName
                           + " did not report number of all pending DTCs = 0xFF");
               });

        // 6.6.8.2.h. Fail if NACK not received from OBD ECUs that did not provide a DM29 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.6.8.2.h");

        // 6.6.8.3.a. Info if any ECU reports > 1 for MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.6.8.3.a - " + moduleName + " reported > 1 for MIL on");
               });

        // 6.6.8.3.b. Info if more than one ECU reports > 0 for MIL on.
        long milOnCount = packets.stream()
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
                                 .count();
        if (milOnCount > 1) {
            addInfo("6.6.8.3.b - More than one ECU reported > 0 for MIL on");
        }

        // 6.6.8.3.c. Info if any ECU reports > 1 for permanent.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.6.8.3.c - " + moduleName + " reported > 1 for permanent");
               });

        // 6.6.8.3.d. Info if more than one ECU reports > 0 for permanent
        long permCount = packets.stream()
                                .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
                                .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
                                .count();
        if (permCount > 1) {
            addInfo("6.6.8.3.d - More than one ECU reported > 0 for permanent");
        }
    }

    private List<DiagnosticTroubleCode> getDM12DTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 6);
    }

    private List<DiagnosticTroubleCode> getDM28DTCs(int moduleAddress) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, moduleAddress, 6);
    }

    private List<DiagnosticTroubleCode> getDM6DTCs(int moduleAddress) {
        return getDTCs(DM6PendingEmissionDTCPacket.class, moduleAddress, 6);
    }

}
