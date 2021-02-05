/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime.create;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursTimer;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
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
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step15ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int STEP = 15;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step15Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part02Step15Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part " + PART + " Step " + STEP, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoResponsesSIEngine() {

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false));
        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "FAIL: 6.2.15.2.a - No ECU responded to the global request" + NL;
        expected += "FAIL: 6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.15.2.a - No ECU responded to the global request");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testNoResponsesCIEngine() {

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false));
        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "FAIL: 6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");

    }

    @Test
    public void testNoResponsesCIEngine2024() {

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false));
        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2024);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "FAIL: 6.2.15.2.a - No ECU responded to the global request" + NL;
        expected += "FAIL: 6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.15.2.a - No ECU responded to the global request");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailNoGlobalResponse() {
        var packet = create(0, EngineHoursTimer.create(0, 0, 0));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false));

        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false, packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "FAIL: 6.2.15.2.a - No ECU responded to the global request" + NL;
        expected += "FAIL: 6.2.15.5.a - Engine #1 (0) did not return timer 0 in both responses" + NL;
        expected += "FAIL: 6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.15.2.a - No ECU responded to the global request");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.a - Engine #1 (0) did not return timer 0 in both responses");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");

    }

    @Test
    public void testWarnValueOfFB() {
        var packet = create(0, EngineHoursTimer.create(0xFB, 0, 0));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false, packet));

        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false, packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "WARN: 6.2.15.3.a - Engine #1 (0) responded 0xFB for EI-AECD number" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        WARN,
                                        "6.2.15.3.a - Engine #1 (0) responded 0xFB for EI-AECD number");
    }

    @Test
    public void testFailTimerValues() {
        var globalPacket = create(0, EngineHoursTimer.create(1, 4, 10));
        var dsPacket = create(0, EngineHoursTimer.create(1, 7, 13));

        when(diagnosticMessageModule.requestDM33(any())).thenReturn(new RequestResult<>(false, globalPacket));

        when(diagnosticMessageModule.requestDM33(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2020);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any());

        String expected = "";
        expected += "FAIL: 6.2.15.5.a - Engine #1 (0) reported EiAECD Timer 1 from timer 1 with a difference of 3 which is greater than 2 minutes" + NL;
        expected += "FAIL: 6.2.15.5.a - Engine #1 (0) reported EiAECD Timer 2 from timer 1 with a difference of 3 which is greater than 2 minutes" + NL;

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.a - Engine #1 (0) reported EiAECD Timer 1 from timer 1 with a difference of 3 which is greater than 2 minutes");

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.15.5.a - Engine #1 (0) reported EiAECD Timer 2 from timer 1 with a difference of 3 which is greater than 2 minutes");
    }
}