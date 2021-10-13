/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.etools.j1939_84.modules.TestDateTimeModule;
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

import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;

/**
 * The unit test for {@link Part01Step15Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(description = "Test 1.15 - DM1: Active DTCs")
public class Part01Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 15;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step15Controller instance;

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
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part01Step15Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              new TestDateTimeModule());

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
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs:
     * N/A<br>
     * MIL Status: N/A</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.2.e", description = "Fail if no OBD ECU provides DM1") })
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of());

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.15.2.e - No OBD ECU provided a DM1");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs:
     * N/A<br>
     * MIL Status: alt off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.3.a", description = "Warn if any ECU reports the non-preferred MIL off format. See Section A.8 for description of (0b00b, 0b00b).") })
    public void testObdAlternateOffWarning() {
        DM1ActiveDTCsPacket packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                                 ALTERNATE_OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs: 1569,
     * 609, 4334<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.2.a", description = "Fail if any OBD ECU reports an active DTC") })
    public void testObdActiveDtcFailure() {
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(4334, 4, 0, 0);

        DM1ActiveDTCsPacket packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 dtc1,
                                                                 dtc2,
                                                                 dtc3);
        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Engine #2 (1) reported an active DTC");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">Active DTC SPs: 1569,
     * 609, 4334<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x17<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs: 1569,
     * 609, 4334<br>
     * MIL Status: on</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.2.c", description = "Fail if any non-OBD ECU does not report MIL off or not supported MIL status (per SAE J1939-73 Table 5)") })
    public void testNonObdMilOnFailure() {
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(4334, 4, 0, 0);
        DM1ActiveDTCsPacket packet2 = DM1ActiveDTCsPacket.create(0x17,
                                                                 ON,
                                                                 ALTERNATE_OFF,
                                                                 SLOW_FLASH,
                                                                 FAST_FLASH,
                                                                 dtc1,
                                                                 dtc2,
                                                                 dtc3);

        var packet3 = DM1ActiveDTCsPacket.create(0x03,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 OFF);
        // make the module and OBD
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet2, packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.c - Non-OBD ECU Instrument Cluster #1 (23) did not report MIL off or not supported");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">Active DTC SPs: N/A<br>
     * MIL Status: off</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x17<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs: 1569,
     * 609, 4334<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.3.b", description = "Warn if any non-OBD ECU reports SP conversion method (SP 1706) equal to 1") })
    public void testNonObdConversionMethodWarning() {
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(4334, 4, 1, 0);
        DM1ActiveDTCsPacket packet2 = DM1ActiveDTCsPacket.create(0x17,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 dtc1,
                                                                 dtc2,
                                                                 dtc3);

        var packet3 = DM1ActiveDTCsPacket.create(0x03,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 OFF);
        // make the module and OBD
        dataRepository.putObdModule(new OBDModuleInformation(0x03));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet2, packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.15.3.b - Non-OBD ECU Instrument Cluster #1 (23) reported SP conversion method (SP 1706) equal to 1");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x03<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs:
     * 123<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.2.d", description = "Fail if any OBD ECU reports SP conversion method (SP 1706) equal to binary 1") })
    public void testObdSpConversionFailure() {
        // a CM value of 1 will cause the conversion method failure
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 5);
        var packet3 = DM1ActiveDTCsPacket.create(0x03,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 dtc);
        // make the module and OBD
        dataRepository.putObdModule(new OBDModuleInformation(0x03));
        // return the OBD module's packet when requested
        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Transmission #1 (3) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Transmission #1 (3) reported SP conversion method (SP 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x17<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs:
     * N/A<br>
     * MIL Status: off</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.1.a", description = "Gather broadcast DM1 data from all ECUs [PG 65226 (SPs 1213-1215, 1706, and 3038)]") })
    public void testGatherBroadcastDm1() {
        DM1ActiveDTCsPacket packet2 = DM1ActiveDTCsPacket.create(0x17,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF);

        dataRepository.putObdModule(new OBDModuleInformation(0x17));

        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet2));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step11Controller#run()}.
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
     * 4px;word-wrap=break-word">Message Details</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad DM1 response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">Active DTC SPs:
     * N/A<br>
     * MIL Status: on</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.15.2.b", description = "Fail if any OBD ECU does not report MIL off - see Section A.8 for allowed values") })
    public void testObdMilOnFailure() {
        var packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                 ON,
                                                 OFF,
                                                 OFF,
                                                 OFF);
        // make it an OBD module
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        // return the packet with the active dtc and a MIL on for the OBD module
        when(communicationsModule.readDM1(any(ResultsListener.class))).thenReturn(List.of(packet1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).readDM1(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.b - OBD ECU Engine #2 (1) did not report MIL 'off'");

        assertEquals("", listener.getResults());
    }
}
