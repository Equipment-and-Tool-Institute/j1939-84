/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.3 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part04Step03Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part04Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part04Step03Controller(Executor executor,
                           BannerModule bannerModule,
                           DateTimeModule dateTimeModule,
                           DataRepository dataRepository,
                           EngineSpeedModule engineSpeedModule,
                           VehicleInformationModule vehicleInformationModule,
                           DiagnosticMessageModule diagnosticMessageModule) {
        super(executor,
              bannerModule,
              dateTimeModule,
              dataRepository,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule,
              PART_NUMBER,
              STEP_NUMBER,
              TOTAL_STEPS);
    }

    @Override
    protected void run() throws Throwable {
        // 6.4.3.1.a Receive broadcast data ([PGN 65226 (SPNs 1213-1215, 3038, 1706)]).
        List<DM1ActiveDTCsPacket> packets = getDiagnosticMessageModule().readDM1(getListener());

        // 6.4.3.2.a Fail if no ECU reports an active DTC and MIL on.
        boolean noReports = packets.stream()
                .noneMatch(p -> !p.getDtcs().isEmpty() || p.getMalfunctionIndicatorLampStatus() == ON);
        if (noReports) {
            addFailure("6.4.3.2.a - No ECU reported an active DTC and MIL on");
        }

        // 6.4.3.2.b Fail if any OBD ECU report does not include its DM12 DTCs in the list of active DTCs.
        for (OBDModuleInformation moduleInfo : getDataRepository().getObdModules()) {
            int moduleAddress = moduleInfo.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);
            DM12MILOnEmissionDTCPacket dm12 = moduleInfo.get(DM12MILOnEmissionDTCPacket.class);
            List<DiagnosticTroubleCode> existingDTCs = dm12 == null ? List.of() : dm12.getDtcs();

            List<DM1ActiveDTCsPacket> modulePackets = packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .collect(Collectors.toList());
            for (DM1ActiveDTCsPacket packet : modulePackets) {
                boolean failure = false;
                var packetDTCs = toString(packet.getDtcs());
                for (DiagnosticTroubleCode dtc : existingDTCs) {
                    String key = dtc.getSuspectParameterNumber() + ":" + dtc.getFailureModeIndicator();
                    if (!packetDTCs.contains(key)) {
                        failure = true;
                        addFailure("6.4.3.2.b - " + moduleName + " did not include its DM12 DTCs in the list of active DTCs");
                        break;
                    }
                }
                if (failure) {
                    break;
                }
            }
        }

        // 6.4.3.2.c Fail if any OBD ECU reports fewer active DTCs in its DM1 response than its DM12 response.
        for (OBDModuleInformation moduleInfo : getDataRepository().getObdModules()) {
            int moduleAddress = moduleInfo.getSourceAddress();
            String moduleName = Lookup.getAddressName(moduleAddress);
            DM12MILOnEmissionDTCPacket dm12 = moduleInfo.get(DM12MILOnEmissionDTCPacket.class);
            List<DiagnosticTroubleCode> existingDTCs = dm12 == null ? List.of() : dm12.getDtcs();

            List<DM1ActiveDTCsPacket> modulePackets = packets.stream()
                    .filter(p -> p.getSourceAddress() == moduleAddress)
                    .collect(Collectors.toList());
            for (DM1ActiveDTCsPacket packet : modulePackets) {
                if (packet.getDtcs().size() < existingDTCs.size()) {
                    addFailure("6.4.3.2.c - " + moduleName + " reported fewer active DTCs in its DM1 response than its DM12 response");
                    break;
                }
            }
        }

        // 6.4.3.2.d Warn if any non-OBD ECU reports an Active DTC.
        packets.stream()
                .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                .filter(p -> !p.getDtcs().isEmpty())
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addWarning("6.4.3.2.d - Non-OBD ECU " + moduleName + " reported an active DTC"));

        // 6.4.3.2.e Warn if more than 1 active DTC is reported by the vehicle.
        long dtcCount = packets.stream().map(DiagnosticTroubleCodePacket::getDtcs).mapToLong(Collection::size).sum();
        if (dtcCount > 1) {
            addWarning("6.4.3.2.e - More than 1 active DTC is reported by the vehicle");
        }

    }

}