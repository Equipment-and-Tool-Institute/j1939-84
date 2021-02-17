/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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

    private static DM20MonitorPerformanceRatioPacket createDM20(List<Integer> ratios) {
        DM20MonitorPerformanceRatioPacket packet = mock(DM20MonitorPerformanceRatioPacket.class);

        when(packet.getSourceAddress()).thenReturn(0);

        if (ratios != null) {
            List<PerformanceRatio> perfRatios = ratios.stream()
                    .map(spn -> new PerformanceRatio(spn, 0, 0, 0)).collect(Collectors.toList());
            when(packet.getRatios()).thenReturn(perfRatios);
        }

        return packet;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
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

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

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
                                 diagnosticMessageModule);
    }

    @Test
    public void ignitionTypeNotSupported() {
        List<Integer> SPNs = new ArrayList<>();
        int[] SPN3 = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
        SPNs.add(SPN3[1]);
        SPNs.add(SPN3[2]);
        SPNs.add(SPN3[3]);
        SPNs.add(SPN3[4]);
        SPNs.add(SPN3[5]);
        SPNs.add(SPN3[6]);
        SPNs.add(SPN3[7]);
        SPNs.add(SPN3[8]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BATT_ELEC);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void minimumExpectedSPNsCompressionIgnition() {

        List<Integer> SPNs = List.of(3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - minimum expected SPNs are not supported. Not Supported SPNs: 5318, 5322 None of these SPNs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "",
                listener.getResults());
    }

    @Test
    public void minimumExpectedSPNsSparkIgnition() {

        List<Integer> SPNs = List.of(3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - minimum expected SPNs are not supported. Not Supported SPNs: 3054");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void obdModuleNull() {
        List<Integer> SPNs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4792);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testCompressionIgnition() {

        List<Integer> SPNs = List.of(5322, 5318, 3058, 3064, 5321, 3055, 4364);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testEmptyPacketsCompressionIgnition() {
        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(true));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - minimum expected SPNs are not supported. Not Supported SPNs: 3055, 3058, 3064, 5318, 5321, 5322 None of these SPNs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "",
                listener.getResults());
    }

    @Test
    public void testEmptyPacketsSparkIgnition() {
        when(diagnosticMessageModule.requestDM20(any())).thenReturn(RequestResult.empty());

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - minimum expected SPNs are not supported. Not Supported SPNs: 3050, 3051, 3053, 3054, 3055, 3056, 3057, 3058, 3306");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "",
                listener.getResults());
    }

    @Test
    public void testGetDiplayName() {
        assertEquals("Display Name", "Part 1 Step 8", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoSpnNPacketsMatch() {
        List<Integer> spns = List.of(5322, 5318, 3058, 3064, 5321, 3055);
        DM20MonitorPerformanceRatioPacket dm20 = createDM20(spns);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(mockListener).addOutcome(1,
                                        8,
                                        FAIL,
                                        "6.1.8.2.a - minimum expected SPNs are not supported. None of these SPNs are supported: 4364, 4792, 5308");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "",
                listener.getResults());
    }

    @Test
    public void testSparkIgnition() {

        List<Integer> SPNs = List.of(3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(SPNs);

        when(diagnosticMessageModule.requestDM20(any())).thenReturn(new RequestResult<>(false, dm20));

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
