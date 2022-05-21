/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.EngineSpeedPacket.PGN;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.etools.testdoc.TestDoc;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link EngineSpeedPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@TestDoc(description = "Verify the correct interpretation of PGN 61444 as engine speed.")
public class EngineSpeedPacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    @TestDoc(description = "Verify that data 0x11, 0x22, 0x33, 0x60, 0x09, 0x66, 0x77, 0x88 is interpreted as 300 RPM.")
    public void testGetEngineSpeedAndToStringAt300() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x60, 0x09, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(PGN, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(300, instance.getEngineSpeed(), 0.0);
        String expected = "";
        expected += "Engine Speed from Engine #1 (0): " + NL;
        expected += "  SPN   899, Engine Torque Mode: 0001" + NL;
        expected += "  SPN  4154, Actual Engine - Percent Torque (Fractional): 0.125 %" + NL;
        expected += "  SPN   512, Driver's Demand Engine - Percent Torque: -91.000 %" + NL;
        expected += "  SPN   513, Actual Engine - Percent Torque: -74.000 %" + NL;
        expected += "  SPN   190, Engine Speed: 300.000 rpm" + NL;
        expected += "  SPN  1483, Source Address of Controlling Device for Engine Control: 102.000 source address"
                + NL;
        expected += "  SPN  1675, Engine Starter Mode: 0111" + NL;
        expected += "  SPN  2432, Engine Demand - Percent Torque: 11.000 %" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    @TestDoc(description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFE, 0x66, 0x77, 0x88 is interpreted as an error.")
    public void testGetEngineSpeedAndToStringAtError() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFE, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(PGN, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getEngineSpeed(), 0.0);
        String expected = "";
        expected += "Engine Speed from Engine #1 (0): " + NL;
        expected += "  SPN   899, Engine Torque Mode: 0001" + NL;
        expected += "  SPN  4154, Actual Engine - Percent Torque (Fractional): 0.125 %" + NL;
        expected += "  SPN   512, Driver's Demand Engine - Percent Torque: -91.000 %" + NL;
        expected += "  SPN   513, Actual Engine - Percent Torque: -74.000 %" + NL;
        expected += "  SPN   190, Engine Speed: Error" + NL;
        expected += "  SPN  1483, Source Address of Controlling Device for Engine Control: 102.000 source address"
                + NL;
        expected += "  SPN  1675, Engine Starter Mode: 0111" + NL;
        expected += "  SPN  2432, Engine Demand - Percent Torque: 11.000 %" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    @TestDoc(description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFF, 0x66, 0x77, 0x88 is interpreted as not available.")
    public void testGetEngineSpeedAndToStringAtNotAvailable() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFF, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(PGN, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getEngineSpeed(), 0.0);
        String expected = "";
        expected += "Engine Speed from Engine #1 (0): " + NL;
        expected += "  SPN   899, Engine Torque Mode: 0001" + NL;
        expected += "  SPN  4154, Actual Engine - Percent Torque (Fractional): 0.125 %" + NL;
        expected += "  SPN   512, Driver's Demand Engine - Percent Torque: -91.000 %" + NL;
        expected += "  SPN   513, Actual Engine - Percent Torque: -74.000 %" + NL;
        expected += "  SPN   190, Engine Speed: Not Available" + NL;
        expected += "  SPN  1483, Source Address of Controlling Device for Engine Control: 102.000 source address"
                + NL;
        expected += "  SPN  1675, Engine Starter Mode: 0111" + NL;
        expected += "  SPN  2432, Engine Demand - Percent Torque: 11.000 %" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    @TestDoc(description = "Verify that data 0x11, 0x22, 0x33, 0xFF, 0xFA, 0x66, 0x77, 0x88 is interpreted as 8031.875.")
    public void testGetEngineSpeedAtMax() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0xFF, 0xFA, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(PGN, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(8031.875, instance.getEngineSpeed(), 0.0);
    }

    @Test
    @TestDoc(description = "Verify that data 0x11, 0x22, 0x33, 0x00, 0x00, 0x66, 0x77, 0x88 is interpreted as 0.")
    public void testGetEngineSpeedAtZero() {
        int[] data = new int[] { 0x11, 0x22, 0x33, 0x00, 0x00, 0x66, 0x77, 0x88 };
        Packet packet = Packet.create(PGN, 0, data);
        EngineSpeedPacket instance = new EngineSpeedPacket(packet);
        assertEquals(0, instance.getEngineSpeed(), 0.0);
    }

    @Test
    @TestDoc(description = "Verify that the PGN is 61444.")
    public void testPGN() {
        assertEquals(61444, PGN);
    }

}
