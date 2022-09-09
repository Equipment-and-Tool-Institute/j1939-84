/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.PGN;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.create;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.INSIDE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.NOT_AVAILABLE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.OUTSIDE;
import static org.etools.j1939tools.j1939.packets.DM34NTEStatus.AreaStatus.RESERVED;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM34NTEStatus;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
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
    private CommunicationsModule communicationsModule;

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
                                 communicationsModule,
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

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        var packet = create(0, 0, INSIDE, INSIDE, INSIDE, INSIDE, INSIDE, INSIDE);
        obdModule.set(packet, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.4.b - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");

        String expected = "";
        assertEquals(expected, listener.getResults());
        assertEquals("", listener.getMessages());

    }

    @Test
    public void testNoResponsesCIEngine() {

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, List.of()));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.a - No ECU responded to the global request");

    }

    @Test
    public void testFailNotOutsideControlAreas() {
        var packet = create(0, 0, RESERVED, RESERVED, RESERVED, RESERVED, RESERVED, RESERVED);

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(packet, 1);
        dataRepository.putObdModule(moduleInfo);

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, packet));

        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, packet));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        runTest();

        verify(communicationsModule).requestDM34(any(), eq(0x00));
        verify(communicationsModule).requestDM34(any());

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
                                        "6.2.16.2.c - Engine #1 (0) reported NOx carve-out area is 10b");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported NOx deficiency area is 10b");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported PM carve-out area is 10b");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported PM deficiency area is 10b");
    }

    @Test
    public void testFailForDifference() {
        var globalPacket = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, NOT_AVAILABLE);

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));

        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(globalPacket, 1);
        dataRepository.putObdModule(obdModuleInformation);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailForNoxControl() {
        var globalPacket = create(0, 0, RESERVED, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.b - Engine #1 (0) reported NOx control area != 0b00");
    }

    @Test
    public void testFailForCarveOut() {
        var globalPacket = create(0, 0, OUTSIDE, RESERVED, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #1 (0) reported NOx carve-out area is 10b");
    }

    @Test
    public void testFailForPmControl() {
        var globalPacket = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, INSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);

        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, globalPacket));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(globalPacket, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.b - Engine #1 (0) reported PM control area != 0b00");
    }

    @Test
    public void testHappyPathNoFailures() {
        var packet = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var globalPacket = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);
        System.out.println(globalPacket.getPacket());
        System.out.println(packet.getPacket());
        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, packet));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(packet, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailFor() {
        var packet = new DM34NTEStatus(Packet.create(PGN, 0x01, 0x05, 0x05, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF));
        var globalPacket = create(0, 0, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE, OUTSIDE);
        var dsPacket = AcknowledgmentPacket.create(0, NACK, 0, 0xF9, DM34NTEStatus.PGN);
        System.out.println(globalPacket.getPacket());
        System.out.println(packet.getPacket());
        when(communicationsModule.requestDM34(any())).thenReturn(new RequestResult<>(false, packet));
        when(communicationsModule.requestDM34(any(), eq(0))).thenReturn(new RequestResult<>(false, dsPacket));

        var vehInfo = new VehicleInformation();
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(packet, 1);
        dataRepository.putObdModule(obdModule);

        runTest();

        verify(communicationsModule).requestDM34(any());
        verify(communicationsModule).requestDM34(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #2 (1) reported NOx deficiency area is 10b");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.c - Engine #2 (1) reported PM deficiency area is 10b");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.d - Engine #2 (1) reported byte 1 bit 1-2 != 0b11");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.d - Engine #2 (1) reported byte 2 bit 1-2 != 0b11");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.16.2.d - Engine #2 (1) reported reserve bytes 3-8 != 0xFF");
    }
}
