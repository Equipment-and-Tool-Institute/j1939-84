/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part1;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.Outcome;

public class BroadcastValidator {

    private final DataRepository dataRepository;
    private final J1939DaRepository j1939DaRepository;

    BroadcastValidator(DataRepository dataRepository, J1939DaRepository j1939DaRepository) {
        this.dataRepository = dataRepository;
        this.j1939DaRepository = j1939DaRepository;
    }

    /**
     * Determines if the given packets were broadcast at their specified rates.  Adds failures/warnings to the report if they are not within spec
     */
    public void reportBroadcastPeriod(Map<Integer, List<GenericPacket>> packetMap,
                                      int moduleSourceAddress,
                                      ResultsListener listener,
                                      int partNumber,
                                      int stepNumber) {
        // b. Gather/timestamp each parameter at least three times to be able to verify frequency of broadcast.
        for (Map.Entry<Integer, List<GenericPacket>> entry : packetMap.entrySet()) {

            List<GenericPacket> samplePackets = entry.getValue();
            int pgn = entry.getKey();

            if (samplePackets.size() < 3) {
                listener.onResult("");
                samplePackets.forEach(p -> listener.onResult(p.getPacket().toTimeString()));

                addOutcome(listener,
                           partNumber,
                           stepNumber,
                           Outcome.INFO,
                           "Unable to determine period for PGN " + pgn + " from " + Lookup.getAddressName(
                                   moduleSourceAddress));
            } else {
                Packet packet0 = samplePackets.get(0).getPacket();
                Packet packet1 = samplePackets.get(1).getPacket();
                Packet packet2 = samplePackets.get(2).getPacket();

                listener.onResult("");
                listener.onResult(packet0.toTimeString());
                listener.onResult(packet1.toTimeString());
                listener.onResult(packet2.toTimeString());

                LocalDateTime t0 = packet0.getTimestamp();
                LocalDateTime t1 = packet1.getTimestamp();
                long diff1 = ChronoUnit.MILLIS.between(t0, t1);

                LocalDateTime t2 = packet2.getTimestamp();
                long diff2 = ChronoUnit.MILLIS.between(t1, t2);

                PgnDefinition pgnDefinition = j1939DaRepository.findPgnDefinition(pgn);

                long broadcastPeriod = pgnDefinition.getBroadcastPeriod();
                double maxBroadcastPeriod = broadcastPeriod * 1.1;
                double minBroadcastPeriod = broadcastPeriod * 0.9;

                // b. Fail if any parameter is not broadcast within -10% of the fixed, specified broadcast period.
                if (!pgnDefinition.isVariableBroadcast() && (diff1 < minBroadcastPeriod || diff2 < minBroadcastPeriod)) {
                    addOutcome(listener,
                               partNumber,
                               stepNumber,
                               Outcome.FAIL,
                               "Broadcast of " + pgn + " by OBD Module " + Lookup.getAddressName(moduleSourceAddress) + " is less than 90% specified broadcast period.");
                }

                // b. Fail if any parameter is not broadcast within +10% of the fixed, specified broadcast period.
                // c. Fail if any parameter in a variable period broadcast message exceeds 110% of its recommended broadcast period.
                if (diff1 > maxBroadcastPeriod || diff2 > maxBroadcastPeriod) {
                    addOutcome(listener,
                               partNumber,
                               stepNumber,
                               Outcome.FAIL,
                               "Broadcast of " + pgn + " by OBD Module " + Lookup.getAddressName(moduleSourceAddress) + " is beyond 110% specified broadcast period.");
                }

            }
        }

    }

    public Map<Integer, List<GenericPacket>> buildPGNPacketsMap(List<GenericPacket> packets, int moduleAddress) {
        TreeMap<Integer, List<GenericPacket>> foundPackets = new TreeMap<>();
        packets.stream()
                .filter(p -> p.getSourceAddress() == moduleAddress)
                .forEach(p -> foundPackets.computeIfAbsent(p.getPacket().getPgn(), k -> new ArrayList<>())
                        .add(p));
        return foundPackets;
    }

    /**
     * Loops through the give PGNs to find the one with the maximum broadcast period
     *
     * @param pgns
     *         the list of PGN ids
     * @return the maximum broadcast period in seconds
     */
    public int getMaximumBroadcastPeriod(List<Integer> pgns) {
        return pgns.stream()
                .map(j1939DaRepository::findPgnDefinition)
                .filter(Objects::nonNull)
                .mapToInt(PgnDefinition::getBroadcastPeriod)
                .filter(period -> period > 0)
                .map(period -> period / 1000)
                .max()
                .orElse(5);
    }

    /**
     * Look through the obdModules to find the the PGNs they might broadcast which has the long period
     *
     * @return the maximum period in seconds
     */
    public int getMaximumBroadcastPeriod() {
        return dataRepository.getObdModules()
                .stream()
                .flatMap(m -> m.getDataStreamSpns().stream())
                .map(SupportedSPN::getSpn)
                .map(j1939DaRepository::getPgnForSpn)
                .filter(Objects::nonNull)
                .map(j1939DaRepository::findPgnDefinition)
                .filter(Objects::nonNull)
                .mapToInt(PgnDefinition::getBroadcastPeriod)
                .filter(period -> period > 0)
                .map(period -> period / 1000)
                .max()
                .orElse(5);
    }

    @SuppressWarnings("SameParameterValue")
    private static void addOutcome(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   Outcome outcome,
                                   String message) {
        listener.addOutcome(partNumber, stepNumber, outcome, message);
        listener.onResult(outcome + ": 6." + partNumber + "." + stepNumber + " - " + message);
    }

}