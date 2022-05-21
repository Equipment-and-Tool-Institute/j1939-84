/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests the {@link DM1ActiveDTCsPacket} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM1ActiveDTCsPacketTest {

    private DM1ActiveDTCsPacket instance;

    @Before
    public void setUp() {
        DiagnosticTroubleCode dtc1 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        DiagnosticTroubleCode dtc2 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        DiagnosticTroubleCode dtc3 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        instance = DM1ActiveDTCsPacket.create(0, OFF, SLOW_FLASH, OFF, FAST_FLASH, dtc1, dtc2, dtc3);
    }

    @Test
    public void testDiagnosticTroubleCodePacket() {
        DM1ActiveDTCsPacket copy = new DM1ActiveDTCsPacket(
                Packet.create(65226,
                              0x00,
                              0x11,
                              0xCD,
                              0x61,
                              0x02,
                              0x13,
                              0x00,
                              0x21,
                              0x06,
                              0x1F,
                              0x00,
                              0xEE,
                              0x10,
                              0x04,
                              0x00));
        assertEquals(copy, instance);
    }

    @Test
    public void testDM1ActiveDTCsPacket() {
        assertEquals(0x00, instance.getSourceAddress());
        assertEquals("18FECA00 [14] 11 CD 61 02 13 00 21 06 1F 00 EE 10 04 00", instance.getPacket().toString());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getAmberWarningLampStatus()}.
     */
    @Test
    public void testGetAmberWarningLampStatus() {
        assertEquals(OFF, instance.getAmberWarningLampStatus());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getDtcs()}.
     */
    @Test
    public void testGetDtcs() {
        List<DiagnosticTroubleCode> actual = instance.getDtcs();
        assertEquals(609, actual.get(0).getSuspectParameterNumber());
        assertEquals(1569, actual.get(1).getSuspectParameterNumber());
        assertEquals(4334, actual.get(2).getSuspectParameterNumber());
        assertEquals(3, instance.getDtcs().size());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getMalfunctionIndicatorLampStatus()}.
     */
    @Test
    public void testGetMalfunctionIndicatorLampStatus() {
        assertEquals(OFF, instance.getMalfunctionIndicatorLampStatus());
    }

    /**
     * Test method for
     * {@link DM1ActiveDTCsPacket#getName()}.
     */
    @Test
    public void testGetName() {
        assertEquals("DM1", instance.getName());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getProtectLampStatus()}.
     */
    @Test
    public void testGetProtectLampStatus() {
        assertEquals(FAST_FLASH, instance.getProtectLampStatus());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getRedStopLampStatus()}.
     */
    @Test
    public void testGetRedStopLampStatus() {
        assertEquals(SLOW_FLASH, instance.getRedStopLampStatus());
    }

    @Test
    public void testPGN() {
        assertEquals(65226, DM1ActiveDTCsPacket.PGN);
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#toString()}.
     */
    @Test
    public void testToString() {
        String expected = "DM1 from Engine #1 (0): MIL: off, RSL: slow flash, AWL: off, PL: fast flash" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times";
        assertEquals(expected, instance.toString());
    }

}
