/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket.PGN;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
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
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
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
 * The unit test for {@link Part01Step17Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step17ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 17;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step17Controller instance;

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

        instance = new Part01Step17Controller(
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
     * Test method for {@link Part01Step17Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6 response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM6 response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query") })
    public void testEmptyPacketFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM6PendingEmissionDTCPacket packet = DM6PendingEmissionDTCPacket.create(0x00,
                                                                                OFF,
                                                                                OFF,
                                                                                OFF,
                                                                                OFF);

        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x00)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(packet),
                                                                                                                      List.of()));

        when(communicationsModule.requestDM6(any(ResultsListener.class)))
                                                                            .thenReturn(new RequestResult<>(false,
                                                                                                            List.of(packet),
                                                                                                            List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x01)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(),
                                                                                                                      List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any(ResultsListener.class));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x01));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.17.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    /**
     * Test method for {@link Part01Step17Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6 response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6 response
     * (differing)<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.4.a", description = "Fail if any difference compared to data received during global request") })
    public void testDataDifferenceFailure() {
        DM6PendingEmissionDTCPacket packet1 = DM6PendingEmissionDTCPacket.create(0x01,
                                                                                 OFF,
                                                                                 OFF,
                                                                                 OFF,
                                                                                 OFF);
        DM6PendingEmissionDTCPacket packet3 = DM6PendingEmissionDTCPacket.create(0x03,
                                                                                 OFF,
                                                                                 OFF,
                                                                                 ON,
                                                                                 OFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        DM6PendingEmissionDTCPacket obdPacket3 = DM6PendingEmissionDTCPacket.create(0x03, OFF, OFF, OFF, OFF);

        when(communicationsModule.requestDM6(any()))
                                                       .thenReturn(new RequestResult<>(false,
                                                                                       List.of(packet1, packet3),
                                                                                       List.of()));
        when(communicationsModule.requestDM6(any(), eq(0x01)))
                                                                 .thenReturn(new RequestResult<>(false,
                                                                                                 List.of(packet1),
                                                                                                 List.of()));
        when(communicationsModule.requestDM6(any(), eq(0x03)))
                                                                 .thenReturn(new RequestResult<>(false,
                                                                                                 List.of(obdPacket3),
                                                                                                 List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any(ResultsListener.class));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM6(any(), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.17.4.a - Difference compared to data received during global request from Transmission #1 (3)");
    }

    /**
     * Test method for {@link Part01Step17Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: 4334<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6 response<br>
     * DTC SPs: 4334<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6 response
     * (differing)<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.2.a", description = "Fail if any ECU reports pending DTCs") })
    public void testModuleReportsPendingDtcsFailure() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        DM6PendingEmissionDTCPacket packet0 = DM6PendingEmissionDTCPacket.create(0x00, OFF, OFF, OFF, OFF, dtc);
        DM6PendingEmissionDTCPacket packet3 = DM6PendingEmissionDTCPacket.create(0x03, OFF, OFF, OFF, OFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        when(communicationsModule.requestDM6(any(ResultsListener.class)))
                                                                            .thenReturn(new RequestResult<>(false,
                                                                                                            List.of(packet0,
                                                                                                                    packet3),
                                                                                                            List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x00)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(packet0),
                                                                                                                      List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x03)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(packet3),
                                                                                                                      List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.17.2.a - Engine #1 (0) reported pending DTCs");
    }

    /**
     * Test method for {@link Part01Step17Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM6 response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: on</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6 response
     * (differing)<br>
     * DTC SPs: N/A<br>
     * MIL Status: on</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.2.b", description = "Fail if any ECU does not report MIL off") })
    public void testMilNotOffFailure() {
        DM6PendingEmissionDTCPacket packet1 = DM6PendingEmissionDTCPacket.create(0x01, OFF, OFF, OFF, OFF);
        DM6PendingEmissionDTCPacket packet3 = DM6PendingEmissionDTCPacket.create(0x03, ON, OFF, OFF, OFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        when(communicationsModule.requestDM6(any(ResultsListener.class)))
                                                                            .thenReturn(new RequestResult<>(false,
                                                                                                            List.of(packet1,
                                                                                                                    packet3),
                                                                                                            List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x01)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(packet1),
                                                                                                                      List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x03)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(packet3),
                                                                                                                      List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(0x01));
        verify(communicationsModule).requestDM6(any(), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.17.2.b - Transmission #1 (3) did not report MIL off");
    }

    /**
     * Test method for {@link Part01Step17Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">NACK response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.2.c", description = "Fail if no OBD ECU provides DM6") })
    public void testNoObdProvidesDm6Failure() {
        AcknowledgmentPacket ackPacket1 = AcknowledgmentPacket.create(0x01, NACK);

        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(communicationsModule.requestDM6(any(ResultsListener.class)))
                                                                            .thenReturn(new RequestResult<>(false,
                                                                                                            List.of(),
                                                                                                            List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x01)))
                                                                                      .thenReturn(new RequestResult<>(false,
                                                                                                                      List.of(),
                                                                                                                      List.of(ackPacket1)));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any(ResultsListener.class));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x01));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.17.2.c - No OBD ECU provided DM6");
    }

    /**
     * Test method for
     * {@link Part01Step17Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step17Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step17Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step17Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM6 response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.17.1.a", description = "Global DM6 [(send Request (PG 59904) for PG 65227 (SPs 1213-1215, 3038, 1706))]"),
            @TestItem(verifies = "6.1.17.3.a", description = "DS DM6 to each OBD ECU") })
    public void testNoErrors() {

        DM6PendingEmissionDTCPacket packet1 = new DM6PendingEmissionDTCPacket(
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

        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(communicationsModule.requestDM6(any(ResultsListener.class)))
                                                       .thenReturn(new RequestResult<>(false,
                                                                                       List.of(packet1),
                                                                                       List.of()));
        when(communicationsModule.requestDM6(any(ResultsListener.class), eq(0x01)))
                                                                 .thenReturn(new RequestResult<>(false,
                                                                                                 List.of(packet1),
                                                                                                 List.of()));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM6(any(ResultsListener.class));
        verify(communicationsModule).requestDM6(any(ResultsListener.class), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
