/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
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
import org.etools.j1939_84.bus.j1939.packets.CompositeSystem;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step14Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 14;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step14Controller instance;

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

        instance = new Part01Step14Controller(
                                              executor,
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
                                 diagnosticMessageModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM26(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));
        verify(diagnosticMessageModule).requestDM26(any(), eq(1));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNoDm26() {
        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.14.2.f - No OBD ECU provided DM26");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailureForSupportedNotComplete() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.a - Engine #1 (0) response for a monitor Boost pressure control sys in DM5 is reported as supported and is reported as complete/not supported DM26 response");
    }

    @Test
    public void testFailureForDisabledAndNotDisabled() {
        var dm26EnabledSystems = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.CATALYST,
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, dm26EnabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        var dm5SupportedSystem = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, dm5SupportedSystem, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Engine #1 (0) response for a monitor Catalyst in DM5 is reported as not supported and is not reported as disabled and complete/not supported by DM26 response");
    }

    @Test
    public void testFailureForDisabledAndNotComplete() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Engine #1 (0) response for a monitor Cold start aid system in DM5 is reported as not supported and is not reported as disabled and complete/not supported by DM26 response");
    }

    @Test
    public void testFailureForDisabledCCM() {
        var dm26EnabledSystems = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.CATALYST,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, dm26EnabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        var dm5SupportedSystems = List.of(
                                          CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                          CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                          CompositeSystem.CATALYST,
                                          CompositeSystem.COMPREHENSIVE_COMPONENT,
                                          CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                          CompositeSystem.EVAPORATIVE_SYSTEM,
                                          CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, dm5SupportedSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component    not enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.c - Engine #1 (0) response for a monitor Comprehensive component in DM5 is reported as supported and is reported as disabled/not supported by DM26 response");
    }

    @Test
    public void testFailureForNonZeroWarmups() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 1, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.d - Engine #1 (0) response indicates number of warm-ups since code clear is not zero");
    }

    @Test
    public void testFailureForNonZeroTSES() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 1, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.e - Engine #1 (0) response indicates time since engine start is not zero");
    }

    @Test
    public void testWarningForDuplicateSystems() {
        var enabledSystems = List.of(
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.EVAPORATIVE_SYSTEM);
        var completeSystems = List.of(
                                      CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.CATALYST,
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER);
        var dm26_0 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);
        var dm26_1 = DM26TripDiagnosticReadinessPacket.create(1, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule0);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(1, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule1);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26_0, dm26_1));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26_0));
        when(diagnosticMessageModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of(dm26_1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));
        verify(diagnosticMessageModule).requestDM26(any(), eq(1));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant     not enabled,     complete" + NL;
        expectedResults += "    Boost pressure control sys not enabled,     complete" + NL;
        expectedResults += "    Catalyst                   not enabled,     complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater  not enabled,     complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.14.3.a - Required monitor Evaporative system is supported by more than one OBD ECU");
    }

    @Test
    public void testFailureForDifference() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));

        var dm26Ds = DM26TripDiagnosticReadinessPacket.create(0, 1, 0, enabledSystems, completeSystems);
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26Ds));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.5.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNack() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, enabledSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        when(diagnosticMessageModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.empty());

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0));
        verify(diagnosticMessageModule).requestDM26(any(), eq(1));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }
}
