/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
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
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
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
    private DiagnosticMessageModule diagnosticMessageModule;

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
                                              diagnosticMessageModule);

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
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26_0));

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
        when(diagnosticMessageModule.requestDM26(any(), eq(1))).thenReturn(RequestResult.of(dm26_1));

        // Module 2 NACKs the request
        dataRepository.putObdModule(new OBDModuleInformation(2));
        var nack = AcknowledgmentPacket.create(2, NACK);
        when(diagnosticMessageModule.requestDM26(any(), eq(2))).thenReturn(new RequestResult<>(false, nack));

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
        when(diagnosticMessageModule.requestDM26(any(), eq(3))).thenReturn(RequestResult.of(dm26_3));

        runTest();

        verify(diagnosticMessageModule).requestDM26(any(), eq(0));
        verify(diagnosticMessageModule).requestDM26(any(), eq(1));
        verify(diagnosticMessageModule).requestDM26(any(), eq(2));
        verify(diagnosticMessageModule).requestDM26(any(), eq(3));

        assertSame(dm26_0, dataRepository.getObdModule(0).getLatest(DM26TripDiagnosticReadinessPacket.class));
        assertSame(dm26_1, dataRepository.getObdModule(1).getLatest(DM26TripDiagnosticReadinessPacket.class));
        assertSame(dm26_3, dataRepository.getObdModule(3).getLatest(DM26TripDiagnosticReadinessPacket.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForChangingCompletion() {
        // Module 0 responds and doesn't change complete state and has no completions
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM5DiagnosticReadinessPacket.create(0,
                                                                      0,
                                                                      0,
                                                                      0x22,
                                                                      List.of(CompositeSystem.values()),
                                                                      List.of()),
                                  1);
        obdModuleInformation0.set(DM26TripDiagnosticReadinessPacket.create(0,
                                                                           0,
                                                                           0,
                                                                           List.of(),
                                                                           List.of(CompositeSystem.values())),
                                  11);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm26_0 = DM26TripDiagnosticReadinessPacket.create(0, 0, 0, List.of(), List.of());
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of(dm26_0));

        runTest();

        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported A/C system refrigerant as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Boost pressure control sys as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Catalyst as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Cold start aid system as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Diesel Particulate Filter as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported EGR/VVT system as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Evaporative system as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Exhaust Gas Sensor as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Exhaust Gas Sensor heater as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Fuel System as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Heated catalyst as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Misfire as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported NMHC converting catalyst as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported NOx catalyst/adsorber as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.a - Engine #1 (0) reported Secondary air system as 'not complete this cycle' when it reported it as 'complete this cycle' in part 11");
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(diagnosticMessageModule.requestDM26(any(), eq(0))).thenReturn(RequestResult.of());

        runTest();

        verify(diagnosticMessageModule).requestDM26(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.2.2.b - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }
}
