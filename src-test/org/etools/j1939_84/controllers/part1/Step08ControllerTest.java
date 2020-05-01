/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.PerformanceRatio;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author Garrison Garland (garrison@test.soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step08ControllerTest extends AbstractControllerTest {

    private static DM20MonitorPerformanceRatioPacket createDM20(Integer sourceAddress,
            Integer ignitionCycles,
            List<Integer> ratios) {
        DM20MonitorPerformanceRatioPacket packet = mock(DM20MonitorPerformanceRatioPacket.class);
        if (sourceAddress != null) {
            when(packet.getSourceAddress()).thenReturn(sourceAddress);
        }
        if (ignitionCycles != null) {
            when(packet.getIgnitionCycles()).thenReturn(ignitionCycles);
        }

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

    private DateTimeModule dateTimeModule;

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

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step08Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
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
                partResultFactory,
                diagnosticReadinessModule,
                dataRepository,
                mockListener,
                reportFileModule);
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
    public void testHappyRun() throws Throwable {

        List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
        List<Integer> SPN = new ArrayList<>();
        int SPN1[] = { 5322, 5318, 3058, 3064, 5321, 3055 };
        int SPN2[] = { 4792, 5308, 4364 };
        SPN.add(SPN1[0]);
        SPN.add(SPN1[1]);
        SPN.add(SPN1[2]);
        SPN.add(SPN1[3]);
        SPN.add(SPN1[4]);
        SPN.add(SPN1[5]);
        SPN.add(SPN2[0]);

        DM20MonitorPerformanceRatioPacket dm20 = createDM20(0, 10, SPN);

        globalDM20s.add(dm20);
        when(diagnosticReadinessModule.getDM20Packets(ArgumentMatchers.any(), true))
                .thenReturn(globalDM20s);

        OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
        when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        //
        // when(diagnosticReadinessModule.getDM20Packets(listener,
        // eq(true))).thenReturn(globalDM20s);

        Set<Integer> addresses = new HashSet<>();
        addresses.add(0);
        when(dataRepository.getObdModuleAddresses()).thenReturn(addresses);

        runTest();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(diagnosticReadinessModule).getDM20Packets(listener, true);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    // @Test
    // public void testPacketsEmpty() {
    // List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
    // when(diagnosticReadinessModule.getDM20Packets(listener,
    // false)).thenReturn(globalDM20s);
    //
    // runTest();
    //
    // verify(diagnosticReadinessModule).setJ1939(j1939);
    //
    // verify(diagnosticReadinessModule).getDM20Packets(listener, eq(true));
    //
    // verify(reportFileModule).onProgress(0, 1, "");
    // verify(reportFileModule).onResult("DM20 is not supported");
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // assertEquals("DM20 is not supported\n", listener.getResults());
    // }

    // @Test
    // public void toLittleSPNs() {
    //
    // List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
    // when(diagnosticReadinessModule.getDM20Packets(listener,
    // true)).thenReturn(globalDM20s);
    //
    // OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
    // when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);
    //
    // VehicleInformation vehicleInformation = new VehicleInformation();
    // vehicleInformation.setEmissionUnits(1);
    // when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
    //
    // runTest();
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // assertEquals("", listener.getResults());
    //
    // verify(mockListener)
    // .addOutcome(1, 8, FAIL, "6.1.8.2.a - minimum expected SPNs for Diesel fuel
    // type are not supported.");
    //
    // verify(mockListener)
    // .addOutcome(1, 8, FAIL, "6.1.8.2.a - minimum expected SPNs for Spark Ignition
    // are not supported.");
    //
    // verify(diagnosticReadinessModule).setJ1939(j1939);
    // verify(diagnosticReadinessModule).getDM20Packets(listener, eq(true));
    // verify(dataRepository).getObdModule(0);
    // verify(dataRepository).getVehicleInformation();
    //
    // }
    //
    // @Test
    // public void wrongFuelType() {
    // List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
    // when(diagnosticReadinessModule.getDM20Packets(listener,
    // true)).thenReturn(globalDM20s);
    //
    // OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
    // when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);
    //
    // VehicleInformation vehicleInformation = new VehicleInformation();
    // vehicleInformation.setEmissionUnits(1);
    // when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
    //
    // runTest();
    //
    // verify(mockListener).addOutcome(1,
    // 8,
    // FAIL,
    // "6.1.8.2.a - Fuel Type not supported in Monitor Performance Ratio
    // Evaluation.");
    //
    // verify(diagnosticReadinessModule).setJ1939(j1939);
    // verify(diagnosticReadinessModule).getDM20Packets(listener, eq(true));
    // verify(dataRepository).getObdModule(0);
    // verify(dataRepository).getVehicleInformation();
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // assertEquals("", listener.getResults());
    // }
    //
    // @Test
    // public void wrongIgnitionType() {
    // List<DM20MonitorPerformanceRatioPacket> globalDM20s = new ArrayList<>();
    // when(diagnosticReadinessModule.getDM20Packets(listener,
    // true)).thenReturn(globalDM20s);
    //
    // OBDModuleInformation moduleInfo = mock(OBDModuleInformation.class);
    // when(dataRepository.getObdModule(0)).thenReturn(moduleInfo);
    //
    // VehicleInformation vehicleInformation = new VehicleInformation();
    // vehicleInformation.setEmissionUnits(1);
    // when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
    //
    // runTest();
    //
    // verify(mockListener).addOutcome(1,
    // 8,
    // FAIL,
    // "6.1.8.2.a - Ignition Type not supported in Monitor Performance Ratio
    // Evaluation.");
    //
    // verify(diagnosticReadinessModule).setJ1939(j1939);
    // verify(diagnosticReadinessModule).getDM20Packets(listener, eq(true));
    // verify(dataRepository).getObdModule(0);
    // verify(dataRepository).getVehicleInformation();
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // assertEquals("", listener.getResults());
    // }

}
