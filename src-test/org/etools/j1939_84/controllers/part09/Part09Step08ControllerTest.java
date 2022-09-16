/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.SectionA5Verifier;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

;

@RunWith(MockitoJUnitRunner.class)
public class Part09Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 8;

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

    @Mock
    private SectionA5Verifier verifier;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private TestDateTimeModule dateTimeModule;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Part09Step08Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              verifier);

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
                                 verifier);
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
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(123, 1, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 9);
        dataRepository.putObdModule(obdModuleInformation0);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of());
        when(communicationsModule.requestDM11(any(), eq(1))).thenReturn(List.of());
        when(communicationsModule.requestDM11(any())).thenReturn(List.of());

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any(), eq(1));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.2.a"), eq("6.9.8.2.b"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.4.b"), eq("6.9.8.4.c"), eq(false));
        verify(verifier).verifyDataErased(any(), eq("6.9.8.6.c"));

        String expected = getExpectedMessages();
        assertEquals(expected, listener.getMessages());

        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());

        assertEquals(15000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testFailureForNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of());

        var nack_0 = AcknowledgmentPacket.create(0, NACK);
        var nack_1 = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM11(any())).thenReturn(List.of(nack_0, nack_1));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.2.a"), eq("6.9.8.2.b"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.4.b"), eq("6.9.8.4.c"), eq(false));
        verify(verifier).verifyDataErased(any(), eq("6.9.8.6.c"));

        assertEquals(getExpectedMessages(), listener.getMessages());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.8.6.a - Engine #1 (0) provided a NACK to the global DM11 request");

        assertEquals(15000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testWarningForACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of());

        var ack_0 = AcknowledgmentPacket.create(0, ACK);
        var ack_1 = AcknowledgmentPacket.create(1, ACK);
        when(communicationsModule.requestDM11(any())).thenReturn(List.of(ack_0, ack_1));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.2.a"), eq("6.9.8.2.b"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.9.8.4.b"), eq("6.9.8.4.c"), eq(false));
        verify(verifier).verifyDataErased(any(), eq("6.9.8.6.c"));

        assertEquals(getExpectedMessages(), listener.getMessages());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.9.8.6.b - Engine #1 (0) provided an ACK to the global DM11 request");

        assertEquals(15000, dateTimeModule.getTimeAsLong());
    }

    private static String getExpectedMessages() {
        String expected = "";
        expected += "Step 6.9.8.1.b - Waiting 5 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.1.b - Waiting 4 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.1.b - Waiting 3 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.1.b - Waiting 2 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.1.b - Waiting 1 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.3.b - Waiting 5 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.3.b - Waiting 4 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.3.b - Waiting 3 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.3.b - Waiting 2 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.3.b - Waiting 1 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.5.b - Waiting 5 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.5.b - Waiting 4 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.5.b - Waiting 3 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.5.b - Waiting 2 seconds before checking for erased data" + NL;
        expected += "Step 6.9.8.5.b - Waiting 1 seconds before checking for erased data";
        return expected;
    }
}
