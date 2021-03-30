/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
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
 * 6.7.6 DM5: Diagnostic Readiness 1
 */
public class Part07Step06Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;
    private static final byte NA = (byte) 0xFF;

    Part07Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part07Step06Controller(Executor executor,
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
        // 6.7.6.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)]).
        var obdPackets = getDiagnosticMessageModule().requestDM5(getListener())
                                                     .getPackets()
                                                     .stream()
                                                     .filter(p -> isObdModule(p.getSourceAddress()))
                                                     .collect(Collectors.toList());

        // 6.7.6.2.a Fail if any OBD ECU reports > 0 for active DTCs.
        obdPackets.stream()
                  .filter(p -> p.getActiveCodeCount() > 0)
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addFailure("6.7.6.2.a - " + moduleName + " reported > 0 for active DTCs");
                  });

        // 6.7.6.2.b Fail if no [OBD] ECU reports > 0 for previously active DTCs.
        boolean noPrev = obdPackets.stream()
                                   .map(DM5DiagnosticReadinessPacket::getPreviouslyActiveCodeCount)
                                   .allMatch(c -> c == 0);
        if (noPrev) {
            addFailure("6.7.6.2.b - No ECU reported > 0 for previously active DTCs");
        }

        // 6.7.6.2.c Fail if any OBD ECU reports a different number of previously active DTCs than in DM2 response
        // earlier in this Part. [ Ignore previously active count when DM2 is not supported.]
        obdPackets.stream()
                  .filter(p -> getDTCCount(p.getSourceAddress()) != NA)
                  .filter(p -> p.getPreviouslyActiveCodeCount() != getDTCCount(p.getSourceAddress()))
                  .map(ParsedPacket::getModuleName)
                  .forEach(moduleName -> {
                      addFailure("6.7.6.2.c - " + moduleName
                              + " reported a different number of previously active DTCs than in DM2 response earlier in this part");
                  });
    }

    private byte getDTCCount(int address) {
        DM2PreviouslyActiveDTC packet = get(DM2PreviouslyActiveDTC.class, address, 7);
        return packet == null ? NA : (byte) packet.getDtcs().size();
    }

}
