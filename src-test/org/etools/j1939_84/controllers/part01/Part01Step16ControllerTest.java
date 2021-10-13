/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.bus.RequestResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM2PreviouslyActiveDTC;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

/**
 * @author Garrison Garland (garrison@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step16ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step16Controller instance;

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

        instance = new Part01Step16Controller(executor,
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
                                 mockListener,
                                 communicationsModule);
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: 123<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2 response<br>
     * DTC SPs 123<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a", description = "Fail if any OBD ECU reports a previously active DTC"))
    public void testObdModuleReportsPrevActiveDtcFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        var dtc1 = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var packet1 = DM2PreviouslyActiveDTC.create(0x00, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU Engine #1 (0) reported a previously active DTC");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step16Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 16", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step16Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 16, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step16Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.b", description = "Fail if any OBD ECU does not report MIL off") })
    public void testMILNotSupported() {
        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        var packet1 = DM2PreviouslyActiveDTC.create(0x00, NOT_SUPPORTED, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU Engine #1 (0) did not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({
            @TestItem(verifies = "6.1.16.1.a", description = "Global DM2 [(send Request (PG 59904) for PG 65227 (SPs 1213-1215, 3038, 1706))]"),
            @TestItem(verifies = "6.1.16.3.a", description = "DS DM2 to each OBD ECU") })
    public void testMILOff() {
        var packet1 = DM2PreviouslyActiveDTC.create(0x00, OFF, OFF, OFF, OFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: 12<br>
     * MIL Status: slow flash</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM2 response<br>
     * DTC SPs 12<br>
     * MIL Status: slow flash</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.b", description = "Fail if any OBD ECU does not report MIL off") })
    public void testObdModuleReportsMILStatusNotOffFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        var dtc = DiagnosticTroubleCode.create(12, 1, 1, 1);
        var packet1 = DM2PreviouslyActiveDTC.create(0x00, SLOW_FLASH, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU Engine #1 (0) reported a previously active DTC");
        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU Engine #1 (0) did not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 4px;word-wrap=break-word">DS Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: on</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({
            @TestItem(verifies = "6.1.16.2.c", description = "Fail if any non-OBD ECU does not report MIL off or not supported") })
    public void testNonObdModuleMilOnFailure() {
        var packet1 = DM2PreviouslyActiveDTC.create(0x00, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.c - Non-OBD ECU Engine #1 (0) did not report MIL off or not supported");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({
            @TestItem(verifies = "6.1.16.4.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query") })
    public void testObdNackNotRecievedFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        var packet1 = DM2PreviouslyActiveDTC.create(0x00, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(BusResult.of(packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        DM2PreviouslyActiveDTC packet4 = DM2PreviouslyActiveDTC.create(0x03, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x03))).thenReturn(BusResult.of(packet4));

        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0x00));
        verify(communicationsModule).requestDM2(any(), eq(0x03));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.b - OBD ECU Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM2 response (differing)<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">ACK response<br>
     * DTC SPs N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a", description = "Fail if any responses differ from global responses") })
    public void testResponsesAreDifferentFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        DM2PreviouslyActiveDTC packet2 = DM2PreviouslyActiveDTC.create(0x00, OFF, ON, OFF, OFF);
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x00))).thenReturn(BusResult.of(packet2));

        DM2PreviouslyActiveDTC packet1 = DM2PreviouslyActiveDTC.create(0x00, OFF, OFF, OFF, OFF);

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        AcknowledgmentPacket packet4 = AcknowledgmentPacket.create(0x03, NACK);
        when(communicationsModule.requestDM2(any(), eq(0x03))).thenReturn(BusResult.of(packet4));

        DM2PreviouslyActiveDTC packet3 = DM2PreviouslyActiveDTC.create(0x03, OFF, OFF, OFF, OFF);

        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1,
                                                                                                      packet3));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x03));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.a - Difference compared to data received during global request from Engine #1 (0)");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step16Controller#run()}.
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
     * 3px;word-wrap:break-word">good DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: off</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM2 response<br>
     * DTC SPs N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM2
     * response<br>
     * DTC SPs: N/A<br>
     * MIL Status: N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">NACK response<br>
     * DTC SPs N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @TestDoc({
            @TestItem(verifies = "6.1.16.1.a", description = "Global DM2 [(send Request (PG 59904) for PG 65227 (SPs 1213-1215, 3038, 1706))]"),
            @TestItem(verifies = "6.1.16.3.a", description = "DS DM2 to each OBD ECU") })
    @Test
    public void testTwoObdModulesOneWithResponseOneWithNack2() {

        dataRepository.putObdModule(new OBDModuleInformation(0x00));
        DM2PreviouslyActiveDTC packet1 = DM2PreviouslyActiveDTC.create(0x00, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(), eq(0x00))).thenReturn(BusResult.of(packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        AcknowledgmentPacket packet4 = AcknowledgmentPacket.create(0x03, NACK);
        when(communicationsModule.requestDM2(any(ResultsListener.class),
                                             eq(0x03))).thenReturn(BusResult.of(packet4));

        when(communicationsModule.requestDM2(any(ResultsListener.class))).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(communicationsModule).requestDM2(any(ResultsListener.class));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM2(any(ResultsListener.class), eq(0x03));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
