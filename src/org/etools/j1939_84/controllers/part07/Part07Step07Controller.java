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
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.7.7 DM6: Emission Related Pending DTCs
 */
public class Part07Step07Controller extends StepController {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 7;
    private static final int TOTAL_STEPS = 0;

    Part07Step07Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part07Step07Controller(Executor executor,
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
        // 6.7.7.1.a Global DM6 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM6(getListener()).getPackets();

        globalPackets.forEach(this::save);

        // 6.7.7.2.a Fail if any ECU reports a pending DTC.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.7.2.a - " + moduleName + " reported a pending DTC"));

        // 6.7.7.2.b Fail if any ECU does not report MIL off. See Section A.8 for allowed values.
        globalPackets.stream()
                     .filter(p -> {
                         LampStatus mil = p.getMalfunctionIndicatorLampStatus();
                         return mil != OFF && mil != LampStatus.ALTERNATE_OFF;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> addFailure("6.7.7.2.b - " + moduleName + " did not report MIL 'off'"));

        // 6.7.7.3.a DS DM6 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM6(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.7.7.4.a Fail if any difference compared to data received for global request from step 6.7.7.1.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResults), "6.7.7.4.a");

        // 6.7.7.4.b Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.7.7.4.b");
    }

}
