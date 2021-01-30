/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void stripLeadingAndTrailingNulls() {
        String input = "\u0000\u0000\u0000mes\u0000sage\u0000\u0000\u0000";
        String actual = StringUtils.stripLeadingAndTrailingNulls(input);
        assertEquals("mes\u0000sage", actual);
    }
}