/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

public class SpnDefinition {

    private final String label;
    private final int slotNumber;
    private final int spnId;
    private final int startBit;
    private final int startByte;

    public SpnDefinition(int spnId, String label, int startByte, int startBit, int slotNumber) {
        this.spnId = spnId;
        this.label = label;
        this.slotNumber = slotNumber;
        this.startByte = startByte;
        this.startBit = startBit;
    }

    public String getLabel() {
        return label;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public int getSpnId() {
        return spnId;
    }

    public int getStartBit() {
        return startBit;
    }

    public int getStartByte() {
        return startByte;
    }
}
