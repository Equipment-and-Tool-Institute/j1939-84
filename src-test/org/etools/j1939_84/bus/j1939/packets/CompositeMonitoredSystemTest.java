/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the {@link CompositeMonitoredSystem} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@SuppressWarnings("SimplifiableAssertion")
public class CompositeMonitoredSystemTest {

    @Test
    public void testEqualsHashCode() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void testEqualsHashCodeSelf() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(COLD_START_AID_SYSTEM, false);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
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
        assertEquals(null, instance.getStatus());
    }

    @Test
    public void testGetStatusComplete() {
        MonitoredSystem system1 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      123,
                                                      SECONDARY_AIR_SYSTEM,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      2,
                                                      NMHC_CONVERTING_CATALYST,
                                                      true);
        MonitoredSystem system3 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      3,
                                                      EVAPORATIVE_SYSTEM,
                                                      true);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, true), instance.getStatus());
    }

    @Test
    public void testGetStatusNotComplete() {
        MonitoredSystem system1 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      1,
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem("System", findStatus(false, true, false), 2, CATALYST, true);
        MonitoredSystem system3 = new MonitoredSystem("System",
                                                      findStatus(false, false, false),
                                                      3,
                                                      EGR_VVT_SYSTEM,
                                                      true);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, false), instance.getStatus());
    }

    @Test
    public void testGetStatusNotMonitored() {
        MonitoredSystem system1 = new MonitoredSystem("System",
                                                      findStatus(false, false, false),
                                                      1,
                                                      FUEL_SYSTEM,
                                                      true);
        MonitoredSystem system2 = new MonitoredSystem("System",
                                                      findStatus(false, false, false),
                                                      2,
                                                      EVAPORATIVE_SYSTEM,
                                                      true);
        MonitoredSystem system3 = new MonitoredSystem("System",
                                                      findStatus(false, false, false),
                                                      3,
                                                      EXHAUST_GAS_SENSOR,
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
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsById() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(AC_SYSTEM_REFRIGERANT, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(COLD_START_AID_SYSTEM, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsByName() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(HEATED_CATALYST, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(EGR_VVT_SYSTEM, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem system1 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      1,
                                                      EXHAUST_GAS_SENSOR,
                                                      true);
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(system1, false);
        MonitoredSystem system2 = new MonitoredSystem("System",
                                                      findStatus(false, true, false),
                                                      1,
                                                      EXHAUST_GAS_SENSOR,
                                                      true);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(system2, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsObject() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(CompositeSystem.MISFIRE, false);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testToString() {
        MonitoredSystem system1 = new MonitoredSystem("System",
                                                      findStatus(false, true, true),
                                                      1,
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      false);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        assertEquals("    System     enabled,     complete", instance.toString());
    }
}
