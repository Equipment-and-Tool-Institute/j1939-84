/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.model.Outcome.FAIL;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DTCLampStatus;
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
public class Part09Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 13;

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

        instance = new Part09Step13Controller(executor,
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
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc0 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 9);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm31_0 = DM31DtcToLampAssociation.create(0, 0, DTCLampStatus.create(dtc0, OFF, OFF, OFF, OFF));
        when(communicationsModule.requestDM31(any(), eq(0))).thenReturn(RequestResult.of(dm31_0));

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        var dtc1 = DiagnosticTroubleCode.create(456, 9, 0, 1);
        obdModuleInformation1.set(DM28PermanentEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1), 9);
        dataRepository.putObdModule(obdModuleInformation1);
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM31(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        // Module 2 doesn't have any DTCs
        OBDModuleInformation obdModuleInformation2 = new OBDModuleInformation(2);
        obdModuleInformation2.set(DM28PermanentEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF), 9);
        dataRepository.putObdModule(obdModuleInformation2);

        // Module 3 doesn't support DM28
        dataRepository.putObdModule(new OBDModuleInformation(3));

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0));
        verify(communicationsModule).requestDM31(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForMILNotOff() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc0 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 9);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm31_0 = DM31DtcToLampAssociation.create(0, 0, DTCLampStatus.create(dtc0, OFF, ON, OFF, OFF));
        when(communicationsModule.requestDM31(any(), eq(0))).thenReturn(RequestResult.of(dm31_0));

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.13.2.a - Engine #1 (0) MIL is not reported off for all reported DTCs");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc0 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc0), 9);
        dataRepository.putObdModule(obdModuleInformation0);
        when(communicationsModule.requestDM31(any(), eq(0))).thenReturn(RequestResult.of());

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.13.2.b - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }

}
