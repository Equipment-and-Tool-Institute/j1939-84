/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
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
public class Part11Step09ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 9;

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

        instance = new Part11Step09Controller(executor,
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
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 1);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 10);
        dataRepository.putObdModule(obdModuleInformation);

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 2);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM28(any(), eq(0));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoDTCs() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 10);
        dataRepository.putObdModule(obdModuleInformation);

        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.11.9.2.a - No ECU reported a permanent DTC");
    }

    @Test
    public void testFailureForDifferentDTCs() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 1);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 10);
        dataRepository.putObdModule(obdModuleInformation);

        var dtc1 = DiagnosticTroubleCode.create(456, 12, 0, 2);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(communicationsModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.9.2.b - Engine #1 (0) reported a different DTCs than as reported in DM28 in part 10");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 1);
        obdModuleInformation.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 10);
        dataRepository.putObdModule(obdModuleInformation);

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 2);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM28(any(), eq(1))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM28(any(), eq(0));
        verify(communicationsModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.9.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
