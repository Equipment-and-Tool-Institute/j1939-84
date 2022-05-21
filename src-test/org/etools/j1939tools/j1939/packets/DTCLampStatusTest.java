/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.OTHER;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests the {@link DTCLampStatus} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DTCLampStatusTest {

    /**
     * Test method for
     * {@link DTCLampStatus#getRedStopLampStatus()}.
     */
    @Test
    public void testDTCLampsAllOther() {
        int[] data = new int[] { 0xEE, // SPN least significant bit
                0x10, // SPN most significant bit
                0x04, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0xAA, // Lamp Status/Support
                0x55 };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        assertEquals(OTHER, instance.getAmberWarningLampStatus());
        assertEquals(OTHER, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(OTHER, instance.getRedStopLampStatus());
        assertEquals(OTHER, instance.getProtectLampStatus());
    }

    /**
     * Test method for
     * {@link DTCLampStatus#DTCLampStatus(int[])}.
     * {@link DTCLampStatus#getProtectLampStatus()}.
     * {@link DTCLampStatus#getRedStopLampStatus()}.
     * {@link DTCLampStatus#getMalfunctionIndicatorLampStatus()}.
     */
    @Test
    public void testDTCLampStatus() {
        int[] data = new int[] { 0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x73, // Lamp Status/Support
                0x2E };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(609, 19, 1, 1);
        DTCLampStatus instance2 = DTCLampStatus.create(dtc, OFF, SLOW_FLASH, OTHER, OTHER);
        assertEquals(instance, instance2);
        assertEquals(OFF, instance.getAmberWarningLampStatus());
        assertEquals(SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(OTHER, instance.getRedStopLampStatus());
        assertEquals(OTHER, instance.getProtectLampStatus());
    }

    @Test
    public void testDTCLampStatusAllOff() {
        int[] data = new int[] { 0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(609, 19, 1, 1);
        DTCLampStatus instance2 = DTCLampStatus.create(dtc, OFF, OFF, OFF, OFF);
        assertTrue(instance.equals(instance2));
        assertEquals(OFF, instance.getAmberWarningLampStatus());
        assertEquals(OFF, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(OFF, instance.getRedStopLampStatus());
        assertEquals(OFF, instance.getProtectLampStatus());
    }

    @Test
    public void testDTCLampStatusAllSlowFlash() {
        int[] data = new int[] { 0xEE, // SPN least significant bit
                0x10, // SPN most significant bit
                0x04, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0x55, // Lamp Status/Support
                0x00 };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        DTCLampStatus instance2 = DTCLampStatus.create(dtc, SLOW_FLASH, SLOW_FLASH, SLOW_FLASH, SLOW_FLASH);
        assertTrue(instance.equals(instance2));
        assertEquals(SLOW_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(SLOW_FLASH, instance.getRedStopLampStatus());
        assertEquals(SLOW_FLASH, instance.getProtectLampStatus());
    }

    @Test
    public void testEquals() {

        DTCLampStatus instance = new DTCLampStatus(new int[] {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF });// Lamp Status/State
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(609, 19, 1, 1);
        DTCLampStatus instance2 = DTCLampStatus.create(dtc,
                                                       OFF,
                                                       OFF,
                                                       OFF,
                                                       OFF);
        DTCLampStatus instance3 = new DTCLampStatus(new int[] {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0xAA, // Lamp Status/Support
                0x55 });// Lamp Status/State
        assertEquals(instance, instance2);
        assertNotEquals(instance, instance3);
    }

    @Test
    public void testGetDtcs() {
        int[] data = new int[] { 0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        DiagnosticTroubleCode expected = new DiagnosticTroubleCode(new int[] { 0x61, 0x02, 0x13, 0x81 });
        assertEquals(expected, instance.getDtc());
    }

    /**
     * Test method for
     * {@link DTCLampStatus#hashCode()}.
     */
    @Test
    public void testHashCode() {
        DTCLampStatus instance = new DTCLampStatus(new int[] {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF });// Lamp Status/State
        DTCLampStatus instance2 = new DTCLampStatus(new int[] {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF });// Lamp Status/State
        assertTrue(instance.hashCode() == instance2.hashCode());
    }

    /**
     * Test method for
     * {@link DTCLampStatus#toString()}.
     */
    @Test
    public void testToString() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00, 0x62, 0x1D };
        DTCLampStatus instance = new DTCLampStatus(data);
        String expected = "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 0:0 - Unknown, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times";
        assertEquals(expected, instance.toString());
    }
}
