/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

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
public class Part07Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 2;

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

        instance = new Part07Step02Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForDTCs() {
        var dtc = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        var dtc23 = DiagnosticTroubleCode.create(123, 12, 0, 4);
        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc23);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.2.2.b - OBD ECU Engine #1 (0) reported a different DTCs from the DM12 DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.2.2.c - OBD ECU Engine #1 (0) did not report MIL off and not flashing");
    }

    @Test
    public void testFailureForMoreThanOneDtc() {
        var dtc = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc, dtc1);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc, dtc1);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.7.2.3.c - OBD ECU Engine #1 (0) reported > 1 previously active DTCs");
    }

    @Test
    public void testFailureForDtcDuplicate() {
        var dtc = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        var dm6_1 = DM12MILOnEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(dm6_1, 6);
        dataRepository.putObdModule(obdModule1);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        var dm23_1 = DM23PreviouslyMILOnEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(BusResult.of(dm23_1));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.7.2.3.b - More than one ECU reported previously active DTC");
    }

    @Test
    public void testFailureForEmptyDtcs() {
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.2.2.a - No OBD ECU reported a previously active DTC");
    }

    @Test
    public void testFailureForNoNACK() {
        var dtc = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(dm12, 6);
        dataRepository.putObdModule(obdModule);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(BusResult.of(dm23));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dm6_1 = DM12MILOnEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1);
        obdModule.set(dm6_1, 6);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.2.2.d - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");

    }
}
