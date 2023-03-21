/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.junit.Before;
import org.junit.Test;

public class FreezeFrameDataTranslatorTest {

    private FreezeFrameDataTranslator instance;

    private static DM24SPNSupportPacket createDM24() {
        return DM24SPNSupportPacket.create(0,
                                           SupportedSPN.create(91, true, true, true, false, 1),
                                           SupportedSPN.create(27, true, true, true, false, 2),
                                           SupportedSPN.create(513, true, true, true, false, 1),
                                           SupportedSPN.create(132, true, true, true, false, 2),
                                           SupportedSPN.create(3217, true, true, true, false, 2),
                                           SupportedSPN.create(171, true, true, true, false, 2),
                                           SupportedSPN.create(108, true, true, true, false, 1),
                                           SupportedSPN.create(102, true, true, true, false, 1),
                                           SupportedSPN.create(92, true, true, true, false, 1),
                                           SupportedSPN.create(2791, true, true, true, false, 2),
                                           SupportedSPN.create(5313, true, true, true, false, 2),
                                           SupportedSPN.create(5833, true, true, true, false, 2),
                                           SupportedSPN.create(5837, true, true, true, false, 1),
                                           SupportedSPN.create(641, true, true, true, false, 1),
                                           SupportedSPN.create(1692, true, true, true, false, 2),
                                           SupportedSPN.create(512, true, true, true, false, 1),
                                           SupportedSPN.create(2659, true, true, true, false, 2),
                                           SupportedSPN.create(168, true, true, true, false, 2),
                                           SupportedSPN.create(110, true, true, true, false, 1),
                                           SupportedSPN.create(2630, true, true, true, false, 2),
                                           SupportedSPN.create(175, true, true, true, false, 2),
                                           SupportedSPN.create(190, true, true, true, false, 2),
                                           SupportedSPN.create(173, true, true, true, false, 2),
                                           SupportedSPN.create(1436, true, true, true, false, 2),
                                           SupportedSPN.create(157, true, true, true, false, 2),
                                           SupportedSPN.create(1440, true, true, true, false, 2),
                                           SupportedSPN.create(105, true, true, true, false, 1),
                                           SupportedSPN.create(3563, true, true, true, false, 1),
                                           SupportedSPN.create(3226, true, true, true, false, 2),
                                           SupportedSPN.create(3216, true, true, true, false, 2),
                                           SupportedSPN.create(3251, true, true, true, false, 2),
                                           SupportedSPN.create(3609, true, true, true, false, 2),
                                           SupportedSPN.create(4766, true, true, true, false, 2),
                                           SupportedSPN.create(3610, true, true, true, false, 2),
                                           SupportedSPN.create(3246, true, true, true, false, 2),
                                           SupportedSPN.create(976, true, true, true, false, 1),
                                           SupportedSPN.create(3301, true, true, true, false, 2),
                                           SupportedSPN.create(247, true, true, true, false, 4),
                                           SupportedSPN.create(1176, true, true, true, false, 2),
                                           SupportedSPN.create(1172, true, true, true, false, 2),
                                           SupportedSPN.create(2629, true, true, true, false, 2),
                                           SupportedSPN.create(1180, true, true, true, false, 2),
                                           SupportedSPN.create(1184, true, true, true, false, 2),
                                           SupportedSPN.create(103, true, true, true, false, 2),
                                           SupportedSPN.create(2795, true, true, true, false, 1),
                                           SupportedSPN.create(3490, true, true, true, false, 1),
                                           SupportedSPN.create(412, true, true, true, false, 2),
                                           SupportedSPN.create(94, true, true, true, false, 1),
                                           SupportedSPN.create(183, true, true, true, false, 2),
                                           SupportedSPN.create(1081, true, true, true, false, 1),
                                           SupportedSPN.create(3700, true, true, true, false, 1),
                                           SupportedSPN.create(1761, true, true, true, false, 1),
                                           SupportedSPN.create(544, true, true, true, false, 2),
                                           SupportedSPN.create(531, true, true, true, false, 2),
                                           SupportedSPN.create(530, true, true, true, false, 2),
                                           SupportedSPN.create(529, true, true, true, false, 2),
                                           SupportedSPN.create(528, true, true, true, false, 2),
                                           SupportedSPN.create(543, true, true, true, false, 1),
                                           SupportedSPN.create(542, true, true, true, false, 1),
                                           SupportedSPN.create(541, true, true, true, false, 1),
                                           SupportedSPN.create(540, true, true, true, false, 1),
                                           SupportedSPN.create(539, true, true, true, false, 1),
                                           SupportedSPN.create(5466, true, true, true, false, 2),
                                           SupportedSPN.create(84, true, true, true, false, 2),
                                           SupportedSPN.create(5457, true, true, true, false, 1),
                                           SupportedSPN.create(96, true, true, true, false, 1),
                                           SupportedSPN.create(158, true, true, true, false, 2),
                                           SupportedSPN.create(3523, true, true, true, false, 4),
                                           SupportedSPN.create(7351, true, true, true, false, 2),
                                           SupportedSPN.create(106, true, true, true, false, 1),
                                           SupportedSPN.create(188, true, true, true, false, 2));
    }

