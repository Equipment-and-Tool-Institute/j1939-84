/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
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
import static org.etools.j1939_84.model.Outcome.PASS;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.OBDTestsModule;
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
    private DiagnosticReadinessModule diagnosticReadinessModule;

    private TableA6Validator instance;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDTestsModule obdTestsModule;

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
                diagnosticReadinessModule,
                mockListener,
                obdTestsModule,
                vehicleInformationModule);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.TableA6Validator#verify(org.etools.j1939_84.controllers.ResultsListener, org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testVerifyCompression() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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

        assertTrue(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "PASS: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("PASS: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("PASS: TableA6 Catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Cold start aid system verification")
                .append(NL)
                .append("PASS: TableA6 Comprehensive component verification")
                .append(NL)
                .append("PASS: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("PASS: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("PASS: TableA6 Evaporative system verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("PASS: TableA6 Fuel System verification")
                .append(NL)
                .append("PASS: TableA6 Heated catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Misfire verification")
                .append(NL)
                .append("PASS: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("PASS: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("PASS: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                "TableA6 Boost pressure control sys verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Catalyst verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                "TableA6 Diesel Particulate Filter verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Evaporative system verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Heated catalyst verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS,
                "TableA6 NMHC converting catalyst verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, PASS, "TableA6 Secondary air system verification");

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.TableA6Validator#verify(org.etools.j1939_84.controllers.ResultsListener, org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testVerifyDiesel() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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

        assertTrue(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "PASS: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("PASS: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("PASS: TableA6 Catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Cold start aid system verification")
                .append(NL)
                .append("PASS: TableA6 Comprehensive component verification")
                .append(NL)
                .append("PASS: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("PASS: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("PASS: TableA6 Evaporative system verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("WARN: TableA6 Exhaust Gas Sensor heater is not enabled, not complete")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("PASS: TableA6 Fuel System verification")
                .append(NL)
                .append("PASS: TableA6 Heated catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Misfire verification")
                .append(NL)
                .append("PASS: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("PASS: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("PASS: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Boost pressure control sys verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Diesel Particulate Filter verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Evaporative system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Exhaust Gas Sensor heater is not enabled, not complete");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Heated catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 NMHC converting catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Secondary air system verification");
    }

    @Test
    public void testVerifyDieselFail() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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
        when(dataRepository.isAfterCodeClear()).thenReturn(true);

        assertFalse(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "FAIL: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("FAIL: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("FAIL: TableA6 Catalyst verification")
                .append(NL)
                .append("FAIL: TableA6 Cold start aid system verification")
                .append(NL)
                .append("FAIL: TableA6 Comprehensive component verification")
                .append(NL)
                .append("FAIL: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("FAIL: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("FAIL: TableA6 Evaporative system verification")
                .append(NL)
                .append("FAIL: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("FAIL: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("FAIL: TableA6 Fuel System verification")
                .append(NL)
                .append("FAIL: TableA6 Heated catalyst verification")
                .append(NL)
                .append("WARN: TableA6 Misfire is     enabled, not complete")
                .append(NL)
                .append("FAIL: TableA6 Misfire verification")
                .append(NL)
                .append("FAIL: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("FAIL: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("FAIL: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Misfire is     enabled, not complete");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Boost pressure control sys verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Catalyst verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Diesel Particulate Filter verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Evaporative system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Heated catalyst verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 NMHC converting catalyst verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Secondary air system verification");
    }

    @Test
    public void testVerifyElectricFail() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BATT_ELEC);

        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.isAfterCodeClear()).thenReturn(true);

        assertFalse(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "FAIL: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("WARN: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("WARN: TableA6 Catalyst verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("FAIL: TableA6 Cold start aid system verification")
                .append(NL)
                .append("FAIL: TableA6 Comprehensive component verification")
                .append(NL)
                .append("WARN: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("FAIL: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("WARN: TableA6 Evaporative system verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("FAIL: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("FAIL: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("FAIL: TableA6 Fuel System verification")
                .append(NL)
                .append("WARN: TableA6 Heated catalyst verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("FAIL: TableA6 Misfire verification")
                .append(NL)
                .append("WARN: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("This test is only valid for compression or spark ignition")
                .append(NL)
                .append("FAIL: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("FAIL: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Boost pressure control sys verification" + NL
                + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, WARN,
                "TableA6 Catalyst verification" + NL + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Diesel Particulate Filter verification" + NL
                + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Evaporative system verification" + NL
                + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Heated catalyst verification" + NL
                + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 NMHC converting catalyst verification" + NL
                + "This test is only valid for compression or spark ignition");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Secondary air system verification");

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.TableA6Validator#verify(org.etools.j1939_84.controllers.ResultsListener, org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testVerifySpark() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, false, false);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, true);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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

        assertTrue(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "PASS: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("PASS: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("PASS: TableA6 Catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Cold start aid system verification")
                .append(NL)
                .append("PASS: TableA6 Comprehensive component verification")
                .append(NL)
                .append("PASS: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("PASS: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("PASS: TableA6 Evaporative system verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("PASS: TableA6 Fuel System verification")
                .append(NL)
                .append("PASS: TableA6 Heated catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Misfire verification")
                .append(NL)
                .append("PASS: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("PASS: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("PASS: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Boost pressure control sys verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Diesel Particulate Filter verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Evaporative system verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Heated catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 NMHC converting catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Secondary air system verification");

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.TableA6Validator#verify(org.etools.j1939_84.controllers.ResultsListener, org.etools.j1939_84.bus.Packet)}.
     */
    @Test
    public void testVerifySparkFail() {

        Set<MonitoredSystem> systems = new HashSet<>();

        MonitoredSystemStatus status1 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT.getName(), status1, 0,
                                                      AC_SYSTEM_REFRIGERANT, true);

        MonitoredSystemStatus status2 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS.getName(), status2, 0x00,
                                                      BOOST_PRESSURE_CONTROL_SYS, true);

        MonitoredSystemStatus status3 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST.getName(), status3, 0x00, CATALYST, true);

        MonitoredSystemStatus status4 = MonitoredSystemStatus.findStatus(false, false, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM.getName(), status4, 0x00,
                                                      COLD_START_AID_SYSTEM, true);

        MonitoredSystemStatus status5 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT.getName(), status5, 0x00,
                                                      COMPREHENSIVE_COMPONENT, true);

        MonitoredSystemStatus status6 = MonitoredSystemStatus.findStatus(true, true, false);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER.getName(), status6, 0x00,
                                                      DIESEL_PARTICULATE_FILTER, true);

        MonitoredSystemStatus status7 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM.getName(), status7, 0x00, EGR_VVT_SYSTEM, true);

        MonitoredSystemStatus status8 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM.getName(), status8, 0x00, EVAPORATIVE_SYSTEM,
                                                      true);

        MonitoredSystemStatus status9 = MonitoredSystemStatus.findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR.getName(), status9, 0x00, EXHAUST_GAS_SENSOR,
                                                      true);

        MonitoredSystemStatus status10 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER.getName(), status10, 0x00,
                                                       EXHAUST_GAS_SENSOR_HEATER, true);

        MonitoredSystemStatus status11 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM.getName(), status11, 0x00, FUEL_SYSTEM, true);

        MonitoredSystemStatus status12 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST.getName(), status12, 0x00, HEATED_CATALYST, true);

        MonitoredSystemStatus status13 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE.getName(), status13, 0x00, MISFIRE, true);

        MonitoredSystemStatus status14 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST.getName(), status14, 0x00,
                                                       NMHC_CONVERTING_CATALYST, true);

        MonitoredSystemStatus status15 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER.getName(), status15, 0x00,
                                                       NOX_CATALYST_ABSORBER, true);

        MonitoredSystemStatus status16 = MonitoredSystemStatus.findStatus(false, true, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM.getName(), status16, 0x00,
                                                       SECONDARY_AIR_SYSTEM, true);

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
        when(dataRepository.isAfterCodeClear()).thenReturn(false);

        assertFalse(instance.verify(listener, packet1, 1, 2));
        StringBuilder expectedMessages = new StringBuilder(
                "FAIL: TableA6 A/C system refrigerant verification");
        expectedMessages.append(NL)
                .append("FAIL: TableA6 Boost pressure control sys verification")
                .append(NL)
                .append("FAIL: TableA6 Catalyst verification")
                .append(NL)
                .append("WARN: TableA6 Cold start aid system is not enabled,     complete")
                .append(NL)
                .append("FAIL: TableA6 Cold start aid system verification")
                .append(NL)
                .append("FAIL: TableA6 Comprehensive component verification")
                .append(NL)
                .append("FAIL: TableA6 Diesel Particulate Filter verification")
                .append(NL)
                .append("WARN: TableA6 EGR/VVT system is not supported,     complete")
                .append(NL)
                .append("FAIL: TableA6 EGR/VVT system verification")
                .append(NL)
                .append("FAIL: TableA6 Evaporative system verification")
                .append(NL)
                .append("FAIL: TableA6 Exhaust Gas Sensor verification")
                .append(NL)
                .append("PASS: TableA6 Exhaust Gas Sensor heater verification")
                .append(NL)
                .append("PASS: TableA6 Fuel System verification")
                .append(NL)
                .append("PASS: TableA6 Heated catalyst verification")
                .append(NL)
                .append("PASS: TableA6 Misfire verification")
                .append(NL)
                .append("FAIL: TableA6 NMHC converting catalyst verification")
                .append(NL)
                .append("FAIL: TableA6 NOx catalyst/adsorber verification")
                .append(NL)
                .append("PASS: TableA6 Secondary air system verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository, atLeast(1)).getVehicleInformation();
        verify(dataRepository, atLeast(1)).isAfterCodeClear();

        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 A/C system refrigerant verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Boost pressure control sys verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Catalyst verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 Cold start aid system is not enabled,     complete");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Cold start aid system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Comprehensive component verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Diesel Particulate Filter verification");
        verify(mockListener).addOutcome(1, 2, WARN, "TableA6 EGR/VVT system is not supported,     complete");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 EGR/VVT system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Evaporative system verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 Exhaust Gas Sensor verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Exhaust Gas Sensor heater verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Fuel System verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Heated catalyst verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Misfire verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 NMHC converting catalyst verification");
        verify(mockListener).addOutcome(1, 2, FAIL, "TableA6 NOx catalyst/adsorber verification");
        verify(mockListener).addOutcome(1, 2, PASS, "TableA6 Secondary air system verification");

    }

}
