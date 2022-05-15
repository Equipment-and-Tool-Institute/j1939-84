/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939tools.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
        MonitoredSystem instance1 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, getStatus(true), 0, true);
        MonitoredSystem instance2 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, getStatus(true), 0, true);
        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
        assertEquals(instance1.hashCode(), instance2.hashCode());
        assertEquals(0, instance1.compareTo(instance2));
    }

    @Test
    public void testEqualsHashCodeSelf() {
        MonitoredSystem instance = new MonitoredSystem(SECONDARY_AIR_SYSTEM, getStatus(true), 0, true);
        assertEquals(instance, instance);
        assertEquals(instance.hashCode(), instance.hashCode());
        assertEquals(0, instance.compareTo(instance));
    }

    @Test
    public void testGetId() {
        MonitoredSystem instance = new MonitoredSystem(EXHAUST_GAS_SENSOR, getStatus(true), 0, true);
        assertEquals(EXHAUST_GAS_SENSOR, instance.getId());
    }

    @Test
    public void testGetName() {
        MonitoredSystem instance = new MonitoredSystem(EGR_VVT_SYSTEM, getStatus(true), 0, true);
        assertEquals("EGR/VVT system            ", instance.getName());
    }

    @Test
    public void testGetSourceAddress() {
        MonitoredSystem instance = new MonitoredSystem(HEATED_CATALYST, getStatus(true), 0, true);
        assertEquals(0, instance.getSourceAddress());
    }

    @Test
    public void testGetStatus() {
        MonitoredSystem instance = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, getStatus(true), 0, true);
        assertEquals(getStatus(true), instance.getStatus());
    }

    @Test
    public void testNotEqualsByAddress() {
        MonitoredSystem instance1 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, getStatus(true), 0, true);
        MonitoredSystem instance2 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, getStatus(true), 1, true);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
        assertEquals(-1, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem instance1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(true), 0, true);
        MonitoredSystem instance2 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(false), 0, true);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
        assertEquals(-78, instance1.compareTo(instance2));
    }

    @Test
    public void testNotEqualsObject() {
        MonitoredSystem instance = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, getStatus(true), 0, true);
        assertNotEquals(instance, new Object());
    }

    @Test
    public void testToString() {
        MonitoredSystem instance = new MonitoredSystem(COLD_START_AID_SYSTEM, getStatus(true), 0, true);
        assertEquals("    Cold start aid system          supported,     complete", instance.toString());
    }

}
