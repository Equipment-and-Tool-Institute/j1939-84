/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM28PermanentEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM28PermanentEmissionDTCPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEquals("DM28", new DM28PermanentEmissionDTCPacket(packet).getName());
    }

    @Test
    public void testPGN() {
        assertEquals(64896, DM28PermanentEmissionDTCPacket.PGN);
    }

    @Test
    public void testToString() {
        int[] data = { 0x42, 0xFD, 0x9D, 0x00, 0x07, 0x01, 0xFF, 0xFF };
        Packet packet = Packet.create(0, 0, data);
        DM28PermanentEmissionDTCPacket dm28Packet = new DM28PermanentEmissionDTCPacket(packet);

        String expected = "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" + NL +
                "DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment - 1 times";
        assertEquals(expected, dm28Packet.toString());
    }

}
