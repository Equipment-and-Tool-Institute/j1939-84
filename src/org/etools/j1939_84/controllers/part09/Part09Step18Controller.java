/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.9.18 DM23: Emission Related Previously Active DTCs
 */
public class Part09Step18Controller extends StepController {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 18;
    private static final int TOTAL_STEPS = 0;

    Part09Step18Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part09Step18Controller(Executor executor,
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
        // 6.9.18.1.a. DS DM23 [send Request (PGN 59904) for PGN 64949 (SPNs 1213-1215, 1706, and 3038)] to each OBD ECU
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM23(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);
        packets.forEach(this::save);

        // 6.9.18.2.a. Fail if any ECU reports a previously active DTC.
        packets.stream()
               .filter(DiagnosticTroubleCodePacket::hasDTCs)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.18.2.a - " + moduleName + " reported a previously active DTC");
               });

        // 6.9.18.2.b. Fail if any ECU does not report MIL off. See Section A.8 for allowed values.
        packets.stream()
               .filter(p -> isNotOff(p.getMalfunctionIndicatorLampStatus()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.9.18.2.b - " + moduleName + " did not report MIL 'off'");
               });

        // 6.9.18.2.c. Fail if NACK not received from OBD ECUs that did not provide DM23 message
        checkForNACKsDS(packets, filterAcks(dsResults), "6.9.18.2.c");
    }

}
