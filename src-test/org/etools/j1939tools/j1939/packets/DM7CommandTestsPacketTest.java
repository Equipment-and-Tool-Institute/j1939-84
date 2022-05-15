/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.DM7CommandTestsPacket.PGN;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests for the {@link DM7CommandTestsPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM7CommandTestsPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00);
        DM7CommandTestsPacket instance = new DM7CommandTestsPacket(packet);
        assertEquals("DM7", instance.getName());
    }

    @Test
    public void testPGN() {
        assertEquals(58112, PGN);
    }

    @Test
    public void testInstance() {

        var instance = DM7CommandTestsPacket.create(0xF9, 0xA5, 247, 123456, 14);

        Packet packet = instance.getPacket();
        assertEquals(PGN, packet.getPgn());
        assertEquals(0xA5, packet.getDestination());
        assertEquals(0xF9, packet.getSource());

        assertEquals(247, instance.getTestId());
        assertEquals(123456, instance.getSpn());
        assertEquals(14, instance.getFmi());
    }
    @Test
    public void testPacking() {

        var instance = DM7CommandTestsPacket.create(0xF9, 0xA5, 247, 0x7FFFF, 14);

        Packet packet = instance.getPacket();
        assertEquals(PGN, packet.getPgn());
        assertEquals(0xA5, packet.getDestination());
        assertEquals(0xF9, packet.getSource());

        assertEquals(247, instance.getTestId());
        assertEquals(0x7FFFF, instance.getSpn());
        assertEquals(14, instance.getFmi());
    }

}
