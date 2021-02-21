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
 * Unit tests for the {@link EngineHoursPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class EngineHoursPacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    public void testGetEngineHoursAndToStringAtError() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0xFE, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getEngineHours(), 0.0);
        String expected = "";
        expected += "Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: Error" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;

        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtMax() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFA, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(210554060.75, instance.getEngineHours(), 0.0);
        String expected = "";
        expected += "Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 210554060.750000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtNotAvailable() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFF, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getEngineHours(), 0.0);
        String expected = "";
        expected += "Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: Not Available" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetEngineHoursAndToStringAtValue() {
        int[] data = new int[] { 0xFE, 0x05, 0x00, 0x00, 0x55, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(0, 0, data);
        EngineHoursPacket instance = new EngineHoursPacket(packet);
        assertEquals(76.7, instance.getEngineHours(), 0.0);
        String expected = "";
        expected += "Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 76.700000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;

        assertEquals(expected, instance.toString());
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
