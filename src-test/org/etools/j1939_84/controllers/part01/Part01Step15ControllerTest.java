/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.SLOW_FLASH;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
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

/**
 * The unit test for {@link Part01Step15Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int STEP_NUMBER = 15;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step15Controller instance;

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
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part01Step15Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              dataRepository,
                                              new TestDateTimeModule());

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
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener);
    }

    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.15.2 - No OBD ECU provided a DM1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testActiveDtcFailure() {
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(4334, 4, 1, 0);

        DM1ActiveDTCsPacket packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                                 ALTERNATE_OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 dtc1,
                                                                 dtc2,
                                                                 dtc3);
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Engine #2 (1) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailure() {
        var dtc1 = DiagnosticTroubleCode.create(1569, 31, 0, 0);
        var dtc2 = DiagnosticTroubleCode.create(609, 19, 0, 0);
        var dtc3 = DiagnosticTroubleCode.create(4334, 4, 1, 0);
        DM1ActiveDTCsPacket packet2 = DM1ActiveDTCsPacket.create(0x17,
                                                                 ON,
                                                                 ALTERNATE_OFF,
                                                                 SLOW_FLASH,
                                                                 FAST_FLASH,
                                                                 dtc1,
                                                                 dtc2,
                                                                 dtc3);

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet2));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.c - Non-OBD ECU Instrument Cluster #1 (23) did not report MIL off or not supported");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.15.3.b - Non-OBD ECU Instrument Cluster #1 (23) reported SPN conversion method (SPN 1706) equal to 1");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2 - No OBD ECU provided a DM1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testSpnConversionFailures() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 5);
        var packet3 = DM1ActiveDTCsPacket.create(0x03,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 dtc);

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet3));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Transmission #1 (3) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Transmission #1 (3) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testRun() {
        DM1ActiveDTCsPacket packet2 = DM1ActiveDTCsPacket.create(0x17,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF,
                                                                 OFF);

        dataRepository.putObdModule(new OBDModuleInformation(0x17));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet2));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailures() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 1, 5);
        var packet1 = DM1ActiveDTCsPacket.create(0x01,
                                                 ON,
                                                 OFF,
                                                 OFF,
                                                 OFF,
                                                 dtc);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.readDM1(any())).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.a - OBD ECU Engine #2 (1) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.b - OBD ECU Engine #2 (1) did not report MIL 'off'");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.15.2.d - OBD ECU Engine #2 (1) reported SPN conversion method (SPN 1706) equal to binary 1");

        assertEquals("", listener.getResults());
    }
}
