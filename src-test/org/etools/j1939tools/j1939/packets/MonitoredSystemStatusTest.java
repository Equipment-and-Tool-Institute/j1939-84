/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.ENABLED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.ENABLED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.NOT_ENABLED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM26MonitoredSystemStatus.NOT_ENABLED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM5MonitoredSystemStatus.NOT_SUPPORTED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM5MonitoredSystemStatus.NOT_SUPPORTED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM5MonitoredSystemStatus.SUPPORTED_COMPLETE;
import static org.etools.j1939tools.j1939.packets.DM5MonitoredSystemStatus.SUPPORTED_NOT_COMPLETE;
import static org.etools.j1939tools.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests the {@link MonitoredSystem} interface
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class MonitoredSystemStatusTest {

    @Test
    public void testFindStatus() {
        assertEquals(NOT_ENABLED_NOT_COMPLETE, findStatus(false, false, false));
        assertEquals(NOT_ENABLED_COMPLETE, findStatus(false, false, true));
        assertEquals(ENABLED_NOT_COMPLETE, findStatus(false, true, false));
        assertEquals(ENABLED_COMPLETE, findStatus(false, true, true));

        assertEquals(NOT_SUPPORTED_NOT_COMPLETE, findStatus(true, false, false));
        assertEquals(NOT_SUPPORTED_COMPLETE, findStatus(true, false, true));
        assertEquals(SUPPORTED_NOT_COMPLETE, findStatus(true, true, false));
        assertEquals(SUPPORTED_COMPLETE, findStatus(true, true, true));
    }
}
