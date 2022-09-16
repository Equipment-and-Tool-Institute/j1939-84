/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.Lookup.getAddressName;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.model.Outcome;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;

public class BroadcastValidator {

    private final DataRepository dataRepository;
    private final J1939DaRepository j1939DaRepository;

    public BroadcastValidator(DataRepository dataRepository, J1939DaRepository j1939DaRepository) {
        this.dataRepository = dataRepository;
        this.j1939DaRepository = j1939DaRepository;
    }

    private static void addOutcome(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   Outcome outcome,
                                   String message) {
        String msg = "6." + partNumber + "." + stepNumber + " - " + message;
        listener.addOutcome(partNumber, stepNumber, outcome, msg);
    }

    private static void addWarning(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   String section,
                                   String message) {
        listener.addOutcome(partNumber, stepNumber, WARN, section + " - " + message);
    }

    private static void addFailure(ResultsListener listener,
                                   int partNumber,
                                   int stepNumber,
                                   String section,
                                   String message) {
        listener.addOutcome(partNumber, stepNumber, FAIL, section + " - " + message);
    }

    private static Set<Integer> collectNotAvailableSPNs(List<Integer> requiredSpns,
                                                        Stream<GenericPacket> packetStream) {
        return packetStream
                           .flatMap(p -> p.getSpns().stream())
                           .filter(Spn::isNotAvailable)
                           .map(Spn::getId)
                           .filter(requiredSpns::contains)
                           .collect(Collectors.toSet());
    }

    /**
     * Map of PGN to (Map of Source Address to List of Packets)
     */
    public Map<Integer, Map<Integer, List<GenericPacket>>> buildPGNPacketsMap(List<GenericPacket> packets) {
        Map<Integer, Map<Integer, List<GenericPacket>>> foundPackets = new TreeMap<>();
        packets.forEach(p -> {
            int pgn = p.getPacket().getPgn();
            int sourceAddress = p.getSourceAddress();

            Map<Integer, List<GenericPacket>> pgnPackets = foundPackets.getOrDefault(pgn, new TreeMap<>());
            List<GenericPacket> modulePackets = pgnPackets.getOrDefault(sourceAddress, new ArrayList<>());
            modulePackets.add(p);
            pgnPackets.put(sourceAddress, modulePackets);
            foundPackets.put(pgn, pgnPackets);
        });
        return foundPackets;
    }

    /**
     * Look through the obdModules to find the the PGNs they might broadcast
     * which has the long period
     *
     * @return the maximum period in seconds
     */
    public int getMaximumBroadcastPeriod() {
        int maxFromData = dataRepository.getObdModules()
                                        .stream()
                                        .flatMap(m -> m.getFilteredDataStreamSPNs().stream())
                                        .map(SupportedSPN::getSpn)
                                        .map(j1939DaRepository::getPgnForSpn)
                                        .filter(Objects::nonNull)
                                        .flatMap(Collection::stream)
                                        .map(j1939DaRepository::findPgnDefinition)
                                        .filter(Objects::nonNull)
                                        .mapToInt(PgnDefinition::getBroadcastPeriod)
                                        .filter(period -> period > 0)
                                        .map(period -> period / 1000)
                                        .max()
                                        .orElse(5);
        return Math.max(maxFromData, 5);
    }

    /**
     * Loops through the give PGNs to find the one with the maximum broadcast
     * period
     *
     * @param  pgns
     *                  the list of PGN ids
     * @return      the maximum broadcast period in seconds
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
     * Determines if the given packets were broadcast at their specified rates.
     * Adds failures/warnings to the report if they are not within spec
     */
    public void reportBroadcastPeriod(Map<Integer, Map<Integer, List<GenericPacket>>> packetMap,
                                      List<Integer> supportedSPNs,
                                      ResultsListener listener,
                                      int partNumber,
                                      int stepNumber) {

        // b. Gather/timestamp each parameter at least three times to be able to
        // verify frequency of broadcast.
        packetMap.keySet().stream().sorted().forEach(pgn -> {
            PgnDefinition pgnDefinition = j1939DaRepository.findPgnDefinition(pgn);
            boolean isSupported = pgnDefinition.getSpnDefinitions()
                                               .stream()
                                               .map(SpnDefinition::getSpnId)
                                               .anyMatch(supportedSPNs::contains);
            boolean isOnRequest = pgnDefinition.getBroadcastPeriod() <= 0;

            if (!isOnRequest && isSupported) {
                Map<Integer, List<GenericPacket>> pgnPackets = packetMap.get(pgn);

                pgnPackets.keySet().stream().sorted().forEach(moduleAddress -> {
                    String moduleName = getAddressName(moduleAddress);
                    List<GenericPacket> samplePackets = pgnPackets.get(moduleAddress);

                    if (samplePackets.size() < 3) {
                        listener.onResult("");
                        listener.onResult("PGN " + pgn + " from " + moduleName);
                        samplePackets.forEach(p -> listener.onResult(p.getPacket().toTimeString()));
                        addOutcome(listener,
                                   partNumber,
                                   stepNumber,
                                   INFO,
                                   "Unable to determine period for PGN " + pgn + " from " + moduleName);
                    } else {
                        Packet packet0 = samplePackets.get(0).getPacket();
                        Packet packet1 = samplePackets.get(1).getPacket();
                        Packet packet2 = samplePackets.get(2).getPacket();

                        listener.onResult("");
                        listener.onResult("PGN " + pgn + " from " + moduleName);
                        listener.onResult(packet0.toTimeString());
                        listener.onResult(packet1.toTimeString());
                        listener.onResult(packet2.toTimeString());

                        LocalDateTime t0 = packet0.getTimestamp();
                        LocalDateTime t1 = packet1.getTimestamp();
                        long diff1 = ChronoUnit.MILLIS.between(t0, t1);

                        LocalDateTime t2 = packet2.getTimestamp();
                        long diff2 = ChronoUnit.MILLIS.between(t1, t2);

                        long broadcastPeriod = pgnDefinition.getBroadcastPeriod();
                        double maxBroadcastPeriod = broadcastPeriod * 1.1;
                        double minBroadcastPeriod = broadcastPeriod * 0.9;

                        // b. Fail if any parameter is not broadcast within -10% of the fixed, specified broadcast
                        // period.
                        if (!pgnDefinition.isVariableBroadcast()
                                && (diff1 < minBroadcastPeriod || diff2 < minBroadcastPeriod)) {
                            long diff = Math.min(diff1, diff2);
                            addOutcome(listener,
                                       partNumber,
                                       stepNumber,
                                       FAIL,
                                       "Broadcast period of PGN " + pgn + " (" + diff + " ms) by ECU " + moduleName
                                               + " is less than 90% specified broadcast period of " + broadcastPeriod
                                               + " ms.");
                        }

                        // b. Fail if any parameter is not broadcast within +10% of the
                        // fixed, specified broadcast period.
                        // c. Fail if any parameter in a variable period broadcast
                        // message exceeds 110% of its recommended broadcast period.
                        if (diff1 > maxBroadcastPeriod || diff2 > maxBroadcastPeriod) {
                            long diff = Math.max(diff1, diff2);
                            addOutcome(listener,
                                       partNumber,
                                       stepNumber,
                                       FAIL,
                                       "Broadcast period of PGN " + pgn + " (" + diff + " ms) by ECU " + moduleName
                                               + " is beyond 110% specified broadcast period of " + broadcastPeriod
                                               + " ms.");
                        }
                    }
                });
            }
        });

    }

