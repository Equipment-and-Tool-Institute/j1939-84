/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.bus.Bus;
import org.etools.j1939_84.bus.BusException;
import org.etools.j1939_84.bus.EchoBus;
import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.Packet.PacketException;
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
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineSpeedPacket;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.FunctionalModule;

/**
 * A Wrapper around a {@link Bus} that provides functionality specific to SAE
 * J1939
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class J1939 {

    /**
     * The default time to wait for a response (200 ms + 10%)
     */
    private static final int DEFAULT_TIMEOUT = 230;

    /**
     * The default time unit for responses
     */
    private static final TimeUnit DEFAULT_TIMEOUT_UNITS = TimeUnit.MILLISECONDS;

    /**
     * The source address of the engine
     */
    public static final int ENGINE_ADDR = 0x00;

    /**
     * The global source address for broadcast
     */
    public static final int GLOBAL_ADDR = 0xFF;

    /**
     * PGN for J1939 requests
     */
    public static final int REQUEST_PGN = 0xEA00;

    /**
     * Helper to create a packet to request a packet with the given PGN be sent
     * by modules on the bus that support it
     *
     * @param pgn
     *         the PGN of the packet that's being request
     * @param addr
     *         the address the request is being directed at
     * @param tool
     *         the requestor's address
     * @return a {@link Packet}
     */
    static public Packet createRequestPacket(int pgn, int addr, int tool) {
        return Packet.create(REQUEST_PGN | addr, tool, true, pgn, pgn >> 8, pgn >> 16);
    }

    /**
     * Helper method to find first results that are not busy NACKs with the side
     * effect of adding source address of busy NACKs to expectedAddresses.
     */
    static private <T extends GenericPacket> Map<Integer, Either<T, AcknowledgmentPacket>> findTerminalResults(
            Collection<Integer> expectedAddressess,
            List<Either<T, AcknowledgmentPacket>> results) {
        return results
                .stream()
                .filter(r -> r.resolve(p -> true, p -> {
                    if (p.getResponse() != Response.BUSY) {
                        return true;
                    }
                    expectedAddressess.add(p.getSourceAddress());
                    return false;
                }))
                // if there are multiple responses from the same address, use
                // first
                .collect(Collectors.toMap(e -> ((ParsedPacket) e.resolve()).getSourceAddress(), e -> e, (a, b) -> a));
    }

    static private Logger getLogger() {
        return J1939_84.getLogger();
    }

    /**
     * Reads the static field PGN from the given class. Returns null if the PGN
     * can't be read.
     *
     * @param cls
     *         the class of interest
     * @return PGN number based on ParsedPacket class
     */
    static protected <T extends ParsedPacket> int getPgn(Class<T> cls) {
        try {
            return cls.getField("PGN").getInt(null);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * filter by pgn
     */
    static private Predicate<Packet> pgnFilter(int pgn) {
        return response -> response.getPgn() == pgn;
    }

    static private Predicate<Packet> sourceFilter(int addr) {
        if (addr == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Invalid use of global source.");
        }
        return response -> response.getSource() == addr;
    }

    private final Bus bus;

    public J1939() {
        this(new EchoBus(0xA5));
    }

    /**
     * Constructor to be used with tests
     *
     * @param bus
     *         the {@link Bus} used to communicate with the vehicle
     */
    public J1939(Bus bus) {
        this.bus = bus;
    }

    /**
     * Filter to find acknowledgement/nack packets
     *
     * @param pgn
     *         the pgn that's being requested
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
     *         the PGN of the packet that's being request
     * @param addr
     *         the address the request is being directed at
     * @return a {@link Packet}
     */
    public Packet createRequestPacket(int pgn, int addr) {
        return createRequestPacket(pgn, addr, getBusAddress());
    }

    private Predicate<Packet> dsFilter(int pgn, int requestDestination, int requestSource) {
        if (requestDestination == GLOBAL_ADDR || requestSource == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Invalid use of global.");
        }
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
        return bus.getAddress();
    }

    private DateTimeModule getDateTimeModule() {
        return DateTimeModule.getInstance();
    }

    private Predicate<Packet> globalFilter(int pgn) {
        return
                // does the packet have the right ID
                (pgnFilter(pgn).or(ackNackFilter(pgn)))
                        // is it addressed to us or all
                        .and((p -> p.getDestination() == bus.getAddress()
                                || p.getDestination() == GLOBAL_ADDR
                                // A TP message to global will have a destination of 0
                                || (p.getDestination() == 0 && p.getLength() > 8)));
    }

    private void log(String msg) {
        getLogger().log(Level.WARNING, msg);
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the given
     * {@link Packet}
     *
     * @param packet
     *         the {@link Packet} to process
     * @return a subclass of {@link ParsedPacket}
     */
    @SuppressWarnings("unchecked")
    private <T extends GenericPacket> Either<T, AcknowledgmentPacket> process(Packet packet) {
        ParsedPacket pp = processRaw(packet.getPgn(), packet);
        if (pp instanceof AcknowledgmentPacket) {
            return new Either<>(null, (AcknowledgmentPacket) pp);
        } else {
            return new Either<>((T) pp, null);
        }
    }

    private ParsedPacket processRaw(int pgn, Packet packet) {
        switch (pgn) {

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

        case DM27AllPendingDTCsPacket.PGN:
            return new DM27AllPendingDTCsPacket(packet);

        case DM28PermanentEmissionDTCPacket.PGN:
            return new DM28PermanentEmissionDTCPacket(packet);

        case DM29DtcCounts.PGN:
            return new DM29DtcCounts(packet);

        case DM30ScaledTestResultsPacket.PGN:
            return new DM30ScaledTestResultsPacket(packet);

        case DM31DtcToLampAssociation.PGN:
            return new DM31DtcToLampAssociation(packet);

        case DM33EmissionIncreasingAECDActiveTime.PGN:
            return new DM33EmissionIncreasingAECDActiveTime(packet);

        case DM34NTEStatus.PGN:
            return new DM34NTEStatus(packet);

        case DM56EngineFamilyPacket.PGN:
            return new DM56EngineFamilyPacket(packet);

        case AcknowledgmentPacket.PGN:
            return new AcknowledgmentPacket(packet);

        case AddressClaimPacket.PGN:
            return new AddressClaimPacket(packet);

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

        default:
            return new GenericPacket(packet, J1939DaRepository.getInstance().findPgnDefinition(pgn));
        }
    }

    /**
     * Reads the bus indefinitely
     *
     * @return {@link Stream} of {@link ParsedPacket} s
     * @throws BusException
     *         if there is a problem reading the bus
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read() throws BusException {
        return read(365, TimeUnit.DAYS).map(this::process);
    }

    /**
     * Watches the bus for up to the timeout for the first packet that matches
     * the PGN in the given class
     *
     * @param <T>
     *         the Type of Packet to expect back
     * @param T
     *         the class of interest
     * @param addr
     *         the source address the packet should come from. NOTE do not
     *         use the Global Address (0xFF) here
     * @param timeout
     *         the maximum time to wait for a message
     * @param unit
     *         the {@link TimeUnit} for the timeout
     * @return the resulting packet
     */
    public <T extends GenericPacket> Optional<Either<T, AcknowledgmentPacket>> read(Class<T> T, int addr,
                                                                                    long timeout,
                                                                                    TimeUnit unit) {
        if (addr == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Invalid read from global.");
        }

        int pgn = getPgn(T);
        try (Stream<Packet> stream = read(timeout, unit)) {
            return stream
                    .filter(sourceFilter(addr).and(pgnFilter(pgn)))
                    .findFirst()
                    .map(this::process);
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
     *         the Type of Packet to expect back
     * @param T
     *         the class of interest
     * @param timeout
     *         the maximum time to wait for a message
     * @param unit
     *         the {@link TimeUnit} for the timeout
     * @return the resulting packets in a Stream
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read(Class<T> T,
                                                                                  long timeout,
                                                                                  TimeUnit unit) {
        try {
            Stream<Packet> stream = read(timeout, unit);
            final int pgn = getPgn(T);
            if (pgn >= 0) {
                stream = stream.filter(pgnFilter(pgn));
            }

            return stream.map(this::process);
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error reading packets", e);
        }
        return Stream.empty();
    }

    // exposed for mockito
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return bus.read(timeout, unit);
    }

    /**
     * Request DM30 with DM7
     */
    public BusResult<DM30ScaledTestResultsPacket> requestDm7(String title,
                                                             ResultsListener listener,
                                                             Packet request) {
        if (request.getDestination() == GLOBAL_ADDR) {
            throw new IllegalArgumentException("DM7 request to global.");
        }
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " " + title);
        }
        try {
            BusResult<DM30ScaledTestResultsPacket> result;
            for (int i = 0; true; i++) {
                Stream<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> stream = read(DEFAULT_TIMEOUT,
                                                                                                DEFAULT_TIMEOUT_UNITS)
                        .filter(dsFilter(DM30ScaledTestResultsPacket.PGN, request.getDestination(),
                                         getBusAddress()))
                        .map(this::process);
                Packet sent = bus.send(request);
                if (sent != null) {
                    listener.onResult(getDateTimeModule().format(sent.getTimestamp()) + " " + sent.toString());
                } else {
                    log("Failed to send: " + request);
                }
                Optional<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> first = stream.findFirst();
                result = new BusResult<>(i > 0, first);
                result.getPacket().ifPresentOrElse(p -> {
                                                       GenericPacket response = p.resolve();
                                                       Packet packet = response.getPacket();
                                                       listener.onResult(getDateTimeModule().format(packet.getTimestamp()) + " " + packet.toString());
                                                       listener.onResult(response.toString());
                                                   },
                                                   () -> listener.onResult(FunctionalModule.TIMEOUT_MESSAGE));
                // if there is a valid response or a non-busy NACK, return it.
                if (i == 2 || result.getPacket()
                        // valid packet
                        .map(e -> e.resolve(p -> true,
                                            // non-busy NACK
                                            p -> !p.getResponse().equals(Response.BUSY)))
                        .orElse(false)) {
                    break;
                }
            }
            return result;
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error requesting DS packet", e);
            return new BusResult<>(true);
        }
    }

    public List<AcknowledgmentPacket> requestDm11(ResultsListener listener) {
        listener.onResult(getDateTimeModule().getTime() + " " + "Global DM11 Request");

        Packet requestPacket = createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);
        final int pgn = getPgn(DM11ClearActiveDTCsPacket.class);
        var responses = requestGlobalOnce(pgn, requestPacket, listener, true);

        return responses.stream()
                .flatMap(e -> e.right.stream())
                .collect(Collectors.toList());
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            Class<T> clas,
                                                            int address,
                                                            ResultsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, address);
        return requestDS(title, pgn, requestPacket, true, listener);
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            int pgn,
                                                            Packet request,
                                                            boolean fullString,
                                                            ResultsListener listener) {
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " " + title);
        }

        // FIXME verify and make a constant.
        long end = getDateTimeModule().getTimeAsLong() + 1200;
        boolean retry = false;
        for (int noResponse = 0; getDateTimeModule().getTimeAsLong() < end; noResponse++) {
            Optional<Either<T, AcknowledgmentPacket>> result = requestDSOnce(listener,
                                                                             fullString,
                                                                             pgn,
                                                                             request);
            if (result.isPresent()) {
                if (result.get().right.map(a -> a.getResponse() == Response.BUSY).orElse(false)) {
                    // busy. wait 200 ms and try again
                    getDateTimeModule().pauseFor(200);
                    retry = true;
                } else {
                    // either the packet or a permanent NACK, give up
                    return new BusResult<>(noResponse > 0, result);
                }
            } else {
                return new BusResult<>(retry);
            }
        }
        return new BusResult<>(retry);

    }

    /**
     * Make a single DS request with no retries.
     */
    private <T extends GenericPacket> Optional<Either<T, AcknowledgmentPacket>> requestDSOnce(ResultsListener listener,
                                                                                              boolean fullString,
                                                                                              int pgn,
                                                                                              Packet request) {

        if (request.getDestination() == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request to global.");
        }

        try {
            Stream<Either<T, AcknowledgmentPacket>> stream = read(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNITS)
                    .filter(dsFilter(pgn, request.getDestination(), getBusAddress()))
                    .map(this::process);
            Packet sent = bus.send(request);
            if (sent != null) {
                listener.onResult(getDateTimeModule().format(sent.getTimestamp()) + " " + sent.toString());
            } else {
                log("Failed to send: " + request);
            }
            Optional<Either<T, AcknowledgmentPacket>> result = stream.findFirst();
            result.ifPresentOrElse(p -> {
                                       ParsedPacket pp = p.resolve();
                                       listener.onResult(pp.getPacket().toTimeString());
                                       if (fullString) {
                                           listener.onResult(pp.toString());
                                       }
                                   },
                                   () -> listener.onResult(FunctionalModule.TIMEOUT_MESSAGE));

            return result;
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error requesting DS packet", e);
            return Optional.empty();
        }
    }

    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    Class<T> clas,
                                                                    ResultsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, GLOBAL_ADDR);
        return requestGlobal(title, listener, true, pgn, requestPacket);
    }

    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    ResultsListener listener,
                                                                    boolean fullString,
                                                                    final int pgn,
                                                                    Packet requestPacket) {
        boolean retry = false;
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " " + title);
        }

        // expect responses from all OBD modules and anything that responds BUSY
        Collection<Integer> expectedAddressess = new TreeSet<>(DataRepository.getInstance().getObdModuleAddresses());

        // Record successful results and NACKs. Treat busy NACK same as no
        // response.
        Map<Integer, Either<T, AcknowledgmentPacket>> results = findTerminalResults(expectedAddressess,
                                                                                    requestGlobalOnce(pgn,
                                                                                                      requestPacket,
                                                                                                      listener,
                                                                                                      fullString));

        if (!results.keySet().containsAll(expectedAddressess)) {
            // then not all OBD modules were heard, so re-request from global
            // and combine results
            retry = true;
            // replace old results with new successful results and NACKs. Treat
            // busy NACK same as no response.
            results.putAll(
                    findTerminalResults(expectedAddressess,
                                        requestGlobalOnce(pgn, requestPacket, listener, fullString)));
        }

        if (!results.keySet().containsAll(expectedAddressess)) {
            // then not all OBD modules were heard, so DS request from each
            // module that we do not have a terminal result from
            expectedAddressess.stream()
                    .filter(a -> !results.containsKey(a))
                    .forEach(sa -> {
                        Packet dsRequest = createRequestPacket(pgn, sa);
                        Optional<Either<T, AcknowledgmentPacket>> response = requestDSOnce(listener, fullString, pgn,
                                                                                           dsRequest);

                        response.filter(r -> r.right.map(nack -> nack.getResponse() != Response.BUSY).orElse(true))
                                .ifPresent(r -> results.put(sa, r));

                        if (!results.keySet().containsAll(expectedAddressess)) {
                            // still busy, try one last time
                            log("first DS request after global busy NACK: " + dsRequest + " -> " + response);
                            response = requestDSOnce(listener, fullString, pgn, dsRequest);
                            if (response.flatMap(e -> e.left).isEmpty()) {
                                log("second DS request after global busy NACK: " + dsRequest + " -> " + response);
                            }
                            response.ifPresent(r -> results.put(sa, r));
                        }
                    });
        }
        return new RequestResult<>(retry,
                                   results.keySet().stream().sorted().map(results::get).collect(Collectors.toList()));

    }

    /**
     * Request from global only once.
     */
    @SuppressWarnings("unchecked")
    public <T extends GenericPacket> List<Either<T, AcknowledgmentPacket>> requestGlobalOnce(int pgn,
                                                                                             Packet request,
                                                                                             ResultsListener listener,
                                                                                             boolean fullString) {
        if (request.getDestination() != GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request not to global.");
        }

        List<Either<T, AcknowledgmentPacket>> result;
        try {
            Stream<Packet> stream = read(DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNITS);
            Packet sent = bus.send(request);
            if (sent != null) {
                listener.onResult(sent.toTimeString());
            } else {
                log("Failed to send: " + request);
            }
            result = stream
                    .filter(globalFilter(pgn))
                    // Collect all of the packet, even though they are not
                    // complete. They were all announced in time.
                    .collect(Collectors.toList()).stream()
                    .map(rawPacket -> {
                        try {
                            listener.onResult(rawPacket.toTimeString());
                            var pp = (Either<T, AcknowledgmentPacket>) process(rawPacket);
                            if (fullString) {
                                listener.onResult(pp.resolve().toString());
                            }
                            return pp;
                        } catch (PacketException e) {
                            // This is not a complete packet. Should be logged
                            // as a failure elsewhere.
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (result.isEmpty()) {
                listener.onResult(FunctionalModule.TIMEOUT_MESSAGE);
            }
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error requesting packet", e);
            result = Collections.emptyList();
        }
        return result;
    }

}