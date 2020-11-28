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
    public int broadcastPeriod = -1; // in milliseconds
    public boolean isOnRequest = false;
    public List<SpnDefinition> spnDefinitions = Collections.emptyList();

}
