/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.OTHER;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM31DtcToLampAssociation} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM31DtcToLampAssociationTest {

    @Test
    public void testEmptyDTCs() {
        DM31DtcToLampAssociation instance = DM31DtcToLampAssociation.create(0, 0);
        assertEquals("DM31", instance.getName());
        assertTrue(instance.getDtcLampStatuses().isEmpty());
        assertEquals(8, instance.getPacket().getBytes().length);
    }

    @Test
    public void testOneDTCs() {
        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x73, // Lamp Status/Support
                0x2E, // Lamp Status/State
        };
        Packet packet = Packet.create(DM31DtcToLampAssociation.PGN,
                                      0,
                                      data);
        DM31DtcToLampAssociation instance = new DM31DtcToLampAssociation(packet);
        assertEquals(1, instance.getDtcLampStatuses().size());
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(609, 19, 1, 1);
        DTCLampStatus dtcLampStatus = DTCLampStatus.create(dtc, OFF, SLOW_FLASH, OTHER, OTHER);
        DM31DtcToLampAssociation instance2 = DM31DtcToLampAssociation.create(0,
                                                                             0,
                                                                             dtcLampStatus);
        assertEquals(instance, instance2);

        String expected = "DM31 from Engine #1 (0): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 1 times" + NL;
        expected += "]";
        assertEquals(expected, instance.toString());

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
        assertEquals(41728, DM31DtcToLampAssociation.PGN);
    }

    @Test
    public void testThreeDTCs() {
        Packet packet = Packet.create(DM31DtcToLampAssociation.PGN,
                                      0,
                                      0x61, // SPN least significant bit
                                      0x02, // SPN most significant bit
                                      0x13, // Failure mode indicator
                                      0x81, // SPN Conversion Occurrence Count
                                      0x73, // Lamp Status/Support
                                      0x2E, // Lamp Status/State

                                      0x21, // SPN least significant bit
                                      0x06, // SPN most significant bit
                                      0x1F, // Failure mode indicator
                                      0x23, // SPN Conversion Occurrence Count
                                      0x33, // Lamp Status/Support
                                      0xEE, // Lamp Status/State

                                      0xEE, // SPN least significant bit
                                      0x10, // SPN most significant bit
                                      0x04, // Failure mode indicator
                                      0x00, // SPN Conversion Occurrence Count
                                      0xFF, // Lamp Status/Support
                                      0xAA);// Lamp Status/State

        DM31DtcToLampAssociation instance = new DM31DtcToLampAssociation(packet);
        assertEquals("DM31", instance.getName());
        List<DTCLampStatus> lampStatuses = instance.getDtcLampStatuses();
        assertEquals(0x03, lampStatuses.size());
        String expected = "DM31 from Engine #1 (0): " + NL;
        expected += "DTC Lamp Statuses: [" + NL;
        expected += "MIL: slow flash, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 1 times" + NL;
        expected += "MIL: off, RSL: other, AWL: off, PL: other" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 35 times" + NL;
        expected += "MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "]";
        assertEquals(expected, instance.toString());

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

        DM31DtcToLampAssociation instance2 = DM31DtcToLampAssociation.create(0,
                                                                             0,
                                                                             DTCLampStatus.create(dtc0,
                                                                                                  OFF,
                                                                                                  SLOW_FLASH,
                                                                                                  OTHER,
                                                                                                  OTHER),
                                                                             DTCLampStatus.create(dtc1,
                                                                                                  OFF,
                                                                                                  OFF,
                                                                                                  OTHER,
                                                                                                  OTHER),
                                                                             DTCLampStatus.create(dtc2,
                                                                                                  OTHER,
                                                                                                  OTHER,
                                                                                                  OTHER,
                                                                                                  OTHER));
        assertTrue(instance.equals(instance2));

    }

}
