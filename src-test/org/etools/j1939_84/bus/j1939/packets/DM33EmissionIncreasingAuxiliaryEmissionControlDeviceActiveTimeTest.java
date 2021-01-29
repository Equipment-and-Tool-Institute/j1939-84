/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.etools.j1939_84.bus.Packet;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the
 * {@link DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTimeTest {

    private DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime instance;

    @Before
    public void setUp() {
        // 1 good one with two good timers
        byte[] data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00,
                // 1 with FE for timer 1 and FF for timer 2
                0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF,
                0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00,
                // 1 with FF for timer 1 and FE for timer 2
                0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFF };
        Packet packet = Packet.create(0, 0x00, data);
        instance = new DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime(packet);
    }

    @Test
    public void testGetEiAecdEngineHoursTimers() {
        assertEquals(4, instance.getEiAecdEngineHoursTimers().size());
        byte[] timer0Data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, (byte) 0xC4, 0x0B, 0x00 };
        EngineHoursTimer expectedEngineHoursTimer0 = new EngineHoursTimer(timer0Data);
        assertEquals(expectedEngineHoursTimer0, instance.getEiAecdEngineHoursTimers().get(0));
        byte[] timer1Data = { 0x02, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        EngineHoursTimer expectedEngineHoursTimer1 = new EngineHoursTimer(timer1Data);
        assertEquals(expectedEngineHoursTimer1, instance.getEiAecdEngineHoursTimers().get(1));
        byte[] timer2Data = { 0x03, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, 0x2C, 0x0B, 0x03, 0x00 };
        EngineHoursTimer expectedEngineHoursTimer2 = new EngineHoursTimer(timer2Data);
        assertEquals(expectedEngineHoursTimer2, instance.getEiAecdEngineHoursTimers().get(2));
        byte[] timer3Data = { 0x04, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, (byte) 0xFF };
        EngineHoursTimer expectedEngineHoursTimer3 = new EngineHoursTimer(timer3Data);
        assertEquals(expectedEngineHoursTimer3, instance.getEiAecdEngineHoursTimers().get(3));
    }

    @Test
    public void testGetName() {
        assertEquals("DM33 Emission Increasing AECD Active Time", instance.getName());
    }

    @Test
    public void testToString() {
        String expected = "DM33 Emission Increasing AECD Active Time from Engine #1 (0): {" + NL;
        expected += "EI-AECD Number = 1: Timer 1 = 68395 minutes; Timer 2 = 771115 minutes" + NL;
        expected += "EI-AECD Number = 2: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "EI-AECD Number = 3: Timer 1 = errored; Timer 2 = 199468 minutes" + NL;
        expected += "EI-AECD Number = 4: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "}" + NL;
        assertEquals(expected, instance.toString());
    }

}
