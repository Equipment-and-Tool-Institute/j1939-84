/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.resources.Resources;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Defines an SAE SLOT (Scaling, Limit, Offset, and Transfer Function)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Slot {

    /**
     * The Map of SLOT ID to Slot for lookups
     */
    private static Map<Integer, Slot> slots;

    /**
     * Helper method to convert from the given {@link String} to a double
     *
     * @param string
     *            the {@link String} to convert
     * @return Double or null if the string cannot be converted
     */
    private static Double doubleValue(String string) {
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Finds a SLOT given the Identifier of the SLOT
     *
     * @param id
     *            the Identifier of the SLOT
     * @return a SLOT or null if not found
     */
    public static Slot findSlot(int id) {
        return getSlots().get(id);
    }

    /**
     * Caches and returns all known {@link Slot}s
     *
     * @return a Map of SLOT ID to Slot
     */
    private static Map<Integer, Slot> getSlots() {
        if (slots == null) {
            slots = loadSlots();

        }
        return slots;
    }

    /**
     * Helper method to convert from the given {@link String} to an int
     *
     * @param string
     *            the {@link String} to convert
     * @return int
     */
    private static int intValue(String string) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Read the slots.csv file which contains all the SLOTs
     *
     * @return Map of SLOT ID to Slot
     */
    private static Map<Integer, Slot> loadSlots() {
        Map<Integer, Slot> slots = new HashMap<>();
        String[] values;

        final InputStream is = Resources.class.getResourceAsStream("j1939da-slots.csv");
        final InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.ISO_8859_1);
        try (CSVReader reader = new CSVReaderBuilder(isReader).withSkipLines(2).build()) {
            while ((values = reader.readNext()) != null) {
                final int id = Integer.parseInt(values[0]);
                final String name = values[1];
                final String type = values[2];
                final Double scaling = doubleValue(values[3]);
                final String unit = values[4];
                final Double offset = doubleValue(values[5]);
                final int length = intValue(values[6]);
                Slot slot = new Slot(id, name, type, scaling, offset, unit, length);
                slots.put(id, slot);
            }
        } catch (Exception e) {
            J1939_84.getLogger().log(Level.SEVERE, "Error loading map from slots", e);
        }
        return slots;
    }

    private final int id;

    private final int length; // bits

    private final String name;

    private final Double offset;

    private final Double scaling;

    private final String type;

    private final String unit;

    private Slot(int id, String name, String type, Double scaling, Double offset, String unit, int length) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.scaling = scaling;
        this.offset = offset;
        this.unit = unit;
        this.length = length;
    }

    /**
     * Returns the data as a scaled value include the units of measure (if
     * applicable)
     *
     * @param data
     *            the byte array containing the data from the packet
     * @return a String of the value with units of measure
     */
    public String asString(byte[] data) {
        if (isAscii()) {
            String result = new String(data, StandardCharsets.UTF_8);
            if (type.contains("variable, ")) {
                if (type.contains("*")) {
                    return result.split("\\*")[0];
                } else if (type.contains("NULL")) {
                    return result.split(Character.toString(0))[0];
                }
            }
            return result;
        }

        long value = toValue(data);

        if (isBitField()) {
            return String.format("%" + length + "s", Long.toBinaryString(value)).replace(' ', '0');
        }

        if (isNotAvailable(data)) {
            return "Not Available";
        }

        if (isError(data)) {
            return "Error";
        }

        String printedValue = String.format("%f", scale(value));
        if (unit != null) {
            return printedValue + " " + unit;
        } else {
            return printedValue;
        }
    }

    /**
     * Returns the data in a scaled value. If the type is ASCII or the value is
     * NOT_AVAILABLE or ERROR, null is returned
     *
     * @param data
     *            the byte array containing the data from the packet
     * @return the scaled value or null
     */
    public Double asValue(byte[] data) {
        if (isAscii()) {
            return null;
        }

        long value = toValue(data);

        if (isBitField()) {
            return Long.valueOf(value).doubleValue();
        }

        if (isNotAvailable(data) || isError(data)) {
            return null;
        }

        return scale(value);
    }

    private long flipBytes(byte[] data, int byteLength) {
        long value = 0;
        for (int i = 0; i < byteLength; i++) {
            value += ((long) (data[i] & 0xFF)) << i * 8;
        }
        return value;
    }

    /**
     * Returns the Identifier of the SLOT
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the length of the data in bits
     *
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the Name of the SLOT
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the offset
     *
     * @return the offset
     */
    public Double getOffset() {
        return offset;
    }

    /**
     * Returns the scaling
     *
     * @return the scaling
     */
    public Double getScaling() {
        return scaling;
    }

    /**
     * Returns the Type of the SLOT
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the Units of the SLOT
     *
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    private boolean isAscii() {
        return type.toUpperCase(Locale.ROOT).contains("ASCII");
    }

    private boolean isBitField() {
        return type.toUpperCase(Locale.ROOT).startsWith("BIT");
    }

    public boolean isError(byte[] data) {
        if (length == 1 || isAscii()) {
            return false;
        }

        long value = toValue(data);
        if (isBitField()) {
            long mask = mask();
            return (value & mask) == (mask - 1);
        }

        long mask = ((long) 0xFF) << (length - 8);
        long error = ((long) 0xFE) << (length - 8);
        long maskedValue = value & mask;
        return maskedValue == error;
    }

    public boolean isNotAvailable(byte[] data) {
        if (length == 1 || isAscii()) {
            return false;
        }

        long value = toValue(data);
        if (isBitField()) {
            long mask = mask();
            long maskedValue = value & mask;
            return maskedValue == mask;
        }

        long mask = ((long) 0xFF) << (length - 8);
        long maskedValue = value & mask;
        return maskedValue == mask;
    }

    private long mask() {
        return ~0L >>> (64 - length);
    }

    /**
     * Returns a scaled value. That is result = value * scaling + offset
     *
     * @param value
     *            the value to scale
     * @return double
     */
    public double scale(double value) {
        double result = value;
        if (getScaling() != null) {
            result *= getScaling();
        }
        if (getOffset() != null) {
            result += getOffset();
        }
        return result;
    }

    private long toValue(byte[] data) {
        if (length <= 8) {
            return data[0] & 0xFF & mask();
        }

        int byteLength = length / 8;
        if (length % 8 != 0) {
            byteLength++;
        }
        return flipBytes(data, byteLength) & mask();
    }

}
