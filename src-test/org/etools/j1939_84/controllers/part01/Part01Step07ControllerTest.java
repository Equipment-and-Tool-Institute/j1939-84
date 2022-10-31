/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
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
 * The unit test for {@link Part01Step07Controller}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 7", description = "DM19: Calibration information"))
public class Part01Step07ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 7;
    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;
    @Mock
    private EngineSpeedModule engineSpeedModule;
    @Mock
    private Executor executor;
    private Part01Step07Controller instance;
    @Mock
    private J1939 j1939;
    private TestResultsListener listener;
    @Mock
    private ResultsListener mockListener;
    @Mock
    private ReportFileModule reportFileModule;
    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private static DM19CalibrationInformationPacket createDM19(int sourceAddress, String calId, String cvn, int count) {
        CalibrationInformation[] calInfos = new CalibrationInformation[count];
        for (int i = 0; i < count; i++) {
            calInfos[i] = new CalibrationInformation(calId,
                                                     cvn,
                                                     calId.getBytes(StandardCharsets.UTF_8),
                                                     cvn.getBytes(StandardCharsets.UTF_8));
        }
        return DM19CalibrationInformationPacket.create(sourceAddress, 0, calInfos);
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();
        instance = new Part01Step07Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
                                              communicationsModule);

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
     * Test method for
     * {@link Part01Step07Controller#getDisplayName()}
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step07Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7", description = "DM19: Calibration Information"),
            @TestItem(verifies = "6.1.7.1.a", description = "Create list of ECU address + CAL ID + CVN. ([An ECU address may report more than one CAL ID and CVN])"),
            @TestItem(verifies = "6.1.7.1.b", description = "Display this list in the log. ([NOTE: Display the CVNs using big endian format and not little endian format as given in the response.])"),
            @TestItem(verifies = "6.1.7.4.a", description = "Destination Specific (DS) DM19 to each OBD ECU (plus all ECUs that responded to global DM19)") })
    public void testRunHappyPath() {
        DM19CalibrationInformationPacket dm19 = DM19CalibrationInformationPacket.create(0x00,
                                                                                        0xF9,
                                                                                        new CalibrationInformation("SixteenCharacters",
                                                                                                                   "1234"));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm19));

        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x00)))
                                                                                    .thenReturn(BusResult.of(
                                                                                                             dm19));

        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm19.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(ResultsListener.class), eq(0x00));

    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testRealDataFromTruck() {

        DM19CalibrationInformationPacket dm19 = new DM19CalibrationInformationPacket(Packet.create(DM19CalibrationInformationPacket.PGN,
                                                                                                   0x00,
                                                                                                   // LSB CVN; 4 bytes
                                                                                                   // (checksum value of
                                                                                                   // entire
                                                                                                   // calibration)
                                                                                                   // padding at MSB w/
                                                                                                   // 0x00
                                                                                                   0x06, // ASCII value
                                                                                                         // of ACK -
                                                                                                         // unprintable
                                                                                                         // char
                                                                                                   0x7A, // z
                                                                                                   0x6E, // n
                                                                                                   0xC9, // É -
                                                                                                         // unprintable
                                                                                                         // char
                                                                                                   // LSB Cal ID; 16
                                                                                                   // bytes; Padding at
                                                                                                   // LSB w/ 0x00
                                                                                                   0x41, // A
                                                                                                   0x32, // 2
                                                                                                   0x36, // 36
                                                                                                   0x31, // 1
                                                                                                   0x58, // X
                                                                                                   0x58, // X
                                                                                                   0x4D,  // M
                                                                                                   0x5F,  // -
                                                                                                   0x45, // E
                                                                                                   0x37,  // 7
                                                                                                   0x31, // 1
                                                                                                   0x31, // 1
                                                                                                   0x45, // E
                                                                                                   0x33, // 3
                                                                                                   0x31, // 1
                                                                                                   0x44, // D
                                                                                                   // LSB; 4 bytes
                                                                                                   // (checksum value of
                                                                                                   // entire
                                                                                                   // calibration)
                                                                                                   0xA8, // -
                                                                                                         // unprintable
                                                                                                   0x73, // 5
                                                                                                   0x89, // undefined
                                                                                                   0x13, // DC3 Ascii
                                                                                                         // char
                                                                                                         // undefined?
                                                                                                   // LSB Cal ID; 16
                                                                                                   // bytes; Padding at
                                                                                                   // LSB
                                                                                                   0x4E, // N
                                                                                                   0x4F, // O
                                                                                                   0x78, // x
                                                                                                   0x2D, // -
                                                                                                   0x53, // S
                                                                                                   0x41, // A
                                                                                                   0x45, // E
                                                                                                   0x31, // 1
                                                                                                   0x34, // 4
                                                                                                   0x61, // a
                                                                                                   0x20, // " " - space
                                                                                                   0x41, // A
                                                                                                   0x54, // T
                                                                                                   0x49, // I
                                                                                                   0x31, // 1
                                                                                                   0x00, // NUL
                                                                                                   // LSB; 4 bytes
                                                                                                   // (checksum value of
                                                                                                   // entire
                                                                                                   // calibration)
                                                                                                   0x8C, // ¼ -
                                                                                                         // unprintable
                                                                                                   0x4B, // K
                                                                                                   0xF9, // ù -
                                                                                                         // unprintable
                                                                                                   0xC9, // É -
                                                                                                         // unprintable
                                                                                                   // LSB Cal ID; 16
                                                                                                   // bytes; Padding at
                                                                                                   // LSB
                                                                                                   0x4E, // N
                                                                                                   0x4F, // O
                                                                                                   0x78, // x
                                                                                                   0x2D, // -
                                                                                                   0x53, // S
                                                                                                   0x41, // A
                                                                                                   0x45, // E
                                                                                                   0x31, // 1
                                                                                                   0x34, // 4
                                                                                                   0x61, // a
                                                                                                   0x20, // " " - space
                                                                                                   0x41, // A
                                                                                                   0x54, // T
                                                                                                   0x4F, // 0
                                                                                                   0x31, // 1
                                                                                                   0x00, // NUL
                                                                                                   // LSB; 4 bytes
                                                                                                   // (checksum value of
                                                                                                   // entire
                                                                                                   // calibration)
                                                                                                   0x00, // NUL
                                                                                                   0x00, // NUL
                                                                                                   0x00, // NUL
                                                                                                   0x00, // NUL
                                                                                                   // LSB Cal ID; 16
                                                                                                   // bytes; Padding at
                                                                                                   // LSB
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   0xFF, // ÿ
                                                                                                   // LSB; 4 bytes
                                                                                                   // (checksum value of
                                                                                                   // entire
                                                                                                   // calibration)
                                                                                                   0xD2, // Ò -
                                                                                                         // unprintable
                                                                                                   0xBF, // ¿
                                                                                                   0x0F, // undefined
                                                                                                   0xA9, // ©
                                                                                                   // LSB Cal ID; 16
                                                                                                   // bytes; Padding at
                                                                                                   // LSB
                                                                                                   0x50, // P
                                                                                                   0x4D, // M
                                                                                                   0x53, // S
                                                                                                   0x31, // 1
                                                                                                   0x32, // 2
                                                                                                   0x33, // 3
                                                                                                   0x34, // 4
                                                                                                   0x31, // 1
                                                                                                   0x41, // A
                                                                                                   0x31, // 1
                                                                                                   0x30, // 0
                                                                                                   0x31, // 1
                                                                                                   0x00, // NUL
                                                                                                   0x00, // NUL
                                                                                                   0x00, // NUL
                                                                                                   0x00 // NUL
        ));

        when(communicationsModule.requestDM19(any())).thenReturn(RequestResult.of(dm19));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.requestDM19(any(), eq(0)))
                                                            .thenReturn(BusResult.of(
                                                                                     dm19));

        runTest();

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.a - Total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(INFO),
                                        eq("6.1.7.3.b - Engine #1 (0) provided more than one CAL ID and CVN pair in a single DM19 message"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.c - CAL ID ÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿÿ has CVN 0x00000000 which has 00h in either the first or fourth bytes"));
        verify(mockListener, times(1)).addOutcome(eq(PART_NUMBER),
                                                  eq(STEP_NUMBER),
                                                  eq(FAIL),
                                                  eq("6.1.7.2.b.ii - OBD ECU Engine #1 (0) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iv - OBD ECU Received CVN is all 0x00 from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.v - OBD ECU Received CVN with incorrect padding from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iii - OBD ECU Received CAL ID is all 0xFF from Engine #1 (0)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any());
        verify(communicationsModule).requestDM19(any(), eq(0));

    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0E<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id structure</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii") }, description = "Fail if <> 1 CVN for every CAL ID")
    public void testObdModuleLessThan1CvnFailure() {
        DM19CalibrationInformationPacket dm19 = new DM19CalibrationInformationPacket(Packet.create(DM19CalibrationInformationPacket.PGN,
                                                                                                   0x0E,
                                                                                                   0x51, // CVN
                                                                                                   0xBA,
                                                                                                   0xFE,
                                                                                                   0xBD,
                                                                                                   0x20, // Cal Id
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20));

        dataRepository.putObdModule(new OBDModuleInformation(0x0E));

        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm19));
        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x0E)))
                                                                                    .thenReturn(BusResult.of(dm19));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setCalIds(dm19.getCalibrationInformation().size());
        vehicleInformation.setEmissionUnits(1);

        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.b.i - OBD ECU Brakes - Drive Axle #2 (14) <> 1 CVN for every CAL ID");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(ResultsListener.class), eq(0x0E));

    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0E<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id structure</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.ii") }, description = "Warn if <> 1 CVN for every CAL ID")
    public void testNonObdModuleLessThan1CvnFailure() {
        DM19CalibrationInformationPacket dm19 = new DM19CalibrationInformationPacket(Packet.create(DM19CalibrationInformationPacket.PGN,
                                                                                                   0x0E,
                                                                                                   0x51,
                                                                                                   0xBA,
                                                                                                   0xFE,
                                                                                                   0xBD,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20,
                                                                                                   0x20));

        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm19));
        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x0E)))
                                                                                    .thenReturn(BusResult.of(dm19));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm19.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.d.ii - Non-OBD ECU Brakes - Drive Axle #2 (14) <> 1 CVN for every CAL ID"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(ResultsListener.class), eq(0x0E));

    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
     * Test no module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">empty DM19
     * response<br>
     * saved Cal Ids differ</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM19
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.7.2.a", description = "Fail if total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units (test 6.1.2)"))
    public void testAmountOfUserEnteredCalIdDiffersReportedAmountFailure() {
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setCalIds(5);
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.a - Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units");

    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
     * Test no module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM19
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.c", description = "Fail if NACK not received from OBD ECUs that did not respond to global query") })
    public void testNoNacksToGlobalQueryFromObdFailure() {

        DM19CalibrationInformationPacket dm19 = DM19CalibrationInformationPacket.create(0x00,
                                                                                        0xF9,
                                                                                        new CalibrationInformation("CALID",
                                                                                                                   "1234"));

        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.requestDM19(any(CommunicationsListener.class))).thenReturn(RequestResult.of());
        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x00))).thenReturn(BusResult.of(dm19));

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(ResultsListener.class), eq(0x00));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.c - OBD ECU Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
     * Test no module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no DM19 response<br>
     * stored NACK
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id structure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.b", description = "Fail if NACK (PGN 59392) with mode/control byte = 3 (busy) received") })
    public void testObdRespondsWithNackDsQueryFailure() {
        DM19CalibrationInformationPacket dm19 = DM19CalibrationInformationPacket.create(0x00,
                                                                                        0xF9,
                                                                                        new CalibrationInformation("SixteenCharacter",
                                                                                                                   "1234"));
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM19(any())).thenReturn(RequestResult.of(dm19));

        AcknowledgmentPacket nack = AcknowledgmentPacket.create(0x00, NACK);
        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm19.getCalibrationInformation().size());

        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0)))
                                                                                 .thenReturn(BusResult.of(nack));
        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(1)))
                                                                                 .thenReturn(BusResult.of(dm19));

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0));
        verify(communicationsModule).requestDM19(any(), eq(1));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.c - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
     * Test no module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM19
     * response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM19
     * response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">NACK response<br>
     * BUSY</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.b", description = "Fail if NACK (PGN 59392) with mode/control byte = 3 (busy) received") })
    public void testObdRespondHasBusyAtByte3Failure() {

        DM19CalibrationInformationPacket dm19a0 = createDM19(0x00, "SixteenCharacters", "1234", 1);
        DM19CalibrationInformationPacket dm19a1 = createDM19(0x01, "SixteenCharacters", "1234", 1);
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM19(any())).thenReturn(RequestResult.of(dm19a0, dm19a1));

        AcknowledgmentPacket nack = AcknowledgmentPacket.create(0x00, BUSY);
        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm19a0.getCalibrationInformation().size()
                + dm19a1.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x00)))
                                                                                    .thenReturn(BusResult.of(nack));
        when(communicationsModule.requestDM19(any(ResultsListener.class), eq(0x01)))
                                                                                    .thenReturn(BusResult.of(dm19a0));

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0));
        verify(communicationsModule).requestDM19(any(), eq(1));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.b - Engine #1 (0) responded NACK with control byte = 3 (busy)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0B<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * data</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * different data</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.a", description = "Compare to ECU address + CAL ID + CVN list created from global DM19 request and fail if any difference") })
    public void testEcuResponseDiffersFromGlobalDm19Failure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x0B));
        DM19CalibrationInformationPacket dm190B2 = DM19CalibrationInformationPacket.create(0x0B,
                                                                                           0xF9,
                                                                                           new CalibrationInformation("SixteenCharacter",
                                                                                                                      "1234"));
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0B))).thenReturn(BusResult.of(dm190B2));

        DM19CalibrationInformationPacket dm190B = DM19CalibrationInformationPacket.create(0x0B,
                                                                                          0xF9,
                                                                                          new CalibrationInformation("CharacterSixteen",
                                                                                                                     "1234"));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190B));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190B.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0B));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.a - Difference compared to data received during global request from Brakes - System Controller (11)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with CVNs as all zeros to global query;
     * DS query returns same message
     */
    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x1E<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * data</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * different data</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.i", description = "Warn if any non-OBD ECU provides CAL ID"),
            @TestItem(verifies = "6.1.7.3.d.iv", description = "Warn if any received CAL ID is all 0xFF(h) or any CVN is all 0x00(h)") })
    public void testNonObdModuleCvnAllZerosFailure() {
        // Module 1E - CalId all 0x00 and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(DM19CalibrationInformationPacket.PGN,
                                        0x1E,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B);
        DM19CalibrationInformationPacket dm191E = new DM19CalibrationInformationPacket(packet1E);

        when(communicationsModule.requestDM19(any(), eq(0x1E))).thenReturn(BusResult.of(dm191E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm191E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm191E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x1E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Electrical System (30) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.v - Non-OBD ECU Received CVN that is all 0x00 from Electrical System (30)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.vi - Non-OBD ECU Received CVN with incorrect padding from Electrical System (30)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x1E<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad CVN - all 0x00</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN - all 0x00</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.iii", description = "Warn if any non-OBD ECU provides CAL ID") })
    public void testObdModuleCvnAllZerosFailure() {
        // Module 1E - CalId all 0x00 and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(DM19CalibrationInformationPacket.PGN,
                                        0x1E,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B);
        dataRepository.putObdModule(new OBDModuleInformation(0x1E));
        DM19CalibrationInformationPacket dm191E = new DM19CalibrationInformationPacket(packet1E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x1E))).thenReturn(BusResult.of(dm191E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm191E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm191E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x1E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iv - OBD ECU Received CVN is all 0x00 from Electrical System (30)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c - CAL ID QaD;QaD;QaD;QaD; has CVN 0x00000000 which has 00h in either the first or fourth bytes"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.v - OBD ECU Received CVN with incorrect padding from Electrical System (30)"));
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    // TODO: fix this test when we get the contract as this is being handled in a future enhancement capacity
    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x1E<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad CVN - padded with 0xFF</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN - padded with 0xFF</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    // @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.iv", description = "Fail if CVN padded incorrectly (must use 0x00h in MSB for unused bytes)") })
    public void testObdModuleCvnMsbPaddingFailure() {
        // Module 1E - CalId all 0x00 and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(DM19CalibrationInformationPacket.PGN,
                                        0x1E,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x1E));
        DM19CalibrationInformationPacket dm191E = new DM19CalibrationInformationPacket(packet1E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x1E))).thenReturn(BusResult.of(dm191E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm191E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x1E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iv - Fail if CVN padded incorrectly (must use 0x00h in MSB for unused bytes)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0E<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * good CVN/Cal Id data</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good CVN/Cal Id data</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID") })
    public void testNonObdModuleProvidesCalIdWarning() {
        Packet packet0E = Packet.create(DM19CalibrationInformationPacket.PGN,
                                        0x0E,
                                        0x51,
                                        0xBA,
                                        0xFE,
                                        0xBD,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B,
                                        0x51,
                                        0x61,
                                        0x44,
                                        0x3B);
        DM19CalibrationInformationPacket dm190E = new DM19CalibrationInformationPacket(packet0E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0E)))
                                                        .thenReturn(BusResult.of(dm190E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0E<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad CVN - all 0xFF</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN - all 0xFF</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.iv", description = "Warn if any received CAL ID is all 0xFF(h) or any CVN is all 0x00(h)") })
    public void testNonObdModuleCalIdAllFsWarning() {
        Packet packet0E = Packet.create(0,
                                        0x0E,
                                        0x51,
                                        0xBA,
                                        0xFE,
                                        0xBD,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF);
        DM19CalibrationInformationPacket dm190E = new DM19CalibrationInformationPacket(packet0E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0E)))
                                                        .thenReturn(BusResult.of(dm190E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.iii - Non-OBD ECU Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.iv - Non-OBD ECU Received CAL ID is all 0xFF from Brakes - Drive Axle #2 (14)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x1E<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad CVN/Cal Id - all 0xFF</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id - all 0xFF</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.iii", description = "Fail if any received CAL ID is all 0xFF(h) or any CVN is all 0x00(h)") })
    public void testObdModuleCalIdAllFsWarning() {
        Packet packet0E = Packet.create(0,
                                        0x0E,
                                        0x51,
                                        0xBA,
                                        0xFE,
                                        0xBD,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF,
                                        0xFF);
        dataRepository.putObdModule(new OBDModuleInformation(0x0E));
        DM19CalibrationInformationPacket dm190E = new DM19CalibrationInformationPacket(packet0E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0E)))
                                                        .thenReturn(BusResult.of(dm190E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - OBD ECU Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iii - OBD ECU Received CAL ID is all 0xFF from Brakes - Drive Axle #2 (14)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0E<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad Cal Id - contains unprintable char</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad Cal Id - contains unprintable char</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID"),
            @TestItem(verifies = "6.1.7.3.d.iii", description = "For responses from non-OBD ECUs: Warn if <> 1 CVN for every CAL ID") })
    public void testNonObdModuleCalIdContainsNonPrintableCharWarning() {
        Packet packet0E = Packet.create(0,
                                        0x0E,
                                        0x51,
                                        0xBA,
                                        0xFE,
                                        0xBD,
                                        0x36,
                                        0x45,
                                        0x87,
                                        0x0A,  // unprinable char < 32 (0x20) OR char > 127 (0x7F)
                                        0x91,
                                        0x65,
                                        0x2F,
                                        0x6D,
                                        0x7A,
                                        0x34,
                                        0x51,
                                        0x29,
                                        0x5A,
                                        0x22,
                                        0x3B,
                                        0x4F);
        DM19CalibrationInformationPacket dm190E = new DM19CalibrationInformationPacket(packet0E);
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0E)))
                                                        .thenReturn(BusResult.of(dm190E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.iii - Non-OBD ECU Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * 4px;word-wrap=break-word">User
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0B<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * good CVN/Cal Id</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * good differing CVN/Cal Id</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.a", description = "Warn if total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units (test 6.1.2)") })
    public void testReportedCalIdCvnDifferUserEnteredWarning() {
        // packet with one CVN/Cal Id - returned to global and DS requests
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x0B,
                                      // Cal #1
                                      0x51,
                                      0x61,
                                      0x44,
                                      0x3B,
                                      0x39,
                                      0x38,
                                      0x32,
                                      0x33,
                                      0x34,
                                      0x35,
                                      0x36,
                                      0x37,
                                      0x38,
                                      0x39,
                                      0x33,
                                      0x39,
                                      0x76,
                                      0x33,
                                      0x66,
                                      0x35);

        // dataRepository packet with 2 CVN/Cal Ids
        Packet packet2 = Packet.create(DM19CalibrationInformationPacket.PGN,
                                       0x0B,

                                       // Cal #1
                                       0x56,
                                       0x3F,
                                       0x66,
                                       0x70,
                                       0x50,
                                       0x72,
                                       0x54,
                                       0x56,
                                       0x4D,
                                       0x50,
                                       0x52,
                                       0x63,
                                       0x7A,
                                       0x61,
                                       0x67,
                                       0x69,
                                       0x59,
                                       0x76,
                                       0x75,
                                       0x62,

                                       // Cal #2
                                       0x40,
                                       0x71,
                                       0x29,
                                       0x3E,
                                       0x52,
                                       0x50,
                                       0x52,
                                       0x42,
                                       0x42,
                                       0x41,
                                       0x39,
                                       0x32,
                                       0x67,
                                       0x7C,
                                       0x49,
                                       0x39,
                                       0x54,
                                       0x38,
                                       0x67,
                                       0x55);
        dataRepository.putObdModule(new OBDModuleInformation(0x0B));

        DM19CalibrationInformationPacket dm190B = new DM19CalibrationInformationPacket(packet2);

        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190B));
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0B))).thenReturn(BusResult.of(dm190B));

        System.out.println("packet2.getCalibrationInformation().size() is: "
                + new DM19CalibrationInformationPacket(packet2).getCalibrationInformation().size());
        System.out.println("dm190B.getCalibrationInformation().size() is: "
                + dm190B.getCalibrationInformation().size());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(new DM19CalibrationInformationPacket(packet).getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(ResultsListener.class), eq(0x0B));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.a - Total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units"));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(INFO),
                                        eq("6.1.7.3.b - Brakes - System Controller (11) provided more than one CAL ID and CVN pair in a single DM19 message"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0B<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad CVN/Cal Id - more than 1</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad CVN/Cal Id - more than 1</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.b", description = "Warn if more than one CAL ID and CVN pair is provided in a single DM19 message") })
    public void testMoreThanOneCalIdCvnPairWarning() {
        // legit values 0x20 to 0x7F
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x0B,
                                      // Cal #1
                                      0x51,
                                      0x61,
                                      0x44,
                                      0x3B,
                                      0x39,
                                      0x38,
                                      0x32,
                                      0x33,
                                      0x34,
                                      0x35,
                                      0x36,
                                      0x37,
                                      0x38,
                                      0x39,
                                      0x33,
                                      0x39,
                                      0x76,
                                      0x33,
                                      0x66,
                                      0x35,

                                      // Cal #2
                                      0x56,
                                      0x3F,
                                      0x66,
                                      0x70,
                                      0x50,
                                      0x72,
                                      0x54,
                                      0x56,
                                      0x4D,
                                      0x50,
                                      0x52,
                                      0x63,
                                      0x7A,
                                      0x61,
                                      0x67,
                                      0x69,
                                      0x59,
                                      0x76,
                                      0x75,
                                      0x62,

                                      // Cal #3
                                      0x40,
                                      0x71,
                                      0x29,
                                      0x3E,
                                      0x52,
                                      0x50,
                                      0x52,
                                      0x42,
                                      0x42,
                                      0x41,
                                      0x39,
                                      0x32,
                                      0x67,
                                      0x7C,
                                      0x49,
                                      0x39,
                                      0x54,
                                      0x38,
                                      0x67,
                                      0x55);

        // Module 0B - Missing CalId and Different DS value as OBD ECU
        dataRepository.putObdModule(new OBDModuleInformation(0x0B));
        DM19CalibrationInformationPacket dm190B = new DM19CalibrationInformationPacket(packet);

        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190B));
        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0B))).thenReturn(BusResult.of(dm190B));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190B.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0B));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(INFO),
                                        eq("6.1.7.3.b - Brakes - System Controller (11) provided more than one CAL ID and CVN pair in a single DM19 message"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0D<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad Cal Id - padding incorrect</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad Cal Id - padding incorrect</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii", description = "Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.)") })
    public void testObdModuleCalIdPaddingIncorrectlyFailure() {
        Packet packet = Packet.create(0x00,
                                      0x0D,
                                      // Cal #1
                                      0x00,
                                      0xBA,
                                      0x51,
                                      0xBD,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x63,
                                      0x61,
                                      0x6C,
                                      0x69,
                                      0x64,
                                      0x73,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);

        // Module 0D - NonPrintable Chars, padded incorrectly in CalId as OBD Module
        DM19CalibrationInformationPacket dm190D = new DM19CalibrationInformationPacket(packet);
        dataRepository.putObdModule(new OBDModuleInformation(0x0D));

        when(communicationsModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190D));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190D.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0D));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - OBD ECU Brakes - Drive axle #1 (13) CAL ID not formatted correctly (padded incorrectly)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c - CAL ID     calids       has CVN 0xBD51BA00 which has 00h in either the first or fourth bytes"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0x0D<br>
     * non-OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad Cal Id - padding incorrect</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad Cal Id - padding incorrect</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.d.i", description = "Warn if any non-OBD ECU provides CAL ID."),
            @TestItem(verifies = "6.1.7.3.d.iii", description = "Warn if CAL ID not formatted correctly (contains non-printable ASCII, padded incorrectly, etc.)") })
    public void testNonObdModuleCalIdPaddedIncorrectlyWarning() {
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x0D,
                                      // Cal #1
                                      0x51,
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x63,
                                      0x61,
                                      0x6C,
                                      0x69,
                                      0x64,
                                      0x73,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);

        DM19CalibrationInformationPacket dm190D = new DM19CalibrationInformationPacket(packet);
        when(communicationsModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190D));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190D.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0D));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive axle #1 (13) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.iii - Non-OBD ECU Brakes - Drive axle #1 (13) CAL ID not formatted correctly (padded incorrectly)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    public void testNonObdModuleCalIdNullPadded() {
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x0D,
                                      // Cal #1
                                      0x51,
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x63,
                                      0x61,
                                      0x6C,
                                      0x69,
                                      0x63,
                                      0x61,
                                      0x6C,
                                      0x69,
                                      0x64,
                                      0x73,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);

        DM19CalibrationInformationPacket dm190D = new DM19CalibrationInformationPacket(packet);
        when(communicationsModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190D));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190D.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0D));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.d.i - Non-OBD ECU Brakes - Drive axle #1 (13) provided CAL ID"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test method for {@link Part01Step07Controller#run()}.
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
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x0D<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19 response<br>
     * bad Cal Id - contains unprintable char</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good DM19
     * response<br>
     * bad Cal Id - contains unprintable char</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii", description = "Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.)") })
    public void testObdModuleUnprintableCharacterWarning() {
        Packet packet = Packet.create(0,
                                      0x0E,
                                      // Cal #1
                                      0x51,
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x0A, // unprintable character
                                      0x33,
                                      0x33,
                                      0x33,
                                      0x1A,
                                      0x35,
                                      0x36,
                                      0x37,
                                      0x38,
                                      0x39,
                                      0x30,
                                      0x31,
                                      0x32,
                                      0x33,
                                      0x34,
                                      0x35);
        dataRepository.putObdModule(new OBDModuleInformation(0x0E));
        DM19CalibrationInformationPacket dm190E = new DM19CalibrationInformationPacket(packet);

        when(communicationsModule.requestDM19(any(ResultsListener.class),
                                              eq(0x0E)))
                                                        .thenReturn(BusResult.of(dm190E));
        when(communicationsModule.requestDM19(any(ResultsListener.class))).thenReturn(RequestResult.of(dm190E));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        vehicleInformation.setCalIds(dm190E.getCalibrationInformation().size());
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM19(any(ResultsListener.class));
        verify(communicationsModule).requestDM19(any(), eq(0x0E));

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - OBD ECU Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testRealDataFromTruck2() {

        DM19CalibrationInformationPacket dm19 = new DM19CalibrationInformationPacket(Packet.create(DM19CalibrationInformationPacket.PGN,
                                                                                                   0,
                                                                                                   0x76,
                                                                                                   0xA4,
                                                                                                   0xC2,
                                                                                                   0xC8,
                                                                                                   0x52,
                                                                                                   0x41,
                                                                                                   0x44,
                                                                                                   0x58,
                                                                                                   0x53,
                                                                                                   0x55,
                                                                                                   0x41,
                                                                                                   0x41,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0xA8,
                                                                                                   0x73,
                                                                                                   0x89,
                                                                                                   0x13,
                                                                                                   0x4E,
                                                                                                   0x4F,
                                                                                                   0x78,
                                                                                                   0x2D,
                                                                                                   0x53,
                                                                                                   0x41,
                                                                                                   0x45,
                                                                                                   0x31,
                                                                                                   0x34,
                                                                                                   0x61,
                                                                                                   0x20,
                                                                                                   0x41,
                                                                                                   0x54,
                                                                                                   0x49,
                                                                                                   0x31,
                                                                                                   0x00,
                                                                                                   0x8C,
                                                                                                   0x4B,
                                                                                                   0xF9,
                                                                                                   0xC9,
                                                                                                   0x4E,
                                                                                                   0x4F,
                                                                                                   0x78,
                                                                                                   0x2D,
                                                                                                   0x53,
                                                                                                   0x41,
                                                                                                   0x45,
                                                                                                   0x31,
                                                                                                   0x34,
                                                                                                   0x61,
                                                                                                   0x20,
                                                                                                   0x41,
                                                                                                   0x54,
                                                                                                   0x4F,
                                                                                                   0x31,
                                                                                                   0x00,
                                                                                                   0xFB,
                                                                                                   0x1B,
                                                                                                   0xB5,
                                                                                                   0x8C,
                                                                                                   0x30,
                                                                                                   0x32,
                                                                                                   0x30,
                                                                                                   0x31,
                                                                                                   0x30,
                                                                                                   0x31,
                                                                                                   0x30,
                                                                                                   0x30,
                                                                                                   0x30,
                                                                                                   0x35,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0xD2,
                                                                                                   0xBF,
                                                                                                   0x0F,
                                                                                                   0xA9,
                                                                                                   0x50,
                                                                                                   0x4D,
                                                                                                   0x53,
                                                                                                   0x31,
                                                                                                   0x32,
                                                                                                   0x33,
                                                                                                   0x34,
                                                                                                   0x31,
                                                                                                   0x41,
                                                                                                   0x31,
                                                                                                   0x30,
                                                                                                   0x31,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00,
                                                                                                   0x00));

        when(communicationsModule.requestDM19(any())).thenReturn(RequestResult.of(dm19));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.requestDM19(any(), eq(0)))
                                                            .thenReturn(BusResult.of(
                                                                                     dm19));

        runTest();

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.a - Total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(INFO),
                                        eq("6.1.7.3.b - Engine #1 (0) provided more than one CAL ID and CVN pair in a single DM19 message"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any());
        verify(communicationsModule).requestDM19(any(), eq(0));

    }
}
