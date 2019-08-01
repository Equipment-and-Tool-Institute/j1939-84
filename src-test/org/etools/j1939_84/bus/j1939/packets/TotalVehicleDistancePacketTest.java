/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests for the {@link TotalVehicleDistancePacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class TotalVehicleDistancePacketTest {

    @Test
    public void testGetTotalVehicleDistanceAndToString() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x80, 0x14, 0x06, 0x00 };
        Packet packet = Packet.create(0, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(49808.0, instance.getTotalVehicleDistance(), 0.0);
        assertEquals("Total Vehicle Distance from Engine #1 (0): 49,808 km (30,949.256 mi)", instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtError() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x00, 0x00, 0x00, 0xFE };
        Packet packet = Packet.create(0, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getTotalVehicleDistance(), 0.0);
        assertEquals("Total Vehicle Distance from Engine #1 (0): error", instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtNotAvailable() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0xFF, 0xFF, 0xFF, 0xFF };
        Packet packet = Packet.create(0, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getTotalVehicleDistance(), 0.0);
        assertEquals("Total Vehicle Distance from Engine #1 (0): not available", instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAtMax() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0xFF, 0xFF, 0xFF, 0xFA };
        Packet packet = Packet.create(0, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(526385151.875, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testGetTotalVehicleDistanceAtZero() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(0, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(0, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testPGN() {
        assertEquals(65248, TotalVehicleDistancePacket.PGN);
    }

}
