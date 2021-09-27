/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static net.solidDesign.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.YES;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.WARNING;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.modules.CommunicationsModule;
import net.solidDesign.j1939.packets.DM20MonitorPerformanceRatioPacket;
import net.solidDesign.j1939.packets.DM28PermanentEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import net.solidDesign.j1939.packets.GenericPacket;
import net.solidDesign.j1939.packets.PerformanceRatio;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step07ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 7;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private ScheduledExecutorService executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private TableA1Validator validator;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private TestDateTimeModule dateTimeModule;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        dateTimeModule = new TestDateTimeModule();
        instance = new Part11Step07Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              validator);

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
                                 mockListener,
                                 validator);
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
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "Return value ignored on purpose")
    public void testHappyPathNoFailures() {
        // Responds to DM20s and DM28s
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(12, 1, 10, 0);
        obdModuleInformation0.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 0, ratio1), 11);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 11);
        dataRepository.putObdModule(obdModuleInformation0);

        var ratio2 = new PerformanceRatio(12, 1, 10, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 1, 0, ratio2);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        var dtc2 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.setPart11StartTime(dateTimeModule.getTimeAsLong());

        // Doesn't respond
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(engineSpeedModule.isEngineAtIdle()).thenReturn(false, true, false);
        var packet1 = mock(GenericPacket.class);
        var packet2 = mock(GenericPacket.class);
        var packet3 = mock(GenericPacket.class);
        when(j1939.readGenericPacket(any())).thenReturn(Stream.of(packet1, packet2, packet3));

        doAnswer((Answer<Void>) invocation -> {
            ((QuestionListener) invocation.getArguments()[3]).answered(YES);
            return null;
        }).when(mockListener).onUrgentMessage(any(), any(), eq(WARNING), any());

        when(engineSpeedModule.currentEngineSpeed()).thenReturn(1400.0);
        when(engineSpeedModule.averagedEngineSpeed()).thenReturn(1000.0);
        when(engineSpeedModule.idleEngineSpeed()).thenReturn(850.0);
        when(engineSpeedModule.pedalPosition()).thenReturn(29.0);
        when(engineSpeedModule.secondsAtSpeed()).thenReturn(298L).thenReturn(299L).thenReturn(300L);
        when(engineSpeedModule.secondsAtIdle()).thenReturn(400L);

        runTest();

        verify(executor).shutdownNow();

        var submitCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).submit(submitCaptor.capture());
        submitCaptor.getValue().run();

        verify(validator).reportImplausibleSPNValues(eq(packet2), any(), eq(true), eq("6.11.7.3.a"));

        verify(engineSpeedModule).startMonitoringEngineSpeed(eq(executor), any());

        var fixedRateCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).scheduleAtFixedRate(fixedRateCaptor.capture(), eq(0L), eq(1L), eq(TimeUnit.MINUTES));
        fixedRateCaptor.getValue().run();

        verify(engineSpeedModule, atLeastOnce()).currentEngineSpeed();
        verify(engineSpeedModule, atLeastOnce()).averagedEngineSpeed();
        verify(engineSpeedModule, atLeastOnce()).idleEngineSpeed();
        verify(engineSpeedModule, atLeastOnce()).pedalPosition();
        verify(engineSpeedModule, atLeastOnce()).secondsAtSpeed();
        verify(engineSpeedModule, atLeastOnce()).secondsAtIdle();
        verify(engineSpeedModule, atLeastOnce()).isEngineAtIdle();

        verify(communicationsModule, atLeastOnce()).requestDM20(any(), eq(0));
        verify(communicationsModule, atLeastOnce()).requestDM28(any(), eq(0));

        verify(mockListener, times(2)).onUrgentMessage(any(), any(), eq(WARNING), any());

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 180; i > 0; i--) {
            expectedMessages.append("Step 6.11.7.1.b - Waiting ").append(i).append(" seconds").append(NL);
        }
        expectedMessages.append("Step 6.11.7.1.c - Increase engine speed over 1150 rpm for 2 seconds").append(NL);
        expectedMessages.append("Step 6.11.7.1.c - Increase engine speed over 1150 rpm for 1 seconds").append(NL);
        for (int i = 438; i > 0; i--) {
            expectedMessages.append("Step 6.11.7.4.a - Continue to run engine at idle for an additional ")
                            .append(i)
                            .append(" seconds");
            if (i != 1) {
                expectedMessages.append(NL);
            }
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        String expectedResults = "";
        expectedResults += "10:15:30.0000 Test Update:" + NL;
        expectedResults += "          Engine Speed: 1400.0 RPM" + NL;
        expectedResults += "      WMA Engine Speed: 1000.0 RPM" + NL;
        expectedResults += "     Idle Engine Speed: 850.0 RPM" + NL;
        expectedResults += "        Pedal Position: 29.0 %" + NL;
        expectedResults += "  Run Time >= 1150 RPM: 300 seconds" + NL;
        expectedResults += "      Run Time at Idle: 400 seconds" + NL;
        expectedResults += "        Total Run Time: 620 seconds" + NL;
        expectedResults += "" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testFailureForDM20Change() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var ratio1 = new PerformanceRatio(12, 1, 10, 0);
        obdModuleInformation0.set(DM20MonitorPerformanceRatioPacket.create(0, 1, 0, ratio1), 11);
        dataRepository.putObdModule(obdModuleInformation0);

        var ratio2 = new PerformanceRatio(12, 1, 11, 0);
        var dm20 = DM20MonitorPerformanceRatioPacket.create(0, 1, 0, ratio2);
        when(communicationsModule.requestDM20(any(), eq(0))).thenReturn(BusResult.of(dm20));

        dataRepository.setPart11StartTime(dateTimeModule.getTimeAsLong());

        doAnswer((Answer<Void>) invocation -> {
            ((QuestionListener) invocation.getArguments()[3]).answered(YES);
            return null;
        }).when(mockListener).onUrgentMessage(any(), any(), eq(WARNING), any());

        when(engineSpeedModule.secondsAtSpeed()).thenReturn(298L).thenReturn(299L).thenReturn(300L);

        runTest();

        verify(executor).submit((Runnable) any());
        verify(executor).scheduleAtFixedRate(any(), eq(0L), eq(1L), eq(TimeUnit.MINUTES));
        verify(executor).shutdownNow();

        verify(engineSpeedModule).startMonitoringEngineSpeed(eq(executor), any());

        verify(engineSpeedModule, atLeastOnce()).secondsAtSpeed();

        verify(communicationsModule, times(2)).requestDM20(any(), eq(0));

        verify(mockListener, times(2)).onUrgentMessage(any(), any(), eq(WARNING), any());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.7.2.a - Engine #1 (0) DM20 response indicates a denominator is greater than the value it was earlier in this part");
    }

    @Test
    public void testFailureForDM28Change() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 11);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm28 = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(any(), eq(0))).thenReturn(BusResult.of(dm28));

        dataRepository.setPart11StartTime(dateTimeModule.getTimeAsLong());

        doAnswer((Answer<Void>) invocation -> {
            ((QuestionListener) invocation.getArguments()[3]).answered(YES);
            return null;
        }).when(mockListener).onUrgentMessage(any(), any(), eq(WARNING), any());

        when(engineSpeedModule.secondsAtSpeed()).thenReturn(298L).thenReturn(299L).thenReturn(300L);

        runTest();

        verify(executor).submit((Runnable) any());
        verify(executor).scheduleAtFixedRate(any(), eq(0L), eq(1L), eq(TimeUnit.MINUTES));
        verify(executor).shutdownNow();

        verify(engineSpeedModule).startMonitoringEngineSpeed(eq(executor), any());

        verify(engineSpeedModule, atLeastOnce()).secondsAtSpeed();

        verify(communicationsModule, times(2)).requestDM28(any(), eq(0));

        verify(mockListener, times(2)).onUrgentMessage(any(), any(), eq(WARNING), any());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.11.7.2.b - Engine #1 (0) DM28 response indicates the permanent DTC is no longer present");
    }

}
