/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
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
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part06Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 2;
    private static final int PGN = DM5DiagnosticReadinessPacket.PGN;

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

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part06Step02Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
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
                                 bannerModule,
                                 engineSpeedModule,
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
    public void testModulesEmpty() {
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                Collections.emptyList(),
                                                                                                Collections.emptyList());
        when(communicationsModule.requestDM5(any())).thenReturn(globalRequestResponse);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.2.2.a - No OBD ECU reported a count of > 0 active DTCs");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testStepDM5PacketsEmpty() {
        final int ackPgn = DM5DiagnosticReadinessPacket.PGN;
        AcknowledgmentPacket packet44 = new AcknowledgmentPacket(
                                                                 Packet.create(ackPgn,
                                                                               0x44,
                                                                               0x01,
                                                                               0x02,
                                                                               0x03,
                                                                               0x04,
                                                                               0x05,
                                                                               0x06,
                                                                               0x07,
                                                                               0x08));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                Collections.emptyList(),
                                                                                                Collections.singletonList(
                                                                                                                          packet44));
        when(communicationsModule.requestDM5(any())).thenReturn(globalRequestResponse);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.2.2.a - No OBD ECU reported a count of > 0 active DTCs");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testStepAllDM5PacketsEmpty() {
        DM5DiagnosticReadinessPacket packet = DM5DiagnosticReadinessPacket.create(3, 0xFF, 0xFF, 0xFF);

        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(PGN,
                                                                                              0x00,
                                                                                              0x00,
                                                                                              0x00,
                                                                                              0x14,
                                                                                              0x37,
                                                                                              0xE0,
                                                                                              0x1E,
                                                                                              0xE0,
                                                                                              0x1E));
        final int ackPgn = DM5DiagnosticReadinessPacket.PGN;
        AcknowledgmentPacket packet44 = new AcknowledgmentPacket(
                                                                 Packet.create(ackPgn,
                                                                               0x44,
                                                                               0x01,
                                                                               0x02,
                                                                               0x03,
                                                                               0x04,
                                                                               0x05,
                                                                               0x06,
                                                                               0x07,
                                                                               0x08));
        DM5DiagnosticReadinessPacket packet21 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(PGN,
                                                                                               0x21,
                                                                                               0x10,
                                                                                               0x20,
                                                                                               0x30,
                                                                                               0x40,
                                                                                               0x50,
                                                                                               0x60,
                                                                                               0x70,
                                                                                               0x80));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                List.of(packet,
                                                                                                        packet0,
                                                                                                        packet21),
                                                                                                List.of(packet44));
        when(communicationsModule.requestDM5(any())).thenReturn(globalRequestResponse);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x03);
        dataRepository.putObdModule(obdModule);
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        dataRepository.putObdModule(obdModule0);
        OBDModuleInformation obdModule17 = new OBDModuleInformation(0x17);
        dataRepository.putObdModule(obdModule17);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.2.2.a - No OBD ECU reported a count of > 0 active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.2.3.a - OBD ECU Body Controller (33) reported a count of > 1 for previously active DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.2.3.a - OBD ECU Body Controller (33) reported a count of > 1 active DTCs");
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testHappyPathNoFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22);
        var dm5_1 = DM5DiagnosticReadinessPacket.create(1, 0, 0, 0x22);

        when(communicationsModule.requestDM5(any())).thenReturn(RequestResult.of(dm5, dm5_1));

        runTest();

        verify(communicationsModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
