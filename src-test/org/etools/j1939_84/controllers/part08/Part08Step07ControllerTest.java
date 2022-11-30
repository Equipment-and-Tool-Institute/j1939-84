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
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part08Step07ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 7;

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

        instance = new Part08Step07Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNoDTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.7.2.a - No OBD ECU reported a permanent DTC");
    }

    @Test
    public void testFailureForDifferentDTCs() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var dtc2 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.7.2.b - Engine #1 (0) DM28 does not include the DM12 active DTC that the SA reported from earlier in this part.");
    }

    @Test
    public void testFailureForDifferentMIL() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.7.2.c - Engine #1 (0) reported different MIL status than DM12 response earlier in test 6.8.2");
    }

    @Test
    public void testWarningForMoreThanOneDTC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(345, 1, 0, 1);

        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.7.3.b - Engine #1 (0) reported more than one permanent DTC");
    }

    @Test
    public void testWarningForMoreThanOneDTCModuleReal() {
        // 14:25:20.5285 18FD803D [8] 03 FF 00 00 00 00 FF FF
        // 14:25:20.5320 18FD8001 [8] 47 FF EB 0D 04 01 FF FF

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0x3d);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0x3D, OFF, OFF, OFF, OFF), 8);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm28_0 = new DM28PermanentEmissionDTCPacket(Packet.parse("18FD803D [8] 03 FF 00 00 00 00 FF FF"));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28_0));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        var dtc_1 = DiagnosticTroubleCode.create(0x0DEB, 4, 0, 1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1), 8);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm28_1 = new DM28PermanentEmissionDTCPacket(Packet.parse("18FD8001 [8] 47 FF EB 0D 04 01 FF FF"));

        when(communicationsModule.requestDM28(any(), eq(0x3d))).thenReturn(BusResult.of(dm28_0));
        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.of(dm28_1));

        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_0, dm28_1));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0x3d));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testWarningForMoreThanOneDTCModule() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc_0 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc_0), 8);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm28_0 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc_0);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28_0));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        var dtc_1 = DiagnosticTroubleCode.create(234, 1, 0, 1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1), 8);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm28_1 = DM28PermanentEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc_1);

        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.of(dm28_1));

        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_0, dm28_1));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.7.3.a - More than on ECU reported a permanent DTC");
    }

    @Test
    public void testFailureForDifferenceGlobalVsDs() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28_1 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28_1));
        var dm28_2 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28_2));

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.7.5.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(any())).thenReturn(RequestResult.of(dm28));
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM28(any());
        verify(communicationsModule).requestDM28(any(), eq(0));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.7.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }
}
