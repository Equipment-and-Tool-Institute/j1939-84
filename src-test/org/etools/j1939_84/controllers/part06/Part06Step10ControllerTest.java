/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;
@RunWith(MockitoJUnitRunner.class)
public class Part06Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 10;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part06Step10Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
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
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDistanceGreaterThanZero() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 1, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.10.2.a - Engine #1 (0) reported distance with MIL on > 0");
    }

    @Test
    public void testFailureForDistanceNotSupported() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0xFFFF, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.10.2.a - Engine #1 (0) reported distance with MIL on is not supported");
    }

    @Test
    public void testFailureForNoDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.10.2.b - Engine #1 (0) reported with with MIL on > 0 minutes, and did not report a DTC in its DM12 response");
    }

    @Test
    public void testFailureNoSupport() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var nack = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.6.10.2.c - No ECU supports DM21");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.10.3.a - No ECU reported time with MIL on > 0 minutes");
    }

    @Test
    public void testFailureForNoNACK() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.10.2.d - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForNoMILOnGreaterThanZero() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.10.3.a - No ECU reported time with MIL on > 0 minutes");
    }

    @Test
    public void testFailureForTimeDifference() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        dataRepository.putObdModule(obdModuleInformation);

        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        var dtc_1 = DiagnosticTroubleCode.create(234, 12, 0, 1);

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1), 6);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm21_1 = DM21DiagnosticReadinessPacket.create(1, 0, 0, 0, 3, 0);
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(BusResult.of(dm21_1));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.6.10.3.b - More than one ECU reported time with MIL on > 0 and difference between the times reported is > 1 minute");
    }

}
