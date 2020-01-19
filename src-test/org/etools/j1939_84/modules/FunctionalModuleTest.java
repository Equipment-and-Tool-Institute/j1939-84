/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.etools.j1939_84.bus.j1939.J1939;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link FunctionalModule} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class FunctionalModuleTest {

    private FunctionalModule instance;

    @Before
    public void setUp() throws Exception {
        instance = new FunctionalModule(new TestDateTimeModule()) {
        };
    }

    @Test
    public void testGetSetJ1939() {
        J1939 j1939 = new J1939(null);
        instance.setJ1939(j1939);
        assertSame(j1939, instance.getJ1939());
    }

    @Test
    public void testGetTime() {
        assertEquals("10:15:30.000", instance.getTime());
    }

    @Test
    public void testGetTimeoutMessage() {
        assertEquals("Error: Timeout - No Response.", FunctionalModule.TIMEOUT_MESSAGE);
    }

}
