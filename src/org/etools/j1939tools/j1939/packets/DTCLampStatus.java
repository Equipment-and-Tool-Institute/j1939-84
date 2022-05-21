/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.Objects;

import org.etools.j1939tools.bus.Packet;


/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DTCLampStatus {

    private final int[] data;
    private LampStatus awlStatus;
    private DiagnosticTroubleCode dtc;
    private LampStatus milStatus;
    private LampStatus plStatus;
    private LampStatus rslStatus;

    /**
     * Constructor
     *
     * @param data
     *                 the {@link Packet} to parse
     */
    public DTCLampStatus(int[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    public static DTCLampStatus create(DiagnosticTroubleCode dtc,
                                       LampStatus amberWarnLamp,
                                       LampStatus milLamp,
                                       LampStatus protectLamp,
                                       LampStatus redStopLamp) {

        int[] bytes = Arrays.copyOf(dtc.getData(), 6);

        // | all the support bits (see notes on the class called)
        bytes[4] = LampStatus.getBytes(milLamp)[0] << 6 |
                LampStatus.getBytes(redStopLamp)[0] << 4 |
                LampStatus.getBytes(amberWarnLamp)[0] << 2 |
                LampStatus.getBytes(protectLamp)[0];

        // | all the states bits (see notes on the class called)
        bytes[5] = LampStatus.getBytes(milLamp)[1] << 6 |
                LampStatus.getBytes(redStopLamp)[1] << 4 |
                LampStatus.getBytes(amberWarnLamp)[1] << 2 |
                LampStatus.getBytes(protectLamp)[1];

        return new DTCLampStatus(bytes);
    }

    /**
     * Returns the Amber Warning Lamp (AWL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getAmberWarningLampStatus() {
        if (awlStatus == null) {
            awlStatus = getLampStatus(0x0C, 2);
        }
        return awlStatus;
    }

    /**
     * Helper method to get one byte at the given index
     *
     * @param  index
     *                   the index of the byte to get
     * @return       one byte
     */
    private byte getByte(int index) {
        return (byte) (getData()[index] & 0xFF);
    }

    /**
     * @return the data
     */
    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the a link to a {@link DiagnosticTroubleCode}. If the only "DTC"
     * has an SPN of 0 or 524287, the list will be empty, but never null
     *
     * @return DTC
     */
    public DiagnosticTroubleCode getDtc() {
        if (dtc == null) {
            dtc = parseDTC();
        }
        return dtc;
    }

    /**
     * Helper method to get a {@link LampStatus}
     *
     * @param  mask
     *                   the bit mask
     * @param  shift
     *                   the number of bits to shift to the right
     * @return       the {@link LampStatus} that corresponds to the value
     */
    private LampStatus getLampStatus(int mask, int shift) {
        int onOff = getShaveAndAHaircut(4, mask, shift);
        int flash = getShaveAndAHaircut(5, mask, shift);
        return LampStatus.getStatus(onOff, flash);
    }

    /**
     * Returns the Malfunction Indicator Lamp (MIL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getMalfunctionIndicatorLampStatus() {
        if (milStatus == null) {
            milStatus = getLampStatus(0xC0, 6);
        }
        return milStatus;
    }

    /**
     * Returns the Protect Lamp Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getProtectLampStatus() {
        if (plStatus == null) {
            plStatus = getLampStatus(0x03, 0);
        }
        return plStatus;
    }

    /**
     * Returns the Red Stop Lamp (RSL) Status
     *
     * @return {@link LampStatus}
     */
    public LampStatus getRedStopLampStatus() {
        if (rslStatus == null) {
            rslStatus = getLampStatus(0x30, 4);
        }
        return rslStatus;
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
    private int getShaveAndAHaircut(int index, int mask, int shift) {
        return (getByte(index) & mask) >> shift;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAmberWarningLampStatus(),
                            getMalfunctionIndicatorLampStatus(),
                            getProtectLampStatus(),
                            getRedStopLampStatus(),
                            getDtc());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DTCLampStatus)) {
            return false;
        }
        DTCLampStatus that = (DTCLampStatus) obj;
        return Arrays.equals(getData(), that.getData());
    }

    @Override
    public String toString() {
        String result = "MIL: " + getMalfunctionIndicatorLampStatus()
                + ", RSL: " + getRedStopLampStatus()
                + ", AWL: " + getAmberWarningLampStatus()
                + ", PL: " + getProtectLampStatus() + NL;
        result += getDtc().toString();
        return result;
    }

    private DiagnosticTroubleCode parseDTC() {
        return new DiagnosticTroubleCode(Arrays.copyOfRange(data, 0, 4));
    }
}
