/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.6.2 DM5: Diagnostic Readiness 1
 */
public class Part06Step02Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part06Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part06Step02Controller(Executor executor,
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
        // 6.6.2.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)]).
        RequestResult<DM5DiagnosticReadinessPacket> globalDM5Result = getDiagnosticMessageModule().requestDM5(getListener());
        List<DM5DiagnosticReadinessPacket> globalDM5Packets = globalDM5Result.getPackets();
        List<DM5DiagnosticReadinessPacket> obdGlobalPackets = globalDM5Packets.stream()
                                                                              .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                                                              .collect(Collectors.toList());

        // 6.6.2.2.a Fail if no OBD ECU reports a count of > 0 active DTCs.
        boolean noActiveDTCs = obdGlobalPackets.stream().noneMatch(p -> (p.getActiveCodeCount() > 0));
        if (noActiveDTCs) {
            addFailure("6.6.2.2.a - No OBD ECU reported a count of > 0 active DTCs");
        }

        // 6.6.2.2.b Fail if any OBD ECU reports > 0 previously active DTC.
        obdGlobalPackets.stream()
                        .filter(p -> p.getPreviouslyActiveCodeCount() != (byte) 0xFF
                                && p.getPreviouslyActiveCodeCount() > 0)
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> addFailure("6.6.2.2.b - OBD ECU " + moduleName
                                + " reported > 0 previously active DTC"));

        // 6.6.2.3.a Warn if any ECU reports a count of > 1 active DTC or previously active DTC.
        obdGlobalPackets.stream()
                        .filter(p -> (p.getPreviouslyActiveCodeCount() != (byte) 0xFF &&
                                p.getPreviouslyActiveCodeCount() > 1) ||
                                (p.getActiveCodeCount() != (byte) 0xFF &&
                                        p.getActiveCodeCount() > 1))
                        .map(ParsedPacket::getModuleName)
                        .forEach(moduleName -> addWarning("6.6.2.3.a - ECU module " + moduleName
                                + " reported a count of > 1 active DTC or previously active DTC"));

    }

}
