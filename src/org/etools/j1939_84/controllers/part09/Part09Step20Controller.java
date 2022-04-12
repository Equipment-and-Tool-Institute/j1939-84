/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.20 DM6: Emission Related Pending DTCs
 */
public class Part09Step20Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 20;
    private static final int TOTAL_STEPS = 0;

    Part09Step20Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step20Controller(Executor executor,
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
        // 6.9.20.1.a. Global DM6 [(send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 1706, and 3038)]).
        var globalPackets = getCommunicationsModule().requestDM6(getListener()).getPackets();

        // 6.9.20.2.a. Fail if any ECU reports a pending DTC.
        globalPackets.stream()
                     .filter(DiagnosticTroubleCodePacket::hasDTCs)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.9.20.2.a - " + moduleName + " reported a pending DTC");
                     });

        // 6.9.20.2.b. Fail if no OBD ECU provides a DM6 message.
        boolean noDM6 = globalPackets.stream().filter(p -> isObdModule(p.getSourceAddress())).findAny().isEmpty();
        if (noDM6) {
            addFailure("6.9.20.2.b - No OBD ECU provided a DM6 message");
        }

        // 6.9.20.3.a. DS DM6 to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM6(getListener(), a))
                                           .collect(Collectors.toList());

        // 6.9.20.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, filterRequestResultPackets(dsResults), "6.9.20.4.a");

        // 6.9.20.4.b. Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsDS(globalPackets, filterRequestResultAcks(dsResults), "6.9.20.4.b");
    }

}
