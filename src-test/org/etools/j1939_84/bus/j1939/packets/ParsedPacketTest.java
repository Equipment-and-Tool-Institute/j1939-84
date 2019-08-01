/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

/**
 * Unit tests the {@link ParsedPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class ParsedPacketTest {

    @Test
    public void testFormat() {
        byte bytes[] = new byte[255];
        for (int i = 0; i < 255; i++) {
            bytes[i] = (byte) (i & 0xFF);
        }
        String actual = ParsedPacket.format(bytes);
        StringBuilder sb = new StringBuilder(
                "                                 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~                                ");
        for (int i = 0xA0; i < 0xFF; i++) {
            sb.append(Character.toString((char) i));
        }
        assertEquals(sb.toString(), actual);
    }

    @Test
    public void testGetByte() {
        Packet packet = Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals((byte) 0x88, instance.getByte(7));
    }

    @Test
    public void testGetInt() {
        Packet packet = Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(0x44332211, instance.getInt(0));
    }

    @Test
    public void testGetName() {
        Packet packet = Packet.create(0x123456, 0, 0);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals("1193046", instance.getName());
    }

    @Test
    public void testGetPacket() {
        Packet packet = Packet.create(0x123456, 99, 0);
        ParsedPacket instance = new ParsedPacket(packet);
        assertSame(packet, instance.getPacket());
    }

    @Test
    public void testGetScaledIntValue() {
        Packet packet = Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(114420174.5, instance.getScaledIntValue(0, 10), 0.0);
    }

    @Test
    public void testGetScaledIntValueWithError() {
        Packet packet = Packet.create(0, 0, 0x00, 0x00, 0x00, 0xFE, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getScaledIntValue(0, 10), 0.0);
    }

    @Test
    public void testGetScaledIntValueWithNA() {
        Packet packet = Packet.create(0, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getScaledIntValue(0, 10), 0.0);
    }

    @Test
    public void testGetScaledShortValue() {
        Packet packet = Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(872.1, instance.getScaledShortValue(0, 10), 0.0);
    }

    @Test
    public void testGetScaledShortValueWithError() {
        Packet packet = Packet.create(0, 0, 0x00, 0xFE, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getScaledShortValue(0, 10), 0.0);
    }

    @Test
    public void testGetScaledShortValueWithNA() {
        Packet packet = Packet.create(0, 0, 0xFF, 0xFF, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getScaledShortValue(0, 10), 0.0);
    }

    @Test
    public void testGetShaveAndAHaircut() {
        Packet packet = Packet.create(0, 0, 0x80);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(0x2, instance.getShaveAndAHaircut(0, 0xC0, 6));
    }

    @Test
    public void testGetShort() {
        Packet packet = Packet.create(0, 0, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(0x8877, instance.getShort(6));
    }

    @Test
    public void testGetSourceAddress() {
        Packet packet = Packet.create(0x123456, 99, 0);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals(99, instance.getSourceAddress());
    }

    @Test
    public void testGetStringPrefix() {
        Packet packet = Packet.create(0x123456, 23, 0);
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals("1193046 from Instrument Cluster #1 (23): ", instance.getStringPrefix());
    }

    @Test
    public void testGetValuesWithUnitsDouble() {
        assertEquals("123,456,789 unit1 (987,654,321 unit2)",
                ParsedPacket.getValuesWithUnits(123456789.0, "unit1", 987654321, "unit2"));
        assertEquals("11.9 (9.11)", ParsedPacket.getValuesWithUnits(11.9, null, 9.11, null));
        assertEquals("error", ParsedPacket.getValuesWithUnits(ParsedPacket.ERROR, "unit1", 987654321, "unit2"));
        assertEquals("not available",
                ParsedPacket.getValuesWithUnits(ParsedPacket.NOT_AVAILABLE, "unit1", 987654321, "unit2"));
    }

    @Test
    public void testGetValueWithUnitsBytes() {
        assertEquals("11 units", ParsedPacket.getValueWithUnits((byte) 11, "units"));
        assertEquals("11", ParsedPacket.getValueWithUnits((byte) 11, null));
        assertEquals("error", ParsedPacket.getValueWithUnits((byte) 0xFE, "units"));
        assertEquals("not available", ParsedPacket.getValueWithUnits((byte) 0xFF, "units"));
    }

    @Test
    public void testGetValueWithUnitsDouble() {
        assertEquals("123,456,789 units", ParsedPacket.getValueWithUnits(123456789.0, "units"));
        assertEquals("11.9", ParsedPacket.getValueWithUnits(11.9, null));
        assertEquals("error", ParsedPacket.getValueWithUnits(ParsedPacket.ERROR, "units"));
        assertEquals("not available", ParsedPacket.getValueWithUnits(ParsedPacket.NOT_AVAILABLE, "units"));
    }

    @Test
    public void testIsErrorFalse() {
        assertFalse(ParsedPacket.isError(0));
    }

    @Test
    public void testIsErrorTrue() {
        assertTrue(ParsedPacket.isError(ParsedPacket.ERROR));
    }

    @Test
    public void testIsNotAvailableFalse() {
        assertFalse(ParsedPacket.isNotAvailable(0));
    }

    @Test
    public void testIsNotAvailableTrue() {
        assertTrue(ParsedPacket.isNotAvailable(ParsedPacket.NOT_AVAILABLE));
    }

    @Test
    public void testToString() {
        Packet packet = mock(Packet.class);
        when(packet.toString()).thenReturn("packet");
        ParsedPacket instance = new ParsedPacket(packet);
        assertEquals("packet", instance.toString());
    }
}
