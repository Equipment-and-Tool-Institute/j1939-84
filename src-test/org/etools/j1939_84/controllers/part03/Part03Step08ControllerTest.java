/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.RequestResult;
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
public class Part03Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 8;

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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step08Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
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
    public void testNoPackets() {
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getResults());
    }

    @Test
    public void testNAisFiltered() {
        var dm5 = DM5DiagnosticReadinessPacket.create(0, 0xFF, 0xFF, 0x23);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForActiveCount() {
        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 0xFF, 0x23);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("FAIL: 6.3.8.2.a - OBD ECU Engine #1 (0) reported active DTC count not = 0" + NL,
                     listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.8.2.a - OBD ECU Engine #1 (0) reported active DTC count not = 0");
    }

    @Test
    public void testFailureForPreviouslyActiveCount() {
        var dm5 = DM5DiagnosticReadinessPacket.create(0, 0, 1, 0x23);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("FAIL: 6.3.8.2.a - OBD ECU Engine #1 (0) reported previously active DTC count not = 0" + NL,
                     listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.8.2.a - OBD ECU Engine #1 (0) reported previously active DTC count not = 0");
    }

    @Test
    public void testNoFailure() {
        var dm5 = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x23);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getResults());
    }

    @Test
    public void testOBDModulesAreFiltered() {
        var dm5 = DM5DiagnosticReadinessPacket.create(0, 1, 1, 0);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false, dm5));

        runTest();

        verify(diagnosticMessageModule).requestDM5(any());

        assertEquals("", listener.getResults());
    }
}
