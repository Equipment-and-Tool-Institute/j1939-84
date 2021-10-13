/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static net.soliddesign.j1939tools.j1939.packets.DM19CalibrationInformationPacket.PGN;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.bus.Packet;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

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

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

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
        dataRepository = DataRepository.newInstance();

        instance = new Part02Step05Controller(executor,
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
                                 mockListener,
                                 communicationsModule);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testRunHappyPathOneModuleThreeCalibration() {
        // formatter:off
        Packet packet = Packet.create(PGN,
                                      0x00,
                                      // Cal #1
                                      0x51,
                                      0xBA,
                                      0xFE,
                                      0xBD,
                                      0x41,
                                      0x4E,
                                      0x54,
                                      0x35,
                                      0x41,
                                      0x53,
                                      0x52,
                                      0x31,
                                      0x20,
                                      0x20,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,

                                      // Cal #2
                                      0x96,
                                      0xBF,
                                      0xDC,
                                      0x40,
                                      0x50,
                                      0x42,
                                      0x54,
                                      0x35,
                                      0x4D,
                                      0x50,
                                      0x52,
                                      0x33,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,

                                      // Cal #3
                                      0x40,
                                      0x91,
                                      0xB9,
                                      0x3E,
                                      0x52,
                                      0x50,
                                      0x52,
                                      0x42,
                                      0x42,
                                      0x41,
                                      0x31,
                                      0x30,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00,
                                      0x00);
        // formatter:on
        DM19CalibrationInformationPacket dm19 = new DM19CalibrationInformationPacket(packet);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(dm19, 1);
        dataRepository.putObdModule(obd0x00);

        when(communicationsModule.requestDM19(any(), eq(0x00))).thenReturn(BusResult.of(dm19));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(), eq(0));
    }

    /*
     * Make sure we respond correctly when no OBDModule in the repository
     */
    @Test
    public void testRunNoModulesRespond() {

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

    }

    @Test
    public void testRunWithWarningsAndFailures() {

        // formatter:off
        Packet packet0x00 = Packet.create(PGN,
                                          0x00,
                                          // Cal #1
                                          0x51,
                                          0xBA,
                                          0xFE,
                                          0xBD,
                                          0x41,
                                          0x4E,
                                          0x54,
                                          0x35,
                                          0x41,
                                          0x53,
                                          0x52,
                                          0x31,
                                          0x20,
                                          0x20,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,

                                          // Cal #2
                                          0x96,
                                          0xBF,
                                          0xDC,
                                          0x40,
                                          0x50,
                                          0x42,
                                          0x54,
                                          0x35,
                                          0x4D,
                                          0x50,
                                          0x52,
                                          0x33,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,

                                          // Cal #3
                                          0x40,
                                          0x91,
                                          0xB9,
                                          0x3E,
                                          0x52,
                                          0x50,
                                          0x52,
                                          0x42,
                                          0x42,
                                          0x41,
                                          0x31,
                                          0x30,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00);
        // formatter:on
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(new DM19CalibrationInformationPacket(packet0x00), 1);
        dataRepository.putObdModule(obd0x00);

        // formatter:off
        Packet packet0x01 = Packet.create(PGN,
                                          0x01,
                                          // Cal #1
                                          0x00,
                                          0xAC,
                                          0xFF,
                                          0x33,
                                          0x41,
                                          0x4E,
                                          0x54,
                                          0x35,
                                          0x41,
                                          0x53,
                                          0x52,
                                          0x31,
                                          0x20,
                                          0x20,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00);
        // formatter:on
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(new DM19CalibrationInformationPacket(packet0x01), 1);
        dataRepository.putObdModule(obd0x01);

        // formatter:off
        Packet packet0x02 = Packet.create(PGN,
                                          0x02,
                                          // Cal #1
                                          0x96,
                                          0xBF,
                                          0xDC,
                                          0x40,
                                          0x50,
                                          0x42,
                                          0x54,
                                          0x35,
                                          0x4D,
                                          0x50,
                                          0x52,
                                          0x33,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00,
                                          0x00);
        // formatter:on
        OBDModuleInformation obd0x02 = new OBDModuleInformation(0x02);
        obd0x02.set(new DM19CalibrationInformationPacket(packet0x02), 1);
        dataRepository.putObdModule(obd0x02);

        // formatter:off
        Packet packet0x01V2 = Packet.create(PGN,
                                            0x01,
                                            // Cal #1
                                            0x51,
                                            0xBA,
                                            0xFE,
                                            0xBD,
                                            0x41,
                                            0x4E,
                                            0x54,
                                            0x35,
                                            0x41,
                                            0x53,
                                            0x52,
                                            0x31,
                                            0x20,
                                            0x20,
                                            0x00,
                                            0x00,
                                            0x00,
                                            0x00,
                                            0x00,
                                            0x00);
        // formatter:on
        when(communicationsModule.requestDM19(any(), eq(0x00)))
                                                                   .thenReturn(BusResult.of(new DM19CalibrationInformationPacket(packet0x00)));

        when(communicationsModule.requestDM19(any(), eq(0x01)))
                                                                   .thenReturn(BusResult.of(new DM19CalibrationInformationPacket(packet0x01V2)));

        when(communicationsModule.requestDM19(any(), eq(0x02))).thenReturn(BusResult.empty());

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(communicationsModule).requestDM19(any(), eq(0x00));
        verify(communicationsModule).requestDM19(any(), eq(0x01));
        verify(communicationsModule).requestDM19(any(), eq(0x02));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.5.2.a - Engine #2 (1) reported CAL IDs/CVNs with different values/quantity than those reported in Part 1 data");
    }
}
