/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import java.util.List;

public class PgnDefinition {

    public int id;
    public String label;
    public String acronym;
    public int broadcastPeriod;
    public boolean isOnRequest;
    public List<SpnDefinition> spnDefinitions;

}
