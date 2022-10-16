/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.ScaledTestResult.TestResult.CANNOT_BE_PERFORMED;
import static org.etools.j1939tools.j1939.packets.ScaledTestResult.TestResult.FAILED;
import static org.etools.j1939tools.j1939.packets.ScaledTestResult.TestResult.NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.ScaledTestResult.TestResult.PASSED;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.j1939.packets.ScaledTestResult.TestResult;
import org.junit.Test;

/**
 * Unit tests for the {@link ScaledTestResult} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class ScaledTestResultTest {

    @Test
    public void testCannotBePerformed() {
        int[] data = new int[] { 0xF7, 0xBA, 0x12, 0x1F, 0x20, 0x00, 0x01, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(4794, instance.getSpn());
        assertEquals(31, instance.getFmi());
        assertEquals(32, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(64257, instance.getTestValue());
        assertEquals(65535, instance.getTestMaximum());
        assertEquals(65535, instance.getTestMinimum());
        assertEquals(TestResult.CANNOT_BE_PERFORMED, instance.getTestResult());
        String expected = "SPN 4794 FMI 31 (SLOT 32) Result: Test Cannot Be Performed.";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testFailedHigh() {
        int[] data = new int[] { 0xF7, 0xBC, 0x12, 0x1F, 0x44, 0x00, 0xFF, 0xFA, 0x00, 0xF0, 0x00, 0xA0 };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(4796, instance.getSpn());
        assertEquals(31, instance.getFmi());
        assertEquals(68, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(64255, instance.getTestValue());
        assertEquals(61440, instance.getTestMaximum());
        assertEquals(40960, instance.getTestMinimum());
        assertEquals(TestResult.FAILED, instance.getTestResult());
        String expected = "SPN 4796 FMI 31 (SLOT 68) Result: Test Failed. Min: 1,007, Value: 1,734.969, Max: 1,647 °C";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testReal() {
        int[] data = new int[] { 0xF7, 0xC1, 0x0D, 0x12, 0x00, 0x01, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(0x0dC1, instance.getSpn());
        assertEquals(0x12, instance.getFmi());
        assertEquals(256, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(0xFB00, instance.getTestValue());
        assertEquals(0xFFFF, instance.getTestMaximum());
        assertEquals(0xFFFF, instance.getTestMinimum());
        assertEquals(TestResult.NOT_COMPLETE, instance.getTestResult());
        String expected = "SPN 3521 FMI 18 (SLOT 256) Result: Test Not Complete.";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testFailedLow() {
        int[] data = new int[] { 0xF7, 0xBB, 0x12, 0x0C, 0x8A, 0x01, 0x00, 0x0C, 0xFF, 0xEF, 0xFF, 0xAF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(4795, instance.getSpn());
        assertEquals(12, instance.getFmi());
        assertEquals(394, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(3072, instance.getTestValue());
        assertEquals(61439, instance.getTestMaximum());
        assertEquals(45055, instance.getTestMinimum());
        assertEquals(TestResult.FAILED, instance.getTestResult());
        String expected = "SPN 4795 FMI 12 (SLOT 394) Result: Test Failed. Min: 150.55, Value: -269.28, Max: 314.39";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testFailedNoMax() {
        int[] data = new int[] { 0xF7, 0x15, 0x15, 0x10, 0xCE, 0x00, 0xFF, 0x7F, 0xFF, 0xFF, 0xEE, 0xEE };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5397, instance.getSpn());
        assertEquals(16, instance.getFmi());
        assertEquals(206, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(32767, instance.getTestValue());
        assertEquals(65535, instance.getTestMaximum());
        assertEquals(61166, instance.getTestMinimum());
        assertEquals(TestResult.FAILED, instance.getTestResult());
        String expected = "SPN 5397 FMI 16 (SLOT 206) Result: Test Failed. Min: 3.132, Value: 1.678, Max: N/A s";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testFailedNoMin() {
        int[] data = new int[] { 0xF7, 0x15, 0x15, 0x10, 0xCE, 0x00, 0xEE, 0xEE, 0x80, 0x00, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5397, instance.getSpn());
        assertEquals(16, instance.getFmi());
        assertEquals(206, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(61166, instance.getTestValue());
        assertEquals(128, instance.getTestMaximum());
        assertEquals(65535, instance.getTestMinimum());
        assertEquals(TestResult.FAILED, instance.getTestResult());
        String expected = "SPN 5397 FMI 16 (SLOT 206) Result: Test Failed. Min: N/A, Value: 3.132, Max: 0.007 s";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNotComplete() {
        int[] data = new int[] { 0xF7, 0xA7, 0x13, 0x0A, 0x00, 0x01, 0x00, 0xFB, 0xFF, 0xFF, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5031, instance.getSpn());
        assertEquals(10, instance.getFmi());
        assertEquals(256, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(64256, instance.getTestValue());
        assertEquals(65535, instance.getTestMaximum());
        assertEquals(65535, instance.getTestMinimum());
        assertEquals(TestResult.NOT_COMPLETE, instance.getTestResult());
        String expected = "SPN 5031 FMI 10 (SLOT 256) Result: Test Not Complete.";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassed() {
        ScaledTestResult instance = ScaledTestResult.create(247, 5319, 2, 287, 12288, 20480, 4096);
        assertEquals(5319, instance.getSpn());
        assertEquals(2, instance.getFmi());
        assertEquals(287, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(12288, instance.getTestValue());
        assertEquals(20480, instance.getTestMaximum());
        assertEquals(4096, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5319 FMI 2 (SLOT 287) Result: Test Passed. Min: 327.68, Value: 983.04, Max: 1,638.4 g/L";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedAtMax() {
        int[] data = new int[] { 0xF7, 0x15, 0x15, 0x10, 0xCE, 0x00, 0xFF, 0xFA, 0xFF, 0xFA, 0x00, 0x00 };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5397, instance.getSpn());
        assertEquals(16, instance.getFmi());
        assertEquals(206, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(64255, instance.getTestValue());
        assertEquals(64255, instance.getTestMaximum());
        assertEquals(0, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5397 FMI 16 (SLOT 206) Result: Test Passed. Min: 0, Value: 3.29, Max: 3.29 s";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedAtMin() {
        int[] data = new int[] { 0xF7, 0xA0, 0x13, 0x0B, 0x12, 0x00, 0x30, 0x00, 0x50, 0x00, 0x30, 0x00 };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5024, instance.getSpn());
        assertEquals(11, instance.getFmi());
        assertEquals(18, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(48, instance.getTestValue());
        assertEquals(80, instance.getTestMaximum());
        assertEquals(48, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5024 FMI 11 (SLOT 18) Result: Test Passed. Min: 2.4, Value: 2.4, Max: 4 kg/h";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedNoLimits() {
        int[] data = new int[] { 0xF7, 0xC7, 0x14, 0x02, 0x1F, 0x01, 0x00, 0x30, 0xFF, 0xFF, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5319, instance.getSpn());
        assertEquals(2, instance.getFmi());
        assertEquals(287, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(12288, instance.getTestValue());
        assertEquals(65535, instance.getTestMaximum());
        assertEquals(65535, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5319 FMI 2 (SLOT 287) Result: Test Passed. Min: N/A, Value: 983.04, Max: N/A g/L";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedNoMax() {
        int[] data = new int[] { 0xF7, 0x15, 0x15, 0x10, 0xCE, 0x00, 0xFF, 0x7F, 0xFF, 0xFF, 0x00, 0x00 };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5397, instance.getSpn());
        assertEquals(16, instance.getFmi());
        assertEquals(206, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(32767, instance.getTestValue());
        assertEquals(65535, instance.getTestMaximum());
        assertEquals(0, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5397 FMI 16 (SLOT 206) Result: Test Passed. Min: 0, Value: 1.678, Max: N/A s";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedNoMin() {
        int[] data = new int[] { 0xF7, 0x15, 0x15, 0x10, 0xCE, 0x00, 0xFF, 0x7F, 0xFF, 0xFA, 0xFF, 0xFF };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(5397, instance.getSpn());
        assertEquals(16, instance.getFmi());
        assertEquals(206, instance.getSlot().getId());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(32767, instance.getTestValue());
        assertEquals(64255, instance.getTestMaximum());
        assertEquals(65535, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 5397 FMI 16 (SLOT 206) Result: Test Passed. Min: N/A, Value: 1.678, Max: 3.29 s";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPassedWithNoSlot() {
        int[] data = new int[] { 0xF7, 0xE4, 0x0D, 0x12, 0x00, 0x00, 0xFF, 0xFA, 0xFF, 0xFA, 0x00, 0x00 };
        ScaledTestResult instance = new ScaledTestResult(data);
        assertEquals(3556, instance.getSpn());
        assertEquals(18, instance.getFmi());
        assertEquals(247, instance.getTestIdentifier());
        assertEquals(64255, instance.getTestValue());
        assertEquals(64255, instance.getTestMaximum());
        assertEquals(0, instance.getTestMinimum());
        assertEquals(TestResult.PASSED, instance.getTestResult());
        String expected = "SPN 3556 FMI 18 (SLOT 0) Result: Test Passed. Min: 0, Value: 64,255, Max: 64,255";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testTestResultsToString() {
        assertEquals("Test Passed", PASSED.toString());
        assertEquals("Test Failed", FAILED.toString());
        assertEquals("Test Not Complete", NOT_COMPLETE.toString());
        assertEquals("Test Cannot Be Performed", CANNOT_BE_PERFORMED.toString());
    }

    @Test
    public void testTestResultsValueOf() {
        assertEquals(PASSED, TestResult.valueOf("PASSED"));
        assertEquals(FAILED, TestResult.valueOf("FAILED"));
        assertEquals(NOT_COMPLETE, TestResult.valueOf("NOT_COMPLETE"));
        assertEquals(CANNOT_BE_PERFORMED, TestResult.valueOf("CANNOT_BE_PERFORMED"));
    }

    @Test
    public void testTestResultsValues() {
        assertEquals(4, TestResult.values().length);
    }

}
