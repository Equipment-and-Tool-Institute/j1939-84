/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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
 *
 * @author Garrison Garland (garrison@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step08ControllerTest extends AbstractControllerTest {

    private static DM20MonitorPerformanceRatioPacket createDM20(Integer sourceAddress,
            List<Integer> ratios) {
        DM20MonitorPerformanceRatioPacket packet = mock(DM20MonitorPerformanceRatioPacket.class);

        when(packet.getSourceAddress()).thenReturn(sourceAddress);

        if (ratios != null) {
            List<PerformanceRatio> perfRatios = ratios.stream()
                    .map(spn -> new PerformanceRatio(spn, 0, 0, sourceAddress)).collect(Collectors.toList());
            when(packet.getRatios()).thenReturn(perfRatios);
        }

        return packet;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step08Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Test
    public void ignitionTypeNotSupported() {
        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN3[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
        SPNs.add(SPN3[1]);
        SPNs.add(SPN3[2]);
        SPNs.add(SPN3[3]);
        SPNs.add(SPN3[4]);
        SPNs.add(SPN3[5]);
        SPNs.add(SPN3[6]);
        SPNs.add(SPN3[7]);
        SPNs.add(SPN3[8]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BATT_ELEC);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void minimumExpectedSPNsCompressionIgnition() {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN1[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
        SPNs.add(SPN1[2]);
        SPNs.add(SPN1[3]);
        SPNs.add(SPN1[4]);
        SPNs.add(SPN1[5]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");
        verify(reportFileModule).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.\n",
                listener.getResults());
    }

    @Test
    public void minimumExpectedSPNsSparkIgnition() {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN3[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
        SPNs.add(SPN3[1]);
        SPNs.add(SPN3[2]);
        SPNs.add(SPN3[3]);
        SPNs.add(SPN3[4]);
        SPNs.add(SPN3[5]);
        SPNs.add(SPN3[6]);
        SPNs.add(SPN3[7]);
        SPNs.add(SPN3[8]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");
        verify(reportFileModule).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.\n",
                listener.getResults());
    }

    @Test
    public void obdModuleNull() {
        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN1[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
        int SPN2[] = { 4792, 5308, 4364 };
        SPNs.add(SPN1[0]);
        SPNs.add(SPN1[1]);
        SPNs.add(SPN1[2]);
        SPNs.add(SPN1[3]);
        SPNs.add(SPN1[4]);
        SPNs.add(SPN1[5]);
        SPNs.add(SPN2[0]);
        SPNs.add(SPN2[1]);
        SPNs.add(SPN2[2]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);

        instance = new Step08Controller(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticReadinessModule,
                dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticReadinessModule);
    }

    @Test
    public void testCompressionIgnition() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN1[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
        int SPN2[] = { 4792, 5308, 4364 };
        SPNs.add(SPN1[0]);
        SPNs.add(SPN1[1]);
        SPNs.add(SPN1[2]);
        SPNs.add(SPN1[3]);
        SPNs.add(SPN1[4]);
        SPNs.add(SPN1[5]);
        SPNs.add(SPN2[0]);
        SPNs.add(SPN2[1]);
        SPNs.add(SPN2[2]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testEmptyPacketsCompressionIgnition() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();

        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");
        verify(reportFileModule).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.\n",
                listener.getResults());
    }

    @Test
    public void testEmptyPacketsSparkIgnition() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();

        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");
        verify(reportFileModule).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.8.2.a - minimum expected SPNs for spark ignition are not supported.\n",
                listener.getResults());
    }

    @Test
    public void testGetDiplayName() {
        assertEquals("Display Name", "Part 1 Step 8", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testNoSpnNPacketsMatch() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> spns = new ArrayList<>() {
            {
                add(5322);
                add(5318);
                add(3058);
                add(3064);
                add(5321);
                add(3055);
            }
        };

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, spns);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(mockListener).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule)
                .onResult("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");
        verify(reportFileModule).addOutcome(1,
                8,
                FAIL,
                "6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("FAIL: 6.1.8.2.a - minimum expected SPNs for compression ignition are not supported.\n",
                listener.getResults());
    }

    @Test
    public void testSparkIgnition() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPNs = new ArrayList<>();
        int SPN3[] = { 3054, 3058, 3306, 3053, 3050, 3051, 3055, 3056, 3057 };
        SPNs.add(SPN3[0]);
        SPNs.add(SPN3[1]);
        SPNs.add(SPN3[2]);
        SPNs.add(SPN3[3]);
        SPNs.add(SPN3[4]);
        SPNs.add(SPN3[5]);
        SPNs.add(SPN3[6]);
        SPNs.add(SPN3[7]);
        SPNs.add(SPN3[8]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, SPNs);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(any(), eq(true))).thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.BI_CNG);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();
        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).getDM20Packets(any(), eq(true));

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
