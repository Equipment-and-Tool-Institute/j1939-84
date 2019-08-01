/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests for the {@link EngineSpeedPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class EngineSpeedPacketTest {

    @Test
    public void testGetEngineSpeedAndToStringAt300() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x60, 0x09, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(300, instance.getEngineSpeed(), 0.0);
        assertEquals("Engine Speed from Engine #1 (0): 300 RPM", instance.toString());
    }

    @Test
    public void testGetEngineSpeedAndToStringAtError() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFE, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getEngineSpeed(), 0.0);
        assertEquals("Engine Speed from Engine #1 (0): error", instance.toString());
    }

    @Test
    public void testGetEngineSpeedAndToStringAtNotAvailable() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFF, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getEngineSpeed(), 0.0);
        assertEquals("Engine Speed from Engine #1 (0): not available", instance.toString());
    }

    @Test
    public void testGetEngineSpeedAtMax() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFA, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(8031.875, instance.getEngineSpeed(), 0.0);
    }

    @Test
    public void testGetEngineSpeedAtZero() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x00, 0x00, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(0, instance.getEngineSpeed(), 0.0);
    }

    @Test
    public void testPGN() {
        assertEquals(61444, EngineSpeedPacket.PGN);
    }

}
