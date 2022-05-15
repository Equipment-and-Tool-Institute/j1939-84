/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.etools.j1939tools.j1939.J1939DaRepository;
import org.junit.Test;

/**
 * Unit tests for the {@link Slot} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class SlotTest {

    @Test
    public void testNoData() {
        Slot slot = J1939DaRepository.findSlot(205, 0);
        byte[] data = {};
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test10BitsAsPercent() {
        Slot slot = J1939DaRepository.findSlot(205, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5 };
        assertEquals("34.600 %", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0x03 };
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test11Bits() {
        Slot slot = J1939DaRepository.findSlot(218, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5 };
        assertEquals("10101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF };
        assertEquals("11111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF };
        assertEquals("11111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test12Bits() {
        Slot slot = J1939DaRepository.findSlot(281, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5 };
        assertEquals("010101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF };
        assertEquals("111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF };
        assertEquals("111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test16Bits() {
        Slot slot = J1939DaRepository.findSlot(276, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5 };
        assertEquals("1010010101011010", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF };
        assertEquals("1111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF };
        assertEquals("1111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test1Bit() {
        Slot slot = J1939DaRepository.findSlot(86, 0);

        byte[] data = { 1 };
        assertEquals("1", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { 0 };
        assertEquals("0", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test1Byte() {
        Slot slot = J1939DaRepository.findSlot(2, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("1320.000 kPa", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0xFB };
        assertEquals("0xFB", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertTrue(slot.isFB(data));
    }

    @Test
    public void test21Bits() {
        Slot slot = J1939DaRepository.findSlot(217, 0);
        byte[] data = { (byte) 0xA5, (byte) 0x5A, (byte) 0xA5 };
        assertEquals("001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        assertEquals("111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF, (byte) 0xFF };
        assertEquals("111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test24Bits() {
        Slot slot = J1939DaRepository.findSlot(280, 0);
        byte[] data = { (byte) 0xA5, (byte) 0x5A, (byte) 0xA5 };
        assertEquals("101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        assertEquals("111111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF, (byte) 0xFF };
        assertEquals("111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test2Bits() {
        Slot instance = J1939DaRepository.findSlot(87, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("01", instance.asString(data));
        assertFalse(instance.isNotAvailable(data));
        assertFalse(instance.isError(data));

        data = new byte[] { (byte) 0x03 };
        assertEquals("11", instance.asString(data));
        assertTrue(instance.isNotAvailable(data));
        assertFalse(instance.isError(data));

        data = new byte[] { (byte) 0x02 };
        assertEquals("10", instance.asString(data));
        assertFalse(instance.isNotAvailable(data));
        assertTrue(instance.isError(data));
    }

    @Test
    public void test2Bytes() {
        Slot slot = J1939DaRepository.findSlot(13, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5 };
        assertEquals("1033.000 mm", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x00, (byte) 0xFF };
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0x00, (byte) 0xFE };
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0x00, (byte) 0xFB };
        assertEquals("0xFB00", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertTrue(slot.isFB(data));
    }

    @Test
    public void test32Bits() {
        Slot slot = J1939DaRepository.findSlot(245, 0);
        byte[] data = { (byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A };
        assertEquals("01011010101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        assertEquals("11111111111111111111111111111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        assertEquals("11111111111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test3Bits() {
        Slot slot = J1939DaRepository.findSlot(88, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x07 };
        assertEquals("111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x06 };
        assertEquals("110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test3Bytes() {
        Slot slot = J1939DaRepository.findSlot(122, 0);
        byte[] data = { (byte) 0xA5, (byte) 0x5A, (byte) 0xA5 };
        assertEquals("21673290.000 kg", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFF };
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFE };
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0xFB };
        assertEquals("0xFB0000", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertTrue(slot.isFB(data));
    }

    @Test
    public void test4Bits() {
        Slot slot = J1939DaRepository.findSlot(89, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("0101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("1111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("1110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));

    }

    @Test
    public void test4Bytes() {
        Slot slot = J1939DaRepository.findSlot(6, 0);
        byte[] data = { (byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5 };
        assertEquals("2774181210.000 s", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF };
        assertEquals("Not Available", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFE };
        assertEquals("Error", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
        assertFalse(slot.isFB(data));

        data = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFB };
        assertEquals("0xFB000000", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
        assertTrue(slot.isFB(data));
    }

    @Test
    public void test5BytesASCII() {
        Slot slot = J1939DaRepository.findSlot(273, 0);
        byte[] data = "12345".getBytes(UTF_8);
        assertEquals("12345", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test64Bits() {
        Slot slot = J1939DaRepository.findSlot(278, 0);

        byte[] data = { (byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5,
                (byte) 0x5A };
        assertEquals("0101101010100101010110101010010101011010101001010101101010100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF };
        // assertEquals("Not Available", slot.convert(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF };
        assertEquals("1111111111111111111111111111111111111111111111111111111111111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test6Bits() {
        Slot slot = J1939DaRepository.findSlot(91, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void test7Bits() {
        Slot slot = J1939DaRepository.findSlot(92, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("0100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("1111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("1111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));

    }

    @Test
    public void test7BytesASCII() {
        Slot slot = J1939DaRepository.findSlot(110, 0);
        byte[] data = "1234567".getBytes(UTF_8);
        assertEquals("1234567", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void test8Bits() {
        Slot slot = J1939DaRepository.findSlot(93, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("10100101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("11111111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("11111110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void testConvert5Bits() {
        Slot slot = J1939DaRepository.findSlot(292, 0);
        byte[] data = { (byte) 0xA5 };
        assertEquals("00101", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFF };
        assertEquals("11111", slot.asString(data));
        assertTrue(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));

        data = new byte[] { (byte) 0xFE };
        assertEquals("11110", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertTrue(slot.isError(data));
    }

    @Test
    public void testNonDelimitedASCII() {
        Slot slot = J1939DaRepository.findSlot(228, 0);

        byte[] data = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x51, 0x52, '*' };
        assertEquals("ABCDEQR*", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void testNullDelimitedASCII() {
        Slot slot = J1939DaRepository.findSlot(258, 0);

        byte[] data = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x00, 0x51, 0x52 };
        assertEquals("ABCDE", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void testSlotNoScaleNoOffset() {
        Slot slot = J1939DaRepository.findSlot(41, 0);
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
        Slot slot = J1939DaRepository.findSlot(5, 0);
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
    public void testSlotWithPartScale() {
        Slot slot = J1939DaRepository.findSlot(39, 0);
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
        Slot slot = J1939DaRepository.findSlot(284, 0);
        assertNotNull(slot);
        assertEquals(284, slot.getId());
        assertEquals("SAEcy02", slot.getName());
        assertEquals("Calendar, years", slot.getType());
        assertEquals(1, slot.getScaling(), 0.0);
        assertEquals(2000, slot.getOffset(), 0.0);
        assertEquals(6, slot.getLength());
        assertEquals("year", slot.getUnit());
        assertEquals(2017, slot.scale(17), 0.0);
    }

    @Test
    public void testSlotWithScaleNoOffset() {
        Slot slot = J1939DaRepository.findSlot(1, 0);
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
        Slot slot = J1939DaRepository.findSlot(127, 0);
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
    public void testStarDelimitedASCII() {
        Slot slot = J1939DaRepository.findSlot(108, 0);
        byte[] data = "1234567890*ASDFGHJKL".getBytes(UTF_8);
        assertEquals("1234567890", slot.asString(data));
        assertFalse(slot.isNotAvailable(data));
        assertFalse(slot.isError(data));
    }

    @Test
    public void verifySpecialCharacters() {
        assertEquals("km²/h²", J1939DaRepository.findSlot(469, 0).getUnit());
        assertEquals("m/s²", J1939DaRepository.findSlot(140, 0).getUnit());
        assertEquals("(kPa•s)/m³", J1939DaRepository.findSlot(359, 0).getUnit());
        assertEquals("µSiemens/mm", J1939DaRepository.findSlot(255, 0).getUnit());
        assertEquals("MJ/Nm³", J1939DaRepository.findSlot(323, 0).getUnit());
        assertEquals("µA", J1939DaRepository.findSlot(403, 0).getUnit());

    }

}
