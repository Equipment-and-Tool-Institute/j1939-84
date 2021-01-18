/*
  Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket.PGN;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private DTCModule dtcModule;

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

        instance = new Part01Step14Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticReadinessModule,
                dtcModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 diagnosticReadinessModule,
                                 dtcModule,
                                 mockListener);
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testEmptyPacketFailure() {
        List<Integer> obdModuleAddresses = List.of(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.14.2.f - No OBD ECU provided DM26");

        String expectedResults = "FAIL: 6.1.14.2.f - No OBD ECU provided DM26" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testFailures() {
        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x04, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        List<Integer> obdModuleAddresses = List.of(0x01, 0x03);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM26TripDiagnosticReadinessPacket obdPacket3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x03);
        obdModule1.setMonitoredSystems(packet1.getMonitoredSystems());
        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        obdModule3.setMonitoredSystems(obdPacket3.getMonitoredSystems());

        when(dataRepository.getObdModules()).thenReturn(Set.of(obdModule1, obdModule3));

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1, packet3), List.of()));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));
        when(dtcModule.requestDM26(any(), eq(0x03)))
                .thenReturn(new RequestResult<>(false, List.of(obdPacket3), List.of()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));
        verify(dtcModule).requestDM26(any(), eq(0x03));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    Cold start aid system      not enabled,     complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    EGR/VVT system             not enabled,     complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    Exhaust Gas Sensor         not enabled, not complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    Heated catalyst            not enabled, not complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    NMHC converting catalyst   not enabled,     complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    NOx catalyst/adsorber      not enabled, not complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    Secondary air system       not enabled,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.2.d - Response indicates number of warm-ups since code clear is not zero" + NL
                                                + "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                                                + NL
                                                + "DM26 from Transmission #1 (3): Warm-ups: 4, Time Since Engine Start: 0 seconds"
                                                + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.2.e - Response indicates time since engine start is not zero" + NL +
                                                "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                                                + NL);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.14.3.a - An individual required monitor is supported by more than one OBD ECU"
                                                + NL +
                                                "A/C system refrigerant     has reporting from more than one OBD ECU" + NL +
                                                "Boost pressure control sys has reporting from more than one OBD ECU" + NL +
                                                "Catalyst                   has reporting from more than one OBD ECU" + NL +
                                                "Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL +
                                                "Evaporative system         has reporting from more than one OBD ECU" + NL +
                                                "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.5.a - Difference compared to data received during global request");

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system          enabled, not complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "    EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst                enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "    NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "    Secondary air system           enabled, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.d - Response indicates number of warm-ups since code clear is not zero"
                + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 4, Time Since Engine Start: 0 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e - Response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "WARN: 6.1.14.3.a - An individual required monitor is supported by more than one OBD ECU"
                + NL;
        expectedResults += "A/C system refrigerant     has reporting from more than one OBD ECU" + NL;
        expectedResults += "Boost pressure control sys has reporting from more than one OBD ECU" + NL;
        expectedResults += "Catalyst                   has reporting from more than one OBD ECU" + NL;
        expectedResults += "Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL;
        expectedResults += "Evaporative system         has reporting from more than one OBD ECU" + NL;
        expectedResults += "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU" + NL;
        expectedResults += "FAIL: 6.1.14.5.a - Difference compared to data received during global request"
                + NL;

        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testMoreFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, (byte) 0x0A, 0x44, 0x55, 0x66, 0x77, 0x88));

        List<Integer> obdModuleAddresses = Arrays.asList(0x01, 0x03);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM26TripDiagnosticReadinessPacket obdPacket1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMonitoredSystems(obdPacket1.getMonitoredSystems());

        when(dataRepository.getObdModules()).thenReturn(Set.of(obdModule1));

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1, packet3), List.of(ackPacket)));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));
        when(dtcModule.requestDM26(any(), eq(0x03)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));
        verify(dtcModule).requestDM26(any(), eq(0x03));

        verify(mockListener, times(7)).addOutcome(PART_NUMBER,
                                                  STEP_NUMBER,
                                                  FAIL,
                                                  "6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                                                + NL + "    Comprehensive component    not enabled,     complete");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.14.2.b - Response from an ECU indicating support for CCM monitor in DM5 is report as not enabled by DM26 response");

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.2.d - Response indicates number of warm-ups since code clear is not zero" + NL +
                                                "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                                                + NL +
                                                "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                                                + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.2.e - Response indicates time since engine start is not zero" + NL +
                                                "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                                                + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.14.2.e - Response indicates time since engine start is not zero" + NL +
                                                "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds" + NL);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.14.3.a - An individual required monitor is supported by more than one OBD ECU"
                                                + NL + "A/C system refrigerant     has reporting from more than one OBD ECU"
                                                + NL + "Boost pressure control sys has reporting from more than one OBD ECU"
                                                + NL + "Catalyst                   has reporting from more than one OBD ECU"
                                                + NL + "Diesel Particulate Filter  has reporting from more than one OBD ECU"
                                                + NL + "Evaporative system         has reporting from more than one OBD ECU"
                                                + NL + "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU");

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response for a monitor in DM5 is reported as not supported and is reported as enabled by DM26 response"
                + NL;
        expectedResults += "    Comprehensive component    not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b - Response from an ECU indicating support for CCM monitor in DM5 is report as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a - Response for a monitor in DM5 is reported as supported and is reported as not enabled by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.d - Response indicates number of warm-ups since code clear is not zero"
                + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e - Response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e - Response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "WARN: 6.1.14.3.a - An individual required monitor is supported by more than one OBD ECU"
                + NL;
        expectedResults += "A/C system refrigerant     has reporting from more than one OBD ECU"
                + NL;
        expectedResults += "Boost pressure control sys has reporting from more than one OBD ECU"
                + NL;
        expectedResults += "Catalyst                   has reporting from more than one OBD ECU"
                + NL;
        expectedResults += "Diesel Particulate Filter  has reporting from more than one OBD ECU"
                + NL;
        expectedResults += "Evaporative system         has reporting from more than one OBD ECU"
                + NL;
        expectedResults += "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU"
                + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testRun() {

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x00, 0x00, 0x00, 0x44, 0x55, 0x66, 0x77, 0x88));

        List<Integer> obdModuleAddresses = List.of(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMonitoredSystems(packet1.getMonitoredSystems());
        when(dataRepository.getObdModules()).thenReturn(Set.of(obdModule1));
        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));

        String expectedResults = NL + "Vehicle Composite of DM26:" + NL;
        expectedResults += "    A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "    Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "    Catalyst                       enabled, not complete" + NL;
        expectedResults += "    Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "    Comprehensive component        enabled, not complete" + NL;
        expectedResults += "    Diesel Particulate Filter      enabled,     complete" + NL;
        expectedResults += "    EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "    Evaporative system             enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "    Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "    Fuel System                not enabled,     complete" + NL;
        expectedResults += "    Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "    Misfire                    not enabled,     complete" + NL;
        expectedResults += "    NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "    NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "    Secondary air system       not enabled,     complete" + NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
