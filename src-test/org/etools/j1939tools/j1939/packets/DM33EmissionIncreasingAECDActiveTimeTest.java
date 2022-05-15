/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the
 * {@link DM33EmissionIncreasingAECDActiveTime} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM33EmissionIncreasingAECDActiveTimeTest {

    private DM33EmissionIncreasingAECDActiveTime instance;

    @Before
    public void setUp() {
        EngineHoursTimer timer1 = EngineHoursTimer.create(1, 68395, 771115);
        EngineHoursTimer timer2 = EngineHoursTimer.create(2, 0xFE000000, 0xFF000000);
        EngineHoursTimer timer3 = EngineHoursTimer.create(3, 0xFE000000, 199468);
        EngineHoursTimer timer4 = EngineHoursTimer.create(4, 0xFE000000, 0xFF000000);
        instance = DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer1, timer2, timer3, timer4);
    }

    @Test
    public void testGetEiAecdEngineHoursTimers() {
        assertEquals(4, instance.getEiAecdEngineHoursTimers().size());
        int[] timer0Data = { 0x01, 0x2B, 0x0B, 0x01, 0x00, 0x2B, 0xC4, 0x0B, 0x00 };
        EngineHoursTimer expectedEngineHoursTimer0 = new EngineHoursTimer(timer0Data);
        assertEquals(expectedEngineHoursTimer0, instance.getEiAecdEngineHoursTimers().get(0));
        int[] timer1Data = { 0x02, 0xFE, 0xFE, 0xFE, 0xFE, 0xFF, 0xFF, 0xFF, 0xFF };
        EngineHoursTimer expectedEngineHoursTimer1 = new EngineHoursTimer(timer1Data);
        assertEquals(expectedEngineHoursTimer1, instance.getEiAecdEngineHoursTimers().get(1));
        int[] timer2Data = { 0x03, 0xFE, 0xFE, 0xFE, 0xFE, 0x2C, 0x0B, 0x03, 0x00 };
        EngineHoursTimer expectedEngineHoursTimer2 = new EngineHoursTimer(timer2Data);
        assertEquals(expectedEngineHoursTimer2, instance.getEiAecdEngineHoursTimers().get(2));
        int[] timer3Data = { 0x04, 0xFF, 0xFF, 0xFF, 0xFE, 0xFE, 0xFE, 0xFE, 0xFF };
        EngineHoursTimer expectedEngineHoursTimer3 = new EngineHoursTimer(timer3Data);
        assertEquals(expectedEngineHoursTimer3, instance.getEiAecdEngineHoursTimers().get(3));
    }

    @Test
    public void testGetName() {
        assertEquals("DM33 Emission Increasing AECD Active Time", instance.getName());
    }

    @Test
    public void testToString() {
        String expected = "DM33 Emission Increasing AECD Active Time from Engine #1 (0): [" + NL;
        expected += "  EI-AECD Number = 1: Timer 1 = 68395 minutes; Timer 2 = 771115 minutes" + NL;
        expected += "  EI-AECD Number = 2: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "  EI-AECD Number = 3: Timer 1 = errored; Timer 2 = 199468 minutes" + NL;
        expected += "  EI-AECD Number = 4: Timer 1 = errored; Timer 2 = n/a" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

}
