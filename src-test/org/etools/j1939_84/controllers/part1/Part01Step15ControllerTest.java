/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM1ActiveDTCsPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
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
 * The unit test for {@link Part01Step15Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM1ActiveDTCsPacket.PGN;
    private static final int STEP_NUMBER = 15;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step15Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;


    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step15Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 dtcModule,
                                 mockListener);
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#Part01Step15Controller(DataRepository)}.
     */
    @Test
    public void testEmptyPacketFailure() {
        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.readDM1(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), Collections.emptyList()));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).readDM1(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.1.15.2 - Fail if no OBD ECU provides DM1");

        String expectedResults = "FAIL: 6.1.15.2 - Fail if no OBD ECU provides DM1" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step15Controller#Part01Step15Controller(DataRepository)}.
     */
    @Test
    public void testFailures() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x01, 0x00, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x17, 0x00, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet3 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x03, 0xAA, 0x55, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet4 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x00, 0x40, 0x00, 0x61, 0x02, 0x13, 0x80, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));
        DM1ActiveDTCsPacket packet5 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x00, 0xC0, 0xC0, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        List<Integer> obdModuleAddresses = Arrays.asList(0x01, 0x03);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.readDM1(any()))
                .thenReturn(new RequestResult<>(false, Arrays.asList(packet1, packet2, packet3, packet4, packet5),
                                                Collections.emptyList()));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).readDM1(any());

        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                  "6.1.15.2.a - Fail if any OBD ECU reports an active DTC");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                  "6.1.15.2.b - Fail if any OBD ECU does not report MIL off per Section A.8 allowed values");
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                                        "6.1.15.3.a - any ECU reports the non-preferred MIL off format per Section A.8");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                  "6.1.15.2.d - Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                  "6.1.15.2.c - Fail if any non-OBD ECU does not report MIL off or not supported");
        verify(mockListener, times(2)).addOutcome(PART_NUMBER, STEP_NUMBER, WARN,
                                                  "6.1.15.3.b - Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1");

        String expectedResults = "FAIL: 6.1.15.2.a - Fail if any OBD ECU reports an active DTC" + NL;
        expectedResults += "FAIL: 6.1.15.2.b - Fail if any OBD ECU does not report MIL off per Section A.8 allowed values"
                + NL;
        expectedResults += "WARN: 6.1.15.3.a - any ECU reports the non-preferred MIL off format per Section A.8" + NL;
        expectedResults += "FAIL: 6.1.15.2.d - Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1"
                + NL;
        expectedResults += "FAIL: 6.1.15.2.c - Fail if any non-OBD ECU does not report MIL off or not supported" + NL;
        expectedResults += "WARN: 6.1.15.3.b - Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1"
                + NL;
        expectedResults += "FAIL: 6.1.15.2.a - Fail if any OBD ECU reports an active DTC" + NL;
        expectedResults += "FAIL: 6.1.15.2.b - Fail if any OBD ECU does not report MIL off per Section A.8 allowed values"
                + NL;
        expectedResults += "FAIL: 6.1.15.2.d - Fail if any OBD ECU reports SPN conversion method (SPN 1706) equal to binary 1"
                + NL;
        expectedResults += "FAIL: 6.1.15.2.c - Fail if any non-OBD ECU does not report MIL off or not supported" + NL;
        expectedResults += "WARN: 6.1.15.3.b - Warn if any non-OBD ECU reports SPN conversion method (SPN 1706) equal to 1"
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
     * {@link Part01Step15Controller#getStepNumber()}.
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
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link Part01Step14Controller#run()}.
     */
    @Test
    public void testRun() {
        DM1ActiveDTCsPacket packet1 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x01, 0x00, 0xFF, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF));
        DM1ActiveDTCsPacket packet2 = new DM1ActiveDTCsPacket(
                Packet.create(PGN, 0x17, 0x00, 0xFF, 0x61, 0x02, 0x13, 0x00, 0x21, 0x06,
                              0x1F, 0x00, 0xEE, 0x10, 0x04, 0x00));

        List<Integer> obdModuleAddresses = Collections.singletonList(0x01);
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);

        when(dtcModule.readDM1(any()))
                .thenReturn(new RequestResult<>(false, Arrays.asList(packet1, packet2), Collections.emptyList()));

        runTest();
        verify(dataRepository).getObdModuleAddresses();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).readDM1(any());

        String expectedResults = "";
        assertEquals(expectedResults, listener.getResults());
    }
}
