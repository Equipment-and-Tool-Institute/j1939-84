/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part06;

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
public class Part06Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 6;
    private static final int STEP_NUMBER = 3;

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

        instance = new Part06Step03Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var packet1 = DM12MILOnEmissionDTCPacket.create(0x01, ON, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(BusResult.empty());

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported a MIL-on active DTC");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.b - No ECU reported MIL on");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForNoDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var packet1 = DM12MILOnEmissionDTCPacket.create(0x01, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.a - No ECU reported a MIL-on active DTC");
    }

    @Test
    public void testFailureForNoMIL() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var packet1 = DM12MILOnEmissionDTCPacket.create(0x01, OFF, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM12(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM12(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.6.3.2.b - No ECU reported MIL on");
    }

}
