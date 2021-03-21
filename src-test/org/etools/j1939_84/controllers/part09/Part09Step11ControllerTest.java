/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
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
public class Part09Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
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

        instance = new Part09Step11Controller(executor,
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
        var ratio1 = new PerformanceRatio(123, 1, 10, 0);
        var ratio2 = new PerformanceRatio(456, 8, 100, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio1, ratio2), 9);
        dataRepository.putObdModule(obdModuleInformation);

        var ratio11 = new PerformanceRatio(123, 1, 10, 0);
        var ratio21 = new PerformanceRatio(456, 8, 100, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio11, ratio21);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDifferentIgnitionCycles() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(123, 1, 10, 0);
        var ratio2 = new PerformanceRatio(456, 8, 100, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio1, ratio2), 9);
        dataRepository.putObdModule(obdModuleInformation);

        var ratio11 = new PerformanceRatio(123, 1, 10, 0);
        var ratio21 = new PerformanceRatio(456, 8, 100, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 8, 2, ratio11, ratio21);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.11.2.a - Engine #1 (0) reported value for ignition cycles is not equal to the value from Step 6.9.4.1.b");
    }

    @Test
    public void testFailureForDifferentRatios() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(123, 1, 10, 0);
        var ratio2 = new PerformanceRatio(456, 8, 100, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio1, ratio2), 9);
        dataRepository.putObdModule(obdModuleInformation);

        var ratio11 = new PerformanceRatio(123, 1, 10, 0);
        var ratio21 = new PerformanceRatio(789, 8, 100, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio11, ratio21);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.11.2.a - Engine #1 (0) reported values for performance ratios not equal to the values from Step 6.9.4.1.b");
    }

    @Test
    public void testFailureForNACKNow() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(123, 1, 10, 0);
        var ratio2 = new PerformanceRatio(456, 8, 100, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio1, ratio2), 9);
        dataRepository.putObdModule(obdModuleInformation);

        var nack = AcknowledgmentPacket.create(0, AcknowledgmentPacket.Response.NACK);
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(nack));

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.11.2.b - Engine #1 (0) now NACK'd DM20 request after previously providing data in 6.9.4.1");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(123, 1, 10, 0);
        var ratio2 = new PerformanceRatio(456, 8, 100, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 4, 2, ratio1, ratio2), 9);
        dataRepository.putObdModule(obdModuleInformation);

        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(BusResult.empty());

        runTest();

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.11.2.c - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }

}
