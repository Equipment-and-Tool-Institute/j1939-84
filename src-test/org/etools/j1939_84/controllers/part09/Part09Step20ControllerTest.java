/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.RequestResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part09Step20ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 20;

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

        instance = new Part09Step20Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(any(), eq(0))).thenReturn(RequestResult.of(dm6));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM6(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        when(communicationsModule.requestDM6(any())).thenReturn(RequestResult.of(dm6));

        runTest();

        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(0));
        verify(communicationsModule).requestDM6(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM6(any(), eq(0))).thenReturn(RequestResult.of(dm6));

        when(communicationsModule.requestDM6(any())).thenReturn(RequestResult.of(dm6));

        runTest();

        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.20.2.a - Engine #1 (0) reported a pending DTC");
    }

    @Test
    public void testFailureForNoDM6() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM6(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        when(communicationsModule.requestDM6(any())).thenReturn(RequestResult.of());

        runTest();

        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.20.2.b - No OBD ECU provided a DM6 message");
    }

    @Test
    public void testFailureForDifference() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm6 = DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(any(), eq(0))).thenReturn(RequestResult.of(dm6));

        var dm6_2 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(any())).thenReturn(RequestResult.of(dm6_2));

        runTest();

        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.20.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(any(), eq(0))).thenReturn(RequestResult.of(dm6));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(communicationsModule.requestDM6(any(), eq(1))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM6(any())).thenReturn(RequestResult.of(dm6));

        runTest();

        verify(communicationsModule).requestDM6(any());
        verify(communicationsModule).requestDM6(any(), eq(0));
        verify(communicationsModule).requestDM6(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.20.4.b - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
