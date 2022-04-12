/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

import java.util.Collections;
import java.util.List;

public class PgnDefinition {

    private final String acronym;
    private final int broadcastPeriod; // in milliseconds
    private final int id;
    private final boolean isOnRequest;
    private final boolean isVariableBroadcast;
    private final String label;
    private final List<SpnDefinition> spnDefinitions;

    public PgnDefinition(int id,
                         String label,
                         String acronym,
                         boolean isOnRequest,
                         boolean isVariableBroadcast,
                         int broadcastPeriod,
                         List<SpnDefinition> spnDefinitions) {
        this.id = id;
        this.label = label;
        this.acronym = acronym;
        this.isOnRequest = isOnRequest;
        this.isVariableBroadcast = isVariableBroadcast;
        this.broadcastPeriod = broadcastPeriod;
        this.spnDefinitions = Collections.unmodifiableList(spnDefinitions);
    }

    public String getAcronym() {
        return acronym;
    }

    /**
     * The broadcast period for the Parameter Group, in milliseconds. If this is
     * on request, this value should be ignored. If the broadcast period if
     * variable, this indicates the maximum period.
     */
    public int getBroadcastPeriod() {
        return broadcastPeriod;
    }

    public int getId() {
        return id;
    }

    /**
     * This indicates the Parameter Group will only be transmitted when
     * requested.
     */
    public boolean isOnRequest() {
        return isOnRequest;
    }

    /**
     * Indicates the packet may be received more often that the broadcastPeriod
     * indicates
     */
    public boolean isVariableBroadcast() {
        return isVariableBroadcast;
    }

    public String getLabel() {
        return label;
    }

    public List<SpnDefinition> getSpnDefinitions() {
        return spnDefinitions;
    }
}
