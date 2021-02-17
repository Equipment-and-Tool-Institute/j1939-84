/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
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
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step18Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step18ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM12MILOnEmissionDTCPacket.PGN;
    private static final int STEP_NUMBER = 18;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step18Controller instance;

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
        listener = new TestResultsListener(mockListener);
        dataRepository = DataRepository.newInstance();

        DateTimeModule.setInstance(null);

        instance = new Part01Step18Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticMessageModule,
                dataRepository,
                DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
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
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.18.2.c - No OBD ECU provided DM12");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.4.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailures() {
        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x04, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM12MILOnEmissionDTCPacket obdPacket3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1, packet3), List.of()));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x03));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.a - Engine #2 (1) reported active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.a - Transmission #1 (3) reported active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.b - Engine #2 (1) did not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.b - Transmission #1 (3) did not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.4.a - Difference compared to data received during global request from Transmission #1 (3)");
    }

    @Test
    public void testMoreFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, (byte) 0x0A, 0x44, 0x55, 0x66, 0x77,
                              0x88));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM12MILOnEmissionDTCPacket packet3b = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1, packet3), List.of(ackPacket)));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet3b));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x03));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.a - Engine #2 (1) reported active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.a - Transmission #1 (3) reported active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.b - Engine #2 (1) did not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.b - Transmission #1 (3) did not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.4.a - Difference compared to data received during global request from Transmission #1 (3)");

    }

    @Test
    public void testNoErrors() {

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));

        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(diagnosticMessageModule.requestDM12(any())).thenReturn(new RequestResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

}
