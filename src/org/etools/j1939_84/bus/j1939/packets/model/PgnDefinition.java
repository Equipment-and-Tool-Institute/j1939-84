/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import java.util.Collections;
import java.util.List;

public class PgnDefinition {

    final public String acronym;
    /**
     * The broadcast period for the Parameter Group, in milliseconds. If this is
     * on request, this value should be ignored. If the broadcast period if
     * variable, this indicates the maximum period.
     */
    final public int broadcastPeriod; // in milliseconds
    final public int id;

    /**
     * This indicates the Parameter Group will only be transmitted when
     * requested.
     */
    final public boolean isOnRequest;

    /**
     * Indicates the packet may be received more often that the broadcastPeriod
     * indicates
     */
    final public boolean isVariableBroadcast;

    final public String label;

    final public List<SpnDefinition> spnDefinitions;

    public PgnDefinition(int id, String pgnLabel, String pgnAcronym, boolean isOnRequest, boolean isVariableBroadcast,
            int broadcastPeriod,
            List<SpnDefinition> spns) {
        this.id = id;
        label = pgnLabel;
        acronym = pgnAcronym;
        this.isOnRequest = isOnRequest;
        this.isVariableBroadcast = isVariableBroadcast;
        this.broadcastPeriod = broadcastPeriod;
        spnDefinitions = Collections.unmodifiableList(spns);
    }
}
