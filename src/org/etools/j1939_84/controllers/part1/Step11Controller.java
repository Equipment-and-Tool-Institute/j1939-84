/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.Controller;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 *         The controller for 6.1.11 DM21: Diagnostic readiness 2
 */

public class Step11Controller extends Controller {

    private final DataRepository dataRepository;
    private final DiagnosticReadinessModule diagnosticReadinessModule;

    Step11Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new DiagnosticReadinessModule(), new VehicleInformationModule(),
                new PartResultFactory(), dataRepository);
    }

    Step11Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, DiagnosticReadinessModule diagnositcReadinessModule,
            VehicleInformationModule vehicleInformationModule, PartResultFactory partResultFactory,
            DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        diagnosticReadinessModule = diagnositcReadinessModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 11";
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        diagnosticReadinessModule.setJ1939(getJ1939());
        // 6.1.11.1 Actions:
        // a. Global DM21 (send Request (PGN 59904) for PGN 49408 (SPNs 3069,
        // 3294-3296)).
        List<ParsedPacket> parsedPackets = diagnosticReadinessModule.requestDM21Packets(getListener(), true);

        List<DM21DiagnosticReadinessPacket> globalDm21Packets = parsedPackets.stream()
                .filter(p -> p instanceof DM21DiagnosticReadinessPacket)
                .map(p -> (DM21DiagnosticReadinessPacket) p)
                .collect(Collectors.toList());

        // 6.1.11.2 Fail criteria:
        globalDm21Packets.forEach(packet -> {
            // a. Fail if any ECU reports distance with MIL on (SPN 3069) is not zero.
            if (packet.getKmSinceDTCsCleared() != 0 || packet.getMilesSinceDTCsCleared() != 0) {
                addFailure(1,
                        11,
                        "6.1.11.1.a - Fail if any ECU reports distance with MIL on (SPN 3069) is not zero");
            }
            // b. Fail if any ECU reports distance SCC (SPN 3294) is not zero.
            if (packet.getKmWhileMILIsActivated() != 0 || packet.getMilesWhileMILIsActivated() != 0) {
                addFailure(1,
                        11,
                        "6.1.11.1.b - Fail if any ECU reports distance SCC (SPN 3294) is not zero");
            }
            // c. Fail if any ECU reports time with MIL on (SPN 3295) is not zero (if
            // supported).17
            if (packet.getMinutesWhileMILIsActivated() != 0) {
                addFailure(1,
                        11,
                        "6.1.11.1.c - Fail if any ECU reports time with MIL on (SPN 3295) is not zero (if supported)");
            }
            // d. Fail if any ECU reports time SCC (SPN 3296) > 1 minute (if supported).
            if (packet.getMinutesSinceDTCsCleared() > 1) {
                addFailure(1,
                        11,
                        "6.1.11.1.d - Fail if any ECU reports time SCC (SPN 3296) > 1 minute (if supported)");
            }
        });
        // e. Fail if no OBD ECU provides a DM21 message.
        if (globalDm21Packets.isEmpty()) {
            addFailure(1, 11, "6.1.11.1.e - Fail if no OBD ECU provides a DM21 message");
        }

        // 6.1.11.3 Actions2:
        Set<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();

        List<ParsedPacket> addressSpecificDM21packets = new ArrayList<>();

        // a. DS DM21 to each OBD ECU
        obdModuleAddresses.forEach(address -> {
            List<ParsedPacket> dm21packets = diagnosticReadinessModule
                    .getDM21Packets(getListener(), true, address);
            if (dm21packets != null) {
                addressSpecificDM21packets.addAll(dm21packets);
            }

        });

        // 6.1.11.4 Fail criteria2:
        addressSpecificDM21packets.stream()
                .filter(p -> p instanceof DM21DiagnosticReadinessPacket)
                .map(p -> (DM21DiagnosticReadinessPacket) p)
                .forEach(packet -> {
                    // a. Fail if any ECU reports distance with MIL on (SPN 3069) is not zero.
                    if (packet.getKmSinceDTCsCleared() != 0 || packet.getMilesSinceDTCsCleared() != 0) {
                        addFailure(1,
                                11,
                                "6.1.11.4.a - Fail if any ECU reports distance with MIL on (SPN 3069) is not zero");
                    }
                    // b. Fail if any ECU reports distance SCC (SPN 3294) is not zero.
                    if (packet.getKmWhileMILIsActivated() != 0 || packet.getMilesWhileMILIsActivated() != 0) {
                        addFailure(1,
                                11,
                                "6.1.11.4.b. Fail if any ECU reports distance SCC (SPN 3294) is not zero");
                    }
                    // c. Fail if any ECU reports time with MIL on (SPN 3295) is not zero (if
                    // supported)
                    if (packet.getMinutesWhileMILIsActivated() != 0) {
                        addFailure(1,
                                11,
                                "6.1.11.4.c - Fail if any ECU reports time with MIL on (SPN 3295) is not zero (if supported)");
                    }
                    // d. Fail if any ECU reports time SCC (SPN 3296) > 1 minute (if supported).
                    if (packet.getMinutesSinceDTCsCleared() != 0) {
                        addFailure(1,
                                11,
                                "6.1.11.4.d - Fail if any ECU reports time SCC (SPN 3296) > 1 minute (if supported)");
                    }
                });

        // e. Fail if any responses differ from global responses.
        List<DM21DiagnosticReadinessPacket> results = new ArrayList<>();

        if (addressSpecificDM21packets.size() > globalDm21Packets.size()) {
            results.addAll(addressSpecificDM21packets.stream()
                    .filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
                    .map(p -> (DM21DiagnosticReadinessPacket) p).collect(Collectors.toList()));
            results.removeAll(
                    globalDm21Packets.stream().filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
                            .map(p -> p).collect(Collectors.toList()));

        } else {
            results.addAll(globalDm21Packets.stream()
                    .filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
                    .map(p -> p).collect(Collectors.toList()));
            results.removeAll(
                    addressSpecificDM21packets.stream()
                            .filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
                            .map(p -> p).collect(Collectors.toList()));

        }
        if (!results.isEmpty()) {
            addFailure(1,
                    11,
                    "6.1.11.4.e - Fail if any responses differ from global responses");
        }

        // f. Fail if NACK not received from OBD ECUs that did not respond to global
        // query.
        // Basically, sent global request. Did all the modules respond that responded to
        // the global request
        // If not, did the module(s) that didn't respond send a NACK? If not, add
        // failure

        // First get the list of modules from the global request
        List<DM21DiagnosticReadinessPacket> globalPackets = parsedPackets.stream()
                .filter(p -> p instanceof DM21DiagnosticReadinessPacket)
                .map(packet -> (DM21DiagnosticReadinessPacket) packet)
                .collect(Collectors.toList());

        // Get the list of address specific DM21 module responses
        List<ParsedPacket> addressSpecifiPackets = addressSpecificDM21packets.stream()
                .filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
                .map(p -> p)
                .collect(Collectors.toList());

        // filter from the global requests any that we got a response from when asking
        // individually
        addressSpecifiPackets.forEach(addressPacket -> globalPackets
                .removeIf(globalPacket -> globalPacket.getSourceAddress() == addressPacket.getSourceAddress()));

        // Now get a list of the NACKs
        List<ParsedPacket> nackPackets = addressSpecificDM21packets.stream()
                .filter(packet -> packet instanceof AcknowledgmentPacket
                        && ((AcknowledgmentPacket) packet).getResponse() == Response.NACK)
                .map(p -> p)
                .collect(Collectors.toList());

        // filter any that returned a NACK
        nackPackets.forEach(nackPacket -> globalPackets
                .removeIf(globalPacket -> globalPacket.getSourceAddress() == nackPacket.getSourceAddress()));

        if (!globalPackets.isEmpty()) {
            addFailure(1,
                    11,
                    "6.1.11.4.f - Fail if NACK not received from OBD ECUs that did not respond to global query");
        }

    }

}
