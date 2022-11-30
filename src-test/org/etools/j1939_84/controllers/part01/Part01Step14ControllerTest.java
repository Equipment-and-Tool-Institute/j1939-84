/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.model.FuelType.DSL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
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
@TestDoc(value = @TestItem(verifies = "Part 1 Step 14", description = "DM26: Diagnostic readiness 3"))
public class Part01Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 14;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

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

    /**
     * Test method for
     * {@link Part01Step14Controller#getDisplayName()}
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#getStepNumber()}
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#getTotalSteps()}
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test two modules responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list enabled/complete systems<br>
     * matching DM5 stored</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list enabled/complete systems</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">no
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">NACK
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.1.a", description = "Global DM26 [(send Request (PG 59904) for PG 64952 (SPs 3301-3305))]"),
            @TestItem(verifies = "6.1.14.1.a.i", description = "Create list by OBD ECU address of all data and current status for use later in the test"),
            @TestItem(verifies = "6.1.14.1.b", description = "Display monitor readiness composite value in log for OBD ECU replies only"),
            @TestItem(verifies = "6.1.14.4.a", description = "DS DM26 to each OBD ECU") })
    public void testRunNoFailures() {
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
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.HEATED_CATALYST);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x00))).thenReturn(RequestResult.of(dm26));

        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        var nack = AcknowledgmentPacket.create(0x01, NACK);
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(new RequestResult<>(false, nack));

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x01));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL
                + "    Fuel System                not enabled,     complete" + NL
                + "    Misfire                    not enabled,     complete" + NL
                + "    EGR/VVT system             not enabled, not complete" + NL
                + "    Exhaust Gas Sensor heater      enabled, not complete" + NL
                + "    Exhaust Gas Sensor         not enabled, not complete" + NL
                + "    A/C system refrigerant         enabled, not complete" + NL
                + "    Secondary air system       not enabled,     complete" + NL
                + "    Evaporative system             enabled, not complete" + NL
                + "    Heated catalyst            not enabled, not complete" + NL
                + "    Catalyst                       enabled, not complete" + NL
                + "    NMHC converting catalyst   not enabled,     complete" + NL
                + "    NOx catalyst/adsorber      not enabled,     complete" + NL
                + "    Diesel Particulate Filter      enabled, not complete" + NL
                + "    Boost pressure control sys     enabled, not complete" + NL
                + "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());

    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test two modules responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">N/A<br>
     * no modules</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list enabled/complete systems</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no DM26
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.g", description = "Fail if no OBD ECU provides DM26") })
    public void testObdProvidedDM26Failure() {
        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.14.2.g - No OBD ECU provided DM26");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one enabled/complete DM5 monitor</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one enabled/complete DM5 monitor</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.a", description = "Fail if any response for any monitor supported in DM5 by a given ECU is reported as “0=monitor complete this cycle or not supported” in SP 3303 bits 1-4 and SP 3305 [except comprehensive components monitor (CCM)]") })
    public void testMonitorSupportedCompleteFailure() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);

        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COLD_START_AID_SYSTEM,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.MISFIRE);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(), eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys not enabled,     complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.a - Engine #1 (0) response for a monitor Boost pressure control sys in DM5 is reported as supported and is reported as complete/not supported DM26 response");
    }

    @Test
    public void testMonitorSupportedCompleteFailureWithMisfireOn2019() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2019);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);

        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COLD_START_AID_SYSTEM,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.MISFIRE);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(), eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys not enabled,     complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.a - Engine #1 (0) response for a monitor Boost pressure control sys in DM5 is reported as supported and is reported as complete/not supported DM26 response");
    }

    @Test
    public void testMonitorSupportedCompleteFailureWithMisfireOn2018() {
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setVehicleModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);

        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COLD_START_AID_SYSTEM,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.MISFIRE);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(), eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys not enabled,     complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.a - Engine #1 (0) response for a monitor Boost pressure control sys in DM5 is reported as supported and is reported as complete/not supported DM26 response");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.a - Engine #1 (0) response for a monitor Misfire in DM5 is reported as supported and is reported as complete/not supported DM26 response");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one dis-enabled/in-complete DM5 monitor</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one dis-enabled/in-complete DM5 monitor</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.d", description = "Fail if any response from an ECU indicating support for CCM monitor in DM5 reports “0=monitor disabled for rest of this cycle or not supported” in SP 3303 bit 3") })
    public void testDisabledAndNotDisabledFailure() {
        var dm26EnabledSystems = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        var completeSystems = List.of(
                                      CompositeSystem.CATALYST,
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, dm26EnabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5SupportedSystem = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.CATALYST,
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.EXHAUST_GAS_SENSOR);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5SupportedSystem, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any())).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component    not enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                        enabled, not complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system           enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst                enabled, not complete" + NL;
        expectedResults += "    Catalyst                   not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.d - Engine #1 (0) indicates support for Comprehensive component in DM5 is reported as disabled/not supported in SP 3303 bit 3");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one dis-enabled/in-complete DM5 monitor</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one dis-enabled/in-complete DM5 monitor</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.b", description = "Fail if any response from an ECU indicating support for CCM monitor in DM5 reports “0=monitor disabled for rest of this cycle or not supported” in SP 3303 bit 3") })
    public void testMonitorNotSupportedAndNotCompleteFailure() {
        var enabledSystems = List.of(CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     // CompositeSystem.COLD_START_AID_SYSTEM,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
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
                                        CompositeSystem.NOX_CATALYST_ABSORBER);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM);

        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled, not complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor             enabled, not complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Engine #1 (0) response for a monitor Cold start aid system in DM5 is reported as not supported and is reported as not complete by DM26 response");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one DM5/DM26 enable mis-matched monitor</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * full list one DM5/DM26 enable mis-matched monitor</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.c", description = "Fail if any response for each monitor not supported in DM5 by a given ECU is also reported in DM26 as “0=monitor enabled for this monitoring cycle” in SP 3303 bits 1 and 2 and SP 3304") })
    public void testNotSupportedAndNotEnabledFailure() {
        var dm26EnabledSystems = List.of(
                                         CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.CATALYST,
                                         CompositeSystem.COMPREHENSIVE_COMPONENT,
                                         CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.HEATED_CATALYST);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, dm26EnabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5SupportedSystems = List.of(
                                          CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                          CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                          CompositeSystem.CATALYST,
                                          CompositeSystem.COMPREHENSIVE_COMPONENT,
                                          CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                          CompositeSystem.EVAPORATIVE_SYSTEM,
                                          CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5SupportedSystems, completeSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor             enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.c - Engine #1 (0) response for a monitor Exhaust Gas Sensor in DM5 is reported as not supported and is not reported as disabled and complete/not supported by DM26 response");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * non-zero warmups</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * non-zero warmups</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.e", description = "Fail if any response indicates number of warm-ups since code clear (WU-SCC) (SP 3302) is not zero.") })
    public void testNonZeroWarmupsFailure() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COLD_START_AID_SYSTEM,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                     CompositeSystem.EGR_VVT_SYSTEM);
        var completeSystems = List.of(
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NOX_CATALYST_ABSORBER);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 1, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COLD_START_AID_SYSTEM,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.NMHC_CONVERTING_CATALYST,
                                        CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system          enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.e - Engine #1 (0) response indicates number of warm-ups since code clear is not zero");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * non-zero warmups</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * non-zero warmups</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.2.f", description = "Fail if any response indicates time since engine start (SP 3301) is not zero") })
    public void testNonZeroTimeSinceEngineStartFailure() {
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
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 1, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.HEATED_CATALYST);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.SECONDARY_AIR_SYSTEM,
                                         CompositeSystem.NOX_CATALYST_ABSORBER,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.HEATED_CATALYST);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x00))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.f - Engine #1 (0) response indicates time since engine start is not zero");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * contains required monitor</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * contains required monitor</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * contains required monitor</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * contains required monitor</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.3.a", description = "Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU") })
    public void testMonitorSupportedByDuplicateSystemsWarning() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM);
        var dm26_0 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        var dm26_1 = DM26TripDiagnosticReadinessPacket.create(0x01, 0, 0, enabledSystems, completeSystems);

        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.FUEL_SYSTEM,
                                        CompositeSystem.HEATED_CATALYST,
                                        CompositeSystem.MISFIRE,
                                        CompositeSystem.NOX_CATALYST_ABSORBER,
                                        CompositeSystem.NMHC_CONVERTING_CATALYST,
                                        CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                         CompositeSystem.CATALYST,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EVAPORATIVE_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST);
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        obdModule0.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule0);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.set(DM5DiagnosticReadinessPacket.create(0x01, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26_0, dm26_1));
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x00))).thenReturn(RequestResult.of(dm26_0));
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x01))).thenReturn(RequestResult.of(dm26_1));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x01));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled, not complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater  not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled, not complete" + NL;
        expectedResults += "    Evaporative system         not enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Catalyst                   not enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter  not enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys not enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.14.3.a - Required monitor A/C system refrigerant is supported by more than one OBD ECU");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * time since clear code one value</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response<br>
     * time since clear code differs</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.5.a", description = "Fail if any difference compared to data received during global request") })
    public void testGlobalDifferFromDSFailure() {
        var enabledSystems = List.of(
                                     CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER);
        var completeSystems = List.of(
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.EXHAUST_GAS_SENSOR,
                                      CompositeSystem.MISFIRE,
                                      CompositeSystem.NMHC_CONVERTING_CATALYST,
                                      CompositeSystem.NOX_CATALYST_ABSORBER,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COLD_START_AID_SYSTEM,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EGR_VVT_SYSTEM,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.HEATED_CATALYST);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.COLD_START_AID_SYSTEM,
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));

        var dm26Ds = DM26TripDiagnosticReadinessPacket.create(0x00, 1, 0, enabledSystems, completeSystems);
        when(communicationsModule.requestDM26(any(ResultsListener.class),
                                              eq(0x00))).thenReturn(RequestResult.of(dm26Ds));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled,     complete" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled,     complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled, not complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.5.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    /**
     * Test method for {@link Part01Step14Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Global
     * Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DS
     * Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00<br>
     * OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good DM26
     * response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01<br>
     * OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no DM26
     * response</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">no DM26
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.14.5.b", description = "Fail if NACK not received from OBD ECUs that did not respond to global query") })
    public void testNoNackReceivedFailure() {
        var enabledSystems = List.of(
                                     CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                     CompositeSystem.CATALYST,
                                     CompositeSystem.COMPREHENSIVE_COMPONENT,
                                     CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                     CompositeSystem.EVAPORATIVE_SYSTEM,
                                     CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                     CompositeSystem.NOX_CATALYST_ABSORBER);
        var completeSystems = List.of(
                                      CompositeSystem.COLD_START_AID_SYSTEM,
                                      CompositeSystem.EGR_VVT_SYSTEM,
                                      CompositeSystem.FUEL_SYSTEM,
                                      CompositeSystem.HEATED_CATALYST,
                                      CompositeSystem.SECONDARY_AIR_SYSTEM);
        var dm26 = DM26TripDiagnosticReadinessPacket.create(0x00, 0, 0, enabledSystems, completeSystems);

        OBDModuleInformation obdModule = new OBDModuleInformation(0x00);
        var dm5EnabledSystems = List.of(
                                        CompositeSystem.AC_SYSTEM_REFRIGERANT,
                                        CompositeSystem.BOOST_PRESSURE_CONTROL_SYS,
                                        CompositeSystem.CATALYST,
                                        CompositeSystem.COMPREHENSIVE_COMPONENT,
                                        CompositeSystem.DIESEL_PARTICULATE_FILTER,
                                        CompositeSystem.EVAPORATIVE_SYSTEM,
                                        CompositeSystem.EXHAUST_GAS_SENSOR,
                                        CompositeSystem.EXHAUST_GAS_SENSOR_HEATER,
                                        CompositeSystem.MISFIRE,
                                        CompositeSystem.NMHC_CONVERTING_CATALYST,
                                        CompositeSystem.NOX_CATALYST_ABSORBER);
        var dm5CompleteSystems = List.of(
                                         CompositeSystem.EGR_VVT_SYSTEM,
                                         CompositeSystem.EXHAUST_GAS_SENSOR,
                                         CompositeSystem.FUEL_SYSTEM,
                                         CompositeSystem.HEATED_CATALYST,
                                         CompositeSystem.MISFIRE,
                                         CompositeSystem.NMHC_CONVERTING_CATALYST,
                                         CompositeSystem.NOX_CATALYST_ABSORBER);
        obdModule.set(DM5DiagnosticReadinessPacket.create(0x00, 0, 0, 0x22, dm5EnabledSystems, dm5CompleteSystems), 1);
        dataRepository.putObdModule(obdModule);
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0x00))).thenReturn(RequestResult.of(dm26));

        dataRepository.putObdModule(new OBDModuleInformation(0x01));
        when(communicationsModule.requestDM26(any(ResultsListener.class), eq(0x01))).thenReturn(RequestResult.empty());

        when(communicationsModule.requestDM26(any(ResultsListener.class))).thenReturn(RequestResult.of(dm26));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM26(any(ResultsListener.class));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x00));
        verify(communicationsModule).requestDM26(any(ResultsListener.class), eq(0x01));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Misfire                    not enabled, not complete" + NL;

        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    A/C system refrigerant     not enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Heated catalyst            not enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += NL;
        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }
}
