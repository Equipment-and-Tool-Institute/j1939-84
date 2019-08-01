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
 * Unit tests for the {@link CompositeMonitoredSystem} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class CompositeMonitoredSystemTest {

    @Test
    public void testEqualsHashCode() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem("Name", 0, 123, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }

    @Test
    public void testEqualsHashCodeSelf() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertTrue(instance.equals(instance));
        assertTrue(instance.hashCode() == instance.hashCode());
    }

    @Test
    public void testGetId() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertEquals(123, instance.getId());
    }

    @Test
    public void testGetName() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertEquals("Name", instance.getName());
    }

    @Test
    public void testGetSourceAddress() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertEquals(0, instance.getSourceAddress());
    }

    @Test
    public void testGetStatus() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertEquals(null, instance.getStatus());
    }

    @Test
    public void testGetStatusComplete() {
        MonitoredSystem system1 = new MonitoredSystem("System", findStatus(false, true, true), 1, 123);
        MonitoredSystem system2 = new MonitoredSystem("System", findStatus(false, true, true), 2, 123);
        MonitoredSystem system3 = new MonitoredSystem("System", findStatus(false, false, false), 3, 123);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, true), instance.getStatus());
    }

    @Test
    public void testGetStatusNotComplete() {
        MonitoredSystem system1 = new MonitoredSystem("System", findStatus(false, true, true), 1, 123);
        MonitoredSystem system2 = new MonitoredSystem("System", findStatus(false, true, false), 2, 123);
        MonitoredSystem system3 = new MonitoredSystem("System", findStatus(false, false, false), 3, 123);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, true, false), instance.getStatus());
    }

    @Test
    public void testGetStatusNotMonitored() {
        MonitoredSystem system1 = new MonitoredSystem("System", findStatus(false, false, false), 1, 123);
        MonitoredSystem system2 = new MonitoredSystem("System", findStatus(false, false, false), 2, 123);
        MonitoredSystem system3 = new MonitoredSystem("System", findStatus(false, false, false), 3, 123);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        instance.addMonitoredSystems(system2);
        instance.addMonitoredSystems(system3);
        assertEquals(findStatus(false, false, false), instance.getStatus());
    }

    @Test
    public void testNotEqualsByAddress() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem("Name", 0, 123, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem("Name", 1, 123, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsById() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem("Name", 0, 123, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem("Name", 0, 456, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsByName() {
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem("Name1", 0, 123, false);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem("Name2", 0, 123, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsByStatus() {
        MonitoredSystem system1 = new MonitoredSystem("System", findStatus(false, true, true), 1, 123);
        CompositeMonitoredSystem instance1 = new CompositeMonitoredSystem(system1, false);
        MonitoredSystem system2 = new MonitoredSystem("System", findStatus(false, true, false), 1, 123);
        CompositeMonitoredSystem instance2 = new CompositeMonitoredSystem(system2, false);
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testNotEqualsObject() {
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem("Name", 0, 123, false);
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testToString() {
        MonitoredSystem system1 = new MonitoredSystem("System", findStatus(false, true, true), 1, 123);
        CompositeMonitoredSystem instance = new CompositeMonitoredSystem(system1, false);
        assertEquals("System     enabled,     complete", instance.toString());
    }

}
