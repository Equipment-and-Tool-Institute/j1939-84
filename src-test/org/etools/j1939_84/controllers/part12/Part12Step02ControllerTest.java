/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
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
public class Part12Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 2;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part12Step02Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule);

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
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        // Module 0 responds and doesn't change complete state and has no completions
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM5DiagnosticReadinessPacket.create(0,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of()),
                                  1);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0, List.of(), List.of()), 11);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm26_0 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, List.of(), List.of());
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26_0));

        // Module 1 responds with changing status, but doesn't support any systems (probably not a real world test)
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM5DiagnosticReadinessPacket.create(1, 0, 0, 0x22, List.of(), List.of()), 1);
        obdModuleInformation1.set(DM26TripDiagnosticReadinessPacket.create(1,
                                                                           0,
                                                                           0,
                                                                           List.of(),
                                                                           List.of(CompositeSystem.values())),
                                  11);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm26_1 = DM26TripDiagnosticReadinessPacket.create(1, 0, 0, List.of(), List.of());
        when(communicationsModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of(dm26_1));

        // Module 2 NACKs the request
        dataRepository.putObdModule(new OBDModuleInformation(2));
        var nack = AcknowledgmentPacket.create(2, NACK);
        when(communicationsModule.requestDM26(any(), eq(2))).thenReturn(new RequestResult<>(false, nack));

        // Module 3 responds and doesn't change complete state and has all completions
        OBDModuleInformation obdModuleInformation3 = new OBDModuleInformation(3);
        obdModuleInformation3.set(DM5DiagnosticReadinessPacket.create(3,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of(CompositeSystem.values())),
                                  1);
        obdModuleInformation3.set(DM26TripDiagnosticReadinessPacket.create(3,
                                                                           0,
                                                                           0,
                                                                           List.of(CompositeSystem.values()),
                                                                           List.of(CompositeSystem.values())),
                                  11);
        dataRepository.putObdModule(obdModuleInformation3);
        var dm26_3 = DM26TripDiagnosticReadinessPacket.create(3,
                                                              0,
                                                              0,
                                                              List.of(CompositeSystem.values()),
                                                              List.of(CompositeSystem.values()));
        when(communicationsModule.requestDM26(any(), eq(3))).thenReturn(RequestResult.of(dm26_3));

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));
        verify(communicationsModule).requestDM26(any(), eq(1));
        verify(communicationsModule).requestDM26(any(), eq(2));
        verify(communicationsModule).requestDM26(any(), eq(3));

        assertSame(dm26_0, dataRepository.getObdModule(0).getLatest(DM26TripDiagnosticReadinessPacket.class));
        assertSame(dm26_1, dataRepository.getObdModule(1).getLatest(DM26TripDiagnosticReadinessPacket.class));
        assertSame(dm26_3, dataRepository.getObdModule(3).getLatest(DM26TripDiagnosticReadinessPacket.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testInfoTwelveTwoB() {
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

        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, enabledSystems, completeSystems), 11);
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
                       11);
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
                       11);
        dataRepository.putObdModule(obdModule3);
        DM26TripDiagnosticReadinessPacket packet3 = DM26TripDiagnosticReadinessPacket.create(
                                                                                             0x03,
                                                                                             0x00,
                                                                                             0x00,
                                                                                             enabledSystemsObd3,
                                                                                             completedSystemsObd3);
        when(communicationsModule.requestDM26(any(CommunicationsListener.class),
                                              eq(0x03))).thenReturn(RequestResult.of(packet3));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(12),
                                        eq(2),
                                        eq(INFO),
                                        eq("6.12.2.2.b - DM5 message in 6.11.10.1.a from Turbocharger (2) monitor reported supported and DM26 message reported complete or not supported"));

        assertEquals("", listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testFailureForChangingCompletion() {

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


        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).setJ1939(eq(j1939));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x01));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x02));
        verify(communicationsModule).requestDM26(any(CommunicationsListener.class), eq(0x03));

        verify(mockListener).addOutcome(eq(12),
                                        eq(2),
                                        eq(WARN),
                                        eq("6.12.2.2.a - Required monitor Boost pressure control sys is supported by more than one OBD ECU"));

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());

        assertEquals("", listener.getMessages());

    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of());

        runTest();

        verify(communicationsModule).requestDM26(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.c - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }
}
