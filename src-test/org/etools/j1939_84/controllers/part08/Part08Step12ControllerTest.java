/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.create;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_ACT_REQ;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_ACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByte.CLR_PA_REQ;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.ACCESS_DENIED;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.DTC_NOT_PA;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.GENERAL_NACK;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.NOT_SUPPORTED;
import static org.etools.j1939tools.j1939.packets.DM22IndividualClearPacket.ControlByteSpecificIndicator.UNKNOWN_DTC;
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
import org.etools.j1939_84.controllers.SectionA5Verifier;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
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
public class Part08Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
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

    @Mock
    private SectionA5Verifier verifier;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part08Step12Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              verifier);

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
                                 mockListener,
                                 verifier);
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
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm22_1 = create(1, 0, CLR_ACT_NACK, GENERAL_NACK, 0x7FFFF, 31);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(dm22_1));

        var dm22_0 = create(0, 0, CLR_PA_NACK, GENERAL_NACK, 123, 10);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForCLR_PA_ACK() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm22_1 = create(1, 0, CLR_PA_ACK, NOT_SUPPORTED, 0x7FFFF, 31);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(dm22_1));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.2.a - Engine #2 (1) provided CLR_PA_ACK");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_ACT_ACK() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm22_1 = create(1, 0, CLR_ACT_ACK, NOT_SUPPORTED, 0x7FFFF, 31);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(dm22_1));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.2.a - Engine #2 (1) provided CLR_ACT_ACK");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForAck() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var ack = AcknowledgmentPacket.create(1, ACK);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(ack));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.2.b - Engine #2 (1) provided J1939-21 ACK for PGN 49920");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_ACT_NACKWithNonZeroAck() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm22_1 = create(1, 0, CLR_ACT_NACK, ACCESS_DENIED, 0x7FFFF, 31);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(dm22_1));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.2.c - Engine #2 (1) provided CLR_ACT_NACK with an acknowledgement code greater than 0");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_PA_NACKWithNonZeroAck() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm22_1 = create(1, 0, CLR_PA_NACK, ACCESS_DENIED, 0x7FFFF, 31);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(dm22_1));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.2.c - Engine #2 (1) provided CLR_PA_NACK with an acknowledgement code greater than 0");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testInfoForNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM22(any(),
                                              eq(1),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(BusResult.of(nack));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(1), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.b - Engine #2 (1) provided J1939-21 NACK for PGN 49920");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.3.a - Engine #2 (1) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_PA_ACK2() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm22_0 = create(0, 0, CLR_PA_ACK, NOT_SUPPORTED, 123, 10);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.5.a - Engine #1 (0) provided CLR_PA_ACK");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.6.a - Engine #1 (0) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_ACT_ACK2() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm22_0 = create(0, 0, CLR_ACT_ACK, NOT_SUPPORTED, 123, 10);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.5.a - Engine #1 (0) provided CLR_ACT_ACK");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.6.a - Engine #1 (0) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForACK2() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var ack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(ack));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.5.b - Engine #1 (0) provided J1939-21 ACK for PGN 49920");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.6.a - Engine #1 (0) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_ACT_NACKWithNonZeroAck2() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm22_0 = create(0, 0, CLR_ACT_NACK, ACCESS_DENIED, 123, 10);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.5.c - Engine #1 (0) provided CLR_ACT_NACK with an acknowledgement code greater than 0");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.6.a - Engine #1 (0) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testWarningForNACK() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 10, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var nack = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestDM22(any(),
                                              eq(0),
                                              eq(CLR_PA_REQ),
                                              eq(123),
                                              eq(10))).thenReturn(BusResult.of(nack));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(0), eq(CLR_PA_REQ), eq(123), eq(10));
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.8.12.6.b - Engine #1 (0) provided J1939-21 NACK for PGN 49920");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.8.12.6.a - Engine #1 (0) did not provide DM22 CLR_PA_NACK or CLR_ACT_NACK with acknowledgement code of 0");
    }

    @Test
    public void testFailureForCLR_PA_ACK3() {

        var dm22_0 = create(0, 0, CLR_PA_ACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.8.a - Engine #1 (0) provided DM22 with CLR_PA_ACK");
    }

    @Test
    public void testFailureForCLR_ACT_ACK3() {

        var dm22_0 = create(0, 0, CLR_ACT_ACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.8.a - Engine #1 (0) provided DM22 with CLR_ACT_ACK");
    }

    @Test
    public void testFailureForACK() {
        var ack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(new RequestResult<>(false, ack));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.8.b - Engine #1 (0) provided J1939-21 ACK for PGN 49920");
    }

    @Test
    public void testFailureForCLR_ACT_NACKWithNonZeroAck3() {

        var dm22_0 = create(0, 0, CLR_ACT_NACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.8.c - Engine #1 (0) provided CLR_ACT_NACK with an acknowledgement code greater than 0");
    }

    @Test
    public void testFailureForCLR_PA_NACKWithNonZeroAck3() {

        var dm22_0 = create(0, 0, CLR_PA_NACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.8.c - Engine #1 (0) provided CLR_PA_NACK with an acknowledgement code greater than 0");
    }

    @Test
    public void testFailureForCLR_PA_ACK4() {

        var dm22_0 = create(0, 0, CLR_PA_ACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.10.a - Engine #1 (0) provided DM22 with CLR_PA_ACK");
    }

    @Test
    public void testFailureForCLR_ACT_ACK4() {

        var dm22_0 = create(0, 0, CLR_ACT_ACK, NOT_SUPPORTED, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.10.a - Engine #1 (0) provided DM22 with CLR_ACT_ACK");
    }

    @Test
    public void testFailureForACK4() {
        var ack = AcknowledgmentPacket.create(0, ACK);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(new RequestResult<>(false, ack));

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.10.b - Engine #1 (0) provided J1939-21 ACK for PGN 49920");
    }

    @Test
    public void testFailureForCLR_ACT_NACKWithNonZeroAck4() {

        var dm22_0 = create(0, 0, CLR_ACT_NACK, UNKNOWN_DTC, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.10.c - Engine #1 (0) provided CLR_ACT_NACK with an acknowledgement code greater than 0");
    }

    @Test
    public void testFailureForCLR_PA_NACKWithNonZeroAck4() {

        var dm22_0 = create(0, 0, CLR_PA_NACK, DTC_NOT_PA, 0x7FFFF, 31);

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_PA_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of());

        when(communicationsModule.requestDM22(any(),
                                              eq(CLR_ACT_REQ),
                                              eq(0x7FFFF),
                                              eq(31))).thenReturn(RequestResult.of(dm22_0));

        runTest();

        verify(verifier).setJ1939(j1939);
        verify(communicationsModule).requestDM22(any(), eq(CLR_PA_REQ), eq(0x7FFFF), eq(31));
        verify(communicationsModule).requestDM22(any(), eq(CLR_ACT_REQ), eq(0x7FFFF), eq(31));

        verify(verifier).verifyDataNotErased(any(), eq("6.8.12.10.d"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.12.10.c - Engine #1 (0) provided CLR_PA_NACK with an acknowledgement code greater than 0");
    }
}
