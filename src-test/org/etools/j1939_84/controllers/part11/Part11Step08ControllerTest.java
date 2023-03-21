/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 8;

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

        instance = new Part11Step08Controller(executor,
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
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        // Module 0 Responds as expected
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio123_1 = new PerformanceRatio(123, 1, 1, 0);
        var ratio5322_1 = new PerformanceRatio(5322, 0xFFFF, 0xFFFF, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          1,
                                                                          1,
                                                                          ratio3058_1,
                                                                          ratio123_1,
                                                                          ratio5322_1),
                                 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        var ratio123_9 = new PerformanceRatio(123, 1, 2, 0);
        var ratio5322_9 = new PerformanceRatio(5322, 0xFFFF, 0xFFFF, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          3,
                                                                          3,
                                                                          ratio3058_9,
                                                                          ratio123_9,
                                                                          ratio5322_9),
                                 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        var ratio123_11 = new PerformanceRatio(123, 1, 2, 0);
        var ratio5322_11 = new PerformanceRatio(5322, 0xFFFF, 0xFFFF, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          3,
                                                                          3,
                                                                          ratio3058_11,
                                                                          ratio123_11,
                                                                          ratio5322_11),
                                 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 3, 0);
        var ratio123 = new PerformanceRatio(123, 2, 2, 0);
        var ratio5322 = new PerformanceRatio(5322, 0xFFFF, 0xFFFF, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058, ratio123, ratio5322);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        // Module 1 won't be queried
        dataRepository.putObdModule(new OBDModuleInformation(1));

        // Module 2 will NACK
        OBDModuleInformation obdModuleInformation2 = new OBDModuleInformation(2);
        obdModuleInformation2.set(DM20MonitorPerformanceRatioPacket.create(2, 3, 3), 11);
        dataRepository.putObdModule(obdModuleInformation2);
        var nack = AcknowledgmentPacket.create(2, NACK);
        when(communicationsModule.requestDM20(any(), eq(2))).thenReturn(BusResult.of(nack));

        // Module 3 will respond the same as Module 0
        OBDModuleInformation obdModuleInformation3 = new OBDModuleInformation(3);
        var ratio3058_1_3 = new PerformanceRatio(3058, 1, 1, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 1, 1, ratio3058_1_3), 1);

        var ratio3058_9_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 3, ratio3058_9_3), 9);

        var ratio3058_11_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 3, ratio3058_11_3), 11);

        dataRepository.putObdModule(obdModuleInformation3);

        var ratio3058_3 = new PerformanceRatio(3058, 2, 3, 3);
        var dm20_3 = DM20MonitorPerformanceRatioPacket.create(3, 4, 4, ratio3058_3);
        when(communicationsModule.requestDM20(any(), eq(3))).thenReturn(BusResult.of(dm20_3));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));
        verify(communicationsModule).requestDM20(any(), eq(2));
        verify(communicationsModule).requestDM20(any(), eq(3));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForRetry() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation2 = new OBDModuleInformation(2);
        obdModuleInformation2.set(DM20MonitorPerformanceRatioPacket.create(2, 3, 3), 11);
        dataRepository.putObdModule(obdModuleInformation2);
        var nack = AcknowledgmentPacket.create(2, NACK);

        when(communicationsModule.requestDM20(any(), eq(2))).thenReturn(BusResult.empty())
                                                            .thenReturn(new BusResult<>(true, nack))
                                                            .thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(communicationsModule, times(2)).requestDM20(any(), eq(2));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.8.2.a - Retry was required to obtain DM20 response from Turbocharger (2)");
    }

    @Test
    public void testFailureForNoGeneralDenominatorIncrease() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 2, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3058 Engine EGR System Monitor has not incremented by one");
    }

    @Test
    public void testWarningForMonitorDenominatorNotIncreasingForCIEngines() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio5322_1 = new PerformanceRatio(5322, 1, 1, 0);
        var ratio5318_1 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio3064_1 = new PerformanceRatio(3064, 1, 1, 0);
        var ratio5321_1 = new PerformanceRatio(5321, 1, 1, 0);
        var ratio3055_1 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio4792_1 = new PerformanceRatio(4792, 1, 1, 0);
        var ratio5308_1 = new PerformanceRatio(5308, 1, 1, 0);
        var ratio4364_1 = new PerformanceRatio(4364, 1, 1, 0);

        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          1,
                                                                          1,
                                                                          ratio5322_1,
                                                                          ratio5318_1,
                                                                          ratio3058_1,
                                                                          ratio3064_1,
                                                                          ratio5321_1,
                                                                          ratio3055_1,
                                                                          ratio4792_1,
                                                                          ratio5308_1,
                                                                          ratio4364_1),
                                 1);

        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3), 9);

        var ratio5322_11 = new PerformanceRatio(5322, 1, 1, 0);
        var ratio5318_11 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio3058_11 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio3064_11 = new PerformanceRatio(3064, 1, 1, 0);
        var ratio5321_11 = new PerformanceRatio(5321, 1, 1, 0);
        var ratio3055_11 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio4792_11 = new PerformanceRatio(4792, 1, 1, 0);
        var ratio5308_11 = new PerformanceRatio(5308, 1, 1, 0);
        var ratio4364_11 = new PerformanceRatio(4364, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          3,
                                                                          3,
                                                                          ratio5322_11,
                                                                          ratio5318_11,
                                                                          ratio3058_11,
                                                                          ratio3064_11,
                                                                          ratio5321_11,
                                                                          ratio3055_11,
                                                                          ratio4792_11,
                                                                          ratio5308_11,
                                                                          ratio4364_11),
                                 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio5322 = new PerformanceRatio(5322, 1, 1, 0);
        var ratio5318 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio3058 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio3064 = new PerformanceRatio(3064, 1, 1, 0);
        var ratio5321 = new PerformanceRatio(5321, 1, 1, 0);
        var ratio3055 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio4792 = new PerformanceRatio(4792, 1, 1, 0);
        var ratio5308 = new PerformanceRatio(5308, 1, 1, 0);
        var ratio4364 = new PerformanceRatio(4364, 1, 1, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0,
                                                            4,
                                                            4,
                                                            ratio5322,
                                                            ratio5318,
                                                            ratio3058,
                                                            ratio3064,
                                                            ratio5321,
                                                            ratio3055,
                                                            ratio4792,
                                                            ratio5308,
                                                            ratio4364);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 5318 AFT Exhaust Gas Sensor System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3058 Engine EGR System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 5321 Engine Intake Manifold Pressure System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3055 Engine Fuel System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 4792 AFT 1 SCR System has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 5308 AFT 1 NOx Adsorber Catalyst System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 4364 AFT 1 SCR Conversion Efficiency has not incremented by one");
    }

    @Test
    public void testWarningForMonitorDenominatorNotIncreasingForSIEngines() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3050_1 = new PerformanceRatio(3050, 1, 1, 0);
        var ratio3055_1 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio3056_1 = new PerformanceRatio(3056, 1, 1, 0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio5318_1 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio5321_1 = new PerformanceRatio(5321, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          1,
                                                                          1,
                                                                          ratio3050_1,
                                                                          ratio3055_1,
                                                                          ratio3056_1,
                                                                          ratio3058_1,
                                                                          ratio5318_1,
                                                                          ratio5321_1),
                                 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3050_11 = new PerformanceRatio(3050, 1, 1, 0);
        var ratio3055_11 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio3056_11 = new PerformanceRatio(3056, 1, 1, 0);
        var ratio3058_11 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio5318_11 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio5321_11 = new PerformanceRatio(5321, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0,
                                                                          3,
                                                                          3,
                                                                          ratio3050_11,
                                                                          ratio3055_11,
                                                                          ratio3056_11,
                                                                          ratio3058_11,
                                                                          ratio5318_11,
                                                                          ratio5321_11),
                                 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3050 = new PerformanceRatio(3050, 1, 1, 0);
        var ratio3055 = new PerformanceRatio(3055, 1, 1, 0);
        var ratio3056 = new PerformanceRatio(3056, 1, 1, 0);
        var ratio3058 = new PerformanceRatio(3058, 1, 1, 0);
        var ratio5318 = new PerformanceRatio(5318, 1, 1, 0);
        var ratio5321 = new PerformanceRatio(5321, 1, 1, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0,
                                                            4,
                                                            4,
                                                            ratio3050,
                                                            ratio3055,
                                                            ratio3056,
                                                            ratio3058,
                                                            ratio5318,
                                                            ratio5321);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3050 Catalyst Bank 1 System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3055 Engine Fuel System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3056 Engine Exhaust Bank 1 O2 Sensor Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 3058 Engine EGR System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 5318 AFT Exhaust Gas Sensor System Monitor has not incremented by one");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.a - Engine #1 (0) response indicates denominator for monitor SPN 5321 Engine Intake Manifold Pressure System Monitor has not incremented by one");
    }

    @Test
    public void testWarningForMonitorDenominatorGreaterThanGeneralDenominator() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 4, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 5, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.b.i - Engine #1 (0) response shows denominator for monitor SPN 3058 Engine EGR System Monitor is greater than the general denominator");
    }

    @Test
    public void testWarningForGeneralDenominatorGreaterThanIgnitionCycles() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 4, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 4, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 5, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.b.ii - Engine #1 (0) response shows general denominator greater than the ignition cycle counter");
    }

    @Test
    public void testWarningForNumeratorGreaterThanIgnitionCycles() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 5, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.b.iii - Engine #1 (0) response shows numerator for monitor SPN 3058 Engine EGR System Monitor is greater than the ignition cycle counter");
    }

    @Test
    public void testWarningForRatioNumeratorLessThanPart1() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 0, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.c.i - Engine #1 (0) numerator for monitor SPN 3058 Engine EGR System Monitor is less than the corresponding value in part1");
    }

    @Test
    public void testWarningForRatioDenominatorLessThanPart1() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 5, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 1, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.c.i - Engine #1 (0) denominator for monitor SPN 3058 Engine EGR System Monitor is less than the corresponding value in part1");
    }

    @Test
    public void testWarningForIgnitionCycleLessThanPart1() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 7, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 1, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 5, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.c.i - Engine #1 (0) ignition cycle counter is less than the corresponding value in part1");
    }

    @Test
    public void testWarningForDifferentGeneralDenominators() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 5, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        OBDModuleInformation obdModuleInformation3 = new OBDModuleInformation(3);
        var ratio3058_1_3 = new PerformanceRatio(3058, 1, 1, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 1, 4, ratio3058_1_3), 1);

        var ratio3058_9_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 4, ratio3058_9_3), 9);

        var ratio3058_11_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 3, ratio3058_11_3), 11);

        dataRepository.putObdModule(obdModuleInformation3);

        var ratio3058_3 = new PerformanceRatio(3058, 2, 3, 3);
        var dm20_3 = DM20MonitorPerformanceRatioPacket.create(3, 5, 5, ratio3058_3);
        when(communicationsModule.requestDM20(any(), eq(3))).thenReturn(BusResult.of(dm20_3));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));
        verify(communicationsModule).requestDM20(any(), eq(3));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.d - More than one ECU reported DM20 data and general denominators do not match from all ECUs");
    }

    @Test
    public void testWarningForDifferentIgnitionCycles() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var ratio3058_1 = new PerformanceRatio(3058, 1, 1, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio3058_1), 1);

        var ratio3058_9 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_9), 9);

        var ratio3058_11 = new PerformanceRatio(3058, 1, 2, 0);
        obdModuleInformation.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 3, ratio3058_11), 11);

        dataRepository.putObdModule(obdModuleInformation);

        var ratio3058 = new PerformanceRatio(3058, 2, 3, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 4, 4, ratio3058);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        OBDModuleInformation obdModuleInformation3 = new OBDModuleInformation(3);
        var ratio3058_1_3 = new PerformanceRatio(3058, 1, 1, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 1, 1, ratio3058_1_3), 1);

        var ratio3058_9_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 3, ratio3058_9_3), 9);

        var ratio3058_11_3 = new PerformanceRatio(3058, 1, 2, 3);
        obdModuleInformation3.set(DM20MonitorPerformanceRatioPacket.create(3, 3, 3, ratio3058_11_3), 11);

        dataRepository.putObdModule(obdModuleInformation3);

        var ratio3058_3 = new PerformanceRatio(3058, 2, 3, 3);
        var dm20_3 = DM20MonitorPerformanceRatioPacket.create(3, 5, 4, ratio3058_3);
        when(communicationsModule.requestDM20(any(), eq(3))).thenReturn(BusResult.of(dm20_3));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0));
        verify(communicationsModule).requestDM20(any(), eq(3));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.11.8.3.d - More than one ECU reported DM20 data and ignition cycle counts do not match from all ECUs");
    }
}
