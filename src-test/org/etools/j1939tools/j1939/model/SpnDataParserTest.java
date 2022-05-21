/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SpnDataParserTest {

    @Test
    public void test1Bit() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 1, 1, 0);
        assertEquals(1, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 1);

        assertEquals(1, resultData.length);
        assertEquals(1, resultData[0]);
    }

    @Test
    public void test1Byte() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 8, 1, 0);
        assertEquals(8, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 8);

        assertEquals(1, resultData.length);
        assertEquals((byte) 0x88, resultData[0]);
    }

    @Test
    public void test21Bits() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };

        SpnDefinition definition = new SpnDefinition(0, null, 4, 1, 0);
        assertEquals(4, definition.getStartByte());
        assertEquals(1, definition.getStartBit());
        byte[] resultData = SpnDataParser.parse(data, definition, 21);

        assertEquals(3, resultData.length);
        assertEquals(0x44, resultData[0]);
        assertEquals(0x55, resultData[1]);
        assertEquals(0b0110, resultData[2]);
    }

    @Test
    public void test2Bits() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 2, 5, 0);
        assertEquals(2, definition.getStartByte());
        assertEquals(5, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 2);

        assertEquals(1, resultData.length);
        assertEquals(2, resultData[0]);
    }

    @Test
    public void test2Bytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 1, 1, 0);
        assertEquals(1, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 16);

        assertEquals(2, resultData.length);
        assertEquals(0x11, resultData[0]);
        assertEquals(0x22, resultData[1]);
    }

    @Test
    public void test3Bytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 3, 1, 0);
        assertEquals(3, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 24);

        assertEquals(3, resultData.length);
        assertEquals(0x33, resultData[0]);
        assertEquals(0x44, resultData[1]);
        assertEquals(0x55, resultData[2]);
    }

    @Test
    public void test4Bytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 5, 1, 0);
        assertEquals(5, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 32);

        assertEquals(4, resultData.length);
        assertEquals(0x55, resultData[0]);
        assertEquals(0x66, resultData[1]);
        assertEquals(0x77, resultData[2]);
        assertEquals((byte) 0x88, resultData[3]);
    }

    @Test
    public void test8Bytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 1, 1, 0);
        assertEquals(1, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, 64);

        assertEquals(8, resultData.length);
        assertEquals(0x11, resultData[0]);
        assertEquals(0x22, resultData[1]);
        assertEquals(0x33, resultData[2]);
        assertEquals(0x44, resultData[3]);
        assertEquals(0x55, resultData[4]);
        assertEquals(0x66, resultData[5]);
        assertEquals(0x77, resultData[6]);
        assertEquals((byte) 0x88, resultData[7]);
    }

    @Test
    public void testNBytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 1, 1, 0);
        assertEquals(1, definition.getStartByte());
        assertEquals(1, definition.getStartBit());

        byte[] resultData = SpnDataParser.parse(data, definition, -1);

        assertEquals(8, resultData.length);
        assertEquals(0x11, resultData[0]);
        assertEquals(0x22, resultData[1]);
        assertEquals(0x33, resultData[2]);
        assertEquals(0x44, resultData[3]);
        assertEquals(0x55, resultData[4]);
        assertEquals(0x66, resultData[5]);
        assertEquals(0x77, resultData[6]);
        assertEquals((byte) 0x88, resultData[7]);
    }

    @Test
    public void testTooFewBytes() {
        byte[] data = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 };
        SpnDefinition definition = new SpnDefinition(0, null, 10, 1, 0);

        byte[] resultData = SpnDataParser.parse(data, definition, 8);

        assertEquals(0, resultData.length);
    }

}
