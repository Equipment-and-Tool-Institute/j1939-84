/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
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
 * The unit test for {@link Step18Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step18ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM12MILOnEmissionDTCPacket.PGN;
    private static final int STEP_NUMBER = 18;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step18Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDModuleInformation obdModuleInformation;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SectionA6Validator sectionA6Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() {

        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step18Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
                dtcModule, dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                dataRepository,
                dtcModule,
                mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step18Controller#run()} with
     * no response to any request.
     */
    @Test
    public void testEmptyPacketFailure() {
        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x01));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.2.c - Fail if no OBD ECU provides DM12");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.18.3 OBD module Engine #2 (1) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.18.3.a Destination Specific DM12 requests to OBD modules did not return any responses");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.18.2.c - Fail if no OBD ECU provides DM12" + NL;
        expectedResults += "WARN: 6.1.18.3 OBD module Engine #2 (1) did not return a response to a destination specific request"
                + NL;
        expectedResults += "WARN: 6.1.18.3.a Destination Specific DM12 requests to OBD modules did not return any responses"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step18Controller##run()}
     * with the packets in error condition.
     */
    @Test
    public void testFailures() {
        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x04, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        List<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x01);
                add(0x03);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM12MILOnEmissionDTCPacket obdPacket3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));

        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, new ArrayList<>() {
                    {
                        add(packet1);
                        add(packet3);
                    }
                }, Collections.emptyList()));

        when(dtcModule.requestDM12(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet1));
        when(dtcModule.requestDM12(any(), eq(true), eq(0x03)))
                .thenReturn(new BusResult<>(false, obdPacket3));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x01));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x03));

        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.2.a - Fail if any ECU reports active DTCs");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.2.b - Fail if any ECU does not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.4.a Fail if any difference compared to data received during global request");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.18.2.a - Fail if any ECU reports active DTCs" + NL;
        expectedResults += "FAIL: 6.1.18.2.b - Fail if any ECU does not report MIL off" + NL;
        expectedResults += "FAIL: 6.1.18.2.a - Fail if any ECU reports active DTCs" + NL;
        expectedResults += "FAIL: 6.1.18.2.b - Fail if any ECU does not report MIL off" + NL;
        expectedResults += "FAIL: 6.1.18.4.a Fail if any difference compared to data received during global request"
                + NL;

        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step13Controller#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step18Controller#run()} with
     * 'ACKs.
     */
    @Test
    public void testMoreFailures() {
        AcknowledgmentPacket ackPacket = new AcknowledgmentPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        DM12MILOnEmissionDTCPacket packet3 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, (byte) 0x0A, 0x44, 0x55, 0x66, 0x77,
                        0x88));

        List<Integer> obdModuleAddresses = Arrays.asList(0x01, 0x03);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        DM12MILOnEmissionDTCPacket packet3b = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));

        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, new ArrayList<>() {
                    {
                        add(packet1);
                        add(packet3);
                    }
                }, Collections.singletonList(ackPacket)));
        when(dtcModule.requestDM12(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(dtcModule.requestDM12(any(), eq(true), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet3b));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x01));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x03));

        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.2.a - Fail if any ECU reports active DTCs");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.2.b - Fail if any ECU does not report MIL off");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                "6.1.18.3 OBD module Engine #2 (1) did not return a response to a destination specific request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.4.a Fail if any difference compared to data received during global request");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.18.4.b b. Fail if NACK not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.18.2.a - Fail if any ECU reports active DTCs" + NL;
        expectedResults += "FAIL: 6.1.18.2.b - Fail if any ECU does not report MIL off" + NL;
        expectedResults += "FAIL: 6.1.18.2.a - Fail if any ECU reports active DTCs" + NL;
        expectedResults += "FAIL: 6.1.18.2.b - Fail if any ECU does not report MIL off" + NL;
        expectedResults += "WARN: 6.1.18.3 OBD module Engine #2 (1) did not return a response to a destination specific request"
                + NL;
        expectedResults += "FAIL: 6.1.18.4.a Fail if any difference compared to data received during global request"
                + NL;
        expectedResults += "FAIL: 6.1.18.4.b b. Fail if NACK not received from OBD ECUs that did not respond to global query"
                + NL;

        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step18Controller#run()} with
     * non-error packets.
     */
    @Test
    public void testNoErrors() {

        DM12MILOnEmissionDTCPacket packet1 = new DM12MILOnEmissionDTCPacket(
                Packet.create(PGN, 0x01, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00));

        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet1));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM12(any(), eq(true), eq(0x01));

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

}