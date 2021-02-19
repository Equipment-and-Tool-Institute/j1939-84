/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
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
public class Part04Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 11;

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

        instance = new Part04Step11Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        PerformanceRatio ratio = new PerformanceRatio(123, 10, 25, 0);
        obdModuleInformation.setPerformanceRatios(List.of(ratio));
        obdModuleInformation.setIgnitionCycleCounterValue(100);
        dataRepository.putObdModule(obdModuleInformation);

        dataRepository.putObdModule(new OBDModuleInformation(1)); // no query expected

        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 101, 10, ratio);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(new BusResult<>(false, dm20));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForTooFewIgnitionCycles() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        PerformanceRatio ratio = new PerformanceRatio(123, 10, 25, 0);
        obdModuleInformation.setPerformanceRatios(List.of(ratio));
        obdModuleInformation.setIgnitionCycleCounterValue(100);
        dataRepository.putObdModule(obdModuleInformation);

        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 100, 10, ratio);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(new BusResult<>(false, dm20));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.11.2.a - Ignition cycle counter (SPN 3048) from Engine #1 (0) has not " +
                                                "incremented by one compared to the value recorded at the end of part 3");
    }

    @Test
    public void testFailureForTooManyIgnitionCycles() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        PerformanceRatio ratio = new PerformanceRatio(123, 10, 25, 0);
        obdModuleInformation.setPerformanceRatios(List.of(ratio));
        obdModuleInformation.setIgnitionCycleCounterValue(100);
        dataRepository.putObdModule(obdModuleInformation);

        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 103, 10, ratio);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(new BusResult<>(false, dm20));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.11.2.a - Ignition cycle counter (SPN 3048) from Engine #1 (0) has not " +
                                                "incremented by one compared to the value recorded at the end of part 3");
    }
}
