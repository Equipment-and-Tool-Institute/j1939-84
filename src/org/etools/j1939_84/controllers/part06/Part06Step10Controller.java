/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.bus.j1939.packets.ParsedPacket.NOT_AVAILABLE;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * 6.6.10 DM21: Diagnostic Readiness 2
 */
public class Part06Step10Controller extends StepController {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 10;
    private static final int TOTAL_STEPS = 0;

    Part06Step10Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part06Step10Controller(Executor executor,
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
        // 6.6.10.1.a. DS DM21 [(send Request (PGN 59904) for PGN 49408 (SPNs 3069, 3295)]) to each OBD ECU.
        var dsResults = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM21(getListener(), a))
                                           .collect(Collectors.toList());

        var packets = filterPackets(dsResults);

        // 6.6.10.2.a. Fail if any ECU reports distance with MIL on (SPN 3069) is > 0.
        packets.stream()
               .filter(p -> p.getKmWhileMILIsActivated() > 0 && p.getKmWhileMILIsActivated() != NOT_AVAILABLE)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.10.2.a - " + moduleName + " reported distance with MIL on > 0"));

        // 6.6.10.2.a. Fail if any ECU reports distance with MIL on (SPN 3069) is not supported.
        packets.stream()
               .filter(p -> p.getKmWhileMILIsActivated() == NOT_AVAILABLE)
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.10.2.a - " + moduleName
                       + " reported distance with MIL on is not supported"));

        // 6.6.10.2.b. Fail if any ECU reports time with MIL on greater than 0 minute, and did not report a DTC in its
        // DM12 response.
        packets.stream()
               .filter(p -> p.getMinutesWhileMILIsActivated() > 0)
               .filter(p -> !hasDM12DTC(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> addFailure("6.6.10.2.b - " + moduleName
                       + " reported with with MIL on > 0 minutes, and did not report a DTC in its DM12 response"));

        // 6.6.10.2.c. Fail if no ECU supports DM21.
        if (packets.isEmpty()) {
            addFailure("6.6.10.2.c - No ECU supports DM21");
        }

        // 6.6.10.2.d. Fail if NACK not received from OBD ECUs that did not provide a DM21 message.
        checkForNACKsDS(packets, filterAcks(dsResults), "6.6.10.2.d");

        // 6.6.10.3.a. Warn if no ECU reports time with MIL on (SPN 3295) greater than 0 minute.
        boolean noMilOnTime = packets.stream().noneMatch(p -> p.getMinutesWhileMILIsActivated() > 0);
        if (noMilOnTime) {
            addWarning("6.6.10.3.a - No ECU reported time with MIL on > 0 minutes");
        }

        // 6.6.10.3.b. Warn if more than one ECU reports time with MIL on > 0
        // and difference between times reported is > 1 minute.
        var min = packets.stream()
                         .mapToDouble(DM21DiagnosticReadinessPacket::getMinutesWhileMILIsActivated)
                         .filter(t -> t > 0)
                         .min()
                         .orElse(0.0);
        var max = packets.stream()
                         .mapToDouble(DM21DiagnosticReadinessPacket::getMinutesWhileMILIsActivated)
                         .filter(t -> t > 0)
                         .max()
                         .orElse(0.0);
        if (max - min > 1) {
            addWarning("6.6.10.3.b - More than one ECU reported time with MIL on > 0 and difference between the times reported is > 1 minute");
        }
    }

    private boolean hasDM12DTC(int address) {
        return !getDTCs(DM12MILOnEmissionDTCPacket.class, address, 6).isEmpty();
    }

}
