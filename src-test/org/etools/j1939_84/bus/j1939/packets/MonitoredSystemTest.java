/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests the {@link MonitoredSystem} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class MonitoredSystemTest {

    @Test
    public void testEqualsHashCode() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.DIESEL_PARTICULATE_FILTER);
        MonitoredSystem instance2 = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.DIESEL_PARTICULATE_FILTER);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
        assertEquals(0, instance1.compareTo(instance2));
    }

    @Test
    public void testEqualsHashCodeSelf() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.SECONDARY_AIR_SYSTEM);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
        assertEquals(0, instance.compareTo(instance));
    }

    @Test
    public void testGetId() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.EXHAUST_GAS_SENSOR);
        assertEquals(CompositeSystem.EXHAUST_GAS_SENSOR, instance.getId());
    }

    @Test
    public void testGetName() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.EGR_VVT_SYSTEM);
        assertEquals("Name", instance.getName());
    }

    @Test
    public void testGetSourceAddress() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.HEATED_CATALYST);
        assertEquals(0, instance.getSourceAddress());
    }

    @Test
    public void testGetStatus() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS);
        assertEquals(findStatus(false, true, true), instance.getStatus());
    }

    @Test
    public void testNotEqualsByAddress() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.NMHC_CONVERTING_CATALYST);
        MonitoredSystem instance2 = new MonitoredSystem("Name", findStatus(false, true, true), 1,
                CompositeSystem.NMHC_CONVERTING_CATALYST);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-1, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsByName() {
        MonitoredSystem instance1 = new MonitoredSystem("Name1", findStatus(false, true, true), 0,
                CompositeSystem.NMHC_CONVERTING_CATALYST);
        MonitoredSystem instance2 = new MonitoredSystem("Name2", findStatus(false, true, true), 0,
                CompositeSystem.NMHC_CONVERTING_CATALYST);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-1, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.AC_SYSTEM_REFRIGERANT);
        MonitoredSystem instance2 = new MonitoredSystem("Name", findStatus(false, true, false), 0,
                CompositeSystem.AC_SYSTEM_REFRIGERANT);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-78, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsObject() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.AC_SYSTEM_REFRIGERANT);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testToString() {
        MonitoredSystem instance = new MonitoredSystem("Name", findStatus(false, true, true), 0,
                CompositeSystem.COLD_START_AID_SYSTEM);
        assertEquals("    Name                               supported,       completed", instance.toString());
    }

}
