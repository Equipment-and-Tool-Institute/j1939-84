/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.etools.j1939_84.J1939_84.getLogger;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.BUSY;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.etools.j1939_84.bus.j1939.packets.DM22IndividualClearPacket;
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
import org.etools.j1939_84.bus.j1939.packets.DM3DiagnosticDataClearPacket;
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
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DateTimeModule;

/**
 * A Wrapper around a {@link Bus} that provides functionality specific to SAE
 * J1939
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class J1939 {

    /**
     * The source address of the engine
     */
    public static final int ENGINE_ADDR = 0x00;
    /**
     * The global source address for broadcast
     */
    public static final int GLOBAL_ADDR = 0xFF;
    /**
     * The time to wait for a response from a destination specific request (200
     * ms + 10% + fudge factor) This time come from J1939-21, 5.12.3 Device
     * Response Time and Timeout Defaults
     */
    private static final int DS_TIMEOUT = 230; // milliseconds
    /**
     * The time to wait for a response from a global request. This time come
     * from Eric. It is based on 200 ms + a delay due to a scheduled DM1.
     */
    private static final long GLOBAL_TIMEOUT = 600; // milliseconds

    /**
     * The time to wait for a response from a global request without issuing a
     * warning.
     */
    private static final long GLOBAL_WARN_TIMEOUT = 200;// milliseconds

    private static final String LATE_BAM_RESPONSE = "Warning: Late BAM response: ";

    private static final String TIMEOUT_MESSAGE = "Timeout - No Response";

    private final Bus bus;

    private int warnings;

    public J1939() {
        this(new EchoBus(0xA5));
    }

    /**
     * Constructor to be used with tests
     *
     * @param bus
     *                the {@link Bus} used to communicate with the vehicle
     */
    public J1939(Bus bus) {
        this.bus = bus;
    }

    /**
     * Reads the static field PGN from the given class. Returns null if the PGN
     * can't be read.
     *
     * @param  cls
     *                 the class of interest
     * @return     PGN number based on ParsedPacket class
     */
    static protected <T extends ParsedPacket> int getPgn(Class<T> cls) {
        try {
            return cls.getField("PGN").getInt(null);
        } catch (Exception e) {
            return -1;
        }
    }

    /** Helper to detect if this response represents busy. */
    static private <T extends GenericPacket> boolean isBusy(Either<T, AcknowledgmentPacket> e) {
        return e.right.stream().anyMatch(p -> p.getResponse() == Response.BUSY);
    }

    /** Used for development to detect DMs that are manually parsed. */
    static public boolean isManual(int pgn) {
        ParsedPacket processRaw = new J1939().processRaw(pgn, Packet.create(pgn, 0x0, new byte[8]));
        return processRaw.getClass() != GenericPacket.class;
    }

    /**
     * filter by pgn
     */
    static private Predicate<Packet> pgnFilter(int pgn) {
        return response -> response.getPgn() == pgn;
    }

    private static void severe(String message, Throwable t) {
        getLogger().log(SEVERE, message, t);
    }

    static private Predicate<Packet> sourceFilter(int addr) {
        if (addr == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Invalid use of global source.");
        }
        return response -> response.getSource() == addr;
    }

    /**
     * Filter to find acknowledgement/nack packets
     *
     * @param  pgn
     *                 the pgn that's being requested
     * @return     true if the message is an Acknowledgement/Nack for the given pgn
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
     * @param  pgn
     *                  the PGN of the packet that's being request
     * @param  addr
     *                  the address the request is being directed at
     * @return      a {@link Packet}
     */
    public Packet createRequestPacket(int pgn, int addr) {
        return Packet.create(0xEA00 | addr, getBusAddress(), true, pgn, pgn >> 8, pgn >> 16);
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
    private int getBusAddress() {
        return bus.getAddress();
    }

    private static DateTimeModule getDateTimeModule() {
        return DateTimeModule.getInstance();
    }

    /** Count of warnings detected in J1939 and J1939TP. */
    public int getWarnings() {
        return warnings;
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

    public void incrementWarning() {
        warnings++;
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the given
     * {@link Packet}
     *
     * @param  packet
     *                    the {@link Packet} to process
     * @return        a subclass of {@link ParsedPacket}
     */
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

            case DM3DiagnosticDataClearPacket.PGN:
                return new DM3DiagnosticDataClearPacket(packet);

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

            case DM22IndividualClearPacket.PGN:
                return new DM22IndividualClearPacket(packet);

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
                return new GenericPacket(packet);
        }
    }

    public Stream<GenericPacket> readGenericPacket(Predicate<Either<GenericPacket, AcknowledgmentPacket>> predicate) {
        try {
            return read()
                         .takeWhile(predicate)
                         .filter(e -> e.left.isPresent())
                         .flatMap(e -> e.left.stream());
        } catch (BusException e) {
            getLogger().log(Level.SEVERE, "Error while reading bus", e);
        }
        return Stream.empty();
    }

    /**
     * Reads the bus indefinitely
     *
     * @return              {@link Stream} of {@link ParsedPacket} s
     * @throws BusException
     *                          if there is a problem reading the bus
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read() throws BusException {
        return read(365, TimeUnit.DAYS).map(this::process);
    }

    /**
     * Watches the bus for up to the timeout for the first packet that matches
     * the PGN in the given class
     *
     * @param  <T>
     *                     the Type of Packet to expect back
     * @param  T
     *                     the class of interest
     * @param  addr
     *                     the source address the packet should come from. NOTE do not
     *                     use the Global Address (0xFF) here
     * @param  timeout
     *                     the maximum time to wait for a message
     * @param  unit
     *                     the {@link TimeUnit} for the timeout
     * @return         the resulting packet
     */
    public <T extends GenericPacket> Optional<Either<T, AcknowledgmentPacket>> read(Class<T> T,
                                                                                    int addr,
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
            severe("Error reading packets", e);
        }
        return Optional.empty();
    }

    /**
     * Watches the bus for up to the timeout for all the packets that match the
     * PGN in the given class
     *
     * @param  <T>
     *                     the Type of Packet to expect back
     * @param  T
     *                     the class of interest
     * @param  timeout
     *                     the maximum time to wait for a message
     * @param  unit
     *                     the {@link TimeUnit} for the timeout
     * @return         the resulting packets in a Stream
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read(Class<T> T,
                                                                                  long timeout,
                                                                                  TimeUnit unit) {
        try {
            Stream<Packet> stream = read(timeout, unit);
            int pgn = getPgn(T);
            if (pgn >= 0) {
                stream = stream.filter(pgnFilter(pgn));
            }

            return stream.map(this::process);
        } catch (BusException e) {
            severe("Error reading packets", e);
        }
        return Stream.empty();
    }

    /** Exposed only for mockito to override in testing */
    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return bus.read(timeout, unit);
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            Class<T> clas,
                                                            int address,
                                                            ResultsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, address);
        return requestDS(title, pgn, requestPacket, listener);
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            int pgn,
                                                            Packet request,
                                                            ResultsListener listener) {
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " " + title);
        }

        // FIXME verify and make a constant.
        long end = getDateTimeModule().getTimeAsLong() + 1200;
        boolean retry = false;
        for (int noResponse = 0; getDateTimeModule().getTimeAsLong() < end; noResponse++) {
            Optional<Either<T, AcknowledgmentPacket>> result = requestDSOnce(pgn, request, listener);
            if (result.isPresent()) {
                if (result.get().right.map(a -> a.getResponse() == BUSY).orElse(false)) {
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
    private <T extends GenericPacket> Optional<Either<T, AcknowledgmentPacket>> requestDSOnce(int pgn,
                                                                                              Packet request,
                                                                                              ResultsListener listener) {

        if (request.getDestination() == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request to global.");
        }

        try {
            Stream<Either<T, AcknowledgmentPacket>> stream = read(DS_TIMEOUT, MILLISECONDS)
                                                                                           .filter(dsFilter(pgn,
                                                                                                            request.getDestination(),
                                                                                                            getBusAddress()))
                                                                                           .map(this::process);
            Packet sent = bus.send(request);
            if (sent != null) {
                listener.onResult(sent.toTimeString());
            } else {
                warn("Failed to send: " + request);
            }
            Optional<Either<T, AcknowledgmentPacket>> result = stream.findFirst();
            result.ifPresentOrElse(p -> {
                ParsedPacket pp = p.resolve();
                listener.onResult(pp.getPacket().toTimeString());
                listener.onResult(pp.toString());
            },
                                   () -> listener.onResult(getDateTimeModule().getTime() + " " + TIMEOUT_MESSAGE));

            return result;
        } catch (BusException e) {
            severe("Error requesting DS packet", e);
            return Optional.empty();
        }
    }

    public List<AcknowledgmentPacket> requestForAcks(ResultsListener listener, String title, int pgn) {
        listener.onResult(getDateTimeModule().getTime() + " " + title);
        Packet requestPacket = createRequestPacket(pgn, GLOBAL_ADDR);
        return requestGlobalOnce(pgn, requestPacket, listener)
                                                              .stream()
                                                              .flatMap(e -> e.right.stream())
                                                              .collect(Collectors.toList());
    }

    public List<AcknowledgmentPacket> requestForAcks(ResultsListener listener, String title, int pgn, int address) {
        listener.onResult(getDateTimeModule().getTime() + " " + title);
        Packet requestPacket = createRequestPacket(pgn, address);
        return requestDSOnce(pgn, requestPacket, listener)
                                                          .stream()
                                                          .flatMap(e -> e.right.stream())
                                                          .collect(Collectors.toList());
    }

    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    Class<T> clas,
                                                                    ResultsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, GLOBAL_ADDR);
        return requestGlobal(title, pgn, requestPacket, listener);
    }

    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    int pgn,
                                                                    Packet requestPacket,
                                                                    ResultsListener listener) {
        boolean retry = false;
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " " + title);
        }

        Collection<Either<T, AcknowledgmentPacket>> results = requestGlobalOnce(pgn, requestPacket, listener);

        if (results.stream().anyMatch(J1939::isBusy)) {
            retry = true;

            // use map to collate by address
            Map<Integer, Either<T, AcknowledgmentPacket>> map = results.stream()
                                                                       .collect(Collectors.toMap(r1 -> ((ParsedPacket) r1.resolve()).getSourceAddress(),
                                                                                                 r1 -> r1));
            List<Either<T, AcknowledgmentPacket>> retryResults = requestGlobalOnce(pgn, requestPacket, listener);
            map.putAll(retryResults.stream()
                                   // don't overwrite with busy responses, but do add them if
                                   // not already in map
                                   .filter(e -> !isBusy(e)
                                           || !map.containsKey(((ParsedPacket) e.resolve()).getSourceAddress()))
                                   .collect(Collectors.toMap(r1 -> ((ParsedPacket) r1.resolve()).getSourceAddress(),
                                                             r1 -> r1)));
            results = map.values();
        }

        // replace any BUSY NACKS with DS results
        results = results.stream()
                         .map(e -> {
                             if (isBusy(e)) {
                                 Packet dsRequest = createRequestPacket(pgn,
                                                                        ((ParsedPacket) e.resolve()).getSourceAddress());
                                 Optional<Either<T, AcknowledgmentPacket>> response = requestDSOnce(pgn,
                                                                                                    dsRequest,
                                                                                                    listener);

                                 if (response.map(J1939::isBusy).orElse(true)) {
                                     // still busy, try one last time
                                     warn("first DS request after global busy NACK: " + dsRequest + " -> " + response);
                                     response = requestDSOnce(pgn, dsRequest, listener);
                                     if (response.map(J1939::isBusy).orElse(true)) {
                                         warn("second DS request after global busy NACK: " + dsRequest + " -> "
                                                 + response);
                                     }
                                 }
                                 return response.orElse(e);
                             } else {
                                 return e;
                             }
                         })
                         .collect(Collectors.toList());

        return new RequestResult<>(retry,
                                   results.stream()
                                          .sorted(Comparator.comparingInt(o -> ((ParsedPacket) o.resolve()).getSourceAddress()))
                                          .collect(Collectors.toList()));

    }

    /**
     * Request from global only once.
     */
    private <T extends GenericPacket> List<Either<T, AcknowledgmentPacket>> requestGlobalOnce(int pgn,
                                                                                              Packet request,
                                                                                              ResultsListener listener) {
        if (request.getDestination() != GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request not to global.");
        }

        List<Either<T, AcknowledgmentPacket>> result;
        try {
            Stream<Packet> stream = read(GLOBAL_TIMEOUT, MILLISECONDS);
            Packet sent = bus.send(request);
            LocalDateTime lateTime;
            if (sent != null) {
                listener.onResult(sent.toTimeString());
                lateTime = sent.getTimestamp().plus(GLOBAL_WARN_TIMEOUT, ChronoUnit.MILLIS);
            } else {
                warn("Failed to send: " + request);
                lateTime = null;
            }
            List<Packet> lateBam = new ArrayList<>();
            result = stream
                           .filter(globalFilter(pgn))
                           .peek(p -> {
                               /*
                                * If the first fragment arrived after lateBam, then it
                                * is late.
                                */
                               if (lateTime != null && p.getFragments().size() > 0
                                       && p.getFragments().get(0).getTimestamp().isAfter(lateTime)) {
                                   lateBam.add(p);
                               }
                           })
                           // Collect all of the packet, even though they are not
                           // complete. They were all announced in time.
                           .collect(Collectors.toList())
                           .stream()
                           .map(rawPacket -> {
                               try {
                                   listener.onResult(rawPacket.toTimeString());
                                   var pp = (Either<T, AcknowledgmentPacket>) process(rawPacket);
                                   listener.onResult(pp.resolve().toString());
                                   return pp;
                               } catch (PacketException e) {
                                   // This is not a complete packet. Should be logged
                                   // as a failure elsewhere.
                                   return null;
                               }
                           })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
            /* Log late fragments as raw packets. */
            lateBam.forEach(p -> {
                listener.onResult(LATE_BAM_RESPONSE + " " + p.getFragments().get(0).toTimeString());
            });

            if (result.isEmpty()) {
                listener.onResult(getDateTimeModule().getTime() + " " + TIMEOUT_MESSAGE);
            }
        } catch (BusException e) {
            severe("Error requesting packet", e);
            result = Collections.emptyList();
        }
        return result;
    }

    public BusResult<DM30ScaledTestResultsPacket> requestTestResults(int tid,
                                                                     int spn,
                                                                     int fmi,
                                                                     int address,
                                                                     ResultsListener listener) {
        if (address == GLOBAL_ADDR) {
            throw new IllegalArgumentException("DM7 request to global.");
        }

        Packet request = DM7CommandTestsPacket.create(getBusAddress(), address, tid, spn, fmi).getPacket();

        String title = "Sending DM7 for DM30 to " + Lookup.getAddressName(address) + " for SPN " + spn;
        listener.onResult(getDateTimeModule().getTime() + " " + title);

        try {
            BusResult<DM30ScaledTestResultsPacket> result;
            for (int i = 0; true; i++) {
                Stream<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> stream = read(DS_TIMEOUT,
                                                                                                MILLISECONDS)
                                                                                                             .filter(dsFilter(DM30ScaledTestResultsPacket.PGN,
                                                                                                                              request.getDestination(),
                                                                                                                              getBusAddress()))
                                                                                                             .map(this::process);
                Packet sent = bus.send(request);
                if (sent != null) {
                    listener.onResult(sent.toTimeString());
                } else {
                    warn("Failed to send: " + request);
                }
                Optional<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> first = stream.findFirst();
                result = new BusResult<>(i > 0, first);
                result.getPacket().ifPresentOrElse(p -> {
                    GenericPacket response = p.resolve();
                    Packet packet = response.getPacket();
                    listener.onResult(packet.toTimeString());
                    listener.onResult(response.toString());
                },
                                                   () -> listener.onResult(getDateTimeModule().getTime() + " "
                                                           + TIMEOUT_MESSAGE));
                // if there is a valid response or a non-busy NACK, return it.
                if (i == 2 || result.getPacket()
                                    // valid packet
                                    .map(e -> e.resolve(p -> true,
                                                        // non-busy NACK
                                                        p -> !p.getResponse().equals(BUSY)))
                                    .orElse(false)) {
                    break;
                }
            }
            return result;
        } catch (BusException e) {
            severe("Error requesting DS packet", e);
            return new BusResult<>(true);
        }
    }

    private void warn(String message) {
        incrementWarning();
        getLogger().log(WARNING, message);
    }
}
