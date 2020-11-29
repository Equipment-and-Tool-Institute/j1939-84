/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import java.util.Collections;
import java.util.List;

public class PgnDefinition {

    final public String acronym;

    final public int broadcastPeriod;
    final public int id;
    final public boolean isOnRequest;
    final public String label;
    final public List<SpnDefinition> spnDefinitions;

    public PgnDefinition(int id, String pgnLabel, String pgnAcronym, boolean isOnRequest, int broadcastPeriod,
            List<SpnDefinition> spns) {
        this.id = id;
        label = pgnLabel;
        acronym = pgnAcronym;
        this.isOnRequest = isOnRequest;
        this.broadcastPeriod = broadcastPeriod;
        spnDefinitions = Collections.unmodifiableList(spns);
    }
}
