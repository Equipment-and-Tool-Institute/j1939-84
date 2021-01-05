/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import org.etools.j1939_84.bus.j1939.packets.Slot;

public class Spn {

    private final int id;
    private final String label;
    private final Slot slot;
    private final byte[] data;

    public Spn(int id, String label, Slot slot, byte[] data) {
        this.id = id;
        this.label = label;
        this.slot = slot;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns the scaled value of the data.
     * This will return null if the value is NOT_AVAILABLE or ERROR.
     * It will also return null if the type is ASCII.
     *
     * @return Double or null
     */
    public Double getValue() {
        return slot == null ? null : slot.asValue(data);
    }

    /**
     * Returns true of the value of the SPN is NOT_AVAILABLE
     *
     * @return boolean
     */
    public boolean isNotAvailable() {
        return slot == null || slot.isNotAvailable(data);
    }

    /**
     * Returns true of the value of the SPN is ERROR
     *
     * @return boolean
     */
    public boolean isError() {
        return slot == null || slot.isError(data);
    }

    @Override
    public String toString() {
        return String.format("SPN %1$5s, %2$s: %3$s",
                             id,
                             label,
                             slot == null ? "" : slot.asString(data));
    }

}