    private static DM25ExpandedFreezeFrame createDM25() {
        //@formatter:off
        int[] data = {
                0x7C, 0x66, 0x00, 0x04, 0x01, 0x19, 0xF8, 0x4D, 0x84, 0x82, 0x02, 0x31, 0xA7, 0xC6, 0x24, 0xCB, 0x00,
                0x0A, 0x20, 0x4E, 0xB3, 0x3B, 0xE0, 0x01, 0x04, 0x73, 0xA2, 0x04, 0x7D, 0xA5, 0x09, 0x15, 0x01, 0x62,
                0xA9, 0x25, 0x7C, 0x29, 0x54, 0x14, 0xB6, 0x2D, 0xA0, 0x64, 0x0F, 0x3C, 0x00, 0x00, 0x56, 0x00, 0xFF,
                0xFF, 0xA0, 0x0F, 0x01, 0x00, 0x04, 0x00, 0xD6, 0x2C, 0x02, 0x00, 0x40, 0x2E, 0x1F, 0x1A, 0x00, 0xC6,
                0x02, 0x00, 0x00, 0xC0, 0xAF, 0x80, 0x25, 0x96, 0x25, 0x73, 0x2B, 0xB9, 0x2D, 0x76, 0x09, 0x0B, 0x00,
                0x86, 0x29, 0xAF, 0x30, 0x00, 0x00, 0x00, 0xD7, 0x52, 0x09, 0x58, 0x34, 0xC0, 0x2B, 0x20, 0x1C, 0x3A,
                0x41, 0xD3, 0xE1, 0xDF, 0x8C, 0xC1, 0xE5, 0x61, 0x00, 0x00, 0x00, 0x9E, 0x15, 0x01, 0xFF, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0x1F, 0x50, 0x14
        };
        //@formatter:on
        return new DM25ExpandedFreezeFrame(Packet.create(DM25ExpandedFreezeFrame.PGN, 0, data));
    }

