/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests the {@link DM23PreviouslyMILOnEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM23PreviouslyMILOnEmissionDTCPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEquals("DM23", new DM23PreviouslyMILOnEmissionDTCPacket(packet).getName());
    }

    @Test
    public void testPGN() {
        assertEquals(64949, DM23PreviouslyMILOnEmissionDTCPacket.PGN);
    }

}