    /**
     * Reports if the given PGN was not received or if any supported SPNs were
     * received as Not Available
     *
     * @param  supportedSPNs
     *                           the list Supported SPNs
     * @param  pgn
     *                           the PGN of interest
     * @param  packets
     *                           the packet that may contain the PGN
     * @param  moduleAddress
     *                           the module address of concern, can be null for Global messages
     * @return               true if the given PGN wasn't received or any supported SPN is Not
     *                       Available
     */
    public List<String> collectAndReportNotAvailableSPNs(List<Integer> supportedSPNs,
                                                         int pgn,
                                                         List<GenericPacket> packets,
                                                         Integer moduleAddress,
                                                         ResultsListener listener,
                                                         int partNumber,
                                                         int stepNumber,
                                                         String section) {
        var nacks = packets.stream()
                .filter(p -> p instanceof AcknowledgmentPacket)
                .collect(Collectors.toList());
        if(!nacks.isEmpty()){
            addWarning(listener, partNumber, stepNumber, section, "NACK received for SP indicated as supported by the OBD ECU in DM24");
        }

        Set<Integer> spns;

        if (packets.isEmpty()) {
            String message;
            if (moduleAddress != null) {
                message = "No DS response for PGN " + pgn + " from " + getAddressName(moduleAddress);
            } else {
                message = "No Global response for PGN " + pgn;
            }
            addFailure(listener, partNumber, stepNumber, section, message);

            spns = j1939DaRepository.findPgnDefinition(pgn)
                                    .getSpnDefinitions()
                                    .stream()
                                    .map(SpnDefinition::getSpnId)
                                    .filter(supportedSPNs::contains)
                                    .collect(Collectors.toSet());
        } else {
            spns = collectNotAvailableSPNs(supportedSPNs, packets.stream());
        }
        return spns.stream().sorted().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * Reports the PGNs there are supported by the module but not received and
     * the SPNs that were received by broadcast as Not Available
     *
     * @param  moduleSourceAddress
     *                                 the module source address
     * @param  foundPackets
     *                                 the Map of PGNs to the List of those packets sent by the
     *                                 module
     * @param  supportedSPNs
     *                                 the list of SPNs that are still of concern
     * @return                     the List of SPNs which were not found
     */
    public List<Integer> collectAndReportNotAvailableSPNs(int moduleSourceAddress,
                                                          List<GenericPacket> foundPackets,
                                                          List<Integer> supportedSPNs,
                                                          List<Integer> requiredPgns,
                                                          ResultsListener listener,
                                                          int partNumber,
                                                          int stepNumber,
                                                          String section) {

        List<Integer> missingSpns = new ArrayList<>();

        Set<Integer> foundPGNs = foundPackets.stream().map(p -> p.getPacket().getPgn()).collect(Collectors.toSet());
        requiredPgns.removeAll(foundPGNs);
        if (!requiredPgns.isEmpty()) {
            // Expected PGNs were not received.
            // Add those SPNs to the missingSpns List and create a list of them for the report
            // a. Fail if not received for any broadcast SPN indicated as supported by the OBD ECU in DM24
            // with the Source Address matching the received message) in DM24.
            listener.onResult("");
            requiredPgns.stream()
                        .map(j1939DaRepository::findPgnDefinition)
                        .flatMap(pgnDef -> pgnDef.getSpnDefinitions().stream())
                        .map(SpnDefinition::getSpnId)
                        .filter(supportedSPNs::contains)
                        .distinct()
                        .sorted()
                        .peek(missingSpns::add)
                        .map(spn -> "SPN " + spn + " was not broadcast by " + getAddressName(moduleSourceAddress))
                        .forEach(message -> addFailure(listener, partNumber, stepNumber, section, message));
        }

        // Find any Supported SPNs which has a value of Not Available
        missingSpns.addAll(collectNotAvailableSPNs(supportedSPNs, foundPackets.stream()));

        return missingSpns;
    }

}
