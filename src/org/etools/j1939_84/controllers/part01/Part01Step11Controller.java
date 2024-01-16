/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939tools.j1939.packets.ParsedPacket.NOT_AVAILABLE;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

;

/**
 * 6.1.11 DM21: Diagnostic readiness 2
 */
public class Part01Step11Controller extends StepController {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part01Step11Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(),
             new EngineSpeedModule(),
             new BannerModule(),
             new CommunicationsModule(),
             new VehicleInformationModule(),
             dataRepository,
             DateTimeModule.getInstance());
    }

    Part01Step11Controller(Executor executor,
                           EngineSpeedModule engineSpeedModule,
                           BannerModule bannerModule,
                           CommunicationsModule communicationsModule,
                           VehicleInformationModule vehicleInformationModule,
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

    private static boolean isNotZero(double value) {
        return Double.valueOf(value).intValue() != 0;
    }

    @Override
    protected void run() throws Throwable {

        // 6.1.11.1.a. Global DM21 (send Request (PG 59904) for PG 49408
        List<DM21DiagnosticReadinessPacket> globalPackets = getCommunicationsModule()
                .requestDM21(getListener())
                .getPackets()
                .stream().filter(p -> p.getKmWhileMILIsActivated() != NOT_AVAILABLE ||
                            p.getKmSinceDTCsCleared() != NOT_AVAILABLE ||
                            p.getMinutesWhileMILIsActivated() != NOT_AVAILABLE ||
                        p.getMinutesSinceDTCsCleared() != NOT_AVAILABLE)
                .collect(Collectors.toList());


        // 6.1.11.2.e. Fail if no OBD ECU provides a DM21 message.
        if (globalPackets.isEmpty()) {
            addFailure("6.1.11.2.e - No OBD ECU provided a DM21 message");
        } else {
            // 6.1.11.2.a. Fail if any ECU reports distance with MIL on (SP 3069) is not zero.
            globalPackets.stream()
                    .filter(packet -> isNotZero(packet.getKmSinceDTCsCleared()))
                    .map(ParsedPacket::getModuleName)
                    .forEach(moduleName -> {
                        addFailure("6.1.11.2.a - " + moduleName
                                           + " reported distance with MIL on (SP 3069) is not zero");
                    });

            // 6.1.11.2.b. Fail if any ECU reports distance SCC (SP 3294) is not zero.
            globalPackets.stream()
                    .filter(packet -> isNotZero(packet.getKmWhileMILIsActivated()))
                    .map(ParsedPacket::getModuleName)
                    .forEach(moduleName -> {
                        addFailure("6.1.11.2.b - " + moduleName + " reported distance SCC (SP 3294) is not zero");
                    });

            // 6.1.11.2.c. Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported)
            globalPackets.stream()
                    .filter(packet -> isNotZero(packet.getMinutesWhileMILIsActivated()))
                    .map(ParsedPacket::getModuleName)
                    .forEach(moduleName -> {
                        addFailure("6.1.11.2.c - " + moduleName
                                           + " reported time with MIL on (SP 3295) is not zero");
                    });

            // 6.1.11.2.d. Fail if any ECU reports time SCC (SP 3296) > 1 minute (if supported).
            globalPackets.stream()
                    .filter(packet -> Double.valueOf(packet.getMinutesSinceDTCsCleared()).intValue() > 1)
                    .map(ParsedPacket::getModuleName)
                    .forEach(moduleName -> {
                        addFailure("6.1.11.2.d - " + moduleName + " reported time SCC (SP 3296) > 1 minute");
                    });
        }

        // 6.1.11.3.a. DS DM21 to each OBD ECU
        var dsResults = getDataRepository().getObdModuleAddresses()
                .stream()
                .map(addr -> getCommunicationsModule().requestDM21(getListener(), addr))
                .collect(Collectors.toList());

        // ignore missing responses and NACKs
        List<DM21DiagnosticReadinessPacket> dsPackets = filterPackets(dsResults);

        // 6.1.11.4.a. Fail if any ECU reports distance with MIL on (SP 3069) is not zero.
        dsPackets.stream()
                .filter(packet -> isNotZero(packet.getKmSinceDTCsCleared()))
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> {
                    addFailure("6.1.11.4.a - " + moduleName + " reported distance with MIL on (SP 3069) is not zero");
                });

        // 6.1.11.4.b. Fail if any ECU reports distance SCC (SP 3294) is not zero.
        dsPackets.stream()
                .filter(packet -> isNotZero(packet.getKmWhileMILIsActivated()))
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> {
                    addFailure("6.1.11.4.b - " + moduleName + " reported distance SCC (SP 3294) is not zero");
                });

        // 6.1.11.4.c. Fail if any ECU reports time with MIL on (SP 3295) is not zero (if supported)
        dsPackets.stream()
                .filter(packet -> isNotZero(packet.getMinutesWhileMILIsActivated()))
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> {
                    addFailure("6.1.11.4.c - " + moduleName + " reported time with MIL on (SP 3295) is not zero");
                });

        // 6.1.11.4.d. Fail if any ECU reports time SCC (SP 3296) > 1 minute (if supported).
        dsPackets.stream()
                .filter(packet -> Double.valueOf(packet.getMinutesSinceDTCsCleared()).intValue() > 1)
                .map(ParsedPacket::getModuleName)
                .forEach(moduleName -> {
                    addFailure("6.1.11.4.d - " + moduleName + " reported time SCC (SP 3296) > 1 minute");
                });

        // 6.1.11.4.e Fail if any difference compared to data received during global request.
        compareRequestPackets(globalPackets, dsPackets, "6.1.11.4.e");

        // 6.1.11.4.f Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterAcks(dsResults), "6.1.11.4.f");
    }

}
