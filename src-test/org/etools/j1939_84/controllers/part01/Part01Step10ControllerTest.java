/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
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

    public static final TimeUnit SECONDS = TimeUnit.SECONDS;
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

    private DataRepository dataRepository;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule dateTimeModule = new TestDateTimeModule();
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step10Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              dateTimeModule,
                                              dataRepository);
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
                                 diagnosticMessageModule,
                                 mockListener);
    }

    /**
     * Test three modules respond. One with NACK, one with BUSY and one
     * with a good message.
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.10.2.a", description = "Fail if NACK received from any HD OBD ECU.") })
    public void testMultipleModulesRespondToDM11RequestWithAFailure() {
        AcknowledgmentPacket nackPacket = AcknowledgmentPacket.create(1, NACK);
        AcknowledgmentPacket busyPacket = AcknowledgmentPacket.create(2, BUSY);
        AcknowledgmentPacket dm11Response = AcknowledgmentPacket.create(0x85,
                                                                        ACK,
                                                                        0,
                                                                        0xFF,
                                                                        DM11ClearActiveDTCsPacket.PGN);

        List<AcknowledgmentPacket> acknowledgmentPackets = List.of(nackPacket, busyPacket, dm11Response);

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));

        when(diagnosticMessageModule.requestDM11(any(ResultsListener.class),
                                                 eq(5L),
                                                 eq(TimeUnit.SECONDS))).thenReturn(acknowledgmentPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any(), eq(5L), eq(SECONDS));

        verify(mockListener).addOutcome(1, 10, FAIL, "6.1.10.2.a - The request for DM11 was NACK'ed by Engine #2 (1)");

        assertEquals("", listener.getResults());

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());
    }

    /**
     * Test two modules respond without issue. One with an ACK and the other
     * with a BUSY acknowledgement.
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.10.1.b", description = "Record all ACK/NACK/BUSY/Access Denied responses (for PGN 65235) in the log"),
            @TestItem(verifies = "6.1.10.3.a", description = "Warn if ACK received from any HD OBD ECU.") })
    public void testAckPacketResponseToDM11RequestWarning() {
        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0, ACK);
        AcknowledgmentPacket busyPacket = AcknowledgmentPacket.create(2, BUSY);

        List<AcknowledgmentPacket> acknowledgmentPackets = List.of(ackPacket, busyPacket);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(diagnosticMessageModule.requestDM11(any(), eq(5L), eq(SECONDS))).thenReturn(acknowledgmentPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any(ResultsListener.class), eq(5L), eq(SECONDS));

        verify(mockListener).addOutcome(1, 10, WARN, "6.1.10.3.a - The request for DM11 was ACK'ed by Engine #1 (0)");

        assertEquals("", listener.getResults());

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());
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

    /**
     * Global DM11 verify test passes if BUSY response received
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.10.1.a", description = "Global DM11 (send Request (PGN 59904) for PGN 65235)"),
            @TestItem(verifies = "6.1.10.1.b", description = "Record all ACK/NACK/BUSY/Access Denied responses (for PGN 65235) in the log")
    })
    public void testBusyResponseToDm11() {
        AcknowledgmentPacket acknowledgmentPacket = AcknowledgmentPacket.create(0x00,
                                                                                BUSY,
                                                                                0,
                                                                                0xFF,
                                                                                DM11ClearActiveDTCsPacket.PGN);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(diagnosticMessageModule.requestDM11(any(ResultsListener.class),
                                                 eq(5L),
                                                 eq(SECONDS))).thenReturn(List.of(acknowledgmentPacket));

        runTest();


        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any(ResultsListener.class), eq(5L), eq(SECONDS));

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test no module responds within five seconds passes
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.10.1.c", description = "Pass if no response to the global DM11 query has been received in 5 s") })
    public void testNoResponseInFiveSeconds() {

        when(diagnosticMessageModule.requestDM11(any(ResultsListener.class),
                                                 eq(5L),
                                                 eq(SECONDS))).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any(ResultsListener.class), eq(5L), eq(SECONDS));

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test NACK received from Global DM11 request fails
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.10.1.b", description = "Record all ACK/NACK/BUSY/Access Denied responses (for PGN 65235) in the log"),
            @TestItem(verifies = "6.1.10.2.a", description = "Fail if NACK received from any HD OBD ECU.") })
    public void testNackReturnedToDm11Request() {
        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0, NACK);
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(diagnosticMessageModule.requestDM11(any(ResultsListener.class),
                                                 eq(5L),
                                                 eq(SECONDS))).thenReturn(List.of(ackPacket));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM11(any(ResultsListener.class), eq(5L), eq(SECONDS));

        verify(mockListener).addOutcome(1,
                                        10,
                                        FAIL,
                                        "6.1.10.2.a - The request for DM11 was NACK'ed by Engine #1 (0)");

        String expectedMessages = "";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getResults());
    }
}
