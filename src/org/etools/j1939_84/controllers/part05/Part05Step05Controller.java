/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.5.5 DM29: Regulated DTC Counts
 */
public class Part05Step05Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part05Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part05Step05Controller(Executor executor,
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
        // 6.5.5.1.a Global DM29 [(send Request (PGN 59904) for PGN 40448 (SPNs 4104-4108)]).
        var packets = getCommunicationsModule().requestDM29(getListener()).getPackets();

        // 6.5.5.2.a Fail if any ECU reports > 0 for emission-related pending.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPendingDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.a - " + moduleName + " reported > 0 for emissions-related pending");
               });

        // 6.5.5.2.a Fail if any ECU reports > 0 for previous MIL on.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPreviouslyMILOnDTCCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.a - " + moduleName + " reported > 0 for previous MIL on");
               });

        // 6.5.5.2.b Fail if no ECU reports > 0 MIL on DTCs where the same ECU provides one or more permanent DTCs.
        boolean noReports = packets.stream()
                                   .noneMatch(p -> p.getEmissionRelatedMILOnDTCCount() > 0
                                           && p.getEmissionRelatedPermanentDTCCount() > 0);
        if (noReports) {
            addFailure("6.5.5.2.b - No ECU reported > 0 MIL on DTCs and > 0 permanent DTCs");
        }

        // 6.5.5.2.c Fail if any ECU reports a different number of MIL on DTCs than what that ECU reported in DM12
        // earlier in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() != getDM12DTCs(p.getSourceAddress()).size())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.c - " + moduleName
                           + " reported a different number of MIL on DTCs than what it reported in DM12 earlier in this part");
               });

        // 6.5.5.2.d Fail if any ECU reports a different number of permanent DTCs than what that ECU reported in DM28
        // earlier in this part.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() != getDM28DTCs(p.getSourceAddress()).size())
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.d - " + moduleName
                           + " reported a different number of permanent DTCs than what it reported in DM28 earlier in this part");
               });

        // 6.5.5.2.e.i. For OBD ECUs that support DM27, Fail if any ECU reports > 0 for all pending DTCs (SPN 4105).
        packets.stream()
               .filter(p -> isDM27Supported(p.getSourceAddress()))
               .filter(p -> p.getAllPendingDTCCount() > 0 && p.getAllPendingDTCCount() != 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.e.i - " + moduleName + " reported > 0 for all pending DTCs");
               });

        // 6.5.5.2.e.ii. For OBD ECUs that support DM27, Fail if any ECU reports 0xFF for all pending DTCs.
        packets.stream()
               .filter(p -> isDM27Supported(p.getSourceAddress()))
               .filter(p -> (byte) p.getAllPendingDTCCount() == (byte) 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.e.ii - " + moduleName + " reported 0xFF for all pending DTCs");
               });

        // 6.5.5.2.f.i For ECUs that do not support DM27, Fail if any ECU does not report number of all pending DTCs
        // (SPN 4105) = 0xFF.
        packets.stream()
               .filter(p -> !isDM27Supported(p.getSourceAddress()))
               .filter(p -> (byte) p.getAllPendingDTCCount() != (byte) 0xFF)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.5.2.f.i - " + moduleName + " did not report all pending DTCs = 0xFF");
               });

        // 6.5.5.3.a Warn if any ECU reports > 1 for MIL on
        packets.stream()
               .filter(p -> p.getEmissionRelatedMILOnDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.5.5.3.a - " + moduleName + " reported > 1 for MIL on");
               });

        // 6.5.5.3.a Warn if any ECU reports > 1 for permanent.
        packets.stream()
               .filter(p -> p.getEmissionRelatedPermanentDTCCount() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.5.5.3.a - " + moduleName + " reported > 1 for permanent");
               });

        // 6.5.5.3.b Warn if more than one ECU reports > 0 for MIL on
        long milOnCount = packets.stream().filter(p -> p.getEmissionRelatedMILOnDTCCount() > 0).count();
        if (milOnCount > 1) {
            addWarning("6.5.5.3.b - More than one ECU reported > 0 for MIL on");
        }

        // 6.5.5.3.b Warn if more than one ECU reports > 0 for permanent
        long permanentCount = packets.stream().filter(p -> p.getEmissionRelatedPermanentDTCCount() > 0).count();
        if (permanentCount > 1) {
            addWarning("6.5.5.3.b - More than one ECU reported > 0 for permanent");
        }
    }

    private List<DiagnosticTroubleCode> getDM12DTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 5);
    }

    private List<DiagnosticTroubleCode> getDM28DTCs(int moduleAddress) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, moduleAddress, 5);
    }

    private boolean isDM27Supported(int moduleAddress) {
        OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(moduleAddress);
        return obdModuleInformation != null && obdModuleInformation.supportsDM27();
    }

}
