/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
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
public class Part03Step09ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
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
        instance = new Part03Step09Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
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

        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm12_21 = DM12MILOnEmissionDTCPacket.create(0x21, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_0, dm12_21));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_0));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFailuresAlternateValues() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, ALTERNATE_OFF, OFF, OFF, OFF);
        var dm12_21 = DM12MILOnEmissionDTCPacket.create(0x21, NOT_SUPPORTED, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_0, dm12_21));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_0));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM12(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));
        verify(communicationsModule).requestDM12(any(), eq(1));

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForActiveDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.9.2.a - Engine #1 (0) reported an active DTC");
    }

    @Test
    public void testFailureForMILNotOff() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.9.2.b - Engine #1 (0) did not report MIL 'off'");
    }

    @Test
    public void testFailureForNonOBDMILNotOff() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_0, dm12_1));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_0));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.9.2.c - Non-OBD ECU Engine #2 (1) did not report MIL off or not supported");
    }

    @Test
    public void testFailureForGlobalDSDifference() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm12_1 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        var dm12_2 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_1));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_2));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.9.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNack() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(true, dm12));
        when(communicationsModule.requestDM12(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));
        verify(communicationsModule).requestDM12(any(), eq(1));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.9.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForNoOBDResponse() {

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.3.9.2.d - No OBD ECU provided a DM12");
    }

}
