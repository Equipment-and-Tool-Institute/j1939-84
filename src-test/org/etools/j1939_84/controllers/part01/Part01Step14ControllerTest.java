/*
  Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket.PGN;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
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

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
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

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testEmptyPacketFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0x01));

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(new RequestResult<>(false));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.14.2.f - No OBD ECU provided DM26");

        assertEquals("", listener.getResults());
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

        DM26TripDiagnosticReadinessPacket obdPacket3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMonitoredSystems(packet1.getMonitoredSystems());
        dataRepository.putObdModule(obdModule1);

        OBDModuleInformation obdModule3 = new OBDModuleInformation(0x03);
        obdModule3.setMonitoredSystems(obdPacket3.getMonitoredSystems());
        dataRepository.putObdModule(obdModule3);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(new RequestResult<>(false, packet1, packet3));
        when(diagnosticMessageModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM26(any(), eq(0x03))).thenReturn(new RequestResult<>(false, obdPacket3));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM26(any(), eq(0x03));

        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor NOx catalyst/adsorber in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor Secondary air system in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor NMHC converting catalyst in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor Cold start aid system in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor Exhaust Gas Sensor in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor EGR/VVT system in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.c - Transmission #1 (3) response indicates support for monitor Comprehensive component in DM5 but is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Transmission #1 (3) response for a monitor Heated catalyst in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.d - Engine #2 (1) response indicates number of warm-ups since code clear is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.d - Transmission #1 (3) response indicates number of warm-ups since code clear is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.e - Engine #2 (1) response indicates time since engine start is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor A/C system refrigerant is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Boost pressure control sys is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Catalyst is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Diesel Particulate Filter is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Evaporative system is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Exhaust Gas Sensor heater is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.5.a - Difference compared to data received during global request from Transmission #1 (3)"
        );

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
        expectedResults +=  NL;
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
        DM26TripDiagnosticReadinessPacket packet1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM26TripDiagnosticReadinessPacket packet3 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, (byte) 0x0A, 0x44, 0x55, 0x66, 0x77, 0x88));

        DM26TripDiagnosticReadinessPacket obdPacket1 = new DM26TripDiagnosticReadinessPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMonitoredSystems(obdPacket1.getMonitoredSystems());
        dataRepository.putObdModule(obdModule1);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(new RequestResult<>(false, packet1, packet3));
        when(diagnosticMessageModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(false));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0x01));

        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor Exhaust Gas Sensor in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor Heated catalyst in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor EGR/VVT system in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.b - Engine #2 (1) response for a monitor Comprehensive component in DM5 is reported as not supported and is reported as enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor Secondary air system in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor NOx catalyst/adsorber in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor NMHC converting catalyst in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.a - Engine #2 (1) response for a monitor Cold start aid system in DM5 is reported as supported and is reported as not enabled by DM26 response"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.d - Engine #2 (1) response indicates number of warm-ups since code clear is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.d - Transmission #1 (3) response indicates number of warm-ups since code clear is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.e - Engine #2 (1) response indicates time since engine start is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                FAIL,
                "6.1.14.2.e - Transmission #1 (3) response indicates time since engine start is not zero"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor A/C system refrigerant is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Boost pressure control sys is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Catalyst is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Diesel Particulate Filter is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Evaporative system is supported by more than one OBD ECU"
        );
        verify(mockListener).addOutcome(
                1,
                14,
                WARN,
                "6.1.14.3.a - Required monitor Exhaust Gas Sensor heater is supported by more than one OBD ECU"
        );

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
        expectedResults +=  NL;
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

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        obdModule1.setMonitoredSystems(packet1.getMonitoredSystems());
        dataRepository.putObdModule(obdModule1);

        when(diagnosticMessageModule.requestDM26(any())).thenReturn(new RequestResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM26(any(), eq(0x01))).thenReturn(new RequestResult<>(false, packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM26(any(), eq(0x01));

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
        expectedResults +=  NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
