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
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.VehicleInformationModule;

/**
 * @author Garrison Garland {garrison@soliddesign.net)
 *
 */

public class Step16Controller extends StepController {

    private final DataRepository dataRepository;
    private final DTCModule dtcModule;

    Step16Controller(DataRepository dataRepository) {
        this(Executors.newSingleThreadScheduledExecutor(), new EngineSpeedModule(), new BannerModule(),
                new DateTimeModule(), new VehicleInformationModule(), new DTCModule(),
                new PartResultFactory(), dataRepository);
    }

    protected Step16Controller(Executor executor, EngineSpeedModule engineSpeedModule, BannerModule bannerModule,
            DateTimeModule dateTimeModule, VehicleInformationModule vehicleInformationModule, DTCModule dtcModule,
            PartResultFactory partResultFactory, DataRepository dataRepository) {
        super(executor, engineSpeedModule, bannerModule, dateTimeModule, vehicleInformationModule, partResultFactory);
        this.dtcModule = dtcModule;
        this.dataRepository = dataRepository;
    }

    @Override
    public String getDisplayName() {
        return "Part 1 Step 16";
    }

    @Override
    public int getStepNumber() {
        // TODO Auto-generated method stub
        return 16;
    }

    @Override
    protected int getTotalSteps() {
        return 1;
    }

    @Override
    protected void run() throws Throwable {
        /**
         * 6.1.16.1 Actions:
         *
         * a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs
         * 1213-1215, 3038, 1706)). globalDM2 =
         *
         * 6.1.16.2 Fail criteria (if supported):
         *
         * a. Fail if any OBD ECU reports a previously active DTC.
         *
         * b. Fail if any OBD ECU does not report MIL off.
         *
         * c. Fail if any non-OBD ECU does not report MIL off or not supported.
         *
         * 6.1.16.3 Actions2:
         *
         * a. DS DM2 to each OBD ECU.
         *
         * 6.1.16.4 Fail criteria2 (if supported):
         *
         * a. Fail if any responses differ from global responses.
         *
         * a. Fail if NACK not received from OBD ECUs that did not respond to
         * global query.
         */

        dtcModule.setJ1939(getJ1939());
        // 6.1.16.1 Actions:
        // a. Global DM2 (send Request (PGN 59904) for PGN 65227 (SPNs
        // 1213-1215, 3038,
        // 1706)).
        RequestResult<ParsedPacket> globalDiagnosticTroubleCodePackets = dtcModule.requestDM2(getListener(), true);

        // Get DM2PrevisoulyActiveDTC so we can get DTCs and report accordingly
        List<DM2PreviouslyActiveDTC> globalDM2s = globalDiagnosticTroubleCodePackets.getPackets().stream()
                .filter(p -> p instanceof DM2PreviouslyActiveDTC)
                .map(p -> (DM2PreviouslyActiveDTC) p)
                .collect(Collectors.toList());

        // 6.1.16.2.a Fail if any OBD ECU reports a previously active DTC.
        if (globalDM2s.stream().flatMap(packet -> packet.getDtcs().stream()).findAny().isPresent()) {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.2.a - OBD ECU reported a previously active DTC");
        }

        // 6.1.16.2.b Fail if any OBD ECU does not report MIL (Malfunction
        // Indicator
        // Lamp) off
        if (globalDM2s.stream().filter(packet -> packet.getMalfunctionIndicatorLampStatus() != LampStatus.OFF).findAny()
                .isPresent()) {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.2.b - OBD ECU does not report MIL off");
        }

        // 6.1.16.2.c Fail if any non-OBD ECU does not report MIL off or not
        // supported -
        // LampStatus of OTHER
        Set<Integer> obdModuleAddresses = dataRepository.getObdModuleAddresses();
        if (globalDM2s.stream().filter(p -> !obdModuleAddresses.contains(p.getSourceAddress())
                && (p.getMalfunctionIndicatorLampStatus() != LampStatus.OFF
                        && p.getMalfunctionIndicatorLampStatus() != LampStatus.OTHER))
                .findAny().isPresent()) {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported");
        }

        // 6.1.16.3.a DS DM2 to each OBD ECU
        List<ParsedPacket> dsDM2s = new ArrayList<>();
        obdModuleAddresses.stream()
                .forEach(address -> dsDM2s.addAll(dtcModule.requestDM2(getListener(), true, address).getPackets()));

        List<ParsedPacket> unmatchedPackets = globalDiagnosticTroubleCodePackets.getPackets().stream()
                .filter(aObject -> (!verifyPacketsEquality(dsDM2s, aObject))).collect(Collectors.toList());

        // 6.1.16.4.a Fail if any responses differ from global responses
        if (!unmatchedPackets.isEmpty()) {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.4.a DS DM2 responses differ from global responses");
        }

        // 6.1.16.4.b Fail if NACK not received from OBD ECUs that did not
        // respond to
        // global query
        boolean missingNack = unmatchedPackets.stream()
                .anyMatch(packet -> packet instanceof AcknowledgmentPacket
                        && ((AcknowledgmentPacket) packet).getResponse() != Response.NACK);
        if (missingNack) {
            getListener().addOutcome(1,
                    16,
                    Outcome.FAIL,
                    "6.1.16.4.b Nack not received from OBD ECUs that did not respond to global query");
        }
    }

    private boolean verifyPacketsEquality(List<ParsedPacket> packets, ParsedPacket packet) {
        boolean found = false;
        for (ParsedPacket p : packets) {
            if (p.getSourceAddress() == packet.getSourceAddress() &&
            // This is equivalent to comparing PGN
                    p.getClass() == packet.getClass()) {
                found = true;
            }
        }
        return found;
    }
}