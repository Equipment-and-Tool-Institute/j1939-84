/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.model.Outcome.ABORT;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
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
public class Part03Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dateTimeModule = new TestDateTimeModule();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step02Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
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
    public void testNoResponses() throws InterruptedException {
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(RequestResult.empty());

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.3.2.2.a - No OBD ECU supports DM6");
    }

    @Test
    public void testNoDTCs() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));

        String promptMsg = "No module has reported a Pending Emission DTC." + NL + NL + "Do you wish to continue?";
        String promptTitle = "No Pending Emission DTCs Found";

        doAnswer(invocationOnMock -> {
            QuestionListener questionListener = invocationOnMock.getArgument(3);
            questionListener.answered(NO);
            return null;
        }).when(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());

        runTest();

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedMessages.append("Step 6.3.2.1.a - Requesting DM6 Attempt ").append(i).append(NL);
        }
        expectedMessages.append("User cancelled testing at Part 3 Step 2");
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedResults.append(NL).append("Attempt ").append(i).append(NL);
        }
        assertEquals(expectedResults.toString(), listener.getResults());

        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(diagnosticMessageModule, times(300)).requestDM6(any());
        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(mockListener, times(2)).addOutcome(PART_NUMBER,
                                                  STEP_NUMBER,
                                                  ABORT,
                                                  "User cancelled testing at Part 3 Step 2");

        assertEquals(299000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testMultipleDTCFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 3, 0, 1);
        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1, dtc2);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6));

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.2.3.a - Engine #1 (0) reported > 1 pending DTC");
    }

    @Test
    public void testDTCsMultipleModulesFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm6_0 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        var dm6_1 = DM6PendingEmissionDTCPacket.create(1, OFF, OFF, OFF, OFF, dtc1);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6_0, dm6_1));
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6_0));

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.3.2.3.b - More than one ECU reported a pending DTC");
    }

    @Test
    public void testMILNotOffFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm6 = DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6));

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.2.5.b - Engine #1 (0) did not report MIL 'off'");
    }

    @Test
    public void testGlobalDSDifferenceFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm6_1 = DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        var dm6_2 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6_1));
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6_2));

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.2.5.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testNoNACKFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm6 = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6));
        when(diagnosticMessageModule.requestDM6(any(), eq(1))).thenReturn(new RequestResult<>(false));

        runTest();

        String expectedMessages = "Step 6.3.2.1.a - Requesting DM6 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));
        verify(diagnosticMessageModule).requestDM6(any(), eq(1));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.2.5.c - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }
}
