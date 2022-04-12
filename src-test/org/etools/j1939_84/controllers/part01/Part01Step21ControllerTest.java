/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step21Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step21ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM27AllPendingDTCsPacket.PGN;
    private static final int STEP_NUMBER = 21;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step21Controller instance;

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
    public void setUp() {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step21Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
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
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(communicationsModule.requestDM27(any()))
                                                        .thenReturn(new RequestResult<>(false, List.of(), List.of()));
        when(communicationsModule.requestDM27(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailures() {
        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(PGN,
                                                                                      0x01,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      0x33,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));
        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(PGN,
                                                                                      0x03,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x04,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0xFF,
                                                                                      0xFF,
                                                                                      0xFF));
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM27AllPendingDTCsPacket obdPacket3 = new DM27AllPendingDTCsPacket(
                                                                           Packet.create(PGN,
                                                                                         0x03,
                                                                                         0x11,
                                                                                         0x22,
                                                                                         0x13,
                                                                                         0x44,
                                                                                         0x55,
                                                                                         0x66,
                                                                                         0x77,
                                                                                         0x88));

        when(communicationsModule.requestDM27(any()))
                                                        .thenReturn(new RequestResult<>(false,
                                                                                        List.of(packet1, packet3),
                                                                                        List.of()));

        when(communicationsModule.requestDM27(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x03)))
                                                                  .thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Engine #2 (1) reported an all pending DTC");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Transmission #1 (3) reported an all pending DTC");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.b - Engine #2 (1) did not report MIL off");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.b - Transmission #1 (3) did not report MIL off");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.a - Difference compared to data received during global request from Transmission #1 (3)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.a - Difference compared to data received during global request from Transmission #1 (3)");

    }

    @Test
    public void testMoreEmptyPacketNoFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(
                                                                  Packet.create(PGN,
                                                                                0x01,
                                                                                0x11,
                                                                                0x22,
                                                                                0x33,
                                                                                0x44,
                                                                                0x55,
                                                                                0x66,
                                                                                0x77,
                                                                                0x88));

        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(
                                                                        Packet.create(PGN,
                                                                                      0x03,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      (byte) 0x0A,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false,
                                                                                     List.of(),
                                                                                     List.of(ackPacket)));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, ackPacket));
        when(communicationsModule.requestDM27(any(), eq(0x03))).thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.b - OBD ECU Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");
        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testMoreFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(Packet.create(PGN,
                                                                                0x01,
                                                                                0x11,
                                                                                0x22,
                                                                                0x33,
                                                                                0x44,
                                                                                0x55,
                                                                                0x66,
                                                                                0x77,
                                                                                0x88));

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x01,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      0x33,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));
        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x03,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      (byte) 0x0A,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM27AllPendingDTCsPacket packet3b = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                       0x03,
                                                                                       0x00,
                                                                                       0x00,
                                                                                       0x00,
                                                                                       0x00,
                                                                                       0xFF,
                                                                                       0xFF,
                                                                                       0xFF,
                                                                                       0xFF));

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false,
                                                                                     List.of(packet3),
                                                                                     List.of(ackPacket)));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x03))).thenReturn(new BusResult<>(false, packet3b));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Transmission #1 (3) reported an all pending DTC");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.b - Transmission #1 (3) did not report MIL off");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.a - Difference compared to data received during global request from Transmission #1 (3)");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.a - Difference compared to data received during global request from Transmission #1 (3)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testMoreNackFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(Packet.create(PGN,
                                                                                0x03,
                                                                                0x11,
                                                                                0x22,
                                                                                0x33,
                                                                                0x44,
                                                                                0x55,
                                                                                0x66,
                                                                                0x77,
                                                                                0x88));

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x01,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      0x33,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));
        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x03,
                                                                                      0x11,
                                                                                      0x22,
                                                                                      (byte) 0x0A,
                                                                                      0x44,
                                                                                      0x55,
                                                                                      0x66,
                                                                                      0x77,
                                                                                      0x88));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(communicationsModule.requestDM27(any()))
                                                        .thenReturn(new RequestResult<>(false,
                                                                                        List.of(packet1, packet3),
                                                                                        List.of(ackPacket)));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x03))).thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Engine #2 (1) reported an all pending DTC");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Transmission #1 (3) reported an all pending DTC");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.b - Engine #2 (1) did not report MIL off");
        verify(mockListener).addOutcome(
                                        PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.b - Transmission #1 (3) did not report MIL off");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoErrors() {

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x01,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x00));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(communicationsModule.requestDM27(any())).thenReturn(RequestResult.of(packet1));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testPacketWithDTCsErrors() {

        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x01,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x61,
                                                                                      0x02,
                                                                                      0x13,
                                                                                      0x81));
        DM27AllPendingDTCsPacket packet2 = new DM27AllPendingDTCsPacket(Packet.create(PGN,
                                                                                      0x02,
                                                                                      0x00,
                                                                                      0xFF,
                                                                                      0x00,
                                                                                      0x00,
                                                                                      0x61,
                                                                                      0x02,
                                                                                      0x13,
                                                                                      0x81));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(communicationsModule.requestDM27(any())).thenReturn(RequestResult.of(packet1, packet2));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.21.2.a - Engine #2 (1) reported an all pending DTC");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
