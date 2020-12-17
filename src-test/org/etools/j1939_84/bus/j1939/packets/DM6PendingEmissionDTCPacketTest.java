/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM6PendingEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class DM6PendingEmissionDTCPacketTest {

    @Test
    public void testAllLampsOff() {
        // Test all lights on
        Packet packet = Packet.create(0, 0x00, 0x00, 0xFF, 0x01, 0x019, 0x00, 0x00, 0x00, 0x00);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.OFF, instance.getProtectLampStatus());
        assertEquals(LampStatus.OFF, instance.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, instance.getMalfunctionIndicatorLampStatus());
        StringBuilder expected = new StringBuilder(
                "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL);
        expected.append(
                "DTC:  (6401) Engine Cylinder 2 Peak Pressure Data Valid But Above Normal Operational Range - Most Severe Level (0) 0 times");
        assertEquals(expected.toString(), instance.toString());
        assertEquals(65231, DM6PendingEmissionDTCPacket.PGN);
    }

    @Test
    public void testGetName() {
        // Test all lights on at fast flash
        Packet packet = Packet.create(0, 0, 0x55, 0x55, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(LampStatus.FAST_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getProtectLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getRedStopLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals("DM6", new DM6PendingEmissionDTCPacket(packet).getName());
        StringBuilder expected = new StringBuilder(
                "DM6 from Engine #1 (0): MIL: fast flash, RSL: fast flash, AWL: fast flash, PL: fast flash, No DTCs");
        assertEquals(expected.toString(), instance.toString());
    }

    @Test
    public void testPGN() {
        // Test all lights on
        Packet packet = Packet.create(0, 0x00, 0x55, 0xFF, 0x01, 0x013, 0x00, 0x00, 0x00, 0x00);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(LampStatus.ON, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.ON, instance.getProtectLampStatus());
        assertEquals(LampStatus.ON, instance.getRedStopLampStatus());
        assertEquals(LampStatus.ON, instance.getMalfunctionIndicatorLampStatus());
        StringBuilder expected = new StringBuilder(
                "DM6 from Engine #1 (0): MIL: on, RSL: on, AWL: on, PL: on" + NL);
        expected.append(
                "DTC:  (4865) Special Ignitor Loop 38 - Resistance Data Valid But Above Normal Operational Range - Most Severe Level (0) 0 times");
        assertEquals(expected.toString(), instance.toString());
        assertEquals(65231, DM6PendingEmissionDTCPacket.PGN);
    }

    @Test
    public void testToString() {
        // Test two on and rest off
        Packet packet = Packet.create(0, 0, 0x11, 0xCD, 0x01, 0x01, 0x01, 0x01, 0x31, 0x11);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getProtectLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, instance.getMalfunctionIndicatorLampStatus());
        StringBuilder expected = new StringBuilder(
                "DM6 from Engine #1 (0): MIL: off, RSL: slow flash, AWL: off, PL: fast flash");
        expected.append(NL)
                .append("DTC:  (257) Cold Restart Of Specific Component Data Valid But Below Normal Operational Range - Most Severe Level (1) 1 times");
        assertEquals(expected.toString(), instance.toString());
    }

}
