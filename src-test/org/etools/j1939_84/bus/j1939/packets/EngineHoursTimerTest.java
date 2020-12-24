/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer.ERROR;
import static org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer.NOT_AVAILABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link EngineHoursTimer} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class EngineHoursTimerTest {

    private EngineHoursTimer instance;

    @Before
    public void setUp() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        instance = new EngineHoursTimer(data);
    }

    @Test
    public void testEngineHoursTimerError() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, (byte) 0xFE };
        EngineHoursTimer errorInstance = new EngineHoursTimer(data);
        assertEquals(ERROR, errorInstance.getEiAecdTimer2());
        String expected = "EI-AECD Number = 1: Timer 1 = 0 minutes; Timer 2 = errored";
        assertEquals(expected, errorInstance.toString());
    }

    @Test
    public void testEngineHoursTimerNoError() {
        byte[] data1 = { 0x38, 0x1A, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00 };
        EngineHoursTimer noErrorInstance = new EngineHoursTimer(data1);

        assertEquals(0x38, noErrorInstance.getEiAecdNumber());
        assertEquals(0x1A, noErrorInstance.getEiAecdTimer1());
        assertEquals(0x0B, noErrorInstance.getEiAecdTimer2());

        String expected = "EI-AECD Number = 56: Timer 1 = 26 minutes; Timer 2 = 11 minutes";
        assertEquals(expected, noErrorInstance.toString());
    }

    @SuppressWarnings({ "SimplifiableAssertion", "EqualsWithItself" }) @Test
    public void testEquals() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        EngineHoursTimer expected = new EngineHoursTimer(data);
        assertTrue(instance.equals(expected));
        byte[] data1 = { 0x38, 0x1A, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00 };
        EngineHoursTimer expected1 = new EngineHoursTimer(data1);
        assertFalse(instance.equals(expected1));
        byte[] data2 = { 0x01, 0x1A, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00 };
        EngineHoursTimer expected2 = new EngineHoursTimer(data2);
        assertFalse(instance.equals(expected2));
        byte[] data3 = { 0x01, 0x00, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00 };
        EngineHoursTimer expected3 = new EngineHoursTimer(data3);
        assertFalse(instance.equals(expected3));
        assertFalse(instance.equals(new Object()));
        assertTrue(instance.equals(instance));

    }

    @Test
    public void testGetEiAecdNumber() {
        assertEquals(1, instance.getEiAecdNumber());
    }

    @Test
    public void testGetEiAecdTimer1() {
        assertEquals(0, instance.getEiAecdTimer1());
    }

    @Test
    public void testGetEiAecdTimer2() {
        assertEquals(NOT_AVAILABLE, instance.getEiAecdTimer2());
    }

    @Test
    public void testHashCode() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        EngineHoursTimer expected = new EngineHoursTimer(data);
        assertEquals(expected.hashCode(), instance.hashCode());
    }

    @Test
    public void testToString() {
        String expected = "EI-AECD Number = 1: Timer 1 = 0 minutes; Timer 2 = n/a";
        assertEquals(expected, instance.toString());
    }

}
