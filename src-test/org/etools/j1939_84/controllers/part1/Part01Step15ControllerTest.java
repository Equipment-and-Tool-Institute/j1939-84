/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket.PGN;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step15Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 15;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step15Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part01Step15Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticMessageModule,
                dataRepository,
                new TestDateTimeModule());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                                 diagnosticMessageModule,
                mockListener);
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#Part01Step15Controller(DataRepository)}.
     */
    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.15.2 - No OBD ECU provided a DM1");

        String expectedResults = "FAIL: 6.1.15.2 - No OBD ECU provided a DM1" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#Part01Step15Controller(DataRepository)}.
     */
    @Test
    public void testFailures() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x01, 0x00, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x17, 0x00, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet3 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x03, 0xAA, 0x55, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet4 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x00, 0x40, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet5 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x00, 0xC0, 0xC0, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any()))
                .thenReturn(new RequestResult<>(false, packet1, packet2, packet3, packet4, packet5));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.a - OBD Module Engine #2 (1) reported an active DTC");
        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.a - OBD Module Transmission #1 (3) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.b - OBD Module Engine #2 (1) did not report MIL off per Section A.8 allowed values");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.b - OBD Module Transmission #1 (3) did not report MIL off per Section A.8 allowed values");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                WARN,
                "6.1.15.3.a - OBD Module Engine #2 (1) reported the non-preferred MIL off format per Section A.8");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.d - OBD Module Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.d - OBD Module Transmission #1 (3) reported SPN conversion method (SPN 1706) equal to binary 1");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.c - Non-OBD Module Instrument Cluster #1 (23) did not report MIL off or not supported");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.15.2.c - Non-OBD Module Engine #1 (0) did not report MIL off or not supported");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                WARN,
                "6.1.15.3.b - Non-OBD Module Instrument Cluster #1 (23) reported SPN conversion method (SPN 1706) equal to 1");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                WARN,
                "6.1.15.3.b - Non-OBD Module Engine #1 (0) reported SPN conversion method (SPN 1706) equal to 1");

        String expected = "" + NL;
        expected += "10:15:30.0000 18FECA01 [14] 00 00 61 02 13 80 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #2 (1): MIL: alternate off, RSL: alternate off, AWL: alternate off, PL: alternate off"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "FAIL: 6.1.15.2.a - OBD Module Engine #2 (1) reported an active DTC" + NL;
        expected += "FAIL: 6.1.15.2.b - OBD Module Engine #2 (1) did not report MIL off per Section A.8 allowed values"
                + NL;
        expected += "WARN: 6.1.15.3.a - OBD Module Engine #2 (1) reported the non-preferred MIL off format per Section A.8"
                + NL;
        expected += "FAIL: 6.1.15.2.d - OBD Module Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 18FECA17 [14] 00 00 61 02 13 80 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Instrument Cluster #1 (23): MIL: alternate off, RSL: alternate off, AWL: alternate off, PL: alternate off"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "FAIL: 6.1.15.2.c - Non-OBD Module Instrument Cluster #1 (23) did not report MIL off or not supported"
                + NL;
        expected += "WARN: 6.1.15.3.b - Non-OBD Module Instrument Cluster #1 (23) reported SPN conversion method (SPN 1706) equal to 1"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 18FECA03 [14] AA 55 61 02 13 80 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Transmission #1 (3): MIL: other, RSL: other, AWL: other, PL: other" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "FAIL: 6.1.15.2.a - OBD Module Transmission #1 (3) reported an active DTC" + NL;
        expected += "FAIL: 6.1.15.2.b - OBD Module Transmission #1 (3) did not report MIL off per Section A.8 allowed values"
                + NL;
        expected += "FAIL: 6.1.15.2.d - OBD Module Transmission #1 (3) reported SPN conversion method (SPN 1706) equal to binary 1"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 18FECA00 [14] 40 00 61 02 13 80 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #1 (0): MIL: slow flash, RSL: alternate off, AWL: alternate off, PL: alternate off"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;
        expected += "FAIL: 6.1.15.2.c - Non-OBD Module Engine #1 (0) did not report MIL off or not supported" + NL;
        expected += "WARN: 6.1.15.3.b - Non-OBD Module Engine #1 (0) reported SPN conversion method (SPN 1706) equal to 1"
                + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 18FECA00 [14] C0 C0 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Engine #1 (0): MIL: not supported, RSL: alternate off, AWL: alternate off, PL: alternate off"
                + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for {@link Part01Step15Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testRun() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x01, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x17, 0x00, 0xFF, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                        0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(new RequestResult<>(false, packet1, packet2));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        String expected = "" + NL;
        expected += "10:15:30.0000 18FECA01 [8] 00 FF 00 00 00 00 FF FF" + NL;
        expected += "DM1 from Engine #2 (1): MIL: off, RSL: off, AWL: off, PL: off, No DTCs" + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 18FECA17 [14] 00 FF 61 02 13 00 21 06 1F 00 EE 10 04 00" + NL;
        expected += "DM1 from Instrument Cluster #1 (23): MIL: off, RSL: off, AWL: off, PL: off" + NL;
        expected += "DTC 609:19 - Controller #2, Received Network Data In Error - 0 times" + NL;
        expected += "DTC 1569:31 - Engine Protection Torque Derate, Condition Exists - 0 times" + NL;
        expected += "DTC 4334:4 - AFT 1 DEF Doser 1 Absolute Pressure, Voltage Below Normal, Or Shorted To Low Source - 0 times"
                + NL;

        assertEquals(expected, listener.getResults());
    }
}
