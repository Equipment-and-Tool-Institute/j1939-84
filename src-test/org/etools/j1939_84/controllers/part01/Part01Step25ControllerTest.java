/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
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
    private static final int STEP_NUMBER = 25;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

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
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step25Controller(executor,
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
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.set(packet, 1);
        dataRepository.putObdModule(obd);

        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       WARN,
                                                       "Engine #1 (0) did not respond to the DS DM20 request");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
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
    public void testMultipleObdModules() {

        int[] data = {
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
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

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
                0x00, // Applicable System Monitor Denominator
        };
        var packet1 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x01, data1));

        int[] data2 = new int[] { 0x0C, 0x00, 0x01, 0x00,
                // One
                0xCA, 0x14, 0xF8, 0x00, 0x00, 0x01, 0x00,
                // Two
                0xB8, 0x12, 0xF8, 0x03, 0x00, 0x04, 0x00,
                // Three
                0xBC, 0x14, 0xF8, 0x05, 0x00, 0x06, 0x00 };
        var packet2 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x02, data2));

        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));
        when(communicationsModule.requestDM20(any(), eq(0x01))).thenReturn(BusResult.of(packet1));
        when(communicationsModule.requestDM20(any(), eq(0x02))).thenReturn(BusResult.of(packet2));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.set(packet, 1);
        dataRepository.putObdModule(obd);

        OBDModuleInformation obd1 = new OBDModuleInformation(0x01);
        obd1.set(packet1, 1);
        dataRepository.putObdModule(obd1);

        OBDModuleInformation obd2 = new OBDModuleInformation(0x02);
        obd2.set(packet2, 1);
        dataRepository.putObdModule(obd2);

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));
        verify(communicationsModule).requestDM20(any(), eq(0x01));
        verify(communicationsModule).requestDM20(any(), eq(0x02));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

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
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.set(packet, 1);
        dataRepository.putObdModule(obd);

        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

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
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

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
        var packet1 = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x01, data1));

        // Make the ratios from the dataRepository different from those in the
        // packet return via requestDM20
        OBDModuleInformation obd = new OBDModuleInformation(0x00);
        obd.set(packet1, 1);
        dataRepository.putObdModule(obd);

        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.25.2.b - Difference compared to data received during global request earlier"
                                                               + NL
                                                               + "Engine #1 (0) had a difference between stored performance ratios and DS requested DM20 response ratios");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

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
                0xFF, // Applicable System Monitor Numerator
                0xFE, // Applicable System Monitor Numerator
                0xFF, // Applicable System Monitor Denominator
                0xFF // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));
        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(1, ACK);
        when(communicationsModule.requestDM20(any(), eq(0x01))).thenReturn(BusResult.of(ackPacket));

        AcknowledgmentPacket nackPacket = AcknowledgmentPacket.create(2, NACK);
        when(communicationsModule.requestDM20(any(), eq(0x02))).thenReturn(BusResult.of(nackPacket));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.set(packet, 1);
        dataRepository.putObdModule(obdInfo);

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0x01);
        obdInfo1.set(packet, 1);
        dataRepository.putObdModule(obdInfo1);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(0x02);
        obdInfo2.set(packet, 1);
        dataRepository.putObdModule(obdInfo2);

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));
        verify(communicationsModule).requestDM20(any(), eq(0x01));
        verify(communicationsModule).requestDM20(any(), eq(0x02));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.25.2.c - NACK not received from OBD ECUs that did not respond to global query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

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
                0xFF, // Applicable System Monitor Numerator
                0xFE, // Applicable System Monitor Numerator
                0xFF, // Applicable System Monitor Denominator
                0xFF // Applicable System Monitor Denominator
        };

        // 0xA5, 0xA5, 0x5A, 0x5A, 0xFF, 0xFF, 0xFF, 0xFF, 0xFE, 0xFF, 0xFF
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));

        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(new BusResult<>(true, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.set(packet, 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.25.2.a - Retry was required to obtain DM20 response:"
                                                + NL + "Engine #1 (0) required a retry when DS requesting DM20");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

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
                0xFF, // Applicable System Monitor Numerator
                0xFE, // Applicable System Monitor Numerator
                0xFF, // Applicable System Monitor Denominator
                0xFF // Applicable System Monitor Denominator
        };
        var packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));
        when(communicationsModule.requestDM20(any(), eq(0x00))).thenReturn(BusResult.of(packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0x00);
        obdInfo.set(packet, 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM20(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
