/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.3.7 DM2: Previously active diagnostic trouble codes (DTCs)
 */
public class Part03Step07Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part03Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step07Controller(Executor executor,
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
        // 6.3.7.1.a Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)).
        var globalPackets = getCommunicationsModule().requestDM2(getListener()).getPackets();

        // 6.3.7.2.a (if supported) Fail if any OBD ECU reports a previously active DTC.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.3.7.2.a - OBD ECU " + moduleName + " reported a previously active DTC");
                     });

        // 6.3.7.2.b (if supported) Fail if any OBD ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.3.7.2.b - OBD ECU " + moduleName + " did not report MIL off");
                     });

        // 6.3.7.2.c (if supported) Fail if any non-OBD ECU does not report MIL off or not supported.
        globalPackets.stream()
                     .filter(p -> !getDataRepository().isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         LampStatus milStatus = p.getMalfunctionIndicatorLampStatus();
                         return milStatus != OFF && milStatus != NOT_SUPPORTED;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.3.7.2.c - Non-OBD ECU " + moduleName
                                 + " did not report MIL off or not supported");
                     });

        // 6.3.7.3.a DS DM2 to each OBD ECU.
        var dsResult = getDataRepository().getObdModuleAddresses()
                                          .stream()
                                          .map(a -> getCommunicationsModule().requestDM2(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.3.7.4.a Fail if any difference compared to data received from global request.
        compareRequestPackets(globalPackets, filterPackets(dsResult), "6.3.7.4.a");

        // 6.3.7.4.b Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResult), "6.3.7.4.b");
    }

}
