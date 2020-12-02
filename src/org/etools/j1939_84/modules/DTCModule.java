/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
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
        super();
    }

    public RequestResult<DM1ActiveDTCsPacket> readDM1(ResultsListener listener, boolean fullString) {
        String title = " Reading the bus for published DM1 messages";
        listener.onResult(getTime() + title);

        Collection<DM1ActiveDTCsPacket> allPackets = getJ1939()
                .read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS)
                .flatMap(r -> r.left.stream())
                .collect(Collectors.toMap(p -> p.getSourceAddress(), p -> p, (address, packet) -> address))
                .values();

        List<DM1ActiveDTCsPacket> packets = new ArrayList<>(allPackets);
        packets.sort((o1, o2) -> o1.getPacket().getTimestamp().compareTo(o2.getPacket().getTimestamp()));

        if (packets.isEmpty()) {
            listener.onResult(getTime() + " No published DM1 messages were identified");
        } else {
            for (DM1ActiveDTCsPacket p : packets) {
                listener.onResult(p.toString());
            }
        }

        return new RequestResult<>(false, packets, Collections.emptyList());
    }

    public RequestResult<DM28PermanentEmissionDTCPacket> reportDM28(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM28PermanentEmissionDTCPacket.PGN, address);

        String title = address == GLOBAL_ADDR ? "Global DM28 Request"
                : "Destination Specific DM28 Request to " + Lookup.getAddressName(address);

        return generateReport(listener,
                title,
                DM28PermanentEmissionDTCPacket.class,
                request);
    }

    /**
     * Send Global DM11 Request and generates a {@link String} that's suitable
     * for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @param obdModules
     *            the source address for the OBD Modules
     */
    public <T extends ParsedPacket> RequestResult<DM11ClearActiveDTCsPacket> requestDM11(ResultsListener listener) {
        final int address = GLOBAL_ADDR;
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");
        Packet requestPacket = getJ1939().createRequestPacket(DM11ClearActiveDTCsPacket.PGN, address);

        var results = getJ1939()
                .requestResult("Global DM11 Request", listener, false, DM11ClearActiveDTCsPacket.class, requestPacket);

        if (!results.getAcks().isEmpty() && results.getAcks().stream().allMatch(t -> t.getResponse() == Response.ACK)) {
            listener.onResult(DTCS_CLEARED);
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }

        return results;
    }

    /**
     * Requests DM12 from all vehicle modules and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM12MILOnEmissionDTCPacket> requestDM12(ResultsListener listener, boolean fullString) {
        return getPacketsFromGlobal("Global DM12 Request",
                DM12MILOnEmissionDTCPacket.PGN,
                DM12MILOnEmissionDTCPacket.class,
                listener,
                fullString);
    }

    /**
     * Requests DM12 from a specific vehicle modules address and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public BusResult<DM12MILOnEmissionDTCPacket> requestDM12(ResultsListener listener, boolean fullString,
            int moduleAddress) {
        return getPacketDS("Destination Specific DM12 Request to " + Lookup.getAddressName(moduleAddress),
                DM12MILOnEmissionDTCPacket.PGN,
                DM12MILOnEmissionDTCPacket.class,
                listener,
                fullString,
                moduleAddress);
    }

    /**
     * Requests and return global DM2 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener, boolean fullString) {
        return getPacketsFromGlobal("Global DM2 Request", DM2PreviouslyActiveDTC.PGN,
                DM2PreviouslyActiveDTC.class,
                listener,
                fullString);
    }

    /**
     * Requests and return destination specific DM2 from all vehicle modules and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public BusResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener, boolean fullString,
            int obdAddress) {
        return getPacketDS("Destination Specific DM2 Request to " + Lookup.getAddressName(obdAddress),
                DM2PreviouslyActiveDTC.PGN,
                DM2PreviouslyActiveDTC.class,
                listener,
                fullString,
                obdAddress);
    }

    /**
     * Requests global DM21 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener) {
        return requestDM21(listener, GLOBAL_ADDR);
    }

    /**
     * Requests DM21 from the address specific vehicle module and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM21DiagnosticReadinessPacket.PGN, address);
        String title = address == GLOBAL_ADDR ? "Global DM21 Request"
                : "Destination Specific DM21 Request to " + Lookup.getAddressName(address);

        return generateReport(listener,
                title,
                DM21DiagnosticReadinessPacket.class,
                request);
    }

    /**
     * Global request for DM23 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener,
            boolean fullString) {
        return getPacketsFromGlobal("Global DM23 Request",
                DM23PreviouslyMILOnEmissionDTCPacket.PGN,
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                listener,
                fullString);
    }

    /**
     * Destination specific request for DM23 from vehicle modules specified by
     * address and generates a {@link String} that's suitable for inclusion in
     * the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public BusResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener, boolean fullString,
            int address) {
        return getPacketDS("Destination Specific DM23 Request to " + Lookup.getAddressName(address),
                DM23PreviouslyMILOnEmissionDTCPacket.PGN,
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                listener,
                fullString,
                address);
    }

    /**
     * Sends a request to the vehicle for {@link DM25ExpandedFreezeFrame}s
     *
     * @param listener
     *            the {@link ResultsListener}
     * @param moduleAddress
     *            the address to send the request to
     * @return {@link List} of {@link DM25ExpandedFreezeFrame}s
     */
    public BusResult<DM25ExpandedFreezeFrame> requestDM25(ResultsListener listener, int moduleAddress) {

        Packet request = getJ1939().createRequestPacket(DM25ExpandedFreezeFrame.PGN, moduleAddress);
        String message = "Destination Specific DM25 Request to " + Lookup.getAddressName(moduleAddress);

        return getJ1939().requestDS(message, listener, true, DM25ExpandedFreezeFrame.class, request);
    }

    /**
     * Requests global DM26 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(ResultsListener listener) {
        return requestDM26(listener, GLOBAL_ADDR);
    }

    /**
     * Requests destination specific DM26 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM26TripDiagnosticReadinessPacket.PGN, address);

        String title = address == GLOBAL_ADDR ? "Global DM26 Request"
                : "Destination Specific DM26 Request to " + Lookup.getAddressName(address);

        return generateReport(listener,
                title,
                DM26TripDiagnosticReadinessPacket.class,
                request);
    }

    /**
     * Requests global DM27 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM27AllPendingDTCsPacket> requestDM27(ResultsListener listener, boolean fullString) {
        String title = "Global DM27 Request";

        return getPacketsFromGlobal(title, DM27AllPendingDTCsPacket.PGN, DM27AllPendingDTCsPacket.class,
                listener, fullString);
    }

    /**
     * Requests destination specific DM27 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public BusResult<DM27AllPendingDTCsPacket> requestDM27(ResultsListener listener, boolean fullString,
            int address) {
        return getPacketDS("Destination Specific DM27 Request to " + Lookup.getAddressName(address),
                DM27AllPendingDTCsPacket.PGN,
                DM27AllPendingDTCsPacket.class,
                listener,
                fullString,
                address);
    }

    /**
     * Requests global DM28 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener, boolean fullString) {
        String title = "Global DM28 Request";

        return getPacketsFromGlobal(title, DM28PermanentEmissionDTCPacket.PGN, DM28PermanentEmissionDTCPacket.class,
                listener, fullString);
    }

    /**
     * Requests destination specific DM28 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public BusResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener, boolean fullString,
            int address) {
        return getPacketDS("Destination Specific DM28 Request to " + Lookup.getAddressName(address),
                DM28PermanentEmissionDTCPacket.PGN,
                DM28PermanentEmissionDTCPacket.class,
                listener,
                fullString,
                address);
    }

    /**
     * Requests DM29 from vehicle modules with the address provided and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @param obdAddress
     *            the address of the module from which the DM29 is to be
     *            requested
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM29DtcCounts> requestDM29(ResultsListener listener) {

        Packet request = getJ1939().createRequestPacket(DM29DtcCounts.PGN, GLOBAL_ADDR);

        return generateReport(listener,
                "Global DM29 Request",
                DM29DtcCounts.class,
                request);
    }

    /**
     * Requests DM29 from vehicle modules with the address provided and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @param obdAddress
     *            the address of the module from which the DM29 is to be
     *            requested
     * @return true if there were any DTCs returned
     */
    public BusResult<DM29DtcCounts> requestDM29(ResultsListener listener, int obdAddress) {
        return getPacketDS("Desination Specific DM29 Request to " + Lookup.getAddressName(obdAddress),
                DM29DtcCounts.PGN,
                DM29DtcCounts.class,
                listener,
                true,
                obdAddress);
    }

    /**
     * Requests DM31 from all vehicle module and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM31DtcToLampAssociation> requestDM31(ResultsListener listener) {
        return requestDM31(listener, GLOBAL_ADDR);
    }

    /**
     * Requests DM31 from the address specific vehicle module and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM31DtcToLampAssociation> requestDM31(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM31DtcToLampAssociation.PGN, address);

        String title = address == GLOBAL_ADDR ? "Global DM31 Request"
                : "Destination Specific DM31 Request to " + Lookup.getAddressName(address);

        return generateReport(listener,
                title,
                DM31DtcToLampAssociation.class,
                request);
    }

    /**
     * Requests global DM33 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> requestDM33(
            ResultsListener listener) {
        return requestDM33(listener, GLOBAL_ADDR);
    }

    /**
     * Requests destination specific DM33 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> requestDM33(
            ResultsListener listener, int address) {
        Packet request = getJ1939()
                .createRequestPacket(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN, address);

        String title = address == GLOBAL_ADDR ? "Global DM33 Request"
                : "Destination Specific DM33 Request to " + Lookup.getAddressName(address);

        return generateReport(listener,
                title,
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class,
                request);
    }

    /**
     * Requests a global DM6 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener) {
        return requestDM6(listener, GLOBAL_ADDR);
    }

    /**
     * Requests a destination specific DM6 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener, Integer address) {
        Packet request = getJ1939().createRequestPacket(DM6PendingEmissionDTCPacket.PGN, address);

        String title = address == GLOBAL_ADDR ? " Global DM6 Request"
                : " Destination Specific DM6 Request to " + Lookup.getAddressName(address);

        listener.onResult(getTime() + title);
        listener.onResult(getTime() + " " + request);

        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, getJ1939()
                .requestRaw(DM6PendingEmissionDTCPacket.class,
                        request)
                .collect(Collectors.toList()));
        listener.onResult(result.getPackets().stream().map(getPacketMapperFunction()).collect(Collectors.toList()));

        return result;
    }
}
