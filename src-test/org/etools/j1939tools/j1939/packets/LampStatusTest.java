/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests the {@link LampStatus} enum
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class LampStatusTest {

    @Test
    public void testAlternateOff() {
        assertEquals(LampStatus.ALTERNATE_OFF, LampStatus.getStatus(0, 0));
        assertArrayEquals(new int[] { 0, 0 }, LampStatus.getBytes(LampStatus.ALTERNATE_OFF));
    }

    @Test
    public void testFastFlash() {
        assertEquals(LampStatus.FAST_FLASH, LampStatus.getStatus(1, 1));
        assertArrayEquals(new int[] { 1, 1 }, LampStatus.getBytes(LampStatus.FAST_FLASH));
    }

    @Test
    public void testNotSupported() {
        assertEquals(LampStatus.NOT_SUPPORTED, LampStatus.getStatus(3, 3));
        assertArrayEquals(new int[] { 3, 3 }, LampStatus.getBytes(LampStatus.NOT_SUPPORTED));
    }

    @Test
    public void testOff() {
        assertEquals(LampStatus.OFF, LampStatus.getStatus(0, 1));
        assertEquals(LampStatus.OFF, LampStatus.getStatus(0, 2));
        assertEquals(LampStatus.OFF, LampStatus.getStatus(0, 3));
    }

    @Test
    public void testOn() {
        assertEquals(LampStatus.ON, LampStatus.getStatus(1, 3));
        assertArrayEquals(new int[] { 1, 3 }, LampStatus.getBytes(LampStatus.ON));
    }

    @Test
    public void testOther() {
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(1, 2));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(2, 0));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(2, 1));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(2, 2));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(2, 3));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(3, 0));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(3, 1));
        assertEquals(LampStatus.OTHER, LampStatus.getStatus(3, 2));

    }

    @Test
    public void testSlowFlash() {
        assertEquals(LampStatus.SLOW_FLASH, LampStatus.getStatus(1, 0));
        assertArrayEquals(new int[] { 1, 0 }, LampStatus.getBytes(LampStatus.SLOW_FLASH));
    }

    @Test
    public void testToString() {
        assertEquals("alternate off", LampStatus.ALTERNATE_OFF.toString());
        assertEquals("fast flash", LampStatus.FAST_FLASH.toString());
        assertEquals("not supported", LampStatus.NOT_SUPPORTED.toString());
        assertEquals("off", LampStatus.OFF.toString());
        assertEquals("on", LampStatus.ON.toString());
        assertEquals("other", LampStatus.OTHER.toString());
        assertEquals("slow flash", LampStatus.SLOW_FLASH.toString());
    }

    @Test
    public void testValueOf() {
        assertEquals(LampStatus.OFF, LampStatus.valueOf("OFF"));
        assertEquals(LampStatus.ALTERNATE_OFF, LampStatus.valueOf("ALTERNATE_OFF"));
        assertEquals(LampStatus.ON, LampStatus.valueOf("ON"));
        assertEquals(LampStatus.FAST_FLASH, LampStatus.valueOf("FAST_FLASH"));
        assertEquals(LampStatus.NOT_SUPPORTED, LampStatus.valueOf("NOT_SUPPORTED"));
        assertEquals(LampStatus.SLOW_FLASH, LampStatus.valueOf("SLOW_FLASH"));
        assertEquals(LampStatus.OTHER, LampStatus.valueOf("OTHER"));
    }

    @Test
    public void testValues() {
        assertEquals(7, LampStatus.values().length);
    }

}
