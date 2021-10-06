/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package net.solidDesign.j1939.modules;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.etools.j1939_84.J1939_84.NL;
import static net.solidDesign.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

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

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.Lookup;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.ComponentIdentificationPacket;
import net.solidDesign.j1939.packets.CompositeMonitoredSystem;
import net.solidDesign.j1939.packets.CompositeSystem;
import net.solidDesign.j1939.packets.DM11ClearActiveDTCsPacket;
import net.solidDesign.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DM19CalibrationInformationPacket;
import net.solidDesign.j1939.packets.DM1ActiveDTCsPacket;
import net.solidDesign.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.DM22IndividualClearPacket;
import net.solidDesign.j1939.packets.DM22IndividualClearPacket.ControlByte;
import net.solidDesign.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DM24SPNSupportPacket;
import net.solidDesign.j1939.packets.DM25ExpandedFreezeFrame;
import net.solidDesign.j1939.packets.DM26TripDiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.DM27AllPendingDTCsPacket;
import net.solidDesign.j1939.packets.DM28PermanentEmissionDTCPacket;
import net.solidDesign.j1939.packets.DM29DtcCounts;
import net.solidDesign.j1939.packets.DM2PreviouslyActiveDTC;
import net.solidDesign.j1939.packets.DM30ScaledTestResultsPacket;
import net.solidDesign.j1939.packets.DM31DtcToLampAssociation;
import net.solidDesign.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import net.solidDesign.j1939.packets.DM34NTEStatus;
import net.solidDesign.j1939.packets.DM3DiagnosticDataClearPacket;
import net.solidDesign.j1939.packets.DM56EngineFamilyPacket;
import net.solidDesign.j1939.packets.DM5DiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.DM6PendingEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticReadinessPacket;
import net.solidDesign.j1939.packets.EngineHoursPacket;
import net.solidDesign.j1939.packets.IdleOperationPacket;
import net.solidDesign.j1939.packets.MonitoredSystem;
import net.solidDesign.j1939.packets.ParsedPacket;
import net.solidDesign.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

import net.solidDesign.j1939.CommunicationsListener;
import org.etools.j1939_84.modules.FunctionalModule;

public class CommunicationsModule extends FunctionalModule {

    public CommunicationsModule() {
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

    public List<DM1ActiveDTCsPacket> readDM1(CommunicationsListener listener) {
        listener.onResult("");
        String title = " Reading the bus for published DM1 messages";
        listener.onResult(getTime() + title);

        Collection<DM1ActiveDTCsPacket> allPackets = getJ1939()
                                                               .read(DM1ActiveDTCsPacket.class, 3, TimeUnit.SECONDS)
                                                               .flatMap(r -> r.left.stream())
                                                               .collect(Collectors.toMap(ParsedPacket::getSourceAddress,
                                                                                         p -> p,
                                                                                         (address, packet) -> address))
                                                               .values();

        List<DM1ActiveDTCsPacket> packets = allPackets.stream()
                                                      .sorted(Comparator.comparing(o -> o.getPacket().getTimestamp()))
                                                      .collect(Collectors.toList());

        packets.forEach(dm1 -> listener.onResult(NL + dm1.getPacket().toTimeString() + NL + dm1.toString()));

        return packets;
    }

    public RequestResult<DM2PreviouslyActiveDTC> requestDM2(CommunicationsListener listener) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM2PreviouslyActiveDTC> requestDM2(CommunicationsListener listener, int address) {
        return requestDMPackets("DM2", DM2PreviouslyActiveDTC.class, address, listener).busResult();
    }

    public List<AcknowledgmentPacket> requestDM3(CommunicationsListener listener) {
        return getJ1939().requestForAcks(listener, "Global DM3 Request", DM3DiagnosticDataClearPacket.PGN);
    }

    public List<AcknowledgmentPacket> requestDM3(CommunicationsListener listener, int address) {
        return getJ1939().requestForAcks(listener,
                                         "DS DM3 Request to " + Lookup.getAddressName(address),
                                         DM3DiagnosticDataClearPacket.PGN,
                                         address);
    }

    public RequestResult<DM5DiagnosticReadinessPacket> requestDM5(CommunicationsListener listener) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM5DiagnosticReadinessPacket> requestDM5(CommunicationsListener listener, int address) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, address, listener).busResult();
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(CommunicationsListener listener) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM6PendingEmissionDTCPacket> requestDM6(CommunicationsListener listener, int address) {
        return requestDMPackets("DM6", DM6PendingEmissionDTCPacket.class, address, listener);
    }

    /*
     * TODO: Remove when Step 9.8 & 12.9 are updated to match new requirements.
     */
    @Deprecated
    public List<AcknowledgmentPacket> requestDM11(CommunicationsListener listener) {
        return requestDM11(listener, 600, MILLISECONDS);
    }

