/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
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

import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.modules.CommunicationsModule;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 12;

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

        instance = new Part11Step12Controller(executor,
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
        obdModuleInformation.set(DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0), 11);
        obdModuleInformation.setDeltaEngineStart(600.0);
        dataRepository.putObdModule(obdModuleInformation);
        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 10);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDifferenceTooMuch() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0), 11);
        obdModuleInformation.setDeltaEngineStart(600.0);
        dataRepository.putObdModule(obdModuleInformation);
        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 11);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.12.2.a - Engine #1 (0) reported time since code clear differs by more than ±1 minute from expected value");
    }

    @Test
    public void testFailureForDifferenceTooLittle() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0), 11);
        obdModuleInformation.setDeltaEngineStart(600.0);
        dataRepository.putObdModule(obdModuleInformation);
        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 9);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.12.2.a - Engine #1 (0) reported time since code clear differs by more than ±1 minute from expected value");
    }

    @Test
    public void testFailureForDistanceSCC() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0), 11);
        obdModuleInformation.setDeltaEngineStart(600.0);
        dataRepository.putObdModule(obdModuleInformation);
        var dm21 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 1, 0, 10);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(BusResult.of(dm21));

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.12.2.b - Engine #1 (0) reported distance SCC is > 0");
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM21(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.12.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
