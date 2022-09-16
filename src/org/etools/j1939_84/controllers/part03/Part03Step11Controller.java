/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
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
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.3.11 DM28: permanent DTCs
 */
public class Part03Step11Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part03Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step11Controller(Executor executor,
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
        // 6.3.11.1.a. Global DM28 (send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 3038, 1706)).
        var globalPackets = getCommunicationsModule().requestDM28(getListener()).getPackets();

        // 6.3.11.2.a. Fail if any ECU reports a permanent active DTC.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.11.2.a - " + moduleName + " reported a permanent DTC"));

        // 6.3.11.2.b. Fail if any OBD ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.11.2.b - " + moduleName + " did not report MIL off"));

        // 6.3.11.2.c. Fail if any non- OBD ECU does not report MIL off or not supported.
        globalPackets.stream()
                     .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF
                             && p.getMalfunctionIndicatorLampStatus() != NOT_SUPPORTED)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.11.2.c - Non-OBD ECU " + moduleName
                             + " did not report MIL off or not supported"));

        // 6.3.11.2.d. Fail if no OBD ECU provides DM28
        boolean noObdModuleResponded = globalPackets.stream()
                                                    .noneMatch(p -> getDataRepository().isObdModule(p.getSourceAddress()));
        if (noObdModuleResponded) {
            addFailure("6.3.11.2.d - No OBD ECU provided a DM28");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.3.11.3.a. DS DM28 to each OBD ECU.
        var dsResults = obdModuleAddresses
                                          .stream()
                                          .map(address -> getCommunicationsModule().requestDM28(getListener(),
                                                                                                address))
                                          .collect(Collectors.toList());

        // 6.3.11.4.a. Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.3.11.4.a");

        // 6.3.11.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.3.11.4.b");
    }

}
