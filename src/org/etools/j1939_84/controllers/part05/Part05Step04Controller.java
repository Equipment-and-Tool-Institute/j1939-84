/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part05;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.5.4 DM28: Permanent DTCs
 */
public class Part05Step04Controller extends StepController {
    private static final int PART_NUMBER = 5;
    private static final int STEP_NUMBER = 4;
    private static final int TOTAL_STEPS = 0;

    Part05Step04Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part05Step04Controller(Executor executor,
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
        // 6.5.4.1.a DS DM28 ([send Request (PGN 59904) for PGN 64896 (SPNs 1213-1215, 3038, 1706)]) to each OBD ECU.
        var responses = getDataRepository().getObdModuleAddresses()
                                           .stream()
                                           .map(a -> getCommunicationsModule().requestDM28(getListener(), a))
                                           .map(BusResult::requestResult)
                                           .collect(Collectors.toList());

        var packets = filterRequestResultPackets(responses);

        // Save the DM28 packet for later use
        packets.forEach(this::save);

        // 6.5.4.2.a Fail if no ECU reports a permanent DTC.
        boolean noDTCs = packets.stream().allMatch(p -> p.getDtcs().isEmpty());
        if (noDTCs) {
            addFailure("6.5.4.2.a - No ECU reported a permanent DTC");
        }

        // 6.5.4.2.b Fail if permanent DTC response from the SA reporting a DM12 active DTC does not include the DM12 active DTC that the SA reported from earlier in this part.
        packets.stream()
               .filter(p -> p.getMalfunctionIndicatorLampStatus() != getDM12MilStatus(p.getSourceAddress()))
               .map(ParsedPacket::getModuleName)
               .forEach(moduleName -> {
                   addFailure("6.5.4.2.b - " + moduleName + " reported a different MIL status " +
                           "than it did for DM12 response earlier in this part");
               });

        // 6.5.4.2.c Fail if permanent DTC does not match DM12 active DTC from earlier in this part.
        packets.forEach(p -> {
            if (!p.getDtcs().containsAll(getDTCs(p.getSourceAddress()))) {
                addFailure("6.5.4.2.c - " + p.getModuleName()
                        + " DM28 does not include the DM12 active DTC that the SA reported from earlier in this part.");
            }
        });

        // 6.5.4.2.d Fail if NACK not received from OBD ECUs that did not provide a DM28 message.
        checkForNACKsDS(packets, filterRequestResultAcks(responses), "6.5.4.2.d");
    }

    private List<DiagnosticTroubleCode> getDTCs(int moduleAddress) {
        return getDTCs(DM12MILOnEmissionDTCPacket.class, moduleAddress, 5);
    }

    private LampStatus getDM12MilStatus(int moduleAddress) {
        var packet = (DiagnosticTroubleCodePacket) get(DM12MILOnEmissionDTCPacket.class, moduleAddress, 5);
        return packet == null ? null : packet.getMalfunctionIndicatorLampStatus();
    }

}
