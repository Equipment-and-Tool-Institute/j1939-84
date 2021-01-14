/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;

import java.util.Objects;

/**
 * The Name and Status of an Emission Monitored System (DM5 and DM26)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class MonitoredSystem implements Comparable<MonitoredSystem> {

    private final CompositeSystem id;
    private final String name;
    private final int sourceAddress;
    private final MonitoredSystemStatus status;
    private final boolean isDM5;

    /**
     * Creates a Monitored System
     *
     * @param name
     *         the Name of the Monitored System
     * @param status
     *         the {@link MonitoredSystemStatus} of the Monitored System
     * @param sourceAddress
     *         the source address that reported this
     * @param id
     *         the unique id for this system. This allows
     *         {@link MonitoredSystem} from various source addresses to be
     * @param isDM5
     *         indicates this is from a DM5 Packet
     */
    public MonitoredSystem(String name,
                           MonitoredSystemStatus status,
                           int sourceAddress,
                           CompositeSystem id,
                           boolean isDM5) {
        this.name = name;
        this.status = status;
        this.sourceAddress = sourceAddress;
        this.id = id;
        this.isDM5 = isDM5;
    }

    @Override
    public int compareTo(MonitoredSystem o) {
        int result = getName().compareTo(o.getName());
        if (result == 0) {
            result = getSourceAddress() - o.getSourceAddress();
        }
        if (result == 0) {
            result = getStatus().toString().compareTo(o.getStatus().toString());
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MonitoredSystem)) {
            return false;
        }

        MonitoredSystem that = (MonitoredSystem) obj;
        return Objects.equals(getName(), that.getName())
                && Objects.equals(getStatus(), that.getStatus())
                && Objects.equals(getSourceAddress(), that.getSourceAddress())
                && Objects.equals(getId(), that.getId());
    }

    /**
     * Returns the unique identification for the Monitored System. This allows
     * {@link MonitoredSystem} from various source addresses to be matched up
     *
     * @return int
     */
    public CompositeSystem getId() {
        return id;
    }

    /**
     * Returns the name of the Monitored System
     *
     * @return {@link String}
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the source address of the module this {@link MonitoredSystem} is
     * from
     *
     * @return the sourceAddress
     */
    public int getSourceAddress() {
        return sourceAddress;
    }

    /**
     * Returns the {@link MonitoredSystemStatus} of the Monitored System
     *
     * @return {@link MonitoredSystemStatus}
     */
    public MonitoredSystemStatus getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStatus(), getSourceAddress(), getId());
    }

    @Override
    public String toString() {
        return "    " + getName() + " " + findStatus(isDM5, getStatus().isEnabled(), getStatus().isComplete());
    }
}
