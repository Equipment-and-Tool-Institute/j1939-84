/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void stripLeadingAndTrailingNulls() {
        String input = "\u0000\u0000\u0000mes\u0000sage\u0000\u0000\u0000";
        String actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("mes\u0000sage", actual);

        input = "\u0000\u0000\u0000mes\u0000sage";
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("mes\u0000sage", actual);

        input = "mes\u0000sage\u0000\u0000\u0000";
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("mes\u0000sage", actual);

        input = "mes\u0000sage";
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("mes\u0000sage", actual);

        input = "message";
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("message", actual);

        input = "\u0000\u0000\u0000";
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("", actual);

        input = null;
        actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("", actual);
    }

    @Test
    public void testContainsNonPrintableAsciiCharacter() {
        String input = "\u0000Fmes\u0000Csage\u0000A\u0000\u0000";
        assertTrue(StringUtils.containsNonPrintableAsciiCharacter(input));

        String inputWithoutNonPrintable = "message";
        assertFalse(StringUtils.containsNonPrintableAsciiCharacter(inputWithoutNonPrintable));

        byte[] inputBytes = { (byte) 0x00, (byte) 0x01 };
        assertTrue(StringUtils.containsNonPrintableAsciiCharacter(inputBytes));

        // Legit Hex values between 0x20 and 0x7F
        byte[] inputBytesWithoutNonPrintableChar = { 0x5D,
                0x3F,
                0x6F,
                0x60,
                0x50,
                0x42,
                0x54,
                0x75,
                0x4D,
                0x50,
                0x52,
                0x5F,
                0x4B,
                0x6A,
                0x67,
                0x69,
                0x59,
                0x76,
                0x45,
                0x7A };
        assertFalse(StringUtils.containsNonPrintableAsciiCharacter(inputBytesWithoutNonPrintableChar));

        byte[] inputBytesWithNonPrintableChar = { 0x5D,
                0x3F,
                0x6F,
                0x60,
                0x50,
                0x42,
                0x54,
                0x75,
                0x4D,
                0x50,
                0x52,
                0x5F,
                0x4B,
                0x6A,
                0x1B,  // unprintable character
                0x69,
                0x59,
                0x76,
                0x45,
                0x7A };
        assertTrue(StringUtils.containsNonPrintableAsciiCharacter(inputBytesWithNonPrintableChar));

        byte[] realTest = { (byte) 0xE2, (byte) 0x58, (byte) 0x7B, (byte) 0xB4, (byte) 0x45, (byte) 0x43, (byte) 0x55,
                (byte) 0x2D, (byte) 0x53, (byte) 0x57, (byte) 0x20, (byte) 0x6E, (byte) 0x75, (byte) 0x6D, (byte) 0x62,
                (byte) 0x65, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3F, (byte) 0x6C, (byte) 0x00,
                (byte) 0x00, (byte) 0x35, (byte) 0x38, (byte) 0x34, (byte) 0x33, (byte) 0x53, (byte) 0x30, (byte) 0x30,
                (byte) 0x36, (byte) 0x2E, (byte) 0x30, (byte) 0x30, (byte) 0x35, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x15, (byte) 0x2B, (byte) 0xDF, (byte) 0x8C, (byte) 0x53, (byte) 0x43, (byte) 0x41,
                (byte) 0x4E, (byte) 0x4F, (byte) 0x78, (byte) 0x4E, (byte) 0x2D, (byte) 0x30, (byte) 0x34, (byte) 0x34,
                (byte) 0x30, (byte) 0x41, (byte) 0x54, (byte) 0x49, (byte) 0/*x32*/, (byte) 0x61, (byte) 0x59, (byte) 0xD4,
                (byte) 0xFB, (byte) 0x53, (byte) 0x43, (byte) 0x41, (byte) 0x4E, (byte) 0x4F, (byte) 0x78, (byte) 0x4E,
                (byte) 0x2D, (byte) 0x30, (byte) 0x34, (byte) 0x34, (byte) 0x30, (byte) 0x41, (byte) 0x54, (byte) 0x4F,
                (byte) 0/*x32*/, (byte) 0x15, (byte) 0x2B, (byte) 0xDF, (byte) 0x8C, (byte) 0x53, (byte) 0x43, (byte) 0x41,
                (byte) 0x4E, (byte) 0x4F, (byte) 0x78, (byte) 0x4E, (byte) 0x2D, (byte) 0x30, (byte) 0x34, (byte) 0x34,
                (byte) 0x30, (byte) 0x41, (byte) 0x54, (byte) 0x49, (byte) 0/*x32*/, (byte) 0x8A, (byte) 0xFC, (byte) 0x90,
                (byte) 0x37, (byte) 0x50, (byte) 0x4D, (byte) 0x53, (byte) 0x50, (byte) 0x2A, (byte) 0x31, (byte) 0x32,
                (byte) 0x2A, (byte) 0x33, (byte) 0x35, (byte) 0x30, (byte) 0x2A, (byte) 0x41, (byte) 0x31, (byte) 0x30,
                (byte) 0/*x30*/, (byte) 0xC2, (byte) 0x90, (byte) 0x45, (byte) 0xC4, (byte) 0x30, (byte) 0x31, (byte) 0x38,
                (byte) 0x38, (byte) 0x30, (byte) 0x30, (byte) 0x36, (byte) 0x31, (byte) 0x30, (byte) 0x51, (byte) 0x20,
                (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x00 };
        for (int i = 4; i < realTest.length; i += 20) {

            byte[] buf = Arrays.copyOfRange(realTest, i, i + 16);
            assertTrue("i=" + i + ": " + new String(buf), StringUtils.containsNonPrintableAsciiCharacter(buf));

            buf = new String(buf).trim().getBytes();
            assertFalse("i=" + i + ": " + new String(buf), StringUtils.containsNonPrintableAsciiCharacter(buf));
        }
    }

    @Test
    public void testContainsOnlyNumericAsciiCharacters() {

        String input = "\u0000mes\u0000sage\u0000A\u0000\u0000";
        assertFalse(StringUtils.containsOnlyNumericAsciiCharacters(input));

        String inputOnlyNumbers = "012938457583957";
        assertTrue(StringUtils.containsOnlyNumericAsciiCharacters(inputOnlyNumbers));

        String inputStringAndNumbers = "01V#3845S58A957";
        assertFalse(StringUtils.containsOnlyNumericAsciiCharacters(inputStringAndNumbers));
    }
}
