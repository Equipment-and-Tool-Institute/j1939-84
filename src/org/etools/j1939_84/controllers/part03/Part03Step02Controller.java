/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.bus.DM5Heartbeat;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;

/**
 * 6.3.2 DM6: Emission related pending DTCs
 */
public class Part03Step02Controller extends StepController {

    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 2;
    private static final int TOTAL_STEPS = 0;

    Part03Step02Controller() {
        this(Executors.newSingleThreadScheduledExecutor(),
             new BannerModule(),
             DateTimeModule.getInstance(),
             DataRepository.getInstance(),
             new EngineSpeedModule(),
             new VehicleInformationModule(),
             new CommunicationsModule());
    }

    Part03Step02Controller(Executor executor,
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

        int attempts = 0;
        List<DM6PendingEmissionDTCPacket> globalPackets = List.of();
        boolean foundDTC = false;
        while (!foundDTC) {
            // 6.3.2.1.a. Global DM6 (send Request (PGN 59904) for PGN 65227 (SPNs 1213-1215, 3038, 1706)).
            // 6.3.2.1.a.i. Repeat request for DM6 no more frequently than once per s until one or more ECUs reports a
            // pending DTC.
            attempts++;
            updateProgress("Step 6.3.2.1.a - Requesting DM6 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getCommunicationsModule().requestDM6(getListener()).getPackets();

            // 6.3.2.2.a. Fail if no OBD ECU supports DM6.
            boolean hasNoObdPackets = globalPackets.stream()
                                                   .map(ParsedPacket::getSourceAddress)
                                                   .noneMatch(this::isObdModule);
            if (hasNoObdPackets) {
                addFailure("6.3.2.2.a - No OBD ECU supports DM6");
                break;
            }

            foundDTC = globalPackets.stream().anyMatch(p -> !p.getDtcs().isEmpty());

            if (!foundDTC) {
                if (attempts == 5 * 60) {
                    // 6.3.2.1.a.ii. Time-out after every 5 minutes
                    // and ask user ‘yes/no’ to continue if still no pending DTC;
                    // and fail if user says 'no' and no ECU reports a pending DTC.

                    // This will throw an exception if the user chooses 'no'
                    try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {
                        displayInstructionAndWait("No ECU has reported a Pending Emission DTC." + NL + NL +
                                "Do you wish to continue?",
                                                  "No Pending Emission DTCs Found",
                                                  QUESTION);
                    }
                    attempts = 0;
                } else {
                    getDateTimeModule().pauseFor(1000);
                }
            }
        }

        // Save the DTCs per module
        globalPackets.forEach(this::save);

        // 6.3.2.3.a Warn if any ECU reports > 1 pending DTC
        globalPackets.stream()
                     .filter(p -> p.getDtcs().size() > 1)
                     .map(ParsedPacket::getSourceAddress)
                     .map(Lookup::getAddressName)
                     .forEach(moduleName -> {
                         addWarning("6.3.2.3.a - " + moduleName + " reported > 1 pending DTC");
                     });

        // 6.3.2.3.b Warn if more than one ECU reports a pending DTC.
        long modulesWithFaults = globalPackets.stream()
                                              .filter(p -> !p.getDtcs().isEmpty())
                                              .count();
        if (modulesWithFaults > 1) {
            addWarning("6.3.2.3.b - More than one ECU reported a pending DTC");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.3.2.4 DS DM6 to each OBD ECU.
        var dsResults = obdModuleAddresses.stream()
                                          .map(a -> getCommunicationsModule().requestDM6(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.3.2.5.a Fail if any difference compared to data received with global request.
        List<DM6PendingEmissionDTCPacket> dsPackets = filterRequestResultPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.3.2.5.a");

        // 6.3.2.5.b Fail if all [OBD] ECUs do not report MIL off. See section A.8 for allowed values.
        dsPackets.stream()
                 .filter(p -> getDataRepository().isObdModule(p.getSourceAddress()))
                 .filter(p -> {
                     return isNotOff(p.getMalfunctionIndicatorLampStatus());
                 })
                 .map(ParsedPacket::getSourceAddress)
                 .map(Lookup::getAddressName)
                 .forEach(moduleName -> {
                     addFailure("6.3.2.5.b - " + moduleName + " did not report MIL 'off'");
                 });

        // 6.3.2.5.c Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.3.2.5.c");
    }

}
