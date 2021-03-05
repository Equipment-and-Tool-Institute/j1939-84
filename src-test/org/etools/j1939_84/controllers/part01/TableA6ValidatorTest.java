/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.COMPREHENSIVE_COMPONENT;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.MISFIRE;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939_84.bus.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link TableA6Validator}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TableA6ValidatorTest {

    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 2;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    private TableA6Validator instance;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        instance = new TableA6Validator(dataRepository);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(
                                 dataRepository,
                                 diagnosticMessageModule,
                                 mockListener,
                                 diagnosticMessageModule,
                                 vehicleInformationModule);
    }

    /**
     * Test method for
     */
    @Test
    public void testVerifyCompression() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.DSL);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.isAfterCodeClear()).thenReturn(false);

        assertTrue(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        verify(dataRepository, times(12)).getVehicleInformation();
        verify(dataRepository, times(9)).isAfterCodeClear();
    }

    /**
     * Test method for
     */
    @Test
    public void testVerifyDiesel() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.DSL);
        when(vehicleInformation.getEngineModelYear()).thenReturn(2013);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.isAfterCodeClear()).thenReturn(false);

        assertTrue(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "TableA6 Exhaust Gas Sensor heater is not enabled, not complete");

    }

    @Test
    public void testVerifyDieselFail() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        assertFalse(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "TableA6 A/C system refrigerant verification");
    }

    @Test
    public void testVerifyElectricFail() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        assertFalse(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "TableA6 A/C system refrigerant verification");
    }

    /**
     * Test method for
     */
    @Test
    public void testVerifySpark() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.GAS);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.isAfterCodeClear()).thenReturn(true);

        assertTrue(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

    }

    @Test
    public void testVerifySparkFail() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(
                                                      AC_SYSTEM_REFRIGERANT,
                                                      status1,
                                                      0,
                                                      true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system2 = new MonitoredSystem(
                                                      BOOST_PRESSURE_CONTROL_SYS,
                                                      status2,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(
                                                      COLD_START_AID_SYSTEM,
                                                      status4,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(
                                                      COMPREHENSIVE_COMPONENT,
                                                      status5,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system6 = new MonitoredSystem(
                                                      DIESEL_PARTICULATE_FILTER,
                                                      status6,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(
                                                      EVAPORATIVE_SYSTEM,
                                                      status8,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(
                                                      EXHAUST_GAS_SENSOR,
                                                      status9,
                                                      0x00,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system10 = new MonitoredSystem(
                                                       EXHAUST_GAS_SENSOR_HEATER,
                                                       status10,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(
                                                       HEATED_CATALYST,
                                                       status12,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(
                                                       NMHC_CONVERTING_CATALYST,
                                                       status14,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(
                                                       NOX_CATALYST_ABSORBER,
                                                       status15,
                                                       0x00,
                                                       true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(
                                                       SECONDARY_AIR_SYSTEM,
                                                       status16,
                                                       0x00,
                                                       true);

        systems.add(system1);
        systems.add(system2);
        systems.add(system3);
        systems.add(system4);
        systems.add(system5);
        systems.add(system6);
        systems.add(system7);
        systems.add(system8);
        systems.add(system9);
        systems.add(system10);
        systems.add(system11);
        systems.add(system12);
        systems.add(system13);
        systems.add(system14);
        systems.add(system15);
        systems.add(system16);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getMonitoredSystems()).thenReturn(systems);

        assertFalse(instance.verify(listener, packet1, PART_NUMBER, STEP_NUMBER));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "TableA6 A/C system refrigerant verification");
    }

}
