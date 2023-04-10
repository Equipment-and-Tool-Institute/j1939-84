/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.2 DM23: Emission Related Previously Active DTCs
 */
public class Part07Step02Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part07Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step02Controller(Executor executor,
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
        // 6.7.2.1.a DS DM23 ([send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)]) to each OBD
        // ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM23(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.7.2.2.a Fail if no OBD ECU reports previously active DTC.
        boolean noObdDtc = packets.stream()
                                  .filter(p -> isObdModule(p.getSourceAddress()))
                                  .noneMatch(DiagnosticTroubleCodePacket::hasDTCs);
        if (noObdDtc) {
            addFailure("6.7.2.2.a - No OBD ECU reported a previously active DTC");
        }

        // 6.7.2.2.b Fail if reported previously active DTC does not match DM12 active DTC from part 6.
        packets.forEach(p -> {
            List<DiagnosticTroubleCode> dm12DTCs = getDTCs(DM12MILOnEmissionDTCPacket.class, p.getSourceAddress(), 6);
            if (isNotSubset(dm12DTCs, p.getDtcs())) {
                addFailure("6.7.2.2.b - OBD ECU " + p.getModuleName()
                        + " reported a different DTCs from the DM12 DTCs");
            }
        });

        // 6.7.2.2.c. Fail if any ECU does not report MIL off and not flashing.
        packets.forEach(p -> {
            if (p.getMalfunctionIndicatorLampStatus() != OFF) {
                addFailure("6.7.2.2.c - OBD ECU " + p.getModuleName() + " did not report MIL off and not flashing");
            }
        });

        // 6.7.2.3.a Warn if any ECU reports > 1 previously active DTC.
        packets.forEach(p -> {
            if (p.getDtcs().size() > 1) {
                addWarning("6.7.2.3.a - OBD ECU " + p.getModuleName() + " reported > 1 previously active DTCs");
            }
        });

        // 6.7.2.3.b Warn if more than one ECU reports a previously active DTC.
        long activeDtcCount = packets.stream().filter(DiagnosticTroubleCodePacket::hasDTCs).count();
        if (activeDtcCount > 1) {
            addWarning("6.7.2.3.b - More than one ECU reported previously active DTC");
        }

        // 6.7.2.2.d Fail if NACK not received from OBD ECUs that did not provide a DM23 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.7.2.2.d");
    }

}
