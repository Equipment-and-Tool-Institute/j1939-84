/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import java.util.Arrays;

import org.etools.j1939_84.bus.Packet;

/**
 * The {@link ParsedPacket} for Diagnostic Trouble Code Counts
 * Codes (DM29)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 * This DM conveys the number of regulated DTC counts (Pending, Permanent, MIL-
 * On, PMIL-On)
 */
public class DM29DtcCounts extends ParsedPacket {
    // Hex value of PGN = 009E00
    public static final int PGN = 40448;

    private int allPendingDTCCount = -1;
    private int emissionRelatedMILOnDTCCount = -1;
    private int emissionRelatedPendingDTCCount = -1;
    private int emissionRelatedPermanentDTCCount = -1;
    private int emissionRelatedPreviouslyMILOnDTCCount = -1;

    public DM29DtcCounts(Packet packet) {
        super(packet);
    }

    /**
     * @return the allPendingDTCCount
     */
    public int getAllPendingDTCCount() {
        if (allPendingDTCCount == -1) {
            parsePacket();
        }
        return allPendingDTCCount;
    }

    /**
     * @return the emissionRelatedMILOnDTCCount
     */
    public int getEmissionRelatedMILOnDTCCount() {
        if (emissionRelatedMILOnDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedMILOnDTCCount;
    }

    /**
     * @return the emissionRelatedPendingDTCCount
     */
    public int getEmissionRelatedPendingDTCCount() {
        if (emissionRelatedPendingDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedPendingDTCCount;
    }

    /**
     * @return the emissionRelatedPermanentDTCCount
     */
    public int getEmissionRelatedPermanentDTCCount() {
        if (emissionRelatedPermanentDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedPermanentDTCCount;
    }

    /**
     * @return the emissionRelatedPreviouslyMILOnDTCCount
     */
    public int getEmissionRelatedPreviouslyMILOnDTCCount() {
        if (emissionRelatedPreviouslyMILOnDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedPreviouslyMILOnDTCCount;
    }

    @Override
    public String getName() {
        return "DM29";
    }

    /**
     * Parses the packet to populate all the member variables
     */
    private void parsePacket() {
        final int length = getPacket().getLength();
        byte[] data = Arrays.copyOf(getPacket().getBytes(), length);
        System.out.println("length is " + length + "  " + getPacket().getBytes()[0]);
        emissionRelatedPendingDTCCount = data[0];
        allPendingDTCCount = data[1];
        emissionRelatedMILOnDTCCount = data[2];
        emissionRelatedPreviouslyMILOnDTCCount = data[3];
        emissionRelatedPermanentDTCCount = data[4];
    }
}
