/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Unit tests for the {@link BusException} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class BusExceptionTest {

    @Test
    public void testStringCauseContructor() {
        RuntimeException expectedCause = new RuntimeException();
        String expectedMessage = "message";

        BusException instance = new BusException(expectedMessage, expectedCause);
        assertSame(expectedCause, instance.getCause());
        assertEquals(expectedMessage, instance.getMessage());
    }

    @Test
    public void testStringContructor() {
        String expectedMessage = "message";

        BusException instance = new BusException(expectedMessage);
        assertEquals(expectedMessage, instance.getMessage());
    }
}
