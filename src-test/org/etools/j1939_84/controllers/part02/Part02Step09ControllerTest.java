/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step09ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

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

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dataRepository = DataRepository.newInstance();

        DateTimeModule.setInstance(null);

        Part02Step09Controller instance = new Part02Step09Controller(executor,
                                                                     engineSpeedModule,
                                                                     bannerModule,
                                                                     vehicleInformationModule,
                                                                     dataRepository,
                                                                     DateTimeModule.getInstance(),
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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
    }

    @Test
    public void testDifferentInPacketsFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 1);
        var packet1 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 2);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet1));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.4.a - Difference compared to data received during global request from Engine #1 (0)");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoFailures() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 1);
        var packet17 = DM21DiagnosticReadinessPacket.create(17, 0, 0, 0, 0, 2);

        var globalResults = new RequestResult<>(false, packet0, packet17);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoNackFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 1);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));
        when(communicationsModule.requestDM21(any(), eq(1))).thenReturn(new BusResult<>(true));

        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));
        verify(communicationsModule).requestDM21(any(), eq(1));

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNonZeroDistanceSCCFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 1, 0, 0, 1);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.2.c - Engine #1 (0) reported > 0 distance with MIL on");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNonZeroTimeSCCFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 1);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.2.c - Engine #1 (0) reported > 0 time with MIL on");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoPackets() {

        when(communicationsModule.requestDM21(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.2.b - No ECU reported time (SPN 3295) or distance (SPN 3069) with MIL on");

        verify(mockListener).addOutcome(2,
                                        9,
                                        WARN,
                                        "6.2.9.2.e - No OBD ECU reported time (SPN 3296) for DM21");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testTimeWithSCCAsNotAvailableFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0xFF00);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        verify(mockListener).addOutcome(2,
                                        9,
                                        WARN,
                                        "6.2.9.2.e - No OBD ECU reported time (SPN 3296) for DM21");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testZeroTimeWithSCCFailure() {

        var packet0 = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);

        var globalResults = new RequestResult<>(false, packet0);

        when(communicationsModule.requestDM21(any())).thenReturn(globalResults);
        when(communicationsModule.requestDM21(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM21(any());
        verify(communicationsModule).requestDM21(any(), eq(0));

        verify(mockListener).addOutcome(2,
                                        9,
                                        FAIL,
                                        "6.2.9.2.d - Engine #1 (0) reported zero time SCC (SPN 3296)");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }
}
