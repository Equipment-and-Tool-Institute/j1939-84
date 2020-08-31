/**
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for the {@link OBDModuleInformation} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class OBDModuleInformationTest {

    private static List<SupportedSPN> makeListOfSupportedSPNs(int[] data) {
        List<SupportedSPN> supportedSpnsList = new ArrayList<>();

        if (data == null) {
            SupportedSPN supportedSpn = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
            SupportedSPN supportedSpn2 = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
            SupportedSPN supportedSpn3 = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
            supportedSpnsList.add(supportedSpn);
            supportedSpnsList.add(supportedSpn2);
            supportedSpnsList.add(supportedSpn3);
        } else {
            SupportedSPN supportedSpn = new SupportedSPN(data);
            supportedSpnsList.add(supportedSpn);
        }
        return supportedSpnsList;
    }

    private OBDModuleInformation instance;

    private OBDModuleInformation instance2;

    private OBDModuleInformation instance3;

    @Before
    public void setUp() throws Exception {
        instance = new OBDModuleInformation(0);
        instance.setObdCompliance((byte) 4);
        instance.setSupportedSpns(makeListOfSupportedSPNs(null));

        instance2 = new OBDModuleInformation(0);
        instance2.setObdCompliance((byte) 4);
        instance2.setSupportedSpns(makeListOfSupportedSPNs(null));

        instance3 = instance.clone();
    }

    @Test
    public void testEquals() {
        assertFalse(instance.equals(new Object()));
        assertFalse(instance.equals(null));
        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(instance2));
        assertTrue(instance.equals(instance3));
    }

    @Test
    public void testGetDataStreamSpns() {
        List<SupportedSPN> expectedSPNs = new ArrayList<>();
        SupportedSPN supportedSpn = new SupportedSPN(new int[] { 0x01, 0x02, 0x1D, 8 });
        SupportedSPN supportedSpn2 = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
        expectedSPNs.add(supportedSpn);
        expectedSPNs.add(supportedSpn2);
        assertEquals("GetDataStreamSpn", expectedSPNs, instance.getDataStreamSpns());
    }

    @Test
    public void testGetFreezeFrameSpns() {
        List<SupportedSPN> expectedSPNs = new ArrayList<>();
        SupportedSPN supportedSpn = new SupportedSPN(new int[] { 0x00, 0x00, 0x00, 0x00 });
        SupportedSPN supportedSpn2 = new SupportedSPN(new int[] { 0xFE, 0xFE, 0xFE, 0xFE });
        expectedSPNs.add(supportedSpn);
        expectedSPNs.add(supportedSpn2);
        assertEquals("FreezeFrmSpn", expectedSPNs, instance.getFreezeFrameSpns());
    }

    @Test
    public void testGetObdCompliance() {
        assertEquals("ObdCompliance", (byte) 4, instance.getObdCompliance());
    }

    @Test
    public void testGetSupportedSpns() {
        assertEquals("SupportedSpn", makeListOfSupportedSPNs(null), instance.getSupportedSpns());
        instance.setSupportedSpns(Collections.emptyList());
        assertNotNull("SupportedSpn", instance.getSupportedSpns());
    }

    @Test
    public void testGetTestResultSpns() {
        List<SupportedSPN> expectedSPNs = makeListOfSupportedSPNs(new int[] { 0x00, 0x00, 0x00, 0x00 });
        assertEquals("", expectedSPNs, instance.getTestResultSpns());
    }

    @Test
    public void testHashCode() {
        assertTrue("HashCode", instance.hashCode() == instance2.hashCode());
        assertTrue("HashCode", instance.hashCode() == instance3.hashCode());
    }

    @Test
    public void testNotEqualsCalibrationInformation() {
        List<CalibrationInformation> calibrationInformation = new ArrayList<>();
        calibrationInformation.add(new CalibrationInformation("id", "cvn", new byte[] {}, new byte[] {}));
        instance2.setCalibrationInformation(calibrationInformation);
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testNotEqualsCompliance() {
        instance2.setObdCompliance((byte) 6);
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testNotEqualsModuleAddress() {
        instance2 = new OBDModuleInformation(4);
        instance2.setObdCompliance((byte) 4);
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testNotEqualsSupportedSpns() {
        instance2.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 1, 3, 5, 7, 9 }));
        SupportedSPN supportedSpn = new SupportedSPN(new int[] { 4, 5, 6, 7, 8 });
        List<SupportedSPN> supportedSpns = new ArrayList<>();
        supportedSpns.add(supportedSpn);
        instance2.setSupportedSpns(supportedSpns);
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testSetSupportedSpns() {
        List<SupportedSPN> supportedSpns = makeListOfSupportedSPNs(null);
        assertEquals("SupportedSpn", supportedSpns, instance.getSupportedSpns());
    }

    @Test
    public void testToString() {
        String expectedObd = "OBD Module Information:\n";
        expectedObd += "sourceAddress is : " + 0 + "\n";
        expectedObd += "obdCompliance is : " + 4 + "\n";
        expectedObd += "function is : " + 0 + "\n";
        expectedObd += "Supported SPNs: \n";
        expectedObd += "SPN 513 - Actual Engine - Percent Torque,SPN 0 - Unknown,SPN 524030 - Manufacturer Assignable SPN 524030";
        assertEquals(expectedObd, instance.toString());
    }

}
