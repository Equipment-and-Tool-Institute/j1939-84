/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket.PGN;
import static org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM26TripDiagnosticReadinessPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM26TripDiagnosticReadinessPacketTest extends DiagnosticReadinessPacketTest {

    @Override
    protected DiagnosticReadinessPacket createInstance(Packet packet) {
        return new DM26TripDiagnosticReadinessPacket(packet);
    }

    @Override
    protected MonitoredSystemStatus findStatus(boolean enabled, boolean complete) {
        return MonitoredSystemStatus.findStatus(false, enabled, complete);
    }

    @Test
    @Override
    public void test0x00() {
        super.test0x00();
    }

    @Test
    @Override
    public void testEqualsAndHashCode() {
        super.testEqualsAndHashCode();
    }

    @Test
    @Override
    public void testEqualsAndHashCodeSelf() {
        super.testEqualsAndHashCodeSelf();
    }

    @Test
    @Override
    public void testEqualsContinuouslyMonitoredSystems() {
        super.testEqualsContinuouslyMonitoredSystems();
    }

    @Test
    @Override
    public void testEqualsWithObject() {
        super.testEqualsWithObject();
    }

    @Test
    @Override
    public void testGetContinuouslyMonitoredSystemsComprehensiveComponentMonitoring() {
        super.testGetContinuouslyMonitoredSystemsComprehensiveComponentMonitoring();
    }

    @Test
    @Override
    public void testGetContinuouslyMonitoredSystemsFuelSystemMonitoring() {
        super.testGetContinuouslyMonitoredSystemsFuelSystemMonitoring();
    }

    @Test
    @Override
    public void testGetContinuouslyMonitoredSystemsMisfireMonitoring() {
        super.testGetContinuouslyMonitoredSystemsMisfireMonitoring();
    }

    @Test
    @Override
    public void testGetMonitoredSystems() {
        super.testGetMonitoredSystems();
    }

    @Test
    @Override
    public void testGetNonContinuouslyMonitoredSystems() {
        super.testGetNonContinuouslyMonitoredSystems();
    }

    @Test
    @Override
    public void testNotEqualsNonContinuouslyMonitoredSystemsCompleted() {
        super.testNotEqualsNonContinuouslyMonitoredSystemsCompleted();
    }

    @Test
    @Override
    public void testNotEqualsNonContinuouslyMonitoredSystemsSupported() {
        super.testNotEqualsNonContinuouslyMonitoredSystemsSupported();
    }

    @Test
    @Override
    public void testNotEqualsSourceAddress() {
        super.testNotEqualsSourceAddress();
    }

    @Test
    public void test0xFF() {
        DiagnosticReadinessPacket instance = createInstance(0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        {
            List<MonitoredSystem> systems = instance.getContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(true, false), system.getStatus());
            }
        }
        {
            List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(true, false), system.getStatus());
            }
        }
    }

    @Test
    public void test0xFFWith0x05AsThirdByte() {
        DiagnosticReadinessPacket instance = createInstance(0xFF, 0xFF, 0x05, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        {
            List<MonitoredSystem> systems = instance.getContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(true, false), system.getStatus());
            }
        }
        {
            List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(true, false), system.getStatus());
            }
        }
    }

    @Test
    public void testGetTimeSinceEngineStart() {
        DM26TripDiagnosticReadinessPacket instance = DM26TripDiagnosticReadinessPacket.create(0, 5643, 33);
        assertEquals(5643, instance.getTimeSinceEngineStart(), 0.0);
        assertEquals(33, instance.getWarmUpsSinceClear());
    }

    @Test
    public void testGetTimeSinceEngineStartWithError() {
        Packet packet = Packet.create(PGN, 0, 0x00, 0xFE, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getTimeSinceEngineStart(), 0.0);
    }

    @Test
    public void testGetTimeSinceEngineStartWithNA() {
        Packet packet = Packet.create(PGN, 0, 0x00, 0xFF, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getTimeSinceEngineStart(), 0.0);
    }

    @Test
    public void testGetWarmUpsSinceClear() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(33, instance.getWarmUpsSinceClear());
    }

    @Test
    public void testGetWarmUpsSinceClearWithError() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 0xFE, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFE, instance.getWarmUpsSinceClear());
    }

    @Test
    public void testGetWarmUpsSinceClearWithNA() {
        Packet packet = Packet.create(0, 0, 11, 22, 0xFF, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFF, instance.getWarmUpsSinceClear());
    }

    @Test
    public void testNotEqualsTimeSinceClear() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 0, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testNotEqualsTimeSinceClearByte2() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 11, 0, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testNotEqualsWarmUps() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 11, 22, 0, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testPGN() {
        assertEquals(64952, PGN);
    }

    @Test
    public void testToString() {
        var enabledSystems = List.of(CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EXHAUST_GAS_SENSOR,
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.HEATED_CATALYST,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS);
        var completeSystems = List.of(CompositeSystem.COMPREHENSIVE_COMPONENT,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.COLD_START_AID_SYSTEM);

        DM26TripDiagnosticReadinessPacket instance = create(0, 5643, 20, enabledSystems, completeSystems);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: 20, Time Since Engine Start: 5,643 seconds",
                     instance.toString());

        StringBuilder actual = new StringBuilder();
        for (MonitoredSystem system : instance.getMonitoredSystems()) {
            actual.append(system.toString()).append(NL);
        }

        String expected = "";
        expected += "    Comprehensive component        enabled,     complete" + NL;
        expected += "    Fuel System                not enabled, not complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expected += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expected += "    A/C system refrigerant         enabled,     complete" + NL;
        expected += "    Secondary air system       not enabled, not complete" + NL;
        expected += "    Evaporative system             enabled, not complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    Catalyst                       enabled, not complete" + NL;
        expected += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    Boost pressure control sys     enabled,     complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testToStringWithError() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: error, Time Since Engine Start: error", instance.toString());
    }

    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFF, 0xFF, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: not available, Time Since Engine Start: not available",
                     instance.toString());
    }
}
