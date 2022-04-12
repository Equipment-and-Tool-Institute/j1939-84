/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

;

@RunWith(MockitoJUnitRunner.class)
public class Part09Step16ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 16;

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

        instance = new Part09Step16Controller(executor,
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
    public void testHappyPathNoFailures() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0), 8);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm26_0 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0);
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26_0));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM26TripDiagnosticReadinessPacket.create(1, 0, 1), 8);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm26_1 = DM26TripDiagnosticReadinessPacket.create(1, 0, 0);
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of(dm26_1));

        dataRepository.putObdModule(new OBDModuleInformation(2));
        var dm26_2 = DM26TripDiagnosticReadinessPacket.create(2, 100, 100);
        when(communicationsModule.requestDM26(any(), eq(2))).thenReturn(RequestResult.of(dm26_2));

        dataRepository.putObdModule(new OBDModuleInformation(3));
        var nack = AcknowledgmentPacket.create(3, NACK);
        when(communicationsModule.requestDM26(any(), eq(3))).thenReturn(new RequestResult<>(false, nack));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));
        verify(communicationsModule).requestDM26(any(), eq(1));
        verify(communicationsModule).requestDM26(any(), eq(2));
        verify(communicationsModule).requestDM26(any(), eq(3));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForWarmUpsSinceCodeClearNonZero() {
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM26TripDiagnosticReadinessPacket.create(1, 0, 1), 8);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm26_1 = DM26TripDiagnosticReadinessPacket.create(1, 0, 1);
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of(dm26_1));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.16.2.a - Engine #2 (1) reported a non-zero value of number of WU-SCC in test 6.8.16.1.a and is still reporting > 0");
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of());

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.16.2.b - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
