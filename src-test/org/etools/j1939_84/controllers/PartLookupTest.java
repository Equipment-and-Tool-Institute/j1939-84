/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit Tests the {@link PartLookup} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class PartLookupTest {

    @Test
    public void testGetPartName() {
        assertEquals("Part 1 - KOEO Data Collection", PartLookup.getPartName(1));
        assertEquals("Part 2 - Key On Engine Running Data Collection", PartLookup.getPartName(2));
        assertEquals("Part 3 - Test Pending Fault A", PartLookup.getPartName(3));
        assertEquals("Part 4 - Test Confirmed Fault A", PartLookup.getPartName(4));
        assertEquals("Part 5 - Correct fault A first cycle", PartLookup.getPartName(5));
        assertEquals("Part 6 - Complete fault A three cycle countdown", PartLookup.getPartName(6));
        assertEquals("Part 7 - Verify DM23 transition", PartLookup.getPartName(7));
        assertEquals("Part 8 - Verify fault B for general denominator demonstration", PartLookup.getPartName(8));
        assertEquals("Part 9 - Verify deletion of fault B with DM11", PartLookup.getPartName(9));
        assertEquals("Part 10 - Prime diagnostic executive for general denominator demonstration",
                     PartLookup.getPartName(10));
        assertEquals("Part 11 - Exercise general denominator", PartLookup.getPartName(11));
        assertEquals("Part 12 - Verify deletion of fault B from DM28", PartLookup.getPartName(12));
        assertEquals("Unknown", PartLookup.getPartName(13));
    }

    @Test
    public void testGetStepName() {
        assertEquals("Unknown", PartLookup.getStepName(13, 1));
        assertEquals("Unknown", PartLookup.getStepName(1, 0));
        assertEquals("Unknown", PartLookup.getStepName(-1, 1));
        assertEquals("Test vehicle data collection", PartLookup.getStepName(1, 1));
        assertEquals("DM5: Diagnostic readiness 1", PartLookup.getStepName(11, 10));
        assertEquals("DM7/DM30: Command Non-Continuously Monitored Test/Scaled Test Results",
                     PartLookup.getStepName(12, 11));
    }

}
