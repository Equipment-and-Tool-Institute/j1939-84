/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

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
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
    private DiagnosticMessageModule diagnosticMessageModule;

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

    private static DM27AllPendingDTCsPacket dm27(int source) {
        return new DM27AllPendingDTCsPacket(Packet.create(DM27AllPendingDTCsPacket.PGN, source, 0, 0, 0, 0, 0, 0));
    }

    @Before
    public void setUp() {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step22Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
    public void testNoErrors() {

        DM29DtcCounts packet1 = DM29DtcCounts.create(1, 0, 0, 0, 0, 0, 0);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(1);
        obdModuleInformation.set(dm27(1), 1);
        dataRepository.putObdModule(obdModuleInformation);

        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM29(any(), eq(0x01))).thenReturn(BusResult.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM29(any(), eq(0x01));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testFailures() {

        // Module 0 will support DM27 and have no errors
        OBDModuleInformation module0 = new OBDModuleInformation(0);
        module0.set(dm27(0), 1);
        dataRepository.putObdModule(module0);
        DM29DtcCounts packet0 = DM29DtcCounts.create(0, 0, 0, 0, 0, 0, 0);

        // Module 1 will not support DM27 and have no errors
        dataRepository.putObdModule(new OBDModuleInformation(1));
        DM29DtcCounts packet1 = DM29DtcCounts.create(0x01, 0, 0, 0xFF, 0, 0, 0);

        // Module 2 will support DM27 but return bad values
        OBDModuleInformation module2 = new OBDModuleInformation(2);
        module2.set(dm27(2), 1);
        dataRepository.putObdModule(module2);
        DM29DtcCounts packet2 = DM29DtcCounts.create(0x02, 0, 0x00, 0x00, 0x04, 0x00, 0xFF);

        // Module 3 will not support DM27 but return bad values
        dataRepository.putObdModule(new OBDModuleInformation(3));
        DM29DtcCounts packet3 = DM29DtcCounts.create(0x03, 0, 0x00, 0x00, 0x04, 0x00, 0xFF);

        // Module 4 will not respond at all
        dataRepository.putObdModule(new OBDModuleInformation(4));

        // Module 5 will be a non-obd module with no issues
        DM29DtcCounts packet5 = DM29DtcCounts.create(0x05, 0, 0, 0xFF, 0, 0, 0);

        // Module 6 will be a non-obd module with bad values
        DM29DtcCounts packet6 = DM29DtcCounts.create(0x06, 0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF);

        // Module 7 will return different values global/ds
        dataRepository.putObdModule(new OBDModuleInformation(7));
        DM29DtcCounts packet71 = DM29DtcCounts.create(0x07, 0, 0, 0xFF, 0, 0, 0);
        DM29DtcCounts packet72 = DM29DtcCounts.create(0x07, 0, 1, 0xFF, 0, 0, 0);

        when(diagnosticMessageModule.requestDM29(any()))
                                                        .thenReturn(new RequestResult<>(false,
                                                                                        packet0,
                                                                                        packet1,
                                                                                        packet2,
                                                                                        packet3,
                                                                                        packet5,
                                                                                        packet6,
                                                                                        packet71));

        when(diagnosticMessageModule.requestDM29(any(), eq(0))).thenReturn(BusResult.of(packet0));
        when(diagnosticMessageModule.requestDM29(any(), eq(1))).thenReturn(BusResult.of(packet1));
        when(diagnosticMessageModule.requestDM29(any(), eq(2))).thenReturn(BusResult.of(packet2));
        when(diagnosticMessageModule.requestDM29(any(), eq(3))).thenReturn(BusResult.of(packet3));
        when(diagnosticMessageModule.requestDM29(any(), eq(4))).thenReturn(new BusResult<>(true));
        when(diagnosticMessageModule.requestDM29(any(), eq(7))).thenReturn(BusResult.of(packet72));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM29(any(), eq(0));
        verify(diagnosticMessageModule).requestDM29(any(), eq(1));
        verify(diagnosticMessageModule).requestDM29(any(), eq(2));
        verify(diagnosticMessageModule).requestDM29(any(), eq(3));
        verify(diagnosticMessageModule).requestDM29(any(), eq(4));
        verify(diagnosticMessageModule).requestDM29(any(), eq(7));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.a - Turbocharger (2) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.b - Transmission #1 (3) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.b - Shift Console - Secondary (6) did not report pending/all pending/MIL on/previous MIL on/permanent = 0/0xFF/0/0/0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.2.c - A non-OBD ECU Shift Console - Secondary (6) reported pending, MIL-on, previously MIL-on or permanent DTC count greater than 0");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.a - Difference compared to data received during global request from Power TakeOff - (Main or Rear) (7)");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.b - OBD ECU Transmission #2 (4) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @Test
    public void testEmptyPacketFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(diagnosticMessageModule.requestDM29(any())).thenReturn(new RequestResult<>(true));
        when(diagnosticMessageModule.requestDM29(any(), eq(0x01))).thenReturn(new BusResult<>(true));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM29(any(), eq(0x01));

        verify(mockListener).addOutcome(PART, STEP, FAIL, "6.1.22.2.d - No OBD ECU provided DM29");
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.1.22.4.b - OBD ECU Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

}
