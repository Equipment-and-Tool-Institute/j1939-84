/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.SEVERE;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
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

import org.etools.j1939_84.J1939_84;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.EchoBus;
import org.etools.j1939tools.bus.Either;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.Packet.PacketException;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.DM34NTEStatus;
import org.etools.j1939tools.j1939.packets.DM3DiagnosticDataClearPacket;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM7CommandTestsPacket;
import org.etools.j1939tools.j1939.packets.EngineHoursPacket;
import org.etools.j1939tools.j1939.packets.EngineSpeedPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.GhgActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.GhgLifetimeActiveTechnologyPacket;
import org.etools.j1939tools.j1939.packets.HighResVehicleDistancePacket;
import org.etools.j1939tools.j1939.packets.ParsedPacket;
import org.etools.j1939tools.j1939.packets.TotalVehicleDistancePacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A Wrapper around a {@link Bus} that provides functionality specific to SAE
 * J1939
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class J1939 {

    private static final String FAILED_TO_SEND = "Failed to send - ";
    /**
     * The source address of the engine
     */
    public static final int ENGINE_ADDR = 0x00;
    /**
     * The global source address for broadcast
     */
    public static final int GLOBAL_ADDR = 0xFF;
    /**
     * The time to wait for a response from a global request. This time come
     * from Eric. It is based on 200 ms + a delay due to a scheduled DM1.
     */
    private static final int DS_TIMEOUT = 750; // milliseconds
    /**
     * The time to wait for a response from a global request. This time come
     * from Eric. It is based on 200 ms + a delay due to a scheduled DM1.
     */
    public static final long GLOBAL_TIMEOUT = 750; // milliseconds

    /**
     * The time to wait for a response from a global request without issuing a
     * warning.
     */
    private static final long GLOBAL_WARN_TIMEOUT = 200;// milliseconds

    private static final String LATE_RESPONSE = "TIMING: Late response - ";

    private static final String TIMEOUT_MESSAGE = "Timeout - No Response";

    private final Bus bus;

    private int warnings;

    private boolean logDeltaTime;

    private Stream<Packet> loggerStream = Stream.empty();

    public J1939() {
        this(new EchoBus(0xA5));
    }

    /**
     * Constructor to be used with tests
     *
     * @param bus the {@link Bus} used to communicate with the vehicle
     */
    public J1939(Bus bus) {
        this.bus = bus;
    }

    /**
     * Reads the static field PGN from the given class. Returns null if the PGN
     * can't be read.
     *
     * @param  cls the class of interest
     * @return     PGN number based on ParsedPacket class
     */
    public static <T extends ParsedPacket> int getPgn(Class<T> cls) {
        try {
            return cls.getField("PGN").getInt(null);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Helper to detect if this response represents busy.
     */
    static private <T extends GenericPacket> boolean isBusy(Either<T, AcknowledgmentPacket> e) {
        return e.right.stream().anyMatch(p -> p.getResponse() == BUSY);
    }

    /**
     * Used for development to detect DMs that are manually parsed.
     */
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
        J1939_84.getLogger().log(SEVERE, message, t);
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
     * @param  pgn the pgn that's being requested
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
     * @param  pgn  the PGN of the packet that's being request
     * @param  addr the address the request is being directed at
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

    private Predicate<Packet> dsCommandFilter(int command, int pgn, int requestDestination, int requestSource) {
        if (requestDestination == GLOBAL_ADDR || requestSource == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Invalid use of global.");
        }
        return // does the packet have the right ID
        (pgnFilter(pgn)
                       .or(ackNackFilter(pgn))
                       .or(ackNackFilter(command)))
                                                   // not something tool sent
                                                   .and(p -> !p.isTransmitted())
                                                   // is it addressed to tool or all
                                                   .and((p -> p.getDestination() == bus.getAddress()
                                                           || p.getDestination() == GLOBAL_ADDR
                                                           // A TP message to global will have a
                                                           // destination of 0
                                                           || (p.getDestination() == 0
                                                                   && p.getLength() > 8)))
                                                   // did it come from the right module or any if
                                                   // addressed to all
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

    public boolean isLogDeltaTime() {
        return logDeltaTime;
    }

    public void setLogDeltaTime(boolean logDeltaTime) {
        this.logDeltaTime = logDeltaTime;
    }

    /**
     * Count of warnings detected in J1939 and J1939TP.
     */
    public int getWarnings() {
        return warnings;
    }

    private Predicate<Packet> globalFilter(int pgn) {
        return
        // does the packet have the right ID
        (pgnFilter(pgn).or(ackNackFilter(pgn)))
                                               // not something tool sent
                                               .and(p -> !p.isTransmitted())
                                               // is it addressed to tool or all
                                               .and((p -> p.getDestination() == bus.getAddress()
                                                       || p.getDestination() == GLOBAL_ADDR
                                                       // A TP message to global will have a destination of 0
                                                       || (p.getDestination() == 0 && p.getLength() > 8)));
    }

    /**
     * Returns a Subclass of {@link ParsedPacket} that corresponds to the given
     * {@link Packet}
     *
     * @param  packet the {@link Packet} to process
     * @return        a subclass of {@link ParsedPacket}
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

    public GenericPacket emptyPacket(int pgn) {
        return (GenericPacket) processRaw(pgn, Packet.create(pgn, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    public Stream<ParsedPacket> processedStream(int time, TimeUnit unit) throws BusException {
        return read(time, unit).map(p -> processRaw(p.getPgn(), p));
    }

    static public ParsedPacket processRaw(int pgn, Packet packet) {
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

            case DM58RationalityFaultSpData.PGN:
                return new DM58RationalityFaultSpData(packet);

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

            case GhgTrackingModule.GHG_STORED_100_HR:
            case GhgTrackingModule.GHG_STORED_GREEN_HOUSE_100_HR:
            case GhgTrackingModule.GHG_STORED_HYBRID_100_HR:
            case GhgTrackingModule.GHG_STORED_HYBRID_CHG_DEPLETING_100_HR:
            case GhgTrackingModule.GHG_ACTIVE_100_HR:
            case GhgTrackingModule.GHG_ACTIVE_GREEN_HOUSE_100_HR:
            case GhgTrackingModule.GHG_ACTIVE_HYBRID_100_HR:
            case GhgTrackingModule.GHG_ACTIVE_HYBRID_CHG_DEPLETING_100_HR:
                return new GhgActiveTechnologyPacket(packet);

            case GhgTrackingModule.GHG_TRACKING_LIFETIME_GREEN_HOUSE_PG:
            case GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_PG:
            case GhgTrackingModule.GHG_TRACKING_LIFETIME_HYBRID_CHG_DEPLETING_PG:
            case GhgTrackingModule.GHG_TRACKING_LIFETIME_PG:
                return new GhgLifetimeActiveTechnologyPacket(packet);

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
            J1939_84.getLogger().log(Level.SEVERE, "Error while reading bus", e);
        }
        return Stream.empty();
    }

    /**
     * Reads the bus indefinitely
     *
     * @return              {@link Stream} of {@link ParsedPacket} s
     * @throws BusException if there is a problem reading the bus
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read() throws BusException {
        return read(365, TimeUnit.DAYS).map(this::process);
    }

    /**
     * Watches the bus for up to the timeout for the first packet that matches
     * the PGN in the given class
     *
     * @param  <T>     the Type of Packet to expect back
     * @param  T       the class of interest
     * @param  addr    the source address the packet should come from. NOTE do not
     *                     use the Global Address (0xFF) here
     * @param  timeout the maximum time to wait for a message
     * @param  unit    the {@link TimeUnit} for the timeout
     * @return         the resulting packet
     */
    @SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "There are no null checks back up the line")
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
     * @param  pg      the Type of Packet to expect back
     *                     the class of interest
     * @param  timeout the maximum time to wait for a message
     * @param  unit    the {@link TimeUnit} for the timeout
     * @return         the resulting packets in a Stream
     */
    public <T extends GenericPacket> Stream<Either<T, AcknowledgmentPacket>> read(int pg,
                                                                                  long timeout,
                                                                                  TimeUnit unit) {
        try {
            Stream<Packet> stream = read(timeout, unit);
            if (pg >= 0) {
                stream = stream.filter(pgnFilter(pg));
            }

            return stream.map(this::process);
        } catch (BusException e) {
            severe("Error reading packets", e);
        }
        return Stream.empty();
    }

    /**
     * Watches the bus for up to the timeout for all the packets that match the
     * PGN in the given class
     *
     * @param  <T>     the Type of Packet to expect back
     * @param  T       the class of interest
     * @param  timeout the maximum time to wait for a message
     * @param  unit    the {@link TimeUnit} for the timeout
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

    public Stream<Packet> read(long timeout, TimeUnit unit) throws BusException {
        return bus.read(timeout, unit)
                  // only return complete and valid packets (not broken TP packets).
                  .filter(Packet::isValid);
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            Class<T> clas,
                                                            int address,
                                                            CommunicationsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, address);
        return requestDS(title, pgn, requestPacket, listener);
    }

    public <T extends GenericPacket> BusResult<T> requestDS(String title,
                                                            int pgn,
                                                            Packet request,
                                                            CommunicationsListener listener) {
        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " Destination Specific " + title + " Request to "
                    + Lookup.getAddressName(request.getDestination()));
        }

        // 6.II.B
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
                                                                                              CommunicationsListener listener) {

        if (request.getDestination() == GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request to global.");
        }

        try {
            Stream<Packet> packetStream = read(DS_TIMEOUT, MILLISECONDS);
            Packet sent = bus.send(request);
            LocalDateTime lateTime;
            if (sent != null) {
                listener.onResult(sent.toTimeString());
                lateTime = sent.getTimestamp().plus(GLOBAL_WARN_TIMEOUT, ChronoUnit.MILLIS);
            } else {
                logWarning(listener, FAILED_TO_SEND + request);
                lateTime = null;
            }
            Stream<Either<T, AcknowledgmentPacket>> stream = packetStream.filter(after(sent).and(dsFilter(pgn,
                                                                                                          request.getDestination(),
                                                                                                          getBusAddress())))
                                                                         .map(this::process);
            Optional<Either<T, AcknowledgmentPacket>> result = stream.findFirst();
            result.ifPresentOrElse(p -> {
                ParsedPacket pp = p.resolve();
                logResponse(listener, sent, pp.getPacket());
                listener.onResult(pp.toString());

                if (lateTime != null && pp.getPacket().getFragments().get(0).getTimestamp().isAfter(lateTime)) {
                    logTiming(listener, LATE_RESPONSE + " " + pp.getPacket().getFragments().get(0).toTimeString());
                }
            },
                                   () -> listener.onResult(getDateTimeModule().getTime() + " " + TIMEOUT_MESSAGE));

            return result;
        } catch (BusException e) {
            severe("Error requesting DS packet", e);
            return Optional.empty();
        }
    }

    static private Predicate<Packet> after(Packet sent) {
        return new Predicate<Packet>() {
            // sent == null for TP requests and some tests
            boolean pass = sent == null;

            @Override
            public boolean test(Packet p) {
                // p == null comes from tests
                pass |= (p == sent || p == null);
                return pass && p != null;
            }
        };
    }

    private void logResponse(CommunicationsListener listener, Packet sent, Packet response) {
        if (logDeltaTime) {
            listener.onResult(response.toDeltaTimeString(sent));
        } else {
            listener.onResult(response.toTimeString());
        }
    }

    /**
     * Make a single Global request with standard wait bus read time specified @ 600ms.
     */
    public List<AcknowledgmentPacket> requestForAcks(CommunicationsListener listener, String title, int pgn) {
        return requestForAcks(listener, title, pgn, GLOBAL_TIMEOUT, MILLISECONDS);
    }

    /**
     * Make a single Global request with wait bus read time specified.
     */
    public List<AcknowledgmentPacket>
           requestForAcks(CommunicationsListener listener, String title, int pgn, long timeOut, TimeUnit timeUnit) {
        listener.onResult("");
        listener.onResult(getDateTimeModule().getTime() + " " + title);
        Packet requestPacket = createRequestPacket(pgn, GLOBAL_ADDR);
        return requestGlobalOnce(pgn, requestPacket, listener, timeOut, timeUnit)
                                                                                 .stream()
                                                                                 .flatMap(e -> e.right.stream())
                                                                                 .collect(Collectors.toList());
    }

    /**
     * Make a single DS request with no retries.
     */
    public List<AcknowledgmentPacket>
           requestForAcks(CommunicationsListener listener, String title, int pgn, int address) {
        listener.onResult("");
        listener.onResult(getDateTimeModule().getTime() + " " + title);
        Packet requestPacket = createRequestPacket(pgn, address);
        return requestDSOnce(pgn, requestPacket, listener)
                                                          .stream()
                                                          .flatMap(e -> e.right.stream())
                                                          .collect(Collectors.toList());
    }

    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    Class<T> clas,
                                                                    CommunicationsListener listener) {
        int pgn = getPgn(clas);
        Packet requestPacket = createRequestPacket(pgn, GLOBAL_ADDR);
        return requestGlobal(title, pgn, requestPacket, listener);
    }

    /**
     * 1. send request to global
     * 2. collect responses announced within 600 ms
     * 3. if any responses are NACK 03
     * 3.1 send request to global
     * 3.2 collect responses announced within 600 ms
     * 4. for any NACK 03 responses, replace with results of DS request (announced within 600 ms)
     * 5. for any NACK 03 responses, replace with results of DS request (announced within 600 ms)
     */
    public <T extends GenericPacket> RequestResult<T> requestGlobal(String title,
                                                                    int pgn,
                                                                    Packet requestPacket,
                                                                    CommunicationsListener listener) {
        boolean retry = false;

        if (title != null) {
            listener.onResult(getDateTimeModule().getTime() + " Global " + title + " Request");
        }

        Collection<Either<T, AcknowledgmentPacket>> results = requestGlobalOnce(pgn, requestPacket, listener);

        if (results.stream().anyMatch(J1939::isBusy)) {
            retry = true;

            // use map to collate by address
            Map<Integer, Either<T, AcknowledgmentPacket>> map = results.stream()
                                                                       .collect(Collectors.toMap(r1 -> ((ParsedPacket) r1.resolve()).getSourceAddress(),
                                                                                                 r1 -> r1,
                                                                                                 // just take second if
                                                                                                 // there are multiple
                                                                                                 (a, b) -> b));
            List<Either<T, AcknowledgmentPacket>> retryResults = requestGlobalOnce(pgn, requestPacket, listener);
            map.putAll(retryResults.stream()
                                   // don't overwrite with busy responses, but do add them if
                                   // not already in map
                                   .filter(e -> !isBusy(e)
                                           || !map.containsKey(((ParsedPacket) e.resolve()).getSourceAddress()))
                                   .collect(Collectors.toMap(r1 -> ((ParsedPacket) r1.resolve()).getSourceAddress(),
                                                             r1 -> r1,
                                                             // if there are two responses, take the first. This should
                                                             // only happen if there are rogue tools on the bus.
                                                             (a, b) -> a)));
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
                                     logInfo(
                                             "first DS request after global busy NACK: " + dsRequest + " -> "
                                                     + response);
                                     response = requestDSOnce(pgn, dsRequest, listener);
                                     if (response.map(J1939::isBusy).orElse(true)) {
                                         logInfo(
                                                 "second DS request after global busy NACK: " + dsRequest + " -> "
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
                                                                                              CommunicationsListener listener) {
        return requestGlobalOnce(pgn, request, listener, GLOBAL_TIMEOUT, MILLISECONDS);
    }

    /**
     * Request from global only once.
     */
    private <T extends GenericPacket> List<Either<T, AcknowledgmentPacket>> requestGlobalOnce(int pgn,
                                                                                              Packet request,
                                                                                              CommunicationsListener listener,
                                                                                              long timeOut,
                                                                                              TimeUnit timeUnit) {
        if (request.getDestination() != GLOBAL_ADDR) {
            throw new IllegalArgumentException("Request not to global.");
        }

        List<Either<T, AcknowledgmentPacket>> result;
        try (Stream<Packet> stream = read(timeOut, timeUnit)) {
            Packet sent = bus.send(request);
            LocalDateTime lateTime;
            if (sent != null) {
                listener.onResult(sent.toTimeString());
                lateTime = sent.getTimestamp().plus(GLOBAL_WARN_TIMEOUT, ChronoUnit.MILLIS);
            } else {
                logWarning(listener, FAILED_TO_SEND + request);
                lateTime = null;
            }
            List<Packet> lateBam = new ArrayList<>();
            result = stream.filter(after(sent))
                           .filter(globalFilter(pgn))
                           .peek(p -> {
                               /*
                                * If the first fragment arrived after lateBam, then it
                                * is late.
                                */
                               if (lateTime != null && p.getFragments().size() > 0
                                       && p.getFragments().get(0).getTimestamp().isAfter(lateTime)
                               // only record first one
                                       && !lateBam.contains(p)) {
                                   lateBam.add(p);
                               }
                           })
                           // Collect all of the packets, even though they are not
                           // complete. They were all announced in time.
                           .collect(Collectors.toList())
                           .stream()
                           .map(rawPacket -> {
                               try {
                                   logResponse(listener, sent, rawPacket);
                                   Either<T, AcknowledgmentPacket> pp = process(rawPacket);
                                   listener.onResult(pp.resolve().toString());
                                   listener.onResult("");
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
                logTiming(listener, LATE_RESPONSE + " " + p.getFragments().get(0).toTimeString());
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
                                                                     CommunicationsListener listener) {
        if (address == GLOBAL_ADDR) {
            throw new IllegalArgumentException("DM7 request to global.");
        }

        Packet request = DM7CommandTestsPacket.create(getBusAddress(), address, tid, spn, fmi).getPacket();

        listener.onResult("");
        String title = "Sending DM7 for DM30 to " + Lookup.getAddressName(address) + " for SPN " + spn;
        listener.onResult(getDateTimeModule().getTime() + " " + title);

        try {
            BusResult<DM30ScaledTestResultsPacket> result;
            for (int i = 0; true; i++) {
                Stream<Packet> packetStream = read(DS_TIMEOUT, MILLISECONDS);
                Packet sent = bus.send(request);
                if (sent != null) {
                    listener.onResult(sent.toTimeString());
                } else {
                    logWarning(listener, FAILED_TO_SEND + request);
                }
                Stream<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> stream = packetStream
                                                                                                       .filter(after(sent).and(dsCommandFilter(DM7CommandTestsPacket.PGN,
                                                                                                                                               DM30ScaledTestResultsPacket.PGN,
                                                                                                                                               request.getDestination(),
                                                                                                                                               getBusAddress())))
                                                                                                       .map(this::process);
                Optional<Either<DM30ScaledTestResultsPacket, AcknowledgmentPacket>> first = stream.findFirst();
                result = new BusResult<>(i > 0, first);
                result.getPacket().ifPresentOrElse(p -> {
                    GenericPacket response = p.resolve();
                    logResponse(listener, sent, response.getPacket());
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

    public BusResult<DM58RationalityFaultSpData> requestRationalityTestResults(CommunicationsListener listener,
                                                                               int address,
                                                                               int spn) {

        int tid = 245;
        int fmi = 31;
        if (address == GLOBAL_ADDR) {
            listener.onResult("DM7 request to global.");
            throw new IllegalArgumentException("DM7 request to global.");
        }

        Packet request = DM7CommandTestsPacket.create(getBusAddress(), address, tid, spn, fmi).getPacket();

        String title = "Sending DM7 for DM58 to " + Lookup.getAddressName(address) + " for SPN " + spn;
        listener.onResult(getDateTimeModule().getTime() + " " + title);

        try {
            BusResult<DM58RationalityFaultSpData> result;
            for (int i = 0; true; i++) {
                Stream<Packet> packetStream = read(DS_TIMEOUT, MILLISECONDS);
                Packet sent = bus.send(request);
                if (sent != null) {
                    listener.onResult(sent.toTimeString());
                } else {
                    logWarning(listener, FAILED_TO_SEND + request);
                }

                Stream<Either<DM58RationalityFaultSpData, AcknowledgmentPacket>> stream = packetStream
                                                                                                      .filter(after(sent).and(dsCommandFilter(DM7CommandTestsPacket.PGN,
                                                                                                                                              DM58RationalityFaultSpData.PGN,
                                                                                                                                              request.getDestination(),
                                                                                                                                              getBusAddress())))
                                                                                                      .map(this::process);
                Optional<Either<DM58RationalityFaultSpData, AcknowledgmentPacket>> first = stream.findFirst();
                result = new BusResult<>(i > 0, first);
                result.getPacket().ifPresentOrElse(p -> {
                    GenericPacket response = p.resolve();
                    logResponse(listener, sent, response.getPacket());
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

    private void logTiming(CommunicationsListener listener, String message) {
        warnings++;
        listener.onResult(message);
        J1939_84.getLogger().warning(message);
    }

    private void logWarning(CommunicationsListener listener, String message) {
        listener.onResult(message);
        J1939_84.getLogger().warning(message);
    }

    private void logInfo(String message) {
        J1939_84.getLogger().info(message);
    }

    public void startLogger() throws BusException {
        Instant start = Instant.now();
        // do not crash tests that do not include a raw bus.
        loggerStream = (bus.getRawBus() == null ? bus : bus.getRawBus()).read(Integer.MAX_VALUE, TimeUnit.DAYS);
        new Thread(() -> {
            try {
                final String PREFIX = "J1939-84-CAN-";
                final String SUFFIX = ".asc";
                File file = File.createTempFile(PREFIX, SUFFIX);
                // delete all but last 10 logs
                Stream.of(file.getParentFile()
                              .listFiles((dir, name) -> name.startsWith(PREFIX) && name.endsWith(SUFFIX)))
                      .sorted(Comparator.comparing(f -> -f.lastModified()))
                      .skip(10)
                      .forEach(f -> f.delete());
                try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
                    out.println("base hex timestamps absolute");
                    loggerStream.forEach(p -> {
                        try {
                            out.println(p.toVectorString(start));
                        } catch (Throwable t) {
                            out.println(t.getMessage());
                            J1939_84.getLogger().log(Level.WARNING, "Packet Failure", t);
                        }
                    });
                }
            } catch (Throwable e) {
                J1939_84.getLogger().log(Level.SEVERE, "Unable to log packets.", e);
            }
        }).start();
    }

    public void closeLogger() {
        loggerStream.close();
    }
}
