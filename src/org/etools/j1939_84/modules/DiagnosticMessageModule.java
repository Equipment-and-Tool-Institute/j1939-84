/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
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
import org.etools.j1939_84.bus.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

public class DiagnosticMessageModule extends FunctionalModule {

    public DiagnosticMessageModule() {
        super();
    }


    public static List<CompositeMonitoredSystem> getCompositeSystems(Collection<MonitoredSystem> monitoredSystems,
                                                                     boolean isDM5) {
        Map<CompositeSystem, CompositeMonitoredSystem> map = new HashMap<>();
        for (MonitoredSystem system : monitoredSystems) {
            CompositeSystem key = system.getId();
            CompositeMonitoredSystem existingSystem = map.get(key);
            if (existingSystem == null) {
                map.put(key, new CompositeMonitoredSystem(system, isDM5));
            } else {
                existingSystem.addMonitoredSystems(system);
            }
        }
        List<CompositeMonitoredSystem> systems = new ArrayList<>(map.values());
        Collections.sort(systems);
        return systems;
    }

    public static List<CompositeMonitoredSystem> getCompositeSystems(List<? extends DiagnosticReadinessPacket> packets,
                                                                     boolean isDM5) {
        Set<MonitoredSystem> systems = packets.stream()
                .flatMap(p -> p.getMonitoredSystems().stream())
                .collect(Collectors.toSet());
        return getCompositeSystems(systems, isDM5);
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

    public RequestResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM2PreviouslyActiveDTC> requestDM2(ResultsListener listener, int address) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, address, listener).busResult();
    }

    public RequestResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener, int address) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, address, listener).busResult();
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(ResultsListener listener, int address) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, address, listener);
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

    public RequestResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener, int address) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, address, listener).busResult();
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

    public BusResult<DM24SPNSupportPacket> requestDM24(ResultsListener listener, int obdModuleAddress) {
        return requestDMPackets("DM24", DM24SPNSupportPacket.class, obdModuleAddress, listener).busResult();
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

    public List<DM30ScaledTestResultsPacket> getDM30Packets(ResultsListener listener, int address, SupportedSPN spn) {
        int spnId = spn.getSpn();
        Packet request = createDM7Packet(address, spnId);
        String title = "DM7 for DM30 from " + Lookup.getAddressName(address) + " for SPN "+ spnId;
        BusResult<DM30ScaledTestResultsPacket> result = getJ1939().requestDm7(title, listener, request);
        listener.onResult("");
        return result.requestResult().getPackets();
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

    public List<DM56EngineFamilyPacket> requestDM56(ResultsListener listener, int address) {
        return requestDMPackets("DM56", DM56EngineFamilyPacket.class, address, listener).getPackets();
    }

    public List<DM56EngineFamilyPacket> requestDM56(ResultsListener listener) {
        return requestDMPackets("DM56", DM56EngineFamilyPacket.class, GLOBAL_ADDR, listener).getPackets();
    }

    private Packet createDM7Packet(int destination, int spn) {
        return Packet.create(DM7CommandTestsPacket.PGN | destination,
                             getJ1939().getBusAddress(),
                             true,
                             247,
                             spn & 0xFF,
                             (spn >> 8) & 0xFF,
                             (((spn >> 16) & 0xFF) << 5) | 31,
                             0xFF,
                             0xFF,
                             0xFF,
                             0xFF);
    }

}
