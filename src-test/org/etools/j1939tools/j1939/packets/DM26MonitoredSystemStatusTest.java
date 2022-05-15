/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.ENABLED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.ENABLED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.NOT_ENABLED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.NOT_ENABLED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.valueOf;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.values;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests the {@link DM26MonitoredSystemStatus} enum
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM26MonitoredSystemStatusTest {

    @Test
    public void testToString() {
        assertEquals("    enabled,     complete", ENABLED_COMPLETE.toString());
        assertEquals("    enabled, not complete", ENABLED_NOT_COMPLETE.toString());
        assertEquals("not enabled,     complete", NOT_ENABLED_COMPLETE.toString());
        assertEquals("not enabled, not complete", NOT_ENABLED_NOT_COMPLETE.toString());
    }

    @Test
    public void testValueOf() {
        assertEquals(ENABLED_COMPLETE, valueOf("ENABLED_COMPLETE"));
        assertEquals(ENABLED_NOT_COMPLETE, valueOf("ENABLED_NOT_COMPLETE"));
        assertEquals(NOT_ENABLED_COMPLETE, valueOf("NOT_ENABLED_COMPLETE"));
        assertEquals(NOT_ENABLED_NOT_COMPLETE, valueOf("NOT_ENABLED_NOT_COMPLETE"));
    }

    @Test
    public void testValues() {
        assertEquals(4, values().length);
    }
}
