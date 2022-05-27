/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the {@link SupportedSPN} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class SupportedSPNTest {

    @Test
    public void testEquals() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        SupportedSPN instance2 = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        SupportedSPN instance3 = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
        assertFalse(instance.equals(new Object()));
        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(instance2));
        assertFalse(instance.equals(instance3));
    }

    @Test
    public void testError() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
        assertEquals(524030, instance.getSpn());
        assertEquals((byte) 254, instance.getLength());
        assertEquals(false, instance.supportsDataStream());
        assertEquals(true, instance.supportsExpandedFreezeFrame());
        assertEquals(false, instance.supportsScaledTestResults());
        String expected = "SPN 524030 - Manufacturer Assignable SPN";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testHashCode() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        SupportedSPN instance2 = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        assertTrue(instance.hashCode() == instance2.hashCode());
    }

    @Test
    public void testMax() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        assertEquals(524287, instance.getSpn());
        assertEquals((byte) 250, instance.getLength());
        assertEquals(false, instance.supportsDataStream());
        assertEquals(false, instance.supportsExpandedFreezeFrame());
        assertEquals(false, instance.supportsScaledTestResults());
        String expected = "SPN 524287 - Manufacturer Assignable SPN (last entry)";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testMin() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
        assertEquals(0, instance.getSpn());
        assertEquals(0, instance.getLength());
        assertEquals(true, instance.supportsDataStream());
        assertEquals(true, instance.supportsExpandedFreezeFrame());
        assertEquals(true, instance.supportsScaledTestResults());
        String expected = "SPN 0 - Unknown";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNotAvailable() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0xFF, 0xFF, 0xFF, 0xFF });
        assertEquals(524287, instance.getSpn());
        assertEquals((byte) 255, instance.getLength());
        assertEquals(false, instance.supportsDataStream());
        assertEquals(false, instance.supportsExpandedFreezeFrame());
        assertEquals(false, instance.supportsScaledTestResults());
        String expected = "SPN 524287 - Manufacturer Assignable SPN (last entry)";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testParseSPN() {
        int[] data = new int[] { 0x61, 0x02, 0x13, 0x81 };
        int actual = SupportedSPN.parseSPN(data);
        assertEquals(609, actual);
    }

    @Test
    public void testSupportsDataStream() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        assertEquals(513, instance.getSpn());
        assertEquals(8, instance.getLength());
        assertEquals(true, instance.supportsDataStream());
        assertEquals(false, instance.supportsExpandedFreezeFrame());
        assertEquals(false, instance.supportsScaledTestResults());
        String expected = "SPN 513 - Actual Engine - Percent Torque";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testSupportsFreezeFrame() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x03, 0x04, 0x1E, 1 });
        assertEquals(1027, instance.getSpn());
        assertEquals(1, instance.getLength());
        assertEquals(false, instance.supportsDataStream());
        assertEquals(true, instance.supportsExpandedFreezeFrame());
        assertEquals(false, instance.supportsScaledTestResults());
        String expected = "SPN 1027 - Trip Time in Derate by Engine";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testSupportsScaledTestsResults() {
        SupportedSPN instance = new SupportedSPN(new int[] { 0x05, 0x06, 0x1B, 16 });
        assertEquals(1541, instance.getSpn());
        assertEquals(16, instance.getLength());
        assertEquals(false, instance.supportsDataStream());
        assertEquals(false, instance.supportsExpandedFreezeFrame());
        assertEquals(true, instance.supportsScaledTestResults());
        String expected = "SPN 1541 - Reel Speed";
        assertEquals(expected, instance.toString());
    }

    // FIXME: add tests for validateDataStreamSpns & validateFreezeFrameSpns
    // will need this stuff:

    // verify(vehicleInformationModule).setJ1939(j1939tools);
    // {
    // SupportedSPN spn = expectedSPNs1.get(0);
    // assertEquals(92, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs1.get(1);
    // assertEquals(512, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs1.get(2);
    // assertEquals(513, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // assertEquals("Packet 1 SPNs size ", 3,
    // packet1.getSupportedSpns().size());
    //
    // {
    // SupportedSPN spn = expectedSPNs4.get(0);
    // assertEquals(92, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(1);
    // assertEquals(512, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(2);
    // assertEquals(513, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(3);
    // assertEquals(544, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(4);
    // assertEquals(539, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(5);
    // assertEquals(540, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(6);
    // assertEquals(541, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(7);
    // assertEquals(542, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(8);
    // assertEquals(543, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(9);
    // assertEquals(110, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(10);
    // assertEquals(175, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(11);
    // assertEquals(190, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(12);
    // assertEquals(84, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(13);
    // assertEquals(108, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(14);
    // assertEquals(158, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(15);
    // assertEquals(51, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(16);
    // assertEquals(94, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(17);
    // assertEquals(172, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(18);
    // assertEquals(105, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(19);
    // assertEquals(132, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(20);
    // assertEquals(976, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(21);
    // assertEquals(91, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(22);
    // assertEquals(183, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(23);
    // assertEquals(102, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(24);
    // assertEquals(173, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(25);
    // assertEquals(3251, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(26);
    // assertEquals(3483, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(27);
    // assertEquals(5837, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(28);
    // assertEquals(3301, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(29);
    // assertEquals(5466, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(30);
    // assertEquals(5323, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(31);
    // assertEquals(3464, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(32);
    // assertEquals(1209, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(33);
    // assertEquals(5541, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(34);
    // assertEquals(164, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(35);
    // assertEquals(2791, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(36);
    // assertEquals(1413, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(37);
    // assertEquals(1414, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(38);
    // assertEquals(1415, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(39);
    // assertEquals(1416, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(40);
    // assertEquals(1417, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(41);
    // assertEquals(1418, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(42);
    // assertEquals(3563, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(43);
    // assertEquals(27, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(44);
    // assertEquals(3242, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(45);
    // assertEquals(3246, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(46);
    // assertEquals(3216, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(47);
    // assertEquals(1081, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(48);
    // assertEquals(1189, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(49);
    // assertEquals(5314, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(50);
    // assertEquals(3609, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(51);
    // assertEquals(3480, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(52);
    // assertEquals(3226, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(53);
    // assertEquals(1761, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(54);
    // assertEquals(3482, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(55);
    // assertEquals(3490, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(56);
    // assertEquals(4360, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(57);
    // assertEquals(4363, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(58);
    // assertEquals(3031, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(59);
    // assertEquals(4421, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(60);
    // assertEquals(1173, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(true, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(61);
    // assertEquals(237, spn.getSpn());
    // assertEquals(17, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(62);
    // assertEquals(4257, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(63);
    // assertEquals(2304, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(64);
    // assertEquals(899, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(65);
    // assertEquals(2305, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(66);
    // assertEquals(3069, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(67);
    // assertEquals(3294, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(68);
    // assertEquals(3295, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(69);
    // assertEquals(3296, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(70);
    // assertEquals(3302, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(71);
    // assertEquals(3719, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(72);
    // assertEquals(1323, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(73);
    // assertEquals(1324, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(74);
    // assertEquals(1325, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(75);
    // assertEquals(1326, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(76);
    // assertEquals(1327, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(77);
    // assertEquals(1328, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(78);
    // assertEquals(2630, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(79);
    // assertEquals(2659, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(80);
    // assertEquals(1322, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(81);
    // assertEquals(4752, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(82);
    // assertEquals(4766, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(83);
    // assertEquals(5319, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(84);
    // assertEquals(651, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(85);
    // assertEquals(652, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(86);
    // assertEquals(653, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(87);
    // assertEquals(654, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(88);
    // assertEquals(655, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(89);
    // assertEquals(656, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(90);
    // assertEquals(3471, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(91);
    // assertEquals(3556, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(true, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(92);
    // assertEquals(2307, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(93);
    // assertEquals(4106, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(94);
    // assertEquals(1216, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(95);
    // assertEquals(1220, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(96);
    // assertEquals(4127, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(97);
    // assertEquals(4130, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(98);
    // assertEquals(171, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(99);
    // assertEquals(157, spn.getSpn());
    // assertEquals(2, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(100);
    // assertEquals(2623, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(101);
    // assertEquals(247, spn.getSpn());
    // assertEquals(4, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(102);
    // assertEquals(235, spn.getSpn());
    // assertEquals(4, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(103);
    // assertEquals(1213, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(104);
    // assertEquals(3303, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(105);
    // assertEquals(3304, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(106);
    // assertEquals(5454, spn.getSpn());
    // assertEquals(4, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(107);
    // assertEquals(588, spn.getSpn());
    // assertEquals(17, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(108);
    // assertEquals(3055, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(false, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(109);
    // assertEquals(5463, spn.getSpn());
    // assertEquals(1, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // {
    // SupportedSPN spn = expectedSPNs4.get(110);
    // assertEquals(248, spn.getSpn());
    // assertEquals(4, spn.getLength());
    // assertEquals(true, spn.supportsDataStream());
    // assertEquals(false, spn.supportsExpandedFreezeFrame());
    // assertEquals(false, spn.supportsScaledTestResults());
    // }
    // assertEquals("Packet 4 SPNs size ", 111,
    // packet4.getSupportedSpns().size());
}
