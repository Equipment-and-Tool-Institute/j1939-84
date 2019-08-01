/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.nio.charset.StandardCharsets;

import org.etools.j1939_84.NumberFormatter;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.Lookup;

/**
 * Wrapper around {@link Packet}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ParsedPacket {

    public static final double ERROR = Double.MIN_VALUE;
    protected static final double KM_TO_MILES_FACTOR = 0.62137119;
    public static final double NOT_AVAILABLE = Double.MAX_VALUE;

    /**
     * Converts the given byte array into a {@link String}
     *
     * @param bytes
     *            the byte array to convert
     * @return {@link String}
     */
    protected static String format(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            int chr = bytes[i] & 0xFF;
            if ((chr < 0x20) || ((chr > 0x7F) && (chr < 0xA0))) {
                bytes[i] = ' ';
            }
        }
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    protected static String getValuesWithUnits(double primaryValue, String primaryUnits, double secondaryValue,
            String secondaryUnits) {
        String result = getValueWithUnits(primaryValue, primaryUnits);
        if (!isError(primaryValue) && !isNotAvailable(primaryValue)) {
            result += " (" + getValueWithUnits(secondaryValue, secondaryUnits) + ")";
        }
        return result;
    }

    /**
     * Returns the value as a string with units appended, if units is not null.
     * If the value is NOT_AVAILABLE or ERROR, the string for those is returned
     * instead
     *
     * @param value
     *            the value to display as a string
     * @param units
     *            the units to append, can be null
     * @return the value with units appended or "not available"/"error" as
     *         applicable.
     */
    protected static String getValueWithUnits(byte value, String units) {
        if (value == (byte) 0xFF) {
            return "not available";
        }
        if (value == (byte) 0xFE) {
            return "error";
        }
        return (value & 0xFF) + (units != null ? " " + units : "");
    }

    /**
     * Returns the value as a string with units appended, if units is not null.
     * If the value is NOT_AVAILABLE or ERROR, the string for those is returned
     * instead
     *
     * @param value
     *            the value to display as a string
     * @param units
     *            the units to append, can be null
     * @return the value with units appended or "not available"/"error" as
     *         applicable.
     */
    protected static String getValueWithUnits(double value, String units) {
        if (isNotAvailable(value)) {
            return "not available";
        }
        if (isError(value)) {
            return "error";
        }
        String valueString = NumberFormatter.format(value);
        return valueString + (units != null ? " " + units : "");
    }

    /**
     * Returns true if the given value equates to Error
     *
     * @param value
     *            the value to evaluate
     * @return boolean
     */
    protected static boolean isError(double value) {
        return value == ERROR;
    }

    /**
     * Returns true if the given value equates to Not Available
     *
     * @param value
     *            the value to evaluate
     * @return boolean
     */
    protected static boolean isNotAvailable(double value) {
        return value == NOT_AVAILABLE;
    }

    /**
     * The wrapped packet
     */
    private final Packet packet;

    /**
     * Constructor
     *
     * @param packet
     *            the {@link Packet} to wrap
     */
    public ParsedPacket(Packet packet) {
        this.packet = packet;
    }

    /**
     * Helper method to get one byte at the given index
     *
     * @param index
     *            the index of the byte to get
     * @return one byte
     */
    protected byte getByte(int index) {
        return (byte) (getPacket().get(index) & 0xFF);
    }

    /**
     * Helper method to get four bytes at the given index
     *
     * @param index
     *            the index of the byte to get
     * @return four byte
     */
    protected long getInt(int index) {
        return getPacket().get32(index) & 0xFFFFFFFFL;
    }

    /**
     * Returns the SAE name of the packet
     *
     * @return String
     */
    public String getName() {
        return String.valueOf(getPacket().getId());
    }

    /**
     * Returns the wrapped {@link Packet}
     *
     * @return the {@link Packet}
     */
    public final Packet getPacket() {
        return packet;
    }

    /**
     * Returns the 32-bit value at the given index divided by the divisor. If
     * the value is "Error" or "Not Available", those values are returned
     * instead
     *
     * @param index
     *            the index of the value
     * @param divisor
     *            the divisor for scaling
     * @return double
     */
    protected double getScaledIntValue(int index, double divisor) {
        byte upperByte = getByte(index + 3);
        switch (upperByte) {
        case (byte) 0xFF:
            return NOT_AVAILABLE;
        case (byte) 0xFE:
            return ERROR;
        default:
            return getInt(index) / divisor;
        }
    }

    /**
     * Returns the 16-bit value at the given index divided by the divisor. If
     * the value is "Error" or "Not Available", those values are returned
     * instead
     *
     * @param index
     *            the index of the value
     * @param divisor
     *            the divisor for scaling
     * @return double
     */
    protected double getScaledShortValue(int index, double divisor) {
        byte upperByte = getByte(index + 1);
        switch (upperByte) {
        case (byte) 0xFF:
            return NOT_AVAILABLE;
        case (byte) 0xFE:
            return ERROR;
        default:
            return getShort(index) / divisor;
        }
    }

    /**
     * Helper method to get two bits at the given byte index
     *
     * @param index
     *            the index of the byte that contains the bits
     * @param mask
     *            the bit mask for the bits
     * @param shift
     *            the number bits to shift right so the two bits are fully right
     *            shifted
     * @return two bit value
     */
    protected int getShaveAndAHaircut(int index, int mask, int shift) {
        return (getByte(index) & mask) >> shift;
    }

    /**
     * Helper method to get two bytes at the given index
     *
     * @param index
     *            the index of the bytes to get
     * @return two bytes
     */
    protected int getShort(int index) {
        return getPacket().get16(index) & 0xFFFF;
    }

    /**
     * Returns the Source Address of the wrapped Packet
     *
     * @return the integer value of the source address
     */
    public int getSourceAddress() {
        return getPacket().getSource();
    }

    /**
     * Returns the prefix that's used in the toString methods
     *
     * @return {@link String}
     */
    protected String getStringPrefix() {
        return getName() + " from " + Lookup.getAddressName(getSourceAddress()) + ": ";
    }

    @Override
    public String toString() {
        return getPacket().toString();
    }

}
