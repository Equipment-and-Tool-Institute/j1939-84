/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;

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
                request);
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
                request);
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
                request);
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
                request);
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
                request);
        return packets.stream().anyMatch(t -> !t.getDtcs().isEmpty());
    }

    /**
     * Requests and return global DM2 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public List<? extends DiagnosticTroubleCodePacket> requestDM2(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM2PreviouslyActiveDTC.PGN, GLOBAL_ADDR);
        List<? extends DiagnosticTroubleCodePacket> packets = generateReport(listener,
                "Global DM2 Request",
                DM2PreviouslyActiveDTC.class,
                request);
        return packets;
    }
}
