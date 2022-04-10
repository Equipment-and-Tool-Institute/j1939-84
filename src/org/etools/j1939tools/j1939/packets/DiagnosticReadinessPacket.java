/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;


import static org.etools.j1939tools.j1939.packets.CompositeSystem.*;

import java.util.*;

import org.etools.j1939tools.bus.Packet;

/**
 * A Super class for Diagnostic Readiness Packets
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public abstract class DiagnosticReadinessPacket extends GenericPacket {

    protected DiagnosticReadinessPacket(Packet packet) {
        super(packet);
    }

    private MonitoredSystem createMonitoredSystem(CompositeSystem compositeSystem) {
        int lowerByte = compositeSystem.getLowerByte();
        boolean notCompleted;
        boolean supported;

        if (compositeSystem.isContinuouslyMonitored()) {
            int completedMask = compositeSystem.getMask();
            int supportedMask = completedMask >> 4;
            notCompleted = (getByte(lowerByte) & completedMask) == completedMask;
            supported = isOBDModule() && (getByte(lowerByte) & supportedMask) == supportedMask;
        } else {
            int mask = compositeSystem.getMask();
            notCompleted = (getByte(lowerByte + 2) & mask) == mask;
            supported = isOBDModule() && (getByte(lowerByte) & mask) == mask;
        }

        MonitoredSystemStatus status = MonitoredSystemStatus.findStatus(isDM5(), supported, !notCompleted);
        return new MonitoredSystem(compositeSystem, status, getSourceAddress(), isDM5());
    }

    protected static void populateData(CompositeSystem systemId, boolean isComplete, boolean isEnabled, int[] data) {
        int index = systemId.getLowerByte();

        if (systemId.isContinuouslyMonitored()) {
            int completeMask = systemId.getMask();
            if (!isComplete) {
                data[index] |= completeMask;
            } else {
                data[index] &= ~completeMask;
            }

            int supportedMask = systemId.getMask() >> 4;
            if (isEnabled) {
                data[index] |= supportedMask;
            } else {
                data[index] &= ~supportedMask;
            }
        } else {
            int mask = systemId.getMask();
            if (!isComplete) {
                data[index + 2] |= mask;
            } else {
                data[index + 2] &= ~mask;
            }
            if (isEnabled) {
                data[index] |= mask;
            } else {
                data[index] &= ~mask;
            }
        }
    }

    /**
     * Returns the List of Continuously monitored systems
     *
     * @return {@link List}
     */
    public List<MonitoredSystem> getContinuouslyMonitoredSystems() {
        List<MonitoredSystem> systems = new ArrayList<>();
        systems.add(createMonitoredSystem(CompositeSystem.COMPREHENSIVE_COMPONENT));
        systems.add(createMonitoredSystem(CompositeSystem.FUEL_SYSTEM));
        systems.add(createMonitoredSystem(CompositeSystem.MISFIRE));
        return systems;
    }

    /**
     * Returns the {@link Set} of Continuously and Non-continuously monitored
     * systems
     *
     * @return {@link Set}
     */
    public List<MonitoredSystem> getMonitoredSystems() {
        List<MonitoredSystem> systems = new ArrayList<>();
        systems.addAll(getContinuouslyMonitoredSystems());
        systems.addAll(getNonContinuouslyMonitoredSystems());
        return systems;
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
            systems.add(createMonitoredSystem(compositeSystem));
        }

        return systems;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContinuouslyMonitoredSystems(), getNonContinuouslyMonitoredSystems());
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
