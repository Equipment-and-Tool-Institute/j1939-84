/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part02Step05Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step05ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 2;

    private static final int STEP_NUMBER = 5;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step05Controller instance;

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
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part02Step05Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
                                              diagnosticMessageModule);

        setup(instance, listener, j1939, executor, reportFileModule, engineSpeedModule, vehicleInformationModule, diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 mockListener,
                                 diagnosticMessageModule);
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
     * Test method for {@link StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for {@link StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test one module responds without issue
     */
    @Test
    public void testRunHappyPathOneModuleThreeCalibration() {
        //formatter:off
        Packet packet = Packet.create(DM19CalibrationInformationPacket.PGN,
                                      0x00,
                                      // Cal #1
                                      0x51, 0xBA, 0xFE, 0xBD, 0x41, 0x4E,
                                      0x54, 0x35, 0x41, 0x53, 0x52, 0x31, 0x20,
                                      0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                                      // Cal #2
                                      0x96, 0xBF, 0xDC, 0x40, 0x50, 0x42, 0x54,
                                      0x35, 0x4D, 0x50, 0x52, 0x33, 0x00, 0x00,
                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                                      // Cal #3
                                      0x40, 0x91, 0xB9, 0x3E, 0x52, 0x50, 0x52,
                                      0x42, 0x42, 0x41, 0x31, 0x30, 0x00, 0x00,
                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        //formatter:on
        DM19CalibrationInformationPacket dm19CalibrationInformationPacket = new DM19CalibrationInformationPacket(packet);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.setCalibrationInformation(dm19CalibrationInformationPacket.getCalibrationInformation());

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        obdModuleInformations.add(obd0x00);

        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, dm19CalibrationInformationPacket));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModules();

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0));

    }

    /*
     *  Make sure we respond correctly when no OBDModule in the repository
     */
    @Test
    public void testRunNoModulesRespond() {
        when(dataRepository.getObdModules()).thenReturn(List.of());

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModules();
    }

    @Test
    public void testRunWithWarningsAndFailures() {

        //formatter:off
        Packet packet0x00 = Packet.create(DM19CalibrationInformationPacket.PGN,
                                          0x00,
                                          // Cal #1
                                          0x51, 0xBA, 0xFE, 0xBD, 0x41, 0x4E,
                                          0x54, 0x35, 0x41, 0x53, 0x52, 0x31, 0x20,
                                          0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                                          // Cal #2
                                          0x96, 0xBF, 0xDC, 0x40, 0x50, 0x42, 0x54,
                                          0x35, 0x4D, 0x50, 0x52, 0x33, 0x00, 0x00,
                                          0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

                                          // Cal #3
                                          0x40, 0x91, 0xB9, 0x3E, 0x52, 0x50, 0x52,
                                          0x42, 0x42, 0x41, 0x31, 0x30, 0x00, 0x00,
                                          0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        //formatter:on
        DM19CalibrationInformationPacket dm19CalibrationInformationPacket0x00 = new DM19CalibrationInformationPacket(
                packet0x00);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.setCalibrationInformation(dm19CalibrationInformationPacket0x00.getCalibrationInformation());

        //formatter:off
        Packet packet0x01 = Packet.create(DM19CalibrationInformationPacket.PGN,
                                          0x01,
                                          // Cal #1
                                          0x00, 0xAC, 0xFF, 0x33, 0x41, 0x4E,
                                          0x54, 0x35, 0x41, 0x53, 0x52, 0x31, 0x20,
                                          0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        //formatter:on
        DM19CalibrationInformationPacket dm19CalibrationInformationPacket0x01 = new DM19CalibrationInformationPacket(
                packet0x01);
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.setCalibrationInformation(dm19CalibrationInformationPacket0x01.getCalibrationInformation());

        //formatter:off
        Packet packet0x02 = Packet.create(DM19CalibrationInformationPacket.PGN,
                                          0x02,
                                          // Cal #1
                                          0x96, 0xBF, 0xDC, 0x40, 0x50, 0x42, 0x54,
                                          0x35, 0x4D, 0x50, 0x52, 0x33, 0x00, 0x00,
                                          0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        //formatter:on
        DM19CalibrationInformationPacket dm19CalibrationInformationPacket0x02 = new DM19CalibrationInformationPacket(
                packet0x02);
        OBDModuleInformation obd0x02 = new OBDModuleInformation(0x02);
        obd0x02.setCalibrationInformation(dm19CalibrationInformationPacket0x02.getCalibrationInformation());

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        obdModuleInformations.add(obd0x00);
        obdModuleInformations.add(obd0x01);
        obdModuleInformations.add(obd0x02);

        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        //formatter:off
        Packet packet0x01V2 = Packet.create(DM19CalibrationInformationPacket.PGN,
                                            0x01,
                                            // Cal #1
                                            0x51, 0xBA, 0xFE, 0xBD, 0x41, 0x4E,
                                            0x54, 0x35, 0x41, 0x53, 0x52, 0x31, 0x20,
                                            0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        //formatter:on
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, dm19CalibrationInformationPacket0x00));

        DM19CalibrationInformationPacket dm19CalibrationInformationPacket0x01V2 = new DM19CalibrationInformationPacket(
                packet0x01V2);
        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, dm19CalibrationInformationPacket0x01V2));

        when(vehicleInformationModule.reportCalibrationInformation(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.5.2.a - Engine #2 (1) reported CAL IDs/CVNs with different values/quantity than those reported in Part 1 data" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, Outcome.FAIL, "6.2.5.2.a - Engine #2 (1) reported CAL IDs/CVNs with different values/quantity than those reported in Part 1 data");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x00));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x01));
        verify(vehicleInformationModule).reportCalibrationInformation(any(), eq(0x02));
    }
}
