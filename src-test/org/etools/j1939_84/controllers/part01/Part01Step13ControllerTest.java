/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
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
public class Part01Step13ControllerTest extends AbstractControllerTest {
    public static final int PGN = DM5DiagnosticReadinessPacket.PGN;
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 13;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step13Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDModuleInformation obdModuleInformation;

    @Mock
    private SectionA6Validator sectionA6Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step13Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              sectionA6Validator,
                                              DateTimeModule.getInstance());

        ReportFileModule reportFileModule = mock(ReportFileModule.class);
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
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 obdModuleInformation,
                                 vehicleInformationModule,
                                 mockListener,
                                 sectionA6Validator,
                                 communicationsModule);
    }

    /**
     * Test method for
     * {@link Part01Step13Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step13Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step13Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step13Controller#run()}.
     * Test three module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM05 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM05 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM05 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM05 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM05
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM05
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.13.1.a", description = "Global DM5 [(send Request (PG 59904) for PG 65230 (SPs 1218-1223))]."),
            @TestItem(verifies = "6.1.13.1.b", description = "Display monitor readiness composite value in log for OBD ECU replies only."),
            @TestItem(verifies = "6.1.13.3.a", description = "DS DM5 to each OBD ECU.") })
    public void testRun() {

        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(PGN,
                                                                                              0x00,
                                                                                              0xFF,
                                                                                              0xFF,
                                                                                              0x14,
                                                                                              0x37,
                                                                                              0xE0,
                                                                                              0x1E,
                                                                                              0xE0,
                                                                                              0x1E));
        DM5DiagnosticReadinessPacket packet17 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(PGN,
                                                                                               0x17,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x05,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00));
        DM5DiagnosticReadinessPacket packet21 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(PGN,
                                                                                               0x21,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x05,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x00));

        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                                                                                                false,
                                                                                                packet0,
                                                                                                packet17,
                                                                                                packet21);
        when(communicationsModule.requestDM5(any())).thenReturn(globalRequestResponse);

        runTest();

        verify(communicationsModule).requestDM5(any());

        verify(sectionA6Validator).verify(any(), eq("6.1.13.2.a"), eq(globalRequestResponse),eq(false));

        assertEquals("", listener.getMessages());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    A/C system refrigerant     not supported,     complete" + NL +
                "    Boost pressure control sys     supported, not complete" + NL +
                "    Catalyst                   not supported,     complete" + NL +
                "    Cold start aid system      not supported,     complete" + NL +
                "    Comprehensive component        supported,     complete" + NL +
                "    Diesel Particulate Filter      supported, not complete" + NL +
                "    EGR/VVT system                 supported, not complete" + NL +
                "    Evaporative system         not supported,     complete" + NL +
                "    Exhaust Gas Sensor             supported, not complete" + NL +
                "    Exhaust Gas Sensor heater      supported, not complete" + NL +
                "    Fuel System                    supported, not complete" + NL +
                "    Heated catalyst            not supported,     complete" + NL +
                "    Misfire                        supported, not complete" + NL +
                "    NMHC converting catalyst       supported, not complete" + NL +
                "    NOx catalyst/adsorber          supported, not complete" + NL +
                "    Secondary air system       not supported,     complete" + NL;
        assertEquals(expectedVehicleComposite + NL, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step13Controller#run()}.
     * Test one module responding with an ACK:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x44</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.13.1.a", description = "Global DM5 [(send Request (PG 59904) for PG 65230 (SPs 1218-1223))]") })
    public void testStep13DM5PacketsEmpty() {
        dataRepository.putObdModule(new OBDModuleInformation(0x44));

        AcknowledgmentPacket ackPacket0x44 = new AcknowledgmentPacket(Packet.create(PGN,
                                                                                    0x44,
                                                                                    0x01,
                                                                                    0x02,
                                                                                    0x03,
                                                                                    0x04,
                                                                                    0x05,
                                                                                    0x06,
                                                                                    0x07,
                                                                                    0x08));
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                List.of(),
                                                                                                List.of(ackPacket0x44));
        when(communicationsModule.requestDM5(any(ResultsListener.class))).thenReturn(globalRequestResponse);

        BusResult<DM5DiagnosticReadinessPacket> busResult0x44 = new BusResult<>(false,
                                                                                ackPacket0x44);
        when(communicationsModule.requestDM5(any(), eq(0x44))).thenReturn(busResult0x44);

        dataRepository.putObdModule(new OBDModuleInformation(0x44));

        runTest();

        verify(communicationsModule).requestDM5(any(ResultsListener.class));
        verify(communicationsModule).requestDM5(any(ResultsListener.class), eq(0x44));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.13.1.a - Global DM5 request did not receive any response packets");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.13.2.c - No OBD ECU provided DM5 with readiness bits showing monitor support");

        verify(sectionA6Validator).verify(any(), eq("6.1.13.2.a"), eq(globalRequestResponse),eq(false));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step13Controller#run()}.
     * Test four modules responding one with a NACK:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x17</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no repsonse</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK - PG 65230 (DM5)</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x44</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.13.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testStep13DM5NackNotRecieved() {
        AcknowledgmentPacket ackPacket0x44 = new AcknowledgmentPacket(Packet.create(AcknowledgmentPacket.PGN,
                                                                                    0x44,
                                                                                    0x01,
                                                                                    0x02,
                                                                                    0x03,
                                                                                    0x04,
                                                                                    0x05,
                                                                                    0x06,
                                                                                    0x07,
                                                                                    0x08));

        DM5DiagnosticReadinessPacket packet0x00 = new DM5DiagnosticReadinessPacket(Packet.create(PGN,
                                                                                                 0x00,
                                                                                                 0xFF,
                                                                                                 0xFF,
                                                                                                 0x14,
                                                                                                 0x37,
                                                                                                 0xE0,
                                                                                                 0x1E,
                                                                                                 0xE0,
                                                                                                 0x1E));
        BusResult<DM5DiagnosticReadinessPacket> busResult0x00 = new BusResult<>(false,
                                                                                packet0x00);
        when(communicationsModule.requestDM5(any(ResultsListener.class), eq(0x00))).thenReturn(busResult0x00);

        DM5DiagnosticReadinessPacket packet0x17 = new DM5DiagnosticReadinessPacket(Packet.create(PGN,
                                                                                                 0x17,
                                                                                                 0xFF,
                                                                                                 0xFF,
                                                                                                 0x14,
                                                                                                 0x37,
                                                                                                 0xE0,
                                                                                                 0x1E,
                                                                                                 0xE0,
                                                                                                 0x1E));
        BusResult<DM5DiagnosticReadinessPacket> busResult0x17 = new BusResult<>(false,
                                                                                packet0x17);
        when(communicationsModule.requestDM5(any(), eq(0x17))).thenReturn(busResult0x17);

        AcknowledgmentPacket ackPacket0x21 = AcknowledgmentPacket.create(0x21, NACK);
        BusResult<DM5DiagnosticReadinessPacket> busResult0x21 = new BusResult<>(false,
                                                                                ackPacket0x21);
        when(communicationsModule.requestDM5(any(), eq(0x21))).thenReturn(busResult0x21);
        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(false,
                                                                                                List.of(packet0x00),
                                                                                                List.of(ackPacket0x44));
        when(communicationsModule.requestDM5(any(ResultsListener.class))).thenReturn(globalRequestResponse);

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));

        runTest();

        verify(communicationsModule).requestDM5(any());
        verify(communicationsModule).requestDM5(any(), eq(0x00));
        verify(communicationsModule).requestDM5(any(), eq(0x17));
        verify(communicationsModule).requestDM5(any(), eq(0x21));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.13.4.b. - OBD ECU Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query");

        verify(sectionA6Validator).verify(any(ResultsListener.class), eq("6.1.13.2.a"), eq(globalRequestResponse),eq(false));

        assertEquals("", listener.getMessages());

        String expectedResult = "";
        expectedResult += NL + "Vehicle Composite of DM5:";
        expectedResult += NL + "    A/C system refrigerant     not supported,     complete";
        expectedResult += NL + "    Boost pressure control sys     supported, not complete";
        expectedResult += NL + "    Catalyst                   not supported,     complete";
        expectedResult += NL + "    Cold start aid system      not supported,     complete";
        expectedResult += NL + "    Comprehensive component        supported,     complete";
        expectedResult += NL + "    Diesel Particulate Filter      supported, not complete";
        expectedResult += NL + "    EGR/VVT system                 supported, not complete";
        expectedResult += NL + "    Evaporative system         not supported,     complete";
        expectedResult += NL + "    Exhaust Gas Sensor             supported, not complete";
        expectedResult += NL + "    Exhaust Gas Sensor heater      supported, not complete";
        expectedResult += NL + "    Fuel System                    supported, not complete";
        expectedResult += NL + "    Heated catalyst            not supported,     complete";
        expectedResult += NL + "    Misfire                        supported, not complete";
        expectedResult += NL + "    NMHC converting catalyst       supported, not complete";
        expectedResult += NL + "    NOx catalyst/adsorber          supported, not complete";
        expectedResult += NL + "    Secondary air system       not supported,     complete" + NL + NL;
        assertEquals(expectedResult, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step13Controller#run()}.
     * Test one module responding with an ACK:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x44</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">ACK - PG 65230 (DM5)</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.13.1.a", description = "Global DM5 [(send Request (PG 59904) for PG 65230 (SPs 1218-1223))] - no packet returned.") })
    public void testStep13DM5PacketsFail() {
        DM5DiagnosticReadinessPacket packet0 = new DM5DiagnosticReadinessPacket(
                                                                                Packet.create(PGN,
                                                                                              0x00,
                                                                                              0x03,
                                                                                              0x10,
                                                                                              0x14,
                                                                                              0x37,
                                                                                              0xE0,
                                                                                              0x1E,
                                                                                              0xE0,
                                                                                              0x1E));
        DM5DiagnosticReadinessPacket packet21 = new DM5DiagnosticReadinessPacket(
                                                                                 Packet.create(PGN,
                                                                                               0x21,
                                                                                               0x00,
                                                                                               0x00,
                                                                                               0x14,
                                                                                               0x37,
                                                                                               0xE0,
                                                                                               0x1E,
                                                                                               0xE0,
                                                                                               0x1E));
        DM5DiagnosticReadinessPacket packet21V2 = new DM5DiagnosticReadinessPacket(
                                                                                   Packet.create(PGN,
                                                                                                 0x21,
                                                                                                 0x00,
                                                                                                 0x00,
                                                                                                 0x00,
                                                                                                 0x00,
                                                                                                 0xE0,
                                                                                                 0x1E,
                                                                                                 0xE0,
                                                                                                 0x1E));

        final int ackPgn = AcknowledgmentPacket.PGN;
        AcknowledgmentPacket packet23 = new AcknowledgmentPacket(
                                                                 Packet.create(ackPgn,
                                                                               0x23,
                                                                               0x10,
                                                                               0x20,
                                                                               0x30,
                                                                               0x40,
                                                                               0x50,
                                                                               0x60,
                                                                               0x70,
                                                                               0x80));
        RequestResult<DM5DiagnosticReadinessPacket> globalResponse = new RequestResult<>(
                                                                                         false,
                                                                                         List.of(packet0, packet21),
                                                                                         List.of(packet23));
        when(communicationsModule.requestDM5(any())).thenReturn(globalResponse);

        when(communicationsModule.requestDM5(any(), eq(0x00)))
                                                                 .thenReturn(new BusResult<>(false, Optional.empty()));
        when(communicationsModule.requestDM5(any(), eq(0x17)))
                                                                 .thenReturn(new BusResult<>(false, Optional.empty()));
        when(communicationsModule.requestDM5(any(), eq(0x21)))
                                                                 .thenReturn(new BusResult<>(false, packet21V2));
        when(communicationsModule.requestDM5(any(), eq(0x23)))
                                                                 .thenReturn(new BusResult<>(false, packet23));

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x21));
        dataRepository.putObdModule(new OBDModuleInformation(0x23));

        runTest();

        verify(communicationsModule).requestDM5(any());
        verify(communicationsModule).requestDM5(any(), eq(0x00));
        verify(communicationsModule).requestDM5(any(), eq(0x17));
        verify(communicationsModule).requestDM5(any(), eq(0x21));
        verify(communicationsModule).requestDM5(any(), eq(0x23));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.13.2.b - OBD ECU Engine #1 (0) reported active DTC count not = 0");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.13.2.b - OBD ECU Engine #1 (0) reported previously active DTC count not = 0");

        verify(mockListener).addOutcome(
                                        1,
                                        13,
                                        FAIL,
                                        "6.1.13.4.a - Difference compared to data received during global request from Body Controller (33)");
        verify(mockListener).addOutcome(
                                        1,
                                        13,
                                        FAIL,
                                        "6.1.13.4.b. - OBD ECU Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(
                                        1,
                                        13,
                                        FAIL,
                                        "6.1.13.4.b. - OBD ECU Hitch Control (35) did not provide a response to Global query and did not provide a NACK for the DS query");

        verify(sectionA6Validator).verify(any(), eq("6.1.13.2.a"), eq(globalResponse),eq(false));

        assertEquals("", listener.getMessages());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    A/C system refrigerant     not supported,     complete" + NL +
                "    Boost pressure control sys     supported, not complete" + NL +
                "    Catalyst                   not supported,     complete" + NL +
                "    Cold start aid system      not supported,     complete" + NL +
                "    Comprehensive component        supported,     complete" + NL +
                "    Diesel Particulate Filter      supported, not complete" + NL +
                "    EGR/VVT system                 supported, not complete" + NL +
                "    Evaporative system         not supported,     complete" + NL +
                "    Exhaust Gas Sensor             supported, not complete" + NL +
                "    Exhaust Gas Sensor heater      supported, not complete" + NL +
                "    Fuel System                    supported, not complete" + NL +
                "    Heated catalyst            not supported,     complete" + NL +
                "    Misfire                        supported, not complete" + NL +
                "    NMHC converting catalyst       supported, not complete" + NL +
                "    NOx catalyst/adsorber          supported, not complete" + NL +
                "    Secondary air system       not supported,     complete" + NL;
        assertEquals(expectedVehicleComposite + NL, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step13Controller#run()}.
     * Test three modules responding with one with changing data<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response<br>
     * data</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response<br>
     * data</></td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x21</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response<br>
     * data</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM5 response<br>
     * different data</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x23</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK - PG 59392 (AcknowledgmentPacket)</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK - PG 59392 (AcknowledgmentPacket)</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.13.4.a", description = "Fail if any difference compared to data received during global request.") })
    public void testGlobalAndDsDataDifferentFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        DM5DiagnosticReadinessPacket packet0x17 = new DM5DiagnosticReadinessPacket(
                                                                                   Packet.create(PGN,
                                                                                                 0x17,
                                                                                                 0xFF,
                                                                                                 0xFF,
                                                                                                 0x14,
                                                                                                 0x37,
                                                                                                 0xE0,
                                                                                                 0x1E,
                                                                                                 0xE0,
                                                                                                 0x1E));
        DM5DiagnosticReadinessPacket dsPacket0x17 = new DM5DiagnosticReadinessPacket(
                                                                                     Packet.create(PGN,
                                                                                                   0x17,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x05,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00));
        when(communicationsModule.requestDM5(any(ResultsListener.class),
                                             eq(0x17))).thenReturn(new BusResult<>(false, dsPacket0x17));

        dataRepository.putObdModule(new OBDModuleInformation(0x23));
        AcknowledgmentPacket nackPacket0x23 = AcknowledgmentPacket.create(0x23,
                                                                          NACK,
                                                                          1,
                                                                          0xF9,
                                                                          PGN);
        when(communicationsModule.requestDM5(any(ResultsListener.class),
                                             eq(0x23))).thenReturn(new BusResult<>(false, nackPacket0x23));

        RequestResult<DM5DiagnosticReadinessPacket> globalRequestResponse = new RequestResult<>(
                                                                                                false,
                                                                                                List.of(packet0x17),
                                                                                                List.of());
        when(communicationsModule.requestDM5(any(ResultsListener.class))).thenReturn(globalRequestResponse);

        runTest();

        verify(communicationsModule).requestDM5(any(ResultsListener.class));
        verify(communicationsModule).requestDM5(any(ResultsListener.class), eq(0x17));
        verify(communicationsModule).requestDM5(any(ResultsListener.class), eq(0x23));

        verify(mockListener).addOutcome(
                                        1,
                                        13,
                                        FAIL,
                                        "6.1.13.4.a - Difference compared to data received during global request from Instrument Cluster #1 (23)");

        verify(sectionA6Validator).verify(any(), eq("6.1.13.2.a"), eq(globalRequestResponse),eq(false));

        assertEquals("", listener.getMessages());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    A/C system refrigerant     not supported,     complete" + NL +
                "    Boost pressure control sys     supported, not complete" + NL +
                "    Catalyst                   not supported,     complete" + NL +
                "    Cold start aid system      not supported,     complete" + NL +
                "    Comprehensive component        supported,     complete" + NL +
                "    Diesel Particulate Filter      supported, not complete" + NL +
                "    EGR/VVT system                 supported, not complete" + NL +
                "    Evaporative system         not supported,     complete" + NL +
                "    Exhaust Gas Sensor             supported, not complete" + NL +
                "    Exhaust Gas Sensor heater      supported, not complete" + NL +
                "    Fuel System                    supported, not complete" + NL +
                "    Heated catalyst            not supported,     complete" + NL +
                "    Misfire                        supported, not complete" + NL +
                "    NMHC converting catalyst       supported, not complete" + NL +
                "    NOx catalyst/adsorber          supported, not complete" + NL +
                "    Secondary air system       not supported,     complete" + NL;
        assertEquals(expectedVehicleComposite + NL, listener.getResults());
    }
}
