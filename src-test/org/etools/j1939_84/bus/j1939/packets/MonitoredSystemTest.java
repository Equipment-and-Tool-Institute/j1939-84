/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests the {@link MonitoredSystem} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class MonitoredSystemTest {

    private static MonitoredSystemStatus getStatus(boolean b) {
        return findStatus(false, true, b);
    }

    @Test
    public void testEqualsHashCode() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", getStatus(true), 0, DIESEL_PARTICULATE_FILTER, true);
        MonitoredSystem instance2 = new MonitoredSystem("Name", getStatus(true), 0, DIESEL_PARTICULATE_FILTER, true);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
        assertEquals(0, instance1.compareTo(instance2));
    }

    @Test
    public void testEqualsHashCodeSelf() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, SECONDARY_AIR_SYSTEM, true);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
        assertEquals(0, instance.compareTo(instance));
    }

    @Test
    public void testGetId() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, EXHAUST_GAS_SENSOR, true);
        assertEquals(EXHAUST_GAS_SENSOR, instance.getId());
    }

    @Test
    public void testGetName() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, EGR_VVT_SYSTEM, true);
        assertEquals("Name", instance.getName());
    }

    @Test
    public void testGetSourceAddress() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, HEATED_CATALYST, true);
        assertEquals(0, instance.getSourceAddress());
    }

    @Test
    public void testGetStatus() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, BOOST_PRESSURE_CONTROL_SYS, true);
        assertEquals(getStatus(true), instance.getStatus());
    }

    @Test
    public void testNotEqualsByAddress() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", getStatus(true), 0, NMHC_CONVERTING_CATALYST, true);
        MonitoredSystem instance2 = new MonitoredSystem("Name", getStatus(true), 1, NMHC_CONVERTING_CATALYST, true);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-1, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsByName() {
        MonitoredSystem instance1 = new MonitoredSystem("Name1", getStatus(true), 0, NMHC_CONVERTING_CATALYST, true);
        MonitoredSystem instance2 = new MonitoredSystem("Name2", getStatus(true), 0, NMHC_CONVERTING_CATALYST, true);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-1, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem instance1 = new MonitoredSystem("Name", getStatus(true), 0, AC_SYSTEM_REFRIGERANT, true);
        MonitoredSystem instance2 = new MonitoredSystem("Name", getStatus(false), 0, AC_SYSTEM_REFRIGERANT, true);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
        assertEquals(-78, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsObject() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, AC_SYSTEM_REFRIGERANT, true);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testToString() {
        MonitoredSystem instance = new MonitoredSystem("Name", getStatus(true), 0, COLD_START_AID_SYSTEM, true);
        assertEquals("    Name     supported,     complete", instance.toString());
    }

}
