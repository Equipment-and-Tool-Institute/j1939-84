/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;

/**
 * Unit tests for the {@link CalibrationInformation} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class CalibrationInformationTest {

    @Test
    public void testEqualsAndHashCode() {
        CalibrationInformation instance1 = new CalibrationInformation("id", "cvn");
        CalibrationInformation instance2 = new CalibrationInformation("id", "cvn");
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @Test
    public void testEqualsThis() {
        CalibrationInformation instance = new CalibrationInformation("id", "cvn");
        assertTrue(instance.equals(instance));
    }

    @Test
    public void testGetCalibrationIdentification() {
        CalibrationInformation instance = new CalibrationInformation("id", "cvn");
        assertEquals("id", instance.getCalibrationIdentification());
    }

    @Test
    public void testGetCalibrationVerificationNumber() {
        CalibrationInformation instance = new CalibrationInformation("id", "cvn");
        assertEquals("cvn", instance.getCalibrationVerificationNumber());
    }

    @Test
    public void testNotEqualsCVN() {
        CalibrationInformation instance1 = new CalibrationInformation("id", "cvn");
        CalibrationInformation instance2 = new CalibrationInformation("id", "cvn2");
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsId() {
        CalibrationInformation instance1 = new CalibrationInformation("id", "cvn");
        CalibrationInformation instance2 = new CalibrationInformation("id2", "cvn");
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsObject() {
        CalibrationInformation instance = new CalibrationInformation("id", "cvn");
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testToString() {
        CalibrationInformation instance = new CalibrationInformation("id", "cvn");
        String expected = "CAL ID of id and CVN of cvn";
        assertEquals(expected, instance.toString());
    }
}
