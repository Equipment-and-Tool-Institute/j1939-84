/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

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
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.8.4 DM23: Emission Related Previously Active DTCs
 */
public class Part08Step04Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part08Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step04Controller(Executor executor,
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
        // 6.8.4.1.a Global DM23 [(send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)]).
        var packets = getCommunicationsModule().requestDM23(getListener())
                                               .getPackets()
                                               .stream()
                                               .filter(p -> isObdModule(p.getSourceAddress()))
                                               .collect(Collectors.toList());

        packets.forEach(this::save);

        // 6.8.4.2.a Fail if no OBD ECU reports a previously active DTC.
        boolean noDTCs = packets.stream().allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.8.4.2.a - No OBD ECU reported a previously active DTC");
        }

        // 6.8.4.2.b Fail if previously active DTC reported is not the same as previously active DTC from part 7.
        packets.stream()
               .filter(p -> {
                   List<DiagnosticTroubleCode> oldDTCs = getDTCs(p.getSourceAddress());
                   List<DiagnosticTroubleCode> newDTCs = p.getDtcs();
                   return isNotSubset(oldDTCs, newDTCs);
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.4.2.b - Previously active DTC reported by " + moduleName
                           + " is not the same as previously active DTC from part 7");
               });

        // 6.8.4.2.c Fail if any ECU reporting different MIL status than DM12 response earlier in this part.
        packets.stream()
               .filter(p -> getMIL(p.getSourceAddress()) != null)
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMIL(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.4.2.c - " + moduleName
                           + " reported different MIL status than DM12 response earlier in this part");
               });
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 7);
    }

    private LampStatus getMIL(int address) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, address, 8);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }

}
