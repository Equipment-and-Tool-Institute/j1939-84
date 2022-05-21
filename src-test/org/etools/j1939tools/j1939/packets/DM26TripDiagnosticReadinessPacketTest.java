/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket.PGN;
import static org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.Packet;
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

        String actual = instance.toString();
        String expected = "DM26 from Engine #1 (0): Warm-ups: 20, Time Since Engine Start: 5,643 seconds" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        enabled,     complete" + NL;
        expected += "    Fuel System                not enabled, not complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         enabled,     complete" + NL;
        expected += "    Boost pressure control sys     enabled,     complete" + NL;
        expected += "    Catalyst                       enabled, not complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system             enabled, not complete" + NL;
        expected += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "    Secondary air system       not enabled, not complete";
        assertEquals(expected, actual.toString());

        String expectedMonitorSystemString = "    A/C system refrigerant         enabled,     complete" + NL;
        expectedMonitorSystemString += "    Boost pressure control sys     enabled,     complete" + NL;
        expectedMonitorSystemString += "    Catalyst                       enabled, not complete" + NL;
        expectedMonitorSystemString += "    Cold start aid system      not enabled,     complete" + NL;
        expectedMonitorSystemString += "    Comprehensive component        enabled,     complete" + NL;
        expectedMonitorSystemString += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedMonitorSystemString += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedMonitorSystemString += "    Evaporative system             enabled, not complete" + NL;
        expectedMonitorSystemString += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expectedMonitorSystemString += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expectedMonitorSystemString += "    Fuel System                not enabled, not complete" + NL;
        expectedMonitorSystemString += "    Heated catalyst                enabled,     complete" + NL;
        expectedMonitorSystemString += "    Misfire                    not enabled,     complete" + NL;
        expectedMonitorSystemString += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expectedMonitorSystemString += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedMonitorSystemString += "    Secondary air system       not enabled, not complete";
        String actualMonitorSystemString = instance.getMonitoredSystems()
                                                   .stream()
                                                   .sorted()
                                                   .map(t -> t.toString())
                                                   .collect(Collectors.joining(NL));
        assertEquals(expectedMonitorSystemString, actualMonitorSystemString);
    }

    @Test
    public void testToStringWithError() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        String expected = "DM26 from Engine #1 (0): Warm-ups: error, Time Since Engine Start: error" + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        enabled,     complete" + NL;
        expected += "    Fuel System                not enabled, not complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         enabled,     complete" + NL;
        expected += "    Boost pressure control sys     enabled,     complete" + NL;
        expected += "    Catalyst                       enabled, not complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system             enabled, not complete" + NL;
        expected += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "    Secondary air system       not enabled, not complete";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFF, 0xFF, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        String expected = "DM26 from Engine #1 (0): Warm-ups: not available, Time Since Engine Start: not available"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        enabled,     complete" + NL;
        expected += "    Fuel System                not enabled, not complete" + NL;
        expected += "    Misfire                    not enabled,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         enabled,     complete" + NL;
        expected += "    Boost pressure control sys     enabled,     complete" + NL;
        expected += "    Catalyst                       enabled, not complete" + NL;
        expected += "    Cold start aid system      not enabled,     complete" + NL;
        expected += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expected += "    EGR/VVT system             not enabled,     complete" + NL;
        expected += "    Evaporative system             enabled, not complete" + NL;
        expected += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expected += "    Heated catalyst                enabled,     complete" + NL;
        expected += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expected += "    Secondary air system       not enabled, not complete";
        assertEquals(expected,
                     instance.toString());
    }

    @Test
    public void testValues() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(1, instance.getSpnValues(3301).size());
        assertEquals(1, instance.getSpnValues(3302).size());
        assertEquals(6, instance.getSpnValues(3303).size());
        assertEquals(13, instance.getSpnValues(3304).size());
        assertEquals(13, instance.getSpnValues(3305).size());

        // verify names match
        assertEquals(instance.getContinuouslyMonitoredSystems()
                             .stream()
                             .flatMap(s -> Stream.of(s.getName().trim() + " Support", s.getName().trim() + " Status"))
                             .collect(Collectors.toList()),
                     instance.getSpnValues(3303).stream().map(v -> v.getLabel()).collect(Collectors.toList()));
        assertEquals(instance.getNonContinuouslyMonitoredSystems()
                             .stream()
                             .map(s -> s.getName().trim() + " Support")
                             .collect(Collectors.toList()),
                     instance.getSpnValues(3304).stream().map(v -> v.getLabel()).collect(Collectors.toList()));
        assertEquals(instance.getNonContinuouslyMonitoredSystems()
                             .stream()
                             .map(s -> s.getName().trim() + " Status")
                             .collect(Collectors.toList()),
                     instance.getSpnValues(3305).stream().map(v -> v.getLabel()).collect(Collectors.toList()));
    }
}
