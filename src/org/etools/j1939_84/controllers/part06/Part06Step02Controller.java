/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

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
             new CommunicationsModule());
    }

    Part06Step02Controller(Executor executor,
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
        // 6.6.2.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)]).
        var globalPackets = getCommunicationsModule().requestDM5(getListener()).getPackets();

        // Save the DM5 response for each OBD ECU
        globalPackets.forEach(this::save);

        // 6.6.2.2.a Fail if no OBD ECU reports a count of > 0 active DTCs.
        boolean noActiveDTCs = globalPackets.stream()
                                            .filter(p -> isObdModule(p.getSourceAddress()))
                                            .noneMatch(p -> (p.getActiveCodeCount() > 0));
        if (noActiveDTCs) {
            addFailure("6.6.2.2.a - No OBD ECU reported a count of > 0 active DTCs");
        }

        // 6.6.2.2.b Fail if any OBD ECU reports > 0 previously active DTC.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                        .filter(p -> p.getPreviouslyActiveCodeCount() != (byte) 0xFF
                                && p.getPreviouslyActiveCodeCount() > 0)
                        .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.6.2.2.b - OBD ECU " + moduleName + " reported > 0 previously active DTC");
                     });

        // 6.6.2.3.a Warn if any ECU reports a count of > 1 active DTC or previously active DTC.
        globalPackets.forEach(p -> {
            if (p.getPreviouslyActiveCodeCount() != (byte) 0xFF &&
                    p.getPreviouslyActiveCodeCount() > 1) {
                addWarning("6.6.2.3.a - OBD ECU " + p.getModuleName()
                        + " reported a count of > 1 for previously active DTCs");
            }
            if (p.getActiveCodeCount() != (byte) 0xFF &&
                    p.getActiveCodeCount() > 1) {
                addWarning("6.6.2.3.a - OBD ECU " + p.getModuleName()
                        + " reported a count of > 1 active DTCs");
            }
        });
    }
}
