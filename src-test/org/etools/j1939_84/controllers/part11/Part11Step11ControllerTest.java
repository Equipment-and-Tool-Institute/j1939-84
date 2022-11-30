/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
public class Part11Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 11;

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

        instance = new Part11Step11Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0), 11);
        dataRepository.putObdModule(obdModuleInformation);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 9, 1);
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));
        verify(communicationsModule).requestDM26(any(), eq(1));

        assertSame(dm26, dataRepository.getObdModule(0).getLatest(DM26TripDiagnosticReadinessPacket.class));
        assertNull(dataRepository.getObdModule(1).getLatest(DM26TripDiagnosticReadinessPacket.class));

        assertEquals(9.0, dataRepository.getObdModule(0).getDeltaEngineStart(), 0.0);
        assertEquals("", listener.getMessages());

        String expected = "" + NL;
        expected += "Vehicle Composite of DM26:" + NL;
        expected += "    Comprehensive component    not enabled, not complete" + NL
                + "    Fuel System                not enabled, not complete" + NL
                + "    Misfire                    not enabled, not complete" + NL
                + "    EGR/VVT system             not enabled, not complete" + NL
                + "    Exhaust Gas Sensor heater  not enabled, not complete" + NL
                + "    Exhaust Gas Sensor         not enabled, not complete" + NL
                + "    A/C system refrigerant     not enabled, not complete" + NL
                + "    Secondary air system       not enabled, not complete" + NL
                + "    Evaporative system         not enabled, not complete" + NL
                + "    Heated catalyst            not enabled, not complete" + NL
                + "    Catalyst                   not enabled, not complete" + NL
                + "    NMHC converting catalyst   not enabled, not complete" + NL
                + "    NOx catalyst/adsorber      not enabled, not complete" + NL
                + "    Diesel Particulate Filter  not enabled, not complete" + NL
                + "    Boost pressure control sys not enabled, not complete" + NL
                + "    Cold start aid system      not enabled, not complete" + NL;
        assertEquals(expected, listener.getResults());

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForTimeTooLong() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0), 11);
        dataRepository.putObdModule(obdModuleInformation);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 100, 1);
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));

        assertEquals("", listener.getMessages());
        String expected = "" + NL;
        expected += "Vehicle Composite of DM26:" + NL;
        expected += "    Comprehensive component    not enabled, not complete" + NL
                + "    Fuel System                not enabled, not complete" + NL
                + "    Misfire                    not enabled, not complete" + NL
                + "    EGR/VVT system             not enabled, not complete" + NL
                + "    Exhaust Gas Sensor heater  not enabled, not complete" + NL
                + "    Exhaust Gas Sensor         not enabled, not complete" + NL
                + "    A/C system refrigerant     not enabled, not complete" + NL
                + "    Secondary air system       not enabled, not complete" + NL
                + "    Evaporative system         not enabled, not complete" + NL
                + "    Heated catalyst            not enabled, not complete" + NL
                + "    Catalyst                   not enabled, not complete" + NL
                + "    NMHC converting catalyst   not enabled, not complete" + NL
                + "    NOx catalyst/adsorber      not enabled, not complete" + NL
                + "    Diesel Particulate Filter  not enabled, not complete" + NL
                + "    Boost pressure control sys not enabled, not complete" + NL
                + "    Cold start aid system      not enabled, not complete" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.11.2.a - Engine #1 (0) reported time since engine start differs by more than ±10 seconds from expected value");
    }

    @Test
    public void testFailureForTimeTooShort() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        DM26TripDiagnosticReadinessPacket repoDM26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0);
        repoDM26.getPacket().setTimestamp(LocalDateTime.of(2020, 3, 4, 11, 33, 0));
        obdModuleInformation.set(repoDM26, 11);
        dataRepository.putObdModule(obdModuleInformation);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 5, 1);
        dm26.getPacket().setTimestamp(LocalDateTime.of(2020, 3, 4, 11, 33, 16));
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));

        assertEquals("", listener.getMessages());

        String expected = "" + NL;
        expected += "Vehicle Composite of DM26:" + NL;
        expected += "    Comprehensive component    not enabled, not complete" + NL
                + "    Fuel System                not enabled, not complete" + NL
                + "    Misfire                    not enabled, not complete" + NL
                + "    EGR/VVT system             not enabled, not complete" + NL
                + "    Exhaust Gas Sensor heater  not enabled, not complete" + NL
                + "    Exhaust Gas Sensor         not enabled, not complete" + NL
                + "    A/C system refrigerant     not enabled, not complete" + NL
                + "    Secondary air system       not enabled, not complete" + NL
                + "    Evaporative system         not enabled, not complete" + NL
                + "    Heated catalyst            not enabled, not complete" + NL
                + "    Catalyst                   not enabled, not complete" + NL
                + "    NMHC converting catalyst   not enabled, not complete" + NL
                + "    NOx catalyst/adsorber      not enabled, not complete" + NL
                + "    Diesel Particulate Filter  not enabled, not complete" + NL
                + "    Boost pressure control sys not enabled, not complete" + NL
                + "    Cold start aid system      not enabled, not complete" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.11.2.a - Engine #1 (0) reported time since engine start differs by more than ±10 seconds from expected value");
    }

    @Test
    public void testFailureForNoNACK() {

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(new RequestResult<>(true));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.11.2.b - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
