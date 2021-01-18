/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.*;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step25Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step25ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM20MonitorPerformanceRatioPacket.PGN;
    private static final int STEP_NUMBER = 25;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step25Controller instance;

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
        DateTimeModule.setInstance(null);

        instance = new Part01Step25Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticReadinessModule,
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
                dataRepository,
                diagnosticReadinessModule,
                mockListener);
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testEmptyPacket() {

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State
        };
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.setPerformanceRatios(packet.getRatios());
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obd));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "Engine #1 (0) did not response to the DS20 request");

        String expectedResults = "WARN: Engine #1 (0) did not response to the DS20 request" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
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

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testMultipleObdModules() {

        int[] data = {
                0xA5, // Ignition Cycle Counter
                0xA5, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // Appplicable System Monitor Numerator
                0xFE, // Appplicable System Monitor Numerator
                0xFF, // Appplicable System Monitor Denominator
                0xFF // Appplicable System Monitor Denominator
        };

        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        int[] data1 = {
                0x00, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x00, // OBD Monitoring Conditions Encountered
                0x00, // OBD Monitoring Conditions Encountered
                0x00, // SPN of Applicable System Monitor
                0x00, // SPN of Applicable System Monitor
                0x00, // SPN of Applicable System Monitor
                0x00, // Appplicable System Monitor Numerator
                0x00, // Appplicable System Monitor Numerator
                0x00, // Appplicable System Monitor Denominator
                0x00, // Appplicable System Monitor Denominator
        };
        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x01, data1));

        int[] data2 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x01, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00,
                // Three
                0xBC, 0x14, 0xF8, 0x05, 0x00, 0x06, 0x00 };
        DM20MonitorPerformanceRatioPacket packet2 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x02, data2));

        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet1));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet2));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.setPerformanceRatios(packet.getRatios());
        OBDModuleInformation obd1 = new OBDModuleInformation(0x01);
        obd1.setPerformanceRatios(packet1.getRatios());
        OBDModuleInformation obd2 = new OBDModuleInformation(0x02);
        obd2.setPerformanceRatios(packet2.getRatios());
        when(dataRepository.getObdModules()).thenReturn(new ArrayList<>(Arrays.asList(obd, obd1, obd2)));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x01));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x02));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testNoFail() {

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State
        };
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.setPerformanceRatios(packet.getRatios());
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obd));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testObdInfoDifferentFail() {

        int[] data = new int[] {
                0xA5, // Ignition Cycle Counter
                0xA5, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // Applicable System Monitor Numerator
                0xFE, // Applicable System Monitor Numerator
                0xFF, // Applicable System Monitor Denominator
                0xFF // Applicable System Monitor Denominator
        };
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        int[] data1 = {
                0x00, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x00, // OBD Monitoring Conditions Encountered
                0x00, // OBD Monitoring Conditions Encountered
                0x00, // SPN of Applicable System Monitor
                0x00, // SPN of Applicable System Monitor
                0x00, // SPN of Applicable System Monitor
                0x00, // Applicable System Monitor Numerator
                0x00, // Applicable System Monitor Numerator
                0x00, // Applicable System Monitor Denominator
                0x00 // Applicable System Monitor Denominator
        };
        DM20MonitorPerformanceRatioPacket packet1 = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x01, data1));

        // Make the ratios from the dataRepository different from those in the
        // packet return via requestDM20
        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.setPerformanceRatios(packet1.getRatios());
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obd));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x01);
        obdInfo.setPerformanceRatios(packet1.getRatios());

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.25.2.b - Difference compared to data received during global request earlier"
                        + NL
                        + "Engine #1 (0) had a difference between stored performance ratios and destination specific requested DM20 response ratios");

        String expectedResults = "FAIL: 6.1.25.2.b - Difference compared to data received during global request earlier"
                + NL
                + "Engine #1 (0) had a difference between stored performance ratios and destination specific requested DM20 response ratios"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testResponseIsNotNack() {

        int[] data = {
                0xA5, // Ignition Cycle Counter
                0xA5, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // Appplicable System Monitor Numerator
                0xFE, // Appplicable System Monitor Numerator
                0xFF, // Appplicable System Monitor Denominator
                0xFF // Appplicable System Monitor Denominator
        };
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        AcknowledgmentPacket ackPacket = mock(AcknowledgmentPacket.class);
        when(ackPacket.getResponse()).thenReturn(ACK);
        AcknowledgmentPacket nackPacket = mock(AcknowledgmentPacket.class);
        when(nackPacket.getResponse()).thenReturn(NACK);

        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, ackPacket));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x02)))
                .thenReturn(new BusResult<>(false, nackPacket));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setPerformanceRatios(packet.getRatios());
        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0x01);
        obdInfo1.setPerformanceRatios(packet.getRatios());
        OBDModuleInformation obdInfo2 = new OBDModuleInformation(0x02);
        obdInfo2.setPerformanceRatios(packet.getRatios());
        when(dataRepository.getObdModules()).thenReturn(new ArrayList<>(Arrays.asList(obdInfo, obdInfo1, obdInfo2)));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x01));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x02));

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.25.2.c - NACK not received from OBD ECUs that did not respond to global query");

        String expectedResult = "FAIL: 6.1.25.2.c - NACK not received from OBD ECUs that did not respond to global query"
                + NL;
        assertEquals(expectedResult, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testRetryUsed() {

        int[] data = {
                0xA5, // Ignition Cycle Counter
                0xA5, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // Appplicable System Monitor Numerator
                0xFE, // Appplicable System Monitor Numerator
                0xFF, // Appplicable System Monitor Denominator
                0xFF // Appplicable System Monitor Denominator
        };

        // 0xA5, 0xA5, 0x5A, 0x5A, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0xFF, 0xFF
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));

        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(true, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setPerformanceRatios(packet.getRatios());
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));

        verify(mockListener).addOutcome(PART_NUMBER,
                STEP_NUMBER,
                FAIL,
                "6.1.25.2.a - Retry was required to obtain DM20 response:"
                        + NL + "Engine #1 (0) required a retry when requesting its destination specific DM20");

        String expectedResult = "FAIL: 6.1.25.2.a - Retry was required to obtain DM20 response:" + NL
                + "Engine #1 (0) required a retry when requesting its destination specific DM20" + NL;
        assertEquals(expectedResult, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for {@link Part01Step25Controller#run()}.
     */
    @Test
    public void testRun() {

        int[] data = {
                0xA5, // Ignition Cycle Counter
                0xA5, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // SPN of Applicable System Monitor
                0xFF, // Appplicable System Monitor Numerator
                0xFE, // Appplicable System Monitor Numerator
                0xFF, // Appplicable System Monitor Denominator
                0xFF // Appplicable System Monitor Denominator
        };
        DM20MonitorPerformanceRatioPacket packet = new DM20MonitorPerformanceRatioPacket(
                Packet.create(PGN, 0x00, data));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.setPerformanceRatios(packet.getRatios());
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

}
