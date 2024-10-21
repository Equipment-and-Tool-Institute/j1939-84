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
        int expectedTwoTripA = getDataRepository().getVehicleInformation().getTwoTripFaultACount();
        int foundDTCCount = 0;
        boolean hasNoObdPackets = false;

        while (foundDTCCount < expectedTwoTripA) {
            // 6.3.2.1.a. Global DM6 (send Request (PGN 59904) for PGN 65231 (SPNs 1213-1215, 3038, 1706)).
            // 6.3.2.1.a.i. Repeat request for DM6 no more frequently than once per second, until the expected
            // number of pending two trip faults are observed in DM6 responses

            //(Count the number of DTCs returned in DM6 responses until the count is equal or greater than
            // the Total number of fault A faults implanted less the number of one trip faults implanted.

            attempts++;
            updateProgress("Step 6.3.2.1.a - Requesting DM6 Attempt " + attempts);

            getListener().onResult(NL + "Attempt " + attempts);
            globalPackets = getCommunicationsModule().requestDM6(getListener()).getPackets();

            // 6.3.2.2.a. Fail if no OBD ECU supports DM6
            hasNoObdPackets = globalPackets.stream()
                                                   .map(ParsedPacket::getSourceAddress)
                                                   .noneMatch(this::isObdModule);

            if (hasNoObdPackets) {
                addFailure("6.3.2.2.a - No OBD ECU supports DM6");
                break;
            }

            foundDTCCount = globalPackets.stream().mapToInt(p -> p.getDtcs().size()).sum();

            if (foundDTCCount < expectedTwoTripA) {
                if (attempts == 5 * 60) {
                    // 6.3.2.1.a.ii Time-out every 5 minutes and ask user “yes/no” to continue,
                    // if the number of returned DM6 (pending) DTCs is
                    // less than the expected number of two trip Fault A DTCs;
                    // and fail if user says “no” and fewer DM6 DTCs were
                    // reported than the expected number of two trip Fault A DTCs

                    // This will throw an exception if the user chooses 'no'
                    try (var dm5 = DM5Heartbeat.run(getJ1939(), getListener())) {
                        displayInstructionAndWait("Fewer Pending Emission DTCs have been reported than the expected number of two trip Fault A DTCs." + NL + NL +
                                "Do you wish to continue?",
                                                  "Fewer Than Expected Pending Emission DTCs Found",
                                                  QUESTION);
                    }
                    attempts = 0;
                } else {
                    getDateTimeModule().pauseFor(1000);
                }
            }
        }

        // 6.3.2.2.a. Fail if no OBD ECU supports DM6, or if fewer DM6 DTCs are reported
        // than the expected number of two trip Fault A DTCs
        if (!hasNoObdPackets && foundDTCCount < expectedTwoTripA) {
            addFailure("6.3.2.2.a - Fewer DM6 DTCs were reported than the expected number of two trip Fault A DTCs (" + foundDTCCount + "/" + expectedTwoTripA + ")");
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

        // 6.3.2.3.b Info if more than one ECU reports a pending DTC.
        long modulesWithFaults = globalPackets.stream()
                                              .filter(p -> !p.getDtcs().isEmpty())
                                              .count();
        if (modulesWithFaults > 1) {
            addInfo("6.3.2.3.b - More than one ECU reported a pending DTC");
        }

        List<Integer> obdModuleAddresses = getDataRepository().getObdModuleAddresses();

        // 6.3.2.4 DS DM6 to each OBD ECU.
        var dsResults = obdModuleAddresses.stream()
                                          .map(a -> getCommunicationsModule().requestDM6(getListener(), a))
                                          .collect(Collectors.toList());

        // 6.3.2.5.a Fail if any difference compared to data received with global request.
        List<DM6PendingEmissionDTCPacket> dsPackets = filterRequestResultPackets(dsResults);
        compareRequestPackets(globalPackets, dsPackets, "6.3.2.5.a");

        // 6.3.2.5.b Fail if all [OBD] ECUs do not report MIL off,
        // where the expected number of one trip Fault A DTCs is zero.
        // See section A.8 for allowed values.
        if (getDataRepository().getVehicleInformation().getOneTripFaultACount() == 0) {
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
        }

        // 6.3.2.5.c Fail if NACK not received from OBD ECUs that did not respond to global query.
        checkForNACKsGlobal(globalPackets, filterRequestResultAcks(dsResults), "6.3.2.5.c");
    }

}
