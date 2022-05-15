/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests for the {@link DM20MonitorPerformanceRatioPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM20MonitorPerformanceRatioPacketTest {

    @Test
    public void testOne() {
        int[] data = new int[] { 0xA5, 0xA5, 0x5A, 0x5A, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0xFF, 0xFF };
        Packet packet = Packet.create(0, 0, data);
        DM20MonitorPerformanceRatioPacket instance = new DM20MonitorPerformanceRatioPacket(packet);
        assertEquals(42405, instance.getIgnitionCycles());
        assertEquals(23130, instance.getOBDConditionsCount());
        List<PerformanceRatio> ratios = instance.getRatios();
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
        expected += "                                                        Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                               42,405" + NL;
        expected += "  OBD Monitoring Conditions Encountered                         23,130" + NL;
        expected += "  SPN 524287 Manufacturer Assignable SPN (last entry)  65,279 / 65,535" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPGN() {
        assertEquals(49664, DM20MonitorPerformanceRatioPacket.PGN);
    }

    @Test
    public void testThree() {
        var instance = DM20MonitorPerformanceRatioPacket.create(0,
                                                                12,
                                                                1,
                                                                new PerformanceRatio(5322, 0, 1, 0),
                                                                new PerformanceRatio(4792, 3, 4, 0),
                                                                new PerformanceRatio(5308, 5, 6, 0));

        assertEquals(12, instance.getIgnitionCycles());
        assertEquals(1, instance.getOBDConditionsCount());
        List<PerformanceRatio> ratios = instance.getRatios();
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
        expected += "                                                         Num'r /  Den'r" + NL;
        expected += "  Ignition Cycles                                                    12" + NL;
        expected += "  OBD Monitoring Conditions Encountered                               1" + NL;
        expected += "  SPN 5322 AFT NMHC Converting Catalyst System Monitor       0 /      1" + NL;
        expected += "  SPN 4792 AFT 1 SCR System                                  3 /      4" + NL;
        expected += "  SPN 5308 AFT 1 NOx Adsorber Catalyst System Monitor        5 /      6" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());
    }
}
