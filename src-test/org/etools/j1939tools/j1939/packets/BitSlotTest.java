/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.j1939.J1939DaRepository;
import org.junit.Test;

/**
 * Unit tests for the {@link BitSlot} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class BitSlotTest {

    @Test
    public void test5843Construction() {
        Slot instance = getSlot(-5843);
        assertEquals("UNKNOWN", instance.getName());
        assertEquals(-5843, instance.getId());
        assertEquals("BITFIELD", instance.getType());
        assertEquals("", instance.getUnit());
        assertEquals(0.0, instance.getOffset(), 0.5);
    }

    @Test
    public void testSlot5843NotOccurred() {
        assertEquals("A warm-up cycle has not occurred on this engine start",
                getSlot(-5843).asString(new byte[] { 0x00 }));
    }

    @Test
    public void testSlot5843SaeReserved() {
        assertEquals("SAE reserved", getSlot(-5843).asString(new byte[] { 0x02 }));
    }

    @Test
    public void testSlot5843NotSupported() {
        assertEquals("Not supported or not available", getSlot(-5843).asString(new byte[] { 0x03 }));
    }

    @Test
    public void testSlot5843Occurred() {
        assertEquals("A warm-up cycle has occurred on this engine start",
                getSlot(-5843).asString(new byte[] { 0x01 }));
    }

    @Test
    public void testRealPacket() {
        Slot instance = getSlot(-5843);
        assertEquals("UNKNOWN", instance.getName());
        assertEquals(-5843, instance.getId());
        assertEquals("BITFIELD", instance.getType());
        assertEquals("", instance.getUnit());
        assertEquals(0.0, instance.getOffset(), 0.5);
    }

    @SuppressWarnings("SameParameterValue")
    private Slot getSlot(int id) {
        return J1939DaRepository.getInstance().findSLOT(id);
    }
}
