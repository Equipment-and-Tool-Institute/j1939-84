/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.etools.j1939_84.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM31ScaledTestResults} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
public class DM31ScaledTestResultTest {

    @Test
    public void testEmptyDTCs() {
        Packet packet = Packet.create(DM31ScaledTestResults.PGN,
                0);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        assertTrue(instance.getDtcLampStatuses().isEmpty());
        assertEquals(0, instance.getPacket().getBytes().length);
    }

    @Test
    public void testOneDTCs() {
        int[] data = {
                0x61,       // SPN least significant bit
                0x02,       // SPN most significant bit
                0x13,       // Failure mode indicator
                0x81,
                0x58,
                0x34
        };
        Packet packet = Packet.create(DM31ScaledTestResults.PGN,
                0,
                data);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals(1, instance.getDtcLampStatuses().size());

        DTCLampStatus dtcLampStatus0 = instance.getDtcLampStatuses().get(0);
        DiagnosticTroubleCode actualDTC = dtcLampStatus0.getDtc();
        assertEquals(0x0261, actualDTC.getSuspectParameterNumber());
        assertEquals(0x01, actualDTC.getConversionMethod());
        assertEquals(0x13, actualDTC.getFailureModeIndicator());
        assertEquals(0x01, actualDTC.getOccurrenceCount());
        assertEquals(LampStatus.SLOW_FLASH, dtcLampStatus0.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, dtcLampStatus0.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, dtcLampStatus0.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, dtcLampStatus0.getAmberWarningLampStatus());
    }

    @Test
    public void testPGN() {
        assertEquals(41728, DM31ScaledTestResults.PGN);
    }

    @Test
    public void testThreeDTCs() {
        Packet packet = Packet.create(DM31ScaledTestResults.PGN,
                0,
                0x61,       // SPN least significant bit
                0x02,       // SPN most significant bit
                0x13,       // Failure mode indicator
                0x81,
                0x58,
                0x34,

                0x21,       // SPN least significant bit
                0x06,       // SPN most significant bit
                0x1F,       // Failure mode indicator
                0x23,
                0x4A,
                0x34,

                0xEE,       // SPN least significant bit
                0x10,       // SPN most significant bit
                0x04,       // Failure mode indicator
                0x00,
                0x37,
                0x2A);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        List<DTCLampStatus> lampStatuses = instance.getDtcLampStatuses();
        assertEquals(0x03, lampStatuses.size());

        DTCLampStatus lampStatus0 = lampStatuses.get(0);
        DiagnosticTroubleCode dtc0 = lampStatus0.getDtc();
        assertEquals(0x01, dtc0.getConversionMethod());
        assertEquals(0x13, dtc0.getFailureModeIndicator());
        assertEquals(0x01, dtc0.getOccurrenceCount());
        assertEquals(0x261, dtc0.getSuspectParameterNumber());
        assertEquals(LampStatus.SLOW_FLASH, lampStatus0.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus0.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus0.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, instance.getDtcLampStatuses().get(0).getAmberWarningLampStatus());

        DTCLampStatus lampStatus1 = lampStatuses.get(1);
        DiagnosticTroubleCode dtc1 = lampStatus1.getDtc();
        assertEquals(0x0, dtc1.getConversionMethod());
        assertEquals(0x1F, dtc1.getFailureModeIndicator());
        assertEquals(0x23, dtc1.getOccurrenceCount());
        assertEquals(0x0621, dtc1.getSuspectParameterNumber());
        assertEquals(LampStatus.OFF, lampStatus1.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus1.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus1.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, lampStatus1.getAmberWarningLampStatus());

        DTCLampStatus lampStatus2 = lampStatuses.get(2);
        DiagnosticTroubleCode dtc2 = lampStatus2.getDtc();
        assertEquals(0x00, dtc2.getConversionMethod());
        assertEquals(0x04, dtc2.getFailureModeIndicator());
        assertEquals(0x00, dtc2.getOccurrenceCount());
        assertEquals(0x10EE, dtc2.getSuspectParameterNumber());
        assertEquals(LampStatus.OTHER, lampStatus2.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus2.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus2.getRedStopLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus2.getAmberWarningLampStatus());
    }

}
