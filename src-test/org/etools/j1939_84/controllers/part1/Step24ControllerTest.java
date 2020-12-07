/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
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
 * The unit test for {@link Step24Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Step24ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM25ExpandedFreezeFrame.PGN;
    private static final int STEP_NUMBER = 24;

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

    private Step24Controller instance;

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

        instance = new Step24Controller(executor,
                                        engineSpeedModule,
                                        bannerModule,
                                        vehicleInformationModule,
                                        dtcModule,
                                        dataRepository);

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
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFail() {

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State
        };
        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(
                Packet.create(PGN, 0x00, data));

        when(dtcModule.requestDM25(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteEight() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0x00));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(dtcModule).setJ1939(j1939);

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteFive() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteFour() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x0E,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteOne() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x0B,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteSeven() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0x00));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteSix() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0x00,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteThree() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x0D,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteTwo() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x0C,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testFailByteZero() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x0A,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]");

        String expectedResults = "FAIL: 6.1.24.2.a. Fail if any OBD ECU provides freeze frame data other than no freeze frame data stored [i.e., bytes 1-5= 0x00 and bytes 6-8 = 0xFF]"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
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
     * {@link org.etools.j1939_84.controllers.StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getStepNumber()}.
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
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testRun() {

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.emptyList());
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step24Controller#run()}.
     */
    @Test
    public void testRunTwo() {

        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN,
                                                                                   0,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0x00,
                                                                                   0xFF,
                                                                                   0xFF,
                                                                                   0xFF));

        when(dtcModule.requestDM25(any(), eq(0x00))).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = mock(OBDModuleInformation.class);
        SupportedSPN supportedSpn = mock(SupportedSPN.class);
        when(obdInfo.getFreezeFrameSpns()).thenReturn(Collections.singletonList(supportedSpn));
        when(dataRepository.getObdModules()).thenReturn(Collections.singletonList(obdInfo));

        runTest();

        verify(dataRepository).getObdModules();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM25(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }
}
