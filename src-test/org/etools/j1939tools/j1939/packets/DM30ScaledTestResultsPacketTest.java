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
 * Unit test for the {@link DM30ScaledTestResultsPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM30ScaledTestResultsPacketTest {

    @Test
    public void testPGN() {
        assertEquals(41984, DM30ScaledTestResultsPacket.PGN);
    }

    @Test
    public void testWithOne() {
        ScaledTestResult testResult0 = ScaledTestResult.create(247, 3362, 31, 208, 951, 1000, 800);
        DM30ScaledTestResultsPacket instance = DM30ScaledTestResultsPacket.create(0, 0, testResult0);
        List<ScaledTestResult> testResults = instance.getTestResults();
        assertEquals(1, testResults.size());
        {
            ScaledTestResult testResult = testResults.get(0);
            String expected = "SPN 3362 FMI 31 (SLOT 208) Result: Test Passed. Min: 800, Value: 951, Max: 1,000 count";
            assertEquals(expected, testResult.toString());
        }

        String expected = "DM30 from 0: SPN 3362 FMI 31 (SLOT 208) Result: Test Passed. Min: 800, Value: 951, Max: 1,000 count";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testWithThree() {
        int[] data = new int[] { 0xF7, 0xD4, 0x02, 0x14, 0x3E, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF7, 0xD4,
                0x02, 0x15, 0x3E, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xF7, 0xD4, 0x02, 0x02, 0x84, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(0, 0, data);
        DM30ScaledTestResultsPacket instance = new DM30ScaledTestResultsPacket(packet);
        List<ScaledTestResult> testResults = instance.getTestResults();
        assertEquals(3, testResults.size());
        {
            ScaledTestResult testResult = testResults.get(0);
            String expected = "SPN 724 FMI 20 (SLOT 318) Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92";
            assertEquals(expected, testResult.toString());
        }
        {
            ScaledTestResult testResult = testResults.get(1);
            String expected = "SPN 724 FMI 21 (SLOT 318) Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92";
            assertEquals(expected, testResult.toString());
        }
        {
            ScaledTestResult testResult = testResults.get(2);
            String expected = "SPN 724 FMI 2 (SLOT 132) Result: Test Passed. Min: 0, Value: 0, Max: 0 ms";
            assertEquals(expected, testResult.toString());
        }

        String expected = "DM30 from 0: [" + NL
                + "  SPN 724 FMI 2 (SLOT 132) Result: Test Passed. Min: 0, Value: 0, Max: 0 ms" + NL
                + "  SPN 724 FMI 20 (SLOT 318) Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92" + NL
                + "  SPN 724 FMI 21 (SLOT 318) Result: Test Passed. Min: -3.92, Value: -3.92, Max: -3.92" + NL
                + "]";
        assertEquals(expected, instance.toString());
    }

}
