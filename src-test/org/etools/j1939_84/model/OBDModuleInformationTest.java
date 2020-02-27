/**
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939_84.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
        SupportedSPN supportedSpn = new SupportedSPN(data);
        supportedSpnsList.add(supportedSpn);
        return supportedSpnsList;
    }

    private OBDModuleInformation instance;

    private OBDModuleInformation instance2;

    @Before
    public void setUp() throws Exception {
        instance = new OBDModuleInformation(0);
        instance.setObdCompliance((byte) 4);
        instance.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 4, 5, 6, 7, 8 }));

        instance2 = new OBDModuleInformation(0);
        instance2.setObdCompliance((byte) 4);
        instance2.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 4, 5, 6, 7, 8 }));

    }

    @Test
    public void testEquals() {
        assertFalse(instance.equals(new Object()));
        assertFalse(instance.equals(null));
        assertTrue(instance.equals(instance));
        assertTrue(instance.equals(instance2));
    }

    @Test
    public void testGetDataStreamSpns() {
        System.out.println(instance.getDataStreamSpns());
        assertTrue(instance.getDataStreamSpns().equals(new ArrayList<SupportedSPN>()));
    }

    @Test
    public void testGetFreezeFrameSpns() {
        List<SupportedSPN> expectedSPNs = makeListOfSupportedSPNs(new int[] { 4, 5, 6, 7, 8 });
        assertEquals(expectedSPNs, instance.getFreezeFrameSpns());
    }

    @Test
    public void testGetObdCompliance() {
        assertEquals("ObdCompliance", (byte) 4, instance.getObdCompliance());
    }

    @Test
    public void testGetSupportedSpns() {
        assertEquals("SupportedSpn", makeListOfSupportedSPNs(new int[] { 4, 5, 6, 7, 8 }), instance.getSupportedSpns());
        instance.setSupportedSpns(null);
        assertNotNull("SupportedSpn", instance.getSupportedSpns());
    }

    @Test
    public void testGetTestResultSpns() {
        assertTrue(instance.getTestResultSpns().equals(new ArrayList<SupportedSPN>()));
    }

    @Test
    public void testHashCode() {
        assertTrue("HashCode", instance.hashCode() == instance.hashCode());
        assertTrue("HashCode", instance2.hashCode() == instance2.hashCode());

        OBDModuleInformation instance3 = new OBDModuleInformation(0);
        instance3.setObdCompliance((byte) 4);
        instance3.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 1, 3, 5, 7, 9 }));
        assertTrue("HashCode", instance2.hashCode() != instance3.hashCode());
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
        instance2.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 4, 5, 6, 7, 8 }));
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testNotEqualsSupportedSpns() {
        instance2.setSupportedSpns(makeListOfSupportedSPNs(new int[] { 1, 3, 5, 7, 9 }));
        assertFalse(instance.equals(instance2));
    }

    @Test
    public void testSetObdCompliance() {
        assertEquals("SetObdCompliance", instance.getObdCompliance(), instance2.getObdCompliance());
    }

    @Test
    public void testSetSupportedSpns() {
        assertTrue("SupportedSpn", instance.getSupportedSpns().equals(instance2.getSupportedSpns()));
    }

    @Test
    public void testToString() {
        String expectedObd = "OBD Module Information:\n";
        expectedObd += "sourceAddress is : " + 0 + "\n";
        expectedObd += "obdCompliance is : " + 4 + "\n";
        expectedObd += "Supported SPNs: \n";
        expectedObd += "SPN 1284 - Engine Ignition Coil #17";
        assertEquals(expectedObd, instance.toString());
    }

}
