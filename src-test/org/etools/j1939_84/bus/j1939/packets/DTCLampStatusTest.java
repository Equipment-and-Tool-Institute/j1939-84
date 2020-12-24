/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DTCLampStatus} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DTCLampStatusTest {

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#getRedStopLampStatus()}.
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
        assertEquals(LampStatus.OTHER, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.OTHER, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, instance.getRedStopLampStatus());
        assertEquals(LampStatus.OTHER, instance.getProtectLampStatus());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#DTCLampStatus(int[])}.
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#getProtectLampStatus()}.
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#getRedStopLampStatus()}.
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#getMalfunctionIndicatorLampStatus()}.
     */
    @Test
    public void testDTCLampStatus() {
        int[] data = new int[] { 0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D };// Lamp Status/State
        DTCLampStatus instance = new DTCLampStatus(data);
        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, instance.getRedStopLampStatus());
        assertEquals(LampStatus.OTHER, instance.getProtectLampStatus());
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
        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.OFF, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OFF, instance.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, instance.getProtectLampStatus());
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
        assertEquals(LampStatus.SLOW_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getRedStopLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getProtectLampStatus());
    }

    @SuppressWarnings({ "EqualsBetweenInconvertibleTypes", "SimplifiableAssertion" }) @Test
    public void testEquals() {

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
        DTCLampStatus instance3 = new DTCLampStatus(new int[] {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0xAA, // Lamp Status/Support
                0x55 });// Lamp Status/State
        DiagnosticTroubleCodePacket instance4 = new DiagnosticTroubleCodePacket(Packet.create(0, 0x00,
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0xAA, // Lamp Status/Support
                0x55), null);
        assertEquals(true, instance.equals(instance2));
        assertEquals(false, instance.equals(instance3));
        assertEquals(false, instance.equals(instance4));
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
        assertEquals(expected, instance.getDtcs());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#hashCode()}.
     */
    @SuppressWarnings("SimplifiableAssertion") @Test
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
     * {@link org.etools.j1939_84.bus.j1939.packets.DTCLampStatus#toString()}.
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
