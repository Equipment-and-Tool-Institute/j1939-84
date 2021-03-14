/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

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
             new DiagnosticMessageModule());
    }

    Part11Step09Controller(Executor executor,
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
        // 6.11.9.1.a. DS DM28 [(send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 3038, 1706)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // 6.11.9.2.a. Fail if no ECU reports a permanent DTC.
        boolean noDTCs = packets.stream().noneMatch(DiagnosticTroubleCodePacket::hasDTCs);
        if (noDTCs) {
            addFailure("6.11.9.2.a - No ECU reported a permanent DTC");
        }

        // 6.11.9.2.b. Fail if the permanent DTC reported is not the same DTC as reported in DM28 in part 10.
        packets.stream()
               .filter(p -> !p.getDtcs().equals(getDTCs(p.getSourceAddress())))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.9.2.b - " + moduleName
                           + " reported a different DTC than as reported in DM28 in part 10");
               });

        // 6.11.9.2.c. Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.11.9.2.c");
    }

    private List<DiagnosticTroubleCode> getDTCs(int address) {
        return getDTCs(DM28PermanentEmissionDTCPacket.class, address, 10);
    }
}
