/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.utils.NumberFormatter;

/**
 * Wrapper around {@link Packet}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ParsedPacket {

    public static final double ERROR = Double.MIN_VALUE;
    public static final double NOT_AVAILABLE = Double.MAX_VALUE;
    protected static final double KM_TO_MILES_FACTOR = 0.62137119;
    /**
     * The wrapped packet
     */
    private final Packet packet;

    /**
     * Constructor
     *
     * @param packet
     *                   the {@link Packet} to wrap
     */
    public ParsedPacket(Packet packet) {
        this.packet = packet;
    }

    public static byte[] to2Bytes(int value) {
        return new byte[] { (byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF) };
    }

    public static byte[] to4Bytes(long value) {
        return new byte[] { (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF) };
    }

    public static int[] to2Ints(int value) {
        return new int[] { (value & 0xFF), ((value >> 8) & 0xFF) };
    }

    public static int[] to3Ints(int value) {
        return new int[] { (byte) (value & 0xFF), (byte) ((value >> 8) & 0xFF), (byte) ((value >> 16) & 0xFF) };
    }

    public static int[] to4Ints(long value) {
        return new int[] { (int) (value & 0xFF),
                (int) ((value >> 8) & 0xFF),
                (int) ((value >> 16) & 0xFF),
                (int) ((value >> 24) & 0xFF) };
    }

    /**
     * Converts the given byte array into a {@link String}
     *
     * @param  bytes
     *                   the byte array to convert
     * @return       {@link String}
     */
    protected static String format(byte[] bytes) {
        byte[] localBytes = Arrays.copyOf(bytes, bytes.length);
        for (int i = 0; i < localBytes.length; i++) {
            int chr = localBytes[i] & 0xFF;
            if ((chr < 0x20) || ((chr > 0x7F) && (chr < 0xA0))) {
                localBytes[i] = ' ';
            }
        }
        return new String(localBytes, StandardCharsets.ISO_8859_1);
    }

    /**
     * Finds and returns the index of the asterisk in the data
     *
     * @param  data
     *                  the data of interest
     * @return      the index of the asterisk, -1 if there is no asterisk
     */
    protected static int getAsteriskOrNullIndex(byte[] data) {
        int index = -1;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '*' || data[i] == '\0') {
                index = i;
                break;
            }
        }
        return index;
    }

    protected static String getValuesWithUnits(double primaryValue,
                                               String primaryUnits,
                                               double secondaryValue,
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
     * @param  value
     *                   the value to display as a string
     * @param  units
     *                   the units to append, can be null
     * @return       the value with units appended or "not available"/"error" as
     *               applicable.
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
     * @param  value
     *                   the value to display as a string
     * @param  units
     *                   the units to append, can be null
     * @return       the value with units appended or "not available"/"error" as
     *               applicable.
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
     * @param  value
     *                   the value to evaluate
     * @return       boolean
     */
    protected static boolean isError(double value) {
        return value == ERROR;
    }

    /**
     * Returns true if the given value equates to Not Available
     *
     * @param  value
     *                   the value to evaluate
     * @return       boolean
     */
    public static boolean isNotAvailable(double value) {
        return value == NOT_AVAILABLE;
    }

    /**
     * Searches the data for the first asterisk returning the ASCII
     * representation between the start of the data and the asterisk. If there
     * is no asterisk, then entire data is translated and returned as ASCII
     *
     * @param  data
     *                  the byte array containing the field
     * @return      the ASCII translation of the field data
     */
    protected static String parseField(byte[] data) {
        return parseField(data, true);
    }

    /**
     * Searches the data for the first asterisk returning the ASCII
     * representation between the start of the data and the asterisk. If there
     * is no asterisk, then entire data is translated and returned as ASCII
     *
     * @param  data
     *                  the byte array containing the field
     * @param  trim
     *                  true to indicate the results should be trimmed
     * @return      the ASCII translation of the field data
     */
    protected static String parseField(byte[] data, boolean trim) {
        // Find the location of the *
        int index = getAsteriskOrNullIndex(data);
        if (index >= 0) {
            // It has a * or \0, return just the field
            data = Arrays.copyOf(data, index);
        }
        if (trim) {
            return format(data).trim();
        } else {
            return format(data);
        }
    }

    /**
     * Helper method to get one byte at the given index
     *
     * @param  index
     *                   the index of the byte to get
     * @return       one byte
     */
    protected byte getByte(int index) {
        return (byte) (getPacket().get(index) & 0xFF);
    }

    /**
     * Helper method to get four bytes at the given index
     *
     * @param  index
     *                   the index of the byte to get
     * @return       four byte
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
        return String.valueOf(getPacket().getPgn());
    }

    /**
     * Returns the wrapped {@link Packet}
     *
     * @return the {@link Packet}
     */
    public Packet getPacket() {
        return packet;
    }

    /**
     * Returns the 32-bit value at the given index divided by the divisor. If
     * the value is "Error" or "Not Available", those values are returned
     * instead
     *
     * @param  index
     *                     the index of the value
     * @param  divisor
     *                     the divisor for scaling
     * @return         double
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
     * @param  index
     *                     the index of the value
     * @param  divisor
     *                     the divisor for scaling
     * @return         double
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
     * @param  index
     *                   the index of the byte that contains the bits
     * @param  mask
     *                   the bit mask for the bits
     * @param  shift
     *                   the number bits to shift right so the two bits are fully right
     *                   shifted
     * @return       two bit value
     */
    protected int getShaveAndAHaircut(int index, int mask, int shift) {
        return (getByte(index) & mask) >> shift;
    }

    /**
     * Helper method to get two bytes at the given index
     *
     * @param  index
     *                   the index of the bytes to get
     * @return       two bytes
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

    public String getModuleName() {
        return Lookup.getAddressName(getSourceAddress());
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
    public int hashCode() {
        return packet.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ParsedPacket)) {
            return false;
        }

        ParsedPacket that = (ParsedPacket) obj;

        return getClass() == that.getClass() && packet.equals(that.packet);
    }

    @Override
    public String toString() {
        return getPacket().toString();
    }
}
