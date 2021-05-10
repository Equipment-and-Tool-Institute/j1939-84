/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
        CalibrationInformation[] calInfo = new CalibrationInformation[count];
        for (int i = 0; i < count; i++) {
            calInfo[i] = new CalibrationInformation(calId, cvn);
        }
        return DM19CalibrationInformationPacket.create(sourceAddress, 0, calInfo);
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
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
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

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.7.2.a", description = "Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units"))
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testRunNoModulesRespond() {
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

    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7.2.b.i"),
            @TestItem(verifies = "6.1.7.2.b.ii"),
            @TestItem(verifies = "6.1.7.2.b.iii"),
            @TestItem(verifies = "6.1.7.3.a"),
            @TestItem(verifies = "6.1.7.3.b"),
            @TestItem(verifies = "6.1.7.3.c.ii"),
            @TestItem(verifies = "6.1.7.3.c.iii"),
            @TestItem(verifies = "6.1.7.3.c.iv"),
            @TestItem(verifies = "6.1.7.5.a"),
            @TestItem(verifies = "6.1.7.5.b"),
            @TestItem(verifies = "6.1.7.5.c ") })
    @SuppressFBWarnings(value = {
            "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT" }, justification = "The method is called just to get some exception.")
    public void testRunWithWarningsAndFailures() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        // Module 0A - Too Many CalInfo's
        dataRepository.putObdModule(new OBDModuleInformation(0x0A));
        DM19CalibrationInformationPacket dm190A = createDM19(0x0A, "CALID", "1234", 2);
        when(vehicleInformationModule.requestDM19(any(), eq(0x0A))).thenReturn(BusResult.of(dm190A));
        globalDM19s.add(dm190A);

        // Module 0B - Missing CalId and Different DS value as OBD ECU
        dataRepository.putObdModule(new OBDModuleInformation(0x0B));
        DM19CalibrationInformationPacket dm190B2 = createDM19(0x0B, "ABCD", "1234", 1);
        when(vehicleInformationModule.requestDM19(any(), eq(0x0B))).thenReturn(BusResult.of(dm190B2));
        DM19CalibrationInformationPacket dm190B = createDM19(0x0B, "", "1234", 1);
        globalDM19s.add(dm190B);


        // Module 1B - Missing CVN as non-OBD ECU
        DM19CalibrationInformationPacket dm191B = createDM19(0x1B, "", "1234", 1);
        when(vehicleInformationModule.requestDM19(any(), eq(0x1B))).thenReturn(BusResult.of(dm191B));
        globalDM19s.add(dm191B);


        // Module 0C - Missing CVN as OBD Module
        dataRepository.putObdModule(new OBDModuleInformation(0x0C));
        when(vehicleInformationModule.requestDM19(any(), eq(0x0C))).thenReturn(BusResult.empty());
        DM19CalibrationInformationPacket dm190C = createDM19(0x0C, "CALID", "", 1);
        globalDM19s.add(dm190C);

        // Module 1C - Missing CVN as non-OBD Module
        DM19CalibrationInformationPacket dm191C = createDM19(0x1C, "CALID", "", 1);
        when(vehicleInformationModule.requestDM19(any(), eq(0x1C))).thenReturn(BusResult.of(dm191C));
        globalDM19s.add(dm191C);

        // Module 0D - NonPrintable Chars, padded incorrectly in CalId as OBD
        // Module Also reports BUSY with DS
        dataRepository.putObdModule(new OBDModuleInformation(0x0D));
        DM19CalibrationInformationPacket dm190D = createDM19(0x0D, "CALID\u0000F", "1234", 1);
        when(vehicleInformationModule.requestDM19(any(), eq(0x0D))).thenReturn(BusResult.of(dm190D));
        globalDM19s.add(dm190D);

        // Module 1D - Non-Printable Chars, padded incorrectly in CalId as
        // non-OBD Module
        DM19CalibrationInformationPacket dm191D = createDM19(0x1D, "CALID\u0000F", "1234", 1);
        when(vehicleInformationModule.requestDM19(any(), eq(0x1D))).thenReturn(BusResult.of(dm191D));
        globalDM19s.add(dm191D);

        dataRepository.putObdModule(new OBDModuleInformation(0x0E));
        Packet packet0E = Packet.create(0,
                                        0x0E,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
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
        when(vehicleInformationModule.requestDM19(any(), eq(0x0E))).thenReturn(BusResult.of(dm190E));
        globalDM19s.add(dm190E);

        // Module 1E - CalId all 0xFF and CVN all 0x00 as OBD Module
        Packet packet1E = Packet.create(0,
                                        0x1E,
                                        0x00,
                                        0x00,
                                        0x00,
                                        0x00,
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
        DM19CalibrationInformationPacket dm191E = new DM19CalibrationInformationPacket(packet1E);
        when(vehicleInformationModule.requestDM19(any(), eq(0x1E))).thenReturn(BusResult.of(dm191E));
        globalDM19s.add(dm191E);

        // Non-NACK from non-reporting OBD Module - Module M
        when(vehicleInformationModule.requestDM19(any())).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(5);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        assertNotNull(dataRepository.getObdModule(0x0A)
                                    .getLatest(DM19CalibrationInformationPacket.class)
                                    .getCalibrationInformation());
        assertNotNull(dataRepository.getObdModule(0x0B)
                                    .getLatest(DM19CalibrationInformationPacket.class)
                                    .getCalibrationInformation());
        assertNotNull(dataRepository.getObdModule(0x0C)
                                    .getLatest(DM19CalibrationInformationPacket.class)
                                    .getCalibrationInformation());
        assertNotNull(dataRepository.getObdModule(0x0D)
                                    .getLatest(DM19CalibrationInformationPacket.class)
                                    .getCalibrationInformation());
        assertNotNull(dataRepository.getObdModule(0x0E)
                                    .getLatest(DM19CalibrationInformationPacket.class)
                                    .getCalibrationInformation());

        verify(vehicleInformationModule).requestDM19(any());
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0A));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0B));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0C));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0D));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x0E));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1B));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1C));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1D));
        verify(vehicleInformationModule).requestDM19(any(), eq(0x1E));

        // assertEquals(List.of(), listener.getOutcomes());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.7.3.c.i - Non-OBD ECU Vehicle Navigation (28) provided CAL ID");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.7.3.c.i - Non-OBD ECU Electrical System (30) provided CAL ID");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.b.ii - Brakes - Drive Axle #2 (14) CAL ID not formatted correctly (contains non-printable ASCII)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.7.3.c.iii - Electrical System (30) CAL ID not formatted correctly (contains non-printable ASCII)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.b.iii - Received CAL ID is all 0xFF from Brakes - Drive Axle #2 (14)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.2.b.iii - Received CVN is all 0x00 from Brakes - Drive Axle #2 (14)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.7.3.c.iv - Received CAL ID is all 0xFF from Electrical System (30)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.3.c.iv Received CVN is all 0x00 from Electrical System (30)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.7.5.a - Difference compared to data received during global request from Brakes - System Controller (11)");
    }
}
