/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939tools.J1939tools.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939tools.j1939.packets.LampStatus;
import org.junit.Test;

/**
 * The unit test for {@link DM27AllPendingDTCsPacket}
 *
 * @author Marianne Schaefer (marianne.schaefer@gmail.com)
 */
public class DM27AllPendingDTCsPacketTest {

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getAmberWarningLampStatus()}.
     */
    @Test
    public void testGetAmberWarningLampStatusFastFlash() {
        int[] data = new int[] { 0x04, 0x04, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.FAST_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getAmberWarningLampStatus());
    }

    @Test
    public void testGetAmberWarningLampStatusOff() {
        int[] data = new int[] { 0x00, 0x0C, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.OFF, instance.getAmberWarningLampStatus());
    }

    @Test
    public void testGetAmberWarningLampStatusOn() {
        int[] data = new int[] { 0x04, 0x0C, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.ON, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.ON, instance.getAmberWarningLampStatus());
    }

    @Test
    public void testGetAmberWarningLampStatusSlowFlash() {
        int[] data = new int[] { 0x04, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.SLOW_FLASH, instance.getAmberWarningLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getAmberWarningLampStatus());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getDtcs()}.
     */
    @Test
    public void testGetDtcsEmptyWithEightBytes() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(0, instance.getDtcs().size());
    }

    @Test
    public void testGetDtcsEmptyWithGrandfathered() {
        int[] data = new int[] { 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);
        assertEquals(0, instance.getDtcs().size());
    }

    @Test
    public void testGetDtcsEmptyWithSixBytes() {
        int[] data = new int[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(0, instance.getDtcs().size());
    }

    @Test
    public void testGetDtcsOneWithEightBytes() {
        int[] data = new int[] { 0x00, 0xFF, 0x61, 0x02, 0x13, 0x00, 0xFF, 0xFF };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        List<DiagnosticTroubleCode> dtcs = instance.getDtcs();
        assertEquals(1, dtcs.size());
        assertEquals(609, dtcs.get(0).getSuspectParameterNumber());
    }

    @Test
    public void testGetDtcsOneWithSixBytes() {
        int[] data = new int[] { 0x00, 0xFF, 0x61, 0x02, 0x13, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        List<DiagnosticTroubleCode> dtcs = instance.getDtcs();
        assertEquals(1, dtcs.size());
        assertEquals(609, dtcs.get(0).getSuspectParameterNumber());
    }

    @Test
    public void testGetDtcsThree() {
        int[] data = new int[] { 0x00, 0xFF, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06, 0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        List<DiagnosticTroubleCode> dtcs = instance.getDtcs();
        assertEquals(3, dtcs.size());
        assertEquals(609, dtcs.get(0).getSuspectParameterNumber());
        assertEquals(1569, dtcs.get(1).getSuspectParameterNumber());
        assertEquals(4334, dtcs.get(2).getSuspectParameterNumber());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getMalfunctionIndicatorLampStatus()}.
     */
    @Test
    public void testGetMalfunctionIndicatorLampStatusFastFlash() {
        int[] data = new int[] { 0x40, 0x40, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.FAST_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getMalfunctionIndicatorLampStatus());
    }

    @Test
    public void testGetMalfunctionIndicatorLampStatusOff() {
        int[] data = new int[] { 0x00, 0x0C0, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.OFF, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OFF, instance.getMalfunctionIndicatorLampStatus());
    }

    @Test
    public void testGetMalfunctionIndicatorLampStatusOn() {
        int[] data = new int[] { 0x40, 0xC0, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.ON, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.ON, instance.getMalfunctionIndicatorLampStatus());
    }

    @Test
    public void testGetMalfunctionIndicatorLampStatusSlowFlash() {
        int[] data = new int[] { 0x40, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getMalfunctionIndicatorLampStatus());
    }

    /**
     * Test method for
     * {@link DM27AllPendingDTCsPacket#getName()}.
     */
    @Test
    public void testGetName() {
        int[] data = new int[] { 0x04, 0x04, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals("DM27", instance.getName());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getProtectLampStatus()}.
     */
    @Test
    public void testGetProtectLampStatusFastFlash() {
        int[] data = new int[] { 0x01, 0x01, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.FAST_FLASH, instance.getProtectLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getProtectLampStatus());
    }

    @Test
    public void testGetProtectLampStatusOff() {
        int[] data = new int[] { 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.OFF, instance.getProtectLampStatus());
        assertEquals(LampStatus.OFF, instance.getProtectLampStatus());
    }

    @Test
    public void testGetProtectLampStatusOn() {
        int[] data = new int[] { 0x01, 0x03, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.ON, instance.getProtectLampStatus());
        assertEquals(LampStatus.ON, instance.getProtectLampStatus());
    }

    @Test
    public void testGetProtectLampStatusSlowFlash() {
        int[] data = new int[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.SLOW_FLASH, instance.getProtectLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getProtectLampStatus());
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#getRedStopLampStatus()}.
     */
    @Test
    public void testGetRedStopLampStatusFastFlash() {
        int[] data = new int[] { 0x10, 0x10, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.FAST_FLASH, instance.getRedStopLampStatus());
        assertEquals(LampStatus.FAST_FLASH, instance.getRedStopLampStatus());
    }

    @Test
    public void testGetRedStopLampStatusOn() {
        int[] data = new int[] { 0x10, 0x30, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.ON, instance.getRedStopLampStatus());
        assertEquals(LampStatus.ON, instance.getRedStopLampStatus());
    }

    @Test
    public void testGetRedStopLampStatusSlowFlash() {
        int[] data = new int[] { 0x10, 0x00, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);

        assertEquals(LampStatus.SLOW_FLASH, instance.getRedStopLampStatus());
        assertEquals(LampStatus.SLOW_FLASH, instance.getRedStopLampStatus());
    }

    /**
     * Test method for
     * {@link DM27AllPendingDTCsPacket#PGN()}.
     */
    @Test
    public void testPGN() {

        assertEquals(64898, DM27AllPendingDTCsPacket.PGN);
    }

    /**
     * Test method for
     * {@link DiagnosticTroubleCodePacket#toString()}.
     */
    @Test
    public void testToString() {
        int[] data = new int[] { 0x54, 0x4F, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06, 0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);
        String expected = "DM27 from Engine #1 (0): MIL: fast flash, RSL: slow flash, AWL: on, PL: off" + NL +
                "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL +
                "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL +
                "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times";
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testToStringNoDtcs() {
        int[] data = new int[] { 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00 };
        Packet packet = Packet.create(DM27AllPendingDTCsPacket.PGN, 0x00, data);
        DM27AllPendingDTCsPacket instance = new DM27AllPendingDTCsPacket(packet);
        String expected = "DM27 from Engine #1 (0): MIL: off, RSL: off, AWL: off, PL: off, No DTCs";
        assertEquals(expected, instance.toString());
    }

}
