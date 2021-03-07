/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part11Step06ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 6;

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

        instance = new Part11Step06Controller(executor,
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
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM29DtcCounts.create(0, 5, 4, 3, 2, 1), 11);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var dm28_0 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm28_1 = DM28PermanentEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_0, dm28_1));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm28_1 = DM28PermanentEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_1));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.11.6.2.a - No ECU report a permanent DTC");
    }

    @Test
    public void testFailureForDifferentNumberOfDTCs() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM29DtcCounts.create(0, 5, 4, 3, 2, 2), 11);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var dm28_0 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);

        when(diagnosticMessageModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_0));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.6.2.b - Engine #1 (0) reported a different number of permanent DTCs that indicate in DM29 response earlier in test 6.11.4");
    }

}
