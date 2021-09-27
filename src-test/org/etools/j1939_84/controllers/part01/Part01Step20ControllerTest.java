/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.DM28PermanentEmissionDTCPacket.PGN;
import static net.solidDesign.j1939.packets.LampStatus.OFF;
import static net.solidDesign.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM28PermanentEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step19Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step20ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 20;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private StepController instance;

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

        instance = new Part01Step20Controller(
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
     * {@link Part01Step20Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step20Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step20Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: N/A</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testNackNotReceivedFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM28(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false));

        dataRepository.putObdModule(new OBDModuleInformation(0x21));
        var packet21 = DM28PermanentEmissionDTCPacket.create(0x21, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any(ResultsListener.class),
                                              eq(0x21))).thenReturn(new BusResult<>(false, packet21));

        when(communicationsModule.requestDM28(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          packet21));

        runTest();

        verify(communicationsModule).requestDM28(any(ResultsListener.class));
        verify(communicationsModule).requestDM28(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM28(any(ResultsListener.class), eq(0x21));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.20.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: N/A</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x17<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response (differing)<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.4.a", description = "Fail if any difference compared to data received during global request.") })
    public void testDataDifferenceFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM28PermanentEmissionDTCPacket packet1 = new DM28PermanentEmissionDTCPacket(
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
        when(communicationsModule.requestDM28(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        DM28PermanentEmissionDTCPacket packet17 = DM28PermanentEmissionDTCPacket.create(
                                                                                        0x17,
                                                                                        OFF,
                                                                                        OFF,
                                                                                        OFF,
                                                                                        OFF);
        when(communicationsModule.requestDM28(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packet1,
                                                                                                             packet17));

        DM28PermanentEmissionDTCPacket obdPacket17 = DM28PermanentEmissionDTCPacket.create(
                                                                                           0x17,
                                                                                           OFF,
                                                                                           OFF,
                                                                                           ON,
                                                                                           OFF);
        when(communicationsModule.requestDM28(any(), eq(0x17)))
                                                                  .thenReturn(new BusResult<>(false, obdPacket17));

        runTest();

        verify(communicationsModule).requestDM28(any(ResultsListener.class));
        verify(communicationsModule).requestDM28(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM28(any(ResultsListener.class), eq(0x17));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.20.4.a - Difference compared to data received during global request from Instrument Cluster #1 (23)");
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: N/A</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: 609<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: 609<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.2.a", description = "Fail if any ECU reports a permanent DTC") })
    public void testEcuReportPermanentDtcFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var ackPacket1 = AcknowledgmentPacket.create(0x01, NACK);
        when(communicationsModule.requestDM28(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false, ackPacket1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        var dtc3 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var packet3 = DM28PermanentEmissionDTCPacket.create(
                                                            0x03,
                                                            OFF,
                                                            OFF,
                                                            OFF,
                                                            OFF,
                                                            dtc3);

        when(communicationsModule.requestDM28(any()))
                                                        .thenReturn(new RequestResult<>(false,
                                                                                        List.of(packet3),
                                                                                        List.of(ackPacket1)));
        when(communicationsModule.requestDM28(any(), eq(0x03)))
                                                                  .thenReturn(new BusResult<>(false, packet3));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0x01));
        verify(communicationsModule).requestDM28(any(), eq(0x03));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.20.2.a - Transmission #1 (3) reported permanent DTCs");
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: on</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK response<br>
     * DTC SPs: N/A<br>
     * MIL Status: on</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.2.b", description = "Fail if any ECU does not report MIL off") })
    public void testEcuDoesNotReportMilOffFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var packet1 = DM28PermanentEmissionDTCPacket.create(0x01, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        var packet3 = DM28PermanentEmissionDTCPacket.create(
                                                            0x03,
                                                            OFF,
                                                            OFF,
                                                            OFF,
                                                            OFF);
        when(communicationsModule.requestDM28(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet3));

        when(communicationsModule.requestDM28(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(packet1,
                                                                                                                     packet3),
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0x01));
        verify(communicationsModule).requestDM28(any(), eq(0x03));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.20.2.b - Engine #2 (1) did not report MIL off");
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: on</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK response<br>
     * DTC SPs: N/A<br>
     * MIL Status: on</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.2.c", description = "Fail if no OBD ECU provides DM28") })
    public void testNoObdProvidesDm28Failure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var ackPacket1 = AcknowledgmentPacket.create(0x01, NACK);
        when(communicationsModule.requestDM28(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false, ackPacket1));

        when(communicationsModule.requestDM28(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of(),
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM28(any(ResultsListener.class));
        verify(communicationsModule).requestDM28(any(ResultsListener.class), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.20.2.c - No OBD ECU provided DM28");
    }

    /**
     * Test method for {@link Part01Step20Controller#run()}.
     * Test two modules responding:<br>
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
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response<br>
     * DTC SPs: N/A<br>
     * MIl Status: N/A</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">NACK response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x21<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM28
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.20.1.a", description = "Global DM28 [(send Request (PG 59904) for PG 64896 (SPs 1213-1215, 3038, 1706))]"),
            @TestItem(verifies = "6.1.20.3.a", description = "DS DM28 to each OBD ECU") })
    public void testNoErrors() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var ackPacket1 = AcknowledgmentPacket.create(0x01,
                                                     NACK);
        when(communicationsModule.requestDM28(any(), eq(0x01)))
                                                                  .thenReturn(new BusResult<>(false, ackPacket1));

        dataRepository.putObdModule(new OBDModuleInformation(0x21));
        var packet21 = DM28PermanentEmissionDTCPacket.create(0x21, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any(), eq(0x21)))
                                                                  .thenReturn(new BusResult<>(false, packet21));
        when(communicationsModule.requestDM28(any()))
                                                        .thenReturn(new RequestResult<>(false, packet21));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0x01));
        verify(communicationsModule).requestDM28(any(), eq(0x21));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
