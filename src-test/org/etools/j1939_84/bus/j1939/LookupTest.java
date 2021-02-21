/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit Tests the {@link Lookup} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class LookupTest {

    @Test
    public void testGetAddressName() {
        assertEquals("Engine #1 (0)", Lookup.getAddressName(0));
        assertEquals("Global (255)", Lookup.getAddressName(255));
        assertEquals("DPF Controller (85)", Lookup.getAddressName(85));
        assertEquals("Unknown (-1)", Lookup.getAddressName(-1));
    }

    @Test
    public void testGetFmiDescription() {
        assertEquals("Data Valid But Above Normal Operational Range - Most Severe Level", Lookup.getFmiDescription(0));
        assertEquals("Data Valid But Below Normal Operational Range - Most Severe Level", Lookup.getFmiDescription(1));
        assertEquals("Data Erratic, Intermittent Or Incorrect", Lookup.getFmiDescription(2));
        assertEquals("Voltage Above Normal, Or Shorted To High Source", Lookup.getFmiDescription(3));
        assertEquals("Voltage Below Normal, Or Shorted To Low Source", Lookup.getFmiDescription(4));
        assertEquals("Current Below Normal Or Open Circuit", Lookup.getFmiDescription(5));
        assertEquals("Current Above Normal Or Grounded Circuit", Lookup.getFmiDescription(6));
        assertEquals("Mechanical System Not Responding Or Out Of Adjustment", Lookup.getFmiDescription(7));
        assertEquals("Abnormal Frequency Or Pulse Width Or Period", Lookup.getFmiDescription(8));
        assertEquals("Abnormal Update Rate", Lookup.getFmiDescription(9));
        assertEquals("Abnormal Rate Of Change", Lookup.getFmiDescription(10));
        assertEquals("Root Cause Not Known", Lookup.getFmiDescription(11));
        assertEquals("Bad Intelligent Device Or Component", Lookup.getFmiDescription(12));
        assertEquals("Out Of Calibration", Lookup.getFmiDescription(13));
        assertEquals("Special Instructions", Lookup.getFmiDescription(14));
        assertEquals("Data Valid But Above Normal Operating Range - Least Severe Level", Lookup.getFmiDescription(15));
        assertEquals("Data Valid But Above Normal Operating Range - Moderately Severe Level",
                     Lookup.getFmiDescription(16));
        assertEquals("Data Valid But Below Normal Operating Range - Least Severe Level", Lookup.getFmiDescription(17));
        assertEquals("Data Valid But Below Normal Operating Range - Moderately Severe Level",
                     Lookup.getFmiDescription(18));
        assertEquals("Received Network Data In Error", Lookup.getFmiDescription(19));
        assertEquals("Data Drifted High", Lookup.getFmiDescription(20));
        assertEquals("Data Drifted Low", Lookup.getFmiDescription(21));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(22));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(23));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(24));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(25));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(26));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(27));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(28));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(29));
        assertEquals("Reserved For SAE Assignment", Lookup.getFmiDescription(30));
        assertEquals("Condition Exists", Lookup.getFmiDescription(31));
        assertEquals("Unknown", Lookup.getFmiDescription(-1));
    }

    @Test
    public void testGetManufacturer() {
        assertEquals("Reserved", Lookup.getManufacturer(0));
        assertEquals("Cummins Inc", Lookup.getManufacturer(10));
        assertEquals("Equipment & Tool Institute", Lookup.getManufacturer(2047));
        assertEquals("Unknown", Lookup.getManufacturer(-1));
    }

    @Test
    public void testGetPartName() {
        assertEquals("Part 1 KOEO Data Collection", Lookup.getPartName(1));
        assertEquals("Part 2 Key On Engine Running Data Collection", Lookup.getPartName(2));
        assertEquals("Part 3 Test Pending Fault A", Lookup.getPartName(3));
        assertEquals("Part 4 Test Confirmed Fault A", Lookup.getPartName(4));
        assertEquals("Part 5 Correct fault A first cycle", Lookup.getPartName(5));
        assertEquals("Part 6 Complete fault A three cycle countdown", Lookup.getPartName(6));
        assertEquals("Part 7 Verify DM23 transition", Lookup.getPartName(7));
        assertEquals("Part 8 Verify fault B for general denominator demonstration", Lookup.getPartName(8));
        assertEquals("Part 9 verify deletion of fault B with DM11", Lookup.getPartName(9));
        assertEquals("Part 10 Prime diagnostic executive for general denominator demonstration",
                     Lookup.getPartName(10));
        assertEquals("Part 11 Exercise general denominator", Lookup.getPartName(11));
        assertEquals("Part 12 Verify deletion of fault B from DM28", Lookup.getPartName(12));
        assertEquals("Unknown", Lookup.getPartName(13));
    }

    @Test
    public void testGetSpnName() {
        assertEquals("Engine Fuel Pressure (Extended Range)", Lookup.getSpnName(18));
        assertEquals("Engine Fuel Supply Pump Actuator", Lookup.getSpnName(931));
        assertEquals("Manufacturer Assignable SPN (last entry)", Lookup.getSpnName(524287));
        assertEquals("Unknown", Lookup.getSpnName(-1));
    }

    @Test
    public void testGetStepName() {
        assertEquals("Unknown", Lookup.getStepName(13, 1));
        assertEquals("Unknown", Lookup.getStepName(1, 0));
        assertEquals("Unknown", Lookup.getStepName(-1, 1));
        assertEquals("Test vehicle data collection", Lookup.getStepName(1, 1));
        assertEquals("DM5: Diagnostic readiness 1", Lookup.getStepName(11, 10));
        assertEquals("DM7/DM30: Command non-continuously monitored test/scaled test results",
                     Lookup.getStepName(12, 10));
    }

    @Test
    public void testGetOutcomeForDuplicateSpn() {
        assertEquals(PASS, Lookup.getOutcomeForDuplicateSpn(123)); // Unknown
        assertEquals(WARN, Lookup.getOutcomeForDuplicateSpn(84)); // WARN
        assertEquals(PASS, Lookup.getOutcomeForDuplicateSpn(2848)); // PASS
        assertEquals(FAIL, Lookup.getOutcomeForDuplicateSpn(102)); // FAIL
    }

    @Test
    public void testGetOutcomeForNonObdModuleProvidingSpn() {
        assertEquals(PASS, Lookup.getOutcomeForNonObdModuleProvidingSpn(123)); // Unknown
        assertEquals(WARN, Lookup.getOutcomeForNonObdModuleProvidingSpn(3226)); // WARN
        assertEquals(PASS, Lookup.getOutcomeForNonObdModuleProvidingSpn(168)); // PASS
        assertEquals(FAIL, Lookup.getOutcomeForNonObdModuleProvidingSpn(183)); // FAIL

    }
}
