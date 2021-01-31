/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.ParsedPacket.toInts;
import static org.etools.j1939_84.utils.CollectionUtils.join;

import java.util.Objects;

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class EngineHoursTimer {

    public static final long ERROR = Long.MIN_VALUE;
    public static final long NOT_AVAILABLE = Long.MAX_VALUE;

    public static EngineHoursTimer create(int timerNumber, long timer1, long timer2) {
        int[] data = new int[1];
        data[0] = timerNumber;
        data = join(data, toInts(timer1));
        data = join(data, toInts(timer2));
        return new EngineHoursTimer(data);
    }

    private final int eiAecdNumber;
    private final long eiAecdTimer1;
    private final long eiAecdTimer2;
    private final int[] data;

    public EngineHoursTimer(int[] bytes) {
        this.data = bytes;
        eiAecdNumber = bytes[0];
        eiAecdTimer1 = getScaledLongValue(bytes, 1);
        eiAecdTimer2 = getScaledLongValue(bytes, 5);
    }

    public int[] getData() {
        return data;
    }

    public int getEiAecdNumber() {
        return eiAecdNumber;
    }

    public long getEiAecdTimer1() {
        return eiAecdTimer1;
    }

    public long getEiAecdTimer2() {
        return eiAecdTimer2;
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

    @Override
    public int hashCode() {
        return Objects.hash(eiAecdNumber, eiAecdTimer1, eiAecdTimer2);
    }

    @Override
    public String toString() {
        return "EI-AECD Number = " + eiAecdNumber
                + ": Timer 1 = " + timerToString(eiAecdTimer1)
                + "; Timer 2 = " + timerToString(eiAecdTimer2);
    }

    private static long get32(int[] bytes, int i) {
        return ((long) (bytes[i + 3] & 0xFF) << 24) | ((bytes[i + 2] & 0xFF) << 16) | ((bytes[i + 1] & 0xFF) << 8)
                | (bytes[i] & 0xFF);
    }

    /**
     * Returns the 32-bit value at the given index divided by the divisor. If
     * the value is "Error" or "Not Available", those values are returned
     * instead
     *
     * @param index
     *         the index of the value
     * @return long
     */
    private static long getScaledLongValue(int[] bytes, int index) {
        int upperByte = bytes[index + 3];
        switch (upperByte) {
        case 0xFF:
            return NOT_AVAILABLE;
        case 0xFE:
            return ERROR;
        default:
            return get32(bytes, index) & 0xFFFFFFFFL;
        }
    }

    private static String timerToString(long timer) {
        if (timer == NOT_AVAILABLE) {
            return "n/a";
        } else if (timer == ERROR) {
            return "errored";
        } else {
            return timer + " minutes";
        }

    }

}
