/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 2;
    private static final int PGN = DM25ExpandedFreezeFrame.PGN;
    private static final int STEP_NUMBER = 14;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step14Controller instance;

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

        instance = new Part02Step14Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
                                              communicationsModule);

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

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
        DM25ExpandedFreezeFrame packet = new DM25ExpandedFreezeFrame(Packet.create(PGN, 0x00, data));

        when(communicationsModule.requestDM25(any(), eq(0x00), any())).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        obdInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 1)), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0x00), any());

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.2.14.2.a - Engine #1 (0) provided freeze frame data other than no freeze frame data stored");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoResponses() {

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        obdInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 1)), 1);
        dataRepository.putObdModule(obdInfo);

        when(communicationsModule.requestDM25(any(), eq(0x00), any())).thenReturn(new BusResult<>(true));

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0x00), any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testNoErrors() {

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

        when(communicationsModule.requestDM25(any(), eq(0x00), any())).thenReturn(new BusResult<>(false, packet));

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        obdInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 1)), 1);
        dataRepository.putObdModule(obdInfo);

        runTest();

        verify(communicationsModule).requestDM25(any(), eq(0x00), any());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }
}
