/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.DM12MILOnEmissionDTCPacket.PGN;
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
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
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
public class Part01Step19ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 19;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step19Controller instance;

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

        instance = new Part01Step19Controller(
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
     * {@link Part01Step19Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step19Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step19Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x58<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
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
            @TestItem(verifies = "6.1.19.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query.") })
    public void testNackNotReceivedFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM23(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new BusResult<>(false, Optional.empty()));

        dataRepository.putObdModule(new OBDModuleInformation(0x58));
        var packet58 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x58, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(any(ResultsListener.class),
                                              eq(0x58))).thenReturn(new BusResult<>(false, packet58));

        when(communicationsModule.requestDM23(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          packet58));

        runTest();

        verify(communicationsModule).requestDM23(any(ResultsListener.class));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x58));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.19.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
     * Test two module responding:<br>
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
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
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
            @TestItem(verifies = "6.1.19.4.a", description = "Fail if any difference compared to data received during global request") })
    public void testDataDifferenceFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM23PreviouslyMILOnEmissionDTCPacket packet1 = new DM23PreviouslyMILOnEmissionDTCPacket(
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
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM23PreviouslyMILOnEmissionDTCPacket packet3 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x03,
                                                                                                   OFF,
                                                                                                   ON,
                                                                                                   OFF,
                                                                                                   OFF);
        when(communicationsModule.requestDM23(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packet1,
                                                                                                             packet3));

        DM23PreviouslyMILOnEmissionDTCPacket obdPacket3 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x03,
                                                                                                      OFF,
                                                                                                      OFF,
                                                                                                      OFF,
                                                                                                      OFF);
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   obdPacket3));

        runTest();

        verify(communicationsModule).requestDM23(any(ResultsListener.class));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.19.4.a - Difference compared to data received during global request from Transmission #1 (3)");
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
     * Test two module responding:<br>
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
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: 609, 4334, 1569<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: 609, 4334, 1569<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
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
            @TestItem(verifies = "6.1.19.2.a", description = "Fail if any ECU reports previously active DTCs") })
    public void testEcuReportsActiveDtcFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var dtc1 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        DM23PreviouslyMILOnEmissionDTCPacket packet1 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x01,
                                                                                                   OFF,
                                                                                                   OFF,
                                                                                                   OFF,
                                                                                                   OFF,
                                                                                                   dtc1,
                                                                                                   dtc2,
                                                                                                   dtc3);
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM23PreviouslyMILOnEmissionDTCPacket packet3 = new DM23PreviouslyMILOnEmissionDTCPacket(Packet.create(PGN,
                                                                                                              0x03,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        when(communicationsModule.requestDM23(any(ResultsListener.class),
                                              eq(0x03))).thenReturn(new BusResult<>(false,
                                                                                       packet3));

        when(communicationsModule.requestDM23(any(ResultsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                          packet1,
                                                                                                          packet3));

        runTest();

        verify(communicationsModule).requestDM23(any(ResultsListener.class));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.19.2.a - Engine #2 (1) reported previously active DTCs");
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
     * Test two module responding:<br>
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
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: on</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: on</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
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
            @TestItem(verifies = "6.1.19.2.b", description = "Fail if any ECU does not report MIL off. See Section A.8 for allowed values.") })
    public void testMilNotOffFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DM23PreviouslyMILOnEmissionDTCPacket packet1 = DM23PreviouslyMILOnEmissionDTCPacket.create(
                                                                                                   0x01,
                                                                                                   ON,
                                                                                                   OFF,
                                                                                                   OFF,
                                                                                                   OFF);
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM23PreviouslyMILOnEmissionDTCPacket packet3 = new DM23PreviouslyMILOnEmissionDTCPacket(
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
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x03)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet3));

        when(communicationsModule.requestDM23(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packet1,
                                                                                                             packet3));

        runTest();

        verify(communicationsModule).requestDM23(any(ResultsListener.class));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x01));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.19.2.b - Engine #2 (1) did not report MIL off");
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
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
            @TestItem(verifies = "6.1.19.2.c", description = "Fail if no OBD ECU provides DM23") })
    public void testNoObdProvidesDm23Failure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        AcknowledgmentPacket packet1 = AcknowledgmentPacket.create(0x01, NACK);

        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet1));

        when(communicationsModule.requestDM23(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             List.of()));

        runTest();

        verify(communicationsModule).requestDM23(any(ResultsListener.class));
        verify(communicationsModule).requestDM23(any(ResultsListener.class), eq(0x01));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.19.2.c - No OBD ECU provided DM23");
    }

    /**
     * Test method for {@link Part01Step19Controller#run()}.
     * Test two module responding:<br>
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
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">NACK
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x33<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
     * response<br>
     * DCT SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM23
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
            @TestItem(verifies = "6.1.19.1.a", description = "Global DM23 [(send Request (PGNPG 59904) for PGNPG 64949 (SPNSPs 1213-1215, 3038, 1706))]"),
            @TestItem(verifies = "6.1.19.3.a", description = "Fail if no OBD ECU provides DM23") })
    public void testNoErrors() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        AcknowledgmentPacket ackPacket1 = AcknowledgmentPacket.create(0x01, NACK);
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x01)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   ackPacket1));

        dataRepository.putObdModule(new OBDModuleInformation(0x33));
        DM23PreviouslyMILOnEmissionDTCPacket packet33 = new DM23PreviouslyMILOnEmissionDTCPacket(Packet.create(PGN,
                                                                                                               0x33,
                                                                                                              0x00,
                                                                                                              0xFF,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00,
                                                                                                              0x00));
        when(communicationsModule.requestDM23(any(ResultsListener.class), eq(0x33)))
                                                                                       .thenReturn(new BusResult<>(false,
                                                                                                                   packet33));

        when(communicationsModule.requestDM23(any(ResultsListener.class)))
                                                                             .thenReturn(new RequestResult<>(false,
                                                                                                             packet33));

        runTest();

        verify(communicationsModule).requestDM23(any());
        verify(communicationsModule).requestDM23(any(), eq(0x01));
        verify(communicationsModule).requestDM23(any(), eq(0x33));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
