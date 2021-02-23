/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.10 DM23: Emission related previously active DTCs
 */
public class Part03Step10Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part03Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new DiagnosticMessageModule());
    }

    Part03Step10Controller(Executor executor,
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

        // 6.3.10.1.a. Global DM23 (send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 3038, 1706)).
        var globalPackets = getDiagnosticMessageModule().requestDM23(getListener()).getPackets();

        // 6.3.10.2.a. Fail if any ECU reports a previously active DTC.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.10.2.a - " + moduleName
                             + " reported an previously active DTC"));

        // 6.3.10.2.b. Fail if any OBD ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.10.2.a - " + moduleName + " did not report MIL off"));

        // 6.3.10.2.c. Fail if any non- OBD ECU does not report MIL off or not supported.
        globalPackets.stream()
                     .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF
                             && p.getMalfunctionIndicatorLampStatus() != NOT_SUPPORTED)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.10.2.a - Non-OBD ECU " + moduleName
                             + " did not report MIL off or not supported"));

        // 6.3.10.2.d. Fail if no OBD ECU provides DM23
        boolean noObdModuleResponded = globalPackets.stream()
                                                    .noneMatch(p -> getDataRepository().isObdModule(p.getSourceAddress()));
        if (noObdModuleResponded) {
            addFailure("6.3.10.2.d - No OBD ECU provided a DM23");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.3.10.3.a. DS DM23 to each OBD ECU.
        var dsResults = obdModuleAddresses
                                          .stream()
                                          .map(address -> getDiagnosticMessageModule().requestDM23(getListener(),
                                                                                                   address))
                                          .collect(Collectors.toList());

        // 6.3.10.4.a. Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.3.10.4.a");

        // 6.3.10.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKs(globalPackets, filterAcks(dsResults), "6.3.10.4.b");
    }

}
