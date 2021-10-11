/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * 6.1.18 DM12: Emissions related active DTCs
 */

public class Part01Step18Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    Part01Step18Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step18Controller(Executor executor,
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

        // 6.1.18.1.a. Global DM12 for PGN 65236
        var globalPackets = getCommunicationsModule().requestDM12(getListener()).getPackets();

        // 6.1.18.2.a. Fail if any ECU reports active DTCs.
        globalPackets.stream()
                     .filter(DiagnosticTroubleCodePacket::hasDTCs)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.18.2.a - " + moduleName + " reported active DTCs");
                     });

        // 6.1.18.2.b. Fail if any ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.18.2.b - " + moduleName + " did not report MIL off");
                     });

        // 6.1.18.2.c. Fail if no OBD ECU provides DM12.
        boolean obdModuleResponded = globalPackets.stream().anyMatch(p -> isObdModule(p.getSourceAddress()));
        if (!obdModuleResponded) {
            addFailure("6.1.18.2.c - No OBD ECU provided DM12");
        }

        // 6.1.18.3.a. DS DM12 to all OBD ECUs.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM12(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.1.18.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.1.18.4.a");

        // 6.1.18.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.1.18.4.b");
    }
}
