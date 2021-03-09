/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus.create;
import static org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus.AreaStatus.INSIDE;
import static org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus.AreaStatus.NOT_AVAILABLE;
import static org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
public class Part02Step16ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int STEP = 16;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step16Controller instance;

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

        instance = new Part02Step16Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part " + PART + " Step " + STEP, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoResponsesSIEngine() {

        when(diagnosticMessageModule.requestDM34(any())).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM34(any());

        String expected = "";

        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

    }

    @Test
    public void testNoResponsesCIEngine() {

        when(diagnosticMessageModule.requestDM34(any())).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM34(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.a - No ECU responded to the global request");

    }

    @Test
    public void testFailNotOutsideControlAreas() {
        var packet = create(0, INSIDE, INSIDE, INSIDE, INSIDE, INSIDE, INSIDE);

        when(diagnosticMessageModule.requestDM34(any())).thenReturn(new RequestResult<>(false, packet));

        when(diagnosticMessageModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM34(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM34(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.b - Engine #1 (0) reported NOx control area != 0b00");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.b - Engine #1 (0) reported PM control area != 0b00");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported NOx carve-out area != 0b00 or 0b11");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported NOx deficiency area != 0b00 or 0b11");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported PM carve-out area != 0b00 or 0b11");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported PM deficiency area != 0b00 or 0b11");
    }

    @Test
    public void testFailForDifference() {
        var globalPacket = create(0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = create(0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, NOT_AVAILABLE);

        when(diagnosticMessageModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));

        when(diagnosticMessageModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM34(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM34(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.16.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailForNACK() {
        var globalPacket = create(0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);

        when(diagnosticMessageModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));

        when(diagnosticMessageModule.requestDM34(any(), eq(0)))
                                                               .thenReturn(new RequestResult<>(false,
                                                                                               List.of(),
                                                                                               List.of(dsPacket)));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        runTest();

        verify(diagnosticMessageModule).requestDM34(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM34(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.4.b - NACK received from Engine #1 (0) which responded to the global query");
    }
}
