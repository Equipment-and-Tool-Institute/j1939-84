/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Step07Controller}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 7", description = "DM19: Calibration information"))
public class Step07ControllerTest {

    private static DM19CalibrationInformationPacket createDM19(int sourceAddress, String calId, String cvn)
            throws UnsupportedEncodingException {
        return createDM19(sourceAddress, calId, cvn, 1);
    }

    private static DM19CalibrationInformationPacket createDM19(int sourceAddress, String calId, String cvn, int count)
            throws UnsupportedEncodingException {
        DM19CalibrationInformationPacket packet = mock(DM19CalibrationInformationPacket.class);
        when(packet.getSourceAddress()).thenReturn(sourceAddress);

        List<CalibrationInformation> calInfo = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            calInfo.add(new CalibrationInformation(calId, cvn, calId.getBytes("UTF-8"), cvn.getBytes("UTF-8")));
        }
        when(packet.getCalibrationInformation()).thenReturn(calInfo);

        return packet;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step07Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private void runTest() {
        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step07Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository,
                mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part 1 Step 7", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(1, instance.getTotalSteps());
    }

    /**
     * Test one module responds without issue
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.7"),
            @TestItem(verifies = "6.1.7.1.a"),
            @TestItem(verifies = "6.1.7.1.b"),
            @TestItem(verifies = "6.1.7.1.c") },
             description = "Global DM19 (send Request (PGN 59904) for PGN 54016 (SPNs 1634 and 1635))"
                     + "<br>"
                     + "Create list of ECU address + CAL ID + CVN. [An ECU address may report more than one CAL ID and CVN]"
                     + "<br>"
                     + "Display this list in the log. [Note display the CVNs using big endian format and not little endian format as given in the response]")
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testRunHappyPath() throws Throwable {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        DM19CalibrationInformationPacket dm19 = createDM19(0, "CALID", "1234");

        globalDM19s.add(dm19);
        when(vehicleInformationModule.reportCalibrationInformation(any())).thenReturn(globalDM19s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm19), Collections.emptyList()));

        Set<Integer> addresses = new HashSet<>();
        addresses.add(0);
        when(dataRepository.getObdModuleAddresses()).thenReturn(addresses);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(moduleInfo).setCalibrationInformation(dm19.getCalibrationInformation());
        verify(vehicleInformationModule).reportCalibrationInformation(any());
        verify(dataRepository, times(2)).getObdModule(0);
        verify(dataRepository).getVehicleInformation();
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0));
        verify(dataRepository).getObdModuleAddresses();
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.7.2.a",
                               description = "Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units"))
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
                        justification = "The method is called just to get some exception.")
    public void testRunNoModulesRespond() {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        when(vehicleInformationModule.reportCalibrationInformation(any())).thenReturn(globalDM19s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(1);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Set<Integer> addresses = new HashSet<>();
        when(dataRepository.getObdModuleAddresses()).thenReturn(addresses);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(1,
                7,
                FAIL,
                "6.1.7.2.a. Total number of reported CAL IDs is < user entered value for number of emission or diagnostic critical control units");

        verify(vehicleInformationModule).reportCalibrationInformation(any());
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).getObdModuleAddresses();
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
            "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT" },
                        justification = "The method is called just to get some exception.")
    public void testRunWithWarningsAndFailures() throws UnsupportedEncodingException {
        List<DM19CalibrationInformationPacket> globalDM19s = new ArrayList<>();

        // Module 0A - Too Many CalInfo's
        DM19CalibrationInformationPacket dm190A = createDM19(0x0A, "CALID", "1234", 2);
        globalDM19s.add(dm190A);

        // Module 0B - Missing CalId and Different DS value as OBD Module
        DM19CalibrationInformationPacket dm190B = createDM19(0x0B, "", "1234");
        globalDM19s.add(dm190B);

        // Module 1B - Missing CVN as non-OBD Module
        DM19CalibrationInformationPacket dm191B = createDM19(0x1B, "", "1234");
        globalDM19s.add(dm191B);

        // Module 0C - Missing CVN as OBD Module
        DM19CalibrationInformationPacket dm190C = createDM19(0x0C, "CALID", "");
        globalDM19s.add(dm190C);

        // Module 1C - Missing CVN as non-OBD Module
        DM19CalibrationInformationPacket dm191C = createDM19(0x1C, "CALID", "");
        globalDM19s.add(dm191C);

        // Module 0D - NonPrintable Chars, padded incorrectly in CalId as OBD
        // Module
        // Also reports BUSY with DS
        DM19CalibrationInformationPacket dm190D = createDM19(0x0D, "CALID\u0000F", "1234");
        globalDM19s.add(dm190D);

        // Module 1D - Non-Printable Chars, padded incorrectly in CalId as
        // non-OBD
        // Module
        DM19CalibrationInformationPacket dm191D = createDM19(0x1D, "CALID\u0000F", "1234");
        globalDM19s.add(dm191D);

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
        globalDM19s.add(dm191E);

        // Non-NACK from non-reporting OBD Module - Module M

        when(vehicleInformationModule.reportCalibrationInformation(any())).thenReturn(globalDM19s);

        OBDModuleInformation moduleInfo0A = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0x0A)).thenReturn(moduleInfo0A);

        OBDModuleInformation moduleInfo0B = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0x0B)).thenReturn(moduleInfo0B);

        OBDModuleInformation moduleInfo0C = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0x0C)).thenReturn(moduleInfo0C);

        OBDModuleInformation moduleInfo0D = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0x0D)).thenReturn(moduleInfo0D);

        OBDModuleInformation moduleInfo0E = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0x0E)).thenReturn(moduleInfo0E);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEmissionUnits(5);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0A)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm190A), Collections.emptyList()));
        DM19CalibrationInformationPacket dm190B2 = createDM19(0x0B, "ABCD", "1234");
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0B)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm190B2), Collections.emptyList()));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x1B)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm191B), Collections.emptyList()));
        AcknowledgmentPacket busy = mock(AcknowledgmentPacket.class);
        when(busy.getResponse()).thenReturn(Response.BUSY);
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0C)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), Collections.singletonList(busy)));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x1C)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm191C), Collections.emptyList()));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0D)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm190D), Collections.emptyList()));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x1D)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm191D), Collections.emptyList()));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0E)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm190E), Collections.emptyList()));
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x1E)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm191E), Collections.emptyList()));
        DM19CalibrationInformationPacket dm190F = createDM19(0x0F, "ABCD", "1234");
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x0F)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm190F), Collections.emptyList()));

        Set<Integer> addresses = new HashSet<>();
        addresses.add(0x0A);
        addresses.add(0x0B);
        addresses.add(0x0C);
        addresses.add(0x0D);
        addresses.add(0x0E);
        addresses.add(0x0F);
        when(dataRepository.getObdModuleAddresses()).thenReturn(addresses);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(moduleInfo0A).setCalibrationInformation(dm190A.getCalibrationInformation());
        verify(moduleInfo0B).setCalibrationInformation(dm190B.getCalibrationInformation());
        verify(moduleInfo0C).setCalibrationInformation(dm190C.getCalibrationInformation());
        verify(moduleInfo0D).setCalibrationInformation(dm190D.getCalibrationInformation());
        verify(moduleInfo0E).setCalibrationInformation(dm190E.getCalibrationInformation());

        verify(vehicleInformationModule).reportCalibrationInformation(any());
        verify(dataRepository, times(2)).getObdModule(0x0A);
        verify(dataRepository, times(2)).getObdModule(0x0B);
        verify(dataRepository, times(2)).getObdModule(0x1B);
        verify(dataRepository, times(2)).getObdModule(0x0C);
        verify(dataRepository, times(2)).getObdModule(0x1C);
        verify(dataRepository, times(2)).getObdModule(0x0D);
        verify(dataRepository, times(2)).getObdModule(0x1D);
        verify(dataRepository, times(2)).getObdModule(0x0E);
        verify(dataRepository, times(2)).getObdModule(0x1E);
        verify(dataRepository).getVehicleInformation();
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0A));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0B));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x1B));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0C));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x1C));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0D));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x1D));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0E));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x1E));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x0F));

        verify(dataRepository).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                7,
                WARN,
                "6.1.7.3.a. Total number of reported CAL IDs is > user entered value for number of emission or diagnostic critical control units");

        verify(mockListener).addOutcome(1,
                7,
                WARN,
                "6.1.7.3.b. More than one CAL ID and CVN pair is provided in a single DM19 message.");

        verify(mockListener, times(2)).addOutcome(1, 7, FAIL, "6.1.7.2.b.i. <> 1 CVN for every CAL ID.");
        verify(mockListener, times(2)).addOutcome(1, 7, WARN, "6.1.7.3.c.ii. <> 1 CVN for every CAL ID.");
        verify(mockListener, times(2)).addOutcome(1,
                7,
                FAIL,
                "6.1.7.2.b.ii. CAL ID not formatted correctly (contains non-printable ASCII)");
        verify(mockListener, times(2)).addOutcome(1,
                7,
                WARN,
                "6.1.7.3.c.iii. Warn if CAL ID not formatted correctly (contains non-printable ASCII)");
        verify(mockListener).addOutcome(1,
                7,
                FAIL,
                "6.1.7.2.b.ii. CAL ID not formatted correctly (padded incorrectly)");
        verify(mockListener).addOutcome(1,
                7,
                WARN,
                "6.1.7.3.c.iii. CAL ID not formatted correctly (padded incorrectly)");

        verify(mockListener).addOutcome(1, 7, FAIL, "6.1.7.2.b.iii. Received CAL ID is all 0xFF");
        verify(mockListener).addOutcome(1, 7, WARN, "6.1.7.3.c.iv. Received CAL ID is all 0xFF");
        verify(mockListener).addOutcome(1, 7, FAIL, "6.1.7.2.b.iii. Received CVN is all 0x00.");
        verify(mockListener).addOutcome(1, 7, FAIL, "6.1.7.3.c.iv. Received CVN is all 0x00.");
        verify(mockListener).addOutcome(1,
                7,
                FAIL,
                "6.1.7.5.a. Compared ECU address + CAL ID + CVN list created from global DM19 request and found difference.");
        verify(mockListener).addOutcome(1,
                7,
                FAIL,
                "6.1.7.5.b. NACK (PGN 59392) with mode/control byte = 3 (busy) received");
        verify(mockListener).addOutcome(1,
                7,
                FAIL,
                "6.1.7.5.c. NACK not received from OBD ECU that did not respond to global query.");
    }
}
