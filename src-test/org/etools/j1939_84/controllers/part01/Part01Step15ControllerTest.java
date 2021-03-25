/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket.PGN;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
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

    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.15.2 - No OBD ECU provided a DM1");

        assertEquals("", listener.getResults());
    }

    @Test
    // @Ignore("This test needs broken up")
    public void testActiveDtcFailure() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x01,
                                                                            0x00,
                                                                            0x00,
                                                                            0x61,
                                                                            0x02,
                                                                            0x13,
                                                                            0x80,
                                                                            0x21,
                                                                            0x06,
                                                                            0x1F,
                                                                            0x00,
                                                                            0xEE,
                                                                            0x10,
                                                                            0x04,
                                                                            0x00));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x17,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet3 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x03,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet4 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x05,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet5 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1, packet2, packet3, packet4, packet5));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Engine #2 (1) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }

    @Test
    // @Ignore("This test needs broken up")
    public void testFailure() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x01,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x17,
                                                                            0x00,
                                                                            0x00,
                                                                            0x61,
                                                                            0x02,
                                                                            0x13,
                                                                            0x80,
                                                                            0x21,
                                                                            0x06,
                                                                            0x1F,
                                                                            0x00,
                                                                            0xEE,
                                                                            0x10,
                                                                            0x04,
                                                                            0x00));
        DM1ActiveDTCsPacket packet3 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x03,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet4 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet5 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x05,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1, packet2, packet3, packet4, packet5));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.c - Non-OBD ECU Instrument Cluster #1 (23) did not report MIL off or not supported");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.15.3.b - Non-OBD ECU Instrument Cluster #1 (23) reported SPN conversion method (SPN 1706) equal to 1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testSpnConversionFailures() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x01,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x17,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 5);
        DM1ActiveDTCsPacket packet3 = DM1ActiveDTCsPacket.create(0x03,
                                                                 LampStatus.OFF,
                                                                 LampStatus.OFF,
                                                                 LampStatus.OFF,
                                                                 LampStatus.OFF,
                                                                 dtc);

        DM1ActiveDTCsPacket packet4 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x85,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet5 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1, packet2, packet3, packet4, packet5));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Transmission #1 (3) reported an active DTC");

        // verify(mockListener).addOutcome(PART_NUMBER,
        // STEP_NUMBER,
        // FAIL,
        // "6.1.15.2.b - OBD ECU Transmission #1 (3) did not report MIL 'off'");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Transmission #1 (3) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testRun() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x01,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(Packet.create(PGN,
                                                                            0x17,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x61,
                                                                            0x02,
                                                                            0x13,
                                                                            0x00,
                                                                            0x21,
                                                                            0x06,
                                                                            0x1F,
                                                                            0x00,
                                                                            0xEE,
                                                                            0x10,
                                                                            0x04,
                                                                            0x00));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1, packet2));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailures() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 5);
        var packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                 LampStatus.ON,
                                                 LampStatus.OFF,
                                                 LampStatus.OFF,
                                                 LampStatus.OFF,
                                                 dtc);
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x17,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));
        DM1ActiveDTCsPacket packet3 = new DM1ActiveDTCsPacket(
                                                              Packet.create(PGN,
                                                                            0x03,
                                                                            0x00,
                                                                            0xFF,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0x00,
                                                                            0xFF,
                                                                            0xFF));


        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1, packet2, packet3));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Engine #2 (1) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.b - OBD ECU Engine #2 (1) did not report MIL 'off'");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }
}
