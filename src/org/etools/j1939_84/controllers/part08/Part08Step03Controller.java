/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.8.3 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part08Step03Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 3;
    private static final int TOTAL_STEPS = 0;

    Part08Step03Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step03Controller(Executor executor,
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

        // 6.8.3.1.a Receive broadcast data [(PGN 65226 (SPNs 1213-1215, 1706, and 3038)]).
        List<DM1ActiveDTCsPacket> packets = read(DM1ActiveDTCsPacket.class,
                                                 3,
                                                 SECONDS).stream()
                                                         .map(p -> new DM1ActiveDTCsPacket(p.getPacket()))
                                                         .collect(
                                                                  Collectors.toList());

        packets.forEach(this::save);

        // 6.8.3.2.a Fail if no ECU reporting MIL on.
        boolean noReports = packets.stream()
                                   .noneMatch(p -> p.getMalfunctionIndicatorLampStatus() == ON);
        if (noReports) {
            addFailure("6.8.3.2.a - No ECU reported MIL on");
        }

        // 6.8.3.2.b Fail if any OBD ECU does not include all DTCs from its DM12 response in its DM1 response.
        for (OBDModuleInformation moduleInfo : getDataRepository().getObdModules()) {
            int moduleAddress = moduleInfo.getSourceAddress();

            packets.stream()
                   .filter(p -> p.getSourceAddress() == moduleAddress)
                   .filter(p -> {
                       List<DiagnosticTroubleCode> dm21DTCs = getDTCs(moduleAddress);
                       List<DiagnosticTroubleCode> dm1DTCs = p.getDtcs();
                       return isNotSubset(dm21DTCs, dm1DTCs);
                   })
                   .map(ParsedPacket::getModuleName)
                   .findFirst()
                   .ifPresent(moduleName -> addFailure("6.8.3.2.b - " + moduleName
                           + " did not include all DTCs from its DM12 response"));
        }

        // 6.8.3.2.c Fail if any OBD ECU reporting different MIL status than DM12 response earlier in this part.
        for (OBDModuleInformation moduleInfo : getDataRepository().getObdModules()) {
            int moduleAddress = moduleInfo.getSourceAddress();

            packets.stream()
                   .filter(p -> p.getSourceAddress() == moduleAddress)
                   .filter(p -> getMIL(moduleAddress) != null)
                   .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMIL(moduleAddress))
                   .map(ParsedPacket::getModuleName)
                   .findFirst()
                   .ifPresent(moduleName -> addFailure("6.8.3.2.c - " + moduleName
                           + " reported a different MIL status than DM12 response earlier in this part"));
        }
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 8);
    }

    private LampStatus getMIL(int moduleAddress) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, moduleAddress, 8);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }
}
