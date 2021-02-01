/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;

import java.util.Arrays;
import java.util.Objects;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;

/**
 * The {@link ParsedPacket} for Diagnostic Trouble Code Counts Codes (DM29)
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 * <p>
 * This DM conveys the number of regulated DTC counts (Pending,
 * Permanent, MIL- On, PMIL-On)
 */
public class DM29DtcCounts extends GenericPacket {
    public static final int PGN = 40448; //9E00

    public static DM29DtcCounts create(int source,
                                       int pendingCount,
                                       int allCount,
                                       int milCount,
                                       int previousCount,
                                       int permanentCount) {
        byte[] data = new byte[8];
        data[0] = (byte) (pendingCount & 0xFF);
        data[1] = (byte) (allCount & 0xFF);
        data[2] = (byte) (milCount & 0xFF);
        data[3] = (byte) (previousCount & 0xFF);
        data[4] = (byte) (permanentCount & 0xFF);
        data[5] = (byte) 0xFF;
        data[6] = (byte) 0xFF;
        data[7] = (byte) 0xFF;

        return new DM29DtcCounts(Packet.create(PGN, source, data));
    }

    private int allPendingDTCCount = -1;
    private int emissionRelatedMILOnDTCCount = -1;
    private int emissionRelatedPendingDTCCount = -1;
    private int emissionRelatedPermanentDTCCount = -1;
    private int emissionRelatedPreviouslyMILOnDTCCount = -1;

    public DM29DtcCounts(Packet packet) {
        super(packet, new J1939DaRepository().findPgnDefinition(PGN));
    }

    public int getAllPendingDTCCount() {
        if (allPendingDTCCount == -1) {
            parsePacket();
        }
        return allPendingDTCCount;
    }

    public int getEmissionRelatedMILOnDTCCount() {
        if (emissionRelatedMILOnDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedMILOnDTCCount;
    }

    public int getEmissionRelatedPendingDTCCount() {
        if (emissionRelatedPendingDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedPendingDTCCount;
    }

    public int getEmissionRelatedPermanentDTCCount() {
        if (emissionRelatedPermanentDTCCount == -1) {
            parsePacket();
        }
        return emissionRelatedPermanentDTCCount;
    }

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

    public boolean hasNonZeroCounts(Boolean allPendingSupported) {
        boolean result = false;
        if (allPendingSupported != null) {
            result = getAllPendingDTCCount() != (allPendingSupported ? 0 : (byte) 0xFF);
        }

        return result
                || getEmissionRelatedPendingDTCCount() != 0
                || getEmissionRelatedMILOnDTCCount() != 0
                || getEmissionRelatedPreviouslyMILOnDTCCount() != 0
                || getEmissionRelatedPermanentDTCCount() != 0;
    }

    private void parsePacket() {
        final int length = getPacket().getLength();
        byte[] data = Arrays.copyOf(getPacket().getBytes(), length);
        emissionRelatedPendingDTCCount = data[0];
        allPendingDTCCount = data[1];
        emissionRelatedMILOnDTCCount = data[2];
        emissionRelatedPreviouslyMILOnDTCCount = data[3];
        emissionRelatedPermanentDTCCount = data[4];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        DM29DtcCounts that = (DM29DtcCounts) o;

        return getAllPendingDTCCount() == that.getAllPendingDTCCount()
                && getEmissionRelatedMILOnDTCCount() == that.getEmissionRelatedMILOnDTCCount()
                && getEmissionRelatedPendingDTCCount() == that.getEmissionRelatedPendingDTCCount()
                && getEmissionRelatedPermanentDTCCount() == that.getEmissionRelatedPermanentDTCCount()
                && getEmissionRelatedPreviouslyMILOnDTCCount() == that.getEmissionRelatedPreviouslyMILOnDTCCount();
    }

    @Override public int hashCode() {
        return Objects.hash(super.hashCode(),
                            getAllPendingDTCCount(),
                            getEmissionRelatedMILOnDTCCount(),
                            getEmissionRelatedPendingDTCCount(),
                            getEmissionRelatedPermanentDTCCount(),
                            getEmissionRelatedPreviouslyMILOnDTCCount());
    }

    @Override
    public String toString() {
        String result = "";
        result += getStringPrefix() + NL;

        String count = getValueWithUnits((byte) getEmissionRelatedPendingDTCCount(), null);
        result += String.format("%1$-45s %2$20s", "Emission-Related Pending DTC Count", count) + NL;

        count = getValueWithUnits((byte) getAllPendingDTCCount(), null);
        result += String.format("%1$-45s %2$20s", "All Pending DTC Count", count) + NL;

        count = getValueWithUnits((byte) getEmissionRelatedMILOnDTCCount(), null);
        result += String.format("%1$-45s %2$20s", "Emission-Related MIL-On DTC Count", count) + NL;

        count = getValueWithUnits((byte) getEmissionRelatedPreviouslyMILOnDTCCount(), null);
        result += String.format("%1$-45s %2$20s", "Emission-Related Previously MIL-On DTC Count", count) + NL;

        count = getValueWithUnits((byte) getEmissionRelatedPermanentDTCCount(), null);
        result += String.format("%1$-45s %2$20s", "Emission-Related Permanent DTC Count", count);
        return result;
    }

}