    public List<AcknowledgmentPacket> requestDM11(CommunicationsListener listener, int address) {
        String title = "Destination Specific DM11 Request to " + Lookup.getAddressName(address);
        return getJ1939().requestForAcks(listener, title, DM11ClearActiveDTCsPacket.PGN, address);
    }

    public List<AcknowledgmentPacket> requestDM11(CommunicationsListener listener, long timeOut, TimeUnit timeUnit) {
        return getJ1939().requestForAcks(listener,
                                         "Global DM11 Request",
                                         DM11ClearActiveDTCsPacket.PGN,
                                         timeOut,
                                         timeUnit);
    }

    public RequestResult<DM12MILOnEmissionDTCPacket> requestDM12(CommunicationsListener listener) {
        return requestDMPackets("DM12", DM12MILOnEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM12MILOnEmissionDTCPacket> requestDM12(CommunicationsListener listener, int address) {
        return requestDMPackets("DM12", DM12MILOnEmissionDTCPacket.class, address, listener).busResult();
    }

    public RequestResult<DM20MonitorPerformanceRatioPacket> requestDM20(CommunicationsListener listener) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM20MonitorPerformanceRatioPacket> requestDM20(CommunicationsListener listener, int address) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, address, listener).busResult();
    }

    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(CommunicationsListener listener) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM21DiagnosticReadinessPacket> requestDM21(CommunicationsListener listener, int address) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, address, listener).busResult();
    }

    public BusResult<DM22IndividualClearPacket> requestDM22(CommunicationsListener listener,
                                                            int address,
                                                            ControlByte controlByte,
                                                            int spn,
                                                            int fmi) {
        String title = "Destination Specific DM22 Request to " + Lookup.getAddressName(address);
        var requestPacket = DM22IndividualClearPacket.createRequest(getJ1939().getBus().getAddress(),
                                                                    address,
                                                                    controlByte,
                                                                    spn,
                                                                    fmi);
        return getJ1939().requestDS(title, DM22IndividualClearPacket.PGN, requestPacket, listener);
    }

    public RequestResult<DM22IndividualClearPacket> requestDM22(CommunicationsListener listener,
                                                                ControlByte controlByte,
                                                                int spn,
                                                                int fmi) {
        String title = "Global DM22 Request";
        var requestPacket = DM22IndividualClearPacket.createRequest(getJ1939().getBus().getAddress(),
                                                                    GLOBAL_ADDR,
                                                                    controlByte,
                                                                    spn,
                                                                    fmi);
        return getJ1939().requestGlobal(title, DM22IndividualClearPacket.PGN, requestPacket, listener);
    }

    public RequestResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(CommunicationsListener listener) {
        return requestDMPackets("DM23", DM23PreviouslyMILOnEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM23PreviouslyMILOnEmissionDTCPacket> requestDM23(CommunicationsListener listener, int address) {
        return requestDMPackets("DM23", DM23PreviouslyMILOnEmissionDTCPacket.class, address, listener).busResult();
    }

    public BusResult<DM24SPNSupportPacket> requestDM24(CommunicationsListener listener, int obdModuleAddress) {
        return requestDMPackets("DM24", DM24SPNSupportPacket.class, obdModuleAddress, listener).busResult();
    }

    public BusResult<DM25ExpandedFreezeFrame> requestDM25(CommunicationsListener listener, int address) {
        return requestDMPackets("DM25", DM25ExpandedFreezeFrame.class, address, listener).busResult();
    }

    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(CommunicationsListener listener) {
        return requestDMPackets("DM26", DM26TripDiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM26TripDiagnosticReadinessPacket> requestDM26(CommunicationsListener listener, int address) {
        return requestDMPackets("DM26", DM26TripDiagnosticReadinessPacket.class, address, listener);
    }

    public RequestResult<DM27AllPendingDTCsPacket> requestDM27(CommunicationsListener listener) {
        return requestDMPackets("DM27", DM27AllPendingDTCsPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM27AllPendingDTCsPacket> requestDM27(CommunicationsListener listener, int address) {
        return requestDMPackets("DM27", DM27AllPendingDTCsPacket.class, address, listener).busResult();
    }

    public RequestResult<DM28PermanentEmissionDTCPacket> requestDM28(CommunicationsListener listener) {
        return requestDMPackets("DM28", DM28PermanentEmissionDTCPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM28PermanentEmissionDTCPacket> requestDM28(CommunicationsListener listener, int address) {
        return requestDMPackets("DM28", DM28PermanentEmissionDTCPacket.class, address, listener).busResult();
    }

    public RequestResult<DM29DtcCounts> requestDM29(CommunicationsListener listener) {
        return requestDMPackets("DM29", DM29DtcCounts.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM29DtcCounts> requestDM29(CommunicationsListener listener, int address) {
        return requestDMPackets("DM29", DM29DtcCounts.class, address, listener).busResult();
    }

    public List<DM30ScaledTestResultsPacket> requestTestResults(CommunicationsListener listener,
                                                                int address,
                                                                int tid,
                                                                int spn,
                                                                int fmi) {
        return getJ1939().requestTestResults(tid, spn, fmi, address, listener).requestResult().getPackets();
    }

    public BusResult<DM30ScaledTestResultsPacket> requestTestResult(CommunicationsListener listener,
                                                                    int address,
                                                                    int tid,
                                                                    int spn,
                                                                    int fmi) {
        return getJ1939().requestTestResults(tid, spn, fmi, address, listener);
    }

    public RequestResult<DM31DtcToLampAssociation> requestDM31(CommunicationsListener listener) {
        return requestDMPackets("DM31", DM31DtcToLampAssociation.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM31DtcToLampAssociation> requestDM31(CommunicationsListener listener, int address) {
        return requestDMPackets("DM31", DM31DtcToLampAssociation.class, address, listener);
    }

    public RequestResult<DM33EmissionIncreasingAECDActiveTime> requestDM33(CommunicationsListener listener) {
        return requestDMPackets("DM33", DM33EmissionIncreasingAECDActiveTime.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM33EmissionIncreasingAECDActiveTime> requestDM33(CommunicationsListener listener,
                                                                           int address) {
        return requestDMPackets("DM33", DM33EmissionIncreasingAECDActiveTime.class, address, listener);
    }

    public RequestResult<DM34NTEStatus> requestDM34(CommunicationsListener listener) {
        return requestDMPackets("DM34", DM34NTEStatus.class, GLOBAL_ADDR, listener);
    }

    public RequestResult<DM34NTEStatus> requestDM34(CommunicationsListener listener, int address) {
        return requestDMPackets("DM34", DM34NTEStatus.class, address, listener);
    }

    public List<DM56EngineFamilyPacket> requestDM56(CommunicationsListener listener, int address) {
        return requestDMPackets("DM56", DM56EngineFamilyPacket.class, address, listener).getPackets();
    }

    public List<DM56EngineFamilyPacket> requestDM56(CommunicationsListener listener) {
        return requestDMPackets("DM56", DM56EngineFamilyPacket.class, GLOBAL_ADDR, listener).getPackets();
    }

    /**
     * Requests the Vehicle Identification from all vehicle modules and
     * generates adds the information gathered to the report returning the
     * Packets returned by the query.
     *
     * @param  listener
     *                      the {@link ResultsListener} that will be given the report
     * @return          List of {@link VehicleIdentificationPacket}
     */
    public List<VehicleIdentificationPacket> reportVin(ResultsListener listener) {
        return getJ1939().requestGlobal("Global VIN Request", VehicleIdentificationPacket.class, listener).getPackets();
    }

    public BusResult<EngineHoursPacket> requestEngineHours(ResultsListener listener, int address) {
        return getJ1939().requestDS("Destination Specific Engine Hours Request to " + getAddressName(address),
                                    EngineHoursPacket.class,
                                    address,
                                    listener);
    }

    public BusResult<IdleOperationPacket> requestIdleOperation(ResultsListener listener, int address) {
        return getJ1939().requestDS("Destination Specific Idle Operation Request to " + getAddressName(address),
                                    IdleOperationPacket.class,
                                    address,
                                    listener);
    }

    public List<DM19CalibrationInformationPacket> requestDM19(ResultsListener listener) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, GLOBAL_ADDR, listener).getPackets();
    }

    public BusResult<DM19CalibrationInformationPacket> requestDM19(ResultsListener listener, int address) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, address, listener).busResult();
    }

    /**
     * Requests globally the Component Identification from all vehicle modules
     * and generates a {@link String} that's suitable for inclusion in the
     * report
     *
     * @param  listener
     *                      the {@link ResultsListener} that will be given the report
     * @return          {@link List} of {@link ComponentIdentificationPacket}
     */
    public RequestResult<ComponentIdentificationPacket> requestComponentIdentification(ResultsListener listener) {
        listener.onResult("");
        return getJ1939().requestGlobal("Global Component Identification Request",
                                        ComponentIdentificationPacket.class,
                                        listener);
    }

    /**
     * Requests the Component Identification from all specified address and
     * generates a {@link String} that's suitable for inclusion in the report
     *
     * @param  listener
     *                      the {@link ResultsListener} that will be given the report
     * @param  address
     *                      the address of vehicle module to which the message will be
     *                      addressed
     * @return          {@link List} of {@link ComponentIdentificationPacket}
     */
    public BusResult<ComponentIdentificationPacket> requestComponentIdentification(ResultsListener listener,
                                                                                   int address) {
        return getJ1939().requestDS("Destination Specific Component Identification Request to "
                + getAddressName(address),
                                    ComponentIdentificationPacket.class,
                                    address,
                                    listener);
    }

}
