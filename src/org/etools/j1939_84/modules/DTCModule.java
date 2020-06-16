/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * The Diagnostic Trouble Code Module that is responsible for Requesting or
 * Clearing the DTCs from the vehicle
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DTCModule extends FunctionalModule {

    /**
     * The string written to the report indicating the DTCs were cleared
     */
    public static final String DTCS_CLEARED = "Diagnostic Trouble Codes were successfully cleared.";

    /**
     * Constructor
     */
    public DTCModule() {
        this(new DateTimeModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param dateTimeModule
     *                       the {@link DateTimeModule}
     */
    public DTCModule(DateTimeModule dateTimeModule) {
        super(dateTimeModule);
    }

    /**
     * Sends an destination specific request for DM2 Packets. The request and
     * results
     * will be returned to the {@link ResultsListener}
     *
     * @param listener         the {@link ResultsListener} for the results
     * @param fullString       true to include the full string of the results in the
     *                         report;
     *                         false to only include the returned raw packet in the
     *                         report
     * @param obdModuleAddress the address to which the destination specific request
     *                         will be sent
     * @return the {@link List} of {@link DM2PreviouslyActiveDTC}s
     */
    public List<DM2PreviouslyActiveDTC> getDM2Packets(ResultsListener listener,
            boolean fullString,
            int obdModuleAddress) {
        return filterPackets(requestDM2(listener, fullString, obdModuleAddress).getPackets(),
                DM2PreviouslyActiveDTC.class);
    }

    /**
     * Requests DM11 from OBD modules and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *                   the {@link ResultsListener} that will be given the report
     * @param obdModules
     *                   the source address for the OBD Modules
     * @return true if there are no NACKs from the OBD Modules; false if an OBD
     *         Module NACK'd the request or didn't respond
     */
    public boolean reportDM11(ResultsListener listener, List<Integer> obdModules) {
        boolean[] result = new boolean[] { true };
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        Packet requestPacket = getJ1939().createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);
        listener.onResult(getTime() + " Global DM11 Request");
        listener.onResult(getTime() + " " + requestPacket);

        Stream<ParsedPacket> results = getJ1939()
                .requestRaw(DM11ClearActiveDTCsPacket.class,
                        requestPacket,
                        5500,
                        TimeUnit.MILLISECONDS);

        List<String> responses = results.peek(t -> {
            if (obdModules.contains(t.getSourceAddress())
                    && t instanceof AcknowledgmentPacket
                    && ((AcknowledgmentPacket) t).getResponse() != Response.ACK) {
                result[0] = false;
            }
        }).map(getPacketMapperFunction()).collect(Collectors.toList());
        listener.onResult(responses);

        if (result[0]) {
            listener.onResult(DTCS_CLEARED);
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }
        return result[0];
    }

    /**
     * Requests DM12 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public boolean reportDM12(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM12MILOnEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM12 Request",
                DM12MILOnEmissionDTCPacket.class,
                request).stream().filter(p -> p instanceof DM12MILOnEmissionDTCPacket)
                        .map(p -> (DM12MILOnEmissionDTCPacket) p).collect(Collectors.toList());
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests and reports DM2 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public boolean reportDM2(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM2PreviouslyActiveDTC.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM2 Request",
                DM2PreviouslyActiveDTC.class,
                request).stream().filter(p -> p instanceof DM2PreviouslyActiveDTC)
                        .map(p -> (DM2PreviouslyActiveDTC) p).collect(Collectors.toList());
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests DM23 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public boolean reportDM23(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM23PreviouslyMILOnEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM23 Request",
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                request).stream().filter(p -> p instanceof DM23PreviouslyMILOnEmissionDTCPacket)
                        .map(p -> (DM23PreviouslyMILOnEmissionDTCPacket) p).collect(Collectors.toList());
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests DM28 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public boolean reportDM28(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM28PermanentEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM28 Request",
                DM28PermanentEmissionDTCPacket.class,
                request).stream().filter(p -> p instanceof DM28PermanentEmissionDTCPacket)
                        .map(p -> (DM28PermanentEmissionDTCPacket) p).collect(Collectors.toList());
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests DM6 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *                 the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public boolean reportDM6(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM6PendingEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM6 Request",
                DM6PendingEmissionDTCPacket.class,
                request).stream().filter(p -> p instanceof DM6PendingEmissionDTCPacket)
                        .map(p -> (DM6PendingEmissionDTCPacket) p).collect(Collectors.toList());
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests DM11 from OBD modules and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     * the {@link ResultsListener} that will be given the report
     * @param obdModules
     * the source address for the OBD Modules
     * @return true if there are no NACKs from the OBD Modules; false if an OBD
     * Module NACK'd the request or didn't respond
     */
    public RequestResult<ParsedPacket> requestDM11(ResultsListener listener, List<Integer> obdModules) {
        boolean[] result = new boolean[] { true };
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        Packet requestPacket = getJ1939().createRequestPacket(DM11ClearActiveDTCsPacket.PGN, GLOBAL_ADDR);
        listener.onResult(getTime() + " Global DM11 Request");
        listener.onResult(getTime() + " " + requestPacket);

        Stream<ParsedPacket> results = getJ1939()
                .requestRaw(DM11ClearActiveDTCsPacket.class,
                        requestPacket,
                        5500,
                        TimeUnit.MILLISECONDS);

        List<String> responses = results.peek(t -> {
            if (obdModules.contains(t.getSourceAddress())
                    && t instanceof AcknowledgmentPacket
                    && ((AcknowledgmentPacket) t).getResponse() != Response.ACK) {
                result[0] = false;
            }
        }).map(getPacketMapperFunction()).collect(Collectors.toList());
        listener.onResult(responses);

        if (result[0]) {
            listener.onResult(DTCS_CLEARED);
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }
        return new RequestResult<>(false, results.collect(Collectors.toList()));

    }

    /**
     * Requests DM12 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     * the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM12(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM12MILOnEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<ParsedPacket> packets = generateReport(listener,
                "Global DM12 Request",
                DM12MILOnEmissionDTCPacket.class,
                request);
        return new RequestResult<>(false, packets);
    }

    /**
     * Requests and return global DM2 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM2(ResultsListener listener, boolean fullString) {

        return getPackets("Global DM2 Request",
                DM2PreviouslyActiveDTC.PGN,
                DM2PreviouslyActiveDTC.class,
                listener,
                fullString);

    }

    /**
     * Requests and return destination specific DM2 from all vehicle modules and
     * generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM2(ResultsListener listener, boolean fullString, int obdAddress) {

        return getPackets("Destination Specific DM2 Request",
                DM2PreviouslyActiveDTC.PGN,
                DM2PreviouslyActiveDTC.class,
                listener,
                fullString,
                obdAddress);

    }

    /**
     * Requests DM23 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     * the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM23(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM23PreviouslyMILOnEmissionDTCPacket.PGN, GLOBAL_ADDR);
        List<ParsedPacket> packets = generateReport(listener,
                "Global DM23 Request",
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                request).stream().filter(p -> p instanceof DM23PreviouslyMILOnEmissionDTCPacket)
                        .map(p -> (DM23PreviouslyMILOnEmissionDTCPacket) p).collect(Collectors.toList());
        return new RequestResult<>(false, packets);
    }

    /**
     * Sends a request to the vehicle for {@link DM25ExpandedFreezeFrame}s
     *
     * @param listener the {@link ResultsListener}
     * @param obdModuleAddresses {@link Collection} of Integers}
     * @return {@link List} of {@link DM25ExpandedFreezeFrame}s
     */
    public RequestResult<ParsedPacket> requestDM25(ResultsListener listener,
            Collection<Integer> obdModuleAddresses) {
        List<ParsedPacket> packets = new ArrayList<>();
        boolean retryUsed = false;

        for (int address : obdModuleAddresses) {
            Packet request = getJ1939().createRequestPacket(DM25ExpandedFreezeFrame.PGN, address);
            listener.onResult(getTime() + " Direct DM25 Request to " + Lookup.getAddressName(address));
            listener.onResult(getTime() + " " + request.toString());
            Optional<BusResult<DM25ExpandedFreezeFrame>> results = getJ1939()
                    .requestPacket(request, DM25ExpandedFreezeFrame.class, address, 3, TimeUnit.SECONDS.toMillis(15));
            if (!results.isPresent()) {
                listener.onResult(TIMEOUT_MESSAGE);
            } else {
                DM25ExpandedFreezeFrame packet = results.get().getPacket();
                listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
                listener.onResult(packet.toString());
                packets.add(packet);
            }
            listener.onResult("");
        }
        return new RequestResult<>(retryUsed, packets);
    }

    /**
     * Requests DM29 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     * the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM29(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM29DtcCounts.PGN, GLOBAL_ADDR);
        List<ParsedPacket> packets = generateReport(listener,
                "Global DM29 Request",
                DM29DtcCounts.class,
                request).stream().filter(p -> p instanceof DM29DtcCounts)
                        .map(p -> (DM29DtcCounts) p).collect(Collectors.toList());
        return new RequestResult<>(false, packets);
    }

    /**
     * Requests DM6 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     * the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<ParsedPacket> requestDM6(ResultsListener listener, List<Integer> obdModules) {
        boolean[] result = new boolean[] { true };
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        Packet requestPacket = getJ1939().createRequestPacket(DM6PendingEmissionDTCPacket.PGN, GLOBAL_ADDR);
        listener.onResult(getTime() + " Global DM6 Request");
        listener.onResult(getTime() + " " + requestPacket);

        Stream<ParsedPacket> results = getJ1939()
                .requestRaw(DM6PendingEmissionDTCPacket.class,
                        requestPacket,
                        5500,
                        TimeUnit.MILLISECONDS);

        List<String> responses = results.peek(t -> {
            if (obdModules.contains(t.getSourceAddress())
                    && t instanceof AcknowledgmentPacket
                    && ((AcknowledgmentPacket) t).getResponse() != Response.ACK) {
                result[0] = false;
            }
        }).map(getPacketMapperFunction()).collect(Collectors.toList());
        listener.onResult(responses);

        if (result[0]) {
            listener.onResult(DTCS_CLEARED);
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }
        return new RequestResult<>(false, results.collect(Collectors.toList()));

    }
}
