/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket.PGN;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM27AllPendingDTCsPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part03Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 3;

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

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private StepController instance;

    private DataRepository dataRepository;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step03Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule);

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
                                 mockListener);
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
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false));

        runTest();

        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.3.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
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

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false, ackPacket));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, ackPacket));
        when(communicationsModule.requestDM27(any(), eq(0x03))).thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.3.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.3.5.b - OBD ECU Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testMoreNackFailures() {
        AcknowledgmentPacket ackPacket3 = AcknowledgmentPacket.create(0x03, NACK);
        DiagnosticTroubleCode dtc1 = DiagnosticTroubleCode.create(257, 1, 1, 1);
        DM27AllPendingDTCsPacket packet1 = DM27AllPendingDTCsPacket.create(0x01,
                                                                           OFF,
                                                                           SLOW_FLASH,
                                                                           OFF,
                                                                           FAST_FLASH,
                                                                           dtc1);

        DM27AllPendingDTCsPacket packet3 = DM27AllPendingDTCsPacket.create(0x01,
                                                                           OFF,
                                                                           SLOW_FLASH,
                                                                           OFF,
                                                                           FAST_FLASH,
                                                                           dtc1);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModule1);

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        DiagnosticTroubleCode dtc3 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        obdModule3.set(DM6PendingEmissionDTCPacket.create(3, OFF, OFF, OFF, OFF, dtc3), 3);
        obdModule3.set(packet3, 3);
        dataRepository.putObdModule(obdModule3);

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x03))).thenReturn(new BusResult<>(false, ackPacket3));

        runTest();

        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x03));

        assertEquals(List.of(dtc1),
                     dataRepository.getObdModule(0x01).getLatest(DM6PendingEmissionDTCPacket.class).getDtcs());
        assertEquals(packet1, dataRepository.getObdModule(0x01).getLatest(DM27AllPendingDTCsPacket.class));
        assertEquals(List.of(dtc3),
                     dataRepository.getObdModule(0x03).getLatest(DM6PendingEmissionDTCPacket.class).getDtcs());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testRun() {

        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1569, 31, 0, 35);
        DM27AllPendingDTCsPacket packet1 = DM27AllPendingDTCsPacket.create(0x01,
                                                                           OFF,
                                                                           OFF,
                                                                           OFF,
                                                                           OFF,
                                                                           dtc);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc), 3);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));

        assertEquals(dataRepository.getObdModule(packet1.getSourceAddress()).getLatest(DM27AllPendingDTCsPacket.class),
                     packet1);

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testPacketsWithDifferentDTCsErrors() {

        DiagnosticTroubleCode dtc1 = DiagnosticTroubleCode.create(257, 1, 1, 1);
        DiagnosticTroubleCode dtc11 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        DM27AllPendingDTCsPacket packet1 = DM27AllPendingDTCsPacket.create(0x01,
                                                                           OFF,
                                                                           SLOW_FLASH,
                                                                           OFF,
                                                                           FAST_FLASH,
                                                                           dtc1,
                                                                           dtc11);
        DiagnosticTroubleCode dtc2 = DiagnosticTroubleCode.create(609, 19, 1, 1);
        DiagnosticTroubleCode dtc22 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        DM27AllPendingDTCsPacket packet2 = DM27AllPendingDTCsPacket.create(0x02,
                                                                           OFF,
                                                                           SLOW_FLASH,
                                                                           OFF,
                                                                           FAST_FLASH,
                                                                           dtc2);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.set(DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModule1);
        OBDModuleInformation obdModule2 = new OBDModuleInformation(0x02);
        obdModule2.set(DM6PendingEmissionDTCPacket.create(2, OFF, OFF, OFF, OFF, dtc22), 3);
        dataRepository.putObdModule(obdModule2);

        when(communicationsModule.requestDM27(any())).thenReturn(new RequestResult<>(false, packet1, packet2));
        when(communicationsModule.requestDM27(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(communicationsModule.requestDM27(any(), eq(0x02))).thenReturn(new BusResult<>(false, packet2));

        runTest();

        verify(communicationsModule).requestDM27(any());
        verify(communicationsModule).requestDM27(any(), eq(0x01));
        verify(communicationsModule).requestDM27(any(), eq(0x02));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        // on longer checking for equality #1227
        // verify(mockListener).addOutcome(PART_NUMBER,
        // STEP_NUMBER,
        // FAIL,
        // "6.3.3.2.a - OBD ECU Engine #2 (1) reported different DTC than observed in Step 6.3.2.1");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.3.3.a - OBD ECU Engine #2 (1) reported 1 DTCs in response to DM6 in 6.3.2.1 and 2 DTCs when responding to DM27");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.3.3.a - OBD ECU Turbocharger (2) reported 1 DTCs in response to DM6 in 6.3.2.1 and 1 DTCs when responding to DM27");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.3.2.a - OBD ECU Turbocharger (2) reported different DTC than observed in Step 6.3.2.1");

        // verify we did NOT update the obd's dtc values set in the data repo
        assertEquals(List.of(dtc1),
                     dataRepository.getObdModule(0x01).getLatest(DM6PendingEmissionDTCPacket.class).getDtcs());
        assertEquals(dataRepository.getObdModule(0x01).getLatest(DM27AllPendingDTCsPacket.class), packet1);
        assertEquals(List.of(dtc22),
                     dataRepository.getObdModule(0x02).getLatest(DM6PendingEmissionDTCPacket.class).getDtcs());
        assertEquals(dataRepository.getObdModule(0x02).getLatest(DM27AllPendingDTCsPacket.class), packet2);
    }
}
