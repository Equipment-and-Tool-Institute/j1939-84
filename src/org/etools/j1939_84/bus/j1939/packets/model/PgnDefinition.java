/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import java.util.Collections;
import java.util.List;

public class PgnDefinition {

    public int id = -1;
    public String label = "";
    public String acronym = "";

    /**
     * The broadcast period for the Parameter Group, in milliseconds.
     * If this is on request, this value should be ignored.
     * If the broadcast period if variable, this indicates the maximum period.
     */
    public int broadcastPeriod = -1; // in milliseconds

    /**
     * Indicates the packet may be received more often that the broadcastPeriod indicates
     */
    public boolean isVariableBroadcast = false;

    /**
     * This indicates the Parameter Group will only be transmitted when requested.
     */
    public boolean isOnRequest = false;

    public List<SpnDefinition> spnDefinitions = Collections.emptyList();

}
