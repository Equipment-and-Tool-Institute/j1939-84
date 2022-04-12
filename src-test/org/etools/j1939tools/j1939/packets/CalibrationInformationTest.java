/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
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
        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testEqualsThis() {
        CalibrationInformation instance = new CalibrationInformation("id",
                                                                     "cvn",
                                                                     new byte[] { (byte) 0 },
                                                                     new byte[] { (byte) 1 });
        assertEquals(instance, instance);
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
        assertNotEquals(instance1, instance2);
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
        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testNotEqualsObject() {
        CalibrationInformation instance = new CalibrationInformation("id",
                                                                     "cvn",
                                                                     new byte[] { (byte) 0 },
                                                                     new byte[] { (byte) 1 });
        assertNotEquals(instance, new Object());
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
