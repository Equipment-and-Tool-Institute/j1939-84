/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.SLOW_FLASH;
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
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Garrison Garland (garrison@soliddesign.net)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step16ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step16Controller instance;

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
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step16Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testDTCsNotEmpty() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc1 = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var packet1 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU Engine #1 (0) reported a previously active DTC");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 16", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILNotSupported() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var packet1 = DM2PreviouslyActiveDTC.create(0, NOT_SUPPORTED, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU Engine #1 (0) did not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testMILOff() {
        var packet1 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM2(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILStatusNotOFF() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc = DiagnosticTroubleCode.create(12, 1, 1, 1);
        var packet1 = DM2PreviouslyActiveDTC.create(0, SLOW_FLASH, OFF, OFF, OFF, dtc);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.a - OBD ECU Engine #1 (0) reported a previously active DTC");
        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.b - OBD ECU Engine #1 (0) did not report MIL off");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b,c") })
    public void testNonOBDMilOn() {
        var packet1 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.2.c - Non-OBD ECU Engine #1 (0) did not report MIL off or not supported");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b") })
    public void testResponseNotNACK() {

        dataRepository.putObdModule(new OBDModuleInformation(0));
        var packet1 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet1));

        dataRepository.putObdModule(new OBDModuleInformation(3));
        DM2PreviouslyActiveDTC packet4 = DM2PreviouslyActiveDTC.create(3, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any(), eq(3))).thenReturn(BusResult.of(packet4));

        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));
        verify(diagnosticMessageModule).requestDM2(any(), eq(3));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.b - OBD module Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a") })
    public void testResponsesAreDifferent() {

        dataRepository.putObdModule(new OBDModuleInformation(0));
        DM2PreviouslyActiveDTC packet2 = DM2PreviouslyActiveDTC.create(0, OFF, ON, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet2));

        DM2PreviouslyActiveDTC packet1 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);

        dataRepository.putObdModule(new OBDModuleInformation(3));
        AcknowledgmentPacket packet4 = AcknowledgmentPacket.create(3, NACK);
        when(diagnosticMessageModule.requestDM2(any(), eq(3))).thenReturn(BusResult.of(packet4));

        DM2PreviouslyActiveDTC packet3 = DM2PreviouslyActiveDTC.create(3, OFF, OFF, OFF, OFF);

        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1, packet3));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));
        verify(diagnosticMessageModule).requestDM2(any(), eq(3));

        verify(mockListener).addOutcome(1,
                                        16,
                                        FAIL,
                                        "6.1.16.4.a - Difference compared to data received during global request from Engine #1 (0)");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testTwoObdModulesOneWithResponseOneWithNack2() {

        dataRepository.putObdModule(new OBDModuleInformation(0));
        DM2PreviouslyActiveDTC packet1 = DM2PreviouslyActiveDTC.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM2(any(), eq(0))).thenReturn(BusResult.of(packet1));

        dataRepository.putObdModule(new OBDModuleInformation(3));
        AcknowledgmentPacket packet4 = AcknowledgmentPacket.create(3, NACK);
        when(diagnosticMessageModule.requestDM2(any(), eq(3))).thenReturn(BusResult.of(packet4));

        when(diagnosticMessageModule.requestDM2(any())).thenReturn(RequestResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).requestDM2(any());
        verify(diagnosticMessageModule).requestDM2(any(), eq(0));
        verify(diagnosticMessageModule).requestDM2(any(), eq(3));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
