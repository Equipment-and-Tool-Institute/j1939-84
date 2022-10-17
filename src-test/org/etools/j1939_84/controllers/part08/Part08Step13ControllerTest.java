/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
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

@RunWith(MockitoJUnitRunner.class)
public class Part08Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 13;

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
        instance = new Part08Step13Controller(executor,
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

        var nack = AcknowledgmentPacket.create(0, Response.DENIED);
        when(communicationsModule.requestDM3(any(), eq(0))).thenReturn(List.of(nack));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.2.b"));
        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.4.a"));

        assertEquals(10000, dateTimeModule.getTimeAsLong());

        String expected = "";
        expected += "Step 6.8.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void test_6_8_13_2_c() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        var nack = AcknowledgmentPacket.create(0, Response.BUSY);
        when(communicationsModule.requestDM3(any(), eq(0))).thenReturn(List.of(nack));

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.2.b"));
        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.4.a"));

        assertEquals(10000, dateTimeModule.getTimeAsLong());

        String expected = "";
        expected += "Step 6.8.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, Outcome.WARN,
                                        "6.8.13.2.c - OBD ECU Engine #1 (0) did provide a NACK with control byte = 3 for the DS query");
  }

    @Test
    public void testFailureForNoNACK() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        var nack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestDM3(any(), eq(0))).thenReturn(List.of(nack));
        when(communicationsModule.requestDM3(any(), eq(1))).thenReturn(List.of());

        runTest();

        verify(verifier).setJ1939(j1939);

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any(), eq(1));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.2.b"));
        verify(verifier).verifyDataNotErased(any(), eq("6.8.13.4.a"));

        assertEquals(10000, dateTimeModule.getTimeAsLong());

        String expected = "";
        expected += "Step 6.8.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.8.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.8.13.2.a - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.8.13.2.a - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

}
