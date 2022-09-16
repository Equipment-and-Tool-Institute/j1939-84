/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939tools.j1939.packets.ParsedPacket.NOT_AVAILABLE;

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
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.9.9 DM21: Diagnostic Readiness 2
 */
public class Part09Step09Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 9;
    private static final int TOTAL_STEPS = 0;

    Part09Step09Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step09Controller(Executor executor,
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

        // 6.9.9.1.a. DS DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3295-3296)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM21(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // 6.9.9.2.a. Fail if any report time SCC (SPN 3296) > 0 (if supported).
        packets.stream()
               .filter(p -> {
                   double tscc = p.getMinutesSinceDTCsCleared();
                   return tscc > 0 && tscc != NOT_AVAILABLE;
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.9.2.a - " + moduleName + " reported time SCC is > 0 minutes");
               });

        // 6.9.9.2.b. Fail if any report time with MIL on (SPN 3295) > 0 (if supported).
        packets.stream()
               .filter(p -> {
                   double minWithMIL = p.getMinutesWhileMILIsActivated();
                   return minWithMIL > 0 && minWithMIL != NOT_AVAILABLE;
               })
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.9.2.b - " + moduleName + " reported time with MIL on > 0 minutes");
               });

        // 6.9.9.2.c. Fail if no OBD ECU supports DM21.
        if (packets.isEmpty()) {
            addFailure("6.9.9.2.c - No OBD ECU provided a DM21 message");
        }

        // 6.9.9.2.d. Fail if NACK not received from OBD ECUs that did not provide DM21 message.
        checkForNACKsGlobal(packets, filterAcks(dsResults), "6.9.9.2.d");
    }

}
