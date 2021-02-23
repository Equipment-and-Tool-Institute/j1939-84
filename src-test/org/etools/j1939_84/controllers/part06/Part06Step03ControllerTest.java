/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
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

@RunWith(MockitoJUnitRunner.class)
public class Part06Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 3;
    private static final int PGN = DM12MILOnEmissionDTCPacket.PGN;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

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

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part06Step03Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule);

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
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
    public void testHappyPathNoFailures() {

        DM12MILOnEmissionDTCPacket packet1 = DM12MILOnEmissionDTCPacket.create(0x01,
                                                                               LampStatus.ON,
                                                                               LampStatus.OFF,
                                                                               LampStatus.OFF,
                                                                               LampStatus.OFF,
                                                                               DiagnosticTroubleCode.create(1569,
                                                                                                            31,
                                                                                                            0,
                                                                                                            0));

        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported an active DTC and MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailures() {
        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(Packet.create(PGN,
                                                                                          0x01,
                                                                                          0x11,
                                                                                          0x22,
                                                                                          0x33,
                                                                                          0x44,
                                                                                          0x55,
                                                                                          0x66,
                                                                                          0x77,
                                                                                          0x88));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM12MILOnEmissionDTCPacket obdPacket3 = new DM12MILOnEmissionDTCPacket(Packet.create(PGN,
                                                                                             0x03,
                                                                                             0x11,
                                                                                             0x22,
                                                                                             0x13,
                                                                                             0x44,
                                                                                             0x55,
                                                                                             0x66,
                                                                                             0x77,
                                                                                             0x88));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x03)))
                                                                  .thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x03));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported an active DTC and MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported MIL on");
    }

    @Test
    public void testMoreFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM12MILOnEmissionDTCPacket packet3b = new DM12MILOnEmissionDTCPacket(
                                                                             Packet.create(PGN,
                                                                                           0x03,
                                                                                           0x00,
                                                                                           0x00,
                                                                                           0x00,
                                                                                           0x00,
                                                                                           0xFF,
                                                                                           0xFF,
                                                                                           0xFF,
                                                                                           0xFF));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, Optional.empty()));
        when(diagnosticMessageModule.requestDM12(any(), eq(0x03)))
                                                                  .thenReturn(new BusResult<>(false, packet3b));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM12(any(), eq(0x03));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported an active DTC and MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

}
