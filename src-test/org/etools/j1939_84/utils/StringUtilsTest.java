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
