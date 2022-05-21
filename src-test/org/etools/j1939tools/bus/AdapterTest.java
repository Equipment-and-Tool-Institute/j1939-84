/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.bus;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the {@link Adapter} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class AdapterTest {

    @Test
    public void test() {
        Adapter instance = new Adapter("Name",
                                       "DLL",
                                       (short) 55,
                                       500,
                                       List.of("J1939:Baud=Auto", "J1939:Baud=500", "J1939:Baud=250"));
        assertEquals("Name", instance.getName());
        assertEquals("DLL", instance.getDLLName());
        assertEquals((short) 55, instance.getDeviceId());
    }
}
