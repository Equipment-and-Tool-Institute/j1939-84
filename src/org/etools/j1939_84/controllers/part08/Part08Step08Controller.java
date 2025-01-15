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
               .filter(p -> p.getEmissionRelatedPendingDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.8.2.a - " + moduleName + " reported > 0 for emissions-related pending");
               });

        // 6.8.8.2.b. Fail if no ECU reports > 0 for MIL on.
        boolean isMilOn = packets.stream()
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
                                 .anyMatch(p -> p.getEmissionRelatedMILOnDTCCount() > 0);
        if (!isMilOn) {
            addFailure("6.8.8.2.b - No ECU reported > 0 for MIL on");
        }

        // 6.8.8.2.c. Fail if any OBD system reports a sum of MIL on DTCs counts that is less than the total
        // number of implanted faults for Fault B.
        int implantedFaultsB = getDataRepository().getVehicleInformation().getNumberOfFaultBImplants();
        int MILOnCount = packets.stream()
                .filter(p -> isObdModule(p.getSourceAddress()))
                .mapToInt(p -> p.getEmissionRelatedMILOnDTCCount()).sum();
        if (MILOnCount < implantedFaultsB){
            addFailure("6.8.8.2.c - OBD System reported a sum of MIL on DTCs counts that is less than the total number of implanted faults for Fault B");
        }

        // 6.8.8.2.d. Fail if no ECU reports > 0 for previous MIL on.
        boolean isPrevMilOn = packets.stream()               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
.anyMatch(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0);
        if (!isPrevMilOn) {
            addFailure("6.8.8.2.d - No ECU reported > 0 for previous MIL on");
        }

        // 6.8.8.2.e Fail if any OBD system reports a sum of previous MIL on DTCs counts that is less than the total
        // number of implanted faults for Fault B.
        int prevMILOnCount = packets.stream()
                .filter(p -> isObdModule(p.getSourceAddress()))
                .mapToInt(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount()).sum();
        if (prevMILOnCount < implantedFaultsB){
            addFailure("6.8.8.2.e - OBD System reported a sum of previous MIL on DTCs counts that is less than the total number of implanted faults for Fault B");
        }

        // 6.8.8.2.f. Fail if no ECU reports > 0 for permanent.
        boolean isPermanent = packets.stream()
                                     .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
                                     .anyMatch(p -> p.getEmissionRelatedPermanentDTCCount() > 0);
        if (!isPermanent) {
            addFailure("6.8.8.2.f - No ECU reported > 0 for permanent");
        }

        // 6.8.8.2.g. Fail if any OBD system reports a sum of permanent DTC counts that is less than the
        // sum of the permanent DTCs counted the DM28 responses, where the total number of faults
        // implanted for Fault B is less than 4.
        if (implantedFaultsB < 4){
            int permanentCount = packets.stream()
                    .filter(p -> isObdModule(p.getSourceAddress()))
                    .mapToInt(p -> p.getEmissionRelatedPermanentDTCCount()).sum();
            int dm28Count = getDataRepository().getObdModuleAddresses().stream()
                    .mapToInt(addr -> getDM28Count(addr)).sum();
            if (permanentCount < dm28Count){
                addFailure("6.8.8.2.g - OBD system reported a sum of permanent DTC counts less than the sum of the permanent DTCs counted the DM28 responses");
            }
        }

        // 6.8.8.2.h. For OBD ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        getDataRepository().getObdModules()
                           .stream()
                           .filter(OBDModuleInformation::supportsDM27)
                           .map(OBDModuleInformation::getSourceAddress)
                           .flatMap(a -> packets.stream().filter(p -> p.getSourceAddress() == a))
                           .filter(p -> p.getAllPendingDTCCount() != 0xFF)
                           .filter(p -> p.getAllPendingDTCCount() > 0)
                           .map(ParsedPacket::getModuleName)
                           .forEach(moduleName -> {
                               addFailure("6.8.8.2.h - " + moduleName + " reported > 0 for all pending DTCs");
                           });

        // 6.8.8.2.i. For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs =
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

        // 6.8.8.3.a. Info if any OBD ECU reports > 1 for MIL on.
        packets.stream()
                .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.8.8.3.a - " + moduleName + " reported > 1 for MIL on");
               });

        // 6.8.8.3.b. Info if more than one ECU reports > 0 for MIL on.
        long milOnCount = packets.stream()
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() != 0xFF)
                                 .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
                                 .count();
        if (milOnCount > 1) {
            addInfo("6.8.8.3.b - More than one ECU reported > 0 for MIL on");
        }

        // 6.8.8.3.c. Info if any ECU reports > 1 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.8.8.3.c - " + moduleName + " reported > 1 for previous MIL on");
               });

        // 6.8.8.3.d. Info if more than one ECU reports > 0 for previous MIL on.
        long prevMilOnCount = packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
                                     .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
                                     .count();
        if (prevMilOnCount > 1) {
            addInfo("6.8.8.3.d - More than one ECU reported > 0 for previous MIL on");
        }

        // 6.8.8.3.e. Info if any ECU report > 1 for permanent.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addInfo("6.8.8.3.e - " + moduleName + " reported > 1 for permanent");
               });

        // 6.8.8.3.f. Info if more than one ECU reports > 0 for permanent.
        long permanentCount = packets.stream()
                                     .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
                                     .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0)
                                     .count();
        if (permanentCount > 1) {
            addInfo("6.8.8.3.f - More than one ECU reported > 0 for permanent");
        }
    }

    private int getDM28Count(int address) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, address, 8).size();
    }
}
