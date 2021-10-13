/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.soliddesign.j1939tools.j1939.Lookup;
import net.soliddesign.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import net.soliddesign.j1939tools.j1939.packets.DM29DtcCounts;
import net.soliddesign.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.4 DM29: Regulated DTC counts
 */
public class Part03Step04Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part03Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step04Controller(Executor executor,
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
        // 6.3.4.1.a Global DM29 (send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        packets.forEach(this::save);

        // 6.3.4.2.a Fail if any ECU reports > 0 for MIL on, previous MIL on, or permanent fault counts.
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.a - " + moduleName + " reported > 0 for MIL on count"));

        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.a - " + moduleName
                       + " reported > 0 for previous MIL on count"));

        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.a - " + moduleName
                       + " reported > 0 for permanent DTC count"));

        // 6.3.4.2.b Fail if no ECU reports > 0 emission-related pending (SPN 4104).
        boolean isNoPendingCounts = packets.stream().noneMatch(p -> p.getEmissionRelatedPendingDTCCount() > 0);
        if (isNoPendingCounts) {
            addFailure("6.3.4.2.b - No ECU reported > 0 emission-related pending count");
        }

        for (OBDModuleInformation obdModuleInformation : getDataRepository().getObdModules()) {
            int moduleAddress = obdModuleInformation.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);
            List<DM29DtcCounts> modulePackets = packets.stream()
                                                       .filter(p -> p.getSourceAddress() == moduleAddress)
                                                       .collect(Collectors.toList());

            // 6.3.4.2.c Fail if any ECU reports a different number of emission-related pending DTCs than
            // what that ECU reported in DM6 earlier in this part.
            boolean hasEmissionDTCDifference = modulePackets.stream()
                                                            .anyMatch(p -> {
                                                                return p.getEmissionRelatedPendingDTCCount() != getDM6DTCSize(p.getSourceAddress());
                                                            });
            if (hasEmissionDTCDifference) {
                addFailure("6.3.4.2.c - " + moduleName
                        + " reported a different number of emission-related pending DTCs " +
                        "than what it reported in the previous DM6");
            }

            var lastDM27 = get(DM27AllPendingDTCsPacket.class, moduleAddress, 3);
            if (lastDM27 != null) {
                // 6.3.4.2.d For OBD ECUs that support DM27, fail if any ECU reports a lower number of
                // all pending DTCs (SPN 4105) than the number of emission-related pending DTCs.
                boolean isLower = modulePackets.stream()
                                               .anyMatch(p -> p.getAllPendingDTCCount() < p.getEmissionRelatedPendingDTCCount());
                if (isLower) {
                    addFailure("6.3.4.2.d - " + moduleName + " reported a lower number of " +
                            "all pending DTCs than the number of emission-related pending DTCs");
                }

                // 6.3.4.2.e For OBD ECUs that support DM27, fail if any ECU reports a lower number of
                // all pending DTCs than what that ECU reported in DM27 earlier in this part.
                boolean hasDifference = modulePackets.stream()
                                                     .anyMatch(p -> p.getAllPendingDTCCount() < lastDM27.getDtcs()
                                                                                                        .size());
                if (hasDifference) {
                    addFailure("6.3.4.2.e - " + moduleName
                            + " reported a lower number of all pending DTCs than what it reported in DM27 earlier");
                }
            } else {
                // 6.3.4.2.f For OBD ECUs that do not support DM27, fail if any ECU does not report number of all
                // pending DTCs = 0xFF.
                boolean hasWrongValue = modulePackets.stream().anyMatch(p -> p.getAllPendingDTCCount() != 0xFF);
                if (hasWrongValue) {
                    addFailure("6.3.4.2.f - " + moduleName
                            + " does not support DM27 and did not report all pending DTCs = 0xFF");
                }
            }
        }

        // 6.3.4.2.g For non-OBD ECUs, fail if any ECU reports pending, MIL-on, previously MIL-on or permanent DTC count
        // greater than 0.
        packets.stream()
               .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.g - Non-OBD ECU " + moduleName
                       + " reported > 0 for pending DTC count"));

        packets.stream()
               .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.g - Non-OBD ECU " + moduleName
                       + " reported > 0 for MIL-on count"));

        packets.stream()
               .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.g - Non-OBD ECU " + moduleName
                       + " reported > 0 for previous MIL-on count"));

        packets.stream()
               .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addFailure("6.3.4.2.g - Non-OBD ECU " + moduleName
                       + " reported > 0 for permanent DTC count"));

        // 6.3.4.3.a Warn if any ECU reports > 1 for pending or all pending.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 1)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addWarning("6.3.4.3.a - " + moduleName + " reported > 1 for pending DTC count"));

        packets.stream()
               .filter(p -> p.getAllPendingDTCCount() > 1)
               .map(ParsedPacket::getSourceAddress)
               .map(Lookup::getAddressName)
               .forEach(moduleName -> addWarning("6.3.4.3.a - " + moduleName
                       + " reported > 1 for all pending DTC count"));

        // 6.3.4.3.b Warn if more than one ECU reports > 0 for pending or all pending.
        var modulesReportingPendingCount = packets.stream()
                                                  .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
                                                  .count();
        if (modulesReportingPendingCount > 1) {
            addWarning("6.3.4.3.b - More than one ECU reported > 0 for pending DTC count");
        }

        var modulesReportingAllPendingCount = packets.stream()
                                                     .filter(p -> p.getAllPendingDTCCount() > 0)
                                                     .count();
        if (modulesReportingAllPendingCount > 1) {
            addWarning("6.3.4.3.b - More than one ECU reported > 0 for all pending DTC count");
        }
    }

    private int getDM6DTCSize(int address) {
        return getDTCs(DM6PendingEmissionDTCPacket.class, address, 3).size();
    }

}
