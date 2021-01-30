/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
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
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * The Diagnostic Trouble Code Module that is responsible for Requesting or
 * Clearing the DTCs from the vehicle
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DTCModule extends FunctionalModule {

    public DTCModule() {
        super();
    }

    public RequestResult<DM1ActiveDTCsPacket> readDM1(ResultsListener listener) {
        String title = " Reading the bus for published DM1 messages";
        listener.onResult(getTime() + title);

        Collection<DM1ActiveDTCsPacket> allPackets = getJ1939()
                .read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS)
                .flatMap(r -> r.left.stream())
                .collect(Collectors.toMap(ParsedPacket::getSourceAddress, p -> p, (address, packet) -> address))
                .values();

        List<DM1ActiveDTCsPacket> packets = allPackets.stream()
                .sorted(Comparator.comparing(o -> o.getPacket().getTimestamp()))
                .collect(Collectors.toList());

        return new RequestResult<>(false, packets, List.of());
    }

    public RequestResult<DM28PermanentEmissionDTCPacket> reportDM28(ResultsListener listener, int address) {
        return requestDMPackets("DM28", DM28PermanentEmissionDTCPacket.class, address, listener);
    }

    public List<AcknowledgmentPacket> requestDM11(ResultsListener listener) {
        listener.onResult(getTime() + " Clearing Diagnostic Trouble Codes");

        List<AcknowledgmentPacket> responses = getJ1939().requestDm11(listener);

        if (!responses.isEmpty() && responses.stream().allMatch(t -> t.getResponse() == Response.ACK)) {
            listener.onResult("Diagnostic Trouble Codes were successfully cleared.");
        } else {
            listener.onResult("ERROR: Clearing Diagnostic Trouble Codes failed.");
        }

        return responses;
    }

    public RequestResult<DM12MILOnEmissionDTCPacket> requestDM12(ResultsListener listener) {
        return requestDMPackets("DM12", DM12MILOnEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM12MILOnEmissionDTCPacket> requestDM12(ResultsListener listener, int address) {
        return requestDMPackets("DM12", DM12MILOnEmissionDTCPacket.class, address, listener).busResult();
    }

    public RequestResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener, int address) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, address, listener).busResult();
    }

    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener, int address) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, address, listener);
    }

    public RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener) {
        return requestDMPackets("DM23", DM23PreviouslyMILOnEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(ResultsListener listener, int address) {
        return requestDMPackets("DM23", DM23PreviouslyMILOnEmissionDTCPacket.class, address, listener).busResult();
    }

    public BusResult<DM25ExpandedFreezeFrame> requestDM25(ResultsListener listener, int address) {
        return requestDMPackets("DM25", DM25ExpandedFreezeFrame.class, address, listener).busResult();
    }

    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(ResultsListener listener) {
        return requestDMPackets("DM26", DM26TripDiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(ResultsListener listener, int address) {
        return requestDMPackets("DM26", DM26TripDiagnosticReadinessPacket.class, address, listener);
    }

    public RequestResult<DM27AllPendingDTCsPacket> requestDM27(ResultsListener listener) {
        return requestDMPackets("DM27", DM27AllPendingDTCsPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM27AllPendingDTCsPacket> requestDM27(ResultsListener listener, int address) {
        return requestDMPackets("DM27", DM27AllPendingDTCsPacket.class, address, listener).busResult();
    }

    public RequestResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener) {
        return requestDMPackets("DM28", DM28PermanentEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM28PermanentEmissionDTCPacket> requestDM28(ResultsListener listener, int address) {
        return requestDMPackets("DM28", DM28PermanentEmissionDTCPacket.class, address, listener).busResult();
    }

    public RequestResult<DM29DtcCounts> requestDM29(ResultsListener listener) {
        return requestDMPackets("DM29", DM29DtcCounts.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM29DtcCounts> requestDM29(ResultsListener listener, int address) {
        return requestDMPackets("DM29", DM29DtcCounts.class, address, listener).busResult();
    }

    public RequestResult<DM31DtcToLampAssociation> requestDM31(ResultsListener listener) {
        return requestDMPackets("DM31", DM31DtcToLampAssociation.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM31DtcToLampAssociation> requestDM31(ResultsListener listener, int address) {
        return requestDMPackets("DM31", DM31DtcToLampAssociation.class, address, listener);
    }

    public RequestResult<DM33EmissionIncreasingAECDActiveTime> requestDM33(ResultsListener listener) {
        return requestDMPackets("DM33", DM33EmissionIncreasingAECDActiveTime.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM33EmissionIncreasingAECDActiveTime> requestDM33(ResultsListener listener, int address) {
        return requestDMPackets("DM33", DM33EmissionIncreasingAECDActiveTime.class, address, listener);
    }

    public RequestResult<DM34NTEStatus> requestDM34(ResultsListener listener) {
        return requestDMPackets("DM34", DM34NTEStatus.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM34NTEStatus> requestDM34(ResultsListener listener, int address) {
        return requestDMPackets("DM34", DM34NTEStatus.class, address, listener);
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener, int address) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, address, listener);
    }

}
