/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.solidDesign.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.14 DM27: All Pending DTCs
 */
public class Part09Step14Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 14;
    private static final int TOTAL_STEPS = 0;

    Part09Step14Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step14Controller(Executor executor,
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
        // 6.9.14.1.a. Global DM27 [(send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM27(getListener()).getPackets();

        // 6.9.14.2.a. (if supported) Fail if any OBD ECU reports a pending DTC.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> !p.getDtcs().isEmpty())
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.9.14.2.a - " + moduleName + " reported a pending DTC");
                     });

        // 6.9.14.3.a. DS DM27 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM27(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.9.14.4.a. (if supported) Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterPackets(dsResults), "6.9.14.4.a");

        // 6.9.14.4.b. (if supported) Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.9.14.4.b");
    }

}
