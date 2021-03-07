/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
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
public class Part09Step07ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 7;

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

        instance = new Part09Step07Controller(executor,
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
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2020);
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM33EmissionIncreasingAECDActiveTime.create(0, EngineHoursTimer.create(0, 1, 1)), 2);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm33_0 = DM33EmissionIncreasingAECDActiveTime.create(0, EngineHoursTimer.create(0, 1, 1));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(RequestResult.of(dm33_0));

        runTest();

        verify(diagnosticMessageModule).requestDM33(any());

        assertSame(dm33_0, dataRepository.getObdModule(0).getLatest(DM33EmissionIncreasingAECDActiveTime.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoResponses() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2020);
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(diagnosticMessageModule).requestDM33(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDifferentTimers() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2020);
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        EngineHoursTimer timer0 = EngineHoursTimer.create(0, 0, 0);
        EngineHoursTimer timer1 = EngineHoursTimer.create(1, 1, 1);
        obdModuleInformation0.set(DM33EmissionIncreasingAECDActiveTime.create(0, timer0, timer1), 2);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm33_0 = DM33EmissionIncreasingAECDActiveTime.create(0, EngineHoursTimer.create(0, 1, 1));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm33_1 = DM33EmissionIncreasingAECDActiveTime.create(1, EngineHoursTimer.create(0, 1, 1));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(RequestResult.of(dm33_0, dm33_1));

        runTest();

        verify(diagnosticMessageModule).requestDM33(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.7.2.a - Engine #1 (0) reported a different number of EI-AECD timers than was reported in part 2");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.7.2.a - Engine #2 (1) reported a different number of EI-AECD timers than was reported in part 2");
    }

    @Test
    public void testFailureForDifferentTimersForSI2024() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2024);
        vehicleInformation.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        EngineHoursTimer timer0 = EngineHoursTimer.create(0, 0, 0);
        EngineHoursTimer timer1 = EngineHoursTimer.create(1, 1, 1);
        obdModuleInformation0.set(DM33EmissionIncreasingAECDActiveTime.create(0, timer0, timer1), 2);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm33_0 = DM33EmissionIncreasingAECDActiveTime.create(0, EngineHoursTimer.create(0, 1, 1));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm33_1 = DM33EmissionIncreasingAECDActiveTime.create(1, EngineHoursTimer.create(0, 1, 1));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(RequestResult.of(dm33_0, dm33_1));

        runTest();

        verify(diagnosticMessageModule).requestDM33(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.7.2.a - Engine #1 (0) reported a different number of EI-AECD timers than was reported in part 2");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.7.2.a - Engine #2 (1) reported a different number of EI-AECD timers than was reported in part 2");
    }

    @Test
    public void testFailureForDifferentTimersForSIPre2024() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2023);
        vehicleInformation.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        EngineHoursTimer timer0 = EngineHoursTimer.create(0, 0, 0);
        EngineHoursTimer timer1 = EngineHoursTimer.create(1, 1, 1);
        obdModuleInformation0.set(DM33EmissionIncreasingAECDActiveTime.create(0, timer0, timer1), 2);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm33_0 = DM33EmissionIncreasingAECDActiveTime.create(0, EngineHoursTimer.create(0, 1, 1));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm33_1 = DM33EmissionIncreasingAECDActiveTime.create(1, EngineHoursTimer.create(0, 1, 1));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(RequestResult.of(dm33_0, dm33_1));

        runTest();

        verify(diagnosticMessageModule).requestDM33(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
