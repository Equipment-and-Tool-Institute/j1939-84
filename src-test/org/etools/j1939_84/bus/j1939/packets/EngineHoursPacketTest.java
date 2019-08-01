/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests for the {@link EngineHoursPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class EngineHoursPacketTest {

    @Test
    public void testGetEngineHoursAndToStringAtError() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0xFE, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getEngineHours(), 0.0);
        assertEquals("Engine Hours from Engine #1 (0): error", instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtMax() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFA, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(210554060.75, instance.getEngineHours(), 0.0);
        assertEquals("Engine Hours from Engine #1 (0): 210,554,060.75 hours", instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtNotAvailable() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFF, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getEngineHours(), 0.0);
        assertEquals("Engine Hours from Engine #1 (0): not available", instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtValue() {
        int[] data = new int[] { 0xFE, 0x05, 0x00, 0x00, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(76.7, instance.getEngineHours(), 0.0);
        assertEquals("Engine Hours from Engine #1 (0): 76.7 hours", instance.toString());
    }

    @Test
    public void testGetEngineHoursAtZero() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(0.0, instance.getEngineHours(), 0.0);
    }

    @Test
    public void testPGN() {
        assertEquals(65253, EngineHoursPacket.PGN);
    }

}
