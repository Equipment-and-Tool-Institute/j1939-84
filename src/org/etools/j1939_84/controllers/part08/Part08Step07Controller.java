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
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.8.7 DM28: Permanent DTCs
 */
public class Part08Step07Controller extends StepController {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part08Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part08Step07Controller(Executor executor,
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
        // 6.8.7.1.a. Global DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM28(getListener()).getPackets();

        globalPackets.forEach(this::save);

        // 6.8.7.2.a. Fail if no OBD ECU reports a permanent DTC.
        boolean noDTCs = globalPackets.stream().allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.8.7.2.a - No OBD ECU reported a permanent DTC");
        }

        // 6.8.7.2.b. Fail if permanent DTC does not match DM12 DTC from earlier in test 6.8.2.
        globalPackets.forEach(p -> {
            if (!p.getDtcs().containsAll(getDTCs(p.getSourceAddress()))) {
                         addFailure("6.8.7.2.b - " + p.getModuleName()
                                 + " DM28 does not include the DM12 active DTC that the SA reported from earlier in this part.");
                     }});

        // 6.8.7.2.c. Fail if any ECU reporting different MIL status than DM12 response earlier in test 6.8.2.
        globalPackets.stream()
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != getMIL(p.getSourceAddress()))
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.8.7.2.c - " + moduleName
                                 + " reported different MIL status than DM12 response earlier in test 6.8.2");
                     });

        // 6.8.7.3.a. Warn if more than one ECU reports a permanent DTC.
        long dtcCount = globalPackets.stream().map(p -> !p.getDtcs().isEmpty()).count();
        if (dtcCount > 1) {
            addWarning("6.8.7.3.a - More than on ECU reported a permanent DTC");
        }

        // 6.8.7.3.b. Warn if any ECU reports more than one permanent DTC.
        globalPackets.stream()
                     .filter(p -> p.getDtcs().size() > 1)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addWarning("6.8.7.3.b - " + moduleName + " reported more than one permanent DTC");
                     });

        // 6.8.7.4.a. DS DM28 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.8.7.5.a. Fail if any difference in data compared to global response.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.8.7.5.a");

        // 6.8.7.5.b. Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.8.7.5.b");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, address, 8);
    }

    private LampStatus getMIL(int address) {
        var dm12 = get(DM12MILOnEmissionDTCPacket.class, address, 8);
        return dm12 == null ? null : dm12.getMalfunctionIndicatorLampStatus();
    }

}
