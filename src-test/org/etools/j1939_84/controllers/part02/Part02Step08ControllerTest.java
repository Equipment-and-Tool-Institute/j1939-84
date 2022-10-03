/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step08ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int STEP = 8;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step08Controller instance;

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
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part02Step08Controller(
                                              executor,
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
        assertEquals("Part " + PART + " Step " + STEP, instance.getDisplayName());
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
    public void testNoResponsesNoModules() {
        when(communicationsModule.requestDM26(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(communicationsModule).requestDM26(any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoResponsesFailureEightFiveA() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(true));
        when(communicationsModule.requestDM26(any())).thenReturn(new RequestResult<>(true));

        runTest();

        verify(communicationsModule).requestDM26(any());
        verify(communicationsModule).requestDM26(any(), eq(0x01));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        INFO,
                                        "6.2.8.5.a - No responses received from Engine #2 (1)");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoFailures() {

        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailureTimeDiffEightFiveA() {

        var enabledSystems = List.of(
                CompositeSystem.AC_SYSTEM_REFRIGERANT,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                CompositeSystem.CATALYST,
                CompositeSystem.COMPREHENSIVE_COMPONENT,
                CompositeSystem.EVAPORATIVE_SYSTEM,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                CompositeSystem.COLD_START_AID_SYSTEM,
                CompositeSystem.DIESEL_PARTICULATE_FILTER,
                CompositeSystem.EGR_VVT_SYSTEM,
                CompositeSystem.EXHAUST_GAS_SENSOR,
                CompositeSystem.FUEL_SYSTEM,
                CompositeSystem.HEATED_CATALYST,
                CompositeSystem.MISFIRE,
                CompositeSystem.NMHC_CONVERTING_CATALYST,
                CompositeSystem.NOX_CATALYST_ABSORBER,
                CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                CompositeSystem.COMPREHENSIVE_COMPONENT,
                CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                CompositeSystem.AC_SYSTEM_REFRIGERANT,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                CompositeSystem.CATALYST,
                CompositeSystem.COLD_START_AID_SYSTEM,
                CompositeSystem.DIESEL_PARTICULATE_FILTER,
                CompositeSystem.EGR_VVT_SYSTEM,
                CompositeSystem.EVAPORATIVE_SYSTEM,
                CompositeSystem.EXHAUST_GAS_SENSOR,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                CompositeSystem.FUEL_SYSTEM,
                CompositeSystem.HEATED_CATALYST,
                CompositeSystem.MISFIRE,
                CompositeSystem.NOX_CATALYST_ABSORBER,
                CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                CompositeSystem.COMPREHENSIVE_COMPONENT,
                CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                CompositeSystem.AC_SYSTEM_REFRIGERANT,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                CompositeSystem.CATALYST,
                CompositeSystem.COLD_START_AID_SYSTEM,
                CompositeSystem.DIESEL_PARTICULATE_FILTER,
                CompositeSystem.EGR_VVT_SYSTEM,
                CompositeSystem.EVAPORATIVE_SYSTEM,
                CompositeSystem.EXHAUST_GAS_SENSOR,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                CompositeSystem.FUEL_SYSTEM,
                CompositeSystem.HEATED_CATALYST,
                CompositeSystem.MISFIRE,
                CompositeSystem.NMHC_CONVERTING_CATALYST,
                CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                0x02,
                0,
                0,
                enabledSystemsObd2,
                completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                CompositeSystem.COMPREHENSIVE_COMPONENT,
                CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                CompositeSystem.AC_SYSTEM_REFRIGERANT,
                CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                CompositeSystem.CATALYST,
                CompositeSystem.COLD_START_AID_SYSTEM,
                CompositeSystem.DIESEL_PARTICULATE_FILTER,
                CompositeSystem.EVAPORATIVE_SYSTEM,
                CompositeSystem.EXHAUST_GAS_SENSOR,
                CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                CompositeSystem.FUEL_SYSTEM,
                CompositeSystem.HEATED_CATALYST,
                CompositeSystem.MISFIRE,
                CompositeSystem.NMHC_CONVERTING_CATALYST,
                CompositeSystem.NOX_CATALYST_ABSORBER,
                CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3Ds = mock(DM26TripDiagnosticReadinessPacket.class);
        when(packet3Ds.getSourceAddress()).thenReturn(0x03);
        when(packet3Ds.getWarmUpsSinceClear()).thenReturn((byte) 0x00);
        when(packet3Ds.getTimeSinceEngineStart()).thenReturn(Double.valueOf(30));
        Packet packet = mock(Packet.class);
        when(packet.getTimestamp()).thenReturn(LocalDateTime.now());
        when(packet3Ds.getPacket()).thenReturn(packet);

        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3Ds));

        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                0x03,
                0x00,
                0x00,
                enabledSystemsObd3,
                completedSystemsObd3);

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2), eq(8), eq(FAIL), eq("6.2.8.2.a - DM5 message in 6.2.2.3 from Transmission #1 (3) monitor reported not supported and DM26 message reported not complete"));
        verify(mockListener).addOutcome(eq(2), eq(8), eq(FAIL), eq("6.2.8.2.c - DM5 message in 6.2.2.3 from Transmission #1 (3) monitor reported CCM supported and DM26 message reported disabled"));
        verify(mockListener).addOutcome(eq(2), eq(8), eq(FAIL), eq("6.2.8.5.a - Difference in data between DS and global responses from Transmission #1 (3)"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailureEightTwoB() {

        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        var enabledSystemsDm5 = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystemsDm5 = List.of(
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystemsDm5, completeSystemsDm5), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(FAIL),
                                        eq("6.2.8.2.a - DM5 message in 6.2.2.3 from Engine #1 (0) monitor reported not supported and DM26 message reported not complete"));
        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(FAIL),
                                        eq("6.2.8.2.b - DM5 message in 6.2.2.3 from Engine #1 (0) monitor reported not supported and DM26 message reported enable"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());
    }

    @Test
    public void testInfoDsMissingEightFiveA() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of());

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(INFO),
                                        eq("6.2.8.5.a - DS response was not received from Transmission #1 (3)"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailureEightFiveB() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));
        var ack = AcknowledgmentPacket.create(0x00, AcknowledgmentPacket.Response.BUSY);

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(new RequestResult<>(false,
                                                                                                                 List.of(
                                                                                                                         packet1,
                                                                                                                         packet2,
                                                                                                                         packet3),
                                                                                                                 List.of(ack)));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(FAIL),
                                        eq("6.2.8.5.b - Response received to global query from Engine #1 (0) is not a NACK"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testFailureEightTwoC() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COMPREHENSIVE_COMPONENT,
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        var enabledSystemsDM5 = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystemsDM5 = List.of(
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystemsDM5, completeSystemsDM5), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0))).thenReturn(new RequestResult<>(false,
                                                                                                                 dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.COMPREHENSIVE_COMPONENT,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(
                                        2,
                                        8,
                                        FAIL,
                                        "6.2.8.2.c - DM5 message in 6.2.2.3 from Engine #1 (0) monitor reported CCM supported and DM26 message reported disabled");

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testFailureEightTwoD() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 1, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(0x03,
                                                                                             0,
                                                                                             1,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(FAIL),
                                        eq("6.2.8.2.d - Transmission #1 (3) indicates number of warm-ups since code clear greater than zero"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testWarningEightThreeA() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0))).thenReturn(new RequestResult<>(false,
                                                                                                                 dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completeSystems), 2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(WARN),
                                        eq("6.2.8.3.a - Required monitor Boost pressure control sys is supported by more than one OBD ECU"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testInfoEightThreeB() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2Dm5 = List.of(
                                            CompositeSystem.COMPREHENSIVE_COMPONENT,
                                            CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2Dm5 = List.of(
                                              CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                              CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                              CompositeSystem.CATALYST,
                                              CompositeSystem.COLD_START_AID_SYSTEM,
                                              CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                              CompositeSystem.EGR_VVT_SYSTEM,
                                              CompositeSystem.EVAPORATIVE_SYSTEM,
                                              CompositeSystem.EXHAUST_GAS_SENSOR,
                                              CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                              CompositeSystem.FUEL_SYSTEM,
                                              CompositeSystem.HEATED_CATALYST,
                                              CompositeSystem.MISFIRE,
                                              CompositeSystem.NMHC_CONVERTING_CATALYST,
                                              CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02,
                                                           0,
                                                           0,
                                                           0x22,
                                                           enabledSystemsObd2Dm5,
                                                           completedSystemsObd2Dm5),
                       2);
        dataRepository.putObdModule(obdModule2);

        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(INFO),
                                        eq("6.2.8.3.b - DM5 message in 6.2.2.3 from Turbocharger (2) monitor reported supported and DM26 message reported complete or not supported"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testFailureEightTwoA() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0))).thenReturn(new RequestResult<>(false,
                                                                                                                 dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completeSystems), 2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet2,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(FAIL),
                                        eq("6.2.8.2.a - DM5 message in 6.2.2.3 from Engine #2 (1) monitor reported not supported and DM26 message reported not complete"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testMissingGlobalInfoEightFiveA() {

        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00,
                                                            0,
                                                            0,
                                                            enabledSystems,
                                                            completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 2);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0))).thenReturn(new RequestResult<>(false,
                                                                                     dm26));

        var enabledSystemsObd1 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        var completedSystemsObd1 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        DM26TripDiagnosticReadinessPacket packet1 = DM26TripDiagnosticReadinessPacket.create(0x01,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd1,
                                                                                             completedSystemsObd1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, enabledSystemsObd1, completedSystemsObd1),
                       2);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        OBDModuleInformation obdModule2 = new OBDModuleInformation(2);
        var enabledSystemsObd2 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        var completedSystemsObd2 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EGR_VVT_SYSTEM,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);

        obdModule2.set(DM5DiagnosticReadinessPacket.create(0x02, 0, 0, 0x22, enabledSystemsObd2, completedSystemsObd2),
                       2);
        dataRepository.putObdModule(obdModule2);
        DM26TripDiagnosticReadinessPacket packet2 = DM26TripDiagnosticReadinessPacket.create(0x02,
                                                                                             0,
                                                                                             0,
                                                                                             enabledSystemsObd2,
                                                                                             completedSystemsObd2);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x02))).thenReturn(RequestResult.of(packet2));

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        var enabledSystemsObd3 = List.of(
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM);
        var completedSystemsObd3 = List.of(
                                           CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                           CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                           CompositeSystem.CATALYST,
                                           CompositeSystem.COLD_START_AID_SYSTEM,
                                           CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                           CompositeSystem.EVAPORATIVE_SYSTEM,
                                           CompositeSystem.EXHAUST_GAS_SENSOR,
                                           CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                           CompositeSystem.FUEL_SYSTEM,
                                           CompositeSystem.HEATED_CATALYST,
                                           CompositeSystem.MISFIRE,
                                           CompositeSystem.NMHC_CONVERTING_CATALYST,
                                           CompositeSystem.NOX_CATALYST_ABSORBER,
                                           CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule3.set(DM5DiagnosticReadinessPacket.create(0x03, 0, 0, 0x22, enabledSystemsObd3, completedSystemsObd3),
                       2);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        when(communicationsModule.requestDM26(any(CommunicationsListener.class))).thenReturn(RequestResult.of(dm26,
                                                                                                              packet1,
                                                                                                              packet3));

        runTest();

        verify(communicationsModule).requestDM26(any(CommunicationsListener.class));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(2),
                                        eq(8),
                                        eq(INFO),
                                        eq("6.2.8.5.a - Global response was not received from Turbocharger (2)"));

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "" + NL;

        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());
    }

}
