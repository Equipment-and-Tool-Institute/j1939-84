/*
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame.PGN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.junit.Test;

/**
 * Unit tests the {@link DM25ExpandedFreezeFrame} class
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
public class DM25ExpandedFreezeFrameTest {

    @Test
    public void testActual() {
        DM25ExpandedFreezeFrame instance = DM25ExpandedFreezeFrame.create(0);
        List<FreezeFrame> freezeFrame = instance.getFreezeFrames();
        assertEquals(0, freezeFrame.size());
        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 8" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  No Freeze Frames" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testActualWithBadFmi() {
        Packet packet = Packet.create(PGN,
                                      0,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0xFF,
                                      0xFE,
                                      0xFF);
        DM25ExpandedFreezeFrame instance = new DM25ExpandedFreezeFrame(packet);
        List<FreezeFrame> freezeFrames = instance.getFreezeFrames();
        assertEquals(1, freezeFrames.size());

        FreezeFrame freezeFrame = freezeFrames.get(0);
        DiagnosticTroubleCode dtc = freezeFrame.getDtc();
        assertEquals(0, dtc.getSuspectParameterNumber());
        assertEquals(0, dtc.getFailureModeIndicator());
        assertArrayEquals(new int[] { 0xFF, 0xFE, 0xFF, 0x00 }, freezeFrame.getSpnData());
        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 8" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 4" + NL;
        expected += "    DTC 0:0 - Unknown, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times"
                + NL;
        expected += "    SPN Data: FF FE FF 00" + NL;
        expected += "  }" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testActualWithBadSpn() {
        Packet packet = Packet.create(PGN,
                                      0,
                                      0x00,
                                      0x01,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0xFF,
                                      0xFF,
                                      0xFF);

        DM25ExpandedFreezeFrame instance = new DM25ExpandedFreezeFrame(packet);

        List<FreezeFrame> freezeFrames = instance.getFreezeFrames();
        assertEquals(1, freezeFrames.size());

        FreezeFrame freezeFrame = freezeFrames.get(0);
        DiagnosticTroubleCode dtc = freezeFrame.getDtc();
        assertEquals(1, dtc.getSuspectParameterNumber());
        assertEquals(0, dtc.getFailureModeIndicator());
        assertArrayEquals(new int[] { 0xFF, 0xFF, 0xFF, 0x00 }, freezeFrame.getSpnData());

        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 8" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 4" + NL;
        expected += "    DTC 1:0 - Unknown, Data Valid But Above Normal Operational Range - Most Severe Level - 0 times"
                + NL;
        expected += "    SPN Data: FF FF FF 00" + NL;
        expected += "  }" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testOne() {
        //@formatter:off
        int[] realData = new int[] {
                0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA };
        //@formatter:on

        var dtc = DiagnosticTroubleCode.create(157, 7, 0, 0x3F);
        var freezeFrame = new FreezeFrame(dtc, realData);
        DM25ExpandedFreezeFrame instance = DM25ExpandedFreezeFrame.create(0, freezeFrame);

        List<FreezeFrame> freezeFrames = instance.getFreezeFrames();
        assertEquals(1, freezeFrames.size());
        FreezeFrame actual = freezeFrames.get(0);
        assertEquals(157, actual.getDtc().getSuspectParameterNumber());
        assertEquals(7, actual.getDtc().getFailureModeIndicator());
        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 87" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 82" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    public void testPGN() {
        assertEquals(64951, PGN);
    }

    @Test
    public void testTwo() {
        //@formatter:off
        int[] realData = new int[] {
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA ,
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA };
        //@formatter:on

        Packet packet = Packet.create(0x00, 0x00, realData);
        DM25ExpandedFreezeFrame instance = new DM25ExpandedFreezeFrame(packet);

        List<FreezeFrame> freezeFrames = instance.getFreezeFrames();
        assertEquals(2, freezeFrames.size());
        for (int i = 0; i < 2; i++) {
            FreezeFrame actual = freezeFrames.get(i);
            assertEquals(157, actual.getDtc().getSuspectParameterNumber());
            assertEquals(7, actual.getDtc().getFailureModeIndicator());
        }

        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 174" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 82" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 82" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "]" + NL;

        assertEquals(expected, instance.toString());
    }

    @Test
    public void testTwoB() {
        //@formatter:off
        int[] realData = new int[] {
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA ,
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA,
                /// Junk
                0, 1, 2, 3};
        //@formatter:on

        Packet packet = Packet.create(0x00, 0x00, realData);
        DM25ExpandedFreezeFrame instance = new DM25ExpandedFreezeFrame(packet);

        List<FreezeFrame> freezeFrames = instance.getFreezeFrames();
        assertEquals(2, freezeFrames.size());
        for (int i = 0; i < 2; i++) {
            FreezeFrame actual = freezeFrames.get(i);
            assertEquals(157, actual.getDtc().getSuspectParameterNumber());
            assertEquals(7, actual.getDtc().getFailureModeIndicator());
        }

        String expected = "DM25 from Engine #1 (0): " + NL;
        expected += "Packet Length: 178" + NL;
        expected += "Freeze Frames: [" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 82" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 82" + NL;
        expected += "    DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment"
                + NL;
        expected += "    SPN Data: 00 01 7B 00 00 39 3A 5C 0F C4 FB 00 00 00 F1 26 00 00 00 12 7A 7D 80 65 00 00 32 00 00 00 00 84 AD 00 39 2C 30 39 FC 38 C6 35 E0 34 2C 2F 00 00 7D 7D 8A 28 A0 0F A0 0F D1 37 00 CA 28 01 A4 0D 00 A8 C3 B2 C2 C3 00 00 00 00 7E D0 07 00 7D 04 FF FA"
                + NL;
        expected += "  }" + NL;
        expected += "]" + NL;

        assertEquals(expected, instance.toString());
    }

    @Test
    public void testGetFreezeFrameForDTC() {
        //@formatter:off
        int[] realData = new int[] {
                0x56, 0x9D, 0x00, 0x07, 0x7F, 0x00, 0x01, 0x7B,
                0x00, 0x00, 0x39, 0x3A, 0x5C, 0x0F, 0xC4, 0xFB,
                0x00, 0x00, 0x00, 0xF1, 0x26, 0x00, 0x00, 0x00,
                0x12, 0x7A, 0x7D, 0x80, 0x65, 0x00, 0x00, 0x32,
                0x00, 0x00, 0x00, 0x00, 0x84, 0xAD, 0x00, 0x39,
                0x2C, 0x30, 0x39, 0xFC, 0x38, 0xC6, 0x35, 0xE0,
                0x34, 0x2C, 0x2F, 0x00, 0x00, 0x7D, 0x7D, 0x8A,
                0x28, 0xA0, 0x0F, 0xA0, 0x0F, 0xD1, 0x37, 0x00,
                0xCA, 0x28, 0x01, 0xA4, 0x0D, 0x00, 0xA8, 0xC3,
                0xB2, 0xC2, 0xC3, 0x00, 0x00, 0x00, 0x00, 0x7E,
                0xD0, 0x07, 0x00, 0x7D, 0x04, 0xFF, 0xFA };
        //@formatter:on

        Packet packet = Packet.create(0x00, 0x00, realData);
        DM25ExpandedFreezeFrame instance = new DM25ExpandedFreezeFrame(packet);

        var dtc = DiagnosticTroubleCode.create(157, 7, 0, 1);
        var actual = instance.getFreezeFrameWithDTC(dtc);
        assertEquals(157, actual.getDtc().getSuspectParameterNumber());
        assertEquals(7, actual.getDtc().getFailureModeIndicator());

        var dtc2 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        assertNull(instance.getFreezeFrameWithDTC(dtc2));
    }

}
