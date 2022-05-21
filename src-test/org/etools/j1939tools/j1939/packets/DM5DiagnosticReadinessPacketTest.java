/*
 * Copyright (c) 2022. Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939tools.bus.Packet;
import org.etools.testdoc.TestDoc;
import org.junit.Test;

/**
 * Unit tests the {@link DM5DiagnosticReadinessPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@TestDoc()
public class DM5DiagnosticReadinessPacketTest extends DiagnosticReadinessPacketTest {

    @Override
    protected DiagnosticReadinessPacket createInstance(Packet packet) {
        return new DM5DiagnosticReadinessPacket(packet);
    }

    @Override
    protected MonitoredSystemStatus findStatus(boolean enabled, boolean complete) {
        return MonitoredSystemStatus.findStatus(true, enabled, complete);
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
                assertEquals(system.getName() + " is wrong", findStatus(false, false), system.getStatus());
            }
        }
        {
            List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(false, false), system.getStatus());
            }
        }
    }

    @Test
    public void test0xFFWithNoOBD() {
        DiagnosticReadinessPacket instance = createInstance(0xFF, 0xFF, 0x05, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);
        {
            List<MonitoredSystem> systems = instance.getContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(false, false), system.getStatus());
            }
        }
        {
            List<MonitoredSystem> systems = instance.getNonContinuouslyMonitoredSystems();
            for (MonitoredSystem system : systems) {
                assertEquals(system.getName() + " is wrong", findStatus(false, false), system.getStatus());
            }
        }
    }

    @Test
    public void testGetActiveCodeCount() {
        var supportedSystems = List.of(CompositeSystem.COMPREHENSIVE_COMPONENT,
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
        DM5DiagnosticReadinessPacket instance = DM5DiagnosticReadinessPacket.create(0,
                                                                                    11,
                                                                                    22,
                                                                                    33,
                                                                                    supportedSystems,
                                                                                    completeSystems);
        assertEquals(11, instance.getActiveCodeCount());

        StringBuilder actual = new StringBuilder();
        for (MonitoredSystem system : instance.getMonitoredSystems()) {
            actual.append(system.toString()).append(NL);
        }

        String expected = "";
        expected += "    Comprehensive component        supported,     complete" + NL;
        expected += "    Fuel System                not supported, not complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor             supported,     complete" + NL;
        expected += "    A/C system refrigerant         supported,     complete" + NL;
        expected += "    Secondary air system       not supported, not complete" + NL;
        expected += "    Evaporative system             supported, not complete" + NL;
        expected += "    Heated catalyst                supported,     complete" + NL;
        expected += "    Catalyst                       supported, not complete" + NL;
        expected += "    NMHC converting catalyst   not supported, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Diesel Particulate Filter  not supported,     complete" + NL;
        expected += "    Boost pressure control sys     supported,     complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        assertEquals(expected, actual.toString());
    }

    @Test
    public void testGetActiveCodeCountWithError() {
        Packet packet = Packet.create(PGN, 0, 0xFE, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFE, instance.getActiveCodeCount());
    }

    @Test
    public void testGetActiveCodeCountWithNA() {
        Packet packet = Packet.create(PGN, 0, 0xFF, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFF, instance.getActiveCodeCount());
    }

    @Test
    public void testGetOBDCompliance() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(33, instance.getOBDCompliance());
    }

    @Test
    public void testGetPreviouslyActiveCodeCount() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(22, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testGetPreviouslyActiveCodeCountWithError() {
        Packet packet = Packet.create(PGN, 0, 11, 0xFE, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFE, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testGetPreviouslyActiveCodeCountWithNA() {
        Packet packet = Packet.create(PGN, 0, 11, 0xFF, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFF, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testisHdObdComplianceFalse() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertFalse(instance.isHdObd());
    }

    @Test
    public void testisHdObdComplianceTrue19() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 19, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertTrue(instance.isHdObd());
    }

    @Test
    public void testisHdObdComplianceTrue20() {
        Packet packet = Packet.create(PGN, 0, 11, 22, 20, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertTrue(instance.isHdObd());
    }

    @Test
    public void testNotEqualsActiveCount() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 0, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testNotEqualsOBDCompliance() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 11, 22, 0, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testNotEqualsPreviouslyActiveCount() {
        Packet packet1 = Packet.create(PGN, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(PGN, 0, 11, 0, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertNotEquals(instance1, instance2);
    }

    @Test
    public void testPGN() {
        assertEquals(65230, PGN);
    }

    @Test
    public void testToString() {
        DM5DiagnosticReadinessPacket instance = DM5DiagnosticReadinessPacket.create(0, 11, 22, 20);

        String expectedMonitorSystemString = "    A/C system refrigerant     not supported, not complete"
                + NL;
        expectedMonitorSystemString += "    Boost pressure control sys not supported, not complete" + NL;
        expectedMonitorSystemString += "    Catalyst                   not supported, not complete" + NL;
        expectedMonitorSystemString += "    Cold start aid system      not supported, not complete" + NL;
        expectedMonitorSystemString += "    Comprehensive component    not supported, not complete" + NL;
        expectedMonitorSystemString += "    Diesel Particulate Filter  not supported, not complete" + NL;
        expectedMonitorSystemString += "    EGR/VVT system             not supported, not complete" + NL;
        expectedMonitorSystemString += "    Evaporative system         not supported, not complete" + NL;
        expectedMonitorSystemString += "    Exhaust Gas Sensor         not supported, not complete" + NL;
        expectedMonitorSystemString += "    Exhaust Gas Sensor heater  not supported, not complete" + NL;
        expectedMonitorSystemString += "    Fuel System                not supported, not complete" + NL;
        expectedMonitorSystemString += "    Heated catalyst            not supported, not complete" + NL;
        expectedMonitorSystemString += "    Misfire                    not supported, not complete" + NL;
        expectedMonitorSystemString += "    NMHC converting catalyst   not supported, not complete" + NL;
        expectedMonitorSystemString += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expectedMonitorSystemString += "    Secondary air system       not supported, not complete";
        String actualMonitorSystemString = instance.getMonitoredSystems()
                                                   .stream()
                                                   .sorted()
                                                   .map(t -> t.toString())
                                                   .collect(Collectors.joining(NL));
        assertEquals(expectedMonitorSystemString, actualMonitorSystemString);

        String expected = "DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not supported, not complete" + NL;
        expected += "    Fuel System                not supported, not complete" + NL;
        expected += "    Misfire                    not supported, not complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not supported, not complete" + NL;
        expected += "    Boost pressure control sys not supported, not complete" + NL;
        expected += "    Catalyst                   not supported, not complete" + NL;
        expected += "    Cold start aid system      not supported, not complete" + NL;
        expected += "    Diesel Particulate Filter  not supported, not complete" + NL;
        expected += "    EGR/VVT system             not supported, not complete" + NL;
        expected += "    Evaporative system         not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor         not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not supported, not complete" + NL;
        expected += "    Heated catalyst            not supported, not complete" + NL;
        expected += "    NMHC converting catalyst   not supported, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Secondary air system       not supported, not complete";
        assertEquals(expected,
                     instance.toString());
    }

    @Test
    public void testToStringWithError() {
        Packet packet = Packet.create(PGN, 0, 0xFE, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        String expected = "DM5 from Engine #1 (0): OBD Compliance: Error (254), Active Codes: error, Previously Active Codes: error"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component        supported,     complete" + NL;
        expected += "    Fuel System                not supported, not complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant         supported,     complete" + NL;
        expected += "    Boost pressure control sys     supported,     complete" + NL;
        expected += "    Catalyst                       supported, not complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        expected += "    Diesel Particulate Filter  not supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Evaporative system             supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor             supported,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not supported, not complete" + NL;
        expected += "    Heated catalyst                supported,     complete" + NL;
        expected += "    NMHC converting catalyst   not supported, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Secondary air system       not supported, not complete";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(PGN, 0, 0xFF, 0xFF, 0xFF, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        String expected = "DM5 from Engine #1 (0): OBD Compliance: Not available (255), Active Codes: not available, Previously Active Codes: not available"
                + NL;
        expected += "Continuously Monitored System Support/Status:" + NL;
        expected += "    Comprehensive component    not supported,     complete" + NL;
        expected += "    Fuel System                not supported, not complete" + NL;
        expected += "    Misfire                    not supported,     complete" + NL;
        expected += "Non-continuously Monitored System Support/Status:" + NL;
        expected += "    A/C system refrigerant     not supported,     complete" + NL;
        expected += "    Boost pressure control sys not supported,     complete" + NL;
        expected += "    Catalyst                   not supported, not complete" + NL;
        expected += "    Cold start aid system      not supported,     complete" + NL;
        expected += "    Diesel Particulate Filter  not supported,     complete" + NL;
        expected += "    EGR/VVT system             not supported,     complete" + NL;
        expected += "    Evaporative system         not supported, not complete" + NL;
        expected += "    Exhaust Gas Sensor         not supported,     complete" + NL;
        expected += "    Exhaust Gas Sensor heater  not supported, not complete" + NL;
        expected += "    Heated catalyst            not supported,     complete" + NL;
        expected += "    NMHC converting catalyst   not supported, not complete" + NL;
        expected += "    NOx catalyst/adsorber      not supported, not complete" + NL;
        expected += "    Secondary air system       not supported, not complete";
        assertEquals(expected,
                     instance.toString());
    }

    @Test
    public void testValues() {
        Packet packet = Packet.create(PGN, 0, 0, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(1, instance.getSpnValues(1218).size());
        assertEquals(1, instance.getSpnValues(1219).size());
        assertEquals(1, instance.getSpnValues(1220).size());
        assertEquals(6, instance.getSpnValues(1221).size());
        assertEquals(26, instance.getSpnValues(1222).size());

        // verify names match
        assertEquals(instance.getContinuouslyMonitoredSystems()
                             .stream()
                             .flatMap(s -> Stream.of(s.getName().trim() + " Support", s.getName().trim() + " Status"))
                             .collect(Collectors.toList()),
                     instance.getSpnValues(1221).stream().map(v -> v.getLabel()).collect(Collectors.toList()));
        assertEquals(instance.getNonContinuouslyMonitoredSystems()
                             .stream()
                             .flatMap(s -> Stream.of(s.getName().trim() + " Support", s.getName().trim() + " Status"))
                             .collect(Collectors.toList()),
                     instance.getSpnValues(1222).stream().map(v -> v.getLabel()).collect(Collectors.toList()));
    }
}
