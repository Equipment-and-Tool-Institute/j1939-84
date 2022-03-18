/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ALTERNATE_OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.NOT_SUPPORTED;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.CommunicationsListener;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM1ActiveDTCsPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part03Step06ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step06Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
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
    public void testNoFailures() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        var dm1_0 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        packetList.add(dm1_0);
        var dm1_21 = DM1ActiveDTCsPacket.create(0x21, OFF, OFF, OFF, OFF);
        packetList.add(dm1_21);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(9),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(9),
                                          eq(SECONDS),
                                          any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFailuresAlternateValues() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        var dm1_0 = DM1ActiveDTCsPacket.create(0, ALTERNATE_OFF, OFF, OFF, OFF);
        packetList.add(dm1_0);
        var dm1_21 = DM1ActiveDTCsPacket.create(0x21, NOT_SUPPORTED, OFF, OFF, OFF);
        packetList.add(dm1_21);
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(9),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(9),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "A.8 - Alternate coding for off (0b00, 0b00) has been accepted");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoMessages() {
        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(9),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(List.of());

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(9),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.3.6.2.a - No OBD ECU supports DM1");
    }

    @Test
    public void testFailureForNoOBDSupport() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        var dm1 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        packetList.add(dm1);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(9),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(9),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.3.6.2.a - No OBD ECU supports DM1");
    }

    @Test
    public void testFailures() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm1_0 = DM1ActiveDTCsPacket.create(0x00, ON, OFF, OFF, OFF, dtc);
        packetList.add(dm1_0);
        var dm1_21 = DM1ActiveDTCsPacket.create(0x21, ON, OFF, OFF, OFF);
        packetList.add(dm1_21);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(9),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class)))
                                                                          .thenReturn(new ArrayList(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(9),
                                          eq(SECONDS),
                                          any(CommunicationsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.6.2.b - Engine #1 (0) reported an active DTC");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.6.2.c - Engine #1 (0) did not report MIL 'off'");

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.6.2.d - Non-OBD ECU Body Controller (33) did not report MIL off or not supported");
    }
}
