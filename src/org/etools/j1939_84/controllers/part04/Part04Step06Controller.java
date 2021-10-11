/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.soliddesign.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import net.soliddesign.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.4.6 DM5: Diagnostic Readiness 1
 */
public class Part04Step06Controller extends StepController {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 6;
    private static final int TOTAL_STEPS = 0;

    Part04Step06Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part04Step06Controller(Executor executor,
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
        // 6.4.6.1.a Global DM5 [(send Request (PGN 59904) for PGN 65230 (SPNs 1218-1219)]).
        var packets = getCommunicationsModule().requestDM5(getListener())
                                                  .getPackets()
                                                  .stream()
                                                  .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                                  .collect(Collectors.toList());

        // 6.4.6.2.a Fail if no OBD ECU reports number of active DTCs as > 0.
        boolean noActiveCodes = packets.stream()
                                       .map(DM5DiagnosticReadinessPacket::getActiveCodeCount)
                                       .allMatch(c -> c == 0);
        if (noActiveCodes) {
            addFailure("6.4.6.2.a - No OBD ECU reported number of active DTCs as > 0");
        }

        // 6.4.6.2.b Fail if any OBD ECU reports a different number of active DTCs than it did in DM1 response earlier
        // in this part.
        packets.stream()
               .filter(p -> {
                   int address = p.getSourceAddress();
                   return p.getActiveCodeCount() != getDTCCount(address);
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.4.6.2.b - " + moduleName + " reported a different number " +
                           "of active DTCs than it did in DM1 response earlier in this part.");
               });

        // 6.4.6.2.c Fail if any OBD ECU reports > 0 previously active DTCs.
        packets.stream()
               .filter(p -> p.getPreviouslyActiveCodeCount() > 0)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.4.6.2.c - " + moduleName + " reported > 0 previously active DTCs");
               });
    }

    private byte getDTCCount(int address) {
        DM1ActiveDTCsPacket packet = get(DM1ActiveDTCsPacket.class, address, 4);
        return packet == null ? (byte) 0xFF : (byte) packet.getDtcs().size();
    }

}
