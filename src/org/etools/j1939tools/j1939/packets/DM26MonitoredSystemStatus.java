/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

/**
 * The Monitored System Status for DM26 Systems
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public enum DM26MonitoredSystemStatus implements MonitoredSystemStatus {

    ENABLED_COMPLETE(true, true),
    ENABLED_NOT_COMPLETE(true, false),
    NOT_ENABLED_COMPLETE(false, true),
    NOT_ENABLED_NOT_COMPLETE(false, false);

    /**
     * Flag to indicate this monitor is complete
     */
    private final boolean complete;

    /**
     * Flag to indicate this monitor is enabled
     */
    private final boolean enabled;

    /**
     * Constructor
     *
     * @param enabled
     *                     true if the monitor is enabled
     * @param complete
     *                     true if the monitor is complete
     */
    DM26MonitoredSystemStatus(boolean enabled, boolean complete) {
        this.enabled = enabled;
        this.complete = complete;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return getEnabledString() + ", " + getCompleteString();
    }

    @Override
    public String getEnabledString() {
        return (isEnabled() ? "    " : "not ") + "enabled";
    }
}
