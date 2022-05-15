/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.TotalVehicleDistancePacket.PGN;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link TotalVehicleDistancePacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class TotalVehicleDistancePacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    public void testGetTotalVehicleDistanceAndToString() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x80, 0x14, 0x06, 0x00 };
        Packet packet = Packet.create(PGN, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(49808.0, instance.getTotalVehicleDistance(), 0.0);

        String expected = "";
        expected += "Total Vehicle Distance from Engine #1 (0): " + NL;
        expected += "  SPN   244, Trip Distance: 143025218.125 km" + NL;
        expected += "  SPN   245, Total Vehicle Distance: 49808.000 km" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtError() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x00, 0x00, 0x00, 0xFE };
        Packet packet = Packet.create(PGN, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getTotalVehicleDistance(), 0.0);

        String expected = "";
        expected += "Total Vehicle Distance from Engine #1 (0): " + NL;
        expected += "  SPN   244, Trip Distance: 143025218.125 km" + NL;
        expected += "  SPN   245, Total Vehicle Distance: Error" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtNotAvailable() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0xFF, 0xFF, 0xFF, 0xFF };
        Packet packet = Packet.create(PGN, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getTotalVehicleDistance(), 0.0);

        String expected = "";
        expected += "Total Vehicle Distance from Engine #1 (0): " + NL;
        expected += "  SPN   244, Trip Distance: 143025218.125 km" + NL;
        expected += "  SPN   245, Total Vehicle Distance: Not Available" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAtMax() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0xFF, 0xFF, 0xFF, 0xFA };
        Packet packet = Packet.create(PGN, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(526385151.875, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testGetTotalVehicleDistanceAtZero() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x44, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(PGN, 0, data);
        TotalVehicleDistancePacket instance = new TotalVehicleDistancePacket(packet);
        assertEquals(0, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testPGN() {
        assertEquals(65248, TotalVehicleDistancePacket.PGN);
    }

}
