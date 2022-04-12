/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.2.11 DM27: All Pending DTCs
 */
public class Part02Step11Controller extends StepController {

    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part02Step11Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance(),
             new CommunicationsModule());
    }

    Part02Step11Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           VehicleInformationModule vehicleInformationModule,
                           DataRepository dataRepository,
                           DateTimeModule dateTimeModule,
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
        // 6.2.11.1.a. Global DM27 (send Request (PGN 59904) for PGN 64898 (SPNs 1213-1215, 3038, 1706)).
        var globalPackets = getCommunicationsModule().requestDM27(getListener()).getPackets();

        Set<Integer> globalPacketAddresses = globalPackets.stream()
                                                          .map(ParsedPacket::getSourceAddress)
                                                          .collect(Collectors.toSet());

        // 6.2.11.2.a. (if supported) Fail if any OBD ECU that supported DM27 in step 6.1.20 fails to respond.
        getDataRepository().getObdModules()
                           .stream()
                           .filter(OBDModuleInformation::supportsDM27)
                           .map(OBDModuleInformation::getSourceAddress)
                           .filter(o -> !globalPacketAddresses.contains(o))
                           .map(Lookup::getAddressName)
                           .forEach(moduleName -> {
                               addFailure("6.2.11.2.a - " + moduleName
                                       + " supported DM27 in part 1 but failed to respond");
                           });

        // Refresh the last DM27 as this required later in Part 2
        globalPackets.forEach(this::save);

        // 6.2.11.2.b. (if supported) Fail if any OBD ECU reports an all pending DTC.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(DiagnosticTroubleCodePacket::hasDTCs)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.11.2.b - " + moduleName + " reported an all pending DTC");
                     });

        // 6.2.11.2.c. (if supported) Fail if any ECU does not report MIL off.
        globalPackets.stream()
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != LampStatus.OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.2.11.2.c - " + moduleName + " did not report MIL off");
                     });

        // 6.2.11.3.a. DS DM27 to each OBD ECU that supported DM27.
        var dsPackets = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM27(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .flatMap(r -> r.getPackets().stream())
                                           .collect(Collectors.toList());

        // 6.2.11.4.a. Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, dsPackets, "6.2.11.4.a");
    }
}
