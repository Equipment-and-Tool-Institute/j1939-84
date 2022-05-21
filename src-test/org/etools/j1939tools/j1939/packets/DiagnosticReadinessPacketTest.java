/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Objects;

import org.etools.j1939tools.bus.Packet;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for the {@link DiagnosticReadinessPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@Ignore // abstract tests called from subclasses
public abstract class DiagnosticReadinessPacketTest {

    protected DiagnosticReadinessPacket createInstance(int... data) {
        return createInstance(Packet.create(0, 0, data));
    }

    protected abstract DiagnosticReadinessPacket createInstance(Packet packet);

    protected abstract MonitoredSystemStatus findStatus(boolean enabled, boolean complete);

    @Test
    public void test0x00() {
        DiagnosticReadinessPacket instance = createInstance(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        {
            List<MonitoredSystem> systems = instance.getContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(false, true), system.getStatus());
            }
        }
        {
            List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(false, true), system.getStatus());
            }
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        DiagnosticReadinessPacket instance1 = createInstance(0x11, 0x22, 0x33, 0x00, 0x55, 0x66, 0x77, 0x88);
        DiagnosticReadinessPacket instance2 = createInstance(0x11, 0x22, 0x33, 0x00, 0x55, 0x66, 0x77, 0x88);

        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeSelf() {
        DiagnosticReadinessPacket instance = createInstance(
                                                            0x11,
                                                            0x22,
                                                            0x33,
                                                            0x00,
                                                            0x55,
                                                            0x66,
                                                            0x77,
                                                            0x88);
        assertEquals(instance, instance);
        assertEquals(instance.hashCode(), instance.hashCode());
    }

    @Test
    public void testEqualsContinuouslyMonitoredSystems() {
        DiagnosticReadinessPacket instance1 = createInstance(0x11, 0x22, 0x33, 0x00, 0x55, 0x66, 0x77, 0x88);
        for (int i = 1; i < 255; i++) {
            DiagnosticReadinessPacket instance2 = createInstance(0x11, 0x22, 0x33, i, 0x55, 0x66, 0x77, 0x88);
            boolean equal = Objects.equals(instance1.getContinuouslyMonitoredSystems(),
                                           instance2.getContinuouslyMonitoredSystems());
            assertEquals("Failed at index " + i, equal, instance1.equals(instance2));
        }
    }

    @Test
    public void testEqualsWithObject() {
        DiagnosticReadinessPacket instance = createInstance(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        assertNotEquals(instance, new Object());
    }

    @Test
    public void testGetContinuouslyMonitoredSystemsComprehensiveComponentMonitoring() {
        final String name = "Comprehensive component   ";
        validateContinuouslyMonitoredSystems(name, 0, 0x00, findStatus(false, true));
        validateContinuouslyMonitoredSystems(name, 0, 0x04, findStatus(true, true));
        validateContinuouslyMonitoredSystems(name, 0, 0x40, findStatus(false, false));
        validateContinuouslyMonitoredSystems(name, 0, 0x44, findStatus(true, false));
    }

    @Test
    public void testGetContinuouslyMonitoredSystemsComprehensiveComponentMonitoring2() {

        final String name = "Comprehensive component   ";

        validateContinuouslyMonitoredSystems(name, 0, 0x04, findStatus(true, true));
        validateContinuouslyMonitoredSystems(name, 0, 0x40, findStatus(false, false));
        validateContinuouslyMonitoredSystems(name, 0, 0x44, findStatus(true, false));
    }

    @Test
    public void testGetContinuouslyMonitoredSystemsFuelSystemMonitoring() {
        final String name = "Fuel System               ";
        validateContinuouslyMonitoredSystems(name, 1, 0x00, findStatus(false, true));
        validateContinuouslyMonitoredSystems(name, 1, 0x02, findStatus(true, true));
        validateContinuouslyMonitoredSystems(name, 1, 0x20, findStatus(false, false));
        validateContinuouslyMonitoredSystems(name, 1, 0x22, findStatus(true, false));
    }

    @Test
    public void testGetContinuouslyMonitoredSystemsMisfireMonitoring() {
        final String name = "Misfire                   ";
        validateContinuouslyMonitoredSystems(name, 2, 0x00, findStatus(false, true));
        validateContinuouslyMonitoredSystems(name, 2, 0x01, findStatus(true, true));
        validateContinuouslyMonitoredSystems(name, 2, 0x10, findStatus(false, false));
        validateContinuouslyMonitoredSystems(name, 2, 0x11, findStatus(true, false));
    }

    @Test
    public void testGetMonitoredSystems() {
        DiagnosticReadinessPacket instance = createInstance(0, 0, 0, 0, 0, 0, 0, 0);

        List<MonitoredSystem> nonContSystems = instance.getNonContinuouslyMonitoredSystems();
        List<MonitoredSystem> contSystems = instance.getContinuouslyMonitoredSystems();
        List<MonitoredSystem> allSystems = instance.getMonitoredSystems();
        assertTrue(allSystems.containsAll(nonContSystems));
        assertTrue(allSystems.containsAll(contSystems));
        assertEquals(nonContSystems.size() + contSystems.size(), allSystems.size());
    }

    @Test
    public void testGetNonContinuouslyMonitoredSystems() {
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.EGR_VVT_SYSTEM, 0);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.EXHAUST_GAS_SENSOR_HEATER, 1);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.EXHAUST_GAS_SENSOR, 2);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.AC_SYSTEM_REFRIGERANT, 3);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.SECONDARY_AIR_SYSTEM, 4);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.EVAPORATIVE_SYSTEM, 5);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.HEATED_CATALYST, 6);
        validateNonContinuouslyMonitoredSystem1(CompositeSystem.CATALYST, 7);
        validateNonContinuouslyMonitoredSystem2(CompositeSystem.NMHC_CONVERTING_CATALYST, 8);
        validateNonContinuouslyMonitoredSystem2(CompositeSystem.NOX_CATALYST_ABSORBER, 9);
        validateNonContinuouslyMonitoredSystem2(CompositeSystem.DIESEL_PARTICULATE_FILTER, 10);
        validateNonContinuouslyMonitoredSystem2(CompositeSystem.BOOST_PRESSURE_CONTROL_SYS, 11);
        validateNonContinuouslyMonitoredSystem2(CompositeSystem.COLD_START_AID_SYSTEM, 12);
    }

    @Test
    public void testNotEqualsNonContinuouslyMonitoredSystemsCompleted() {
        DiagnosticReadinessPacket instance1 = createInstance(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        for (int i = 1; i < 255; i++) {
            DiagnosticReadinessPacket instance2 = createInstance(0x11, 0x22, 0x33, 0x44, 0xFF, 0xFF, i, i);
            assertNotEquals("Failed with index " + i, instance1, instance2);
        }
    }

    @Test
    public void testNotEqualsNonContinuouslyMonitoredSystemsSupported() {
        DiagnosticReadinessPacket instance1 = createInstance(0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88);
        for (int i = 1; i < 255; i++) {
            DiagnosticReadinessPacket instance2 = createInstance(0x11, 0x22, 0x33, 0x44, i, i, 0x77, 0x88);
            assertNotEquals("Failed with index " + i, instance1, instance2);
        }
    }

    @Test
    public void testNotEqualsSourceAddress() {
        DiagnosticReadinessPacket instance1 = createInstance(Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88));
        DiagnosticReadinessPacket instance2 = createInstance(Packet.create(0, 99, 11, 22, 33, 44, 55, 66, 77, 88));

        assertNotEquals(instance1, instance2);
        assertNotEquals(instance2, instance1);
    }

    private void validateContinuouslyMonitoredSystems(String name, int index, int value, MonitoredSystemStatus status) {
        DiagnosticReadinessPacket instance = createInstance(0, 0, 0, value, 0, 0, 0, 0);

        List<MonitoredSystem> systems = instance.getContinuouslyMonitoredSystems();
        assertEquals(3, systems.size());

        MonitoredSystem system = systems.get(index);
        assertEquals(name, system.getName());
        assertEquals(status, system.getStatus());
        assertEquals(instance.getSourceAddress(), system.getSourceAddress());
    }

    private void validateNonContinuouslyMonitoredSystem1(CompositeSystem system, int sourceAddress) {
        validateNonContinuouslyMonitoredSystems1(system,
                                                 sourceAddress,
                                                 0x00,
                                                 0x00,
                                                 findStatus(false, true));
        validateNonContinuouslyMonitoredSystems1(system,
                                                 sourceAddress,
                                                 0x00,
                                                 system.getMask(),
                                                 findStatus(false, false));
        validateNonContinuouslyMonitoredSystems1(system,
                                                 sourceAddress,
                                                 system.getMask(),
                                                 0x00,
                                                 findStatus(true, true));
        validateNonContinuouslyMonitoredSystems1(system,
                                                 sourceAddress,
                                                 system.getMask(),
                                                 system.getMask(),
                                                 findStatus(true, false));
    }

    private void validateNonContinuouslyMonitoredSystem2(CompositeSystem sys, int sourceAddress) {
        validateNonContinuouslyMonitoredSystems2(sys.getName(), sourceAddress, 0x00, 0x00, findStatus(false, true));
        validateNonContinuouslyMonitoredSystems2(sys.getName(),
                                                 sourceAddress,
                                                 0x00,
                                                 sys.getMask(),
                                                 findStatus(false, false));
        validateNonContinuouslyMonitoredSystems2(sys.getName(),
                                                 sourceAddress,
                                                 sys.getMask(),
                                                 0x00,
                                                 findStatus(true, true));
        validateNonContinuouslyMonitoredSystems2(sys.getName(),
                                                 sourceAddress,
                                                 sys.getMask(),
                                                 sys.getMask(),
                                                 findStatus(true, false));
    }

    private void validateNonContinuouslyMonitoredSystems1(CompositeSystem system,
                                                          int sourceAddress,
                                                          int lowerByte,
                                                          int upperByte,
                                                          MonitoredSystemStatus status) {
        DiagnosticReadinessPacket instance = createInstance(0, 0, 0, 0, lowerByte, 0, upperByte, 0);

        List<MonitoredSystem> monitoredSystems = instance.getNonContinuouslyMonitoredSystems();
        assertEquals(13, monitoredSystems.size());

        MonitoredSystem monitoredSystem = monitoredSystems.get(sourceAddress);
        assertEquals(system.getName(), monitoredSystem.getName());
        assertEquals(status, monitoredSystem.getStatus());
    }

    private void validateNonContinuouslyMonitoredSystems2(String name,
                                                          int index,
                                                          int lowerByte,
                                                          int upperByte,
                                                          MonitoredSystemStatus status) {
        DiagnosticReadinessPacket instance = createInstance(0, 0, 0, 0, 0, lowerByte, 0, upperByte);

        List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
        assertEquals(13, systems.size());

        MonitoredSystem system = systems.get(index);
        assertEquals(name, system.getName());
        assertEquals(status, system.getStatus());
        assertEquals(instance.getSourceAddress(), system.getSourceAddress());
    }
}
