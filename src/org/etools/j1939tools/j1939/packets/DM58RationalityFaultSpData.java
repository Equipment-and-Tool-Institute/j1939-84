/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.utils.CollectionUtils.join;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.utils.CollectionUtils;

/**
 * Parses the Rationality Fault SPN Data (DM58) Packet
 * Byte1 the test identifier (SPN 1224 shall echo the value of TID 245
 * Byte2 bits 8-1 Rationality Fault SPN, 8 least significant bits of SPN
 * Byte3 bits 8-1 Rationality Fault SPN, second byte of SPN
 * Byte4 bits 8-6 Rationality Fault SPN, 3 most significant bits
 * bits 5-1 Reserved (set to binary ones)
 * Bytes 5-8 Rationality fault SPN data value
 *
 * @author Marianne Schaefer (marianne@soliddesign.net)
 */
public class DM58RationalityFaultSpData extends GenericPacket {

    public static final int PGN = 64475; // 0xFBDB
    private Integer spnId = null;
    private byte[] dataBytes = null;
    private Spn spn = null;

    public DM58RationalityFaultSpData(Packet packet) {
        super(packet);
    }

    public static DM58RationalityFaultSpData create(int address, int tid, SupportedSPN... spns) {
        int[] data = new int[] { tid };
        for (SupportedSPN spn : spns) {
            data = join(data, spn.getData());
        }
        return new DM58RationalityFaultSpData(Packet.create(PGN, address, data));
    }

    public static DM58RationalityFaultSpData create(int address, int tid, int spn, int[] data) {
        int[] packageData = { tid, spn & 0xFF, (spn >> 8) & 0xFF, ((((spn >> 16) & 0b111) << 5)) };
        packageData = CollectionUtils.join(packageData, data);
        return new DM58RationalityFaultSpData(Packet.create(DM58RationalityFaultSpData.PGN,
                                                            address,
                                                            true,
                                                            packageData));
    }

    @Override
    public String getName() {
        return "DM58";
    }

    public int getTestId() {
        return getByte(0) & 0xFF;
    }

    public int getSpnId() {
        if (spnId == null) {
            int[] bytes = { getByte(1), getByte(2), getByte(3) };
            spnId = SupportedSPN.parseSPN(bytes);
        }
        return spnId;
    }

    public byte[] getSpnDataBytes() {
        if (dataBytes == null) {
            byte[] bytes = getPacket().getBytes();
            dataBytes = Arrays.copyOfRange(bytes, 4, bytes.length);
        }
        return dataBytes;
    }

    @Override
    public String toString() {
        return getStringPrefix() + NL +
                "  Test Identifier: " + getTestId() + NL +
                "  Rationality Fault SPN: " + getSpnId() + NL +
                "  Rationality Fault SPN Data Value: [" + getFormattedData() + "]" + NL +
                "  " + getSpn() + NL;
    }

    public String getFormattedData() {
        return Arrays.stream(getSpn().getData())
                     .mapToObj(x -> String.format("%02x", x).toUpperCase())
                     .collect(Collectors.joining(" "));
    }

    /**
     * Parses a portion of the packet to create a {@link Spn}
     * 
     * @return a {@link Spn}
     */
    public Spn getSpn() {
        if (spn == null) {
            SpnDefinition spnDefinition = J1939DaRepository.getInstance().findSpnDefinition(getSpnId());
            Slot slot = J1939DaRepository.getInstance().findSLOT(spnDefinition.getSlotNumber(), getSpnId());
            spn = new Spn(getSpnId(), spnDefinition.getLabel(), slot, getSpnDataBytes());
        }
        return spn;
    }

}