    @Test
    public void realTest() {
        DM24SPNSupportPacket dm24 = new DM24SPNSupportPacket(Packet.parse("18FDB600 [544] 01 02 1C 01 5C 00 1C 01 00 02 1C 01 6E 00 18 01 BE 00 1C 02 20 02 1C 01 1B 02 1C 01 1C 02 1C 01 1D 02 1C 01 1E 02 1C 01 1F 02 1C 01 E5 0C 1C 02 54 00 1C 02 6C 00 18 01 A8 00 1C 02 5B 00 1C 01 67 04 1C 02 AB 00 1C 02 46 0A 1C 02 C1 14 1C 02 C2 14 1C 02 88 0D 1C 02 81 02 1E 01 EB 0A 1E 01 BC 0D 19 00 85 05 1B 00 86 05 1B 00 87 05 1B 00 88 05 1B 00 89 05 1B 00 8A 05 1B 00 66 00 19 00 69 00 1B 00 6F 04 1B 00 A8 04 1B 00 B9 04 19 00 EF 0B 1B 00 AF 00 19 00 63 0A 19 00 90 0C 19 00 91 0C 1B 00 9A 0C 19 00 9B 0C 19 00 0C 11 1B 00 F3 10 1B 00 BB 12 1B 00 60 0F 1B 00 B7 0C 1B 00 B8 0C 1B 00 44 15 19 00 83 10 1B 00 43 11 1B 00 15 15 1B 00 C7 14 1B 00 A5 14 1B 00 A7 15 1B 00 75 16 1B 00 8B 02 1B 00 8C 02 1B 00 8D 02 1B 00 8E 02 1B 00 8F 02 1B 00 90 02 1B 00 CB 16 1B 00 C3 16 1D 00 FD 0B 1D 00 DE 0C 1D 00 D5 16 1D 00 A6 00 1D 00 BD 00 1D 00 C9 16 1D 00 A4 0C 1D 00 7E 32 1D 00 5A 15 1D 00 E6 0C 1D 00 C4 04 1D 00 74 0E 1D 00 CA FE 1D 00 B8 FD 1D 00 BA 00 1D 00 60 00 1D 00 20 0B 1D 00 4C 02 1D 00 02 02 1D 00 A2 0B 1D 00 EB 00 1D 00 F7 00 1D 00 F8 00 1D 00 EF 1A 1D 00 A5 1C 1D 00 B3 0C 1D 00 DF 0C 1D 00 E0 0C 1D 00 EC 0F 1D 00 3A 10 1D 00 87 0E 1D 00 0A 10 1D 00 75 0E 1D 00 73 0E 1D 00 76 0E 1D 00 4F 10 1D 00 E1 06 1D 00 BC 00 1D 00 10 02 1D 00 11 02 1D 00 12 02 1D 00 13 02 1D 00 9C 05 1D 00 98 04 1D 00 F9 18 1D 00 AE 0C 1D 00 08 11 1D 00 AA 0C 1D 00 0B 11 1D 00 CB 14 1D 00 AD 00 1D 00 CE 31 1D 00 B9 1C 1D 00 94 0C 1D 00 9E 0C 1D 00 65 00 1D 00 33 00 1D 00 94 04 1D 00 CF 31 1D 00 5F 04 1D 00 CD 16 1D 00 E7 0A 1D 00 C5 16 1D 00 1B 00 1D 00 40 06 1D 00 B7 00 1D 00 B2 1C 1D 00 BB 0D 1D 00 D7 0B 1D 00 9D 00 1D 00 9E 00 1D 00"));
        System.err.println(dm24);

        DM25ExpandedFreezeFrame dm25 = new DM25ExpandedFreezeFrame(Packet.parse("18FDB700 [117] 26 FF FF F0 7F 83 FF 7D 58 BD 12 69 A9 D3 D2 DE E1 10 00 00 00 C9 05 01 00 0C 00 C6 1F 23 28 0D 3C 0D 3C 88 13 00 FF 26 41 1A 05 7F 7D FF 7D 57 00 00 69 A9 D3 D2 DE 7D 00 00 00 00 C9 03 01 00 0D 00 96 28 96 28 00 0E 00 0E 00 00 00 FF 26 FF FF F1 7F 84 FF 7D 58 F5 12 69 A9 D3 D2 DE E1 01 00 00 00 C9 F3 00 00 0C 00 C6 1F B0 26 73 24 73 24 FF 7F 00 FF"));
        dm25.setSupportedSpns(dm24.getFreezeFrameSPNsInOrder());
        System.err.println(dm25);
    }

    @Before
    public void setUp() {
        instance = new FreezeFrameDataTranslator();
    }

