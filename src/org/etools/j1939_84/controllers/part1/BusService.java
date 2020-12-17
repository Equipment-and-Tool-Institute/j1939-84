/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part1;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

public class BusService {

    private J1939 j1939;

    private final J1939DaRepository j1939DaRepository;
    private ResultsListener listener;

    BusService(J1939DaRepository j1939DaRepository) {
        this.j1939DaRepository = j1939DaRepository;
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
     * @param pgn
     *            the PGN of interest
     * @param moduleAddress
     *            the module address of interest
     * @return List of Packets received
     */
    public List<GenericPacket> dsRequest(int pgn, int moduleAddress) {
        listener.onResult(J1939_84.NL);
        String title = "DS Request for " + pgn + " to " + Lookup.getAddressName(moduleAddress);
        Packet requestPacket = j1939.createRequestPacket(pgn, moduleAddress);
        return j1939.requestDS(title,
                listener,
                true,
                GenericPacket.class,
                pgn,
                requestPacket)
                .getPacket()
                .stream()
                .filter(p -> p.left.isPresent())
                .map(p -> p.left.get())
                .collect(Collectors.toList());
    }

    /**
     * Determines the PGNs (ids) which will need to be requested.
     *
     * @param missingSpns
     *            the collection of SPNs which need to be request
     * @param supportedSpns
     *            the collection of SPNs which are supported by the module in
     *            the data stream
     * @return list of PGNs
     */
    public List<Integer> getPgnsForDSRequest(Collection<Integer> missingSpns, Collection<Integer> supportedSpns) {
        Stream<Integer> onRequestPgns = j1939DaRepository.getPgnDefinitions()
                .values()
                .stream()
                .filter(PgnDefinition::isOnRequest)
                .filter(pgnDef -> pgnDef.getSpnDefinitions().stream()
                        .anyMatch(s -> supportedSpns.contains(s.getSpnId())))
                .map(PgnDefinition::getId);

        Stream<Integer> missingPgns = j1939DaRepository.getPgnDefinitions()
                .values()
                .stream()
                .filter(pgnDef -> pgnDef.getSpnDefinitions().stream().anyMatch(s -> missingSpns.contains(s.getSpnId())))
                .map(PgnDefinition::getId);

        return Stream.concat(missingPgns, onRequestPgns)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Sends a Request to the Global Address for the given PGN
     */
    public List<GenericPacket> globalRequest(int pgn) {
        listener.onResult(J1939_84.NL);
        String title = "Global Request for " + pgn;
        Packet requestPacket = j1939.createRequestPacket(pgn, J1939.GLOBAL_ADDR);
        RequestResult<GenericPacket> globalResult = j1939.requestGlobal(title,
                listener,
                true,
                GenericPacket.class,
                pgn,
                requestPacket);
        return globalResult
                .getEither()
                .stream()
                .flatMap(e -> e.left.stream())
                .collect(Collectors.toList());
    }

    /**
     * Reads the bus for the given number of seconds returning a List of Packets
     * found
     *
     * @param seconds
     *            the number of seconds to read the bus
     * @return the List of GenericPackets that were received
     */
    public List<GenericPacket> readBus(int seconds) {
        return readBus(seconds, genericPacket -> true);
    }

    /**
     * Reads the bus for the given number of seconds returning a List of Packets
     * found
     *
     * @param seconds
     *            the number of seconds to read the bus
     * @return the List of GenericPackets that were received
     */
    public List<GenericPacket> readBus(int seconds, Predicate<GenericPacket> filter) {
        listener.onResult("Reading bus for " + seconds + " seconds");
        long stopTime = System.currentTimeMillis() + seconds * 1000L;
        new Thread(() -> {
            long secondsToGo = seconds;
            while (secondsToGo > 0) {
                secondsToGo = (stopTime - System.currentTimeMillis()) / 1000;
                listener.onProgress(String.format("Reading bus for %1$d seconds", secondsToGo));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();

        return j1939.read(GenericPacket.class, seconds, TimeUnit.SECONDS)
                .flatMap(e -> e.left.stream())
                .filter(filter)
                .collect(Collectors.toList());
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
