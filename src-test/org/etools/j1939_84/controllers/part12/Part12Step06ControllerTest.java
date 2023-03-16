/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part12Step06ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 6;

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

    private StepController instance;

    DataRepository dataRepository = DataRepository.newInstance();

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);

        instance = new Part12Step06Controller(executor,
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
        var dm1_0 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, NOT_SUPPORTED, OFF, OFF, OFF);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(List.of(dm1_0, dm1_1)));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testWarningForAlternativeOff() {
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ALTERNATE_OFF, OFF, OFF, OFF);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(List.of(dm1_0)));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");
    }

    @Test
    public void testFailureForMILNotOff() {
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(List.of(dm1_0)));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.6.2.a - Engine #1 (0) did not report MIL 'off' or not supported");
    }

    @Test
    public void testFailureForDTC() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var dm1_0 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc);

        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(List.of(dm1_0)));
        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.6.2.b - Engine #1 (0) reported active DTC(s)");
    }

}
