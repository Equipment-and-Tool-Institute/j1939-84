/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link Packet} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class PacketTest {

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
    }

    @Test
    public void testCreateWithBytes() {
        Packet instance = Packet.create(0x1234, 56, new byte[] { 11, 22, 33 });
        assertEquals(6, instance.getPriority());
        assertEquals(0x1234, instance.getId(0xFFFF));
        assertEquals(56, instance.getSource());
        assertEquals(false, instance.isTransmitted());
        assertEquals(3, instance.getBytes().length);
        assertEquals(11, instance.get(0));
        assertEquals(22, instance.get(1));
        assertEquals(33, instance.get(2));
    }

    @Test
    public void testCreateWithInts() {
        Packet instance = Packet.create(0x1234, 56, new int[] { 11, 22, 33 });
        assertEquals(6, instance.getPriority());
        assertEquals(0x1234, instance.getId(0xFFFF));
        assertEquals(56, instance.getSource());
        assertEquals(false, instance.isTransmitted());
        assertEquals(3, instance.getBytes().length);
        assertEquals(11, instance.get(0));
        assertEquals(22, instance.get(1));
        assertEquals(33, instance.get(2));
    }

    @Test
    public void testCreateWithPriority() {
        Packet instance = Packet.create(18, 0x1234, 56, true, new byte[] { 11, 22, 33 });
        assertEquals(18, instance.getPriority());
        assertEquals(0x1234, instance.getId(0xFFFF));
        assertEquals(56, instance.getSource());
        assertEquals(true, instance.isTransmitted());
        assertEquals(3, instance.getBytes().length);
        assertEquals(11, instance.get(0));
        assertEquals(22, instance.get(1));
        assertEquals(33, instance.get(2));
    }

    @Test
    public void testEqualsAndHashCode() {
        Packet instance1 = Packet.create(6, 1234, 56, false, (byte) 11, (byte) 22, (byte) 33);
        Packet instance2 = Packet.create(6, 1234, 56, false, (byte) 11, (byte) 22, (byte) 33);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @Test
    public void testEqualsThis() {
        Packet instance = Packet.create(1234, 56, 11, 22, 33);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
    }

    @Test
    public void testGetBytes() {
        Packet instance = Packet.create(1234, 56, 11, 22, 33);
        byte[] expected = new byte[] { 11, 22, 33 };
        byte[] actual = instance.getBytes();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testGettersAndToString() {
        byte[] bytes = new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        Packet instance = Packet.create(6, 0x1234, 56, true, bytes);
        assertEquals(0x1234, instance.getId(0xFFFF));
        assertEquals(6, instance.getPriority());
        assertEquals(56, instance.getSource());
        assertEquals(true, instance.isTransmitted());
        assertArrayEquals(bytes, instance.getBytes());
        assertEquals(8, instance.getLength());
        int[] data = instance.getData(3, 6);
        assertEquals(3, data.length);
        assertEquals(0x44, data[0]);
        assertEquals(0x55, data[1]);
        assertEquals(0x66, data[2]);

        assertEquals(0x11, instance.get(0));
        assertEquals(0x1122, instance.get16Big(0));
        assertEquals(0x2211, instance.get16(0));
        assertEquals(0x332211, instance.get24(0));
        assertEquals(0x112233, instance.get24Big(0));
        assertEquals(0x44332211, instance.get32(0));
        assertEquals(0x11223344, instance.get32Big(0));

        String expected = "18123438 [8] 11 22 33 44 55 66 77 88 (TX)";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testNotEqualsDataLength() {
        Packet instance1 = Packet.create(1234, 56, 11, 22, 33);
        Packet instance2 = Packet.create(1234, 56, 11, 22);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsDataOrder() {
        Packet instance1 = Packet.create(1234, 56, 11, 22, 33);
        Packet instance2 = Packet.create(1234, 56, 33, 22, 11);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsDataValues() {
        Packet instance1 = Packet.create(1234, 56, 11, 22, 33);
        Packet instance2 = Packet.create(1234, 56, 44, 55, 66);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsId() {
        Packet instance1 = Packet.create(1234, 56, 11, 22, 33);
        Packet instance2 = Packet.create(5678, 56, 11, 22, 33);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsObject() {
        Packet instance = Packet.create(1234, 56, 11, 22, 33);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testNotEqualsPriority() {
        Packet instance1 = Packet.create(6, 1234, 56, true, new byte[8]);
        Packet instance2 = Packet.create(9, 1234, 56, true, new byte[8]);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsSource() {
        Packet instance1 = Packet.create(1234, 56, 11, 22, 33);
        Packet instance2 = Packet.create(1234, 78, 11, 22, 33);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsTransmitted() {
        Packet instance1 = Packet.create(6, 1234, 56, true, new byte[8]);
        Packet instance2 = Packet.create(6, 1234, 56, false, new byte[8]);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testParse() {
        byte[] bytes = new byte[] { 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C, 0x35, 0x37, 0x35,
                0x30, 0x31, 0x32, 0x2A };
        Packet expected = Packet.create(06, 0xFEEC, 0x00, false, bytes);
        Packet instance = Packet
                                .parse("18FEEC00 33 48 41 4D 4B 53 54 4E 30 46 4C 35 37 35 30 31 32 2A");

        assertEquals(expected, instance);
    }

    @Test
    public void testParseFailed1() {
        Packet instance = Packet.parse("18FEEC003348414D4B53544E30464C3537353031322A");
        assertEquals(null, instance);
    }

    @Test
    public void testParseFailed2() {
        Packet instance = Packet.parse("This is not a parseable packet, but it was never meant to be.");
        assertEquals(null, instance);
    }

    @Test
    public void testParseTransmitted() {
        byte[] bytes = new byte[] { 0x33, 0x48, 0x41, 0x4D, 0x4B, 0x53, 0x54, 0x4E, 0x30, 0x46, 0x4C, 0x35, 0x37, 0x35,
                0x30, 0x31, 0x32, 0x2A };
        Packet expected = Packet.create(06, 0xFEEC, 0x00, true, bytes);
        Packet instance = Packet
                                .parse("18FEEC00 33 48 41 4D 4B 53 54 4E 30 46 4C 35 37 35 30 31 32 2A (TX)");

        assertEquals(expected, instance);
    }

    @Test
    public void testToStringWithFormatter() {
        new TestDateTimeModule();
        Packet instance = Packet
                                .parse("18FEEC00 33 48 41 4D 4B 53 54 4E 30 46 4C 35 37 35 30 31 32 2A");

        String expected = "10:15:30.0000 18FEEC00 [18] 33 48 41 4D 4B 53 54 4E 30 46 4C 35 37 35 30 31 32 2A";
        String actual = instance.toTimeString();
        assertEquals(expected, actual);
    }

}
