/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import java.util.Arrays;
import java.util.Objects;

import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * Class that contains the data about Supported SPNs
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class SupportedSPN {

    private final byte length;
    private final int spn;
    private final int support;
    private final int[] data;

    /**
     * Constructor
     *
     * @param data
     *                 the data that contains the information
     */
    public SupportedSPN(int[] data) {

        this.data = Arrays.copyOf(data, data.length);
        support = data[2] & 0x0F ; 
        spn = SupportedSPN.parseSPN(data);
        length = (byte) (data[3] & 0xFF);
    }

    public static SupportedSPN create(int spn,
                                      boolean isScaledTestResult,
                                      boolean isDataStream,
                                      boolean isFreezeFrame,
                                      boolean isRationalData,
                                      int length) {
        byte byte1 = (byte) (spn & 0xFF);
        byte byte2 = (byte) ((spn >> 8) & 0xFF);

        byte byte3 = (byte) 0xFF;
        byte3 &= (byte) (((spn >> 16) & 0xE0) + 0x1F);
        byte3 &= (byte) (isFreezeFrame ? 0xFE : 0xFF);
        byte3 &= (byte) (isDataStream ? 0xFD : 0xFF);
        byte3 &= (byte) (isScaledTestResult ? 0xFB : 0xFF);
        byte3 &= (byte) (isRationalData ? 0xF7 : 0xFF);

        byte byte4 = (byte) (length & 0xFF);

        return new SupportedSPN(new int[] { byte1, byte2, byte3, byte4 });
    }

    /**
     * Parses the data to return the SPN
     *
     * @param  data
     *                  the data to parse
     * @return      the SPN
     */
    public static int parseSPN(int[] data) {
        // Byte: 0 bits 8-1 SPN, 8 least significant bits of SPN (most
        // significant at bit 8)
        // Byte: 1 bits 8-1 SPN, second byte of SPN (most significant at bit 8)
        // Byte: 2 bits 8-6 SPN, 3 most significant bits (most significant at
        // bit 8)
        return (((data[2] & 0xE0) << 11) & 0xFF0000) | ((data[1] << 8) & 0xFF00) | (data[0] & 0xFF);
    }

    /**
     * Returns the length of the support data
     *
     * @return byte
     */
    public byte getLength() {
        return length;
    }

    /**
     * Returns the Suspect Parameter Number
     *
     * @return int
     */
    public int getSpn() {
        return spn;
    }

    public int[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(support, spn, length);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SupportedSPN)) {
            return false;
        }

        SupportedSPN that = (SupportedSPN) obj;
        return Objects.equals(length, that.length)
                && Objects.equals(spn, that.spn)
                && Objects.equals(support, that.support);
    }

    @Override
    public String toString() {
        return "SPN " + getSpn() + " - " + Lookup.getSpnName(getSpn());
    }

    /**
     * Returns true if Data Stream is supported
     *
     * @return boolean
     */
    public boolean supportsDataStream() {
        return (support & 0x02) == 0x00;
    }

    /**
     * Returns true if the Expanded Freeze Frame is supported
     *
     * @return boolean
     */
    public boolean supportsExpandedFreezeFrame() {
        return (support & 0x01) == 0x00;
    }

    /**
     * Returns true if Scaled Test Results are supported
     *
     * @return boolean
     */
    public boolean supportsScaledTestResults() {
        return (support & 0x04) == 0x00;
    }

    /**
     * Returns true if Rationality Fault SP Data is supported
     *
     * @return boolean
     */
    public boolean supportsRationalityFaultData() {
        return (support & 0x08) == 0x00;
    }
}
