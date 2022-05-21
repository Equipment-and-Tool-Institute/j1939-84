package org.etools.j1939tools.j1939.model;

import org.junit.Test;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

public class ActiveTechnologyTest {

    @Test
    public void testToString2() {
        var instance =  ActiveTechnology.create(64255, new int[]{2, 0xA5, 0xA5, 0xA5, 0xA5});
        String expected = "Active Technology:  Cylinder Deactivation (2), Time = 424050.000 s, Vehicle Distance = 10601.250 km" + NL;
        String actual = instance.toString();
        assertEquals(expected, actual);
    }
}