/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
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
public class Part07Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 12;

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

        instance = new Part07Step12Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(122, 3, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);
        var freezeFrame = new FreezeFrame(dtc, new int[0]);
        var dm25 = DM25ExpandedFreezeFrame.create(0, freezeFrame);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM25(any(), eq(1), any())).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());
        verify(communicationsModule).requestDM25(any(), eq(1), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNoFreezeFrames() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM25(any(), eq(1), any())).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(1), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.12.2.a - No ECU reported Freeze Frame data");
    }

    @Test
    public void testFailureForMissingDTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(122, 3, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 7);
        dataRepository.putObdModule(obdModuleInformation);
        var dtc2 = DiagnosticTroubleCode.create(234, 3, 1, 1);
        var freezeFrame = new FreezeFrame(dtc2, new int[0]);
        var dm25 = DM25ExpandedFreezeFrame.create(0, freezeFrame);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.12.2.b - Engine #1 (0) did not reported DTC in Freeze Frame data which included the DTC provided by DM23 earlier in this part");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(122, 3, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);
        var freezeFrame = new FreezeFrame(dtc, new int[0]);
        var dm25 = DM25ExpandedFreezeFrame.create(0, freezeFrame);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

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
                                        "6.7.12.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testWarningForMultipleFreezeFrames() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(122, 3, 1, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var freezeFrame1 = new FreezeFrame(dtc1, new int[0]);
        var dtc2 = DiagnosticTroubleCode.create(234, 3, 1, 1);
        var freezeFrame2 = new FreezeFrame(dtc2, new int[0]);
        var dm25 = DM25ExpandedFreezeFrame.create(0, freezeFrame1, freezeFrame2);
        when(communicationsModule.requestDM25(any(), eq(0), any())).thenReturn(BusResult.of(dm25));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.7.12.3.a - Engine #1 (0) reported more than one Freeze Frame");
    }

}
