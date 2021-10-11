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

import net.soliddesign.j1939tools.j1939.packets.ParsedPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

;

/**
 * 6.1.20 DM28: Permanent DTCs
 */
public class Part01Step20Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 20;
    private static final int TOTAL_STEPS = 0;

    Part01Step20Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step20Controller(Executor executor,
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

        // 6.1.20.1.a. Global DM28 for PGN 64896
        var globalPackets = getCommunicationsModule().requestDM28(getListener()).getPackets();

        // 6.1.20.2.a. Fail if any ECU reports permanent DTCs.
        globalPackets.stream()
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.20.2.a - " + moduleName + " reported permanent DTCs");
                     });

        // 6.1.20.2.b. Fail if any ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.20.2.b - " + moduleName + " did not report MIL off");
                     });

        // 6.1.20.2.c. Fail if no OBD ECU provides DM28.
        boolean obdModuleResponded = globalPackets.stream().anyMatch(p -> isObdModule(p.getSourceAddress()));
        if (!obdModuleResponded) {
            addFailure("6.1.20.2.c - No OBD ECU provided DM28");
        }

        // 6.1.20.3.a. DS DM28 to all OBD ECUs.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM28(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.1.20.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.1.20.4.a");

        // 6.1.20.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.1.20.4.b");
    }
}
