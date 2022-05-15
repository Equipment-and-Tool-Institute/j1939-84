/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests for the {@link DM11ClearActiveDTCsPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM11ClearActiveDTCsPacketTest {

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0, 0, 0);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11", instance.getName());
    }

    @Test
    public void testPGN() {
        assertEquals(65235, DM11ClearActiveDTCsPacket.PGN);
    }

    @Test
    public void testToStringAck() {
        Packet packet = Packet.create(0, 0, 0);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11 from Engine #1 (0): Response is Acknowledged", instance.toString());
    }

    @Test
    public void testToStringBusy() {
        Packet packet = Packet.create(0, 0, 0x03);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11 from Engine #1 (0): Response is Busy", instance.toString());
    }

    @Test
    public void testToStringDenied() {
        Packet packet = Packet.create(0, 0, 0x02);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11 from Engine #1 (0): Response is Denied", instance.toString());
    }

    @Test
    public void testToStringNACK() {
        Packet packet = Packet.create(0, 0, 0x01);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11 from Engine #1 (0): Response is NACK", instance.toString());
    }

    @Test
    public void testToStringUnknown() {
        Packet packet = Packet.create(0, 0, 0x05);
        DM11ClearActiveDTCsPacket instance = new DM11ClearActiveDTCsPacket(packet);
        assertEquals("DM11 from Engine #1 (0): Response is Unknown", instance.toString());
    }
}
