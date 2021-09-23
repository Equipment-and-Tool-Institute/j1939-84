/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

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
 * 6.1.17 DM6: Emission related pending DTCs
 */
public class Part01Step17Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 17;
    private static final int TOTAL_STEPS = 0;

    Part01Step17Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step17Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           CommunicationsModule communicationsModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule) {
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

        // 6.1.17.1.a. Global DM6 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)).
        var globalPackets = getDiagnosticMessageModule().requestDM6(getListener()).getPackets();

        // 6.1.17.2.c. Fail if no OBD ECU provides DM6.
        if (globalPackets.isEmpty()) {
            addFailure("6.1.17.2.c - No OBD ECU provided DM6");
        } else {
            // 6.1.17.2.a. Fail if any ECU reports pending DTCs
            globalPackets.stream()
                         .filter(p -> !p.getDtcs().isEmpty())
                         .map(ParsedPacket::getModuleName)
                         .forEach(moduleName -> {
                             addFailure("6.1.17.2.a - " + moduleName + " reported pending DTCs");
                         });

            // 6.1.17.2.b. Fail if any ECU does not report MIL off.
            globalPackets.stream()
                         .filter(p -> p.getMalfunctionIndicatorLampStatus() != LampStatus.OFF)
                         .map(ParsedPacket::getModuleName)
                         .forEach(moduleName -> {
                             addFailure("6.1.17.2.b - " + moduleName + " did not report MIL off");
                         });
        }

        // 6.1.17.3.a. DS DM6 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getDiagnosticMessageModule().requestDM6(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.1.17.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResults), "6.1.17.4.a");

        // 6.1.17.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.1.17.4.b");
    }
}
