/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
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
public class Part03Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 5;

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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step05Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
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
    public void testNoModules() {

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoDTCs() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

    }

    @Test
    public void testNoFailuresWithPacketOff() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm31 = DM31DtcToLampAssociation.create(0, DTCLampStatus.create(dtc, OFF, OFF, OFF, OFF));
        when(diagnosticMessageModule.requestDM31(any(), eq(0))).thenReturn(new RequestResult<>(false, dm31));

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setEmissionDTCs(List.of(dtc));
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFailuresWithPacketAlternativeOff() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm31 = DM31DtcToLampAssociation.create(0, DTCLampStatus.create(dtc, OFF, ALTERNATE_OFF, OFF, OFF));
        when(diagnosticMessageModule.requestDM31(any(), eq(0))).thenReturn(new RequestResult<>(false, dm31));

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setEmissionDTCs(List.of(dtc));
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFailuresWithNack() {
        var ack = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM31DtcToLampAssociation.PGN);
        when(diagnosticMessageModule.requestDM31(any(), eq(0))).thenReturn(new RequestResult<>(false, ack));

        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setEmissionDTCs(List.of(dtc));
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoNack() {
        when(diagnosticMessageModule.requestDM31(any(), eq(0))).thenReturn(new RequestResult<>(true));

        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setEmissionDTCs(List.of(dtc));
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.3.5.2.b - Engine #1 (0) did not provide a DM31 and did not NACK the request" + NL,
                     listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.5.2.b - Engine #1 (0) did not provide a DM31 and did not NACK the request");
    }

    @Test
    public void testFailureForMilNotOff() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm31 = DM31DtcToLampAssociation.create(0, DTCLampStatus.create(dtc, OFF, ON, OFF, OFF));
        when(diagnosticMessageModule.requestDM31(any(), eq(0))).thenReturn(new RequestResult<>(false, dm31));

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setEmissionDTCs(List.of(dtc));
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.3.5.2.a - Engine #1 (0) did not report MIL 'off' in all returned DTCs" + NL,
                     listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.5.2.a - Engine #1 (0) did not report MIL 'off' in all returned DTCs");
    }

}
