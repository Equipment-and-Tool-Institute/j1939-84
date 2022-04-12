/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.model.FuelType.DSL;
import static org.etools.j1939tools.j1939.model.FuelType.GAS;
import static org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime.create;
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
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.EngineHoursTimer;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part09Step24ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 24;

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

        instance = new Part09Step24Controller(executor,
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
        var dsPacket = create(0, 0, EngineHoursTimer.create(1, 4, 10));

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dsPacket, 2);
        obdModule.set(dsPacket, 9);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoResponsesCIEngine() {

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.empty());

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(GAS);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        var previousPacket = create(0, 0, EngineHoursTimer.create(1, 4, 10));
        obdModule.set(previousPacket, 2);
        obdModule.set(previousPacket, 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.c. - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }

    @Test
    public void testdifferntNumberOfTimers() {
        var timer1 = EngineHoursTimer.create(1, 4, 10);
        var timer2 = EngineHoursTimer.create(1, 7, 9);
        var previousPacket = create(0, 0, timer1);
        var packet = create(0, 0, timer1, timer2);

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(previousPacket, 2);
        obdModule.set(previousPacket, 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.a - ECU Engine #1 (0) reported a different number EI-AECD here (2) and reported (1) in part 2");
    }

    @Test
    public void testWarnValueOfFB() {
        var timer1 = EngineHoursTimer.create(1, 4, 10);
        var timer2 = EngineHoursTimer.create(1, 7, 9);
        var packet = create(0, 0, timer1);

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2024);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(create(0, 0, timer2), 2);
        obdModule.set(create(0, 0, timer2), 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.b - ECU Engine #1 (0) reported timer 1 value less than previously observed in 6.9.7.1");
    }

    @Test
    public void testFailTimer2Values() {
        var globalPacket = create(0, 0, EngineHoursTimer.create(1, 4, 10));
        var dsPacket = create(0, 0, EngineHoursTimer.create(1, 7, 9));

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 2);
        obdModule.set(globalPacket, 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.b - ECU Engine #1 (0) reported timer 2 value less than previously observed in 6.9.7.1");
    }

    @Test
    public void testFailTimer1Values() {
        var globalPacket = create(0, 0, EngineHoursTimer.create(1, 7, 9));
        var dsPacket = create(0, 0, EngineHoursTimer.create(1, 3, 10));

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 2);
        obdModule.set(globalPacket, 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.b - ECU Engine #1 (0) reported timer 1 value less than previously observed in 6.9.7.1");
    }

    @Test
    public void testEngineModelYearGreaterThan2024() {
        var globalPacket = create(0, 0, EngineHoursTimer.create(1, 7, 9));
        var dsPacket = create(0, 0, EngineHoursTimer.create(1, 3, 10));

        when(communicationsModule.requestDM33(any(), eq(0))).thenReturn(RequestResult.of(dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(GAS);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 2);
        obdModule.set(globalPacket, 9);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM33(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.24.2.b - ECU Engine #1 (0) reported timer 1 value less than previously observed in 6.9.7.1");
    }

}
