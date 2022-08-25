/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 4;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step04Controller instance;

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
        dataRepository = DataRepository.newInstance();
        DateTimeModule.setInstance(null);

        instance = new Part02Step04Controller(executor,
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
                                 mockListener,
                                 communicationsModule);
    }

    @Test
    public void testDifferentDenominatorsFailures() {

        int[] data = {
                0x02, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x00, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(packet));
        when(communicationsModule.requestDM20(any(), eq(0x00)))
                                                                  .thenReturn(BusResult.of(packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        var ratio = new PerformanceRatio(0x1111, 0xAAAA, 0xCCCC, 0x00);
        obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 0, ratio), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.4.2.b - ECU Engine #1 (0) reported a denominator that does not match denominator recorded in part 1");
    }

    @Test
    public void testDifferentGlobalVersusDSFailures() {
        int[] data1 = {
                0x04, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };
        var packet1 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data1));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(packet1));

        var packet2 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet2));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        PerformanceRatio[] ratios = packet1.getRatios().toArray(new PerformanceRatio[0]);
        obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 1, ratios), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.4.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testDifferentIgnitionCyclesFailures() {
        int[] data = {
                0x04, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(packet));
        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        PerformanceRatio[] ratios = packet.getRatios().toArray(new PerformanceRatio[0]);
        obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratios), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.4.2.c - Engine #1 (0) reported value for ignition cycle is not one cycle greater than the value reported in part 1");
    }

    @Test
    public void testDifferentNoDSNackFailure() {
        int[] data1 = {
                0x04, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data1));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(packet));
        when(communicationsModule.requestDM20(any(), eq(0x00)))
                                                                  .thenReturn(BusResult.of(packet));

        when(communicationsModule.requestDM20(any(), eq(0x17))).thenReturn(BusResult.empty());

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        PerformanceRatio[] ratios = packet.getRatios().toArray(new PerformanceRatio[0]);
        obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 3, 1, ratios), 1);
        dataRepository.putObdModule(obdInfo);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(0x17);
        dataRepository.putObdModule(obdInfo2);

        runTest();

        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));
        verify(communicationsModule).requestDM20(any(), eq(0x17));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.4.4.b - OBD ECU Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testDifferentSPNFailures() {

        int[] data = {
                0x02, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(packet));
        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        var ratio = new PerformanceRatio(0x222222, 0xAAAA, 0xBBBB, 0x00);
        obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratio), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.4.2.a - ECU Engine #1 (0) reported different SPNs as supported for data than in part 1");
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
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
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoFailures() {
        var packetList = new ArrayList<DM20MonitorPerformanceRatioPacket>();

        {
            int[] data = {
                    0x02, // Ignition Cycle Counter
                    0x00, // Ignition Cycle Counter
                    0x5A, // OBD Monitoring Conditions Encountered
                    0x5A, // OBD Monitoring Conditions Encountered

                    0x11, // SPN of Applicable System Monitor
                    0x11, // SPN of Applicable System Monitor
                    0x11, // SPN of Applicable System Monitor
                    0xAA, // Applicable System Monitor Numerator
                    0xAA, // Applicable System Monitor Numerator
                    0xBB, // Applicable System Monitor Denominator
                    0xBB, // Applicable System Monitor Denominator

                    0x22, // SPN of Applicable System Monitor
                    0x22, // SPN of Applicable System Monitor
                    0x22, // SPN of Applicable System Monitor
                    0xCC, // Applicable System Monitor Numerator
                    0xCC, // Applicable System Monitor Numerator
                    0xDD, // Applicable System Monitor Denominator
                    0xDD, // Applicable System Monitor Denominator
            };
            var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));
            packetList.add(packet);
            when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

            OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
            PerformanceRatio[] ratios = packet.getRatios().toArray(new PerformanceRatio[0]);
            obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 1, ratios), 1);
            dataRepository.putObdModule(obdInfo);
        }

        {
            int[] data = {
                    0xA5, // Ignition Cycle Counter
                    0xA5, // Ignition Cycle Counter
                    0x5A, // OBD Monitoring Conditions Encountered
                    0x5A, // OBD Monitoring Conditions Encountered

                    0x33, // SPN of Applicable System Monitor
                    0x33, // SPN of Applicable System Monitor
                    0x33, // SPN of Applicable System Monitor
                    0x44, // Applicable System Monitor Numerator
                    0x44, // Applicable System Monitor Numerator
                    0x55, // Applicable System Monitor Denominator
                    0x55, // Applicable System Monitor Denominator

                    0x66, // SPN of Applicable System Monitor
                    0x66, // SPN of Applicable System Monitor
                    0x66, // SPN of Applicable System Monitor
                    0x77, // Applicable System Monitor Numerator
                    0x77, // Applicable System Monitor Numerator
                    0x88, // Applicable System Monitor Denominator
                    0x88, // Applicable System Monitor Denominator
            };
            var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x17, data));
            packetList.add(packet);
            when(communicationsModule.requestDM20(any(), eq(0x17))).thenReturn(BusResult.of(packet));

            OBDModuleInformation obdInfo = new OBDModuleInformation(0x17);
            PerformanceRatio[] ratios = packet.getRatios().toArray(new PerformanceRatio[0]);
            obdInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 0xA5A5 - 1, 1, ratios), 1);
            dataRepository.putObdModule(obdInfo);
        }

        when(communicationsModule.requestDM20(any())).thenReturn(new RequestResult<>(false, packetList, List.of()));

        runTest();

        verify(communicationsModule).requestDM20(any());
        verify(communicationsModule).requestDM20(any(), eq(0x00));
        verify(communicationsModule).requestDM20(any(), eq(0x17));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoPackets() {
        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).requestDM20(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }
}
