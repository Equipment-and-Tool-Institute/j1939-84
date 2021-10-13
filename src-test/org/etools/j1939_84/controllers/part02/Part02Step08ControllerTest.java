/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static net.soliddesign.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket.PGN;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
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

import net.soliddesign.j1939tools.bus.Packet;
import net.soliddesign.j1939tools.bus.RequestResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step08ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int STEP = 8;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step08Controller instance;

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

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part02Step08Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART + " Step " + STEP, instance.getDisplayName());
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
    public void testNoResponsesNoModules() {
        when(communicationsModule.requestDM26(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(communicationsModule).requestDM26(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoResponses() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(true));
        when(communicationsModule.requestDM26(any())).thenReturn(new RequestResult<>(true));

        runTest();

        verify(communicationsModule).requestDM26(any());
        verify(communicationsModule).requestDM26(any(), eq(0x01));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.8.2.c - Engine #2 (1) did not provide a NACK and did not provide a DM26 response");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        INFO,
                                        "6.2.8.5.a - No responses received from Engine #2 (1)");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailures() {
        // Module 0 has a different packet from the first time
        DM26TripDiagnosticReadinessPacket packet0 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(PGN,
                                                                                                        0x00,
                                                                                                        0x11,
                                                                                                        0x22,
                                                                                                        0x33,
                                                                                                        0x44,
                                                                                                        0x55,
                                                                                                        0x66,
                                                                                                        0x77,
                                                                                                        0x88));
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(packet0, 1);
        dataRepository.putObdModule(obdModule0);
        DM26TripDiagnosticReadinessPacket packet00 = new DM26TripDiagnosticReadinessPacket(
                                                                                           Packet.create(PGN,
                                                                                                         0x00,
                                                                                                         0x99,
                                                                                                         0xAA,
                                                                                                         0xBB,
                                                                                                         0xCC,
                                                                                                         0xDD,
                                                                                                         0xEE,
                                                                                                         0xFF,
                                                                                                         0x00));
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(new RequestResult<>(false, packet00));

        // Module 1 has the same both times and will not report an error
        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(PGN,
                                                                                                        0x01,
                                                                                                        0x00,
                                                                                                        0x00,
                                                                                                        0x04,
                                                                                                        0x00,
                                                                                                        0xFF,
                                                                                                        0xFF,
                                                                                                        0xFF,
                                                                                                        0xFF));
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(new RequestResult<>(false, packet1));

        // Module 2 will not respond from the first time, but will respond this time
        dataRepository.putObdModule(new OBDModuleInformation(2));
        DM26TripDiagnosticReadinessPacket packet2 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(PGN,
                                                                                                        0x02,
                                                                                                        0x00,
                                                                                                        0x00,
                                                                                                        0x04,
                                                                                                        0x00,
                                                                                                        0xFF,
                                                                                                        0xFF,
                                                                                                        0xFF,
                                                                                                        0xFF));
        when(communicationsModule.requestDM26(any(), eq(2))).thenReturn(new RequestResult<>(false, packet2));

        // Module 3 will not respond
        dataRepository.putObdModule(new OBDModuleInformation(3));
        when(communicationsModule.requestDM26(any(), eq(3))).thenReturn(new RequestResult<>(true));

        when(communicationsModule.requestDM26(any()))
                                                        .thenReturn(new RequestResult<>(false,
                                                                                        packet0,
                                                                                        packet1,
                                                                                        packet2));

        runTest();

        verify(communicationsModule).requestDM26(any());
        verify(communicationsModule).requestDM26(any(), eq(0x00));
        verify(communicationsModule).requestDM26(any(), eq(0x01));
        verify(communicationsModule).requestDM26(any(), eq(0x02));
        verify(communicationsModule).requestDM26(any(), eq(0x03));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system          enabled, not complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst                enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system           enabled, not complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.a - Difference from Turbocharger (2) monitor support bits this cycle compared to responses in part 1 after DM11");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.b - Turbocharger (2) indicates number of warm-ups since code clear greater than zero");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.a - Difference from Engine #1 (0) monitor support bits this cycle compared to responses in part 1 after DM11");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.c - Transmission #1 (3) did not provide a NACK and did not provide a DM26 response");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.b - Engine #2 (1) indicates number of warm-ups since code clear greater than zero");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor A/C system refrigerant is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Boost pressure control sys is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Cold start aid system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Diesel Particulate Filter is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor EGR/VVT system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Evaporative system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Exhaust Gas Sensor is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Exhaust Gas Sensor heater is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Heated catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor NMHC converting catalyst is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor NOx catalyst/adsorber is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        WARN,
                                        "6.2.8.3.a - Required monitor Secondary air system is supported by more than one OBD ECU");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.5.a - Difference in data between DS and global responses from Engine #1 (0)");
        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        INFO,
                                        "6.2.8.5.a - No responses received from Transmission #1 (3)");

    }

    @Test
    public void testNoFailures() {

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                                                                                          Packet.create(PGN,
                                                                                                        0x01,
                                                                                                        0x00,
                                                                                                        0x00,
                                                                                                        0x00,
                                                                                                        0x44,
                                                                                                        0x55,
                                                                                                        0x66,
                                                                                                        0x77,
                                                                                                        0x88));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM26(any())).thenReturn(new RequestResult<>(false, packet1));
        when(communicationsModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        runTest();

        verify(communicationsModule).requestDM26(any());
        verify(communicationsModule).requestDM26(any(), eq(0x01));

        String expectedResults = NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
