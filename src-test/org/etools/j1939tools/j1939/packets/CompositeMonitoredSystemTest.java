/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939tools.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for the {@link CompositeMonitoredSystem} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class CompositeMonitoredSystemTest {

    @Test
    public void testEqualsHashCode() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testEqualsHashCodeSelf() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(COLD_START_AID_SYSTEM, false);
        assertEquals(instance, instance);
        assertEquals(instance.hashCode(), instance.hashCode());
    }

    @Test
    public void testGetId() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(DIESEL_PARTICULATE_FILTER, false);
        assertEquals(DIESEL_PARTICULATE_FILTER, instance.getId());
    }

    @Test
    public void testGetName() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(FUEL_SYSTEM, false);
        assertEquals(FUEL_SYSTEM.getName(), instance.getName());
    }

    @Test
    public void testGetSourceAddress() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, false);
        assertEquals(-1, instance.getSourceAddress());
    }

    @Test
    public void testGetStatus() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(NOX_CATALYST_ABSORBER, false);
        assertNull(instance.getStatus());
    }

    @Test
    public void testGetStatusComplete() {
        MonitoredSystem system1 = new MonitoredSystem(
                                                      SECONDARY_AIR_SYSTEM,
                                                      findStatus(false, true, true),
                                                      123,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      NMHC_CONVERTING_CATALYST,
                                                      findStatus(false, true, true),
                                                      2,
                                                      true);
        MonitoredSystem system3 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      findStatus(false, true, true),
                                                      3,
                                                      true);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, true), instance.getStatus());
    }

    @Test
    public void testGetStatusNotComplete() {
        MonitoredSystem system1 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      findStatus(false, true, true),
                                                      1,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem(CATALYST, findStatus(false, true, false), 2, true);
        MonitoredSystem system3 = new MonitoredSystem(
                                                      EGR_VVT_SYSTEM,
                                                      findStatus(false, false, false),
                                                      3,
                                                      true);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, false), instance.getStatus());
    }

    @Test
    public void testGetStatusNotMonitored() {
        MonitoredSystem system1 = new MonitoredSystem(
                                                      FUEL_SYSTEM,
                                                      findStatus(false, false, false),
                                                      1,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      findStatus(false, false, false),
                                                      2,
                                                      true);
        MonitoredSystem system3 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      findStatus(false, false, false),
                                                      3,
                                                      true);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, false, false), instance.getStatus());
    }

    @Test
    public void testNotEqualsByAddress() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(EXHAUST_GAS_SENSOR, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, false);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
    }

    @Test
    public void testNotEqualsById() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(COLD_START_AID_SYSTEM, false);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
    }

    @Test
    public void testNotEqualsByName() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(HEATED_CATALYST, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(EGR_VVT_SYSTEM, false);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem system1 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      findStatus(false, true, true),
                                                      1,
                                                      true);
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(system1, false);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      findStatus(false, true, false),
                                                      1,
                                                      true);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(system2, false);
        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
    }

    @Test
    public void testNotEqualsObject() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(CompositeSystem.MISFIRE, false);
        assertNotEquals(instance, new Object());
    }

    @Test
    public void testToString() {
        MonitoredSystem system1 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS,
                                                      findStatus(false, true, true),
                                                      1,
                                                      false);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        assertEquals("    Boost pressure control sys     enabled,     complete", instance.toString());
    }
}
