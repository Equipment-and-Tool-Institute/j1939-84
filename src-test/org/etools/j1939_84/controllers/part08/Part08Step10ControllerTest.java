/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part08Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
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

        instance = new Part08Step10Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff1, ff2);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM25(any(), eq(1), any())).thenReturn(BusResult.of(nack));

        dataRepository.putObdModule(new OBDModuleInformation(2));
        var dm25_2 = DM25ExpandedFreezeFrame.create(2);
        when(communicationsModule.requestDM25(any(), eq(2), any())).thenReturn(BusResult.of(dm25_2));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(communicationsModule).requestDM25(any(), eq(1), any());
        verify(communicationsModule).requestDM25(any(), eq(2), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForFreezeFrameWithoutDTC23() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);
        var dtc3 = DiagnosticTroubleCode.create(987, 9, 0, 1);
        var ff3 = new FreezeFrame(dtc3, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff1, ff3);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.10.3.a - DTC(s) reported by DM23 earlier in this part is/are not present in the freeze frame data from Engine #1 (0)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.a - DTC(s) reported in the freeze frame by Engine #1 (0) did not include either the DTC reported in DM12 or DM23 earlier in this part");
    }

    @Test
    public void testFailureForFreezeFrameWithoutDTC12() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);
        var dtc3 = DiagnosticTroubleCode.create(987, 9, 0, 1);
        var ff3 = new FreezeFrame(dtc3, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff2, ff3);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.a - DTC(s) reported in the freeze frame by Engine #1 (0) did not include either the DTC reported in DM12 or DM23 earlier in this part");
    }

    @Test
    public void testFailureForFreezeFrameWithDTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);
        var dtc3 = DiagnosticTroubleCode.create(987, 9, 0, 1);
        var ff3 = new FreezeFrame(dtc3, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff1, ff2, ff3);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForFreezeFrameWithoutDTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);
        var dtc3 = DiagnosticTroubleCode.create(987, 9, 0, 1);
        var ff3 = new FreezeFrame(dtc3, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff3);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.10.3.a - DTC(s) reported by DM23 earlier in this part is/are not present in the freeze frame data from Engine #1 (0)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.a - DTC(s) reported in the freeze frame by Engine #1 (0) did not include either the DTC reported in DM12 or DM23 earlier in this part");
    }

    @Test
    public void testFailureForNoFreezeFrames() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm25 = DM25ExpandedFreezeFrame.create(0);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.b - No ECU provided freeze frame data");
    }

    @Test
    public void testFailureForNoNACK() {

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var ff2 = new FreezeFrame(dtc2, new int[0]);

        var dm25_0 = DM25ExpandedFreezeFrame.create(0, ff1, ff2);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25_0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM25(any(), eq(1), any())).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(communicationsModule).requestDM25(any(), eq(1), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");

    }

    @Test
    public void testWarningForMissingDM23DTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 4, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(456, 4, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var ff1 = new FreezeFrame(dtc1, new int[0]);
        var dm25 = DM25ExpandedFreezeFrame.create(0, ff1);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.10.3.a - DTC(s) reported by DM23 earlier in this part is/are not present in the freeze frame data from Engine #1 (0)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.10.2.a - DTC(s) reported in the freeze frame by Engine #1 (0) did not include either the DTC reported in DM12 or DM23 earlier in this part");
    }

}
