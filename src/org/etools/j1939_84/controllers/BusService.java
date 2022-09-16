/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.modules.DateTimeModule;

public class BusService {

    private final J1939DaRepository j1939DaRepository;
    private final DateTimeModule dateTimeModule;
    private J1939 j1939;
    private ResultsListener listener;

    public BusService(J1939DaRepository j1939DaRepository) {
        this.j1939DaRepository = j1939DaRepository;
        dateTimeModule = DateTimeModule.getInstance();
    }

    /**
     * Returns a List of PGNs which are contained in the given list and have a
     * broadcast period greater than 0 ms.
     */
    public List<Integer> collectBroadcastPGNs(List<Integer> pgns) {
        return pgns.stream()
                   .map(j1939DaRepository::findPgnDefinition)
                   .filter(Objects::nonNull)
                   .filter(d -> d.getBroadcastPeriod() > 0)
                   .map(PgnDefinition::getId)
                   .distinct()
                   .sorted()
                   .collect(Collectors.toList());
    }

    /**
     * Returns a List of PGNs which contain all the given SPNs, if the PGNs is
     * not sent on-request
     */
    public List<Integer> collectNonOnRequestPGNs(List<Integer> spns) {
        return spns.stream()
                   .map(j1939DaRepository::getPgnForSpn)
                   .filter(Objects::nonNull)
                   .flatMap(Collection::stream)
                   .distinct()
                   .map(j1939DaRepository::findPgnDefinition)
                   .filter(Objects::nonNull)
                   .filter(pgnDef -> !pgnDef.isOnRequest())
                   .map(PgnDefinition::getId)
                   .sorted()
                   .collect(Collectors.toList());
    }

    /**
     * Sends a Destination Specific Request for the given PGN to the given
     * moduleAddress
     *
     * @param  pgn
     *                           the PGN of interest
     * @param  moduleAddress
     *                           the module address of interest
     * @return               List of Packets received
     */
    public Stream<GenericPacket> dsRequest(int pgn, int moduleAddress, String spns) {
        String message = "for PGN " + pgn + " for SPNs " + spns;
        Packet requestPacket = j1939.createRequestPacket(pgn, moduleAddress);
        return j1939.requestDS(message, pgn, requestPacket, listener)
                    .getPacket()
                    .stream()
                    .filter(p -> p.left.isPresent())
                    .map(p -> p.left.get());
    }

    /**
     * Determines the PGNs (ids) which will need to be requested.
     *
     * @param  missingSPNs
     *                           the collection of SPNs which need to be request
     * @param  supportedSPNs
     *                           the collection of SPNs which are supported by the module in
     *                           the data stream
     * @return               list of PGNs
     */
    public List<Integer> getPGNsForDSRequest(Collection<Integer> missingSPNs, Collection<Integer> supportedSPNs) {
        Stream<Integer> onRequestPGNs = j1939DaRepository.getPgnDefinitions()
                                                         .values()
                                                         .stream()
                                                         .filter(PgnDefinition::isOnRequest)
                                                         .filter(pgnDef -> pgnDef.getSpnDefinitions()
                                                                                 .stream()
                                                                                 .anyMatch(s -> supportedSPNs.contains(s.getSpnId())))
                                                         .map(PgnDefinition::getId);

        Stream<Integer> missingPGNs = j1939DaRepository.getPgnDefinitions()
                                                       .values()
                                                       .stream()
                                                       .filter(pgnDef -> pgnDef.getSpnDefinitions()
                                                                               .stream()
                                                                               .anyMatch(s -> missingSPNs.contains(s.getSpnId())))
                                                       .map(PgnDefinition::getId);

        return Stream.concat(missingPGNs, onRequestPGNs)
                     .distinct()
                     .sorted()
                     .collect(Collectors.toList());
    }

    /**
     * Sends a Request to the Global Address for the given PGN
     */
    public Stream<GenericPacket> globalRequest(int pgn, String message) {
        listener.onResult("");
        Packet requestPacket = j1939.createRequestPacket(pgn, J1939.GLOBAL_ADDR);
        RequestResult<GenericPacket> globalResult = j1939.requestGlobal(message,
                                                                        pgn,
                                                                        requestPacket,
                                                                        listener);
        return globalResult
                           .getEither()
                           .stream()
                           .flatMap(e -> e.left.stream());
    }

    /**
     * Reads the bus for the given number of seconds returning a Stream of Packets
     * found
     *
     * @param  seconds
     *                     the number of seconds to read the bus
     * @return         the Stream of GenericPackets that were received
     */
    public Stream<GenericPacket> readBus(int seconds, String section) {
        return readBus(seconds, section, genericPacket -> true);
    }

    /**
     * Reads the bus for the given number of seconds returning a Stream of Packets
     * found
     *
     * @param  seconds
     *                     the number of seconds to read the bus
     * @return         the Steam of GenericPackets that were received
     */
    public Stream<GenericPacket> readBus(int seconds, String step, Predicate<GenericPacket> filter) {
        String message = NL + "Step " + step + " - Reading bus for %1$d seconds" + NL;
        listener.onResult(String.format(message, seconds));
        long stopTime = dateTimeModule.getTimeAsLong() + seconds * 1000L;
        new Thread(() -> {
            long secondsToGo = seconds;
            while (secondsToGo > 0) {
                try {
                    secondsToGo = (stopTime - dateTimeModule.getTimeAsLong()) / 1000;
                    Controller.checkEnding();
                    listener.onProgress(String.format(message, secondsToGo));
                    dateTimeModule.pauseFor(1000);
                } catch (InterruptedException e) {
                    secondsToGo = 0;
                }
            }
        }).start();

        return j1939.read(GenericPacket.class, seconds, TimeUnit.SECONDS)
                    .flatMap(e -> e.left.stream())
                    .filter(filter);
    }

    /**
     * This sets up the dependency that are necessary at run time (but not
     * available at compile time) This *MUST* be called before interacting with
     * an instance of this class
     */
    public void setup(J1939 j1939, ResultsListener listener) {
        this.j1939 = j1939;
        this.listener = listener;
    }

}
