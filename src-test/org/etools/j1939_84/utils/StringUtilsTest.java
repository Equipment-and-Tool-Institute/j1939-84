/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
