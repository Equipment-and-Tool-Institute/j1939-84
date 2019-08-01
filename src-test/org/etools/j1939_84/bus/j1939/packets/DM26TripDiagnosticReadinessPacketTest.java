/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import org.etools.j1939_84.bus.Packet;

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
    public void testEqualsContinouslyMonitoredSystems() {
        super.testEqualsContinouslyMonitoredSystems();
    }

    @Test
    @Override
    public void testEqualsWithObject() {
        super.testEqualsWithObject();
    }

    @Test
    @Override
    public void testGetContinouslyMonitoredSystemsComprehensiveComponentMonitoring() {
        super.testGetContinouslyMonitoredSystemsComprehensiveComponentMonitoring();
    }

    @Test
    @Override
    public void testGetContinouslyMonitoredSystemsFuelSystemMonitoring() {
        super.testGetContinouslyMonitoredSystemsFuelSystemMonitoring();
    }

    @Test
    @Override
    public void testGetContinouslyMonitoredSystemsMisfireMonitoring() {
        super.testGetContinouslyMonitoredSystemsMisfireMonitoring();
    }

    @Test
    @Override
    public void testGetMonitoredSystems() {
        super.testGetMonitoredSystems();
    }

    @Test
    @Override
    public void testGetNonContinouslyMonitoredSystems() {
        super.testGetNonContinouslyMonitoredSystems();
    }

    @Test
    public void testGetTimeSinceEngineStart() {
        Packet packet = Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(5643, instance.getTimeSinceEngineStart(), 0.0);
    }

    @Test
    public void testGetTimeSinceEngineStartWithError() {
        Packet packet = Packet.create(0, 0, 0x00, 0xFE, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(ParsedPacket.ERROR, instance.getTimeSinceEngineStart(), 0.0);
    }

    @Test
    public void testGetTimeSinceEngineStartWithNA() {
        Packet packet = Packet.create(0, 0, 0x00, 0xFF, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(ParsedPacket.NOT_AVAILABLE, instance.getTimeSinceEngineStart(), 0.0);
    }

    @Test
    public void testGetWarmUpsSinceClear() {
        Packet packet = Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals(33, instance.getWarmUpsSinceClear());
    }

    @Test
    public void testGetWarmUpsSinceClearWithError() {
        Packet packet = Packet.create(0, 0, 11, 22, 0xFE, 44, 55, 66, 77, 88);
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
    @Override
    public void testNotEqualsNonContinouslyMonitoredSystemsCompleted() {
        super.testNotEqualsNonContinouslyMonitoredSystemsCompleted();
    }

    @Test
    @Override
    public void testNotEqualsNonContinouslyMonitoredSystemsSupported() {
        super.testNotEqualsNonContinouslyMonitoredSystemsSupported();
    }

    @Test
    @Override
    public void testNotEqualsSourceAddress() {
        super.testNotEqualsSourceAddress();
    }

    @Test
    public void testNotEqualsTimeSinceClear() {
        Packet packet1 = Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(0, 0, 00, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsTimeSinceClearByte2() {
        Packet packet1 = Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(0, 0, 11, 00, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testNotEqualsWarmUps() {
        Packet packet1 = Packet.create(0, 0, 11, 22, 33, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance1 = new DM26TripDiagnosticReadinessPacket(packet1);
        Packet packet2 = Packet.create(0, 0, 11, 22, 00, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance2 = new DM26TripDiagnosticReadinessPacket(packet2);

        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void testPGN() {
        assertEquals(64952, DM26TripDiagnosticReadinessPacket.PGN);
    }

    @Test
    public void testToString() {
        Packet packet = Packet.create(0, 0, 11, 22, 20, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: 20, Time Since Engine Start: 5,643 seconds",
                instance.toString());
    }

    @Test
    public void testToStringWithError() {
        Packet packet = Packet.create(0, 0, 00, 0xFE, 0xFE, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: error, Time Since Engine Start: error", instance.toString());
    }

    @Test
    public void testToStringWithNA() {
        Packet packet = Packet.create(0, 0, 00, 0xFF, 0xFF, 44, 55, 66, 77, 88);
        DM26TripDiagnosticReadinessPacket instance = new DM26TripDiagnosticReadinessPacket(packet);
        assertEquals("DM26 from Engine #1 (0): Warm-ups: not available, Time Since Engine Start: not available",
                instance.toString());
    }
}
