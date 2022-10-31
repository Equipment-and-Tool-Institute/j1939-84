/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.DENIED;
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
public class Part04Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
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

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part04Step13Controller(executor,
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
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));

        when(communicationsModule.requestDM3(any(), eq(0)))
                                                              .thenReturn(List.of(AcknowledgmentPacket.create(0,
                                                                                                              NACK)));
        when(communicationsModule.requestDM3(any(), eq(1)))
                                                              .thenReturn(List.of(AcknowledgmentPacket.create(1,
                                                                                                              DENIED)));
        when(communicationsModule.requestDM3(any(), eq(2)))
                                                              .thenReturn(List.of());

        runTest();

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any(), eq(1));
        verify(communicationsModule).requestDM3(any(), eq(2));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.2.c"));
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.4.a"));

        String expected = "";
        expected += "Step 6.4.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());
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
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.2.c"));
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.4.a"));

        String expected = "";
        expected += "Step 6.4.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.13.2.a - Engine #1 (0) did not NACK with control byte 1 or 2 or 3");
    }

    @Test
    public void testWarningForBusy() {

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.requestDM3(any(), eq(0)))
                                                              .thenReturn(List.of(AcknowledgmentPacket.create(0,
                                                                                                              BUSY)));

        runTest();

        verify(communicationsModule).requestDM3(any(), eq(0));
        verify(communicationsModule).requestDM3(any());

        verify(verifier).setJ1939(any());
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.2.c"));
        verify(verifier).verifyDataNotErased(any(), eq("6.4.13.4.a"));

        String expected = "";
        expected += "Step 6.4.13.1.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.1.b - Waiting 1 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 5 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 4 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 3 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 2 seconds before checking for erased information" + NL;
        expected += "Step 6.4.13.3.b - Waiting 1 seconds before checking for erased information";
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.4.13.2.b - Engine #1 (0) NACKs with control = 3");
    }

}
