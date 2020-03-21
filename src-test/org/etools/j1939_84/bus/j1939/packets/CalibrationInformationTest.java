/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.junit.Test;

/**
 * Unit tests for the {@link CalibrationInformation} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class CalibrationInformationTest {

    @Test
    public void testEqualsAndHashCode() {
        CalibrationInformation instance1 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        CalibrationInformation instance2 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @Test
    public void testEqualsThis() {
        CalibrationInformation instance = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertTrue(instance.equals(instance));
    }

    @Test
    public void testGetCalibrationIdentification() {
        CalibrationInformation instance = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertEquals("id", instance.getCalibrationIdentification());
    }

    @Test
    public void testGetCalibrationVerificationNumber() {
        CalibrationInformation instance = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertEquals("cvn", instance.getCalibrationVerificationNumber());
    }

    @Test
    public void testNotEqualsCVN() {
        CalibrationInformation instance1 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        CalibrationInformation instance2 = new CalibrationInformation("id",
                "cvn2",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsId() {
        CalibrationInformation instance1 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        CalibrationInformation instance2 = new CalibrationInformation("id2",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsObject() {
        CalibrationInformation instance = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testNotEqualsRawCalId() {
        CalibrationInformation instance1 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        CalibrationInformation instance2 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 10 },
                new byte[] { (byte) 1 });
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsRawCvn() {
        CalibrationInformation instance1 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        CalibrationInformation instance2 = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 11 });
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testToString() {
        CalibrationInformation instance = new CalibrationInformation("id",
                "cvn",
                new byte[] { (byte) 0 },
                new byte[] { (byte) 1 });
        String expected = "CAL ID of id and CVN of cvn";
        assertEquals(expected, instance.toString());
    }
}
