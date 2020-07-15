/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer.ERROR;
import static org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer.NOT_AVAILABLE;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link EngineHoursTimer} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class EngineHoursTimerTest {

    private EngineHoursTimer instance;

    @Before
    public void setUp() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFF, (byte) 0xFF };
        instance = new EngineHoursTimer(data);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#EngineHoursTimer(byte[])}.
     */
    @Test
    public void testEngineHoursTimerError() {
        byte[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0xFE, (byte) 0xFE,
                (byte) 0xFE, (byte) 0xFE };
        EngineHoursTimer errorInstance = new EngineHoursTimer(data);
        assertEquals(ERROR, errorInstance.getEiAecdTimer2());
        StringBuilder expected = new StringBuilder("EngineHoursTimer");
        expected.append(NL).append("  EI-AECD Number = 1")
                .append(NL)
                .append("  EI-AECD Engine Hours Timer 1 = 0")
                .append(NL)
                .append("  EI-AECD Engine Hours Timer 2 = errored");
        assertEquals(expected.toString(), errorInstance.toString());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#EngineHoursTimer(byte[])}.
     */
    @Test
    public void testEngineHoursTimerNoError() {
        // 0B 00 00 00 00
        byte[] data1 = { 0x38, 0x1A, 0x00, 0x00, 0x00, 0x0B, 0x00, 0x00, 0x00 };
        EngineHoursTimer noErrorInstance = new EngineHoursTimer(data1);

        assertEquals(0x38, noErrorInstance.getEiAecdNumber());
        assertEquals(0x1A, noErrorInstance.getEiAecdTimer1());
        assertEquals(0x0B, noErrorInstance.getEiAecdTimer2());

        StringBuilder expected = new StringBuilder("EngineHoursTimer");
        expected.append(NL).append("  EI-AECD Number = 56").append(NL).append("  EI-AECD Engine Hours Timer 1 = 26")
                .append(NL).append("  EI-AECD Engine Hours Timer 2 = 11");
        assertEquals(expected.toString(), noErrorInstance.toString());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#getEiAecdNumber()}.
     */
    @Test
    public void testGetEiAecdNumber() {
        assertEquals(1, instance.getEiAecdNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#getEiAecdTimer1()}.
     */
    @Test
    public void testGetEiAecdTimer1() {
        assertEquals(0, instance.getEiAecdTimer1());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#getEiAecdTimer2()}.
     */
    @Test
    public void testGetEiAecdTimer2() {
        assertEquals(NOT_AVAILABLE, instance.getEiAecdTimer2());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer#toString()}.
     */
    @Test
    public void testToString() {
        StringBuilder expected = new StringBuilder("EngineHoursTimer");
        expected.append(NL)
                .append("  EI-AECD Number = 1")
                .append(NL)
                .append("  EI-AECD Engine Hours Timer 1 = 0")
                .append(NL)
                .append("  EI-AECD Engine Hours Timer 2 = n/a");
        assertEquals(expected.toString(), instance.toString());
    }

}