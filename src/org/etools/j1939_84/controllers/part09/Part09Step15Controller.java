/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.9.15 DM29: Regulated DTC Counts
 */
public class Part09Step15Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 15;
    private static final int TOTAL_STEPS = 0;

    Part09Step15Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step15Controller(Executor executor,
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
        // 6.9.15.1.a. Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        // 6.9.15.2.a. Fail if any ECU reports > 0 for emission-related pending
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() !=0xFF)
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.a - " + moduleName + " reported > 0 for emission-related pending");
               });

        // 6.9.15.2.a. Fail if any ECU reports > 0 for MIL-on
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() !=0xFF)
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.a - " + moduleName + " reported > 0 for MIL-on");
               });

        // 6.9.15.2.a. Fail if any ECU reports > 0 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() != 0xFF)
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.a - " + moduleName + " reported > 0 for previous MIL on");
               });

        // 6.9.15.2.b. Fail if no ECU reports > 0 for permanent DTC.
        boolean noPermanent = packets.stream()               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
.noneMatch(p -> p.getEmissionRelatedPermanentDTCCount() > 0);
        if (noPermanent) {
            addFailure("6.9.15.2.b - No ECU reported > 0 for permanent DTC");
        }

        // 6.9.15.2.c. Fail if any ECU reports a different number for permanent DTC than what that ECU reported in DM28.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != getDTCs(p.getSourceAddress()).size())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.c - " + moduleName
                           + " reported different number for permanent DTC than what it reported in DM28");
               });

        // 6.9.15.2.d. For OBD ECUs that support DM27, fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .filter(p -> p.getAllPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.d - " + moduleName + " reported > 0 for all pending DTCs");
               });

        // 6.9.15.2.e. For OBD ECUs that do not support DM27, fail if any ECU does not report number of all pending DTCs
        // = 0xFF.
        packets.stream()
               .filter(p -> isObdModule(p.getSourceAddress()))
               .filter(p -> !supportsDM27(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() != 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.15.2.e - " + moduleName + " did not report all pending DTCs = 0xFF");
               });

        // 6.9.15.3.a. Warn if any ECU reports > 1 for permanent DTC.
        packets.stream()
                              .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
.filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.9.15.3.a - " + moduleName + " reported > 1 for permanent DTC");
               });

        // 6.9.15.3.b. Warn if more than one ECU reports > 0 for permanent DTC.
        long count = packets.stream()               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != 0xFF)
.filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0).count();
        if (count > 1) {
            addWarning("6.9.15.3.b - More than one ECU reported > 0 for permanent DTC");
        }
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, address, 9);
    }

}
