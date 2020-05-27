/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import java.util.ArrayList;
import java.util.List;
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
        List<DM21DiagnosticReadinessPacket> globalDm21Packets = diagnosticReadinessModule
                .requestDM21Packets(getListener(), true).getPackets().stream()
                .filter(packet -> packet instanceof DM21DiagnosticReadinessPacket)
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
        // a. DS DM21 to each OBD ECU
        List<ParsedPacket> addressSpecificDM21Results = new ArrayList<>();
        dataRepository.getObdModuleAddresses().forEach(address -> {
            addressSpecificDM21Results
                    .addAll(diagnosticReadinessModule.getDM21Packets(getListener(), true, address).getPackets());
        });
        List<DM21DiagnosticReadinessPacket> addressSpecificDm21Packets = addressSpecificDM21Results.stream()
                .filter(p -> p instanceof DM21DiagnosticReadinessPacket)
                .map(p -> (DM21DiagnosticReadinessPacket) p)
                .collect(Collectors.toList());

        // 6.1.11.4 Fail criteria2:
        addressSpecificDm21Packets
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

        if (addressSpecificDm21Packets.size() > globalDm21Packets.size()) {
            results.addAll(addressSpecificDm21Packets);
            results.removeAll(globalDm21Packets);
        } else {
            results.addAll(globalDm21Packets);
            results.removeAll(addressSpecificDm21Packets);
        }

        if (!results.isEmpty()) {
            addFailure(1,
                    11,
                    "6.1.11.4.e - Fail if any responses differ from global responses");
        }

        // f. Fail if NACK not received from OBD ECUs that did not respond to global
        // query.
        addressSpecificDm21Packets.forEach(addressPacket -> globalDm21Packets
                .removeIf(globalPacket -> globalPacket.getSourceAddress() == addressPacket.getSourceAddress()));

        addressSpecificDM21Results
                .stream()
                .filter(packet -> packet instanceof AcknowledgmentPacket)
                .map(p -> (AcknowledgmentPacket) p)
                .filter(p -> p.getResponse() == Response.NACK)
                .forEach(nackPacket -> globalDm21Packets
                        .removeIf(globalPacket -> globalPacket.getSourceAddress() == nackPacket.getSourceAddress()));

        if (!globalDm21Packets.isEmpty()) {
            addFailure(1,
                    11,
                    "6.1.11.4.f - Fail if NACK not received from OBD ECUs that did not respond to global query");
        }

    }

}
