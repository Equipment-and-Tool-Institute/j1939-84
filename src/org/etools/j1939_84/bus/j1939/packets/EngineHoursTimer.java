/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Objects;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class EngineHoursTimer {

    public static final long ERROR = Long.MIN_VALUE;

    public static final long NOT_AVAILABLE = Long.MAX_VALUE;

    private static String timerToString(long timer) {
        if (timer == NOT_AVAILABLE) {
            return "n/a";
        } else if (timer == ERROR) {
            return "errored";
        } else {
            return timer + " minutes";
        }

    }

    private final int eiAecdNumber;

    private final long eiAecdTimer1;

    private final long eiAecdTimer2;

    public EngineHoursTimer(byte[] bytes) {
        eiAecdNumber = bytes[0];
        eiAecdTimer1 = getScaledLongValue(bytes, 1, 1);
        eiAecdTimer2 = getScaledLongValue(bytes, 5, 1);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EngineHoursTimer)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        EngineHoursTimer that = (EngineHoursTimer) obj;

        return (eiAecdNumber == that.eiAecdNumber &&
                eiAecdTimer1 == that.eiAecdTimer1 &&
                eiAecdTimer2 == that.eiAecdTimer2);

    }

    /**
     * Returns four bytes (32-bits) from the data at the given index, index+1,
     * index+2, and index+3
     *
     * @param i
     *            the index
     * @return int
     */
    private long get32(byte[] bytes, int i) {
        return ((bytes[i + 3] & 0xFF) << 24) | ((bytes[i + 2] & 0xFF) << 16) | ((bytes[i + 1] & 0xFF) << 8)
                | (bytes[i] & 0xFF);
    }

    /**
     * @return the eiAecdNumber
     */
    public int getEiAecdNumber() {
        return eiAecdNumber;
    }

    /**
     * @return the eiAecdTimer1
     */
    public long getEiAecdTimer1() {
        return eiAecdTimer1;
    }

    /**
     * @return the eiAecdTimer2
     */
    public long getEiAecdTimer2() {
        return eiAecdTimer2;
    }

    /**
     * Helper method to get four bytes at the given index
     *
     * @param index
     *            the index of the byte to get
     * @return four byte
     */
    private long getLong(byte[] bytes, int index) {
        return get32(bytes, index) & 0xFFFFFFFFL;
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
     * @return long
     */
    private long getScaledLongValue(byte[] bytes, int index, long divisor) {
        byte upperByte = bytes[index + 3];
        switch (upperByte) {
        case (byte) 0xFF:
            return NOT_AVAILABLE;
        case (byte) 0xFE:
            return ERROR;
        default:
            return getLong(bytes, index) / divisor;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(eiAecdNumber, eiAecdTimer1, eiAecdTimer2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EngineHoursTimer");
        sb.append(NL)
                .append("  EI-AECD Number = ")
                .append(eiAecdNumber)
                .append(NL)
                .append("  EI-AECD Engine Hours Timer 1 = " + timerToString(eiAecdTimer1) + NL)
                .append("  EI-AECD Engine Hours Timer 2 = " + timerToString(eiAecdTimer2));
        return sb.toString();
    }

}