    @Test
    public void testGetFreezeFrameSPNs() {
        List<FreezeFrame> freezeFrames = createDM25().getFreezeFrames();

        FreezeFrame freezeFrame = freezeFrames.get(0);

        List<SupportedSPN> supportedSPNs = createDM24().getFreezeFrameSPNsInOrder();

        List<Spn> spns = instance.getFreezeFrameSPNs(freezeFrame, supportedSPNs);

        freezeFrame.setSPNs(spns);

        String expected = "";
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 120" + NL;
        expected += "    DTC 102:4 - Engine Intake Manifold #1 Pressure, Voltage Below Normal, Or Shorted To Low Source - 1 times"
                + NL;
        expected += "    SPN Data: 19 F8 4D 84 82 02 31 A7 C6 24 CB 00 0A 20 4E B3 3B E0 01 04 73 A2 04 7D A5 09 15 01 62 A9 25 7C 29 54 14 B6 2D A0 64 0F 3C 00 00 56 00 FF FF A0 0F 01 00 04 00 D6 2C 02 00 40 2E 1F 1A 00 C6 02 00 00 C0 AF 80 25 96 25 73 2B B9 2D 76 09 0B 00 86 29 AF 30 00 00 00 D7 52 09 58 34 C0 2B 20 1C 3A 41 D3 E1 DF 8C C1 E5 61 00 00 00 9E 15 01 FF FF FF FF FF FF 1F 50 14"
                + NL;
        expected += "    SPN    91, Accelerator Pedal 1 Position: 10.000 %" + NL;
        expected += "    SPN    27, Engine EGR 1 Valve Position: 49.900 %" + NL;
        expected += "    SPN   513, Actual Engine - Percent Torque: 7.000 %" + NL;
        expected += "    SPN   132, Engine Intake Air Mass Flow Rate: 32.100 kg/h" + NL;
        expected += "    SPN  3217, Engine Exhaust 1 Percent Oxygen 1: 10.000 %" + NL;
        expected += "    SPN   171, Ambient Air Temperature: 21.188 °C" + NL;
        expected += "    SPN   108, Barometric Pressure: 101.500 kPa" + NL;
        expected += "    SPN   102, Engine Intake Manifold #1 Pressure: 0.000 kPa" + NL;
        expected += "    SPN    92, Engine Percent Load At Current Speed: 10.000 %" + NL;
        expected += "    SPN  2791, Engine EGR 1 Valve 1 Control 1: 50.000 %" + NL;
        expected += "    SPN  5313, Commanded Engine Fuel Rail Pressure: 59.699 MPa" + NL;
        expected += "    SPN  5833, Engine Fuel Mass Flow Rate: 2.400 g/s" + NL;
        expected += "    SPN  5837, Fuel Type: Diesel - DSL" + NL;
        expected += "    SPN   641, Engine Variable Geometry Turbocharger Actuator #1: 46.000 %" + NL;
        expected += "    SPN  1692, Engine Intake Manifold Desired Absolute Pressure: 118.600 kPa" + NL;
        expected += "    SPN   512, Driver's Demand Engine - Percent Torque: 0.000 %" + NL;
        expected += "    SPN  2659, Engine EGR 1 Mass Flow Rate: 123.450 kg/h" + NL;
        expected += "    SPN   168, Battery Potential / Power Input 1: 13.850 V" + NL;
        expected += "    SPN   110, Engine Coolant Temperature: 58.000 °C" + NL;
        expected += "    SPN  2630, Engine Charge Air Cooler 1 Outlet Temperature: 28.281 °C" + NL;
        expected += "    SPN   175, Engine Oil Temperature 1: 58.875 °C" + NL;
        expected += "    SPN   190, Engine Speed: 650.500 rpm" + NL;
        expected += "    SPN   173, Engine Exhaust Temperature: 92.688 °C" + NL;
        expected += "    SPN  1436, Engine Actual Ignition Timing: 1.250 deg" + NL;
        expected += "    SPN   157, Engine Fuel 1 Injector Metering Rail 1 Pressure: 60.059 MPa" + NL;
        expected += "    SPN  1440, Engine Fuel Flow Rate 1: 0.000 m³/h" + NL;
        expected += "    SPN   105, Engine Intake Manifold 1 Temperature: 46.000 °C" + NL;
        expected += "    SPN  3563, Engine Intake Manifold #1 Absolute Pressure: 0.000 kPa" + NL;
        expected += "    SPN  3226, AFT 1 Outlet NOx 1: Not Available" + NL;
        expected += "    SPN  3216, Engine Exhaust 1 NOx 1: 0.000 ppm" + NL;
        expected += "    SPN  3251, AFT 1 DPF Differential Pressure: 0.100 kPa" + NL;
        expected += "    SPN  3609, AFT 1 DPF Intake Pressure: 0.400 kPa" + NL;
        expected += "    SPN  4766, AFT 1 Diesel Oxidation Catalyst Outlet Temperature: 85.688 °C" + NL;
        expected += "    SPN  3610, AFT 1 DPF Outlet Pressure: 0.200 kPa" + NL;
        expected += "    SPN  3246, AFT 1 DPF Outlet Temperature: 97.000 °C" + NL;
        expected += "    SPN   976, PTO Governor State: Not available" + NL;
        expected += "    SPN  3301, Time Since Engine Start: 26.000 seconds" + NL;
        expected += "    SPN   247, Engine Total Hours of Operation: 35.500 h" + NL;
        expected += "    SPN  1176, Engine Turbocharger 1 Compressor Intake Pressure: 101.500 kPa" + NL;
        expected += "    SPN  1172, Engine Turbocharger 1 Compressor Intake Temperature: 27.000 °C" + NL;
        expected += "    SPN  2629, Engine Turbocharger 1 Compressor Outlet Temperature: 27.688 °C" + NL;
        expected += "    SPN  1180, Engine Turbocharger 1 Turbine Intake Temperature: 74.594 °C" + NL;
        expected += "    SPN  1184, Engine Turbocharger 1 Turbine Outlet Temperature: 92.781 °C" + NL;
        expected += "    SPN   103, Engine Turbocharger 1 Speed: 9688.000 rpm" + NL;
        expected += "    SPN  2795, Engine Variable Geometry Turbocharger (VGT) 1 Actuator Position: 4.400 %" + NL;
        expected += "    SPN  3490, AFT 1 Purge Air Actuator: not active" + NL;
        expected += "    SPN   412, Engine EGR 1 Temperature: 59.188 °C" + NL;
        expected += "    SPN    94, Engine Fuel Delivery Pressure: 700.000 kPa" + NL;
        expected += "    SPN   183, Engine Fuel Rate: 2.400 l/h" + NL;
        expected += "    SPN  1081, Engine Wait to Start Lamp: Off" + NL;
        expected += "    SPN  3700, AFT DPF Active Regeneration Status: not active" + NL;
        expected += "    SPN  1761, AFT 1 DEF Tank Volume: 86.000 %" + NL;
        expected += "    SPN   544, Engine Reference Torque: 2386.000 Nm" + NL;
        expected += "    SPN   531, Engine Speed At Point 5: 1675.000 rpm" + NL;
        expected += "    SPN   530, Engine Speed At Point 4: 1400.000 rpm" + NL;
        expected += "    SPN   529, Engine Speed At Point 3: 900.000 rpm" + NL;
        expected += "    SPN   528, Engine Speed At Point 2: 2087.250 rpm" + NL;
        expected += "    SPN   543, Engine Percent Torque At Point 5: 86.000 %" + NL;
        expected += "    SPN   542, Engine Percent Torque At Point 4: 100.000 %" + NL;
        expected += "    SPN   541, Engine Percent Torque At Point 3: 98.000 %" + NL;
        expected += "    SPN   540, Engine Percent Torque At Point 2: 15.000 %" + NL;
        expected += "    SPN   539, Engine Percent Torque At Idle, Point 1: 68.000 %" + NL;
        expected += "    SPN  5466, AFT 1 DPF Soot Load Regeneration Threshold: 62.653 %" + NL;
        expected += "    SPN    84, Wheel-Based Vehicle Speed: 0.000 km/h" + NL;
        expected += "    SPN  5457, Engine Variable Geometry Turbocharger 1 Control Mode: open loop" + NL;
        expected += "    SPN    96, Fuel Level 1: 63.200 %" + NL;
        expected += "    SPN   158, Key Switch Battery Potential: 13.850 V" + NL;
        expected += "    SPN  3523, AFT 1 Total Regeneration Time: Not Available" + NL;
        expected += "    SPN  7351, AFT 1 Outlet Corrected NOx: Not Available" + NL;
        expected += "    SPN   106, Engine Intake Air Pressure: 62.000 kPa" + NL;
        expected += "    SPN   188, Engine Speed At Idle, Point 1: 650.000 rpm" + NL;
        expected += "  }";
        assertEquals(expected, freezeFrame.toString());
    }

}
