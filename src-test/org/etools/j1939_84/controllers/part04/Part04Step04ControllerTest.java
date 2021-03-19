/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
public class Part04Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 4;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

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

        instance = new Part04Step04Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
    public void testNoFailures() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm2 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));

        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM2(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));
        verify(diagnosticMessageModule).requestDM2(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoNack() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm2 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));

        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2));

        when(diagnosticMessageModule.requestDM2(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));
        verify(diagnosticMessageModule).requestDM2(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.4.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForDTC() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 4);
        dataRepository.putObdModule(obdModule0);

        var dm2 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.4.2.a - OBD ECU Engine #1 (0) reported > 0 previously active DTCs");
    }

    @Test
    public void testFailureForMILNotOff() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        dataRepository.putObdModule(obdModule0);

        var dm2 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dm2));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.4.2.b - OBD ECU Engine #1 (0) reported a MIL status differing from DM12 response earlier in this part");
    }

    @Test
    public void testFailureGlobalAndDSDifference() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        dataRepository.putObdModule(obdModule0);

        var globalDM2 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(globalDM2));

        var dsDM2 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(dsDM2));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.4.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

}
