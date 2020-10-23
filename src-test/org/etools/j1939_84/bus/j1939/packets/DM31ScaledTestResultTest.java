/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
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
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State
        };
        Packet packet = Packet.create(DM31ScaledTestResults.PGN,
                0,
                data);
        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals(1, instance.getDtcLampStatuses().size());
        String expected = "DM31 from Engine #1 (0): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC: Controller #2 (609) Received Network Data In Error (19) 1 times" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());

        DTCLampStatus dtcLampStatus0 = instance.getDtcLampStatuses().get(0);
        DiagnosticTroubleCode actualDTC = dtcLampStatus0.getDtcs();
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
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State

                0x21, // SPN least significant bit
                0x06, // SPN most significant bit
                0x1F, // Failure mode indicator
                0x23, // SPN Conversion Occurrence Count
                0x22, // Lamp Status/Support
                0xDD, // Lamp Status/State

                0xEE, // SPN least significant bit
                0x10, // SPN most significant bit
                0x04, // Failure mode indicator
                0x00, // SPN Conversion Occurrence Count
                0xAA, // Lamp Status/Support
                0x55);// Lamp Status/State

        DM31ScaledTestResults instance = new DM31ScaledTestResults(packet);
        assertEquals("DM31", instance.getName());
        List<DTCLampStatus> lampStatuses = instance.getDtcLampStatuses();
        assertEquals(0x03, lampStatuses.size());
        String expected = "DM31 from Engine #1 (0): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC: Controller #2 (609) Received Network Data In Error (19) 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC: Engine Protection Torque Derate (1569) Condition Exists (31) 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC: Aftertreatment 1 Diesel Exhaust Fluid Doser 1 Absolute Pressure (4334) Voltage Below Normal, Or Shorted To Low Source (4) 0 times"
                + NL;
        expected += "]";
        assertEquals(expected, instance.toString());

        DTCLampStatus lampStatus0 = lampStatuses.get(0);
        DiagnosticTroubleCode dtc0 = lampStatus0.getDtcs();
        assertEquals(0x01, dtc0.getConversionMethod());
        assertEquals(0x13, dtc0.getFailureModeIndicator());
        assertEquals(0x01, dtc0.getOccurrenceCount());
        assertEquals(0x261, dtc0.getSuspectParameterNumber());
        assertEquals(LampStatus.SLOW_FLASH, lampStatus0.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus0.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus0.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, instance.getDtcLampStatuses().get(0).getAmberWarningLampStatus());

        DTCLampStatus lampStatus1 = lampStatuses.get(1);
        DiagnosticTroubleCode dtc1 = lampStatus1.getDtcs();
        assertEquals(0x0, dtc1.getConversionMethod());
        assertEquals(0x1F, dtc1.getFailureModeIndicator());
        assertEquals(0x23, dtc1.getOccurrenceCount());
        assertEquals(0x0621, dtc1.getSuspectParameterNumber());
        assertEquals(LampStatus.OFF, lampStatus1.getMalfunctionIndicatorLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus1.getProtectLampStatus());
        assertEquals(LampStatus.OTHER, lampStatus1.getRedStopLampStatus());
        assertEquals(LampStatus.OFF, lampStatus1.getAmberWarningLampStatus());

        DTCLampStatus lampStatus2 = lampStatuses.get(2);
        DiagnosticTroubleCode dtc2 = lampStatus2.getDtcs();
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
