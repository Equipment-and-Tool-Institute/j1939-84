/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

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

;

/**
 * 6.7.8 DM27: All Pending DTCs
 */
public class Part07Step08Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 8;
    private static final int TOTAL_STEPS = 0;

    Part07Step08Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step08Controller(Executor executor,
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
        // 6.7.8.1.a Global DM27 [(send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM27(getListener()).getPackets();

        globalPackets.forEach(this::save);

        // 6.7.8.2.a (if supported) Fail if any OBD ECU reports a pending DTC.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.8.2.a - " + moduleName + " reported a pending DTC"));

        // 6.7.8.2.b (if supported) Fail if any ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.8.2.b - " + moduleName + " did not report MIL off"));

        // 6.7.8.3.a DS DM27 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM27(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.7.8.4.a (if supported) Fail if any difference compared to data received for global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.7.8.4.a");

        // 6.7.8.4.b (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.7.8.4.b");
    }

}
