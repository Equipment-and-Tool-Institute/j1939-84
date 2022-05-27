/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DiagnosticTroubleCode} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DiagnosticTroubleCodeTest {

    @Test
    public void test1() {
        DiagnosticTroubleCode instance = DiagnosticTroubleCode.create(609, 19, 1, 1);
        assertEquals(1, instance.getConversionMethod());
        assertEquals(19, instance.getFailureModeIndicator());
        assertEquals(609, instance.getSuspectParameterNumber());
        assertEquals(1, instance.getOccurrenceCount());
    }

    @Test
    public void test2() {
        DiagnosticTroubleCode instance = DiagnosticTroubleCode.create(1569, 31, 0, 35);
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
    public void testEquals() {
        int[] data = new int[] { 0x61, // conversion method
                0x02, // suspect parameter number
                0x13, // failure mode indicator
                0x81 };// occurrence count
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        DiagnosticTroubleCode instance2 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        int[] data2 = new int[] { 0x13, 0x81, 0x61, 0x02 };
        DiagnosticTroubleCode instance3 = new DiagnosticTroubleCode(data2);
        DM31DtcToLampAssociation instance4 = new DM31DtcToLampAssociation(
                Packet.create(0, 0, 0x61, 0x02, 0x13, 0x81));
        assertTrue(instance.equals(instance2));
        assertFalse(instance.equals(instance3));
        // FIXME what is this supposed to be doing?
        assertFalse(instance.equals(instance4));
        int[] data3 = new int[] { 0x61, 0x02, 0x03, 0x81 };
        DiagnosticTroubleCode instance5 = new DiagnosticTroubleCode(data3);
        assertFalse(instance.equals(instance5));
        assertEquals(3, instance5.getFailureModeIndicator());
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
    public void testGetOccurrenceCount() {
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
    public void testHashCode() {
        int[] data = new int[] { 0x61, 0x02, 0x13, 0x81 };
        int[] data2 = new int[] { 0x13, 0x81, 0x61, 0x02 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        DiagnosticTroubleCode instance2 = new DiagnosticTroubleCode(data2);
        DiagnosticTroubleCode instance3 = new DiagnosticTroubleCode(data);
        assertNotEquals(instance.hashCode(), instance2.hashCode());
        assertEquals(instance.hashCode(), instance3.hashCode());
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
        assertEquals("DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 35 times",
                     instance.toString());
    }

    @Test
    public void testToStringNoDTC() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00 };
        DiagnosticTroubleCode instance = new DiagnosticTroubleCode(data);
        assertEquals("DTC 0:0 - Unknown, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times",
                     instance.toString());
    }
}
