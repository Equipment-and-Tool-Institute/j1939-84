/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
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
public class Part04Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
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

        instance = new Part04Step05Controller(executor,
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
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM23(any(), eq(0));
        verify(diagnosticMessageModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
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
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm2 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm2));
        when(diagnosticMessageModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any(), eq(0));
        verify(diagnosticMessageModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - NACK not received from  Engine #2 (1) and did not provide a response to DS DM21 query");
    }

    @Test
    public void testFailureForDTC() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc));
        dataRepository.putObdModule(obdModule0);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.a - OBD module Engine #1 (0) reported active distance > 0");
    }

    @Test
    public void testNackNotRecieved() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));
        dataRepository.putObdModule(obdModule0);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));
        dataRepository.putObdModule(obdModule1);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, FAST_FLASH, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        var dm23Ack = AcknowledgmentPacket.create(0, ACK);
        when(diagnosticMessageModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(false, dm23Ack));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any(), eq(0));
        verify(diagnosticMessageModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - NACK not received from  Engine #1 (0) and did not provide a response to DS DM21 query");
    }

    @Test
    public void testNoModuleResponse() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)));
        dataRepository.putObdModule(obdModule0);

        var dsDM23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(diagnosticMessageModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - NACK not received from  Engine #1 (0) and did not provide a response to DS DM21 query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.d - No OBD module provided a DM23");
    }

}
