/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.etools.j1939_84.bus.j1939.J1939.GLOBAL_ADDR;
import static org.etools.j1939_84.bus.j1939.Lookup.getAddressName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.packets.CompositeMonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.model.RequestResult;

/**
 * {@link FunctionalModule} that requests DM5, DM20, DM21, and DM26 messages
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DiagnosticReadinessModule extends FunctionalModule {

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

    public RequestResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM20MonitorPerformanceRatioPacket> requestDM20(ResultsListener listener, int address) {
        return requestDMPackets("DM20", DM20MonitorPerformanceRatioPacket.class, address, listener).busResult();
    }

    public RequestResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM21DiagnosticReadinessPacket> requestDM21(ResultsListener listener, int address) {
        return requestDMPackets("DM21", DM21DiagnosticReadinessPacket.class, address, listener).busResult();
    }

    /**
     * Sends the DM5 to determine which modules support HD-OBD. It returns a
     * {@link List} of source addresses of the modules that do support HD-OBD.
     *
     * @param listener
     *         the {@link ResultsListener} that is notified of the
     *         communications
     * @return List of source addresses
     */
    public List<Integer> getOBDModules(ResultsListener listener) {
        Packet request = getJ1939().createRequestPacket(DM5DiagnosticReadinessPacket.PGN, GLOBAL_ADDR);

        List<Integer> addresses = getJ1939().requestGlobal("Global DM5 Request",
                                                           listener,
                                                           false,
                                                           DM5DiagnosticReadinessPacket.PGN,
                                                           request)
                .getPackets()
                .stream()
                .filter(p -> p instanceof DM5DiagnosticReadinessPacket)
                .map(p -> (DM5DiagnosticReadinessPacket) p)
                .filter(DM5DiagnosticReadinessPacket::isHdObd)
                .map(ParsedPacket::getSourceAddress)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
        if (addresses.isEmpty()) {
            listener.onResult("No modules report as HD-OBD compliant - stopping.");
        } else {
            for (int i : addresses) {
                listener.onResult(getAddressName(i) + " reported as an HD-OBD Module.");
            }
        }
        return addresses;
    }

    public RequestResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, GLOBAL_ADDR, listener);
    }

    public BusResult<DM5DiagnosticReadinessPacket> requestDM5(ResultsListener listener, int address) {
        return requestDMPackets("DM5", DM5DiagnosticReadinessPacket.class, address, listener).busResult();
    }

}
