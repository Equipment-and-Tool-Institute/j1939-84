/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

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

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
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
public class Part07Step04ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
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

        instance = new Part07Step04Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM12(any(), eq(0))).thenReturn(BusResult.of(dm12));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM12(any(), eq(1))).thenReturn(BusResult.of(nack));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0));
        verify(diagnosticMessageModule).requestDM12(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForActiveDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc = DiagnosticTroubleCode.create(1223, 12, 0, 1);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM12(any(), eq(0))).thenReturn(BusResult.of(dm12));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.4.2.a - Engine #1 (0) reported an active DTC");
    }

    @Test
    public void testFailureForMILNotOff() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM12(any(), eq(0))).thenReturn(BusResult.of(dm12));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.4.2.a - Engine #1 (0) reported an active DTC");
    }

    @Test
    public void testFailureForNoSupport() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var nack = AcknowledgmentPacket.create(0, NACK);
        when(diagnosticMessageModule.requestDM12(any(), eq(0))).thenReturn(BusResult.of(nack));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.7.4.2.c - No OBD ECU supports DM12");
    }

    @Test
    public void testFailureForNoNACKAndNoSupport() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(diagnosticMessageModule.requestDM12(any(), eq(0))).thenReturn(BusResult.empty());

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.4.1.d - OBD module Engine #1 (0) did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.7.4.2.c - No OBD ECU supports DM12");
    }

}
