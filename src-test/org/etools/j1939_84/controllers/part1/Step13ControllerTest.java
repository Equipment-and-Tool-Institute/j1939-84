/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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

/**
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Step13ControllerTest extends AbstractControllerTest {
    final int PGN = DM5DiagnosticReadinessPacket.PGN;
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step13Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDModuleInformation obdModuleInformation;

    //    @Mock
    //    private ReportFileModule reportFileModule;

    @Mock
    private SectionA6Validator sectionA6Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);

        instance = new Step13Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticReadinessModule,
                dataRepository,
                sectionA6Validator);

        ReportFileModule reportFileModule = mock(ReportFileModule.class);
        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                obdModuleInformation,
                vehicleInformationModule,
                dataRepository,
                mockListener,
                sectionA6Validator);
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testRun() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x00, 0x00, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        DM5DiagnosticReadinessPacket packet17 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00));
        DM5DiagnosticReadinessPacket packet23 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                false, List.of(packet0, packet17, packet23), Collections.emptyList());
        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(globalRequestResponse);

        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false, packet0);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x00))).thenReturn(busResult0x00);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false, packet17);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x17))).thenReturn(busResult0x17);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x23 = new BusResult<>(false, packet23);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x23))).thenReturn(busResult0x23);

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse))).thenReturn(
                true);

        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0x00, 0x17, 0x23));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    A/C system refrigerant         not supported,       completed" + NL +
                "    Boost pressure control sys         supported,   not completed" + NL +
                "    Catalyst                       not supported,       completed" + NL +
                "    Cold start aid system          not supported,       completed" + NL +
                "    Comprehensive component            supported,       completed" + NL +
                "    Diesel Particulate Filter          supported,   not completed" + NL +
                "    EGR/VVT system                     supported,   not completed" + NL +
                "    Evaporative system             not supported,       completed" + NL +
                "    Exhaust Gas Sensor                 supported,   not completed" + NL +
                "    Exhaust Gas Sensor heater          supported,   not completed" + NL +
                "    Fuel System                        supported,   not completed" + NL +
                "    Heated catalyst                not supported,       completed" + NL +
                "    Misfire                            supported,   not completed" + NL +
                "    NMHC converting catalyst           supported,   not completed" + NL +
                "    NOx catalyst/adsorber              supported,   not completed" + NL +
                "    Secondary air system           not supported,       completed" + NL;
        assertEquals(expectedVehicleComposite + NL, listener.getResults());
    }

    @Test
    public void testStep13DM5PacketsEmpty() {
        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(PGN, 0x00, 0x00, 0x00, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        final int ackPgn = DM5DiagnosticReadinessPacket.PGN;
        AcknowledgmentPacket packet44 = new AcknowledgmentPacket(
                Packet.create(ackPgn, 0x44, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet21 = new DM5DiagnosticReadinessPacket(
                Packet.create(PGN, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                Collections.emptyList(),
                Collections.singletonList(packet44));
        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(globalRequestResponse);

        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false,
                packet0);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x00))).thenReturn(busResult0x00);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false,
                packet44);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x17))).thenReturn(busResult0x17);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x21 = new BusResult<>(false,
                packet21);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x21))).thenReturn(busResult0x21);

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse))).thenReturn(
                false);

        when(dataRepository.getObdModuleAddresses()).thenReturn(List.of(0x00, 0x17, 0x21));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.1.a - Global DM5 request did not receive any response packets");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support");

        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = "FAIL: 6.1.13.1.a - Global DM5 request did not receive any response packets" + NL;
        expectedResults += "FAIL: 6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testStep13DM5PacketsFail() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;
        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x03, 0x10, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        DM5DiagnosticReadinessPacket packet17 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x00, 0x00, 0x14, 0x37, 0xE0, 0x1E, 0xE0, 0x1E));
        final int ackPgn = AcknowledgmentPacket.PGN;
        AcknowledgmentPacket packet21 = new AcknowledgmentPacket(
                Packet.create(ackPgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                false, Arrays.asList(packet0, packet17),
                Collections.singletonList(packet21));
        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(globalRequestResponse);

        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false,
                Optional.empty());
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x00))).thenReturn(busResult0x00);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false,
                Optional.empty());
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x17))).thenReturn(busResult0x17);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x21 = new BusResult<>(false,
                packet21);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x21))).thenReturn(busResult0x21);

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse))).thenReturn(
                false);

        when(dataRepository.getObdModuleAddresses()).thenReturn(new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        });

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.b - An OBD ECU reported active/previously active fault DTCs count not = 0/0" + NL
                        + "  Reported active fault count = 3" + NL + "  Reported previously active fault count = 16");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3 - OBD module Engine #1 (0) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                WARN,
                "6.1.13.3 - OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3.a - Destination Specific DM5 requests to OBD modules did not return any responses");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.4.a - A difference compared to data received during global request");

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.13.4.b - NACK not received from OBD ECUs that did not respond to global query");

        String expected2dWarning = "6.1.13.2.d - An individual required monitor is supported by more than one OBD ECU" + NL +
                "Boost pressure control sys has reporting from more than one OBD ECU" + NL +
                "Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL +
                "EGR/VVT system             has reporting from more than one OBD ECU" + NL +
                "Exhaust Gas Sensor         has reporting from more than one OBD ECU" + NL +
                "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU" + NL +
                "Fuel System                has reporting from more than one OBD ECU" + NL +
                "Misfire                    has reporting from more than one OBD ECU" + NL +
                "NMHC converting catalyst   has reporting from more than one OBD ECU" + NL +
                "NOx catalyst/adsorber      has reporting from more than one OBD ECU";
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, expected2dWarning);

        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER), eq(globalRequestResponse));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    A/C system refrigerant         not supported,       completed" + NL +
                "    Boost pressure control sys         supported,   not completed" + NL +
                "    Catalyst                       not supported,       completed" + NL +
                "    Cold start aid system          not supported,       completed" + NL +
                "    Comprehensive component            supported,       completed" + NL +
                "    Diesel Particulate Filter          supported,   not completed" + NL +
                "    EGR/VVT system                     supported,   not completed" + NL +
                "    Evaporative system             not supported,       completed" + NL +
                "    Exhaust Gas Sensor                 supported,   not completed" + NL +
                "    Exhaust Gas Sensor heater          supported,   not completed" + NL +
                "    Fuel System                        supported,   not completed" + NL +
                "    Heated catalyst                not supported,       completed" + NL +
                "    Misfire                            supported,   not completed" + NL +
                "    NMHC converting catalyst           supported,   not completed" + NL +
                "    NOx catalyst/adsorber              supported,   not completed" + NL +
                "    Secondary air system           not supported,       completed" + NL;
        String expectedResults = expectedVehicleComposite + NL;
        expectedResults += "FAIL: 6.1.13.2.b - An OBD ECU reported active/previously active fault DTCs count not = 0/0" + NL;
        expectedResults += "  Reported active fault count = 3" + NL;
        expectedResults += "  Reported previously active fault count = 16" + NL;

        expectedResults += "WARN: " + expected2dWarning + NL;
        expectedResults += "WARN: 6.1.13.3 - OBD module Engine #1 (0) did not return a response to a destination specific request" + NL;
        expectedResults += "WARN: 6.1.13.3 - OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request" + NL;
        expectedResults += "WARN: 6.1.13.3.a - Destination Specific DM5 requests to OBD modules did not return any responses" + NL;
        expectedResults += "FAIL: 6.1.13.4.a - A difference compared to data received during global request" + NL;
        expectedResults += "FAIL: 6.1.13.4.b - NACK not received from OBD ECUs that did not respond to global query" + NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
