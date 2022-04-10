/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
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
 * The unit test for {@link Part01Step18Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step18ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM12MILOnEmissionDTCPacket.PGN;
    private static final int STEP_NUMBER = 18;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step18Controller instance;

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
        dataRepository = DataRepository.newInstance();

        DateTimeModule.setInstance(null);

        instance = new Part01Step18Controller(
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

    /**
     * Test method for
     * {@link Part01Step18Controller#getDisplayName()}
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step18Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step18Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test one module responding:<br>
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
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no
     * response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no
     * response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x33<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testNackNotReceivedFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        dataRepository.putObdModule(new OBDModuleInformation(0x33));
        DM12MILOnEmissionDTCPacket dm12Packet33 = DM12MILOnEmissionDTCPacket.create(0x33,
                                                                                    OFF,
                                                                                    OFF,
                                                                                    OFF,
                                                                                    OFF);

        when(communicationsModule.requestDM12(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(dm12Packet33),
                                                                                                             List.of()));
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   Optional.empty()));
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x33)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   dm12Packet33));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0x01));
        verify(communicationsModule).requestDM12(any(), eq(0x33));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test two modules responding:<br>
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
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response (differing)<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.4.a", description = "Fail if any difference compared to data received during global request") })
    public void testDataDiffersFailure() {
        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(PGN,
                                                                                          0x01,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        DM12MILOnEmissionDTCPacket packet3 = DM12MILOnEmissionDTCPacket.create(0x03, OFF, ON, OFF, OFF);

        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        DM12MILOnEmissionDTCPacket obdPacket3 = DM12MILOnEmissionDTCPacket.create(0x03, OFF, OFF, OFF, OFF);

        when(communicationsModule.requestDM12(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(packet1,
                                                                                                                     packet3),
                                                                                                             List.of()));

        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   obdPacket3));

        runTest();

        verify(communicationsModule).requestDM12(any(ResultsListener.class));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.4.a - Difference compared to data received during global request from Transmission #1 (3)");
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test two modules responding:<br>
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
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: 609<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: 609<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response (differing)<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.2.a", description = "Fail if any ECU reports active DTCs") })
    public void testEcuReportsActiveDtcFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(609, 19, 0, 0);
        DM12MILOnEmissionDTCPacket packet1 = DM12MILOnEmissionDTCPacket.create(0x01, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));
        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                                                                            Packet.create(PGN,
                                                                                          0x03,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet3));

        when(communicationsModule.requestDM12(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(packet1,
                                                                                                                     packet3),
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM12(any(ResultsListener.class));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.a - Engine #2 (1) reported active DTCs");
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test two modules responding:<br>
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
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: on</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM12
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: on</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.2.b", description = "Fail if any ECU does not report MIL off") })
    public void testMilNotOffFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM12MILOnEmissionDTCPacket packet1 = DM12MILOnEmissionDTCPacket.create(0x01, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM12MILOnEmissionDTCPacket packet3 = DM12MILOnEmissionDTCPacket.create(0x03, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet3));

        when(communicationsModule.requestDM12(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(packet1,
                                                                                                                     packet3),
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0x01));
        verify(communicationsModule).requestDM12(any(), eq(0x03));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.b - Transmission #1 (3) did not report MIL off");
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test no modules responding:<br>
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">NACK
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.2.c", description = "Fail if no OBD ECU provides DM12") })
    public void testNoObdProvidesDm12Failure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        AcknowledgmentPacket ackPacket1 = AcknowledgmentPacket.create(0x01, NACK);
        when(communicationsModule.requestDM12(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   ackPacket1));

        when(communicationsModule.requestDM12(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(),
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM12(any(ResultsListener.class));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x01));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.18.2.c - No OBD ECU provided DM12");
    }

    /**
     * Test method for {@link Part01Step18Controller#run()}.
     * Test one module responding:<br>
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
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM12
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x33<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">NACK
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.18.1.a", description = "Global DM12 [(send Request (PG 59904) for PG 65236 (SPs 1213-1215, 1706, and 3038))]"),
            @TestItem(verifies = "6.1.18.3.a", description = "DS DM12 to all OBD ECUs") })
    public void testNoErrors() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(Packet.create(PGN,
                                                                                          0x01,
                                                                                          0x00,
                                                                                          0xFF,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00,
                                                                                          0x00));
        when(communicationsModule.requestDM12(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x33));
        AcknowledgmentPacket ackPacket33 = AcknowledgmentPacket.create(0x33, NACK);
        when(communicationsModule.requestDM12(any(ResultsListener.class),
                                              eq(0x33))).thenReturn(new BusResult<>(false, ackPacket33));

        when(communicationsModule.requestDM12(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM12(any(ResultsListener.class));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM12(any(ResultsListener.class), eq(0x33));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
