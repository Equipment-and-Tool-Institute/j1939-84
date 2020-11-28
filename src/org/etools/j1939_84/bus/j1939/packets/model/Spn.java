/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import org.etools.j1939_84.bus.j1939.Lookup;
import org.etools.j1939_84.bus.j1939.packets.Slot;

public class Spn {

    private final byte[] data;
    private final int id;

    private final Slot slot;
    private String name;

    public Spn(int id, Slot slot, byte[] data) {
        this.id = id;
        this.slot = slot;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    private String getName() {
        if (name == null) {
            name = Lookup.getSpnName(id);
        }
        return name;
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

    @Override
    public String toString() {
        return String.format("SPN %1$5s, %2$s: %3$s",
                             getId(),
                             getName(),
                             slot == null ? "" : slot.asString(data));
    }

}
