/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests the {@link DM6PendingEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM6PendingEmissionDTCPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEquals("DM6", new DM6PendingEmissionDTCPacket(packet).getName());
    }

    @Test
    public void testPGN() {
        assertEquals(65231, DM6PendingEmissionDTCPacket.PGN);
    }

}
