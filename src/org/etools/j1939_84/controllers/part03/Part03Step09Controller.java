/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
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
 * 6.3.9 DM12: Emissions related active DTCs
 */
public class Part03Step09Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part03Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step09Controller(Executor executor,
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
        // 6.3.9.1.a Global DM12 (send Request (PGN 59904) for PGN 65236 (SPNs 1213-1215, 1706, and 3038)).
        var globalPackets = getCommunicationsModule().requestDM12(getListener()).getPackets();

        // 6.3.9.2.a Fail if any ECU reports an active DTC.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.9.2.a - " + moduleName + " reported an active DTC"));

        // 6.3.9.2.b Fail if any OBD ECU does not report MIL off. See section A.8 for allowed values
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF
                             && p.getMalfunctionIndicatorLampStatus() != ALTERNATE_OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.9.2.b - " + moduleName + " did not report MIL 'off'"));

        // 6.3.9.2.c Fail if any non-OBD ECU does not report MIL off or not supported.
        globalPackets.stream()
                     .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF
                             && p.getMalfunctionIndicatorLampStatus() != NOT_SUPPORTED)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.3.9.2.c - Non-OBD ECU " + moduleName
                             + " did not report MIL off or not supported"));

        // 6.3.9.2.d Fail if no OBD ECU provides DM12
        boolean noObdModuleResponded = globalPackets.stream()
                                                    .noneMatch(p -> getDataRepository().isObdModule(p.getSourceAddress()));
        if (noObdModuleResponded) {
            addFailure("6.3.9.2.d - No OBD ECU provided a DM12");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.3.9.3.a DS DM12 to each OBD ECU.
        var dsResults = obdModuleAddresses
                                          .stream()
                                          .map(address -> getCommunicationsModule().requestDM12(getListener(),
                                                                                                address))
                                          .collect(Collectors.toList());

        // 6.3.9.4.a Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.3.9.4.a");

        // 6.3.9.4.b Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.3.9.4.b");

    }

}
