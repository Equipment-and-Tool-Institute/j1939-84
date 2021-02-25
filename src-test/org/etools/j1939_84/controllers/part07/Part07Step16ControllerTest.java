/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part01.SectionA5Verifier;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part07Step16ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 16;

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
                                              diagnosticMessageModule,
                                              verifier);

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
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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

        var nackPacket = AcknowledgmentPacket.create(0, NACK);
        when(diagnosticMessageModule.requestDM3(any())).thenReturn(List.of(nackPacket));
        when(diagnosticMessageModule.requestDM3(any(), eq(0)))
                                                              .thenReturn(List.of(nackPacket));

        runTest();

        verify(diagnosticMessageModule).requestDM3(any());
        verify(diagnosticMessageModule).requestDM3(any(), eq(0));

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.2.a"));
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.4.a"));

        String expected = "";
        expected += "Step 6.7.16.1.b Waiting 5 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 4 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 3 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 2 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 1 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 5 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 4 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 3 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 2 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 1 seconds";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForWrongResponse() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(diagnosticMessageModule.requestDM3(any(), eq(0)))
                                                              .thenReturn(List.of(AcknowledgmentPacket.create(0, ACK)));

        runTest();

        verify(diagnosticMessageModule).requestDM3(any(), eq(0));
        verify(diagnosticMessageModule).requestDM3(any());

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.2.a"));
        verify(verifier).verifyDataNotErased(any(), eq("6.7.16.4.a"));

        String expected = "";
        expected += "Step 6.7.16.1.b Waiting 5 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 4 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 3 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 2 seconds" + NL;
        expected += "Step 6.7.16.1.b Waiting 1 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 5 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 4 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 3 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 2 seconds" + NL;
        expected += "Step 6.7.16.3.b Waiting 1 seconds";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.16.4.a - Engine #1 (0) did not NACK the DS DM3 request");
    }
}
