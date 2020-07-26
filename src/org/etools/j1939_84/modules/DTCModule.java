/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.Either;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DM31ScaledTestResults;
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
        this(new DateTimeModule());
    }

    /**
     * Constructor exposed for testing
     *
     * @param dateTimeModule
     *            the {@link DateTimeModule}
     */
    public DTCModule(DateTimeModule dateTimeModule) {
        super(dateTimeModule);
    }

    /**
     * Send Global DM11 Request and generates a {@link String} that's suitable
     * for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @param obdModules
     *            the source address for the OBD Modules
     * @return true if there are no NACKs from the OBD Modules; false if an OBD
     *         Module NACK'd the request or didn't respond
     */
    public <T extends ParsedPacket> RequestResult<DM11ClearActiveDTCsPacket> requestDM11(ResultsListener listener) {
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        Packet requestPacket = getJ1939().createRequestPacket(DM11ClearActiveDTCsPacket.PGN,
                GLOBAL_ADDR);
        listener.onResult(getTime() + " Global DM11 Request");
        listener.onResult(getTime() + " " + requestPacket);

        return requestDM11(listener, requestPacket);
    }

    /**
     * Requests DM11 from OBD modules and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @param obdModules
     *            the source address for the OBD Modules
     * @return true if there are no NACKs from the OBD Modules; false if an OBD
     *         Module NACK'd the request or didn't respond
     */
    public <T extends ParsedPacket> RequestResult<DM11ClearActiveDTCsPacket> requestDM11(ResultsListener listener,
            Integer obdModule) {
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        Packet requestPacket = getJ1939().createRequestPacket(DM11ClearActiveDTCsPacket.PGN, obdModule);
        listener.onResult(getTime() + " Destination Specific DM11 Request");
        listener.onResult(getTime() + " " + requestPacket);

        return requestDM11(listener, requestPacket);

    }

    private <T extends ParsedPacket> RequestResult<DM11ClearActiveDTCsPacket> requestDM11(ResultsListener listener,
            Packet requestPacket) {

        // FIXME, where did 5.5 s come from?
        List<Either<DM11ClearActiveDTCsPacket, AcknowledgmentPacket>> results = getJ1939()
                .requestRaw(DM11ClearActiveDTCsPacket.class,
                        requestPacket,
                        5500,
                        TimeUnit.MILLISECONDS)
                .collect(Collectors.toList());

        listener.onResult(results.stream().map(e -> e.right)
                .filter(o -> o.isPresent()).map(o -> o.get())
                .map(getPacketMapperFunction())
                .collect(Collectors.toList()));

        if (results.stream().map(e -> e.right).filter(o -> o.isPresent()).map(o -> o.get())
                .allMatch(t -> t.getResponse() == Response.ACK)) {
            listener.onResult(DTCS_CLEARED);
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }

        return new RequestResult<>(false, results);
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

        return getPackets("Global DM12 Request",
                DM12MILOnEmissionDTCPacket.PGN,
                DM12MILOnEmissionDTCPacket.class,
                listener,
                fullString,
                GLOBAL_ADDR);
    }

    /**
     * Requests DM12 from a specific vehicle modules address and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM12MILOnEmissionDTCPacket> requestDM12(ResultsListener listener, boolean fullString,
            int moduleAddress) {

        return getPackets("Destination Specific DM12 Request",
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

        return getPackets("Global DM2 Request",
                DM2PreviouslyActiveDTC.PGN,
                DM2PreviouslyActiveDTC.class,
                listener,
                fullString,
                GLOBAL_ADDR);

    }

    /**
     * Requests and return destination specific DM2 from all vehicle modules and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener, boolean fullString,
            int obdAddress) {

        return getPackets("Destination Specific DM2 Request",
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
        Packet request = getJ1939().createRequestPacket(DM21DiagnosticReadinessPacket.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM21 Request",
                DM21DiagnosticReadinessPacket.class,
                request));
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
        return new RequestResult<>(false, generateReport(listener,
                "Global DM21 Request",
                DM21DiagnosticReadinessPacket.class,
                request));
    }

    /**
     * Global request for DM23 from all vehicle modules and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM23PreviouslyMILOnEmissionDTCPacket.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM23 Request",
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                request));
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
    public RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM23PreviouslyMILOnEmissionDTCPacket.PGN, address);
        return new RequestResult<>(false, generateReport(listener,
                "Destination Specific DM23 Request",
                DM23PreviouslyMILOnEmissionDTCPacket.class,
                request));
    }

    /**
     * Sends a request to the vehicle for {@link DM25ExpandedFreezeFrame}s
     *
     * @param listener
     *            the {@link ResultsListener}
     * @param obdModuleAddresses
     *            {@link Collection} of Integers}
     * @return {@link List} of {@link DM25ExpandedFreezeFrame}s
     */
    public <T extends ParsedPacket> RequestResult<DM25ExpandedFreezeFrame> requestDM25(ResultsListener listener) {
        return requestDM25(listener, GLOBAL_ADDR);

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
    public <T extends ParsedPacket> RequestResult<DM25ExpandedFreezeFrame> requestDM25(ResultsListener listener,
            int moduleAddress) {

        Packet request = getJ1939().createRequestPacket(DM25ExpandedFreezeFrame.PGN, moduleAddress);

        String message = moduleAddress == GLOBAL_ADDR ? " Global DM25 Request to "
                : " Destination Specific DM25 Request to ";
        listener.onResult(getTime() + message + Lookup.getAddressName(moduleAddress));
        listener.onResult(getTime() + " " + request.toString());

        List<Either<DM25ExpandedFreezeFrame, AcknowledgmentPacket>> packets = new ArrayList<>();
        boolean retryUsed = false;

        BusResult<DM25ExpandedFreezeFrame> result = getJ1939()
                .requestPacket(request,
                        DM25ExpandedFreezeFrame.class,
                        moduleAddress,
                        3,
                        TimeUnit.SECONDS.toMillis(15))
                .orElse(null);

        ParsedPacket packet = null;
        if (result != null) {
            Either<DM25ExpandedFreezeFrame, AcknowledgmentPacket> either = result.getPacket();
            packets.add(either);
            packet = either.left.orElse(null);
            if (packet == null) {
                packet = either.right.orElse(null);
            }
        }

        if (packet == null) {
            listener.onResult(TIMEOUT_MESSAGE);
        } else {
            listener.onResult(packet.getPacket().toString(getDateTimeModule().getTimeFormatter()));
            listener.onResult(packet.toString());
        }
        return new RequestResult<>(retryUsed, packets);
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
        Packet request = getJ1939().createRequestPacket(DM26TripDiagnosticReadinessPacket.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM26 Request",
                DM26TripDiagnosticReadinessPacket.class,
                request));
    }

    /**
     * Requests global DM28 and generates a {@link String} that's suitable for
     * inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM28PermanentEmissionDTCPacket.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM28 Request",
                DM28PermanentEmissionDTCPacket.class,
                request));
    }

    /**
     * Requests destination specific DM28 and generates a {@link String} that's
     * suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM28PermanentEmissionDTCPacket.PGN, address);
        return new RequestResult<>(false, generateReport(listener,
                "Destination Specific DM28 Request",
                DM28PermanentEmissionDTCPacket.class,
                request));
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
        return new RequestResult<>(false, generateReport(listener,
                "Desination Specific DM29 Request",
                DM29DtcCounts.class,
                request));
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
    public RequestResult<DM29DtcCounts> requestDM29(ResultsListener listener, int obdAddress) {
        Packet request = getJ1939().createRequestPacket(DM29DtcCounts.PGN, obdAddress);
        return new RequestResult<>(false, generateReport(listener,
                "Desination Specific DM29 Request",
                DM29DtcCounts.class,
                request));
    }

    /**
     * Requests DM31 from all vehicle module and generates a {@link String}
     * that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM31ScaledTestResults> requestDM31(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM31ScaledTestResults.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM31 Request",
                DM31ScaledTestResults.class,
                request));
    }

    /**
     * Requests DM31 from the address specific vehicle module and generates a
     * {@link String} that's suitable for inclusion in the report
     *
     * @param listener
     *            the {@link ResultsListener} that will be given the report
     * @return true if there were any DTCs returned
     */
    public RequestResult<DM31ScaledTestResults> requestDM31(ResultsListener listener, int address) {
        Packet request = getJ1939().createRequestPacket(DM31ScaledTestResults.PGN, address);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM31 Request",
                DM31ScaledTestResults.class,
                request));
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
        Packet request = getJ1939()
                .createRequestPacket(DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.PGN, GLOBAL_ADDR);
        return new RequestResult<>(false, generateReport(listener,
                "Global DM33 Request",
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class,
                request));
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
        return new RequestResult<>(false, generateReport(listener,
                "Destination Specific DM33 Request",
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class,
                request));
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
        Packet request = getJ1939().createRequestPacket(DM6PendingEmissionDTCPacket.PGN, GLOBAL_ADDR);
        listener.onResult(getTime() + " Global DM6 Request");
        listener.onResult(getTime() + " " + request);

        RequestResult<DM6PendingEmissionDTCPacket> result = new RequestResult<>(false, getJ1939()
                .requestRaw(DM6PendingEmissionDTCPacket.class,
                        request,
                        5500,
                        TimeUnit.MILLISECONDS)
                .collect(Collectors.toList()));
        listener.onResult(result.getPackets().stream().map(getPacketMapperFunction()).collect(Collectors.toList()));

        return result;
    }
}
