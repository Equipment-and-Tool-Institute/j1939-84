/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;

/**
 * A Super class for Diagnostic Readiness Packets
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public abstract class DiagnosticReadinessPacket extends GenericPacket {

    protected DiagnosticReadinessPacket(Packet packet, PgnDefinition pgnDefinition) {
        super(packet, pgnDefinition);
    }

    private MonitoredSystem createContinuouslyMonitoredSystem(CompositeSystem compositeSystem) {
        String name = compositeSystem.getName();
        int completedMask = compositeSystem.getMask();
        int supportedMask = completedMask >> 4;
        boolean notCompleted = (getByte(3) & completedMask) == completedMask;
        boolean supported = isOBDModule() && (getByte(3) & supportedMask) == supportedMask;
        MonitoredSystemStatus status = MonitoredSystemStatus.findStatus(isDM5(), supported, !notCompleted);
        return new MonitoredSystem(name, status, getSourceAddress(), compositeSystem, isDM5());
    }

    private MonitoredSystem createNonContinuouslyMonitoredSystem(CompositeSystem compositeSystem) {
        String name = compositeSystem.getName();
        int lowerByte = compositeSystem.getLowerByte();
        int mask = compositeSystem.getMask();
        boolean notCompleted = (getByte(lowerByte + 2) & mask) == mask;
        boolean supported = isOBDModule() && (getByte(lowerByte) & mask) == mask;
        MonitoredSystemStatus status = MonitoredSystemStatus.findStatus(isDM5(), supported, !notCompleted);
        return new MonitoredSystem(name, status, getSourceAddress(), compositeSystem, isDM5());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DiagnosticReadinessPacket)) {
            return false;
        }

        DiagnosticReadinessPacket that = (DiagnosticReadinessPacket) obj;
        return getSourceAddress() == that.getSourceAddress()
                && Objects.equals(getContinuouslyMonitoredSystems(), that.getContinuouslyMonitoredSystems())
                && Objects.equals(getNonContinuouslyMonitoredSystems(), that.getNonContinuouslyMonitoredSystems());
    }

    /**
     * Returns the List of Continuously monitored systems
     *
     * @return {@link List}
     */
    public List<MonitoredSystem> getContinuouslyMonitoredSystems() {
        List<MonitoredSystem> systems = new ArrayList<>();
        systems.add(createContinuouslyMonitoredSystem(CompositeSystem.COMPREHENSIVE_COMPONENT));
        systems.add(createContinuouslyMonitoredSystem(CompositeSystem.FUEL_SYSTEM));
        systems.add(createContinuouslyMonitoredSystem(CompositeSystem.MISFIRE));
        return systems;
    }

    /**
     * Returns the {@link Set} of Continuously and Non-continuously monitored
     * systems
     *
     * @return {@link Set}
     */
    public List<MonitoredSystem> getMonitoredSystems() {
        List<MonitoredSystem> set = new ArrayList<>();
        set.addAll(getContinuouslyMonitoredSystems());
        set.addAll(getNonContinuouslyMonitoredSystems());
        return set;
    }

    /**
     * Returns the List of Non-continuously monitored systems
     *
     * @return {@link List}
     */
    public List<MonitoredSystem> getNonContinuouslyMonitoredSystems() {
        List<MonitoredSystem> systems = new ArrayList<>();
        for (CompositeSystem compositeSystem : Arrays.asList(EGR_VVT_SYSTEM,
                                                             EXHAUST_GAS_SENSOR_HEATER,
                                                             EXHAUST_GAS_SENSOR,
                                                             AC_SYSTEM_REFRIGERANT,
                                                             SECONDARY_AIR_SYSTEM,
                                                             EVAPORATIVE_SYSTEM,
                                                             HEATED_CATALYST,
                                                             CATALYST,
                                                             NMHC_CONVERTING_CATALYST,
                                                             NOX_CATALYST_ABSORBER,
                                                             DIESEL_PARTICULATE_FILTER,
                                                             BOOST_PRESSURE_CONTROL_SYS,
                                                             COLD_START_AID_SYSTEM)) {
            systems.add(createNonContinuouslyMonitoredSystem(compositeSystem));
        }

        return systems;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContinuouslyMonitoredSystems(), getNonContinuouslyMonitoredSystems());
    }

    /**
     * Helper method to indicate if this is a DM26 packet
     *
     * @return true if this is a DM26 packet
     */
    private boolean isDM26() {
        return this instanceof DM26TripDiagnosticReadinessPacket;
    }

    /**
     * Helper method to indicate if this is a DM5 packet
     *
     * @return true if this is a DM5 packet
     */
    private boolean isDM5() {
        return this instanceof DM5DiagnosticReadinessPacket;
    }

    /**
     * Returns true if this module is an OBD Module that supports this function
     *
     * @return boolean
     */
    private boolean isOBDModule() {
        return isDM26() || (isDM5() && getByte(2) != (byte) 0x05 && getByte(2) != (byte) 0xFF);
    }
}