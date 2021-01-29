/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step22Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step22ControllerTest extends AbstractControllerTest {
    private static final int PART = 1;
    private static final int STEP = 22;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step22Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step22Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dtcModule,
                                 mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART + " Step " + STEP, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step22Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link Part01Step22Controller#run()} with
     * non-error packets.
     */
    @Test
    public void testNoErrors() {

        DM29DtcCounts packet1 = DM29DtcCounts.create(1, 0, 0, 0, 0, 0);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(dtcModule.requestDM29(any())).thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));
        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link Part01Step22Controller##run()}
     * with the packets in error condition.
     */
    @Test
    public void testAckPacketFailures() {
        DM29DtcCounts packet1 = DM29DtcCounts.create(0x01, 0x00, 0x00, 0x01, 0x00, 0x55);
        DM29DtcCounts packet2 = DM29DtcCounts.create(0x04, 0x00, 0x01, 0x00, 0x02, 0x00);
        AcknowledgmentPacket ack3 = new AcknowledgmentPacket(
                Packet.create(0xE8FF, 0x03, 0x00, 0x00, 0x00, 0x02, 0xFF));
        AcknowledgmentPacket ack21 = new AcknowledgmentPacket(
                Packet.create(0xE8FF, 0x21, 0x01, 0xFF, 0xFF, 0xFF, 0xF9));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));
        dataRepository.putObdModule(new OBDModuleInformation(4));

        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet1, packet2), List.of(ack3, ack21)));
        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(dtcModule.requestDM29(any(), eq(0x03))).thenReturn(new BusResult<>(false, ack3));
        when(dtcModule.requestDM29(any(), eq(0x04))).thenReturn(new BusResult<>(false, ack21));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));
        verify(dtcModule).requestDM29(any(), eq(0x03));
        verify(dtcModule).requestDM29(any(), eq(0x04));

        String expectedResults = "";
        expectedResults += "FAIL: 6.1.22.2.a - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.a - Transmission #2 (4) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.4.b - OBD module Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.a - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.a - Transmission #2 (4) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.b - OBD module Transmission #1 (3) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    /**
     * Test method for
     * {@link Part01Step22Controller##run()}
     * with the packets in error condition.
     */
    @Test
    public void testDM27SupportedFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM29DtcCounts packet1 = DM29DtcCounts.create(0x01, 0x00, 0x00, 0x01, 0x00, 0x55);
        DM29DtcCounts packet2 = DM29DtcCounts.create(0x02, 0x00, 0x01, 0x00, 0x02, 0x00);
        DM29DtcCounts packet3 = DM29DtcCounts.create(0x03, 0x00, 0x00, 0x00, 0x02, 0xFF);

        DM29DtcCounts obdPacket3 = DM29DtcCounts.create(0x03, 0x00, 0x00, 0x00, 0x44, 0x55);

        when(dtcModule.requestDM29(any())).thenReturn(new RequestResult<>(false, packet1, packet2, packet3));

        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(dtcModule.requestDM29(any(), eq(0x03))).thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));
        verify(dtcModule).requestDM29(any(), eq(0x03));

        String expectedResults = "";
        expectedResults += "FAIL: 6.1.22.2.a - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.a - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0" + NL;
        expectedResults += "FAIL: 6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)"
        );

    }

    /**
     * Test method for
     * {@link Part01Step22Controller##run()}
     * with the packets in error condition.
     */
    @Test
    public void testDM27UnsupportedFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM29DtcCounts packet1 = DM29DtcCounts.create(0x01, 0x11, 0xFF, 0x33, 0x44, 0x55);
        DM29DtcCounts packet2 = DM29DtcCounts.create(0x02, 0x00, 0x00, 0x04, 0x00, 0xFF);
        DM29DtcCounts packet3 = DM29DtcCounts.create(0x03, 0x00, 0xFF, 0x04, 0x00, 0xFF);
        DM29DtcCounts packet4 = DM29DtcCounts.create(0x04, 0x01, 0x00, 0x00, 0x00, 0xFF);
        DM29DtcCounts packet5 = DM29DtcCounts.create(0x05, 0x00, 0x00, 0x00, 0x00, 0xFF);

        DM29DtcCounts obdPacket3 = DM29DtcCounts.create(0x03, 0x11, 0x22, 0x13, 0x44, 0x55);

        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, packet1, packet2, packet3, packet4, packet5));

        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(dtcModule.requestDM29(any(), eq(0x03))).thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));
        verify(dtcModule).requestDM29(any(), eq(0x03));

        String expectedResults = "";
        expectedResults += "FAIL: 6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.a - Transmission #2 (4) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.a - Shift Console - Primary (5) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.b - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.b - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0" + NL;
        expectedResults += "FAIL: 6.1.22.2.c - A non-OBD ECU Transmission #2 (4) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0" + NL;
        expectedResults += "FAIL: 6.1.22.2.c - A non-OBD ECU Shift Console - Primary (5) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0" + NL;
        expectedResults += "FAIL: 6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Transmission #2 (4) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.a - Shift Console - Primary (5) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.b - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.b - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.c - A non-OBD ECU Transmission #2 (4) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.2.c - A non-OBD ECU Shift Console - Primary (5) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0"
        );
        verify(mockListener).addOutcome(PART, STEP, FAIL,
                "6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)"
        );

    }

    /**
     * Test method for
     * {@link Part01Step22Controller#run()} with
     * no response to any request.
     */
    @Test
    public void testEmptyPacketFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(dtcModule.requestDM29(any())).thenReturn(new RequestResult<>(true));
        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(true));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.1.22.2.d - No OBD ECU provided DM29");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        String expectedResults = "";
        expectedResults += "FAIL: 6.1.22.2.d - No OBD ECU provided DM29" + NL;
        expectedResults += "FAIL: 6.1.22.4.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link Part01Step22Controller##run()}
     * with the packets in error condition.
     */
    @Test
    public void testFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        DM29DtcCounts packet1 = DM29DtcCounts.create(0x01, 0x00, 0xFF, 0x00, 0x00, 0x55);
        DM29DtcCounts packet2 = DM29DtcCounts.create(0x02, 0x00, 0x00, 0x00, 0x02, 0x00);
        DM29DtcCounts packet3 = DM29DtcCounts.create(0x03, 0x00, 0xFF, 0x00, 0x02, 0xFF);

        DM29DtcCounts obdPacket3 = DM29DtcCounts.create(0x03, 0x00, 0x00, 0x00, 0x44, 0x55);

        when(dtcModule.requestDM29(any())).thenReturn(new RequestResult<>(false, packet1, packet2, packet3));

        when(dtcModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(false, packet1));
        when(dtcModule.requestDM29(any(), eq(0x03))).thenReturn(new BusResult<>(false, obdPacket3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM29(any(), eq(0x01));
        verify(dtcModule).requestDM29(any(), eq(0x03));

        String expectedResults = "";
        expectedResults += "FAIL: 6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.b - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.b - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0" + NL;
        expectedResults += "FAIL: 6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0" + NL;
        expectedResults += "FAIL: 6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.c - A non-OBD ECU Turbocharger (2) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.b - Engine #2 (1) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.b - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.a - Difference compared to data received during global request from Transmission #1 (3)");

    }

}
