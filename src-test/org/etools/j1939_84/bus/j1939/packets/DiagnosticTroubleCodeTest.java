/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests the {@link DiagnosticTroubleCode} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DiagnosticTroubleCodeTest {

    @Test
    public void test1() {
        int[] data = new int[] { 0x61, 0x02, 0x13, 0x81 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(1, instance.getConversionMethod());
        assertEquals(19, instance.getFailureModeIndicator());
        assertEquals(609, instance.getSuspectParameterNumber());
        assertEquals(1, instance.getOccurrenceCount());
    }

    @Test
    public void test2() {
        int[] data = new int[] { 0x21, 0x06, 0x1F, 0x23 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(31, instance.getFailureModeIndicator());
        assertEquals(1569, instance.getSuspectParameterNumber());
        assertEquals(35, instance.getOccurrenceCount());
    }

    @Test
    public void test3() {
        int[] data = new int[] { 0xEE, 0x10, 0x04, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(4, instance.getFailureModeIndicator());
        assertEquals(4334, instance.getSuspectParameterNumber());
        assertEquals(0, instance.getOccurrenceCount());
    }

    @Test
    public void testFMI() {
        int[] data = new int[] { 0x00, 0x00, 0x1F, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(31, instance.getFailureModeIndicator());
        assertEquals(0, instance.getSuspectParameterNumber());
        assertEquals(0, instance.getOccurrenceCount());
    }

    @Test
    public void testGetConversionMethod() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x80 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(1, instance.getConversionMethod());
        assertEquals(0, instance.getFailureModeIndicator());
        assertEquals(0, instance.getSuspectParameterNumber());
        assertEquals(0, instance.getOccurrenceCount());
    }

    @Test
    public void testGetOccurranceCount() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x7F };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(0, instance.getFailureModeIndicator());
        assertEquals(0, instance.getSuspectParameterNumber());
        assertEquals(127, instance.getOccurrenceCount());
    }

    @Test
    public void testGetSuspectParameterNumber() {
        int[] data = new int[] { 0xFF, 0xFF, 0xE0, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(0, instance.getFailureModeIndicator());
        assertEquals(524287, instance.getSuspectParameterNumber());
        assertEquals(0, instance.getOccurrenceCount());
    }

    @Test
    public void testNoDTC() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals(0, instance.getConversionMethod());
        assertEquals(0, instance.getFailureModeIndicator());
        assertEquals(0, instance.getSuspectParameterNumber());
        assertEquals(0, instance.getOccurrenceCount());
    }

    @Test
    public void testToString() {
        int[] data = new int[] { 0x21, 0x06, 0x1F, 0x23 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals("DTC: Engine Protection Torque Derate (1569) Condition Exists (31) 35 times", instance.toString());
    }

    @Test
    public void testToStringNoDTC() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals("DTC: Unknown (0) Data Valid But Above Normal Operational Range - Most Severe Level (0) 0 times",
                instance.toString());
    }
}
