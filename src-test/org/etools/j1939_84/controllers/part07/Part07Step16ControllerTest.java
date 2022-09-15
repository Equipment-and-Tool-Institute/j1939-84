/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
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
public class Part07Step16ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 16;

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
    private SectionA5Verifier verifier;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part07Step16Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
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

        var nackPacket = AcknowledgmentPacket.create(0, Response.DENIED);
        when(communicationsModule.requestDM3(any())).thenReturn(List.of(nackPacket));
        when(communicationsModule.requestDM3(any(), eq(0))).thenReturn(List.of(nackPacket));

        runTest();

        verify(communicationsModule).requestDM3(any());
        verify(communicationsModule).requestDM3(any(), eq(0));

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.2.a"));
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.4.b"));

        String expected = "";
        expected += "Step 6.7.16.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void test6_7_16_4_c() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        var nackPacket = AcknowledgmentPacket.create(0, Response.NACK);
        when(communicationsModule.requestDM3(any())).thenReturn(List.of(nackPacket));
        when(communicationsModule.requestDM3(any(), eq(0))).thenReturn(List.of(nackPacket));

        runTest();

        verify(communicationsModule).requestDM3(any());
        verify(communicationsModule).requestDM3(any(), eq(0));

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.2.a"));
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.4.b"));

        String expected = "";
        expected += "Step 6.7.16.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        Outcome.WARN,
                                        "6.7.16.4.c - OBD ECU Engine #1 (0) did provide a NACK with control byte = 3 for the DS query");
    }

    @Test
    public void testFailureForWrongResponse() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.requestDM3(any(), eq(0)))
                                                           .thenReturn(List.of(AcknowledgmentPacket.create(0,
                                                                                                           ACK)));

        runTest();

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.2.a"));
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.4.b"));

        String expected = "";
        expected += "Step 6.7.16.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.7.16.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.16.4.a - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
    }
}
