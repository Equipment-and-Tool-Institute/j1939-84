/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step10Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step10ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step10Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private DataRepository dataRepository;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule dateTimeModule = new TestDateTimeModule();

        instance = new Part01Step10Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              dateTimeModule,
                                              dataRepository);
        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 mockListener,
                                 dataRepository);
    }

    @Test
    public void testError() {
        AcknowledgmentPacket ackPacket = mock(AcknowledgmentPacket.class);
        when(ackPacket.getResponse()).thenReturn(Response.ACK);
        when(ackPacket.getSourceAddress()).thenReturn(0);

        AcknowledgmentPacket ackPacket2 = mock(AcknowledgmentPacket.class);
        when(ackPacket2.getSourceAddress()).thenReturn(2);

        AcknowledgmentPacket nackPacket = mock(AcknowledgmentPacket.class);
        when(nackPacket.getResponse()).thenReturn(Response.NACK);
        when(nackPacket.getSourceAddress()).thenReturn(1);

        List<AcknowledgmentPacket> acknowledgmentPackets = List.of(ackPacket, nackPacket, ackPacket2);

        when(dataRepository.isObdModule(0)).thenReturn(true);
        when(dataRepository.isObdModule(1)).thenReturn(true);
        when(dataRepository.isObdModule(2)).thenReturn(false);

        when(diagnosticMessageModule.requestDM11(any())).thenReturn(acknowledgmentPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any());

        verify(mockListener).addOutcome(1, 10, FAIL, "6.1.10.3.a - The request for DM11 was NACK'ed by Engine #2 (1)");
        verify(mockListener).addOutcome(1, 10, WARN, "6.1.10.3.a - The request for DM11 was ACK'ed by Engine #1 (0)");

        verify(dataRepository).isObdModule(0);
        verify(dataRepository).isObdModule(1);
        verify(dataRepository).isObdModule(2);

        String expected = "";
        expected += "FAIL: 6.1.10.3.a - The request for DM11 was NACK'ed by Engine #2 (1)" + NL;
        expected += "WARN: 6.1.10.3.a - The request for DM11 was ACK'ed by Engine #1 (0)" + NL;
        assertEquals(expected, listener.getResults());

        String expectedMessages = "";
        expectedMessages += "Waiting for 5 seconds" + NL;
        expectedMessages += "Waiting for 4 seconds" + NL;
        expectedMessages += "Waiting for 3 seconds" + NL;
        expectedMessages += "Waiting for 2 seconds" + NL;
        expectedMessages += "Waiting for 1 seconds";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 10", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 10, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoError() {

        AcknowledgmentPacket acknowledgmentPacket = mock(AcknowledgmentPacket.class);
        when(acknowledgmentPacket.getResponse()).thenReturn(Response.BUSY);
        when(acknowledgmentPacket.getSourceAddress()).thenReturn(0);

        when(dataRepository.isObdModule(0)).thenReturn(true);

        when(diagnosticMessageModule.requestDM11(any())).thenReturn(List.of(acknowledgmentPacket));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any());
        verify(dataRepository).isObdModule(0);

        String expectedMessages = "";
        expectedMessages += "Waiting for 5 seconds" + NL;
        expectedMessages += "Waiting for 4 seconds" + NL;
        expectedMessages += "Waiting for 3 seconds" + NL;
        expectedMessages += "Waiting for 2 seconds" + NL;
        expectedMessages += "Waiting for 1 seconds";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
