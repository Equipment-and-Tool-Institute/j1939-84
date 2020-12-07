/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests for the {@link DM20MonitorPerformanceRatioPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM20MonitorPerformanceRatioPacketTest {

    @Test
    public void testOne() {
        int[] data = new int[] { 0xA5, 0xA5, 0x5A, 0x5A, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0xFF, 0xFF };
        Packet packet = Packet.create(0, 0, data);
        DM20MonitorPerformanceRatioPacket instance = new DM20MonitorPerformanceRatioPacket(packet);
        assertEquals(42405, instance.getIgnitionCycles());
        assertEquals(23130, instance.getOBDConditionsCount());
        final List<PerformanceRatio> ratios = instance.getRatios();
        assertEquals(1, ratios.size());
        {
            PerformanceRatio ratio = ratios.get(0);
            assertEquals(524287, ratio.getSpn());
            assertEquals(65279, ratio.getNumerator());
            assertEquals(65535, ratio.getDenominator());
            assertEquals("Engine #1 (0)", ratio.getSource());
        }

        String expected = "";
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                      Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                               42,405" + NL;
        expected += "OBD Monitoring Conditions Encountered                         23,130" + NL;
        expected += "SPN 524287 Manufacturer Assignable SPN (last entry)  65,279 / 65,535" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPGN() {
        assertEquals(49664, DM20MonitorPerformanceRatioPacket.PGN);
    }

    @Test
    public void testThree() {
        int[] data = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x01, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00,
                // Three
                0xBC, 0x14, 0xF8, 0x05, 0x00, 0x06, 0x00 };
        Packet packet = Packet.create(0, 0, data);
        DM20MonitorPerformanceRatioPacket instance = new DM20MonitorPerformanceRatioPacket(packet);
        assertEquals(12, instance.getIgnitionCycles());
        assertEquals(1, instance.getOBDConditionsCount());
        final List<PerformanceRatio> ratios = instance.getRatios();
        assertEquals(3, ratios.size());
        {
            PerformanceRatio ratio = ratios.get(0);
            assertEquals(5322, ratio.getSpn());
            assertEquals(0, ratio.getNumerator());
            assertEquals(1, ratio.getDenominator());
            assertEquals("Engine #1 (0)", ratio.getSource());
        }
        {
            PerformanceRatio ratio = ratios.get(1);
            assertEquals(4792, ratio.getSpn());
            assertEquals(3, ratio.getNumerator());
            assertEquals(4, ratio.getDenominator());
            assertEquals("Engine #1 (0)", ratio.getSource());
        }
        {
            PerformanceRatio ratio = ratios.get(2);
            assertEquals(5308, ratio.getSpn());
            assertEquals(5, ratio.getNumerator());
            assertEquals(6, ratio.getDenominator());
            assertEquals("Engine #1 (0)", ratio.getSource());
        }

        String expected = "";
        expected += "DM20 from Engine #1 (0):  [" + NL;
        expected += "                                                                  Num'r /  Den'r" + NL;
        expected += "Ignition Cycles                                                               12" + NL;
        expected += "OBD Monitoring Conditions Encountered                                          1" + NL;
        expected += "SPN 5322 Aftertreatment NMHC Converting Catalyst System Monitor       0 /      1" + NL;
        expected += "SPN 4792 Aftertreatment 1 Selective Catalytic Reduction System        3 /      4" + NL;
        expected += "SPN 5308 Aftertreatment 1 NOx Adsorber Catalyst System Monitor        5 /      6" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());
    }
}
