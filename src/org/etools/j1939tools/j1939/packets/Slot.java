/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Defines an SAE SLOT (Scaling, Limit, Offset, and Transfer Function)
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class Slot {

    private final int id;
    private final int length; // bits
    private final String name;
    private final Double offset;
    private final Double scaling;
    private final String type;
    private final String unit;

    public Slot(int id, String name, String type, Double scaling, Double offset, String unit, int length) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.scaling = scaling;
        this.offset = offset;
        this.unit = unit;
        this.length = length;
    }

    /**
     * @param  data
     *                  the byte array containing the data from the packet
     * @return      a String of the value
     */
    public String asStringNoUnit(byte[] data) {
        return asString(false, data);
    }

    /**
     * @param  data The byte array containing the data from the packet.
     * @return      A String representation of the value with units of measure.
     */
    public String asString(byte[] data) {
        return asString(true, data);
    }

    private String asString(boolean includeUnits, byte[] data) {
        if (data.length == 0) {
            return "Not Available";
        }

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

        if (isFB(data)) {
            return String.format("0x%X", value);
        }

        String printedValue = String.format("%.3f", scale(value));
        if (unit != null && includeUnits) {
            return printedValue + " " + unit;
        } else {
            return printedValue;
        }
    }

    /**
     * Returns the data in a scaled value. If the type is ASCII or the value is
     * NOT_AVAILABLE or ERROR, null is returned
     *
     * @param  data
     *                  the byte array containing the data from the packet
     * @return      the scaled value or null
     */
    public Double asValue(byte[] data) {
        if (isAscii() || data.length == 0) {
            return null;
        }

        long value = toValue(data);

        if (isBitField()) {
            return (double) value;
        }

        if (isNotAvailable(data) || isError(data) || isFB(data)) {
            return null;
        }

        return scale(value);
    }

    public byte[] asBytes(double value) {
        if (isAscii()) {
            return new byte[0];
        }
        double unscaled = unscale(value);
        return toBytes(unscaled);
    }

    private long flipBytes(byte[] data) {
        long value = 0;
        for (int i = 0; i < getByteLength(); i++) {
            value += ((long) (data[i] & 0xFF)) << i * 8;
        }
        return value;
    }

    private byte[] flipBytes(long value) {
        byte[] bytes = new byte[getByteLength()];
        for (int i = 0; i < getByteLength(); i++) {
            bytes[i] += (byte) (value >> (i * 8)) & 0xFF;
        }
        return bytes;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public Double getOffset() {
        return offset;
    }

    public Double getScaling() {
        return scaling;
    }

    public String getType() {
        return type;
    }

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
        if (length == 1 || isAscii() || data.length == 0) {
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

    public boolean isFB(byte[] data) {
        if (length == 1 || isAscii() || data.length == 0) {
            return false;
        }

        long value = toValue(data);

        long mask = ((long) 0xFF) << (length - 8);
        long fb = ((long) 0xFB) << (length - 8);
        long maskedValue = value & mask;
        return maskedValue == fb;
    }

    public boolean isNotAvailable(byte[] data) {
        if (length == 1 || isAscii()) {
            return false;
        }

        if (data.length == 0) {
            return true;
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

    private long mask(int dataLength) {
        return ~0L >>> (dataLength - length);
    }

    private long mask() {
        return ~0L >>> (64 - length);
    }

    /**
     * Returns a scaled value. That is result = value * scaling + offset
     *
     * @param  value
     *                   the value to scale
     * @return       double
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

    private double unscale(double value) {
        double result = value;
        if (getOffset() != null) {
            result -= getOffset();
        }

        if (getScaling() != null) {
            result /= getScaling();
        }
        return result;
    }

    public long toValue(byte[] data) {
        if (data.length == 0) {
            return -1;
        }
        if (length <= 8) {
            return data[0] & 0xFF & mask();
        }
        return flipBytes(data) & mask();
    }

    public int getByteLength() {
        int byteLength = length / 8;
        if (length % 8 != 0) {
            byteLength++;
        }
        return byteLength;
    }

    private byte[] toBytes(double value) {
        long data = Double.valueOf(value).longValue();
        if (length <= 8) {
            return new byte[] { (byte) (data & mask()) };
        }
        return flipBytes(data & mask());
    }


}
