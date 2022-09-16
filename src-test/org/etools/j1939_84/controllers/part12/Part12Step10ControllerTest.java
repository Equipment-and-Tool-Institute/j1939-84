/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
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
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part12Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 10;

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

        instance = new Part12Step10Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var nack0 = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of(nack0));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var nack1 = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM11(any(), eq(1))).thenReturn(List.of(nack1));

        var ack = AcknowledgmentPacket.create(2, ACK);
        when(communicationsModule.requestDM11(any())).thenReturn(List.of(ack));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any(), eq(1));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.2.b"), eq("6.12.10.2.c"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.4.c"), eq("6.12.10.4.c"), eq(true));

        String expected = getExpectedMessages();
        assertEquals(expected, listener.getMessages());

        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());

        assertEquals(10000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of());

        when(communicationsModule.requestDM11(any())).thenReturn(List.of());

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.2.b"), eq("6.12.10.2.c"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.4.c"), eq("6.12.10.4.c"), eq(true));

        String expected = getExpectedMessages();
        assertEquals(expected, listener.getMessages());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.10.2.a - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");

        assertEquals(10000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testFailureForNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var nack0 = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of(nack0));

        when(communicationsModule.requestDM11(any())).thenReturn(List.of(nack0));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.2.b"), eq("6.12.10.2.c"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.4.c"), eq("6.12.10.4.c"), eq(true));

        String expected = getExpectedMessages();
        assertEquals(expected, listener.getMessages());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.12.10.4.a - Engine #1 (0) responded with a NACK");

        assertEquals(10000, dateTimeModule.getTimeAsLong());
    }

    @Test
    public void testWarningForACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        var nack0 = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestDM11(any(), eq(0))).thenReturn(List.of(nack0));

        var ack0 = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestDM11(any())).thenReturn(List.of(ack0));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM11(any(), eq(0));
        verify(communicationsModule).requestDM11(any());

        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.2.b"), eq("6.12.10.2.c"), eq(false));
        verify(verifier).verifyDataNotPartialErased(any(), eq("6.12.10.4.c"), eq("6.12.10.4.c"), eq(true));

        String expected = getExpectedMessages();
        assertEquals(expected, listener.getMessages());

        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        INFO,
                                        "6.12.10.4.b - Engine #1 (0) responded with a ACK");

        assertEquals(10000, dateTimeModule.getTimeAsLong());
    }

    private static String getExpectedMessages() {
        String expected = "";
        expected += "Step 6.12.10.1.b - Waiting 5 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.1.b - Waiting 4 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.1.b - Waiting 3 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.1.b - Waiting 2 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.1.b - Waiting 1 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.3.b - Waiting 5 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.3.b - Waiting 4 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.3.b - Waiting 3 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.3.b - Waiting 2 seconds before checking for erased data" + NL;
        expected += "Step 6.12.10.3.b - Waiting 1 seconds before checking for erased data";
        return expected;
    }

}
