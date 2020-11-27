/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Slot} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
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
        assertNull(slot.getScaling());
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

    @Test
    public void test1Bit() {
        Slot slot = Slot.findSlot(86);

        byte[] data = {1};
        assertEquals("1", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{0};
        assertEquals("0", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test2Bits() {
        Slot instance = Slot.findSlot(87);
        byte[] data = {(byte) 0xA5};
        assertEquals("01", instance.asString(data));
        assertFalse(instance.isNotAvailable(data));
        assertFalse(instance.isError(data));

        data = new byte[]{(byte) 0x03};
        assertEquals("11", instance.asString(data));
        assertTrue(instance.isNotAvailable(data));
        assertFalse(instance.isError(data));

        data = new byte[]{(byte) 0x02};
        assertEquals("10", instance.asString(data));
        assertFalse(instance.isNotAvailable(data));
        assertTrue(instance.isError(data));
    }

    @Test
    public void test3Bits() {
        Slot slot = Slot.findSlot(88);
        byte[] data = {(byte) 0xA5};
        assertEquals("101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x07};
        assertEquals("111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x06};
        assertEquals("110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test4Bits() {
        Slot slot = Slot.findSlot(89);
        byte[] data = {(byte) 0xA5};
        assertEquals("0101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("1111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("1110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));

    }

    @Test
    public void testConvert5Bits() {
        Slot slot = Slot.findSlot(292);
        byte[] data = {(byte) 0xA5};
        assertEquals("00101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("11111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("11110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test6Bits() {
        Slot slot = Slot.findSlot(91);
        byte[] data = {(byte) 0xA5};
        assertEquals("100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test7Bits() {
        Slot slot = Slot.findSlot(92);
        byte[] data = {(byte) 0xA5};
        assertEquals("0100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("1111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("1111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));

    }

    @Test
    public void test8Bits() {
        Slot slot = Slot.findSlot(93);
        byte[] data = {(byte) 0xA5};
        assertEquals("10100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("11111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("11111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test10BitsAsPercent() {
        Slot slot = Slot.findSlot(205);
        byte[] data = {(byte) 0x5A, (byte) 0xA5};
        assertEquals("34.600000 %", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0x03};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test11Bits() {
        Slot slot = Slot.findSlot(218);
        byte[] data = {(byte) 0x5A, (byte) 0xA5};
        assertEquals("10101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF};
        assertEquals("11111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF};
        assertEquals("11111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test12Bits() {
        Slot slot = Slot.findSlot(281);
        byte[] data = {(byte) 0x5A, (byte) 0xA5};
        assertEquals("010101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF};
        assertEquals("111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF};
        assertEquals("111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test16Bits() {
        Slot slot = Slot.findSlot(276);
        byte[] data = {(byte) 0x5A, (byte) 0xA5};
        assertEquals("1010010101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF};
        assertEquals("1111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF};
        assertEquals("1111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test21Bits() {
        Slot slot = Slot.findSlot(217);
        byte[] data = {(byte) 0xA5, (byte) 0x5A, (byte) 0xA5};
        assertEquals("001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals("111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF};
        assertEquals("111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test24Bits() {
        Slot slot = Slot.findSlot(280);
        byte[] data = {(byte) 0xA5, (byte) 0x5A, (byte) 0xA5};
        assertEquals("101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals("111111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF};
        assertEquals("111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test32Bits() {
        Slot slot = Slot.findSlot(245);
        byte[] data = {(byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A};
        assertEquals("01011010101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals("11111111111111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals("11111111111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test64Bits() {
        Slot slot = Slot.findSlot(278);

        byte[] data = {(byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A};
        assertEquals("0101101010100101010110101010010101011010101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        //assertEquals("Not Available", slot.convert(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals("1111111111111111111111111111111111111111111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test1Byte() {
        Slot slot = Slot.findSlot(2);
        byte[] data = {(byte) 0xA5};
        assertEquals("1320.000000 kPa", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFF};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0xFE};
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test2Bytes() {
        Slot slot = Slot.findSlot(13);
        byte[] data = {(byte) 0x5A, (byte) 0xA5};
        assertEquals("1033.000000 mm", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0xFF};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0xFE};
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test3Bytes() {
        Slot slot = Slot.findSlot(122);
        byte[] data = {(byte) 0xA5, (byte) 0x5A, (byte) 0xA5};
        assertEquals("21673290.000000 kg", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFF};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0xFE};
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test4Bytes() {
        Slot slot = Slot.findSlot(6);
        byte[] data = {(byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5};
        assertEquals("2774181210.000000 s", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFE};
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test5BytesASCII() {
        Slot slot = Slot.findSlot(273);
        byte[] data = "12345".getBytes();
        assertEquals("12345", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test7BytesASCII() {
        Slot slot = Slot.findSlot(110);
        byte[] data = "1234567".getBytes();
        assertEquals("1234567", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void testStarDelimitedASCII() {
        Slot slot = Slot.findSlot(108);
        byte[] data = "1234567890*ASDFGHJKL".getBytes();
        assertEquals("1234567890", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void testNullDelimitedASCII() {
        Slot slot = Slot.findSlot(258);

        byte[] data = {0x41, 0x42, 0x43, 0x44, 0x45, 0x00, 0x51, 0x52};
        assertEquals("ABCDE", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void testNonDelimitedASCII() {
        Slot slot = Slot.findSlot(228);

        byte[] data = {0x41, 0x42, 0x43, 0x44, 0x45, 0x51, 0x52, '*'};
        assertEquals("ABCDEQR*", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

}
