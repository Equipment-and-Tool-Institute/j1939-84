/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.5.2 DM12: Emission-Related Active DTCs
 */
public class Part05Step02Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part05Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part05Step02Controller(Executor executor,
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

        // 6.5.2.1.a Global DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM12(getListener()).getPackets();

        globalPackets.forEach(this::save);

        // 6.5.2.2.a Fail if no OBD ECU reporting MIL on. See Section A.8 for allowed values.
        boolean noMilOn = globalPackets.stream().noneMatch(p -> (p.getMalfunctionIndicatorLampStatus() == ON));
        if (noMilOn) {
            addFailure("6.5.2.2.a - No OBD ECU reported MIL on");
        }

        // 6.5.2.2.b Fail if all OBD ECUs report no DM12 DTC set.
        boolean noDM12 = globalPackets.stream().noneMatch(p -> (p.getDtcs().size() > 0));
        if (noDM12) {
            addFailure("6.5.2.2.b - All OBD ECUs report no DM12 DTCs");
        }

        // 6.5.2.2.c Fail if DM12 DTC reported does not match the DM6 DTC SPN and FMI reported from step 6.3.2.
        globalPackets.forEach(packet -> {
            var dm6DTCs = getDTCs(DM6PendingEmissionDTCPacket.class, packet.getSourceAddress(), 3);
            List<DiagnosticTroubleCode> dm12DTCs = packet.getDtcs();
            if (isNotSubset(dm12DTCs, dm6DTCs)) {
                addFailure("6.5.2.2.c - OBD ECU " + packet.getModuleName() +
                        " had a discrepancy between reported DM12 DTCs and DM6 DTCs reported in 6.3.2");
            }
        });

        // 6.5.2.2.d Fail if any ECU reporting MIL as ON, flashing. See Section A.8 for allowed values.
        globalPackets.forEach(packet -> {
            if (packet.getMalfunctionIndicatorLampStatus() == FAST_FLASH ||
                    packet.getMalfunctionIndicatorLampStatus() == SLOW_FLASH) {
                addFailure("6.5.2.2.d - OBD ECU " + packet.getModuleName() +
                        " reported a MIL as " + packet.getMalfunctionIndicatorLampStatus());

            }
        });
    }

}
