/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket.PGN;
import static org.junit.Assert.assertEquals;
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
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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

@RunWith(MockitoJUnitRunner.class)
public class Part02Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 4;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

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
    private DiagnosticReadinessModule readinessModule;

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
                readinessModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                readinessModule,
                mockListener);
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

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false, packet));
        when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setIgnitionCycleCounterValue(1);
        var ratio = new PerformanceRatio(0x1111, 0xAAAA, 0xCCCC, 0x00);
        obdInfo.setPerformanceRatios(List.of(ratio));
        dataRepository.putObdModule(0, obdInfo);

        runTest();

        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));

        String expected = "FAIL: 6.2.4.2.b - ECU Engine #1 (0) reported a denominator that does not match denominator recorded in part 1"
                + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                Outcome.FAIL,
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

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1));

        var packet2 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet2));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setIgnitionCycleCounterValue(3);
        obdInfo.setPerformanceRatios(packet1.getRatios());
        dataRepository.putObdModule(0, obdInfo);

        runTest();

        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));

        String expected = "FAIL: 6.2.4.4.a - Difference compared to data received during global request from Engine #1 (0)" + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                Outcome.FAIL,
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

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false, packet));
        when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setIgnitionCycleCounterValue(1);
        obdInfo.setPerformanceRatios(packet.getRatios());
        dataRepository.putObdModule(0, obdInfo);

        runTest();

        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));

        String expected = "FAIL: 6.2.4.2.a - ECU Engine #1 (0) reported ignition cycle is invalid.  Expected 2 but was 4"
                + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                Outcome.FAIL,
                "6.2.4.2.a - ECU Engine #1 (0) reported ignition cycle is invalid.  Expected 2 but was 4");
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

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false, packet));
        when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        when(readinessModule.requestDM20(any(), eq(true), eq(0x17))).thenReturn(new BusResult<>(false));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setIgnitionCycleCounterValue(3);
        obdInfo.setPerformanceRatios(packet.getRatios());
        dataRepository.putObdModule(0, obdInfo);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(0x17);
        dataRepository.putObdModule(0x17, obdInfo2);

        runTest();

        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x17));

        String expected = "FAIL: 6.2.4.4.b - OBD module Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query"
                + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                Outcome.FAIL,
                "6.2.4.4.b - OBD module Instrument Cluster #1 (23) did not provide a response to Global query and did not provide a NACK for the DS query");
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

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false, packet));
        when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setIgnitionCycleCounterValue(1);
        var ratio = new PerformanceRatio(0x222222, 0xAAAA, 0xBBBB, 0x00);
        obdInfo.setPerformanceRatios(List.of(ratio));
        dataRepository.putObdModule(0, obdInfo);

        runTest();

        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));

        String expected = "FAIL: 6.2.4.2.a - ECU Engine #1 (0) reported different SPNs as supported for data than in part 1"
                + NL;
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                Outcome.FAIL,
                "6.2.4.2.a - ECU Engine #1 (0) reported different SPNs as supported for data than in part 1");
    }

    /**
     * Test method for {@link StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
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
     * Test method for {@link StepController#getTotalSteps()}.
     */
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
            when(readinessModule.requestDM20(any(), eq(true), eq(0x00)))
                    .thenReturn(new BusResult<>(false, packet));

            OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
            obdInfo.setIgnitionCycleCounterValue(1);
            obdInfo.setPerformanceRatios(packet.getRatios());
            dataRepository.putObdModule(0, obdInfo);
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
            when(readinessModule.requestDM20(any(), eq(true), eq(0x17)))
                    .thenReturn(new BusResult<>(false, packet));

            OBDModuleInformation obdInfo = new OBDModuleInformation(0x17);
            obdInfo.setPerformanceRatios(packet.getRatios());
            obdInfo.setIgnitionCycleCounterValue(0xA5A5 - 1);
            dataRepository.putObdModule(0x17, obdInfo);
        }

        when(readinessModule.requestDM20(any(), eq(true))).thenReturn(new RequestResult<>(false,
                packetList,
                List.of()));

        runTest();
        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x00));
        verify(readinessModule).requestDM20(any(), eq(true), eq(0x17));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    @Test
    public void testNoPackets() {
        when(readinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        runTest();
        verify(readinessModule).setJ1939(j1939);
        verify(readinessModule).requestDM20(any(), eq(true));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }
}
