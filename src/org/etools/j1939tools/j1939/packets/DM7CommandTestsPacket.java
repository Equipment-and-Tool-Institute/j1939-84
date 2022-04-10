/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import org.etools.j1939tools.bus.Packet;

/**
 * The DM7 Packet. This isn't used to parse any packets as it will only be sent
 * to the vehicle. Responses will be {@link DM30ScaledTestResultsPacket}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM7CommandTestsPacket extends GenericPacket {

    public static final int PGN = 58112; // 0xE300

    public static DM7CommandTestsPacket create(int address, int destination, int tid, int spn, int fmi) {
        return new DM7CommandTestsPacket(Packet.create(DM7CommandTestsPacket.PGN | destination,
                                                       address,
                                                       true,
                                                       tid,
                                                       spn & 0xFF,
                                                       (spn >> 8) & 0xFF,
                                                       ((((spn >> 16) & 0b111) << 5)) | (fmi & 0x1F),
                                                       0xFF,
                                                       0xFF,
                                                       0xFF,
                                                       0xFF));
    }

    public DM7CommandTestsPacket(Packet packet) {
        super(packet);
    }

    @Override
    public String getName() {
        return "DM7";
    }

    public int getTestId() {
        return getByte(0) & 0xFF;
    }

    public int getSpn() {
        return (getByte(1) & 0xFF) + ((getByte(2) & 0xFF) << 8) + ((getByte(3) & 0xE0) >> 5 << 16);
    }

    public int getFmi() {
        return getByte(3) & 0x1F;
    }

}
