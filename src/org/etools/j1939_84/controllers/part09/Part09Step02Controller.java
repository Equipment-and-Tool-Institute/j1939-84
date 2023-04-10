/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939tools.j1939.packets.LampStatus.ON;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.2 DM12: Emissions Related Active DTCs
 */
public class Part09Step02Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part09Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step02Controller(Executor executor,
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
        // 6.9.2.1.a Global DM12 [(send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)]).
        var packets = getCommunicationsModule().requestDM12(getListener())
                                               .getPackets()
                                               .stream()
                                               .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                                               .collect(Collectors.toList());

        // 6.9.2.1.b Create list of which OBD ECU(s) have a DM12 active MIL on DTC and which do not.
        packets.forEach(this::save);

        // 6.9.2.2.a Fail if no OBD ECU reporting one or more active MIL on DTCs.
        var noDTCs = packets.stream().allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.9.2.2.a - No OBD ECU reported one or more active MIL on DTCs");
        }

        // 6.9.2.2.b Fail if no OBD ECUs reporting MIL commanded on.
        var noMIL = packets.stream().noneMatch(p -> p.getMalfunctionIndicatorLampStatus() == ON);
        if (noMIL) {
            addFailure("6.9.2.2.b - No OBD ECUs reported MIL commanded on");
        }

        // 6.9.2.2.c Fail if any ECU reports a different active MIL on DTC(s) than what that ECU reported in part 8 DM12
        // response.
        packets.forEach(dm12 -> {
            var prevDM12 = get(DM12MILOnEmissionDTCPacket.class, dm12.getSourceAddress(), 8);
            if (prevDM12 == null || isNotSubset(prevDM12.getDtcs(), dm12.getDtcs())) {
                addFailure("6.9.2.2.c - " + dm12.getModuleName()
                        + " reported different active MIL on DTC(s) than what it reported in part 8 DM 12 response");
            }
        });

        // 6.9.2.3.a Warn if any ECU reports > 1 active DTC.
        packets.stream()
               .filter(p -> p.getDtcs().size() > 1)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addWarning("6.9.2.3.a - " + moduleName + " reported > 1 active DTC");
               });

        // 6.9.2.3.b Warn if more than one ECU reports an active DTC
        var dtcCount = packets.stream().filter(DiagnosticTroubleCodePacket::hasDTCs).count();
        if (dtcCount > 1) {
            addWarning("6.9.2.3.b - More than one ECU reported an active DTC");
        }

    }

}
