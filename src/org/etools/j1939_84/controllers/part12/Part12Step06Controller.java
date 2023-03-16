/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.12.6 DM1: Active Diagnostic Trouble Codes (DTCs)
 */
public class Part12Step06Controller extends StepController {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part12Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part12Step06Controller(Executor executor,
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
        // 6.12.6.1.a. Receive broadcast info ([PGN 65226 (SPNs 1213-1215, 1706, 3038)]).
        var dm1s = read(DM1ActiveDTCsPacket.class, 3, SECONDS).stream().map(p -> {
            return new DM1ActiveDTCsPacket(p.getPacket());
        }).collect(Collectors.toList());

        // 6.12.6.2.a. Fail if any ECU does not report MIL off or not supported. See Section A.8 for allowed values.
        dm1s.stream()
            .filter(p -> {
                var mil = p.getMalfunctionIndicatorLampStatus();
                return isNotOff(mil) && mil != NOT_SUPPORTED;
            })
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.12.6.2.a - " + moduleName + " did not report MIL 'off' or not supported");
            });

        // 6.12.6.2.b. Fail if any ECU reports active DTCs.
        dm1s.stream()
            .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
            .filter(DiagnosticTroubleCodePacket::hasDTCs)
            .map(ParsedPacket::getModuleName)
            .forEach(moduleName -> {
                addFailure("6.12.6.2.b - " + moduleName + " reported active DTC(s)");
            });
    }

}
