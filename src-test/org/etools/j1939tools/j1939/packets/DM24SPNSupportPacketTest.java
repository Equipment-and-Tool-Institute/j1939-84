/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939tools.j1939.packets;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.etools.j1939tools.bus.Packet;
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

        String expected = "DM24 from Engine #1 (0): [" + NL;
        expected += "  D F T D F" + NL;
        expected += "  a r e M F" + NL;
        expected += "  t F s 5 l" + NL;
        expected += "  a r t 8 n  SPN — SP Name" + NL;
        expected += "  ------------------------" + NL;
        expected += "  D F     2  SPN 27 - Engine EGR 1 Valve Position" + NL;
        expected += "  D F     1  SPN 51 - Engine Throttle Valve 1 Position 1" + NL;
        expected += "  D F     2  SPN 84 - Wheel-Based Vehicle Speed" + NL;
        expected += "  D F     1  SPN 91 - Accelerator Pedal Position 1" + NL;
        expected += "  D F     1  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expected += "  D F     1  SPN 94 - Engine Fuel Delivery Pressure" + NL;
        expected += "  D F T   1  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expected += "  D F     1  SPN 105 - Engine Intake Manifold 1 Temperature" + NL;
        expected += "  D F     1  SPN 108 - Barometric Pressure" + NL;
        expected += "  D F     1  SPN 110 - Engine Coolant Temperature" + NL;
        expected += "  D F     2  SPN 132 - Engine Intake Air Mass Flow Rate" + NL;
        expected += "          2  SPN 157 - Engine Fuel 1 Injector Metering Rail 1 Pressure" + NL;
        expected += "  D F     2  SPN 158 - Key Switch Battery Potential" + NL;
        expected += "  D F T   2  SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expected += "  D       2  SPN 171 - Ambient Air Temperature" + NL;
        expected += "  D F     1  SPN 172 - Engine Intake 1 Air Temperature" + NL;
        expected += "  D F     2  SPN 173 - Engine Exhaust Temperature" + NL;
        expected += "  D F     2  SPN 175 - Engine Oil Temperature 1" + NL;
        expected += "  D F     2  SPN 183 - Engine Fuel Rate" + NL;
        expected += "  D F     2  SPN 190 - Engine Speed" + NL;
        expected += "  D       4  SPN 235 - Engine Total Idle Hours" + NL;
        expected += "  D       17 SPN 237 - Vehicle Identification Number" + NL;
        expected += "  D       4  SPN 247 - Engine Total Hours of Operation" + NL;
        expected += "  D       4  SPN 248 - Total Power Takeoff Hours" + NL;
        expected += "  D F     1  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expected += "  D F     1  SPN 513 - Actual Engine - Percent Torque" + NL;
        expected += "  D F     1  SPN 539 - Engine Percent Torque At Idle, Point 1" + NL;
        expected += "  D F     1  SPN 540 - Engine Percent Torque At Point 2" + NL;
        expected += "  D F     1  SPN 541 - Engine Percent Torque At Point 3" + NL;
        expected += "  D F     1  SPN 542 - Engine Percent Torque At Point 4" + NL;
        expected += "  D F     1  SPN 543 - Engine Percent Torque At Point 5" + NL;
        expected += "  D F     2  SPN 544 - Engine Reference Torque" + NL;
        expected += "  D       17 SPN 588 - Serial Number" + NL;
        expected += "      T   1  SPN 651 - Engine Fuel 1 Injector Cylinder 1" + NL;
        expected += "      T   1  SPN 652 - Engine Fuel 1 Injector Cylinder 2" + NL;
        expected += "      T   1  SPN 653 - Engine Fuel 1 Injector Cylinder 3" + NL;
        expected += "      T   1  SPN 654 - Engine Fuel 1 Injector Cylinder 4" + NL;
        expected += "      T   1  SPN 655 - Engine Fuel 1 Injector Cylinder 5" + NL;
        expected += "      T   1  SPN 656 - Engine Fuel 1 Injector Cylinder 6" + NL;
        expected += "  D       1  SPN 899 - Engine Torque Mode" + NL;
        expected += "  D F     1  SPN 976 - PTO Governor State" + NL;
        expected += "  D F     1  SPN 1081 - Engine Wait to Start Lamp" + NL;
        expected += "  D F T   2  SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        expected += "  D F     1  SPN 1189 - Engine Turbocharger Wastegate Actuator 2 Position" + NL;
        expected += "  D F     2  SPN 1209 - Engine Exhaust Pressure 1" + NL;
        expected += "  D       1  SPN 1213 - Malfunction Indicator Lamp" + NL;
        expected += "  D       1  SPN 1216 - Occurrence Count" + NL;
        expected += "  D       1  SPN 1220 - OBD Compliance" + NL;
        expected += "      T   1  SPN 1322 - Engine Misfire for Multiple Cylinders" + NL;
        expected += "      T   1  SPN 1323 - Engine Cylinder 1 Misfire Rate" + NL;
        expected += "      T   1  SPN 1324 - Engine Cylinder 2 Misfire Rate" + NL;
        expected += "      T   1  SPN 1325 - Engine Cylinder 3 Misfire Rate" + NL;
        expected += "      T   1  SPN 1326 - Engine Cylinder 4 Misfire Rate" + NL;
        expected += "      T   1  SPN 1327 - Engine Cylinder 5 Misfire Rate" + NL;
        expected += "      T   1  SPN 1328 - Engine Cylinder 6 Misfire Rate" + NL;
        expected += "  D F     2  SPN 1413 - Engine Cylinder 1 Ignition Timing" + NL;
        expected += "  D F     2  SPN 1414 - Engine Cylinder 2 Ignition Timing" + NL;
        expected += "  D F     2  SPN 1415 - Engine Cylinder 3 Ignition Timing" + NL;
        expected += "  D F     2  SPN 1416 - Engine Cylinder 4 Ignition Timing" + NL;
        expected += "  D F     2  SPN 1417 - Engine Cylinder 5 Ignition Timing" + NL;
        expected += "  D F     2  SPN 1418 - Engine Cylinder 6 Ignition Timing" + NL;
        expected += "  D F     1  SPN 1761 - AFT 1 DEF Tank Volume" + NL;
        expected += "          1  SPN 2304 - Aux Valve 12 Extend Port Pressure" + NL;
        expected += "          1  SPN 2305 - Aux Valve 12 Retract Port Pressure" + NL;
        expected += "          1  SPN 2307 - Aux Valve 12 Port Flow Command" + NL;
        expected += "  D       1  SPN 2623 - Accelerator Pedal #1 Channel 2" + NL;
        expected += "      T   2  SPN 2630 - Engine Charge Air Cooler 1 Outlet Temperature" + NL;
        expected += "      T   2  SPN 2659 - Engine EGR 1 Mass Flow Rate" + NL;
        expected += "  D F     2  SPN 2791 - Engine EGR 1 Valve 1 Control 1" + NL;
        expected += "    F     1  SPN 3031 - AFT 1 DEF Tank Temperature 1" + NL;
        expected += "          1  SPN 3055 - Engine Fuel System Monitor" + NL;
        expected += "  D       1  SPN 3069 - Distance Traveled While MIL is Activated" + NL;
        expected += "  D F T   2  SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expected += "  D F     2  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expected += "  D F     2  SPN 3242 - AFT 1 DPF Intake Temperature" + NL;
        expected += "  D F     2  SPN 3246 - AFT 1 DPF Outlet Temperature" + NL;
        expected += "  D F     2  SPN 3251 - AFT 1 DPF Differential Pressure" + NL;
        expected += "  D       1  SPN 3294 - Distance Since DTCs Cleared" + NL;
        expected += "  D       1  SPN 3295 - Minutes Run by Engine While MIL is Activated" + NL;
        expected += "  D       1  SPN 3296 - Time Since Diagnostic Trouble Codes Cleared" + NL;
        expected += "  D F     2  SPN 3301 - Time Since Engine Start" + NL;
        expected += "  D       1  SPN 3302 - Number of Warm-ups Since DTCs Cleared" + NL;
        expected += "  D       1  SPN 3303 - Continuously Monitored Systems Enable/Completed Status" + NL;
        expected += "  D       1  SPN 3304 - Non-continuously Monitored Systems Enable Status" + NL;
        expected += "  D F     2  SPN 3464 - Engine Throttle Actuator 1 Control Command" + NL;
        expected += "      T   1  SPN 3471 - AFT 1 Fuel Pressure Control Actuator" + NL;
        expected += "    F     2  SPN 3480 - AFT 1 Fuel Pressure 1" + NL;
        expected += "    F     1  SPN 3482 - AFT 1 Fuel Enable Actuator" + NL;
        expected += "  D F     1  SPN 3483 - AFT 1 Regeneration Status" + NL;
        expected += "    F     1  SPN 3490 - AFT 1 Purge Air Actuator" + NL;
        expected += "      T   1  SPN 3556 - AFT 1 Hydrocarbon Doser 1" + NL;
        expected += "  D F     1  SPN 3563 - Engine Intake Manifold #1 Absolute Pressure" + NL;
        expected += "    F     2  SPN 3609 - AFT 1 DPF Intake Pressure" + NL;
        expected += "  D       1  SPN 3719 - AFT 1 DPF Soot Load Percent" + NL;
        expected += "  D       1  SPN 4106 - Emission-Related MIL-On DTC Count" + NL;
        expected += "  D       1  SPN 4127 - NOx NTE Control Area Status" + NL;
        expected += "  D       1  SPN 4130 - PM NTE Control Area Status" + NL;
        expected += "      T   1  SPN 4257 - Engine Fuel 1 Injector Group 3" + NL;
        expected += "    F     2  SPN 4360 - AFT 1 SCR Intake Temperature" + NL;
        expected += "    F     2  SPN 4363 - AFT 1 SCR Outlet Temperature" + NL;
        expected += "          1  SPN 4421 - AFT 2 DEF Concentration" + NL;
        expected += "      T   1  SPN 4752 - Engine EGR 1 Cooler Efficiency" + NL;
        expected += "          2  SPN 4766 - AFT 1 Diesel Oxidation Catalyst Outlet Temperature" + NL;
        expected += "  D F     2  SPN 5314 - Commanded Engine Fuel Injection Control Pressure" + NL;
        expected += "          1  SPN 5319 - AFT 1 DPF Incomplete Regeneration" + NL;
        expected += "  D F     1  SPN 5323 - Engine Fuel Control Mode" + NL;
        expected += "  D       4  SPN 5454 - AFT 1 DPF Average Time Between Active Regenerations" + NL;
        expected += "  D       1  SPN 5463 - AFT SCR Operator Inducement Active Traveled Distance" + NL;
        expected += "  D F     2  SPN 5466 - AFT 1 DPF Soot Load Regeneration Threshold" + NL;
        expected += "  D F     1  SPN 5541 - Engine Turbocharger 1 Turbine Outlet Pressure" + NL;
        expected += "  D F     1  SPN 5837 - Fuel Type" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());

        List<SupportedSPN> actualSPNs = instance.getFreezeFrameSPNsInOrder();
        StringBuilder actualSPNString = new StringBuilder();
        for (SupportedSPN spn : actualSPNs) {
            actualSPNString.append(spn).append(NL);
        }
        String expectedSPNString = "";
        expectedSPNString += "SPN 92 - Engine Percent Load At Current Speed" + NL;
        expectedSPNString += "SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expectedSPNString += "SPN 513 - Actual Engine - Percent Torque" + NL;
        expectedSPNString += "SPN 544 - Engine Reference Torque" + NL;
        expectedSPNString += "SPN 539 - Engine Percent Torque At Idle, Point 1" + NL;
        expectedSPNString += "SPN 540 - Engine Percent Torque At Point 2" + NL;
        expectedSPNString += "SPN 541 - Engine Percent Torque At Point 3" + NL;
        expectedSPNString += "SPN 542 - Engine Percent Torque At Point 4" + NL;
        expectedSPNString += "SPN 543 - Engine Percent Torque At Point 5" + NL;
        expectedSPNString += "SPN 110 - Engine Coolant Temperature" + NL;
        expectedSPNString += "SPN 175 - Engine Oil Temperature 1" + NL;
        expectedSPNString += "SPN 190 - Engine Speed" + NL;
        expectedSPNString += "SPN 84 - Wheel-Based Vehicle Speed" + NL;
        expectedSPNString += "SPN 108 - Barometric Pressure" + NL;
        expectedSPNString += "SPN 158 - Key Switch Battery Potential" + NL;
        expectedSPNString += "SPN 51 - Engine Throttle Valve 1 Position 1" + NL;
        expectedSPNString += "SPN 94 - Engine Fuel Delivery Pressure" + NL;
        expectedSPNString += "SPN 172 - Engine Intake 1 Air Temperature" + NL;
        expectedSPNString += "SPN 105 - Engine Intake Manifold 1 Temperature" + NL;
        expectedSPNString += "SPN 132 - Engine Intake Air Mass Flow Rate" + NL;
        expectedSPNString += "SPN 976 - PTO Governor State" + NL;
        expectedSPNString += "SPN 91 - Accelerator Pedal Position 1" + NL;
        expectedSPNString += "SPN 183 - Engine Fuel Rate" + NL;
        expectedSPNString += "SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expectedSPNString += "SPN 173 - Engine Exhaust Temperature" + NL;
        expectedSPNString += "SPN 3251 - AFT 1 DPF Differential Pressure" + NL;
        expectedSPNString += "SPN 3483 - AFT 1 Regeneration Status" + NL;
        expectedSPNString += "SPN 5837 - Fuel Type" + NL;
        expectedSPNString += "SPN 3301 - Time Since Engine Start" + NL;
        expectedSPNString += "SPN 5466 - AFT 1 DPF Soot Load Regeneration Threshold" + NL;
        expectedSPNString += "SPN 5323 - Engine Fuel Control Mode" + NL;
        expectedSPNString += "SPN 3464 - Engine Throttle Actuator 1 Control Command" + NL;
        expectedSPNString += "SPN 1209 - Engine Exhaust Pressure 1" + NL;
        expectedSPNString += "SPN 5541 - Engine Turbocharger 1 Turbine Outlet Pressure" + NL;
        expectedSPNString += "SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expectedSPNString += "SPN 2791 - Engine EGR 1 Valve 1 Control 1" + NL;
        expectedSPNString += "SPN 1413 - Engine Cylinder 1 Ignition Timing" + NL;
        expectedSPNString += "SPN 1414 - Engine Cylinder 2 Ignition Timing" + NL;
        expectedSPNString += "SPN 1415 - Engine Cylinder 3 Ignition Timing" + NL;
        expectedSPNString += "SPN 1416 - Engine Cylinder 4 Ignition Timing" + NL;
        expectedSPNString += "SPN 1417 - Engine Cylinder 5 Ignition Timing" + NL;
        expectedSPNString += "SPN 1418 - Engine Cylinder 6 Ignition Timing" + NL;
        expectedSPNString += "SPN 3563 - Engine Intake Manifold #1 Absolute Pressure" + NL;
        expectedSPNString += "SPN 27 - Engine EGR 1 Valve Position" + NL;
        expectedSPNString += "SPN 3242 - AFT 1 DPF Intake Temperature" + NL;
        expectedSPNString += "SPN 3246 - AFT 1 DPF Outlet Temperature" + NL;
        expectedSPNString += "SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expectedSPNString += "SPN 1081 - Engine Wait to Start Lamp" + NL;
        expectedSPNString += "SPN 1189 - Engine Turbocharger Wastegate Actuator 2 Position" + NL;
        expectedSPNString += "SPN 5314 - Commanded Engine Fuel Injection Control Pressure" + NL;
        expectedSPNString += "SPN 3609 - AFT 1 DPF Intake Pressure" + NL;
        expectedSPNString += "SPN 3480 - AFT 1 Fuel Pressure 1" + NL;
        expectedSPNString += "SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expectedSPNString += "SPN 1761 - AFT 1 DEF Tank Volume" + NL;
        expectedSPNString += "SPN 3482 - AFT 1 Fuel Enable Actuator" + NL;
        expectedSPNString += "SPN 3490 - AFT 1 Purge Air Actuator" + NL;
        expectedSPNString += "SPN 4360 - AFT 1 SCR Intake Temperature" + NL;
        expectedSPNString += "SPN 4363 - AFT 1 SCR Outlet Temperature" + NL;
        expectedSPNString += "SPN 3031 - AFT 1 DEF Tank Temperature 1" + NL;
        expectedSPNString += "SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        assertEquals(expectedSPNString, actualSPNString.toString());

        String expectedFreezeFrame = "";
        expectedFreezeFrame += "SPs Supported in Expanded Freeze Frame from Engine #1 (0): [" + NL;
        expectedFreezeFrame += "  LN  SPN — SP Name" + NL;
        expectedFreezeFrame += "  -----------------" + NL;
        expectedFreezeFrame += "   1  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expectedFreezeFrame += "   1  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expectedFreezeFrame += "   1  SPN 513 - Actual Engine - Percent Torque" + NL;
        expectedFreezeFrame += "   2  SPN 544 - Engine Reference Torque" + NL;
        expectedFreezeFrame += "   1  SPN 539 - Engine Percent Torque At Idle, Point 1" + NL;
        expectedFreezeFrame += "   1  SPN 540 - Engine Percent Torque At Point 2" + NL;
        expectedFreezeFrame += "   1  SPN 541 - Engine Percent Torque At Point 3" + NL;
        expectedFreezeFrame += "   1  SPN 542 - Engine Percent Torque At Point 4" + NL;
        expectedFreezeFrame += "   1  SPN 543 - Engine Percent Torque At Point 5" + NL;
        expectedFreezeFrame += "   1  SPN 110 - Engine Coolant Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 175 - Engine Oil Temperature 1" + NL;
        expectedFreezeFrame += "   2  SPN 190 - Engine Speed" + NL;
        expectedFreezeFrame += "   2  SPN 84 - Wheel-Based Vehicle Speed" + NL;
        expectedFreezeFrame += "   1  SPN 108 - Barometric Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 158 - Key Switch Battery Potential" + NL;
        expectedFreezeFrame += "   1  SPN 51 - Engine Throttle Valve 1 Position 1" + NL;
        expectedFreezeFrame += "   1  SPN 94 - Engine Fuel Delivery Pressure" + NL;
        expectedFreezeFrame += "   1  SPN 172 - Engine Intake 1 Air Temperature" + NL;
        expectedFreezeFrame += "   1  SPN 105 - Engine Intake Manifold 1 Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 132 - Engine Intake Air Mass Flow Rate" + NL;
        expectedFreezeFrame += "   1  SPN 976 - PTO Governor State" + NL;
        expectedFreezeFrame += "   1  SPN 91 - Accelerator Pedal Position 1" + NL;
        expectedFreezeFrame += "   2  SPN 183 - Engine Fuel Rate" + NL;
        expectedFreezeFrame += "   1  SPN 102 - Engine Intake Manifold #1 Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 173 - Engine Exhaust Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 3251 - AFT 1 DPF Differential Pressure" + NL;
        expectedFreezeFrame += "   1  SPN 3483 - AFT 1 Regeneration Status" + NL;
        expectedFreezeFrame += "   1  SPN 5837 - Fuel Type" + NL;
        expectedFreezeFrame += "   2  SPN 3301 - Time Since Engine Start" + NL;
        expectedFreezeFrame += "   2  SPN 5466 - AFT 1 DPF Soot Load Regeneration Threshold" + NL;
        expectedFreezeFrame += "   1  SPN 5323 - Engine Fuel Control Mode" + NL;
        expectedFreezeFrame += "   2  SPN 3464 - Engine Throttle Actuator 1 Control Command" + NL;
        expectedFreezeFrame += "   2  SPN 1209 - Engine Exhaust Pressure 1" + NL;
        expectedFreezeFrame += "   1  SPN 5541 - Engine Turbocharger 1 Turbine Outlet Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 164 - Engine Fuel Injection Control Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 2791 - Engine EGR 1 Valve 1 Control 1" + NL;
        expectedFreezeFrame += "   2  SPN 1413 - Engine Cylinder 1 Ignition Timing" + NL;
        expectedFreezeFrame += "   2  SPN 1414 - Engine Cylinder 2 Ignition Timing" + NL;
        expectedFreezeFrame += "   2  SPN 1415 - Engine Cylinder 3 Ignition Timing" + NL;
        expectedFreezeFrame += "   2  SPN 1416 - Engine Cylinder 4 Ignition Timing" + NL;
        expectedFreezeFrame += "   2  SPN 1417 - Engine Cylinder 5 Ignition Timing" + NL;
        expectedFreezeFrame += "   2  SPN 1418 - Engine Cylinder 6 Ignition Timing" + NL;
        expectedFreezeFrame += "   1  SPN 3563 - Engine Intake Manifold #1 Absolute Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 27 - Engine EGR 1 Valve Position" + NL;
        expectedFreezeFrame += "   2  SPN 3242 - AFT 1 DPF Intake Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 3246 - AFT 1 DPF Outlet Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 3216 - Engine Exhaust 1 NOx 1" + NL;
        expectedFreezeFrame += "   1  SPN 1081 - Engine Wait to Start Lamp" + NL;
        expectedFreezeFrame += "   1  SPN 1189 - Engine Turbocharger Wastegate Actuator 2 Position" + NL;
        expectedFreezeFrame += "   2  SPN 5314 - Commanded Engine Fuel Injection Control Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 3609 - AFT 1 DPF Intake Pressure" + NL;
        expectedFreezeFrame += "   2  SPN 3480 - AFT 1 Fuel Pressure 1" + NL;
        expectedFreezeFrame += "   2  SPN 3226 - AFT 1 Outlet NOx 1" + NL;
        expectedFreezeFrame += "   1  SPN 1761 - AFT 1 DEF Tank Volume" + NL;
        expectedFreezeFrame += "   1  SPN 3482 - AFT 1 Fuel Enable Actuator" + NL;
        expectedFreezeFrame += "   1  SPN 3490 - AFT 1 Purge Air Actuator" + NL;
        expectedFreezeFrame += "   2  SPN 4360 - AFT 1 SCR Intake Temperature" + NL;
        expectedFreezeFrame += "   2  SPN 4363 - AFT 1 SCR Outlet Temperature" + NL;
        expectedFreezeFrame += "   1  SPN 3031 - AFT 1 DEF Tank Temperature 1" + NL;
        expectedFreezeFrame += "   2  SPN 1173 - Engine Turbocharger 2 Compressor Intake Temperature" + NL;
        expectedFreezeFrame += "]" + NL;
        assertEquals(expectedFreezeFrame, instance.printFreezeFrameSPNsInOrder());
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
        String expected = "DM24 from Engine #1 (0): [" + NL;
        expected += "  D F T D F" + NL;
        expected += "  a r e M F" + NL;
        expected += "  t F s 5 l" + NL;
        expected += "  a r t 8 n  SPN — SP Name" + NL;
        expected += "  ------------------------" + NL;
        expected += "      T   1  SPN 92 - Engine Percent Load At Current Speed" + NL;
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
        SupportedSPN spn92 = SupportedSPN.create(92, true, false, false, false, 1);
        SupportedSPN spn512 = SupportedSPN.create(512, true, false, false, false, 1);
        SupportedSPN spn513 = SupportedSPN.create(513, true, false, false, false, 1);

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
        String expected = "DM24 from Engine #1 (0): [" + NL;
        expected += "  D F T D F" + NL;
        expected += "  a r e M F" + NL;
        expected += "  t F s 5 l" + NL;
        expected += "  a r t 8 n  SPN — SP Name" + NL;
        expected += "  ------------------------" + NL;
        expected += "      T   1  SPN 92 - Engine Percent Load At Current Speed" + NL;
        expected += "      T   1  SPN 512 - Driver's Demand Engine - Percent Torque" + NL;
        expected += "      T   1  SPN 513 - Actual Engine - Percent Torque" + NL;
        expected += "]" + NL;
        assertEquals(expected, instance.toString());
    }

}
