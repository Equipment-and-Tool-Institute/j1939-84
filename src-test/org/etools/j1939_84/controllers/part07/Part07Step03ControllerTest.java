/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.LampStatus.NOT_SUPPORTED;
import static net.solidDesign.j1939.packets.LampStatus.OFF;
import static net.solidDesign.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DM2PreviouslyActiveDTC;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
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

@RunWith(MockitoJUnitRunner.class)
public class Part07Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 3;

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

        instance = new Part07Step03Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM2(any(), eq(1))).thenReturn(BusResult.of(nack));

        var dm2_2 = DM2PreviouslyActiveDTC.create(2, NOT_SUPPORTED, NOT_SUPPORTED, NOT_SUPPORTED, NOT_SUPPORTED);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0, dm2_2));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));
        verify(communicationsModule).requestDM2(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoPackets() {
        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).requestDM2(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoDTCs() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        var dm2_2 = DM2PreviouslyActiveDTC.create(2, OFF, OFF, OFF, OFF, dtc);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0, dm2_2));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.2.a - No OBD ECU reported previously active DTC(s)");
    }

    @Test
    public void testFailureForDifferentSizeDTCs() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);
        var dtc2 = DiagnosticTroubleCode.create(423, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc, dtc2), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.2.b - Engine #1 (0) reported fewer previously active DTCs than in DM23 response earlier in this part");
    }

    @Test
    public void testFailureForDifferentDTCInDM12() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dtc2 = DiagnosticTroubleCode.create(423, 11, 1, 19);
        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.2.c - Engine #1 (0) DM2 response does not include SPN = 123, FMI = 11 in the previous DM12 response");
    }

    @Test
    public void testFailureForOBDMilNotOff() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.2.d - Engine #1 (0) did not report MIL 'off'");

    }

    @Test
    public void testFailureForNonOBDMILNotOff() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        var dm2_2 = DM2PreviouslyActiveDTC.create(2, ON, OFF, OFF, OFF);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0, dm2_2));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.2.e - Turbocharger (2) did not report MIL off or not supported");

    }

    @Test
    public void testFailureForDifference() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 6);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 7);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        var dm2_0_2 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0_2));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.4.a - Difference compared to data received during global request from Engine #1 (0)");

    }

    @Test
    public void testFailureForNoNack() {
        var dtc = DiagnosticTroubleCode.create(123, 11, 1, 19);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        var dm2_0 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2_0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM2(any(), eq(1))).thenReturn(BusResult.empty());

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0));

        runTest();

        verify(communicationsModule).requestDM2(any());
        verify(communicationsModule).requestDM2(any(), eq(0));
        verify(communicationsModule).requestDM2(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.3.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

}
