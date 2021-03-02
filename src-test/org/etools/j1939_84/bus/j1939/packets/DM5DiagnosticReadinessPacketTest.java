/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.etools.j1939_84.bus.Packet;
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
    @TestDoc()
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
        Packet packet = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(11, instance.getActiveCodeCount());
    }

    @Test
    public void testGetActiveCodeCountWithError() {
        Packet packet = Packet.create(65230, 0, 0xFE, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFE, instance.getActiveCodeCount());
    }

    @Test
    public void testGetActiveCodeCountWithNA() {
        Packet packet = Packet.create(65230, 0, 0xFF, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFF, instance.getActiveCodeCount());
    }

    @Test
    public void testGetOBDCompliance() {
        Packet packet = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(33, instance.getOBDCompliance());
    }

    @Test
    public void testGetPreviouslyActiveCodeCount() {
        Packet packet = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(22, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testGetPreviouslyActiveCodeCountWithError() {
        Packet packet = Packet.create(65230, 0, 11, 0xFE, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFE, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testGetPreviouslyActiveCodeCountWithNA() {
        Packet packet = Packet.create(65230, 0, 11, 0xFF, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals((byte) 0xFF, instance.getPreviouslyActiveCodeCount());
    }

    @Test
    public void testisHdObdComplianceFalse() {
        Packet packet = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertFalse(instance.isHdObd());
    }

    @Test
    public void testisHdObdComplianceTrue19() {
        Packet packet = Packet.create(65230, 0, 11, 22, 19, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertTrue(instance.isHdObd());
    }

    @Test
    public void testisHdObdComplianceTrue20() {
        Packet packet = Packet.create(65230, 0, 11, 22, 20, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertTrue(instance.isHdObd());
    }

    @Test
    public void testNotEqualsActiveCount() {
        Packet packet1 = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(65230, 0, 00, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsOBDCompliance() {
        Packet packet1 = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(65230, 0, 11, 22, 00, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsPreviouslyActiveCount() {
        Packet packet1 = Packet.create(65230, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance1 = new DM5DiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(65230, 0, 11, 00, 33, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance2 = new DM5DiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testPGN() {
        assertEquals(65230, DM5DiagnosticReadinessPacket.PGN);
    }

    @Test
    public void testToString() {
        Packet packet = Packet.create(65230, 0, 11, 22, 20, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(
                     "DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22",
                     instance.toString());
    }

    @Test
    public void testToStringWithAllOBDComplianceValues() {
        Map<Integer, String> testCases = new HashMap<>();
        testCases.put(1, "OBD II");
        testCases.put(2, "OBD");
        testCases.put(3, "OBD and OBD II");
        testCases.put(4, "OBD I");
        testCases.put(5, "Not intended to meet OBD II requirements");
        testCases.put(6, "EOBD");
        testCases.put(7, "EOBD and OBD II");
        testCases.put(8, "EOBD and OBD");
        testCases.put(9, "EOBD, OBD and OBD II");
        testCases.put(10, "JOBD");
        testCases.put(11, "JOBD and OBD II");
        testCases.put(12, "JOBD and EOBD");
        testCases.put(13, "JOBD, EOBD and OBD II");
        testCases.put(14, "Heavy Duty Vehicles (EURO IV) B1");
        testCases.put(15, "Heavy Duty Vehicles (EURO V) B2");
        testCases.put(16, "Heavy Duty Vehicles (EURO EEC) C (gas engines)");
        testCases.put(17, "EMD");
        testCases.put(18, "EMD+");
        testCases.put(19, "HD OBD P");
        testCases.put(20, "HD OBD");
        testCases.put(21, "WWH OBD");
        testCases.put(22, "OBD II");
        testCases.put(23, "HD EOBD");
        testCases.put(24, "Reserved for SAE/Unknown");
        testCases.put(25, "OBD-M (SI-SD/I)");
        testCases.put(26, "EURO VI");
        testCases.put(34, "OBD, OBD II, HD OBD");
        testCases.put(35, "OBD, OBD II, HD OBD P");
        testCases.put(251, "value 251");
        testCases.put(252, "value 252");
        testCases.put(253, "value 253");
        testCases.put(254, "Error");
        testCases.put(255, "Not available");

        for (Entry<Integer, String> testCase : testCases.entrySet()) {
            int value = testCase.getKey();
            String expected = testCase.getValue();
            DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(
                                                                                     Packet.create(65230,
                                                                                                   0,
                                                                                                   11,
                                                                                                   22,
                                                                                                   value,
                                                                                                   44,
                                                                                                   55,
                                                                                                   66,
                                                                                                   77,
                                                                                                   88));
            assertEquals("DM5 from Engine #1 (0): OBD Compliance: " + expected + " (" + value
                    + "), Active Codes: 11, Previously Active Codes: 22", instance.toString());
        }
    }

    @Test
    public void testToStringWithError() {
        Packet packet = Packet.create(65230, 0, 0xFE, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(
                     "DM5 from Engine #1 (0): OBD Compliance: Error (254), Active Codes: error, Previously Active Codes: error",
                     instance.toString());
    }

    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(65230, 0, 0xFF, 0xFF, 0xFF, 44, 55, 66, 77, 88);
        DM5DiagnosticReadinessPacket instance = new DM5DiagnosticReadinessPacket(packet);
        assertEquals(
                     "DM5 from Engine #1 (0): OBD Compliance: Not available (255), Active Codes: not available, Previously Active Codes: not available",
                     instance.toString());
    }

}
