/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM27AllPendingDTCsPacket;
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

@RunWith(MockitoJUnitRunner.class)
public class Part02Step11ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int PGN = DM27AllPendingDTCsPacket.PGN;
    private static final int STEP = 11;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step11Controller instance;

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

        instance = new Part02Step11Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                DateTimeModule.getInstance(),
                dtcModule);

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

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART + " Step " + STEP, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testEmptyPacketFailure() {

        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(dtcModule.requestDM27(any(), eq(true))).thenReturn(new RequestResult<>(false));
        when(dtcModule.requestDM27(any(), eq(true), eq(0x01))).thenReturn(new BusResult<>(false));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM27(any(), eq(true));
        verify(dtcModule).requestDM27(any(), eq(true), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    @Test
    public void testFailures() {
        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(
                Packet.create(PGN, 0x01, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.setLastDM27(packet1);
        dataRepository.putObdModule(obdModule1);
        when(dtcModule.requestDM27(any(), eq(true), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        dataRepository.putObdModule(new OBDModuleInformation(2));
        when(dtcModule.requestDM27(any(), eq(true), eq(0x02))).thenReturn(new BusResult<>(true));

        DM27AllPendingDTCsPacket packet3 = new DM27AllPendingDTCsPacket(
                Packet.create(PGN, 0x03, 0x00, 0x00, 0x04, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        OBDModuleInformation obdModule2 = new OBDModuleInformation(3);
        obdModule2.setLastDM27(packet3);
        dataRepository.putObdModule(obdModule2);

        DM27AllPendingDTCsPacket obdPacket3 = new DM27AllPendingDTCsPacket(
                Packet.create(PGN, 0x03, 0x11, 0x22, 0x13, 0x44, 0x55, 0x66, 0x77, 0x88));
        when(dtcModule.requestDM27(any(), eq(true), eq(0x03))).thenReturn(new BusResult<>(false, obdPacket3));

        when(dtcModule.requestDM27(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1, packet3));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM27(any(), eq(true));
        verify(dtcModule).requestDM27(any(), eq(true), eq(0x01));
        verify(dtcModule).requestDM27(any(), eq(true), eq(0x02));
        verify(dtcModule).requestDM27(any(), eq(true), eq(0x03));

        String expectedResults = "";
        expectedResults += "FAIL: 6.2.11.2.b - Engine #2 (1) reported an all pending DTC" + NL;
        expectedResults += "FAIL: 6.2.11.2.b - Transmission #1 (3) reported an all pending DTC" + NL;
        expectedResults += "FAIL: 6.2.11.2.c - Engine #2 (1) did not report MIL off" + NL;
        expectedResults += "FAIL: 6.2.11.2.c - Transmission #1 (3) did not report MIL off" + NL;
        expectedResults += "FAIL: 6.2.11.4.a - Difference compared to data received during global request from Transmission #1 (3)" + NL;

        assertEquals(expectedResults, listener.getResults());

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.11.2.b - Engine #2 (1) reported an all pending DTC");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.11.2.b - Transmission #1 (3) reported an all pending DTC");
        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.11.2.c - Engine #2 (1) did not report MIL off");
        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.2.11.2.c - Transmission #1 (3) did not report MIL off");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.11.4.a - Difference compared to data received during global request from Transmission #1 (3)");
    }

    @Test
    public void testNoFailures() {
        DM27AllPendingDTCsPacket packet1 = new DM27AllPendingDTCsPacket(Packet.create(PGN, 0x01, 0, 0xFF, 0, 0, 0, 0));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.setLastDM27(packet1);
        dataRepository.putObdModule(obdModule1);
        when(dtcModule.requestDM27(any(), eq(true), eq(0x01))).thenReturn(new BusResult<>(false, packet1));

        when(dtcModule.requestDM27(any(), eq(true))).thenReturn(new RequestResult<>(false, packet1));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM27(any(), eq(true));
        verify(dtcModule).requestDM27(any(), eq(true), eq(0x01));

        assertEquals("", listener.getResults());
    }

}
