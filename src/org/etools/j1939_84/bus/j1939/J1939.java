/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.bus.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.model.RequestResult;

/**
 * A Wrapper around a {@link Bus} that provides functionality specific to SAE
 * J1939
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class J1939 {

    /**
     * The default number of times to retry sending a message if response isn't
     * received with in the specified timeout period
     */
    public static final int DEFAULT_NUMBER_OF_TRIES = 3;

    /**
     * The default time to wait for a response
     */
    public static final int DEFAULT_TIMEOUT = 220;

    /**
     * The default time unit for responses
     */
    private static final TimeUnit DEFAULT_TIMEOUT_UNITS = TimeUnit.MILLISECONDS;

    /**
     * The source address of the engine
     */
    public static final int ENGINE_ADDR = 0x00;

    /**
     * The 'other' address for an engine
     */
    public static final int ENGINE_ADDR_1 = 0x01;

    /**
     * The global source address for broadcast
     */
    public static final int GLOBAL_ADDR = 0xFF;

    /**
     * The default time to wait for a response when requesting from global
     */
    public static final int GLOBAL_TIMEOUT = 1250;

    public static final int REQUEST_PGN = 0xEA00;

    private static final int TOOL_ADDRESS = 0xF9;

    /**
     * Helper to create a packet to request a packet with the given PGN be sent
     * by modules on the bus that support it
     *
     * @param pgn
     *            the PGN of the packet that's being request
     * @param addr
     *            the address the request is being directed at
     * @param tool
     *            the requestor's address
     * @return a {@link Packet}
     */
    static public Packet createRequestPacket(int pgn, int addr, int tool) {
        return Packet.create(REQUEST_PGN | addr, tool, true, pgn, pgn >> 8, pgn >> 16);
    }

    private static <T extends ParsedPacket> int getPgn(Class<T> cls) {
        try {
            return cls.getField("PGN").getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException("Unexpected error occured.", e);
        }
    }

    private static Predicate<Packet> pgnFilter(int pgn) {
        return response -> response.getPgn() == pgn;
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the id
     *
     * @param id
     *            the id to match
     * @param packet
     *            the {@link Packet} to process
     * @return a subclass of {@link ParsedPacket}
     */
    @SuppressWarnings("unchecked")
    private static <T extends ParsedPacket> Either<T, AcknowledgmentPacket> process(int id, Packet packet) {
        ParsedPacket pp = processRaw(id, packet);
        if (pp instanceof AcknowledgmentPacket) {
            return new Either<>(null, (AcknowledgmentPacket) pp);
        } else {
            return new Either<>((T) pp, null);
        }
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the given
     * {@link Packet}
     *
     * @param packet
     *            the {@link Packet} to process
     * @return a subclass of {@link ParsedPacket}
     */
    private static <T extends ParsedPacket> Either<T, AcknowledgmentPacket> process(Packet packet) {
        return process(packet.getPgn(), packet);
    }

    private static ParsedPacket processRaw(int id, Packet packet) {
        switch (id) {
        case DM1ActiveDTCsPacket.PGN:
            return new DM1ActiveDTCsPacket(packet);

        case DM2PreviouslyActiveDTC.PGN:
            return new DM2PreviouslyActiveDTC(packet);

        case DM5DiagnosticReadinessPacket.PGN:
            return new DM5DiagnosticReadinessPacket(packet);

        case DM6PendingEmissionDTCPacket.PGN:
            return new DM6PendingEmissionDTCPacket(packet);

        case DM7CommandTestsPacket.PGN:
            return new DM7CommandTestsPacket(packet);

        case DM11ClearActiveDTCsPacket.PGN:
            return new DM11ClearActiveDTCsPacket(packet);

        case DM12MILOnEmissionDTCPacket.PGN:
            return new DM12MILOnEmissionDTCPacket(packet);

        case DM19CalibrationInformationPacket.PGN:
            return new DM19CalibrationInformationPacket(packet);

        case DM20MonitorPerformanceRatioPacket.PGN:
            return new DM20MonitorPerformanceRatioPacket(packet);

        case DM21DiagnosticReadinessPacket.PGN:
            return new DM21DiagnosticReadinessPacket(packet);

        case DM23PreviouslyMILOnEmissionDTCPacket.PGN:
            return new DM23PreviouslyMILOnEmissionDTCPacket(packet);

        case DM24SPNSupportPacket.PGN:
            return new DM24SPNSupportPacket(packet);

        case DM25ExpandedFreezeFrame.PGN:
            return new DM25ExpandedFreezeFrame(packet);

        case DM26TripDiagnosticReadinessPacket.PGN:
            return new DM26TripDiagnosticReadinessPacket(packet);

        case DM28PermanentEmissionDTCPacket.PGN:
            return new DM28PermanentEmissionDTCPacket(packet);

        case DM29DtcCounts.PGN:
            return new DM29DtcCounts(packet);

        case DM30ScaledTestResultsPacket.PGN:
            return new DM30ScaledTestResultsPacket(packet);

        case DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN:
            return new DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(packet);

        case ComponentIdentificationPacket.PGN:
            return new ComponentIdentificationPacket(packet);

        case EngineSpeedPacket.PGN:
            return new EngineSpeedPacket(packet);

        case EngineHoursPacket.PGN:
            return new EngineHoursPacket(packet);

        case HighResVehicleDistancePacket.PGN:
            return new HighResVehicleDistancePacket(packet);

        case TotalVehicleDistancePacket.PGN:
            return new TotalVehicleDistancePacket(packet);

        case VehicleIdentificationPacket.PGN:
            return new VehicleIdentificationPacket(packet);

        case AddressClaimPacket.PGN:
            return new AddressClaimPacket(packet);

        case DM56EngineFamilyPacket.PGN:
            return new DM56EngineFamilyPacket(packet);

        case REQUEST_PGN:
            // Request; just return a wrapped packet
            return new UnknownParsedPacket(packet);

        default:
            // FIXME why blindly mask off lower byte?
            int maskedId = id & 0xFF00;

            switch (maskedId) {
            case AcknowledgmentPacket.PGN:
                // Acknowledgement, return the packet that was requested
                return new AcknowledgmentPacket(packet);

            case DM7CommandTestsPacket.PGN:
            case DM19CalibrationInformationPacket.PGN:
            case DM20MonitorPerformanceRatioPacket.PGN:
            case DM21DiagnosticReadinessPacket.PGN:
            case DM30ScaledTestResultsPacket.PGN:
            case AddressClaimPacket.PGN:
                return processRaw(maskedId, packet);

            default:
                // IDK
                return new UnknownParsedPacket(packet);
            }
        }
    }

    private static Predicate<Packet> sourceFilter(int addr) {
        return response -> response.getSource() == addr;
    }

    /**
     * The bus used to communicate with the vehicle
     */
    private final Bus bus;

    /**
     * Constructor
     *
     * @param bus
     *            the {@link Bus} used to communicate with the vehicle
     */
    public J1939(Bus bus) {
        this.bus = bus;
    }

    /**
     * Filter to find acknowledgement/nack packets
     *
     * @param pgn
     *            the pgn that's being requested
     * @return true if the message is an Acknowledgement/Nack for the given pgn
     */
    private Predicate<Packet> ackNackFilter(int pgn) {
        return response -> {
            return // ID is Acknowledgment
            response.getPgn() == 0xE800
                    // There are enough bytes
                    && response.getLength() == 8
            // Accepting 0xFF as "Address Acknowledged" is to handle Cummins
                    && (response.get(4) == getBusAddress() || response.get(4) == 0xFF)
            // The Acknowledged PGN matches
                    && response.get24(5) == pgn;
        };
    }

    /**
     * Helper to create a packet to request a packet with the given PGN be sent
     * by modules on the bus that support it
     *
     * @param pgn
     *            the PGN of the packet that's being request
     * @param addr
     *            the address the request is being directed at
     * @return a {@link Packet}
     */
    public Packet createRequestPacket(int pgn, int addr) {
        return createRequestPacket(pgn, addr, getBusAddress());
    }

    private Predicate<Packet> daFilter(int pgn, int requestDestination, int requestSource) {
        return globalFilter(pgn)
                // did it come from the right module or any if addressed to all
                .and(sourceFilter(requestDestination));
    }

    /**
     * Exposed for system testing purposes. Calling classes should interact
     * directly with the bus
     *
     * @return the {@link Bus} that backs this class
     */
    public Bus getBus() {
        return bus;
    }

    /**
     * Returns the address of this tool on the bus
     *
     * @return the address of the tool
     */
    public int getBusAddress() {
        return getBus().getAddress();
    }

    private Logger getLogger() {
        return J1939_84.getLogger();
    }

    private Predicate<Packet> globalFilter(int pgn) {
        return
        // does the packet have the right ID
        (pgnFilter(pgn).or(ackNackFilter(pgn)))
                // is it addressed to us or all
                .and(((Predicate<Packet>) response -> response.getDestination() == 0xF9)
                        .or(p -> p.getDestination() == GLOBAL_ADDR)
                        // A TP message to braodbase will have a destination of
                        // 0
                        .or(p -> p.getDestination() == 0 && p.getLength() > 8));
    }

    /**
     * Reads the bus indefinitely
     *
     * @return {@link Stream} of {@link ParsedPacket} s
     * @throws BusException
     *             if there is a problem reading the bus
     */
    public <T extends ParsedPacket> Stream<Either<T, AcknowledgmentPacket>> read() throws BusException {
        return getBus().read(365, TimeUnit.DAYS).map(t -> process(t));
    }

    /**
     * Watches the bus for up to the timeout for the first packet that matches
     * the PGN in the given class
     *
     * @param <T>
     *            the Type of Packet to expect back
     *
     * @param T
     *            the class of interest
     * @param addr
     *            the source address the packet should come from. NOTE do not
     *            use the Global Address (0xFF) here
     * @param timeout
     *            the maximum time to wait for a message
     * @param unit
     *            the {@link TimeUnit} for the timeout
     * @return the resulting packet
     */
    public <T extends ParsedPacket> Optional<Either<T, AcknowledgmentPacket>> read(Class<T> T, int addr,
            long timeout,
            TimeUnit unit) {
        int pgn = getPgn(T);
        try (Stream<Packet> stream = read(timeout, unit)) {
            return stream
                    .filter(sourceFilter(addr).and(pgnFilter(pgn)))
                    .findFirst().map(t -> process(t));
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error reading packets", e);
        }
        return Optional.empty();
    }

    /**
     * Watches the bus for up to the timeout for all the packets that match the
     * PGN in the given class
     *
     * @param <T>
     *            the Type of Packet to expect back
     * @param T
     *            the class of interest
     * @param timeout
     *            the maximum time to wait for a message
     * @param unit
     *            the {@link TimeUnit} for the timeout
     * @return the resulting packets in a Stream
     */
    public <T extends ParsedPacket> Stream<Either<T, AcknowledgmentPacket>> read(Class<T> T, long timeout,
            TimeUnit unit) {
        int pgn = getPgn(T);
        try {
            return read(timeout, unit)
                    .filter(pgnFilter(pgn))
                    .map(t -> process(t));
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error reading packets", e);
        }
        return Stream.empty();
    }

    private Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        Stream<Packet> stream = getBus().read(timeout, unit);
        return stream.peek(packet -> getLogger().log(Level.FINE, "P->" + packet.toString()));
    }

    /**
     * Request a packet from a specific address. As long as the module responds
     * "busy", retry for up to 1.2s. Fail after 3 non-responses.
     *
     * @param <T>
     * @param packetClass
     * @param address
     * @return
     * @throws BusException
     */
    public <T extends ParsedPacket> BusResult<T> requestDa(Class<T> packetClass, int address) {
        long end = System.currentTimeMillis() + 1200;
        int noResponse = 0;
        while (true) {
            Optional<Either<T, AcknowledgmentPacket>> result = requestDaOnce(packetClass, address);
            if (result.isPresent()) {
                if (System.currentTimeMillis() < end
                        && result.get().right.map(a -> a.getResponse() == Response.BUSY).orElse(false)) {
                    // busy. wait 200 ms and try again
                    sleep(200);
                } else {
                    // either the packet or a permanent NACK, give up
                    return new BusResult<>(noResponse > 0, result);
                }
            } else {
                if (noResponse++ >= 2) {
                    return new BusResult<>(true);
                }
            }
        }
    }

    private <T extends ParsedPacket> Optional<Either<T, AcknowledgmentPacket>> requestDaOnce(Class<T> packetClass,
            int address) {
        try {
            Stream<Either<T, AcknowledgmentPacket>> stream = bus.read(220, TimeUnit.MILLISECONDS)
                    .filter(daFilter(getPgn(packetClass), address, TOOL_ADDRESS))
                    .map(p -> process(p));
            bus.send(createRequestPacket(getPgn(packetClass), address));
            return stream.findFirst();
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error requesting DS packet", e);
            return Optional.empty();
        }
    }

    public <T extends ParsedPacket> BusResult<DM30ScaledTestResultsPacket> requestDm7(int address, int spn) {
        // FIXME
        return null;
    }

    public <T extends ParsedPacket> RequestResult<T> requestGlobal(Class<T> packetClass) {
        int pgn = getPgn(packetClass);
        return requestGlobal(packetClass, pgn,
                createRequestPacket(pgn, 0xFF));
    }

    public <T extends ParsedPacket> RequestResult<T> requestGlobal(Class<T> packetClass, int pgn,
            Packet request) {
        List<Either<T, AcknowledgmentPacket>> results = requestMultipleOnce(pgn, request);
        List<AcknowledgmentPacket> busyNACKs = results.stream().flatMap(e -> e.right.stream())
                .filter(p -> p.getResponse() == Response.BUSY)
                .collect(Collectors.toList());
        boolean retry = !busyNACKs.isEmpty();
        if (retry) {
            // then rerequest from global and combine results
            List<Either<T, AcknowledgmentPacket>> secondResults = requestMultipleOnce(pgn, request);

            // find any results in the first request that are not in the second
            // FIXME

            results = secondResults;
            busyNACKs = results.stream().flatMap(e -> e.right.stream()).filter(p -> p.getResponse() == Response.BUSY)
                    .collect(Collectors.toList());
        }
        // now try the DA request
        Function<AcknowledgmentPacket, Stream<Either<T, AcknowledgmentPacket>>> mapper = p -> requestDaOnce(packetClass,
                p.getSourceAddress()).stream();
        Collection<Either<T, AcknowledgmentPacket>> list = busyNACKs.stream()
                .flatMap(mapper)
                .collect(Collectors.toList());
        results.addAll(list);
        return new RequestResult<>(retry, results);
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally.
     *
     * @param clas
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<Either<T, AcknowledgmentPacket>> requestMultiple(Class<T> clas) {
        return requestMultiple(clas, createRequestPacket(getPgn(clas), GLOBAL_ADDR));
    }

    /**
     * Sends a request for a Packet specified by the given class (T). T will
     * provide the PGN for the Packet that is requested. This will request the
     * packet globally.
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @param requestPacket
     *            the {@link Packet} to send that will generate the responses
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<Either<T, AcknowledgmentPacket>> requestMultiple(Class<T> T,
            Packet requestPacket) {
        return requestGlobal(T, getPgn(T), requestPacket).getEither().stream();
    }

    private <T extends ParsedPacket> List<Either<T, AcknowledgmentPacket>> requestMultipleOnce(int pgn,
            Packet request) {
        List<Either<T, AcknowledgmentPacket>> result;
        try {
            Stream<Packet> stream = read(GLOBAL_TIMEOUT, DEFAULT_TIMEOUT_UNITS);
            getBus().send(request);
            result = stream
                    .filter(globalFilter(pgn))
                    .map(rawPacket -> (Either<T, AcknowledgmentPacket>) process(rawPacket))
                    .collect(Collectors.toList());
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Sends a Request for the given packet. The request will repeat the given
     * number of tries.
     *
     * @param <T>
     *            the Type of Packet that will be returned
     * @param packetToSend
     *            the packet that will be sent
     * @param T
     *            the Class of packet that's expected to be returned
     * @param destination
     *            the address response should come from
     * @param tries
     *            the number of times to try the request
     * @return {@link Optional} {@link Packet} This may not contain a value if
     *         there was an exception
     */
    @Deprecated // use BusResult version instead
    public <T extends ParsedPacket> Optional<Either<T, AcknowledgmentPacket>> requestPacket(Packet packetToSend,
            Class<T> T,
            int destination,
            int tries) {
        return requestPacket(packetToSend, T, destination, tries, DEFAULT_TIMEOUT).getPacket();
    }

    public <T extends ParsedPacket> BusResult<T> requestPacket(Packet requestPacket,
            Class<T> clas,
            int destination, // FIXME this arg is redundant. Remove.
            int tries,
            long timeout) {
        return requestDa(clas, destination);
    }

    /**
     * Sends a request for a Packet. T will provide the PGN for the response.
     * NACKs will NOT be ignored.
     *
     * @param <T>
     *            the Type of Packet to request
     * @param T
     *            the class that extends {@link ParsedPacket} that provides the
     *            PGN for the packet to be requested
     * @param requestPacket
     *            the {@link Packet} to send that will generate the responses
     * @param timeout
     *            the maximum time to wait for responses
     * @param unit
     *            the {@link TimeUnit} of the timeout
     * @return a {@link Stream} containing {@link ParsedPacket}
     */
    public <T extends ParsedPacket> Stream<Either<T, AcknowledgmentPacket>> requestRaw(Class<T> T,
            Packet requestPacket,
            long timeout,
            TimeUnit unit) {
        return (requestPacket.getDestination() == 0xFF)
                ? requestGlobal(T, getPgn(T), requestPacket).getEither().stream()
                : requestDa(T, requestPacket.getDestination()).getPacket().stream();
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // not expected
            e.printStackTrace();
        }
    }

}
