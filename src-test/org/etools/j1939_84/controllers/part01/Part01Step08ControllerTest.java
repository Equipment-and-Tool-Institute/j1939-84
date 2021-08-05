/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
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
    private DiagnosticMessageModule diagnosticMessageModule;
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

    private static DM20MonitorPerformanceRatioPacket createDM20(List<Integer> ratios) {
        DM20MonitorPerformanceRatioPacket packet = mock(DM20MonitorPerformanceRatioPacket.class);

        when(packet.getSourceAddress()).thenReturn(0);

        if (ratios != null) {
            List<PerformanceRatio> perfRatios = ratios.stream()
                                                      .map(spn -> new PerformanceRatio(spn, 0, 0, 0))
                                                      .collect(Collectors.toList());
            when(packet.getRatios()).thenReturn(perfRatios);
        }

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
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
    }

    /**
     * Test one module of an electric vehicle responds
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Fail if minimum expected SPNSPs are not supported (in the aggregate response for the vehicle) per Section A.4"))
    public void ignitionTypeNotSupported() {
        List<Integer> SPNs = List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BATT_ELEC);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one module responds with minimum SPN for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Minimum expected SPNs are not supported. Not Supported SPNs: 5318, 5322 None of these SPNs are supported: 4364, 4792, 5308"))
    public void minimumExpectedSPNsCompressionIgnition() {

        List<Integer> SPNs = List.of(3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. Not Supported SPNs: 5318, 5322 None of these SPNs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one module responds with minimum SPN for a
     * spark ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Minimum expected SPNs are not supported. Not Supported SPNs: 3054"))
    public void minimumExpectedSPNsSparkIgnition() {

        List<Integer> SPNs = List.of(3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. Not Supported SPNs: 3054");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one random message thrown on the bus with minimum SPN for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "Verification of proper response for stray bus messages of DM20s"))
    public void obdModuleNull() {
        List<Integer> SPNs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4792);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test one random message thrown on the bus with minimum SPN for a
     * compression ignition engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Compression Ignition Engine Minimum SPs Verified: 5322, 5318, 3058, 3064, 5321, 3055, 4364"))
    public void testCompressionIgnition() {

        List<Integer> SPNs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4364);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

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
        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.empty());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. Not Supported SPNs: 3055, 3058, 3064, 5318, 5321, 5322 None of these SPNs are supported: 4364, 4792, 5308");

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
        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.empty());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. Not Supported SPNs: 3050, 3051, 3053, 3054, 3055, 3056, 3057, 3058, 3306");

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
     * Test packet SPN mismatch returned for global or destination specific
     * DM20 request - compression engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Compression Ignition Engine Minimum SPs Verified: SPN mismatch"))
    public void testNoSpnNPacketsMatch() {
        List<Integer> spns = List.of(5322, 5318, 3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(spns);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. None of these SPNs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test packet with expected SPNs returned for global or destination specific
     * DM20 request - spark engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "A.4 - Spark Ignition Engine Minimum SPs Verified: Expected SPNs"))
    public void testSparkIgnition() {

        List<Integer> SPNs = List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test packet with expected SPNs returned for global or destination specific
     * DM20 request - spark engine
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.8.2.a", description = "When a numerator and denominator are provided as FFFFh and FFFFh, the monitor identified in the label SPN shall be considered to be unsupported"))
    public void testAllNumeratorAndDenominatorAllFs() {
        int moduleAddress = 0;
        int spn = 524287;
        int numerator = Byte.toUnsignedInt((byte) 0xFF);
        int denominator = Byte.toUnsignedInt((byte) 0xFF);
        PerformanceRatio performanceRatio = new PerformanceRatio(spn, numerator, denominator, moduleAddress);

        DM20MonitorPerformanceRatioPacket dm20 = DM20MonitorPerformanceRatioPacket.create(0, 13, 9, performanceRatio);
        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.of(dm20));

        dataRepository.putObdModule(new OBDModuleInformation(0));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Engine #1 (0) numerator and denominator are provided as 0xFFFF(h)");
        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - Minimum expected SPNs are not supported. Not Supported SPNs: 3050, 3051, 3053, 3054, 3055, 3056, 3057, 3058, 3306");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
