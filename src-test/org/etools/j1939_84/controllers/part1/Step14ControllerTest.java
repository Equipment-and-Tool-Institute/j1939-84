/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
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

/**
 * The unit test for {@link Step14Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM26TripDiagnosticReadinessPacket.PGN;
    private static final int STEP_NUMBER = 14;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step14Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDModuleInformation obdModuleInformation;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SectionA6Validator sectionA6Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step14Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                diagnosticReadinessModule,
                dtcModule, dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository,
                diagnosticReadinessModule,
                dtcModule,
                mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step14Controller#Step14Controller(org.etools.j1939_84.controllers.part1.DataRepository)}.
     */
    @Test
    public void testEmptyPacketFailure() {
        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), Collections.emptyList()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.14.2.f. Fail if no OBD ECU provides DM26");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.14.2.f. Fail if no OBD ECU provides DM26" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step14Controller#Step14Controller(org.etools.j1939_84.controllers.part1.DataRepository)}.
     */
    @Test
    public void testFailures() {
        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x04, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        List<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x01);
                add(0x03);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM26TripDiagnosticReadinessPacket obdPacket3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));
        System.out.println("packet3 systems are: ");
        packet3.getMonitoredSystems().stream().sorted().forEach(system -> {
            System.out.println(system.toString());
        });
        System.out.println("obdPacket3 systems are: ");
        obdPacket3.getMonitoredSystems().stream().sorted().forEach(system -> {
            System.out.println(system.toString());
        });
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x03);
        obdModule1.setMontioredSystems(packet1.getMonitoredSystems());
        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        obdModule3.setMontioredSystems(obdPacket3.getMonitoredSystems());

        when(dataRepository.getObdModules()).thenReturn(new HashSet<OBDModuleInformation>() {
            {
                add(obdModule1);
                add(obdModule3);
            }
        });

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, new ArrayList<>() {
                    {
                        add(packet1);
                        add(packet3);
                    }
                }, Collections.emptyList()));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));
        when(dtcModule.requestDM26(any(), eq(0x03)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(obdPacket3), Collections.emptyList()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));
        verify(dtcModule).requestDM26(any(), eq(0x03));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "Cold start aid system      not supported,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "EGR/VVT system             not supported,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "Exhaust Gas Sensor         not supported, not complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "Heated catalyst            not supported, not complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "NMHC converting catalyst   not supported,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "NOx catalyst/adsorber      not supported, not complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "Secondary air system       not supported,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.d Fail if any response indicates number of warm-ups since code clear is not zero" + NL
                        + "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                        + NL
                        + "DM26 from Transmission #1 (3): Warm-ups: 4, Time Since Engine Start: 0 seconds"
                        + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL +
                        "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                        + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.14.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU"
                        + NL +
                        "A/C system refrigerant     has reporting from more than one OBD ECU" + NL +
                        "Boost pressure control sys has reporting from more than one OBD ECU" + NL +
                        "Catalyst                   has reporting from more than one OBD ECU" + NL +
                        "Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL +
                        "Evaporative system         has reporting from more than one OBD ECU" + NL +
                        "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.5.a Fail if any difference compared to data received during global request");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "Boost pressure control sys     enabled, not complete" + NL;
        expectedResults += "Catalyst                       enabled, not complete" + NL;
        expectedResults += "Cold start aid system          enabled, not complete" + NL;
        expectedResults += "Comprehensive component        enabled, not complete" + NL;
        expectedResults += "Diesel Particulate Filter      enabled, not complete" + NL;
        expectedResults += "EGR/VVT system                 enabled, not complete" + NL;
        expectedResults += "Evaporative system             enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor             enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "Fuel System                not enabled,     complete" + NL;
        expectedResults += "Heated catalyst                enabled, not complete" + NL;
        expectedResults += "Misfire                    not enabled,     complete" + NL;
        expectedResults += "NMHC converting catalyst       enabled, not complete" + NL;
        expectedResults += "NOx catalyst/adsorber          enabled, not complete" + NL;
        expectedResults += "Secondary air system           enabled, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "Cold start aid system      not supported,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "EGR/VVT system             not supported,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "Exhaust Gas Sensor         not supported, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "Heated catalyst            not supported, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "NMHC converting catalyst   not supported,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "NOx catalyst/adsorber      not supported, not complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "Secondary air system       not supported,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.d Fail if any response indicates number of warm-ups since code clear is not zero"
                + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 4, Time Since Engine Start: 0 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "WARN: 6.1.14.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU"
                + NL;
        expectedResults += "A/C system refrigerant     has reporting from more than one OBD ECU" + NL;
        expectedResults += "Boost pressure control sys has reporting from more than one OBD ECU" + NL;
        expectedResults += "Catalyst                   has reporting from more than one OBD ECU" + NL;
        expectedResults += "Diesel Particulate Filter  has reporting from more than one OBD ECU" + NL;
        expectedResults += "Evaporative system         has reporting from more than one OBD ECU" + NL;
        expectedResults += "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU" + NL;
        expectedResults += "FAIL: 6.1.14.5.a Fail if any difference compared to data received during global request"
                + NL;

        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step14Controller#Step14Controller(org.etools.j1939_84.controllers.part1.DataRepository)}.
     */
    @Test
    public void testMoreFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, (byte) 0x0A, 0x44, 0x55, 0x66, 0x77,
                        0x88));

        List<Integer> obdModuleAddresses = Arrays.asList(0x01, 0x03);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM26TripDiagnosticReadinessPacket obdPacket1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMontioredSystems(obdPacket1.getMonitoredSystems());

        when(dataRepository.getObdModules()).thenReturn(new HashSet<OBDModuleInformation>() {
            {
                add(obdModule1);
            }
        });

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, new ArrayList<>() {
                    {
                        add(packet1);
                        add(packet3);
                    }
                }, Collections.singletonList(ackPacket)));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false,
                        Collections.emptyList(), Collections.emptyList()));
        when(dtcModule.requestDM26(any(), eq(0x03)))
                .thenReturn(new RequestResult<>(false,
                        Collections.emptyList(), Collections.emptyList()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));
        verify(dtcModule).requestDM26(any(), eq(0x03));

        verify(mockListener, times(7)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                        + NL + "Comprehensive component    not supported,     complete");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.b Fail if any response from an ECU indicating support for CCM monitor in DM5 is report as not supported by DM26 response");

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.d Fail if any response indicates number of warm-ups since code clear is not zero" + NL +
                        "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                        + NL +
                        "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                        + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL +
                        "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                        + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.14.4.a Destination Specific DM5 requests to OBD modules did not return any responses");
        // verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL +
                        "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds" + NL);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.14.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU"
                        + NL + "A/C system refrigerant     has reporting from more than one OBD ECU"
                        + NL + "Boost pressure control sys has reporting from more than one OBD ECU"
                        + NL + "Catalyst                   has reporting from more than one OBD ECU"
                        + NL + "Diesel Particulate Filter  has reporting from more than one OBD ECU"
                        + NL + "Evaporative system         has reporting from more than one OBD ECU"
                        + NL + "Exhaust Gas Sensor heater  has reporting from more than one OBD ECU");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.14.4.a Destination Specific DM5 requests to OBD modules did not return any responses");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.5.a Fail if any difference compared to data received during global request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.14.5.b Fail if NACK not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "Catalyst                       enabled, not complete" + NL;
        expectedResults += "Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "Comprehensive component        enabled, not complete" + NL;
        expectedResults += "Diesel Particulate Filter      enabled,     complete" + NL;
        expectedResults += "EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "Evaporative system             enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "Fuel System                not enabled,     complete" + NL;
        expectedResults += "Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "Misfire                    not enabled,     complete" + NL;
        expectedResults += "NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "Secondary air system       not enabled,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response for a monitor in DM5 is reported as not supported and is reported as supported by DM26 response"
                + NL;
        expectedResults += "Comprehensive component    not supported,     complete" + NL;
        expectedResults += "FAIL: 6.1.14.2.b Fail if any response from an ECU indicating support for CCM monitor in DM5 is report as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.a Fail if any response for a monitor in DM5 is reported as supported and is reported as not supported by DM26 response"
                + NL;
        expectedResults += "FAIL: 6.1.14.2.d Fail if any response indicates number of warm-ups since code clear is not zero"
                + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Engine #2 (1): Warm-ups: 51, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "FAIL: 6.1.14.2.e Fail if any response indicates time since engine start is not zero" + NL;
        expectedResults += "DM26 from Transmission #1 (3): Warm-ups: 10, Time Since Engine Start: 8,721 seconds"
                + NL + NL;
        expectedResults += "WARN: 6.1.14.3.a Warn if any individual required monitor, except Continuous Component Monitoring (CCM) is supported by more than one OBD ECU"
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
        expectedResults += "WARN: 6.1.14.4.a Destination Specific DM5 requests to OBD modules did not return any responses"
                + NL;
        expectedResults += "FAIL: 6.1.14.5.a Fail if any difference compared to data received during global request"
                + NL;
        expectedResults += "FAIL: 6.1.14.5.b Fail if NACK not received from OBD ECUs that did not respond to global query"
                + NL;

        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step14Controller#run()}.
     */
    @Test
    public void testRun() {

        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x00, 0x00, 0x00, 0x44, 0x55, 0x66, 0x77, 0x88));

        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMontioredSystems(packet1.getMonitoredSystems());
        System.out.println("obdModule1 size is " + obdModule1.getMonitoredSystems().size());
        when(dataRepository.getObdModules()).thenReturn(new HashSet<OBDModuleInformation>() {
            {
                add(obdModule1);
            }
        });

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));
        when(dtcModule.requestDM26(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        runTest();
        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();
        verify(dataRepository, atLeastOnce()).getObdModules();

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM26(any(), eq(0x01));

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "" + NL;
        expectedResults += "Vehicle Composite of DM26:" + NL;
        expectedResults += "A/C system refrigerant         enabled, not complete" + NL;
        expectedResults += "Boost pressure control sys     enabled,     complete" + NL;
        expectedResults += "Catalyst                       enabled, not complete" + NL;
        expectedResults += "Cold start aid system      not enabled,     complete" + NL;
        expectedResults += "Comprehensive component        enabled, not complete" + NL;
        expectedResults += "Diesel Particulate Filter      enabled,     complete" + NL;
        expectedResults += "EGR/VVT system             not enabled,     complete" + NL;
        expectedResults += "Evaporative system             enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor         not enabled, not complete" + NL;
        expectedResults += "Exhaust Gas Sensor heater      enabled, not complete" + NL;
        expectedResults += "Fuel System                not enabled,     complete" + NL;
        expectedResults += "Heated catalyst            not enabled, not complete" + NL;
        expectedResults += "Misfire                    not enabled,     complete" + NL;
        expectedResults += "NMHC converting catalyst   not enabled,     complete" + NL;
        expectedResults += "NOx catalyst/adsorber      not enabled, not complete" + NL;
        expectedResults += "Secondary air system       not enabled,     complete" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

}