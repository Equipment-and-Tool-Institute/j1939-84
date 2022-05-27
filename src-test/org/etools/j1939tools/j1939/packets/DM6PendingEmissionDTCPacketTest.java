/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM6PendingEmissionDTCPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM6PendingEmissionDTCPacketTest {

    @Test
    public void testAllLampsOff() {
        // Test all lights on
        Packet packet = Packet.create(0, 0x00, 0x00, 0xFF, 0x01, 0x019, 0x00, 0x00, 0x00, 0x00);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(OFF, instance.getAmberWarningLampStatus());
        assertEquals(OFF, instance.getProtectLampStatus());
        assertEquals(OFF, instance.getRedStopLampStatus());
        assertEquals(OFF, instance.getMalfunctionIndicatorLampStatus());
        String expected = "DM6 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off" + NL +
                "DTC 6401:0 - Engine Cylinder 2 Peak Pressure, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times";
        assertEquals(expected, instance.toString());
        assertEquals(65231, DM6PendingEmissionDTCPacket.PGN);
    }

    @Test
    public void testGetName() {
        // Test all lights on at fast flash
        Packet packet = Packet.create(0, 0, 0x55, 0x55, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(FAST_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(FAST_FLASH, instance.getProtectLampStatus());
        assertEquals(FAST_FLASH, instance.getRedStopLampStatus());
        assertEquals(FAST_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals("DM6", new DM6PendingEmissionDTCPacket(packet).getName());
        assertEquals(
                     "DM6 from Engine #1 (0): MIL: fast flash, RSL: fast flash, AWL: fast flash, PL: fast flash, No DTCs",
                     instance.toString());
    }

    @Test
    public void testPGN() {
        // Test all lights on
        Packet packet = Packet.create(0, 0x00, 0x55, 0xFF, 0x01, 0x013, 0x00, 0x00, 0x00, 0x00);
        DM6PendingEmissionDTCPacket instance = new DM6PendingEmissionDTCPacket(packet);
        assertEquals(ON, instance.getAmberWarningLampStatus());
        assertEquals(ON, instance.getProtectLampStatus());
        assertEquals(ON, instance.getRedStopLampStatus());
        assertEquals(ON, instance.getMalfunctionIndicatorLampStatus());
        String expected = "DM6 from Engine #1 (0): MIL: on, RSL: on, AWL: on, PL: on" + NL +
                "DTC 4865:0 - Special Ignitor Loop 38 - Resistance, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times";
        assertEquals(expected, instance.toString());
        assertEquals(65231, DM6PendingEmissionDTCPacket.PGN);
    }

    @Test
    public void testToString() {
        // Test two on and rest off
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(257, 1, 1, 1);
        var instance = DM6PendingEmissionDTCPacket.create(0, OFF, SLOW_FLASH, OFF, FAST_FLASH, dtc);

        assertEquals(OFF, instance.getAmberWarningLampStatus());
        assertEquals(FAST_FLASH, instance.getProtectLampStatus());
        assertEquals(SLOW_FLASH, instance.getRedStopLampStatus());
        assertEquals(OFF, instance.getMalfunctionIndicatorLampStatus());
        String expected = "DM6 from Engine #1 (0): MIL: off, RSL: slow flash, AWL: off, PL: fast flash" + NL +
                "DTC 257:1 - Cold Restart Of Specific Component, Data Valid But Below Normal Operational Range - Most Severe Level - 1 times";
        assertEquals(expected, instance.toString());
    }

}
