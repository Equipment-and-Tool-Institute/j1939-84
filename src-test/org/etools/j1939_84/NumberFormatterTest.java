/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the {@link NumberFormatter} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class NumberFormatterTest {

    @Test
    public void testFormatDropsDecimals() {
        assertEquals("123.456", NumberFormatter.format(123.45600000000000000000000009));
    }

    @Test
    public void testFormatDropsDecimalZeros() {
        assertEquals("123", NumberFormatter.format(123.0000));
    }

    @Test
    public void testFormatLargeDecimal() {
        assertEquals("123,456.789", NumberFormatter.format(123456.789));
    }

    @Test
    public void testFormatLargeNumber() {
        assertEquals("123,456", NumberFormatter.format(123456));
    }

    @Test
    public void testFormatSmallNumber() {
        assertEquals("123", NumberFormatter.format(123));
    }

    @Test
    public void testFormatSmallNumberDecimal() {
        assertEquals("123.456", NumberFormatter.format(123.456));
    }

}
