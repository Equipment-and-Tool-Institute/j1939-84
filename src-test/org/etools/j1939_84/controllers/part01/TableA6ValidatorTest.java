/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.AC_SYSTEM_REFRIGERANT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.BOOST_PRESSURE_CONTROL_SYS;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COLD_START_AID_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.COMPREHENSIVE_COMPONENT;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.DIESEL_PARTICULATE_FILTER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EGR_VVT_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EVAPORATIVE_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.EXHAUST_GAS_SENSOR_HEATER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.FUEL_SYSTEM;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.HEATED_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.MISFIRE;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NMHC_CONVERTING_CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.NOX_CATALYST_ABSORBER;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.SECONDARY_AIR_SYSTEM;
import static org.etools.j1939tools.j1939.packets.MonitoredSystemStatus.findStatus;
import static org.etools.j1939tools.modules.CommunicationsModule.getCompositeSystems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.packets.MonitoredSystem;
import org.etools.j1939tools.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939tools.modules.CommunicationsModule;
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

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    private TableA6Validator instance;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dataRepository = DataRepository.newInstance();
        instance = new TableA6Validator(dataRepository, PART_NUMBER, STEP_NUMBER);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockListener,
                                 communicationsModule,
                                 vehicleInformationModule);
    }

    @Test
    public void testVerifyCompressionNotRun() {

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        MonitoredSystemStatus status1 = findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, true, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, false, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, true, true);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

        var systems = List.of(system1,
                              system2,
                              system3,
                              system4,
                              system5,
                              system6,
                              system7,
                              system8,
                              system9,
                              system10,
                              system11,
                              system12,
                              system13,
                              system14,
                              system15,
                              system16);

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", false);

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Boost pressure control sys did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Cold start aid system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Evaporative system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor heater did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Fuel System did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Heated catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Misfire did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Secondary air system did not meet the criteria of Table A4");

    }

    @Test
    public void testVerifyCompressionHasRun() {

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        MonitoredSystemStatus status1 = findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, true, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, false, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, true, true);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

        var systems = List.of(system1,
                              system2,
                              system3,
                              system4,
                              system5,
                              system6,
                              system7,
                              system8,
                              system9,
                              system10,
                              system11,
                              system12,
                              system13,
                              system14,
                              system15,
                              system16);

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", true);

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Boost pressure control sys did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Cold start aid system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Evaporative system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor heater did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        INFO,
                                        "6.1.2.3.a - Fuel System is supported, complete");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Heated catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        INFO,
                                        "6.1.2.3.a - Misfire is supported, complete");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Secondary air system did not meet the criteria of Table A4");

    }

    @Test
    public void testVerifyDiesel() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, false, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, true, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, false, false);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, false, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, false, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

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

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        vehicleInformation.setEngineModelYear(2013);
        dataRepository.setVehicleInformation(vehicleInformation);

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", false);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Boost pressure control sys did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Cold start aid system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Evaporative system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a - Exhaust Gas Sensor heater is not supported, not complete");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor heater did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Fuel System did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Heated catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Misfire did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Secondary air system did not meet the criteria of Table A4");

    }

    @Test
    public void testVerifyDieselFail() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, true, false);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, true, false);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, false, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, false, true);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, false, true);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, true, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, true, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

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

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", false);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Comprehensive component did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor heater did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Heated catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Secondary air system did not meet the criteria of Table A4");
    }

    @Test
    public void testVerifySpark() {

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = findStatus(true, false, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, true, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, true, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, true, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, true, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, false, false);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, true, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, true, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, true, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, false, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, true);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, true, true);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, false, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, false, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, false, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, false, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

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

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", false);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Boost pressure control sys did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Cold start aid system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Evaporative system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor heater did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Fuel System did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Heated catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Misfire did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for NMHC converting catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for NOx catalyst/adsorber did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Secondary air system did not meet the criteria of Table A4");
    }

    @Test
    public void testVerifySparkFail() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        List<MonitoredSystem> systems = new ArrayList<>();

        MonitoredSystemStatus status1 = findStatus(true, true, false);
        MonitoredSystem system1 = new MonitoredSystem(AC_SYSTEM_REFRIGERANT, status1, 0, true);

        MonitoredSystemStatus status2 = findStatus(true, false, true);
        MonitoredSystem system2 = new MonitoredSystem(BOOST_PRESSURE_CONTROL_SYS, status2, 0x00, true);

        MonitoredSystemStatus status3 = findStatus(true, false, true);
        MonitoredSystem system3 = new MonitoredSystem(CATALYST, status3, 0x00, true);

        MonitoredSystemStatus status4 = findStatus(true, false, true);
        MonitoredSystem system4 = new MonitoredSystem(COLD_START_AID_SYSTEM, status4, 0x00, true);

        MonitoredSystemStatus status5 = findStatus(true, false, true);
        MonitoredSystem system5 = new MonitoredSystem(COMPREHENSIVE_COMPONENT, status5, 0x00, true);

        MonitoredSystemStatus status6 = findStatus(true, true, false);
        MonitoredSystem system6 = new MonitoredSystem(DIESEL_PARTICULATE_FILTER, status6, 0x00, true);

        MonitoredSystemStatus status7 = findStatus(true, false, true);
        MonitoredSystem system7 = new MonitoredSystem(EGR_VVT_SYSTEM, status7, 0x00, true);

        MonitoredSystemStatus status8 = findStatus(true, false, true);
        MonitoredSystem system8 = new MonitoredSystem(EVAPORATIVE_SYSTEM, status8, 0x00, true);

        MonitoredSystemStatus status9 = findStatus(true, false, true);
        MonitoredSystem system9 = new MonitoredSystem(EXHAUST_GAS_SENSOR, status9, 0x00, true);

        MonitoredSystemStatus status10 = findStatus(true, true, false);
        MonitoredSystem system10 = new MonitoredSystem(EXHAUST_GAS_SENSOR_HEATER, status10, 0x00, true);

        MonitoredSystemStatus status11 = findStatus(true, true, false);
        MonitoredSystem system11 = new MonitoredSystem(FUEL_SYSTEM, status11, 0x00, true);

        MonitoredSystemStatus status12 = findStatus(true, true, false);
        MonitoredSystem system12 = new MonitoredSystem(HEATED_CATALYST, status12, 0x00, true);

        MonitoredSystemStatus status13 = findStatus(true, true, false);
        MonitoredSystem system13 = new MonitoredSystem(MISFIRE, status13, 0x00, true);

        MonitoredSystemStatus status14 = findStatus(true, true, false);
        MonitoredSystem system14 = new MonitoredSystem(NMHC_CONVERTING_CATALYST, status14, 0x00, true);

        MonitoredSystemStatus status15 = findStatus(true, true, false);
        MonitoredSystem system15 = new MonitoredSystem(NOX_CATALYST_ABSORBER, status15, 0x00, true);

        MonitoredSystemStatus status16 = findStatus(true, true, false);
        MonitoredSystem system16 = new MonitoredSystem(SECONDARY_AIR_SYSTEM, status16, 0x00, true);

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

        instance.verify(listener, getCompositeSystems(systems, true), "6.1.2.3.a", false);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for A/C system refrigerant did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a - Cold start aid system is not supported, complete");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Comprehensive component did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Diesel Particulate Filter did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        WARN,
                                        "6.1.2.3.a - EGR/VVT system is not supported, complete");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for EGR/VVT system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Evaporative system did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Exhaust Gas Sensor did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for Misfire did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for NMHC converting catalyst did not meet the criteria of Table A4");
        verify(mockListener).addOutcome(
                                        1,
                                        2,
                                        FAIL,
                                        "6.1.2.3.a - Composite vehicle readiness for NOx catalyst/adsorber did not meet the criteria of Table A4");

    }

}
