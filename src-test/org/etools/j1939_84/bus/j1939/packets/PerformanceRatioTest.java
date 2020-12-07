/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the {@link PerformanceRatio} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class PerformanceRatioTest {

    @Test
    public void testEqualsAndHashCode() {
        PerformanceRatio instance1 = new PerformanceRatio(123, 456, 789, 0);
        PerformanceRatio instance2 = new PerformanceRatio(123, 456, 789, 0);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @Test
    public void testEqualsObject() {
        PerformanceRatio instance = new PerformanceRatio(0, 0, 0, 0);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testEqualsThis() {
        PerformanceRatio instance = new PerformanceRatio(0, 0, 0, 0);
        assertTrue(instance.equals(instance));
    }

    @Test
    public void testGetDenominator() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals(789, instance.getDenominator());
    }

    @Test
    public void testGetId() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals(123 << 8 | 0, instance.getId());
    }

    @Test
    public void testGetName() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals("SPN  123 Transmission Clutch 1 Pressure", instance.getName());
        // Make sure it's cached
        assertSame(instance.getName(), instance.getName());
    }

    @Test
    public void testGetNumerator() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals(456, instance.getNumerator());
    }

    @Test
    public void testGetSource() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals("Engine #1 (0)", instance.getSource());
        // Make sure it's cached
        assertSame(instance.getSource(), instance.getSource());
    }

    @Test
    public void testGetSourceAddress() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals(0, instance.getSourceAddress());
    }

    @Test
    public void testGetSpn() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        assertEquals(123, instance.getSpn());
    }

    @Test
    public void testNotEqualsDenominator() {
        PerformanceRatio instance1 = new PerformanceRatio(123, 456, 789, 0);
        PerformanceRatio instance2 = new PerformanceRatio(123, 456, 0, 0);
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsNumerator() {
        PerformanceRatio instance1 = new PerformanceRatio(123, 456, 789, 0);
        PerformanceRatio instance2 = new PerformanceRatio(123, 0, 789, 0);
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsSourceAddress() {
        PerformanceRatio instance1 = new PerformanceRatio(123, 456, 789, 0);
        PerformanceRatio instance2 = new PerformanceRatio(123, 456, 789, 1);
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsSPN() {
        PerformanceRatio instance1 = new PerformanceRatio(123, 456, 789, 0);
        PerformanceRatio instance2 = new PerformanceRatio(0, 456, 789, 0);
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testToString() {
        PerformanceRatio instance = new PerformanceRatio(123, 456, 789, 0);
        String expected = "SPN  123 Transmission Clutch 1 Pressure: 456 / 789";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testToStringLargeNumbers() {
        PerformanceRatio instance = new PerformanceRatio(123456, 4567, 78913, 0);
        String expected = "SPN 123456 Unknown: 4,567 / 78,913";
        assertEquals(expected, instance.toString());
    }

}
