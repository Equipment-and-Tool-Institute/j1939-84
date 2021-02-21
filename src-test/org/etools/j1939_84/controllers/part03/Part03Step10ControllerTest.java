/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
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
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
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
public class Part03Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 10;

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
        instance = new Part03Step10Controller(executor,
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
                                 mockListener,
                                 diagnosticMessageModule);
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
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm23_0 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm23_21 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x21, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23_0, dm23_21));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23_0));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFailuresAlternateValues() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm23_0 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm23_21 = DM23PreviouslyMILOnEmissionDTCPacket.create(0x21, NOT_SUPPORTED, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23_0, dm23_21));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23_0));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));
        verify(diagnosticMessageModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForActiveDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);

        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.10.2.a - Engine #1 (0) reported an previously active DTC");
    }

    @Test
    public void testFailureForMILNotOff() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.10.2.a - Engine #1 (0) did not report MIL off");
    }

    @Test
    public void testFailureForNonOBDMILNotOff() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm23_0 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm23_1 = DM23PreviouslyMILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23_0, dm23_1));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23_0));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.10.2.a - Non-OBD ECU Engine #2 (1) did not report MIL off or not supported");
    }

    @Test
    public void testFailureForGlobalDSDifference() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm23_1 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm23_2 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23_1));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23_2));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.10.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNack() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false, dm23));
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(true, dm23));
        when(diagnosticMessageModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));
        verify(diagnosticMessageModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.10.4.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForNoOBDResponse() {

        when(diagnosticMessageModule.requestDM23(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.3.10.2.d - No OBD ECU provided a DM23");
    }

}
