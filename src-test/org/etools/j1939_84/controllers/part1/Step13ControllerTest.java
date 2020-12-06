/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.etools.j1939_84.model.PartResultFactory;
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
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step13ControllerTest extends AbstractControllerTest {
    private static final String EXPECTED_PASS_6_1_13_1_A = "6.1.13.1.a";
    private static final String EXPECTED_PASS_6_1_13_2_A = "6.1.13.2.a";
    private static final String EXPECTED_PASS_6_1_13_2_B = "6.1.13.2.b";
    private static final String EXPECTED_PASS_6_1_13_2_C = "6.1.13.2.c";
    private static final String EXPECTED_PASS_6_1_13_2_D = "6.1.13.2.d";
    private static final String EXPECTED_PASS_6_1_13_3_A = "6.1.13.3.a";
    private static final String EXPECTED_PASS_6_1_13_4_A = "6.1.13.4.a";
    private static final String EXPECTED_PASS_6_1_13_4_B = "6.1.13.4.b";
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

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SectionA6Validator sectionA6Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
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

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                obdModuleInformation,
                vehicleInformationModule,
                dataRepository,
                mockListener,
                reportFileModule,
                sectionA6Validator);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#run()}.
     */
    @Test
    public void testRun() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;

        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x00, 0x00, 0x33, 0x44, 0x94, 0x0A, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet17 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x00, 0x00, 0x00, 0x00, 0x68, 0x14, 0x00, 0x00));
        DM5DiagnosticReadinessPacket packet23 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x00, 0x00, 0x00, 0x20, 0x03, 0x01, 0x16, 0x00));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                false, new ArrayList<>() {
                    {
                        add(packet0);
                        add(packet17);
                        add(packet23);
                    }
                },
                Collections.emptyList());
        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(globalRequestResponse);

        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false,
                packet0);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x00))).thenReturn(busResult0x00);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false,
                packet17);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x17))).thenReturn(busResult0x17);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x23 = new BusResult<>(false,
                packet23);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x23))).thenReturn(busResult0x23);

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(true);

        when(dataRepository.getObdModuleAddresses()).thenReturn(new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x23);
            }
        });

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_1_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_C);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_D);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_3_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_1_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_B);

        verify(reportFileModule).onProgress(0, 1, "");
        StringBuilder expectedVehicleComposite = new StringBuilder("Global DM5 Vehicle Composite" + NL);
        expectedVehicleComposite.append("  DM5 Response from Engine #1 (0)" + NL)
                .append("    A/C system refrigerant             supported,   not completed" + NL)
                .append("    Boost pressure control sys         supported,       completed" + NL)
                .append("    Catalyst                       not supported,   not completed" + NL)
                .append("    Cold start aid system          not supported,       completed" + NL)
                .append("    Comprehensive component            supported,   not completed" + NL)
                .append("    Diesel Particulate Filter      not supported,       completed" + NL)
                .append("    EGR/VVT system                     supported,       completed" + NL)
                .append("    Evaporative system                 supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor             not supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor heater      not supported,   not completed" + NL)
                .append("    Fuel System                    not supported,       completed" + NL)
                .append("    Heated catalyst                not supported,   not completed" + NL)
                .append("    Misfire                        not supported,       completed" + NL)
                .append("    NMHC converting catalyst       not supported,       completed" + NL)
                .append("    NOx catalyst/adsorber              supported,   not completed" + NL)
                .append("    Secondary air system           not supported,       completed" + NL)
                .append("  DM5 Response from Instrument Cluster #1 (23)" + NL)
                .append("    A/C system refrigerant         not supported,       completed" + NL)
                .append("    Boost pressure control sys     not supported,       completed" + NL)
                .append("    Catalyst                       not supported,       completed" + NL)
                .append("    Cold start aid system          not supported,       completed" + NL)
                .append("    Comprehensive component        not supported,       completed" + NL)
                .append("    Diesel Particulate Filter          supported,       completed" + NL)
                .append("    EGR/VVT system                 not supported,       completed" + NL)
                .append("    Evaporative system             not supported,       completed" + NL)
                .append("    Exhaust Gas Sensor                 supported,       completed" + NL)
                .append("    Exhaust Gas Sensor heater          supported,       completed" + NL)
                .append("    Fuel System                    not supported,       completed" + NL)
                .append("    Heated catalyst                not supported,       completed" + NL)
                .append("    Misfire                        not supported,       completed" + NL)
                .append("    NMHC converting catalyst           supported,       completed" + NL)
                .append("    NOx catalyst/adsorber          not supported,       completed" + NL)
                .append("    Secondary air system               supported,       completed" + NL)
                .append("  DM5 Response from Body Controller (33)" + NL)
                .append("    A/C system refrigerant         not supported,   not completed" + NL)
                .append("    Boost pressure control sys     not supported,       completed" + NL)
                .append("    Catalyst                           supported,       completed" + NL)
                .append("    Cold start aid system              supported,       completed" + NL)
                .append("    Comprehensive component        not supported,       completed" + NL)
                .append("    Diesel Particulate Filter      not supported,       completed" + NL)
                .append("    EGR/VVT system                 not supported,       completed" + NL)
                .append("    Evaporative system             not supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor             not supported,       completed" + NL)
                .append("    Exhaust Gas Sensor heater      not supported,       completed" + NL)
                .append("    Fuel System                    not supported,   not completed" + NL)
                .append("    Heated catalyst                    supported,   not completed" + NL)
                .append("    Misfire                        not supported,       completed" + NL)
                .append("    NMHC converting catalyst       not supported,       completed" + NL)
                .append("    NOx catalyst/adsorber          not supported,       completed" + NL)
                .append("    Secondary air system           not supported,       completed");
        verify(reportFileModule).onResult(expectedVehicleComposite.toString());
        verify(reportFileModule).onResult("PASS: 6.1.13.1.a");
        verify(reportFileModule).onResult("PASS: 6.1.13.2.a");
        verify(reportFileModule).onResult("PASS: 6.1.13.2.b");
        verify(reportFileModule).onResult("PASS: 6.1.13.2.c");
        verify(reportFileModule).onResult("PASS: 6.1.13.2.d");
        verify(reportFileModule).onResult("PASS: 6.1.13.3.a");
        verify(reportFileModule).onResult("PASS: 6.1.13.4.a");
        verify(reportFileModule).onResult("PASS: 6.1.13.4.b");

        verify(sectionA6Validator).setJ1939(j1939);
        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        StringBuilder expectedResults = new StringBuilder("PASS: 6.1.13.1.a" + NL);
        expectedResults.append(expectedVehicleComposite.toString() + NL)
                .append("PASS: 6.1.13.2.a" + NL)
                .append("PASS: 6.1.13.2.b" + NL)
                .append("PASS: 6.1.13.2.c" + NL)
                .append("PASS: 6.1.13.2.d" + NL)
                .append("PASS: 6.1.13.3.a" + NL)
                .append("PASS: 6.1.13.4.a" + NL)
                .append("PASS: 6.1.13.4.b" + NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }

    @Test
    public void testStep13DM5PacketsEmpty() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;
        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x11, 0x22, 0x22, 0x44, 0x55, 0x66, 0x77, 0x88));
        final int ackPgn = DM5DiagnosticReadinessPacket.PGN;
        AcknowledgmentPacket packet44 = new AcknowledgmentPacket(
                Packet.create(ackPgn, 0x44, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08));
        DM5DiagnosticReadinessPacket packet21 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(new RequestResult<>(
                false, Collections.emptyList(), Collections.singletonList(packet44)));

        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false,
                packet0);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x00))).thenReturn(busResult0x00);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false,
                packet44);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x17))).thenReturn(busResult0x17);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x21 = new BusResult<>(false,
                packet21);
        when(diagnosticReadinessModule.requestDM5(any(), eq(true), eq(0x21))).thenReturn(busResult0x21);

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(false);

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
                "6.1.13.1.a Global DM5 request did not receive any respone packets");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.c Fail if no OBD ECU provides DM5 with readiness bits showing monitor support");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_D);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_3_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.1.a Global DM5 request did not receive any respone packets");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.c Fail if no OBD ECU provides DM5 with readiness bits showing monitor support");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_4_B);

        verify(reportFileModule).onProgress(0, 1, "");

        verify(reportFileModule).onResult("FAIL: 6.1.13.1.a Global DM5 request did not receive any respone packets");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_2_B);
        verify(reportFileModule).onResult(
                "FAIL: 6.1.13.2.c Fail if no OBD ECU provides DM5 with readiness bits showing monitor support");
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_2_D);
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_3_A);
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_4_A);
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_4_B);

        verify(sectionA6Validator).setJ1939(j1939);
        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        StringBuilder expectedResults = new StringBuilder(
                "FAIL: 6.1.13.1.a Global DM5 request did not receive any respone packets" + NL);
        expectedResults.append("FAIL: 6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation" + NL)
                .append(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_2_B + NL)
                .append("FAIL: 6.1.13.2.c Fail if no OBD ECU provides DM5 with readiness bits showing monitor support"
                        + NL)
                .append(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_2_D + NL)
                .append(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_3_A + NL)
                .append(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_4_A + NL)
                .append(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_4_B + NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }

    @Test
    public void testStep13DM5PacketsFail() {
        final int pgn = DM5DiagnosticReadinessPacket.PGN;
        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x00, 0x00, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM5DiagnosticReadinessPacket packet17 = new DM5DiagnosticReadinessPacket(
                Packet.create(pgn, 0x17, 0x11, 0x00, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        final int ackPgn = AcknowledgmentPacket.PGN;
        AcknowledgmentPacket packet21 = new AcknowledgmentPacket(
                Packet.create(ackPgn, 0x21, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                false, Arrays.asList(new DM5DiagnosticReadinessPacket[] { packet0, packet17 }),
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

        when(sectionA6Validator.verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER))).thenReturn(false);

        when(dataRepository.getObdModuleAddresses()).thenReturn(new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        });

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                EXPECTED_PASS_6_1_13_1_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0" + NL
                        + "  Reported active fault count = 0" + NL + "  Reported previously active fault count = 34");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0" + NL
                        + "  Reported active fault count = 17" + NL + "  Reported previously active fault count = 0");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, EXPECTED_PASS_6_1_13_2_C);
        StringBuilder expected2dWarning = new StringBuilder(
                "6.1.13.2.d Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU"
                        + NL);
        expected2dWarning.append("A/C system refrigerant     has reporting from more than one OBD ECU" + NL)
                .append("Boost pressure control sys has reporting from more than one OBD ECU" + NL)
                .append("Catalyst                   has reporting from more than one OBD ECU" + NL)
                .append("Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL)
                .append("Evaporative system         has reporting from more than one OBD ECU" + NL)
                .append("Exhaust Gas Sensor heater  has reporting from more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                expected2dWarning.toString());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3 OBD module Engine #1 (0) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3 OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3.a Destination Specific DM5 requests to OBD modules did not return any responses");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.4.a Fail if any difference compared to data received during global request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                EXPECTED_PASS_6_1_13_1_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0" + NL
                        + "  Reported active fault count = 0" + NL + "  Reported previously active fault count = 34");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0" + NL
                        + "  Reported active fault count = 17" + NL + "  Reported previously active fault count = 0");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                EXPECTED_PASS_6_1_13_2_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                expected2dWarning.toString());
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3 OBD module Engine #1 (0) did not return a response to a destination specific request");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3 OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.13.3.a Destination Specific DM5 requests to OBD modules did not return any responses");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.4.a Fail if any difference compared to data received during global request");
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.13.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).onProgress(0, 1, "");

        verify(reportFileModule).onResult("PASS: 6.1.13.1.a");
        StringBuilder expectedVehicleComposite = new StringBuilder("Global DM5 Vehicle Composite" + NL);
        expectedVehicleComposite.append("  DM5 Response from Engine #1 (0)" + NL)
                .append("    A/C system refrigerant             supported,   not completed" + NL)
                .append("    Boost pressure control sys         supported,       completed" + NL)
                .append("    Catalyst                           supported,   not completed" + NL)
                .append("    Cold start aid system          not supported,       completed" + NL)
                .append("    Comprehensive component            supported,   not completed" + NL)
                .append("    Diesel Particulate Filter          supported,       completed" + NL)
                .append("    EGR/VVT system                 not supported,       completed" + NL)
                .append("    Evaporative system                 supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor             not supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor heater          supported,   not completed" + NL)
                .append("    Fuel System                    not supported,       completed" + NL)
                .append("    Heated catalyst                not supported,   not completed" + NL)
                .append("    Misfire                        not supported,       completed" + NL)
                .append("    NMHC converting catalyst       not supported,       completed" + NL)
                .append("    NOx catalyst/adsorber          not supported,   not completed" + NL)
                .append("    Secondary air system           not supported,       completed" + NL)
                .append("  DM5 Response from Instrument Cluster #1 (23)" + NL)
                .append("    A/C system refrigerant             supported,   not completed" + NL)
                .append("    Boost pressure control sys         supported,       completed" + NL)
                .append("    Catalyst                           supported,   not completed" + NL)
                .append("    Cold start aid system          not supported,       completed" + NL)
                .append("    Comprehensive component            supported,   not completed" + NL)
                .append("    Diesel Particulate Filter          supported,       completed" + NL)
                .append("    EGR/VVT system                 not supported,       completed" + NL)
                .append("    Evaporative system                 supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor             not supported,   not completed" + NL)
                .append("    Exhaust Gas Sensor heater          supported,   not completed" + NL)
                .append("    Fuel System                    not supported,       completed" + NL)
                .append("    Heated catalyst                not supported,   not completed" + NL)
                .append("    Misfire                        not supported,       completed" + NL)
                .append("    NMHC converting catalyst       not supported,       completed" + NL)
                .append("    NOx catalyst/adsorber          not supported,   not completed" + NL)
                .append("    Secondary air system           not supported,       completed");
        verify(reportFileModule).onResult(expectedVehicleComposite.toString());
        verify(reportFileModule)
                .onResult("FAIL: 6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0"
                        + NL + "  Reported active fault count = 0" + NL
                        + "  Reported previously active fault count = 34");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0"
                        + NL + "  Reported active fault count = 17" + NL
                        + "  Reported previously active fault count = 0");
        verify(reportFileModule).onResult(
                "WARN: " + expected2dWarning.toString());
        verify(reportFileModule).onResult(PASS.toString() + COLON_SPACE + EXPECTED_PASS_6_1_13_2_C);
        verify(reportFileModule).onResult(
                "WARN: 6.1.13.3 OBD module Engine #1 (0) did not return a response to a destination specific request");
        verify(reportFileModule).onResult(
                "WARN: 6.1.13.3 OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request");
        verify(reportFileModule).onResult(
                "WARN: 6.1.13.3.a Destination Specific DM5 requests to OBD modules did not return any responses");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.13.4.a Fail if any difference compared to data received during global request");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.13.4.b Fail if NACK not received from OBD ECUs that did not respond to global query");

        verify(sectionA6Validator).setJ1939(j1939);
        verify(sectionA6Validator).verify(any(), eq(PART_NUMBER), eq(STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        StringBuilder expectedResults = new StringBuilder("PASS: 6.1.13.1.a" + NL);
        expectedResults.append(expectedVehicleComposite.toString() + NL)
                .append("FAIL: 6.1.13.2.a Fail/warn per section A.6, Criteria for Readiness 1 Evaluation" + NL)
                .append("FAIL: 6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0"
                        + NL)
                .append("  Reported active fault count = 0" + NL)
                .append("  Reported previously active fault count = 34" + NL)
                .append("FAIL: 6.1.13.2.b - Fail if any OBD ECU reports active/previously active fault DTCs count not = 0/0"
                        + NL)
                .append("  Reported active fault count = 17" + NL)
                .append("  Reported previously active fault count = 0" + NL)
                .append("PASS: 6.1.13.2.c" + NL)
                .append("WARN: " + expected2dWarning.toString() + NL)
                .append("WARN: 6.1.13.3 OBD module Engine #1 (0) did not return a response to a destination specific request"
                        + NL)
                .append("WARN: 6.1.13.3 OBD module Instrument Cluster #1 (23) did not return a response to a destination specific request"
                        + NL)
                .append("WARN: 6.1.13.3.a Destination Specific DM5 requests to OBD modules did not return any responses"
                        + NL)
                .append("FAIL: 6.1.13.4.a Fail if any difference compared to data received during global request" + NL)
                .append("FAIL: 6.1.13.4.b Fail if NACK not received from OBD ECUs that did not respond to global query"
                        + NL);
        assertEquals(expectedResults.toString(), listener.getResults());
    }

}
