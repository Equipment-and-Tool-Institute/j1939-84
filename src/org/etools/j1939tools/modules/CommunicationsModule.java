/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939tools.modules;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939tools.j1939.Lookup.getAddressName;

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

import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket;
import org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte;
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
import org.etools.j1939tools.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.ParsedPacket;

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
                                         "DS DM3 Request to " + getAddressName(address),
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
        String title = "Destination Specific DM11 Request to " + getAddressName(address);
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
        String title = "DM22";
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
        String title = "DM22";
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

    public List<? extends GenericPacket> requestDM57(CommunicationsListener listener,
                                                     int address) {
        return request(64710, address, listener);
    }

    public BusResult<DM58RationalityFaultSpData> requestDM58(CommunicationsListener listener, int address, int spn) {
        return getJ1939().requestRationalityTestResults(listener, address, spn);
    }

    // Helper method to get the pg for the class object
    private int getPg(Class<? extends GenericPacket> clazz, CommunicationsListener listener) {
        int pg = 0;
        try {
            pg = clazz.getField("PGN").getInt(null);
        } catch (Exception e) {
            listener.onResult("Error occurred while trying to get PG of class");
        }
        return pg;
    }

    /**
     * Read the bus for <? extends GenericPacket> and return the latest message also
     * generates and logs a {@link String} that's suitable for inclusion in the report
     *
     * @param clazz
     *                     The class name of the packet to read from the bus
     * @param timeout
     *                     bus observation time in milliseconds
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    @SuppressWarnings("unchecked")
    public <T extends GenericPacket> List<T> read(Class<T> clazz,
                                                            int timeout,
                                                            TimeUnit timeUnit,
                                                            CommunicationsListener listener) {

        return (List<T>) read(getPg(clazz, listener), timeout, timeUnit, listener);
    }

    /**
     * Read the bus for EngineSpeedPacket and return the latest message also
     * generates and logs a {@link String} that's suitable for inclusion in the report
     *
     * @param pg
     *                     the PG number of the message to be read from the bus
     * @param timeout
     *                     bus observation time
     * @param timeUnit
     *                     the time unit of the timeout
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    public List<? extends GenericPacket> read(int pg, int timeout, TimeUnit timeUnit, CommunicationsListener listener) {
        listener.onResult("");
        String title = " Reading the bus for published " + getPgDefinition(pg).getAcronym() + " messages";
        listener.onResult(getTime() + title);
        return (getJ1939().read(pg, timeout, timeUnit)
                          .flatMap((r) -> r.left.stream())
                          .collect(Collectors.toMap(ParsedPacket::getSourceAddress,
                                                    (p1) -> p1,
                                                    (address, packet) -> address)))
                                                                                   .values()
                                                                                   .stream()
                                                                                   .sorted(Comparator.comparing(o -> o.getPacket()
                                                                                                                      .getTimestamp()))
                                                                                   .peek(p -> listener.onResult(p.getPacket()
                                                                                                                 .toTimeString()
                                                                                           + NL + p + NL))
                                                                                   .collect(Collectors.toList());
    }

    /**
     * Request for <? extends GenericPacket> and return the latest message also
     *
     * @param clazz
     *                     The class name of the packet to read from the bus
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    @SuppressWarnings("unchecked")
    public <T extends GenericPacket> List<T> request(Class<T> clazz, CommunicationsListener listener) {
        return (List<T>) request(getPg(clazz, listener), listener).stream()
                                                                  .sorted(Comparator.comparing((o) -> o.getPacket()
                                                                                                       .getTimestamp()))
                                                                  .collect(Collectors.toList());
    }

    /**
     * Request for List<? extends GenericPacket> and return the latest message
     *
     * @param pg
     *                     the PG number of the message to be read from the bus
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    public List<? extends GenericPacket> request(int pg, CommunicationsListener listener) {
        Packet requestPacket = getJ1939().createRequestPacket(pg, GLOBAL_ADDR);
        return getJ1939().requestGlobal(getPgDefinition(pg).getLabel(), pg, requestPacket, listener)
                         .getPackets()
                         .stream()
                         .sorted(Comparator.comparing((p) -> p.getPacket().getTimestamp()))
                         .collect(Collectors.toList());

    }

    /**
     * Request for List<? extends GenericPacket> and return the latest message
     *
     * @param clazz
     *                     The class name of the packet to read from the bus
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    public <T extends GenericPacket> BusResult<T> request(Class<T> clazz,
                                                          int address,
                                                          CommunicationsListener listener) {
        int pg = getPg(clazz, listener);
        Packet requestPacket = getJ1939().createRequestPacket(pg, address);
        return getJ1939().requestDS(clazz.getName(), pg, requestPacket, listener);
    }

    /**
     * Request for List<? extends GenericPacket> and return the latest message
     *
     * @param pg
     *                     the PG number of the message to be read from the bus
     * @param listener
     *                     the {@link CommunicationsListener} that will be given the report
     */
    public List<? extends GenericPacket> request(int pg, int address, CommunicationsListener listener) {
        Packet requestPacket = getJ1939().createRequestPacket(pg, address);

        return getJ1939().requestDS(getPgDefinition(pg).getAcronym(), pg, requestPacket, listener)
                         .toPacketStream()
                         .collect(Collectors.toList());
    }

    /**
     * Helper method for get the PG definitions using the representive int value
     *
     * @param  pg
     *                {@link int} base 10 representation of the PG value
     * @return    {@link PgnDefinition} as imported from the DA
     */
    private PgnDefinition getPgDefinition(int pg) {
        J1939DaRepository j1939 = J1939DaRepository.getInstance();
        return j1939.findPgnDefinition(pg);
    }

    public RequestResult<DM19CalibrationInformationPacket> requestDM19(CommunicationsListener listener) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM19CalibrationInformationPacket> requestDM19(CommunicationsListener listener, int address) {
        return requestDMPackets("DM19", DM19CalibrationInformationPacket.class, address, listener).busResult();
    }
}
