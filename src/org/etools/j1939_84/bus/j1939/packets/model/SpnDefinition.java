/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

public class SpnDefinition {

    final public String label;
    final public int slotNumber;
    final public int spnId;

    final public int startBit;

    final public int startByte;

    public SpnDefinition(int spn, String spnLabel, int startByte, int startBit, int slot) {
        spnId = spn;
        label = spnLabel;
        slotNumber = slot;
        this.startByte = startByte;
        this.startBit = startBit;
    }
}
