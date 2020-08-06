/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a {@link MonitoredSystem} that is a composite of all
 * {@link MonitoredSystem} s from various sources
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class CompositeMonitoredSystem extends MonitoredSystem {

    /**
     * Flag to indicate if this is a Monitored System from a DM5 packet
     */
    private final boolean isDm5;

    /**
     * The composite {@link MonitoredSystemStatus}
     */
    private MonitoredSystemStatus status;

    /**
     * The Map of source address to {@link MonitoredSystem}
     */
    private final Map<Integer, MonitoredSystemStatus> systems = new ConcurrentHashMap<>();

    /**
     * Creates a {@link CompositeMonitoredSystem} starting with the given
     * {@link MonitoredSystem}
     *
     * @param system
     *            the {@link MonitoredSystem} to start with
     * @param isDm5
     *            true to indicate this system is from a DM5 message
     */
    public CompositeMonitoredSystem(MonitoredSystem system, boolean isDm5) {
        super(system.getName(), system.getStatus(), system.getSourceAddress(), system.getId());
        this.isDm5 = isDm5;
        addMonitoredSystems(system);
    }

    /**
     * Creates a {@link CompositeMonitoredSystem} with a name and id
     *
     * @param name
     *            the name
     * @param sourceAddress
     *            the source address of the module this is from
     * @param id
     *            the id
     * @param isDm5
     *            true to indicate this system is for a DM5 message
     */
    public CompositeMonitoredSystem(String name, int sourceAddress, int id, boolean isDm5) {
        super(name, null, sourceAddress, id);
        this.isDm5 = isDm5;
    }

    /**
     * Adds a {@link MonitoredSystem} to the collection
     *
     * @param system
     *            the {@link MonitoredSystem} to add
     * @return true if the {@link MonitoredSystem} changed; false if it didn't
     *         change
     */
    public boolean addMonitoredSystems(MonitoredSystem system) {
        systems.put(system.getSourceAddress(), system.getStatus());

        MonitoredSystemStatus newStatus = getCompositeStatus();
        boolean result = !newStatus.equals(status);
        status = newStatus;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompositeMonitoredSystem)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Helper method to determine the {@link MonitoredSystemStatus} based upon
     * all the {@link MonitoredSystem} s
     *
     * @return {@link Status}
     */
    private MonitoredSystemStatus getCompositeStatus() {
        if (systems.isEmpty()) {
            return MonitoredSystemStatus.findStatus(isDm5, false, false);
        }

        boolean isEnabled = systems.values().stream().filter(s -> s.isEnabled()).findFirst().isPresent();

        boolean isComplete = false;
        for (MonitoredSystemStatus systemStatus : systems.values()) {
            isComplete = systemStatus.isComplete();
            if (!isComplete) {
                break;
            }
        }

        return MonitoredSystemStatus.findStatus(isDm5, isEnabled, isComplete);
    }

    /**
     * Returns the composite {@link MonitoredSystemStatus}
     *
     * @return {@link MonitoredSystemStatus}
     */
    @Override
    public MonitoredSystemStatus getStatus() {
        return status;
    }
}
