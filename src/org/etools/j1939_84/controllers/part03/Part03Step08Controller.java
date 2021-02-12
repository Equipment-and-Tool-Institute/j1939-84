/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.8 DM5: Diagnostic readiness 1
 */
public class Part03Step08Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part03Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step08Controller(Executor executor,
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
        // 6.3.8.1.a. Global DM5 (send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)).
        var packets = getDiagnosticMessageModule().requestDM5(getListener())
                .getPackets()
                .stream()
                .filter(DM5DiagnosticReadinessPacket::isObd)
                .collect(Collectors.toList());

        // 6.3.8.2.a Fail if any OBD ECU does not report 0 for the number of active DTCs.
        packets.stream()
                .filter(p -> p.getActiveCodeCount() != 0 && p.getActiveCodeCount() != (byte) 0xFF)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.3.8.2.a - OBD ECU " + moduleName + " reported active DTC count not = 0"));

        // 6.3.8.2.a Fail if any OBD ECU does not report 0 for the number of previously active DTCs.
        packets.stream()
                .filter(p -> p.getPreviouslyActiveCodeCount() != 0 && p.getPreviouslyActiveCodeCount() != (byte) 0xFF)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> addFailure("6.3.8.2.a - OBD ECU " + moduleName + " reported previously active DTC count not = 0"));
    }

}
