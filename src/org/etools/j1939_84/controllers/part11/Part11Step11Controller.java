/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;;

/**
 * 6.11.11 DM26: Diagnostic Readiness 3
 */
public class Part11Step11Controller extends StepController {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 11;
    private static final int TOTAL_STEPS = 0;

    Part11Step11Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part11Step11Controller(Executor executor,
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
        // 6.11.11.1.a. DS DM26 [(send Request (PGN 59904) for PGN 64952 (SPNs 3301-3305)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM26(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterRequestResultPackets(dsResults);

        // 6.11.11.2.a. Fail if response indicates time since engine start (SPN 3301) differs by more than ±10 seconds
        // from expected value (calculated by software using original DM26 response in this part plus accumulated time
        // since then);.
        // i.e., Fail if ABS[(Time Since Engine StartB - Time Since Engine StartA) - Delta Time] > 10 seconds.
        packets.stream()
               .filter(p -> !areTimesConsistent(p))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.11.11.2.a - " + moduleName
                           + " reported time since engine start differs by more than ±10 seconds from expected value");
               });

        // 6.11.11.2.b. Fail if NACK not received from OBD ECUs that did not provide a DM26 message.
        checkForNACKsDS(packets, filterRequestResultAcks(dsResults), "6.11.11.2.b");

        // Save Delta Engine Start
        packets.forEach(p -> {
            Double delta = getDeltaEngineStart(p);
            if (delta != null) {
                OBDModuleInformation obdModuleInformation = getDataRepository().getObdModule(p.getSourceAddress());
                obdModuleInformation.setDeltaEngineStart(delta);
                getDataRepository().putObdModule(obdModuleInformation);
            }
        });

        // 6.11.11.1.b. Record all monitor readiness this trip data (i.e., which supported monitors are complete this
        // trip or supported and not complete this trip).
        // This is out of order to prevent overwriting previous data before use.
        packets.forEach(this::save);

        // 6.11.11.1.c. Display composite status for support and enable bits for responses received from OBD ECUs.
        if (!packets.isEmpty()) {
            getListener().onResult("");
            getListener().onResult("Vehicle Composite of DM26:");
            getCompositeSystems(packets, false).stream()
                                               .map(MonitoredSystem::toString)
                                               .forEach(s -> getListener().onResult(s));
        }
    }

    private boolean areTimesConsistent(DM26TripDiagnosticReadinessPacket currentPacket) {
        final int THRESHOLD = 10; // seconds

        var previousPacket = getDM26(currentPacket.getSourceAddress());
        if (previousPacket == null) {
            return false;
        }

        var currentTime = currentPacket.getPacket().getTimestamp();
        var previousTime = previousPacket.getPacket().getTimestamp();
        var deltaTime = previousTime.until(currentTime, ChronoUnit.SECONDS);

        var tempTSES = getDeltaEngineStart(currentPacket);
        var deltaTSES = 0;
        if (tempTSES != null) {
            deltaTSES = (int) Math.round(tempTSES);
        }

        return Math.abs(deltaTime - deltaTSES) < THRESHOLD;
    }

    private Double getDeltaEngineStart(DM26TripDiagnosticReadinessPacket currentPacket) {
        var previousPacket = getDM26(currentPacket.getSourceAddress());
        if (previousPacket == null) {
            return null;
        }

        var currentTSES = currentPacket.getTimeSinceEngineStart();
        var previousTSES = previousPacket.getTimeSinceEngineStart();
        return currentTSES - previousTSES;
    }

    private DM26TripDiagnosticReadinessPacket getDM26(int address) {
        return get(DM26TripDiagnosticReadinessPacket.class, address, 11);
    }

}
