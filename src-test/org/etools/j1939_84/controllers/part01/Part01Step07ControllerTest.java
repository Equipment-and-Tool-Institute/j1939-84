/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
    private DiagnosticMessageModule diagnosticMessageModule;
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
                                              diagnosticMessageModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    /**
     * Test one module responds without issue
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7"),
            @TestItem(verifies = "6.1.7.1.a"),
            @TestItem(verifies = "6.1.7.1.b"),
            @TestItem(verifies = "6.1.7.1.c") }, description = "Global DM19 (send Request (PGN 59904) for PGN 54016 (SPNs 1634 and 1635))"
                    + "<br>"
                    + "Create list of ECU address + CAL ID + CVN. [An ECU address may report more than one CAL ID and CVN]"
                    + "<br>"
                    + "Display this list in the log. [Note display the CVNs using big endian format and not little endian format as given in the response]")
    public void testRunHappyPath() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        DM19CalibrationInformationPacket dm19 = createDM19(0, "CALID", "1234", 1);

        globalDM19s.add(dm19);
        when(vehicleInformationModule.requestDM19(any())).thenReturn(globalDM19s);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(vehicleInformationModule.requestDM19(any(), eq(0)))
                                                                .thenReturn(BusResult.of(
                                                                                         dm19));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(vehicleInformationModule).requestDM19(any());
        verify(vehicleInformationModule).requestDM19(any(), eq(0));

    }

    /**
     * Test one obd module responds with a CVN count of less than one
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.i") }, description = "For responses from OBD ECUs: Fail if <> 1 CVN for every CAL ID.")
    public void testObdModuleLessThan1CvnFailure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

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

        globalDM19s.add(dm19);
        dataRepository.putObdModule(new OBDModuleInformation(0x0E));

        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(0x0E)))
                                                                                        .thenReturn(BusResult.of(dm19));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.b.i - Brakes - Drive Axle #2 (14) <> 1 CVN for every CAL ID");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(vehicleInformationModule).requestDM19(any());
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

    }

    /**
     * Test one non obd module respond with a CVN count of less than 1
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.c.ii") }, description = "For responses from non-OBD ECUs: Warn if <> 1 CVN for every CAL ID")
    public void testNonObdModuleLessThan1CvnFailure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

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

        globalDM19s.add(dm19);

        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(0x0E)))
                                                                                        .thenReturn(BusResult.of(dm19));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.1.7.3.c.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.1.7.3.c.ii - Brakes - Drive Axle #2 (14) <> 1 CVN for every CAL ID"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(vehicleInformationModule).requestDM19(any());
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

    }

    /**
     * Test no obd module responds
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.7.2.a", description = "Fail if total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units (test 6.1.2)"))
    public void testAmountOfUserEnteredCalIdDiffersReportedAmountFailure() {
        when(vehicleInformationModule.requestDM19(any())).thenReturn(List.of());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setCalIds(5);
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.a - Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units");

        verify(vehicleInformationModule).requestDM19(any());
    }

    /**
     * Test no response to global DM19 request; DS request return good message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.c", description = "Fail if NACK not received from OBD ECUs that did not respond to global query") })
    public void testNoNacksToGlobalQueryFromObdFailure() {

        DM19CalibrationInformationPacket dm19 = createDM19(0, "CALID", "1234", 1);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(vehicleInformationModule.requestDM19(any(), eq(0)))
                                                                .thenReturn(BusResult.of(
                                                                                         dm19));

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.c - OBD ECU Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query"));

        verify(vehicleInformationModule).requestDM19(any());
        verify(vehicleInformationModule).requestDM19(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns NACK to global query; DS query returns good message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.b", description = "Fail if NACK (PGN 59392) with mode/control byte = 3 (busy) received") })
    public void testObdRespondsWithBusyDsQueryFailure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        DM19CalibrationInformationPacket dm19 = createDM19(0, "CALID", "1234", 1);
        globalDM19s.add(dm19);
        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(vehicleInformationModule.requestDM19(any())).thenReturn(globalDM19s);

        AcknowledgmentPacket nack = AcknowledgmentPacket.create(0, NACK);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(0)))
                                                                                     .thenReturn(BusResult.of(nack));
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(1)))
                                                                                     .thenReturn(BusResult.of(dm19));

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.c - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0));
        verify(vehicleInformationModule).requestDM19(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns BUSY to global query; DS query returns good message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.b", description = "Fail if NACK (PGN 59392) with mode/control byte = 3 (busy) received") })
    public void testObdRespondHasBusyAtByte3Failure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        DM19CalibrationInformationPacket dm19a0 = createDM19(0, "CALID", "1234", 1);
        globalDM19s.add(dm19a0);
        DM19CalibrationInformationPacket dm19a1 = createDM19(1, "CALID", "1234", 1);
        globalDM19s.add(dm19a1);
        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(vehicleInformationModule.requestDM19(any())).thenReturn(globalDM19s);

        AcknowledgmentPacket nack = AcknowledgmentPacket.create(0, BUSY);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(0)))
                                                                                     .thenReturn(BusResult.of(nack));
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class), eq(1)))
                                                                                     .thenReturn(BusResult.of(dm19a0));

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.b - Engine #1 (0) responded NACK with control byte = 3 (busy)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0));
        verify(vehicleInformationModule).requestDM19(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns one to global query; DS query returns a different message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.5.b", description = "Compare to ECU address + CAL ID + CVN list created from global DM19 request and fail if any difference") })
    public void testEcuResponseDiffersFromGlobalDm19Failure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        dataRepository.putObdModule(new OBDModuleInformation(0x0B));
        DM19CalibrationInformationPacket dm190B2 = createDM19(0x0B, "ABCD", "1234", 1);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0B))).thenReturn(BusResult.of(dm190B2));
        DM19CalibrationInformationPacket dm190B = createDM19(0x0B, "", "1234", 1);
        globalDM19s.add(dm190B);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.5.a - Difference compared to data received during global request from Brakes - System Controller (11)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0B));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with CVNs as all zeros to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.c.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID"),
            @TestItem(verifies = "6.1.7.3.c.iv", description = "Received CVN that is all 0x00") })
    public void testNonObdModuleCvnAllZerosFailure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        // Module 1E - CalId all 0x00 and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(0,
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
        when(vehicleInformationModule.requestDM19(any(), eq(0x1E))).thenReturn(BusResult.of(dm191E));
        globalDM19s.add(dm191E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.i - Non-OBD ECU Electrical System (30) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.3.c.iv Received CVN that is all 0x00 from Electrical System (30)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns a message with CVNs as all 0X00 to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.iii", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID") })
    public void testObdModuleCvnAllZerosFailure() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        // Module 1E - CalId all 0x00 and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(0,
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
        when(vehicleInformationModule.requestDM19(any(), eq(0x1E))).thenReturn(BusResult.of(dm191E));
        globalDM19s.add(dm191E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iii - Received CVN is all 0x00 from Electrical System (30)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with CVNs as all 0xFFs to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.c.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID"),
            @TestItem(verifies = "6.1.7.3.c.iii", description = "For responses from non-OBD ECUs: Warn if <> 1 CVN for every CAL ID"),
            @TestItem(verifies = "6.1.7.3.c.iv", description = "For responses from non-OBD ECUs: Warn if any received CAL ID is all 0xFF(h) or any CVN is all 0x00(h)") })
    public void testNonObdModuleCalIdAllFsWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
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
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0E)))
                                                            .thenReturn(BusResult.of(dm190E));
        globalDM19s.add(dm190E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.iii - Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.iv - Received CAL ID is all 0xFF from Brakes - Drive Axle #2 (14)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns a message with CVNs as all 0xFFs to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii", description = "For responses from OBD ECUs: Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.)"),
            @TestItem(verifies = "6.1.7.2.b.iii", description = "For responses from OBD ECUs: Fail if any received CAL ID is all 0xFF(h) or any CVN is all 0x00(h)") })
    public void testObdModuleCalIdAllFsWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
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
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0E)))
                                                            .thenReturn(BusResult.of(dm190E));
        globalDM19s.add(dm190E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.iii - Received CAL ID is all 0xFF from Brakes - Drive Axle #2 (14)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with a CAL ID containing an ASCII unprintable to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.c.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID"),
            @TestItem(verifies = "6.1.7.3.c.iii", description = "For responses from non-OBD ECUs: Warn if <> 1 CVN for every CAL ID") })
    public void testNonObdModuleCalIdContainsNonPrintableCharWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
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
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0E)))
                                                            .thenReturn(BusResult.of(dm190E));
        globalDM19s.add(dm190E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.i - Non-OBD ECU Brakes - Drive Axle #2 (14) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.iii - Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test user entered number of CAL IDs differs from module report
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.b", description = "Warn if more than one CAL ID and CVN pair is provided in a single DM19 message") })
    public void testMoreThanOneCalIdCvnPairWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        // legit values 0x20 to 0x7F
        Packet packet = Packet.create(0,
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
        DM19CalibrationInformationPacket dm190B = // createDM19(0x0B, "CALID", "1234", 2);
                new DM19CalibrationInformationPacket(packet);
        globalDM19s.add(dm190B);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0B))).thenReturn(BusResult.of(dm190B));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.b - Brakes - System Controller (11) provided more than one CAL ID and CVN pair in a single DM19 message"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0B));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test obd module returns a message with a CAL ID padded incorrectly to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii", description = "Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.)") })
    public void testObdModuleCalIdPaddingIncorrectlyWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
        Packet packet = Packet.create(0,
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

        when(vehicleInformationModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        globalDM19s.add(dm190D);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - Brakes - Drive axle #1 (13) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - Brakes - Drive axle #1 (13) CAL ID not formatted correctly (padded incorrectly)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0D));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with a CAL ID padded incorrectly to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.3.c.i", description = "For responses from non-OBD ECUs: Warn if any non-OBD ECU provides CAL ID."),
            @TestItem(verifies = "6.1.7.3.c.iii", description = "For responses from non-OBD ECUs: Warn if CAL ID not formatted correctly (contains non-printable ASCII, padded incorrectly, etc.)") })
    public void testNonObdModuleCalIdPaddingIncorrectlyWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
        Packet packet = Packet.create(0,
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
        when(vehicleInformationModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        globalDM19s.add(dm190D);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.i - Non-OBD ECU Brakes - Drive axle #1 (13) provided CAL ID"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.iii - Brakes - Drive axle #1 (13) CAL ID not formatted correctly (contains non-printable ASCII)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(WARN),
                                        eq("6.1.7.3.c.iii - Brakes - Drive axle #1 (13) CAL ID not formatted correctly (padded incorrectly)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0D));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test non obd module returns a message with a CAL ID containing an ASCII unprintable to global query;
     * DS query returns same message
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.ii", description = "For responses from OBD ECUs: Fail if CAL ID not formatted correctly (printable ASCII, padded incorrectly, etc.)") })
    public void testObdModuleUnprintableCharacterWarning() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();
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

        when(vehicleInformationModule.requestDM19(any(ResultsListener.class),
                                                  eq(0x0E)))
                                                            .thenReturn(BusResult.of(dm190E));
        globalDM19s.add(dm190E);
        when(vehicleInformationModule.requestDM19(any(ResultsListener.class))).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(mockListener).addOutcome(eq(1),
                                        eq(7),
                                        eq(FAIL),
                                        eq("6.1.7.2.b.ii - Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)"));

        verify(vehicleInformationModule).requestDM19(any(ResultsListener.class));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }
}
