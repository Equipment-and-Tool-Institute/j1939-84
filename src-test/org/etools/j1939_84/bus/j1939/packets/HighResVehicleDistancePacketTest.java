/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link HighResVehicleDistancePacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class HighResVehicleDistancePacketTest {

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
        int[] data = new int[] { 0x80, 0x14, 0x06, 0x00, 0x11, 0x22, 0x33, 0x44 };
        Packet packet = Packet.create(0, 0, data);
        HighResVehicleDistancePacket instance = new HighResVehicleDistancePacket(packet);
        assertEquals(1992.32, instance.getTotalVehicleDistance(), 0.0);
        String expected = "";
        expected += "10:15:30.0000 18000000 80 14 06 00 11 22 33 44" + NL;
        expected += "High Resolution Vehicle Distance from Engine #1 (0): " + NL;
        expected += "  SPN   917, Total Vehicle Distance (High Resolution): 1992320.000000 m" + NL;
        expected += "  SPN   918, Trip Distance (High Resolution): 5721008725.000000 m" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtError() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0xFE, 0x11, 0x22, 0x33, 0x44 };
        Packet packet = Packet.create(0, 0, data);
        HighResVehicleDistancePacket instance = new HighResVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getTotalVehicleDistance(), 0.0);
        String expected = "";
        expected += "10:15:30.0000 18000000 00 00 00 FE 11 22 33 44" + NL;
        expected += "High Resolution Vehicle Distance from Engine #1 (0): " + NL;
        expected += "  SPN   917, Total Vehicle Distance (High Resolution): Error" + NL;
        expected += "  SPN   918, Trip Distance (High Resolution): 5721008725.000000 m" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetTotalVehicleDistanceAndToStringAtNotAvailable() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFF, 0x11, 0x22, 0x33, 0x44 };
        Packet packet = Packet.create(0, 0, data);
        HighResVehicleDistancePacket instance = new HighResVehicleDistancePacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getTotalVehicleDistance(), 0.0);

        String expected = "";
        expected += "10:15:30.0000 18000000 FF FF FF FF 11 22 33 44"+NL;
        expected += "High Resolution Vehicle Distance from Engine #1 (0): "+NL;
        expected += "  SPN   917, Total Vehicle Distance (High Resolution): Not Available"+NL;
        expected += "  SPN   918, Trip Distance (High Resolution): 5721008725.000000 m"+NL;
        assertEquals(expected, instance.toString());

    }

    @Test
    public void testGetTotalVehicleDistanceAtMax() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFA, 0x11, 0x22, 0x33, 0x44 };
        Packet packet = Packet.create(0, 0, data);
        HighResVehicleDistancePacket instance = new HighResVehicleDistancePacket(packet);
        assertEquals(21055406.075, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testGetTotalVehicleDistanceAtZero() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00, 0x11, 0x22, 0x33, 0x44 };
        Packet packet = Packet.create(0, 0, data);
        HighResVehicleDistancePacket instance = new HighResVehicleDistancePacket(packet);
        assertEquals(0, instance.getTotalVehicleDistance(), 0.0);
    }

    @Test
    public void testPGN() {
        assertEquals(65217, HighResVehicleDistancePacket.PGN);
    }

}
