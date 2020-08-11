/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.etools.j1939_84.bus.Packet;

/**
 * A Super class for Diagnostic Readiness Packets
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public abstract class DiagnosticReadinessPacket extends ParsedPacket {

    protected DiagnosticReadinessPacket(Packet packet) {
        super(packet);
    }

    private MonitoredSystem createContinouslyMonitoredSystem(CompositeSystem compositeSystem) {
        String name = compositeSystem.getName();
        int completedMask = compositeSystem.getMask();
        int supportedMask = completedMask >> 4;
        boolean notCompleted = (getByte(3) & completedMask) == completedMask;
        boolean supported = isOBDModule() && (getByte(3) & supportedMask) == supportedMask;
        MonitoredSystemStatus status = MonitoredSystemStatus.findStatus(isDM5(), supported, !notCompleted);
        return new MonitoredSystem(name, status, getSourceAddress(), compositeSystem);
    }

    private MonitoredSystem createNonContinouslyMonitoredSystem(CompositeSystem compositeSystem) {
        String name = compositeSystem.getName();
        int lowerByte = compositeSystem.getLowerByte();
        int mask = compositeSystem.getMask();
        boolean notCompleted = (getByte(lowerByte + 2) & mask) == mask;
        boolean supported = isOBDModule() && (getByte(lowerByte) & mask) == mask;
        MonitoredSystemStatus status = MonitoredSystemStatus.findStatus(isDM5(), supported, !notCompleted);
        return new MonitoredSystem(name, status, getSourceAddress(), compositeSystem);
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
        systems.add(createContinouslyMonitoredSystem(CompositeSystem.COMPREHENSIVE_COMPONENT));
        systems.add(createContinouslyMonitoredSystem(CompositeSystem.FUEL_SYSTEM));
        systems.add(createContinouslyMonitoredSystem(CompositeSystem.MISFIRE));
        return systems;
    }

    /**
     * Returns the {@link Set} of Continuously and Non-continuously monitored
     * systems
     *
     * @return {@link Set}
     */
    public Set<MonitoredSystem> getMonitoredSystems() {
        Set<MonitoredSystem> set = new HashSet<>();
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
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.EGR_VVT_SYSTEM));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.EXHAUST_GAS_SENSOR_HEATER));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.EXHAUST_GAS_SENSOR));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.AC_SYSTEM_REFRIGERANT));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.SECONDARY_AIR_SYSTEM));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.EVAPORATIVE_SYSTEM));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.HEATED_CATALYST));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.CATALYST));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.NMHC_CONVERTING_CATALYST));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.NOX_CATALYST_ABSORBER));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.DIESEL_PARTICULATE_FILTER));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.BOOST_PRESSURE_CONTROL_SYS));
        systems.add(
                createNonContinouslyMonitoredSystem(CompositeSystem.COLD_START_AID_SYSTEM));

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