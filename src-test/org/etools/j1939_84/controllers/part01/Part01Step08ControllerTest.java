/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
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
public class Part01Step08ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;
    @Mock
    private EngineSpeedModule engineSpeedModule;
    @Mock
    private Executor executor;
    private Part01Step08Controller instance;
    @Mock
    private J1939 j1939;
    private TestResultsListener listener;
    @Mock
    private ResultsListener mockListener;
    @Mock
    private ReportFileModule reportFileModule;
    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private static DM20MonitorPerformanceRatioPacket createDM20(int address, List<Integer> ratios) {

        PerformanceRatio[] performanceRatios = ratios.stream()
                                                     .map(sp -> new PerformanceRatio(sp, 3, 4, address))
                                                     .toArray(PerformanceRatio[]::new);

        DM20MonitorPerformanceRatioPacket packet = DM20MonitorPerformanceRatioPacket.create(address,
                                                                                            1,
                                                                                            10,
                                                                                            performanceRatios);
        return packet;
    }

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step08Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    /**
     * Test one module of an electric vehicle responds
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Fail if minimum expected SPs are not supported (in the aggregate response for the vehicle) per Section A.4"))
    public void ignitionTypeNotSupported() {
        List<Integer> SPs = List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BATT_ELEC);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one module responds with minimum SP for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Minimum expected SPs are not supported. Not Supported SPs: 5318, 5322 None of these SPs are supported: 4364, 4792, 5308"))
    public void minimumExpectedSPsCompressionIgnition() {

        List<Integer> SPs = List.of(3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. Not Supported SPs: 5318, 5322 None of these SPs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one module responds with minimum SP for a
     * spark ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Minimum expected SPs are not supported. Not Supported SPs: 3054"))
    public void minimumExpectedSPsSparkIgnition() {

        List<Integer> SPs = List.of(3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057,21227,21228);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. Not Supported SPs: 3054");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one random message thrown on the bus with minimum SP for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Verification of proper response for stray bus messages of DM20s"))
    public void obdModuleNull() {
        List<Integer> SPs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4792);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one random message thrown on the bus with minimum SPs for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Compression Ignition Engine Minimum SPs Verified: 5322, 5318, 3058, 3064, 5321, 3055, 4364"))
    public void testCompressionIgnition() {

        List<Integer> SPs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4364);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test empty packet returned for global or destination specific
     * DM20 request - compression engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Compression Ignition Engine Minimum SPs Verified: none - empty packet returned"))
    public void testEmptyPacketsCompressionIgnition() {
        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.empty());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. Not Supported SPs: 3055, 3058, 3064, 5318, 5321, 5322 None of these SPs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test empty packet returned for global or destination specific
     * DM20 request - compression engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Spark Ignition Engine Minimum SPs Verified: none - empty packet returned"))
    public void testEmptyPacketsSparkIgnition() {
        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.empty());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. Not Supported SPs: 3050, 3051, 3053, 3054, 3055, 3056, 3057, 3058, 3306, 21227, 21228");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 8", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test packet SP mismatch returned for global or destination specific
     * DM20 request - compression engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Compression Ignition Engine Minimum SPs Verified: SP mismatch"))
    public void testNoSpPacketsMatch() {
        List<Integer> sps = List.of(5322, 5318, 3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, sps);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. None of these SPs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test packet with expected SPs returned for global or destination specific
     * DM20 request - spark engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Spark Ignition Engine Minimum SPs Verified: Expected SPs"))
    public void testSparkIgnition() {

        List<Integer> SPs = List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057, 21227, 21228);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0x00, SPs);

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test packet with expected SPs returned for global or destination specific
     * DM20 request - spark engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "When a numerator and denominator are provided as FFFFh and FFFFh, the monitor identified in the label SP shall be considered to be unsupported"))
    public void testAllNumeratorAndDenominatorAllFs() {

        DM20MonitorPerformanceRatioPacket dm20 = DM20MonitorPerformanceRatioPacket.create(0x00,
                                                                                          12,
                                                                                          1,
                                                                                          new PerformanceRatio(3050,
                                                                                                               0xFFFF,
                                                                                                               0XFFFF,
                                                                                                               0x00), // this
                                                                                                                      // one
                                                                                                                      // is
                                                                                                                      // considered
                                                                                                                      // unsupported
                                                                                          new PerformanceRatio(3051,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3053,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3054,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3055,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3056,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3057,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3058,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(3306,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(21227,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00),
                                                                                          new PerformanceRatio(21228,
                                                                                                               0,
                                                                                                               1,
                                                                                                               0x00));

        when(communicationsModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0x00));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPs are not supported. Not Supported SPs: 3050");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
