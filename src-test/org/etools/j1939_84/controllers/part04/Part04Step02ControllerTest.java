/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.QuestionListener.AnswerType.NO;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.QUESTION;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.QuestionListener;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
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
public class Part04Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 2;

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

    private DateTimeModule dateTimeModule;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Part04Step02Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var nack = AcknowledgmentPacket.create(0, AcknowledgmentPacket.Response.NACK);
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, nack));

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any(), eq(1))).thenReturn(new BusResult<>(false, dm12_1));

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_1));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(1));
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoResponses() {

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(true));

        String promptMsg = "No ECU has reported a confirmed and active DTC." + NL + "Do you wish to continue?";
        String promptTitle = "No Confirmed and Active DTCs Found";

        doAnswer(invocationOnMock -> {
            QuestionListener questionListener = invocationOnMock.getArgument(3);
            questionListener.answered(NO);
            return null;
        }).when(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());

        runTest();

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedMessages.append("Step 6.4.2.1.a - Requesting DM12 Attempt ").append(i);
            if (i != 300) {
                expectedMessages.append(NL);
            }
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedResults.append(NL).append("Attempt ").append(i).append(NL);
        }
        assertEquals(expectedResults.toString(), listener.getResults());

        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(communicationsModule, times(300)).requestDM12(any());
        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.1.a.ii - User said 'no' and no ECU reported a confirmed and active DTC");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.2.a - No ECU reported MIL on");

        assertEquals(299000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testNoDTCs() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));

        String promptMsg = "No ECU has reported a confirmed and active DTC." + NL + "Do you wish to continue?";
        String promptTitle = "No Confirmed and Active DTCs Found";

        doAnswer(invocationOnMock -> {
            QuestionListener questionListener = invocationOnMock.getArgument(3);
            questionListener.answered(NO);
            return null;
        }).when(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());

        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(BusResult.of(dm12));

        runTest();

        StringBuilder expectedMessages = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedMessages.append("Step 6.4.2.1.a - Requesting DM12 Attempt ").append(i);
            if (i != 300) {
                expectedMessages.append(NL);
            }
        }
        assertEquals(expectedMessages.toString(), listener.getMessages());

        StringBuilder expectedResults = new StringBuilder();
        for (int i = 1; i <= 300; i++) {
            expectedResults.append(NL).append("Attempt ").append(i).append(NL);
        }
        assertEquals(expectedResults.toString(), listener.getResults());

        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(communicationsModule, times(300)).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));
        verify(mockListener).onUrgentMessage(eq(promptMsg), eq(promptTitle), eq(QUESTION), any());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.1.a.ii - User said 'no' and no ECU reported a confirmed and active DTC");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.2.a - No ECU reported MIL on");

        assertEquals(299000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testMultipleDTCFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 3, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 3);
        dataRepository.putObdModule(obdModuleInformation);

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.2.3.a - Engine #1 (0) reported > 1 confirmed and active DTC");
    }

    @Test
    public void testDTCsMultipleModulesFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModuleInformation);

        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_0, dm12_1));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_0));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.2.3.b - More than one ECU reported a confirmed and active DTC");
    }

    @Test
    public void testMultipleModulesWithOneDTC() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_0));

        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any(), eq(1))).thenReturn(new BusResult<>(false, dm12_1));

        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_0, dm12_1));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(1));
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testMILNotOnFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModuleInformation);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.2.a - No ECU reported MIL on");
    }

    @Test
    public void testDifferentDTCsFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModuleInformation);

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.2.b - Engine #1 (0) reported DM12 DTC(s) different than DM6 pending DTC(s) in part 3");
    }

    @Test
    public void testGlobalDSDifferenceFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModuleInformation);

        var dm12_1 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        var dm12_2 = DM12MILOnEmissionDTCPacket.create(0, ON, ON, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12_1));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12_2));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.5.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testNoNACKFailure() {
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 3);
        dataRepository.putObdModule(obdModuleInformation);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false, dm12));
        when(communicationsModule.requestDM12(any(), eq(0))).thenReturn(new BusResult<>(false, dm12));

        when(communicationsModule.requestDM12(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        String expectedMessages = "Step 6.4.2.1.a - Requesting DM12 Attempt 1";
        assertEquals(expectedMessages, listener.getMessages());

        String expectedResults = "" + NL;
        expectedResults += "Attempt 1" + NL;
        assertEquals(expectedResults, listener.getResults());

        verify(communicationsModule).requestDM12(any());
        verify(communicationsModule).requestDM12(any(), eq(0));
        verify(communicationsModule).requestDM12(any(), eq(1));

        assertEquals(0, dateTimeModule.getTimeAsLong());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.2.5.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

}
