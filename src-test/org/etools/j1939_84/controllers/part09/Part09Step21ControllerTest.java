/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.Packet;
import net.soliddesign.j1939tools.bus.RequestResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part09Step21ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 21;
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

        instance = new Part09Step21Controller(executor,
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
    public void testActivePreviouslyActiveDTCsGreaterThanOne() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(0x44));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));
        DM5DiagnosticReadinessPacket packet0 = DM5DiagnosticReadinessPacket.create(0x00,
                                                                                   0x02,
                                                                                   0x00,
                                                                                   0x22);
        AcknowledgmentPacket packet44 = new AcknowledgmentPacket(
                                                                 Packet.create(PGN,
                                                                               0x44,
                                                                               0x01,
                                                                               0x02,
                                                                               0x03,
                                                                               0x04,
                                                                               0x05,
                                                                               0x06,
                                                                               0x07,
                                                                               0x08));
        DM5DiagnosticReadinessPacket packet21 = DM5DiagnosticReadinessPacket.create(0x21,
                                                                                    0,
                                                                                    1,
                                                                                    0x22);
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                List.of(packet0,
                                                                                                        packet21),
                                                                                                List.of(packet44));
        when(communicationsModule.requestDM5(any())).thenReturn(globalRequestResponse);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.21.2.a - OBD ECU Engine #1 (0) reported > 0 active DTCs count");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.21.2.a - OBD ECU Body Controller (33) reported > 0 previously active DTCs count");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testStepAllDM5PacketsEmpty() {
        DM5DiagnosticReadinessPacket packet = DM5DiagnosticReadinessPacket.create(3, 0xFF, 0xFF, 0xFF);

        DM5DiagnosticReadinessPacket packet0 = DM5DiagnosticReadinessPacket.create(0, 0xFF, 0xFF, 0xFF);

        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                List.of(packet,
                                                                                                        packet0),
                                                                                                List.of());
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

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testRun() {
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule);
        OBDModuleInformation obdModule_1 = new OBDModuleInformation(1);
        dataRepository.putObdModule(obdModule_1);

        var dm5 = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22);
        var dm5_1 = DM5DiagnosticReadinessPacket.create(1, 0, 0, 0x22);

        when(communicationsModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5, dm5_1));

        runTest();

        verify(communicationsModule).requestDM5(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
