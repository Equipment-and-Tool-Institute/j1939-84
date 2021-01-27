/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.bus.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.testdoc.TestDoc;
import org.junit.Test;

/**
 * Unit tests the {@link DM24SPNSupportPacket} class
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class DM24SPNSupportPacketTest {

    @Test
    @TestDoc(description = "Verify that an example DM24 from a real vehicle parses correctly.")
    public void testActual() {
        //@formatter:off
        Packet packet = Packet.create(0,
                                      0,
                                      0x5C, 0x00, 0x1C, 0x01,
                                      0x00, 0x02, 0x1C, 0x01,
                                      0x01, 0x02, 0x1C, 0x01,
                                      0x20, 0x02, 0x1C, 0x02,
                                      0x1B, 0x02, 0x1C, 0x01,
                                      0x1C, 0x02, 0x1C, 0x01,
                                      0x1D, 0x02, 0x1C, 0x01,
                                      0x1E, 0x02, 0x1C, 0x01,
                                      0x1F, 0x02, 0x1C, 0x01,
                                      0x6E, 0x00, 0x1C, 0x01,
                                      0xAF, 0x00, 0x1C, 0x02,
                                      0xBE, 0x00, 0x1C, 0x02,
                                      0x54, 0x00, 0x1C, 0x02,
                                      0x6C, 0x00, 0x1C, 0x01,
                                      0x9E, 0x00, 0x1C, 0x02,
                                      0x33, 0x00, 0x1C, 0x01,
                                      0x5E, 0x00, 0x1C, 0x01,
                                      0xAC, 0x00, 0x1C, 0x01,
                                      0x69, 0x00, 0x1C, 0x01,
                                      0x84, 0x00, 0x1C, 0x02,
                                      0xD0, 0x03, 0x1C, 0x01,
                                      0x5B, 0x00, 0x1C, 0x01,
                                      0xB7, 0x00, 0x1C, 0x02,
                                      0x66, 0x00, 0x18, 0x01,
                                      0xAD, 0x00, 0x1C, 0x02,
                                      0xB3, 0x0C, 0x1C, 0x02,
                                      0x9B, 0x0D, 0x1C, 0x01,
                                      0xCD, 0x16, 0x1C, 0x01,
                                      0xE5, 0x0C, 0x1C, 0x02,
                                      0x5A, 0x15, 0x1C, 0x02,
                                      0xCB, 0x14, 0x1C, 0x01,
                                      0x88, 0x0D, 0x1C, 0x02,
                                      0xB9, 0x04, 0x1C, 0x02,
                                      0xA5, 0x15, 0x1C, 0x01,
                                      0xA4, 0x00, 0x18, 0x02,
                                      0xE7, 0x0A, 0x1C, 0x02,
                                      0x85, 0x05, 0x1C, 0x02,
                                      0x86, 0x05, 0x1C, 0x02,
                                      0x87, 0x05, 0x1C, 0x02,
                                      0x88, 0x05, 0x1C, 0x02,
                                      0x89, 0x05, 0x1C, 0x02,
                                      0x8A, 0x05, 0x1C, 0x02,
                                      0xEB, 0x0D, 0x1C, 0x01,
                                      0x1B, 0x00, 0x1C, 0x02,
                                      0xAA, 0x0C, 0x1C, 0x02,
                                      0xAE, 0x0C, 0x1C, 0x02,
                                      0x90, 0x0C, 0x18, 0x02,
                                      0x39, 0x04, 0x1C, 0x01,
                                      0xA5, 0x04, 0x1C, 0x01,
                                      0xC2, 0x14, 0x1C, 0x02,
                                      0x19, 0x0E, 0x1E, 0x02,
                                      0x98, 0x0D, 0x1E, 0x02,
                                      0x9A, 0x0C, 0x1C, 0x02,
                                      0xE1, 0x06, 0x1C, 0x01,
                                      0x9A, 0x0D, 0x1E, 0x01,
                                      0xA2, 0x0D, 0x1E, 0x01,
                                      0x08, 0x11, 0x1E, 0x02,
                                      0x0B, 0x11, 0x1E, 0x02,
                                      0xD7, 0x0B, 0x1E, 0x01,
                                      0x45, 0x11, 0x1F, 0x01,
                                      0x95, 0x04, 0x18, 0x02,
                                      0xED, 0x00, 0x1D, 0x11,
                                      0xA1, 0x10, 0x1B, 0x01,
                                      0x00, 0x09, 0x1F, 0x01,
                                      0x83, 0x03, 0x1D, 0x01,
                                      0x01, 0x09, 0x1F, 0x01,
                                      0xFD, 0x0B, 0x1D, 0x01,
                                      0xDE, 0x0C, 0x1D, 0x01,
                                      0xDF, 0x0C, 0x1D, 0x01,
                                      0xE0, 0x0C, 0x1D, 0x01,
                                      0xE6, 0x0C, 0x1D, 0x01,
                                      0x87, 0x0E, 0x1D, 0x01,
                                      0x2B, 0x05, 0x1B, 0x01,
                                      0x2C, 0x05, 0x1B, 0x01,
                                      0x2D, 0x05, 0x1B, 0x01,
                                      0x2E, 0x05, 0x1B, 0x01,
                                      0x2F, 0x05, 0x1B, 0x01,
                                      0x30, 0x05, 0x1B, 0x01,
                                      0x46, 0x0A, 0x1B, 0x02,
                                      0x63, 0x0A, 0x1B, 0x02,
                                      0x2A, 0x05, 0x1B, 0x01,
                                      0x90, 0x12, 0x1B, 0x01,
                                      0x9E, 0x12, 0x1F, 0x02,
                                      0xC7, 0x14, 0x1F, 0x01,
                                      0x8B, 0x02, 0x1B, 0x01,
                                      0x8C, 0x02, 0x1B, 0x01,
                                      0x8D, 0x02, 0x1B, 0x01,
                                      0x8E, 0x02, 0x1B, 0x01,
                                      0x8F, 0x02, 0x1B, 0x01,
                                      0x90, 0x02, 0x1B, 0x01,
                                      0x8F, 0x0D, 0x1B, 0x01,
                                      0xE4, 0x0D, 0x1B, 0x01,
                                      0x03, 0x09, 0x1F, 0x01,
                                      0x0A, 0x10, 0x1D, 0x01,
                                      0xC0, 0x04, 0x1D, 0x01,
                                      0xC4, 0x04, 0x1D, 0x01,
                                      0x1F, 0x10, 0x1D, 0x01,
                                      0x22, 0x10, 0x1D, 0x01,
                                      0xAB, 0x00, 0x1D, 0x02,
                                      0x9D, 0x00, 0x1F, 0x02,
                                      0x3F, 0x0A, 0x1D, 0x01,
                                      0xF7, 0x00, 0x1D, 0x04,
                                      0xEB, 0x00, 0x1D, 0x04,
                                      0xBD, 0x04, 0x1D, 0x01,
                                      0xE7, 0x0C, 0x1D, 0x01,
                                      0xE8, 0x0C, 0x1D, 0x01,
                                      0x4E, 0x15, 0x1D, 0x04,
                                      0x4C, 0x02, 0x1D, 0x11,
                                      0xEF, 0x0B, 0x1F, 0x01,
                                      0x57, 0x15, 0x1D, 0x01,
                                      0xF8, 0x00, 0x1D, 0x04,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00,
                                      0x00, 0x00, 0x1F, 0x00);
        //@formatter:on
        DM24SPNSupportPacket instance = new DM24SPNSupportPacket(packet);
        List<SupportedSPN> spns = instance.getSupportedSpns();
        assertEquals(111, spns.size());

        String expected = "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "  SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expected += "  SPN 651 - Engine Fuel 1 Injector Cylinder 1" + NL;
        expected += "  SPN 652 - Engine Fuel 1 Injector Cylinder 2" + NL;
        expected += "  SPN 653 - Engine Fuel 1 Injector Cylinder 3" + NL;
        expected += "  SPN 654 - Engine Fuel 1 Injector Cylinder 4" + NL;
        expected += "  SPN 655 - Engine Fuel 1 Injector Cylinder 5" + NL;
        expected += "  SPN 656 - Engine Fuel 1 Injector Cylinder 6" + NL;
        expected += "  SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        expected += "  SPN 1322 - Engine Misfire for Multiple Cylinders" + NL;
        expected += "  SPN 1323 - Engine Cylinder 1 Misfire Rate" + NL;
        expected += "  SPN 1324 - Engine Cylinder 2 Misfire Rate" + NL;
        expected += "  SPN 1325 - Engine Cylinder 3 Misfire Rate" + NL;
        expected += "  SPN 1326 - Engine Cylinder 4 Misfire Rate" + NL;
        expected += "  SPN 1327 - Engine Cylinder 5 Misfire Rate" + NL;
        expected += "  SPN 1328 - Engine Cylinder 6 Misfire Rate" + NL;
        expected += "  SPN 2630 - Engine Charge Air Cooler 1 Outlet Temperature" + NL;
        expected += "  SPN 2659 - Engine Exhaust Gas Recirculation 1 Mass Flow Rate" + NL;
        expected += "  SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expected += "  SPN 3471 - AFT 1 Fuel Pressure Control Actuator" + NL;
        expected += "  SPN 3556 - AFT 1 Hydrocarbon Doser 1" + NL;
        expected += "  SPN 4257 - Engine Fuel 1 Injector Group 3" + NL;
        expected += "  SPN 4752 - Engine Exhaust Gas Recirculation 1 Cooler Efficiency" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "  SPN 27 - Engine Exhaust Gas Recirculation 1 Valve Position" + NL;
        expected += "  SPN 51 - Engine Throttle Valve 1 Position 1" + NL;
        expected += "  SPN 84 - Wheel-Based Vehicle Speed" + NL;
        expected += "  SPN 91 - Accelerator Pedal Position 1" + NL;
        expected += "  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expected += "  SPN 94 - Engine Fuel Delivery Pressure" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "  SPN 105 - Engine Intake Manifold 1 Temperature" + NL;
        expected += "  SPN 108 - Barometric Pressure" + NL;
        expected += "  SPN 110 - Engine Coolant Temperature" + NL;
        expected += "  SPN 132 - Engine Intake Air Mass Flow Rate" + NL;
        expected += "  SPN 158 - Key Switch Battery Potential" + NL;
        expected += "  SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expected += "  SPN 171 - Ambient Air Temperature" + NL;
        expected += "  SPN 172 - Engine Intake 1 Air Temperature" + NL;
        expected += "  SPN 173 - Engine Exhaust Temperature" + NL;
        expected += "  SPN 175 - Engine Oil Temperature 1" + NL;
        expected += "  SPN 183 - Engine Fuel Rate" + NL;
        expected += "  SPN 190 - Engine Speed" + NL;
        expected += "  SPN 235 - Engine Total Idle Hours" + NL;
        expected += "  SPN 237 - Vehicle Identification Number" + NL;
        expected += "  SPN 247 - Engine Total Hours of Operation" + NL;
        expected += "  SPN 248 - Total Power Takeoff Hours" + NL;
        expected += "  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expected += "  SPN 513 - Actual Engine - Percent Torque" + NL;
        expected += "  SPN 539 - Engine Percent Torque At Idle, Point 1" + NL;
        expected += "  SPN 540 - Engine Percent Torque At Point 2" + NL;
        expected += "  SPN 541 - Engine Percent Torque At Point 3" + NL;
        expected += "  SPN 542 - Engine Percent Torque At Point 4" + NL;
        expected += "  SPN 543 - Engine Percent Torque At Point 5" + NL;
        expected += "  SPN 544 - Engine Reference Torque" + NL;
        expected += "  SPN 588 - Serial Number" + NL;
        expected += "  SPN 899 - Engine Torque Mode" + NL;
        expected += "  SPN 976 - PTO Governor State" + NL;
        expected += "  SPN 1081 - Engine Wait to Start Lamp" + NL;
        expected += "  SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        expected += "  SPN 1189 - Engine Turbocharger Wastegate Actuator 2 Position" + NL;
        expected += "  SPN 1209 - Engine Exhaust Pressure 1" + NL;
        expected += "  SPN 1213 - Malfunction Indicator Lamp" + NL;
        expected += "  SPN 1216 - Occurrence Count" + NL;
        expected += "  SPN 1220 - OBD Compliance" + NL;
        expected += "  SPN 1413 - Engine Cylinder 1 Ignition Timing" + NL;
        expected += "  SPN 1414 - Engine Cylinder 2 Ignition Timing" + NL;
        expected += "  SPN 1415 - Engine Cylinder 3 Ignition Timing" + NL;
        expected += "  SPN 1416 - Engine Cylinder 4 Ignition Timing" + NL;
        expected += "  SPN 1417 - Engine Cylinder 5 Ignition Timing" + NL;
        expected += "  SPN 1418 - Engine Cylinder 6 Ignition Timing" + NL;
        expected += "  SPN 1761 - AFT 1 DEF Tank Volume" + NL;
        expected += "  SPN 2623 - Accelerator Pedal #1 Channel 2" + NL;
        expected += "  SPN 2791 - Engine Exhaust Gas Recirculation 1 Valve 1 Control 1" + NL;
        expected += "  SPN 3069 - Distance Travelled While MIL is Activated" + NL;
        expected += "  SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expected += "  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  SPN 3242 - AFT 1 DPF Intake Temperature" + NL;
        expected += "  SPN 3246 - AFT 1 DPF Outlet Temperature" + NL;
        expected += "  SPN 3251 - AFT 1 DPF Differential Pressure" + NL;
        expected += "  SPN 3294 - Distance Since Diagnostic Trouble Codes Cleared" + NL;
        expected += "  SPN 3295 - Minutes Run by Engine While MIL is Activated" + NL;
        expected += "  SPN 3296 - Time Since Diagnostic Trouble Codes Cleared" + NL;
        expected += "  SPN 3301 - Time Since Engine Start" + NL;
        expected += "  SPN 3302 - Number of Warm-Ups Since Diagnostic Trouble Codes Cleared" + NL;
        expected += "  SPN 3303 - Continuously Monitored Systems Enabled/Completed Status" + NL;
        expected += "  SPN 3304 - Non-Continuously Monitored Systems Enabled Status" + NL;
        expected += "  SPN 3464 - Engine Throttle Actuator 1 Control Command" + NL;
        expected += "  SPN 3483 - AFT 1 Regeneration Status" + NL;
        expected += "  SPN 3563 - Engine Intake Manifold #1 Absolute Pressure" + NL;
        expected += "  SPN 3719 - AFT 1 DPF Soot Load Percent" + NL;
        expected += "  SPN 4106 - Emission-Related MIL-On DTC Count" + NL;
        expected += "  SPN 4127 - NOx NTE Control Area Status" + NL;
        expected += "  SPN 4130 - PM NTE Control Area Status" + NL;
        expected += "  SPN 5314 - Commanded Engine Fuel Injection Control Pressure" + NL;
        expected += "  SPN 5323 - Engine Fuel Control Mode" + NL;
        expected += "  SPN 5454 - AFT 1 DPF Average Time Between Active Regenerations" + NL;
        expected += "  SPN 5463 - AFT SCR Operator Inducement Active Traveled Distance" + NL;
        expected += "  SPN 5466 - AFT 1 DPF Soot Load Regeneration Threshold" + NL;
        expected += "  SPN 5541 - Engine Turbocharger 1 Turbine Outlet Pressure" + NL;
        expected += "  SPN 5837 - Fuel Type" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "  SPN 27 - Engine Exhaust Gas Recirculation 1 Valve Position" + NL;
        expected += "  SPN 51 - Engine Throttle Valve 1 Position 1" + NL;
        expected += "  SPN 84 - Wheel-Based Vehicle Speed" + NL;
        expected += "  SPN 91 - Accelerator Pedal Position 1" + NL;
        expected += "  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expected += "  SPN 94 - Engine Fuel Delivery Pressure" + NL;
        expected += "  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "  SPN 105 - Engine Intake Manifold 1 Temperature" + NL;
        expected += "  SPN 108 - Barometric Pressure" + NL;
        expected += "  SPN 110 - Engine Coolant Temperature" + NL;
        expected += "  SPN 132 - Engine Intake Air Mass Flow Rate" + NL;
        expected += "  SPN 158 - Key Switch Battery Potential" + NL;
        expected += "  SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expected += "  SPN 172 - Engine Intake 1 Air Temperature" + NL;
        expected += "  SPN 173 - Engine Exhaust Temperature" + NL;
        expected += "  SPN 175 - Engine Oil Temperature 1" + NL;
        expected += "  SPN 183 - Engine Fuel Rate" + NL;
        expected += "  SPN 190 - Engine Speed" + NL;
        expected += "  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expected += "  SPN 513 - Actual Engine - Percent Torque" + NL;
        expected += "  SPN 539 - Engine Percent Torque At Idle, Point 1" + NL;
        expected += "  SPN 540 - Engine Percent Torque At Point 2" + NL;
        expected += "  SPN 541 - Engine Percent Torque At Point 3" + NL;
        expected += "  SPN 542 - Engine Percent Torque At Point 4" + NL;
        expected += "  SPN 543 - Engine Percent Torque At Point 5" + NL;
        expected += "  SPN 544 - Engine Reference Torque" + NL;
        expected += "  SPN 976 - PTO Governor State" + NL;
        expected += "  SPN 1081 - Engine Wait to Start Lamp" + NL;
        expected += "  SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        expected += "  SPN 1189 - Engine Turbocharger Wastegate Actuator 2 Position" + NL;
        expected += "  SPN 1209 - Engine Exhaust Pressure 1" + NL;
        expected += "  SPN 1413 - Engine Cylinder 1 Ignition Timing" + NL;
        expected += "  SPN 1414 - Engine Cylinder 2 Ignition Timing" + NL;
        expected += "  SPN 1415 - Engine Cylinder 3 Ignition Timing" + NL;
        expected += "  SPN 1416 - Engine Cylinder 4 Ignition Timing" + NL;
        expected += "  SPN 1417 - Engine Cylinder 5 Ignition Timing" + NL;
        expected += "  SPN 1418 - Engine Cylinder 6 Ignition Timing" + NL;
        expected += "  SPN 1761 - AFT 1 DEF Tank Volume" + NL;
        expected += "  SPN 2791 - Engine Exhaust Gas Recirculation 1 Valve 1 Control 1" + NL;
        expected += "  SPN 3031 - AFT 1 DEF Tank Temperature 1" + NL;
        expected += "  SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expected += "  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  SPN 3242 - AFT 1 DPF Intake Temperature" + NL;
        expected += "  SPN 3246 - AFT 1 DPF Outlet Temperature" + NL;
        expected += "  SPN 3251 - AFT 1 DPF Differential Pressure" + NL;
        expected += "  SPN 3301 - Time Since Engine Start" + NL;
        expected += "  SPN 3464 - Engine Throttle Actuator 1 Control Command" + NL;
        expected += "  SPN 3480 - AFT 1 Fuel Pressure 1" + NL;
        expected += "  SPN 3482 - AFT 1 Fuel Enable Actuator" + NL;
        expected += "  SPN 3483 - AFT 1 Regeneration Status" + NL;
        expected += "  SPN 3490 - AFT 1 Purge Air Actuator" + NL;
        expected += "  SPN 3563 - Engine Intake Manifold #1 Absolute Pressure" + NL;
        expected += "  SPN 3609 - AFT 1 DPF Intake Pressure" + NL;
        expected += "  SPN 4360 - AFT 1 SCR Intake Temperature" + NL;
        expected += "  SPN 4363 - AFT 1 SCR Outlet Temperature" + NL;
        expected += "  SPN 5314 - Commanded Engine Fuel Injection Control Pressure" + NL;
        expected += "  SPN 5323 - Engine Fuel Control Mode" + NL;
        expected += "  SPN 5466 - AFT 1 DPF Soot Load Regeneration Threshold" + NL;
        expected += "  SPN 5541 - Engine Turbocharger 1 Turbine Outlet Pressure" + NL;
        expected += "  SPN 5837 - Fuel Type" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    @TestDoc(description = "Verify that a DM24 with a single SPN parses correctly.")
    public void testOne() {
        Packet packet = Packet.create(0, 0, 0x5C, 0x00, 0x1B, 0x01);
        DM24SPNSupportPacket instance = new DM24SPNSupportPacket(packet);
        List<SupportedSPN> spns = instance.getSupportedSpns();
        assertEquals(1, spns.size());
        SupportedSPN spn = spns.get(0);
        assertEquals(92, spn.getSpn());
        assertEquals(1, spn.getLength());
        assertFalse(spn.supportsDataStream());
        assertFalse(spn.supportsExpandedFreezeFrame());
        assertTrue(spn.supportsScaledTestResults());
        String expected = "DM24 from Engine #1 (0): " + NL;
        expected += "(Supporting Scaled Test Results) [" + NL;
        expected += "  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expected += "]" + NL;
        expected += "(Supports Data Stream Results) [" + NL;
        expected += "  No Supported SPNs" + NL;
        expected += "]" + NL;
        expected += "(Supports Freeze Frame Results) [" + NL;
        expected += "  No Supported SPNs" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

    @Test
    @TestDoc(description = "Verify DM24 PGN is 64950.")
    public void testPGN() {
        assertEquals(64950, DM24SPNSupportPacket.PGN);
    }

    @Test
    @TestDoc(description = "Verify that a DM24 with a three SPNs parses correctly.")
    public void testThree() {
        SupportedSPN spn92 = SupportedSPN.create(92, true, false, false, 1);
        SupportedSPN spn512 = SupportedSPN.create(512, true, false, false, 1);
        SupportedSPN spn513 = SupportedSPN.create(513, true, false, false, 1);

        DM24SPNSupportPacket instance = DM24SPNSupportPacket.create(0, spn92, spn512, spn513);
        List<SupportedSPN> spns = instance.getSupportedSpns();
        assertEquals(3, spns.size());

        {
            SupportedSPN spn = spns.get(0);
            assertEquals(92, spn.getSpn());
            assertEquals(1, spn.getLength());
            assertFalse(spn.supportsDataStream());
            assertFalse(spn.supportsExpandedFreezeFrame());
            assertTrue(spn.supportsScaledTestResults());
        }
        {
            SupportedSPN spn = spns.get(1);
            assertEquals(512, spn.getSpn());
            assertEquals(1, spn.getLength());
            assertFalse(spn.supportsDataStream());
            assertFalse(spn.supportsExpandedFreezeFrame());
            assertTrue(spn.supportsScaledTestResults());
        }
        {
            SupportedSPN spn = spns.get(2);
            assertEquals(513, spn.getSpn());
            assertEquals(1, spn.getLength());
            assertFalse(spn.supportsDataStream());
            assertFalse(spn.supportsExpandedFreezeFrame());
            assertTrue(spn.supportsScaledTestResults());
        }
        String expected = "DM24 from Engine #1 (0): " + NL
                + "(Supporting Scaled Test Results) [" + NL
                + "  SPN 92 - Engine Percent Load At Current Speed" + NL
                + "  SPN 512 - Driver's Demand Engine - Percent Torque" + NL
                + "  SPN 513 - Actual Engine - Percent Torque" + NL
                + "]" + NL
                + "(Supports Data Stream Results) [" + NL
                + "  No Supported SPNs" + NL
                + "]" + NL
                + "(Supports Freeze Frame Results) [" + NL
                + "  No Supported SPNs" + NL
                + "]" + NL;
        assertEquals(expected, instance.toString());
    }

}
