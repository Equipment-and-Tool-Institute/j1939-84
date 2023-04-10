/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.11.9 DM28: Permanent DTCs
 */
public class Part11Step09Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part11Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part11Step09Controller(Executor executor,
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
        // 6.11.9.1.a. DS DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 3038, 1706)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // 6.11.9.2.a. Fail if no ECU reports a permanent DTC.
        boolean noDTCs = packets.stream().noneMatch(DiagnosticTroubleCodePacket::hasDTCs);
        if (noDTCs) {
            addFailure("6.11.9.2.a - No ECU reported a permanent DTC");
        }

        // 6.11.9.2.b. Fail if the permanent DTCs reported are not the same DTCs that were reported in DM28 in part 10.
        packets.forEach(p -> {
            if (isNotSubset(getDTCs(p.getSourceAddress()), p.getDtcs())) {
                addFailure("6.11.9.2.b - " + p.getModuleName()
                        + " reported a different DTCs than as reported in DM28 in part 10");
            }
        });

        // 6.11.9.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.11.9.2.c");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, address, 10);
    }
}
