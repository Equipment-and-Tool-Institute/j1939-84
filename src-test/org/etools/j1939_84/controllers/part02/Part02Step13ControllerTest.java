/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode.create;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.etools.j1939tools.j1939.packets.LampStatus.OTHER;
import static org.etools.j1939tools.j1939.packets.LampStatus.SLOW_FLASH;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DTCLampStatus;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

;

/**
 * The unit test for {@link Part02Step13Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 2;
    private static final int PGN = DM31DtcToLampAssociation.PGN;
    private static final int STEP_NUMBER = 13;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step13Controller instance;

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
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part02Step13Controller(
                                              executor,
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
    public void testNoObdResponseFailure() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        when(communicationsModule.requestDM31(any(), eq(0x00))).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.2.13.2.b - OBD ECU Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testAckResponsePass() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        AcknowledgmentPacket ackPacket0x00 = AcknowledgmentPacket.create(0, NACK);

        when(communicationsModule.requestDM31(any(), eq(0x00)))
                                                                  .thenReturn(new RequestResult<>(false,
                                                                                                  List.of(),
                                                                                                  List.of(ackPacket0x00)));

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testMilOndResponseFailure() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));

        DiagnosticTroubleCode dtc = create(609, 19, 1, 1);
        DTCLampStatus dtcLampStatus = DTCLampStatus.create(dtc, OFF, SLOW_FLASH, OTHER, OTHER);
        DM31DtcToLampAssociation packet = DM31DtcToLampAssociation.create(0, 0, dtcLampStatus);
        when(communicationsModule.requestDM31(any(), eq(0x00))).thenReturn(RequestResult.of(packet));

        DiagnosticTroubleCode dtc1 = create(4334, 77, 0, 23);
        DTCLampStatus dtcLampStatus1 = DTCLampStatus.create(dtc1, ON, FAST_FLASH, OTHER, OTHER);
        DM31DtcToLampAssociation packet1 = DM31DtcToLampAssociation.create(1, 0, dtcLampStatus1);
        when(communicationsModule.requestDM31(any(), eq(0x01))).thenReturn(RequestResult.of(packet1));

        DiagnosticTroubleCode dtc2 = create(62002, 77, 0, 23);
        DTCLampStatus dtcLampStatus2 = DTCLampStatus.create(dtc2, ON, ON, ON, ON);
        DM31DtcToLampAssociation packet2 = DM31DtcToLampAssociation.create(2, 0, dtcLampStatus2);
        when(communicationsModule.requestDM31(any(), eq(0x02))).thenReturn(RequestResult.of(packet2));

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0x00));
        verify(communicationsModule).requestDM31(any(), eq(0x01));
        verify(communicationsModule).requestDM31(any(), eq(0x02));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.2.13.2.a - Engine #1 (0) did not report MIL 'off'");
        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.2.13.2.a - Engine #2 (1) did not report MIL 'off'");
        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER,
                                                       STEP_NUMBER,
                                                       FAIL,
                                                       "6.2.13.2.a - Turbocharger (2) did not report MIL 'off'");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
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
    public void testRun() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF, // Lamp Status/State
        };
        DM31DtcToLampAssociation packet = new DM31DtcToLampAssociation(Packet.create(PGN, 0x00, data));
        when(communicationsModule.requestDM31(any(), eq(0))).thenReturn(RequestResult.of(packet));

        runTest();

        verify(communicationsModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }
}
