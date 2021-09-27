/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part03Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 15;

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
        instance = new Part03Step15Controller(executor,
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
    public void testRun() {
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule);

        DM21DiagnosticReadinessPacket dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);

        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, dm21));

        runTest();

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(communicationsModule).requestDM21(any(), eq(0));

    }

    @Test
    public void testFailure() {
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        OBDModuleInformation obdModule3 = new OBDModuleInformation(3);
        dataRepository.putObdModule(obdModule);
        dataRepository.putObdModule(obdModule1);
        dataRepository.putObdModule(obdModule2);
        dataRepository.putObdModule(obdModule3);

        DM21DiagnosticReadinessPacket dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 1000, 0, 0, 0);
        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(1, NACK);
        DM21DiagnosticReadinessPacket dm21_2 = DM21DiagnosticReadinessPacket.create(2, 0, 0, 0, 90, 0);
        DM21DiagnosticReadinessPacket dm21_3 = DM21DiagnosticReadinessPacket.create(3, 0, 0, 0, 90, 0);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, dm21));
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(new BusResult<>(false, ackPacket));
        when(communicationsModule.requestDM21(any(), eq(2))).thenReturn(new BusResult<>(false, dm21_2));
        when(communicationsModule.requestDM21(any(), eq(3))).thenReturn(new BusResult<>(false, dm21_3));

        runTest();

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());


        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));
        verify(communicationsModule).requestDM21(any(), eq(2));
        verify(communicationsModule).requestDM21(any(), eq(3));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.15.2.a - OBD ECU Engine #1 (0) reported active distance > 0");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.15.2.a - OBD ECU Turbocharger (2) reported active time > 0");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.15.2.a - OBD ECU Transmission #1 (3) reported active time > 0");
    }

    @Test
    public void testModuleNotRespond() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));

        DM21DiagnosticReadinessPacket dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);

        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, dm21));
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(new BusResult<>(true));
        var ack = AcknowledgmentPacket.create(2, ACK);
        when(communicationsModule.requestDM21(any(), eq(2))).thenReturn(new BusResult<>(false, ack));

        runTest();

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());


        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));
        verify(communicationsModule).requestDM21(any(), eq(2));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.15.2.b - NACK not received from  Engine #2 (1) and did not provide a response to DS DM21 query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.15.2.b - NACK not received from  Turbocharger (2) and did not provide a response to DS DM21 query");
    }
}
