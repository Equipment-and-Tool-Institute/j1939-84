/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
public class Part07Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 5;

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

        instance = new Part07Step05Controller(executor,
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
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm1_0 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF);
        packetList.add(dm1_0);

        // Non-OBD Module
        var dtc = DiagnosticTroubleCode.create(122, 12, 1, 12);
        var dm1_1 = DM1ActiveDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc);
        packetList.add(dm1_1);
        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDTC() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc = DiagnosticTroubleCode.create(122, 12, 1, 12);
        var dm1 = DM1ActiveDTCsPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        packetList.add(dm1);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(packetList));

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
                                        "6.7.5.2.a - Engine #1 (0) reported an active DTC");
    }

    @Test
    public void testFailureForMILNotOff() {
        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();

        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dm1 = DM1ActiveDTCsPacket.create(0, ON, OFF, OFF, OFF);
        packetList.add(dm1);

        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(packetList));

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
                                        "6.7.5.2.b - Engine #1 (0) did not report MIL off");
    }

    @Test
    public void testFailureForNoDM1() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        List<DM1ActiveDTCsPacket> packetList = new ArrayList<>();
        // Non-OBD Module
        var dtc = DiagnosticTroubleCode.create(122, 12, 1, 12);
        var dm1 = DM1ActiveDTCsPacket.create(1, ON, OFF, OFF, OFF, dtc);
        packetList.add(dm1);
        when(communicationsModule.read(eq(DM1ActiveDTCsPacket.class),
                                       eq(3),
                                       eq(SECONDS),
                                       any(CommunicationsListener.class))).thenReturn(new ArrayList<>(packetList));

        runTest();

        verify(communicationsModule).read(eq(DM1ActiveDTCsPacket.class),
                                          eq(3),
                                          eq(SECONDS),
                                          any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.7.5.2.c - No OBD ECU provided a DM1");
    }

}
