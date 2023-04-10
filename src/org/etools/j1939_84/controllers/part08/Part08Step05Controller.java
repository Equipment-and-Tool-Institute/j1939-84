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

/**
 * 6.8.5 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part08Step05Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 5;
    private static final int TOTAL_STEPS = 0;

    Part08Step05Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step05Controller(Executor executor,
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
        // 6.8.5.1.a Global DM2 ([send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)]).
        var packets = getCommunicationsModule().requestDM2(getListener())
                                               .getPackets()
                                               .stream()
                                               .filter(p -> isObdModule(p.getSourceAddress()))
                                               .collect(Collectors.toList());

        packets.forEach(this::save);

        // 6.8.5.2.a (if supported) Fail if any OBD ECU does not include all DTCs from its DM23 response in its DM2
        // response.
        packets.stream()
               .filter(p -> isNotSubset(getDTCs(p.getSourceAddress()), p.getDtcs()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.5.2.a - " + moduleName
                           + " did not include all DTCs from its DM23 response in its DM2 response");
               });

        // 6.8.5.2.b (if supported) Fail if any OBD ECU reporting a different MIL status than DM12 response earlier in
        // this part.
        packets.stream()
               .filter(p -> getMIL(p.getSourceAddress()) != null)
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMIL(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.8.5.2.b - " + moduleName
                           + " reported different MIL status than DM12 response earlier in this part");
               });
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM23PreviouslyMILOnEmissionDTCPacket.class, address, 8);
    }

    private LampStatus getMIL(int address) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, address, 8);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }
}
