/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939tools.j1939.packets;

import java.util.HashMap;
import java.util.Map;

public class BitSlot extends Slot {

    private final Map<Integer, String> valuesMap;

    public BitSlot(int id, String name, int length) {
        this(id, name, length, new HashMap<>());
    }

    private BitSlot(int id, String name, int length, Map<Integer, String> valuesMap) {
        super(id,
              name,
              "BITFIELD",
              1.0,
              0.0,
              "",
              length);
        this.valuesMap = valuesMap;
    }

    /**
     * Helper method to find a value in the given map
     *
     * @param  int value
     *                 the map that contains the values
     *                 the key to find in the map
     * @return     the value from the map or "Unknown" if the key does not have a
     *             value in the map
     */
    public String find(int value) {
        final String str = valuesMap.get(value);
        if (str == null) {
            return String.format("Unknown %X", value);
        }
        return str;
    }

    @Override
    public String asStringNoUnit(byte[] data) {
        return asString(data);
    }

    @Override
    public String asString(byte[] data) {
        if (data.length == 0) {
            return "Not Available";
        }
        Double aDouble = asValue(data);
        int value = aDouble.intValue();
        return find(value);
    }

    public void addValue(int value, String meaning) {
        valuesMap.put(value, meaning);
    }
}
