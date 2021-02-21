/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets.model;

import org.etools.j1939_84.bus.j1939.J1939DaRepository;
import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.utils.CollectionUtils;

public class Spn {

    private final byte[] data;
    private final int id;
    private final String label;
    private final Slot slot;

    public Spn(int id, String label, Slot slot, byte[] data) {
        this.id = id;
        this.label = label;
        this.slot = slot;
        this.data = data;
    }

    public static Spn create(int id, double value) {
        J1939DaRepository j1939DaRepository = J1939DaRepository.getInstance();
        SpnDefinition spnDefinition = j1939DaRepository.findSpnDefinition(id);
        String label = spnDefinition.getLabel();
        Slot slot = j1939DaRepository.findSLOT(spnDefinition.getSlotNumber(), id);
        byte[] data = slot.asBytes(value);
        return new Spn(id, label, slot, data);
    }

    public int[] getData() {
        return CollectionUtils.toIntArray(data);
    }

    public int getId() {
        return id;
    }

    /**
     * Returns the scaled value of the data. This will return null if the value
     * is NOT_AVAILABLE or ERROR. It will also return null if the type is ASCII.
     *
     * @return Double or null
     */
    public Double getValue() {
        return slot.asValue(data);
    }

    public boolean hasValue() {
        return getValue() != null;
    }

    /**
     * Returns true of the value of the SPN is ERROR
     *
     * @return boolean
     */
    public boolean isError() {
        return slot.isError(data);
    }

    /**
     * Returns true of the value of the SPN is NOT_AVAILABLE
     *
     * @return boolean
     */
    public boolean isNotAvailable() {
        return slot.isNotAvailable(data);
    }

    @Override
    public String toString() {
        return String.format("SPN %1$5s, %2$s: %3$s",
                             id,
                             label,
                             slot.asString(data));
    }

}
