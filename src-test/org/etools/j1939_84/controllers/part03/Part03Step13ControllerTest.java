/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA2ValueValidator;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part03Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 13;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    @Mock
    private TableA2ValueValidator validator;

    private StepController instance;

    private static DM24SPNSupportPacket createDM24() {
       //@formatter:off
       int[] data = {
             0x5B, 0x00, 0x1C, 0x01, 0x1B, 0x00, 0x1C, 0x02, 0x01, 0x02, 0x1C, 0x01, 0x84, 0x00, 0x1C, 0x02, 0x91,
             0x0C, 0x18, 0x02, 0xAB, 0x00, 0x1C, 0x02, 0x6C, 0x00, 0x1C, 0x01, 0x66, 0x00, 0x18, 0x01, 0x5C, 0x00,
             0x1C, 0x01, 0xE7, 0x0A, 0x1C, 0x02, 0xC1, 0x14, 0x1C, 0x02, 0xC9, 0x16, 0x1C, 0x02, 0xCD, 0x16, 0x1C,
             0x01, 0x81, 0x02, 0x1C, 0x01, 0x9C, 0x06, 0x1C, 0x02, 0xC1, 0x0D, 0x1D, 0x00, 0x00, 0x02, 0x1C, 0x01,
             0x63, 0x0A, 0x1C, 0x02, 0xA8, 0x00, 0x1C, 0x02, 0x6E, 0x00, 0x1C, 0x01, 0x46, 0x0A, 0x18, 0x02, 0xAF,
             0x00, 0x1C, 0x02, 0xBE, 0x00, 0x1C, 0x02, 0xAD, 0x00, 0x1C, 0x02, 0xB9, 0x04, 0x19, 0x00, 0x9C, 0x05,
             0x1C, 0x02, 0x9D, 0x00, 0x18, 0x02, 0xA0, 0x05, 0x1C, 0x02, 0x69, 0x00, 0x1C, 0x01, 0xEB, 0x0D, 0x1C,
             0x01, 0x9A, 0x0C, 0x18, 0x02, 0x90, 0x0C, 0x18, 0x02, 0xB3, 0x0C, 0x1C, 0x02, 0x19, 0x0E, 0x1C, 0x02,
             0x9E, 0x12, 0x1C, 0x02, 0x1A, 0x0E, 0x1C, 0x02, 0xAE, 0x0C, 0x1C, 0x02, 0xCB, 0x16, 0x1B, 0x00, 0xD0,
             0x03, 0x1C, 0x01, 0xE5, 0x0C, 0x1C, 0x02, 0xF7, 0x00, 0x1C, 0x04, 0x98, 0x04, 0x1C, 0x02, 0x94, 0x04,
             0x1C, 0x02, 0x45, 0x0A, 0x1C, 0x02, 0x9C, 0x04, 0x1C, 0x02, 0xA0, 0x04, 0x1C, 0x02, 0x67, 0x00, 0x1C,
             0x02, 0xEB, 0x0A, 0x1C, 0x01, 0xA2, 0x0D, 0x1C, 0x01, 0x9C, 0x01, 0x1C, 0x02, 0x5E, 0x00, 0x1C, 0x01,
             0xB7, 0x00, 0x1C, 0x02, 0x39, 0x04, 0x1C, 0x01, 0x74, 0x0E, 0x1C, 0x01, 0xE1, 0x06, 0x1C, 0x01, 0x20,
             0x02, 0x1C, 0x02, 0x13, 0x02, 0x1C, 0x02, 0x12, 0x02, 0x1C, 0x02, 0x11, 0x02, 0x1C, 0x02, 0x10, 0x02,
             0x1C, 0x02, 0x1F, 0x02, 0x1C, 0x01, 0x1E, 0x02, 0x1C, 0x01, 0x1D, 0x02, 0x1C, 0x01, 0x1C, 0x02, 0x1C,
             0x01, 0x1B, 0x02, 0x1C, 0x01, 0x5A, 0x15, 0x1C, 0x02, 0x54, 0x00, 0x1C, 0x02, 0x51, 0x15, 0x1C, 0x01,
             0xC3, 0x16, 0x1D, 0x00, 0xBD, 0x04, 0x1D, 0x00, 0x9D, 0x12, 0x1D, 0x00, 0x08, 0x11, 0x1D, 0x00, 0x0B,
             0x11, 0x1D, 0x00, 0xEB, 0x00, 0x1D, 0x00, 0xF8, 0x00, 0x1D, 0x00, 0x57, 0x15, 0x1D, 0x00, 0xE6, 0x16,
             0x1D, 0x00, 0x3A, 0x10, 0x1D, 0x00, 0xBC, 0x0D, 0x1D, 0x01, 0xA4, 0x0C, 0x1D, 0x00, 0x35, 0x16, 0x1D,
             0x00, 0x65, 0x06, 0x1D, 0x00, 0xEF, 0x1A, 0x1D, 0x00, 0xC5, 0x16, 0x1D, 0x00, 0xED, 0x1A, 0x1D, 0x00,
             0x40, 0x06, 0x1D, 0x00, 0x64, 0x06, 0x1D, 0x00, 0x20, 0x10, 0x1D, 0x00, 0x23, 0x10, 0x1D, 0x00, 0xEE,
             0x1A, 0x1D, 0x00, 0x02, 0x02, 0x1D, 0x00, 0x1F, 0x10, 0x1D, 0x00, 0xE6, 0x0C, 0x1D, 0x00, 0x22, 0x10,
             0x1D, 0x00, 0x21, 0x10, 0x1D, 0x00, 0x24, 0x10, 0x1D, 0x00, 0xEB, 0x10, 0x1B, 0x00, 0xB2, 0x14, 0x1B,
             0x00, 0xBC, 0x12, 0x1B, 0x00, 0xC7, 0x14, 0x1B, 0x00, 0xBB, 0x12, 0x1B, 0x00, 0x15, 0x15, 0x1B, 0x00,
             0xA0, 0x13, 0x1B, 0x00, 0xBA, 0x12, 0x1B, 0x00, 0x0C, 0x11, 0x1B, 0x00, 0x8B, 0x02, 0x1B, 0x00, 0x8C,
             0x02, 0x1B, 0x00, 0x8D, 0x02, 0x1B, 0x00, 0x8E, 0x02, 0x1B, 0x00, 0x8F, 0x02, 0x1B, 0x00, 0x90, 0x02,
             0x1B, 0x00, 0x90, 0x12, 0x1B, 0x00, 0x42, 0x15, 0x1B, 0x00, 0xF2, 0x0B, 0x1B, 0x00, 0x2B, 0x05, 0x1B,
             0x00, 0x2C, 0x05, 0x1B, 0x00, 0x2D, 0x05, 0x1B, 0x00, 0x2E, 0x05, 0x1B, 0x00, 0x2F, 0x05, 0x1B, 0x00,
             0x30, 0x05, 0x1B, 0x00, 0x67, 0x04, 0x1B, 0x02, 0x9B, 0x0C, 0x1B, 0x00, 0x21, 0x0D, 0x1B, 0x00, 0xA7,
             0x13, 0x1B, 0x00, 0xA5, 0x1C, 0x1D, 0x00, 0xB9, 0x1C, 0x1D, 0x00, 0xA2, 0x0B, 0x1D, 0x00, 0xBB, 0x0D,
             0x1D, 0x00, 0x60, 0x00, 0x1C, 0x01, 0x24, 0x0D, 0x1B, 0x00, 0x60, 0x0F, 0x1B, 0x00, 0xC2, 0x14, 0x1B,
             0x00, 0x75, 0x1A, 0x1B, 0x00, 0x79, 0x1A, 0x1B, 0x00, 0xA1, 0x1C, 0x1B, 0x00, 0x9E, 0x00, 0x1C, 0x02,
             0xC3, 0x0D, 0x1C, 0x04, 0xB7, 0x1C, 0x1C, 0x02, 0x6A, 0x00, 0x1C, 0x01, 0x87, 0x0E, 0x1D, 0x01, 0xBC,
             0x00, 0x1C, 0x02, 0xE1, 0x1C, 0x1D, 0x00, 0xD7, 0x0B, 0x1D, 0x01, 0x2A, 0x05, 0x1B, 0x00, 0x9A, 0x1C,
             0x1B, 0x00, 0x85, 0x05, 0x1B, 0x00, 0x86, 0x05, 0x1B, 0x00, 0x87, 0x05, 0x1B, 0x00, 0x88, 0x05, 0x1B,
             0x00, 0x89, 0x05, 0x1B, 0x00, 0x8A, 0x05, 0x1B, 0x00
       };
       //@formatter:on
        return new DM24SPNSupportPacket(Packet.create(DM24SPNSupportPacket.PGN, 0, data));
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

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step13Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              validator);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              communicationsModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener,
                                 validator,
                                 communicationsModule);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPath() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(102, 4, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc), 3);
        moduleInfo.set(createDM24(), 1);
        dataRepository.putObdModule(moduleInfo);

        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(createDM25()));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(validator).reportWarnings(any(), any(), eq("6.3.13.2.e"));

        String expected = "";
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
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
        expected += "  }" + NL;
        expected += NL;
        assertEquals(expected, listener.getResults());
    }

    @Test
    public void testResponseWithRetry() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(102, 4, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc), 3);
        moduleInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(91, true, true, true, false, 1)), 1);
        dataRepository.putObdModule(moduleInfo);

        DM25ExpandedFreezeFrame dm25 = DM25ExpandedFreezeFrame.create(0, new FreezeFrame(dtc, Spn.create(91, 10)));
        when(communicationsModule.requestDM25(any(), eq(0), any()))
                                                               .thenReturn(new BusResult<>(true))
                                                               .thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule, times(2)).requestDM25(any(), eq(0), any());
        verify(validator).reportWarnings(any(), any(), eq("6.3.13.2.e"));

        String expected = "";
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 1" + NL;
        expected += "    DTC 102:4 - Engine Intake Manifold #1 Pressure, Voltage Below Normal, Or Shorted To Low Source - 1 times"
                + NL;
        expected += "    SPN Data: 19" + NL;
        expected += "    SPN    91, Accelerator Pedal 1 Position: 10.000 %" + NL;
        expected += "  }" + NL;
        expected += NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.a - Retry was required to obtain DM25 response from Engine #1 (0)");
    }

    @Test
    public void testFailureForDifferentDTC() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        DiagnosticTroubleCode dtc1 = DiagnosticTroubleCode.create(555, 4, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1), 3);
        DM24SPNSupportPacket dm24 = DM24SPNSupportPacket.create(0, SupportedSPN.create(91, true, true, true, false, 1));
        moduleInfo.set(dm24, 1);
        dataRepository.putObdModule(moduleInfo);

        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(102, 4, 0, 1);
        DM25ExpandedFreezeFrame dm25 = DM25ExpandedFreezeFrame.create(0, new FreezeFrame(dtc, Spn.create(91, 10)));
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(validator).reportWarnings(any(), any(), eq("6.3.13.2.e"));

        String expected = "";
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 1" + NL;
        expected += "    DTC 102:4 - Engine Intake Manifold #1 Pressure, Voltage Below Normal, Or Shorted To Low Source - 1 times"
                + NL;
        expected += "    SPN Data: 19" + NL;
        expected += "    SPN    91, Accelerator Pedal 1 Position: 10.000 %" + NL;
        expected += "  }" + NL;
        expected += NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.d - Freeze frame data from Engine #1 (0) does not include the same SPN+FMI as DM6 Pending DTC earlier in this part.");
    }

    @Test
    public void testFailureMultipleFreezeFrames() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        DiagnosticTroubleCode dtc1 = DiagnosticTroubleCode.create(102, 4, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1), 3);
        DM24SPNSupportPacket dm24 = DM24SPNSupportPacket.create(0, SupportedSPN.create(91, true, true, true, false, 1));
        moduleInfo.set(dm24, 1);
        dataRepository.putObdModule(moduleInfo);

        DiagnosticTroubleCode dtc2 = DiagnosticTroubleCode.create(999, 4, 0, 2);
        DM25ExpandedFreezeFrame dm25 = DM25ExpandedFreezeFrame.create(0,
                                                                      new FreezeFrame(dtc1, Spn.create(91, 10)),
                                                                      new FreezeFrame(dtc2, Spn.create(91, 100)));
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(validator, times(2)).reportWarnings(any(), any(), eq("6.3.13.2.e"));

        String expected = "";
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 1" + NL;
        expected += "    DTC 102:4 - Engine Intake Manifold #1 Pressure, Voltage Below Normal, Or Shorted To Low Source - 1 times"
                + NL;
        expected += "    SPN Data: 19" + NL;
        expected += "    SPN    91, Accelerator Pedal 1 Position: 10.000 %" + NL;
        expected += "  }" + NL;
        expected += NL;
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 1" + NL;
        expected += "    DTC 999:4 - Trip Gear Down Distance, Voltage Below Normal, Or Shorted To Low Source - 2 times"
                + NL;
        expected += "    SPN Data: FA" + NL;
        expected += "    SPN    91, Accelerator Pedal 1 Position: 100.000 %" + NL;
        expected += "  }" + NL;
        expected += "" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.13.2.e - More than 1 freeze frame data set is included in the response from Engine #1 (0)");
    }

    @Test
    public void testFailureMismatchedDataLengths() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(102, 4, 0, 1);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc), 3);
        DM24SPNSupportPacket dm24 = DM24SPNSupportPacket.create(0,
                                                                SupportedSPN.create(91, true, true, true, false, 1),
                                                                SupportedSPN.create(92, true, true, true, false, 1));
        moduleInfo.set(dm24, 1);
        dataRepository.putObdModule(moduleInfo);

        DM25ExpandedFreezeFrame dm25 = DM25ExpandedFreezeFrame.create(0, new FreezeFrame(dtc, Spn.create(91, 10)));
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(validator).reportWarnings(any(), any(), eq("6.3.13.2.e"));

        String expected = "";
        expected += "6.3.13.1.c - Received from Engine #1 (0): " + NL;
        expected += "  Freeze Frame: {" + NL;
        expected += "    Data Length: 1" + NL;
        expected += "    DTC 102:4 - Engine Intake Manifold #1 Pressure, Voltage Below Normal, Or Shorted To Low Source - 1 times"
                + NL;
        expected += "    SPN Data: 19" + NL;
        expected += "  }" + NL;
        expected += NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.c - Received data (1) does not match expected number of bytes (2) based on DM24 supported SPN list for Engine #1 (0)");
    }

    @Test
    public void testNoResponses() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        dataRepository.putObdModule(moduleInfo);

        when(communicationsModule.requestDM25(any(), eq(0), any()))
                                                               .thenReturn(new BusResult<>(true))
                                                               .thenReturn(new BusResult<>(true));

        runTest();

        verify(communicationsModule, times(2)).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.a - Retry was required to obtain DM25 response from Engine #1 (0)");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.b - No ECU has freeze frame data to report");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.13.2.g - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }
}
