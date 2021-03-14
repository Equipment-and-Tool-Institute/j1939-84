/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part09Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 12;

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

        instance = new Part09Step12Controller(executor,
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
    public void testHappyPathNoFailures() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 3);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 9);
        dataRepository.putObdModule(obdModule0);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM28(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any(), eq(0));
        verify(diagnosticMessageModule).requestDM28(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testWarningAltOff() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 3);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, ALTERNATE_OFF, OFF, OFF, OFF, dtc), 9);
        dataRepository.putObdModule(obdModule0);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ALTERNATE_OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any(), eq(0));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

    }

    @Test
    public void testFailureForDifferentFromDM12() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 3);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.9.12.3.a - Engine #1 (0) reported different DTC than DM12 response earlier in step 6.9.2.1.b");
    }

    @Test
    public void testFailureForMILNotOff() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 3);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 9);
        dataRepository.putObdModule(obdModule0);

        var dm28 = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        runTest();

        verify(diagnosticMessageModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.12.2.b - Engine #1 (0) did not report MIL 'off'");
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(diagnosticMessageModule.requestDM28(any(), eq(0))).thenReturn(BusResult.empty());

        runTest();

        verify(diagnosticMessageModule).requestDM28(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.12.2.a - No OBD ECU reported a permanent DTC");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.12.2.c - OBD module Engine #1 (0) did not provide a NACK for the DS query");
    }

}
