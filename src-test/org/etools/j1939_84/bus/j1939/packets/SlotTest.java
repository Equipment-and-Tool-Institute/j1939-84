/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for the {@link Slot} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class SlotTest {

    @Test
    public void testSlotNoScaleNoOffset() {
        Slot slot = Slot.findSlot(41);
        assertNotNull(slot);
        assertEquals(41, slot.getId());
        assertEquals("SAEec03", slot.getName());
        assertEquals("Electrical Current", slot.getType());
        assertEquals(1, slot.getScaling(), 0.0);
        assertEquals(0, slot.getOffset(), 0.0);
        assertEquals(8, slot.getLength());
        assertEquals("A", slot.getUnit());
        assertEquals(200, slot.scale(200), 0.0);
    }

    @Test
    public void testSlotNoScaleWithOffset() {
        Slot slot = Slot.findSlot(5);
        assertNotNull(slot);
        assertEquals(5, slot.getId());
        assertEquals("SAEtm12", slot.getName());
        assertEquals("Time", slot.getType());
        assertEquals(1, slot.getScaling(), 0.0);
        assertEquals(-32127, slot.getOffset(), 0.0);
        assertEquals(16, slot.getLength());
        assertEquals("h", slot.getUnit());
        assertEquals(7873.0, slot.scale(40000), 0.0);
    }

    @Test
    public void testSlotWithBlankScaleWithBlankOffset() {
        Slot slot = Slot.findSlot(214);
        assertNotNull(slot);
        assertEquals(214, slot.getId());
        assertEquals("SAESP00", slot.getName());
        assertEquals("SPN", slot.getType());
        assertEquals(1, slot.getScaling(), 0.0);
        assertEquals(0, slot.getOffset(), 0.0);
        assertEquals(19, slot.getLength());
        assertEquals("", slot.getUnit());
        assertEquals(200, slot.scale(200), 0.0);
    }

    @Test
    public void testSlotWithPartScale() {
        Slot slot = Slot.findSlot(39);
        assertNotNull(slot);
        assertEquals(39, slot.getId());
        assertEquals("SAEds06", slot.getName());
        assertEquals("Distance", slot.getType());
        assertEquals(0.125, slot.getScaling(), 0.0);
        assertEquals(-2500, slot.getOffset(), 0.0);
        assertEquals(16, slot.getLength());
        assertEquals("m", slot.getUnit());
        assertEquals(7500, slot.scale(80000), 0.0);
    }

    @Test
    public void testSlotWithPositiveOffset() {
        Slot slot = Slot.findSlot(284);
        assertNotNull(slot);
        assertEquals(284, slot.getId());
        assertEquals("SAEcy02", slot.getName());
        assertEquals("Calendar, years", slot.getType());
        assertEquals(1, slot.getScaling(), 0.0);
        assertEquals(2000, slot.getOffset(), 0.0);
        assertEquals(6, slot.getLength());
        assertEquals("years", slot.getUnit());
        assertEquals(2017, slot.scale(17), 0.0);
    }

    @Test
    public void testSlotWithScaleNoOffset() {
        Slot slot = Slot.findSlot(1);
        assertNotNull(slot);
        assertEquals(1, slot.getId());
        assertEquals("SAEpr11", slot.getName());
        assertEquals("Pressure", slot.getType());
        assertEquals(5, slot.getScaling(), 0.0);
        assertEquals(0, slot.getOffset(), 0.0);
        assertEquals(8, slot.getLength());
        assertEquals("kPa", slot.getUnit());
        assertEquals(200, slot.scale(40), 0.0);
    }

    @Test
    public void testSlotWithWholeScaleWithWholeOffset() {
        Slot slot = Slot.findSlot(127);
        assertNotNull(slot);
        assertEquals(127, slot.getId());
        assertEquals("SAEfr02", slot.getName());
        assertEquals("Force", slot.getType());
        assertEquals(10, slot.getScaling(), 0.0);
        assertEquals(-320000, slot.getOffset(), 0.0);
        assertEquals(16, slot.getLength());
        assertEquals("N", slot.getUnit());
        assertEquals(80000.0, slot.scale(40000), 0.0);
    }
}
