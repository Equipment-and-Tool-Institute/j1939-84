/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM12MILOnEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM12MILOnEmissionDTCPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        assertEquals("DM12", new DM12MILOnEmissionDTCPacket(packet).getName());
    }

    @Test
    public void testPGN() {
        assertEquals(65236, DM12MILOnEmissionDTCPacket.PGN);
    }

}
