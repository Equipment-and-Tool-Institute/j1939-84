/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.CATALYST;
import static org.etools.j1939tools.j1939.packets.CompositeSystem.MISFIRE;
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
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
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
public class Part12Step03ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 3;

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

        instance = new Part12Step03Controller(executor,
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
                                  11);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0, List.of(), List.of()), 12);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm5_0 = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, List.of(), List.of());
        when(communicationsModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5_0));

        // Module 1 responds with changing status, but doesn't support any systems (probably not a real world test)
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM5DiagnosticReadinessPacket.create(1,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(),
                                                                      List.of(CompositeSystem.values())),
                                  11);
        obdModuleInformation1.set(DM26TripDiagnosticReadinessPacket.create(1,
                                                                           0,
                                                                           0,
                                                                           List.of(),
                                                                           List.of()),
                                  12);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm5_1 = DM5DiagnosticReadinessPacket.create(1, 0, 0, 0x22, List.of(), List.of());
        when(communicationsModule.requestDM5(any(), eq(1))).thenReturn(BusResult.of(dm5_1));

        // Module 3 responds and doesn't change complete state and has all completions
        OBDModuleInformation obdModuleInformation3 = new OBDModuleInformation(3);
        obdModuleInformation3.set(DM5DiagnosticReadinessPacket.create(3,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of(CompositeSystem.values())),
                                  11);
        obdModuleInformation3.set(DM26TripDiagnosticReadinessPacket.create(3,
                                                                           0,
                                                                           0,
                                                                           List.of(CompositeSystem.values()),
                                                                           List.of(CompositeSystem.values())),
                                  12);
        dataRepository.putObdModule(obdModuleInformation3);
        var dm5_3 = DM5DiagnosticReadinessPacket.create(3,
                                                        0,
                                                        0,
                                                        0x22,
                                                        List.of(CompositeSystem.values()),
                                                        List.of(CompositeSystem.values()));
        when(communicationsModule.requestDM5(any(), eq(3))).thenReturn(BusResult.of(dm5_3));

        runTest();

        verify(communicationsModule).requestDM5(any(), eq(0));
        verify(communicationsModule).requestDM5(any(), eq(1));
        verify(communicationsModule).requestDM5(any(), eq(3));

        assertEquals("", listener.getMessages());
        String expected = "" + NL;
        expected += "Vehicle Composite of DM5:" + NL;
        expected += "    Comprehensive component        supported, not complete" + NL
                + "    Fuel System                    supported, not complete" + NL
                + "    Misfire                        supported, not complete" + NL
                + "    EGR/VVT system                 supported, not complete" + NL
                + "    Exhaust Gas Sensor heater      supported, not complete" + NL
                + "    Exhaust Gas Sensor             supported, not complete" + NL
                + "    A/C system refrigerant         supported, not complete" + NL
                + "    Secondary air system           supported, not complete" + NL
                + "    Evaporative system             supported, not complete" + NL
                + "    Heated catalyst                supported, not complete" + NL
                + "    Catalyst                       supported, not complete" + NL
                + "    NMHC converting catalyst       supported, not complete" + NL
                + "    NOx catalyst/adsorber          supported, not complete" + NL
                + "    Diesel Particulate Filter      supported, not complete" + NL
                + "    Boost pressure control sys     supported, not complete" + NL
                + "    Cold start aid system          supported, not complete" + NL;
        assertEquals(expected, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForChangingCompletion() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM5DiagnosticReadinessPacket.create(0,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of(CompositeSystem.values())),
                                  11);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0, 0, 0, List.of(), List.of()), 12);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm5_0 = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22, List.of(CompositeSystem.values()), List.of());
        when(communicationsModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5_0));

        runTest();

        verify(communicationsModule).requestDM5(any(), eq(0));

        assertEquals("", listener.getMessages());
        String expected = "" + NL;
        expected += "Vehicle Composite of DM5:" + NL;
        expected += "    Comprehensive component        supported, not complete" + NL
                + "    Fuel System                    supported, not complete" + NL
                + "    Misfire                        supported, not complete" + NL
                + "    EGR/VVT system                 supported, not complete" + NL
                + "    Exhaust Gas Sensor heater      supported, not complete" + NL
                + "    Exhaust Gas Sensor             supported, not complete" + NL
                + "    A/C system refrigerant         supported, not complete" + NL
                + "    Secondary air system           supported, not complete" + NL
                + "    Evaporative system             supported, not complete" + NL
                + "    Heated catalyst                supported, not complete" + NL
                + "    Catalyst                       supported, not complete" + NL
                + "    NMHC converting catalyst       supported, not complete" + NL
                + "    NOx catalyst/adsorber          supported, not complete" + NL
                + "    Diesel Particulate Filter      supported, not complete" + NL
                + "    Boost pressure control sys     supported, not complete" + NL
                + "    Cold start aid system          supported, not complete" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported A/C system refrigerant as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Boost pressure control sys as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Catalyst as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Cold start aid system as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Diesel Particulate Filter as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported EGR/VVT system as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Evaporative system as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Exhaust Gas Sensor as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Exhaust Gas Sensor heater as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Fuel System as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Heated catalyst as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Misfire as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported NMHC converting catalyst as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported NOx catalyst/adsorber as 'complete' in part 11 and is now reporting 'not complete'");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.3.2.a - Engine #1 (0) reported Secondary air system as 'complete' in part 11 and is now reporting 'not complete'");
    }

    @Test
    public void testWarningForFewerCompletions() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM5DiagnosticReadinessPacket.create(0,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of()),
                                  11);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0,
                                                                           0,
                                                                           0,
                                                                           List.of(),
                                                                           List.of(MISFIRE, CATALYST)),
                                  12);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm5_0 = DM5DiagnosticReadinessPacket.create(0,
                                                        0,
                                                        0,
                                                        0x22,
                                                        List.of(CompositeSystem.values()),
                                                        List.of(CATALYST));
        when(communicationsModule.requestDM5(any(), eq(0))).thenReturn(BusResult.of(dm5_0));

        runTest();

        verify(communicationsModule).requestDM5(any(), eq(0));

        assertEquals("", listener.getMessages());

        String expected = "" + NL;
        expected += "Vehicle Composite of DM5:" + NL;
        expected += "    Comprehensive component        supported, not complete" + NL
                + "    Fuel System                    supported, not complete" + NL
                + "    Misfire                        supported, not complete" + NL
                + "    EGR/VVT system                 supported, not complete" + NL
                + "    Exhaust Gas Sensor heater      supported, not complete" + NL
                + "    Exhaust Gas Sensor             supported, not complete" + NL
                + "    A/C system refrigerant         supported, not complete" + NL
                + "    Secondary air system           supported, not complete" + NL
                + "    Evaporative system             supported, not complete" + NL
                + "    Heated catalyst                supported, not complete" + NL
                + "    Catalyst                       supported,     complete" + NL
                + "    NMHC converting catalyst       supported, not complete" + NL
                + "    NOx catalyst/adsorber          supported, not complete" + NL
                + "    Diesel Particulate Filter      supported, not complete" + NL
                + "    Boost pressure control sys     supported, not complete" + NL
                + "    Cold start aid system          supported, not complete" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.12.3.3.a - Engine #1 (0) DM5 reported fewer complete monitors than DM26 in step 6.12.2.1");
    }

}
