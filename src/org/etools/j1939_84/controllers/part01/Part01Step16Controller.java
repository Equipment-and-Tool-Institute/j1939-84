/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.1.16 DM2: Previously Active Diagnostic Trouble Codes (DTCs)
 */
public class Part01Step16Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 16;
    private static final int TOTAL_STEPS = 0;

    Part01Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new VehicleInformationModule(),
             new CommunicationsModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    protected Part01Step16Controller(Executor executor,
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

        // 6.1.16.1.a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706))
        var globalPackets = getCommunicationsModule().requestDM2(getListener()).getPackets();

        // 6.1.16.2.a Fail if any OBD ECU reports a previously active DTC.
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(DiagnosticTroubleCodePacket::hasDTCs)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.16.2.a - OBD ECU " + moduleName + " reported a previously active DTC");
                     });

        // 6.1.16.2.b Fail if any OBD ECU does not report MIL (Malfunction Indicator Lamp) off
        globalPackets.stream()
                     .filter(p -> isObdModule(p.getSourceAddress()))
                     .filter(p -> p.getMalfunctionIndicatorLampStatus() != OFF)
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.16.2.b - OBD ECU " + moduleName + " did not report MIL off");
                     });

        // 6.1.16.2.c Fail if any non-OBD ECU does not report MIL off or not supported
        globalPackets.stream()
                     .filter(p -> !isObdModule(p.getSourceAddress()))
                     .filter(p -> {
                         LampStatus milStatus = p.getMalfunctionIndicatorLampStatus();
                         return milStatus != OFF && milStatus != NOT_SUPPORTED;
                     })
                     .map(ParsedPacket::getModuleName)
                     .forEach(moduleName -> {
                         addFailure("6.1.16.2.c - Non-OBD ECU " + moduleName
                                 + " did not report MIL off or not supported");
                     });

        // 6.1.16.3.a DS DM2 to each OBD ECU
        var dsResult = getDataRepository().getObdModuleAddresses()
                                          .stream()
                                          .map(a -> getCommunicationsModule().requestDM2(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.1.16.4.a Fail if any responses differ from global responses
        compareRequestPackets(globalPackets, filterPackets(dsResult), "6.1.16.4.a");

        // 6.1.16.4.b Fail if NACK not received from OBD ECUs that did not respond to global query
        checkForNACKsGlobal(globalPackets, filterAcks(dsResult), "6.1.16.4.b");
    }
}
