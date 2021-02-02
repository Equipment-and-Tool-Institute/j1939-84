/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation.*;
import static org.etools.j1939_84.bus.j1939.packets.DTCLampStatus.create;
import static org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode.create;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.*;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OTHER;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.SLOW_FLASH;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM34NTEStatus;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
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
    private DiagnosticMessageModule diagnosticMessageModule;

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
                diagnosticMessageModule);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule, diagnosticMessageModule);
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

    /**
     * Test method for
     * {@link Part02Step13Controller#run()}.
     */
    @Test
    public void testNoObdResponseFailure() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        when(diagnosticMessageModule.requestDM31(any(), eq(0x00)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of()));

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0x00));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                       "6.2.13.2.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query");

        String expectedResults = "FAIL: 6.2.13.2.b - OBD module Engine #1 (0) did not provide a response to Global query and did not provide a NACK for the DS query" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link Part02Step13Controller#run()}.
     */
    @Test
    public void testAckResponsePass() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        AcknowledgmentPacket ackPacket0x00 = AcknowledgmentPacket.create(0x0,
                                                                         NACK,
                                                                         0,
                                                                         0xF9,
                                                                         PGN);

        when(diagnosticMessageModule.requestDM31(any(), eq(0x00)))
                .thenReturn(new RequestResult<>(false, List.of(), List.of(ackPacket0x00)));

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0x00));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }
    /**
     * Test method for
     * {@link Part02Step13Controller#run()}.
     */
    @Test
    public void testMilOndResponseFailure() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        dataRepository.putObdModule(obdModuleInformation1);
        OBDModuleInformation obdModuleInformation2 = new OBDModuleInformation(2);
        dataRepository.putObdModule(obdModuleInformation2);

        DiagnosticTroubleCode dtc = create(609, 19, 1, 1);
        DTCLampStatus dtcLampStatus = create(dtc, OFF, SLOW_FLASH, OTHER, OTHER);
        DM31DtcToLampAssociation packet = DM31DtcToLampAssociation.create(0,
                                                                          List.of(dtcLampStatus));
        DiagnosticTroubleCode dtc1 = create(4334, 77, 0, 23);
        DTCLampStatus dtcLampStatus1 = create(dtc1, ON, SLOW_FLASH, OTHER, OTHER);
        DM31DtcToLampAssociation packet1 = DM31DtcToLampAssociation.create(1,
                                                                          List.of(dtcLampStatus1));
        DiagnosticTroubleCode dtc2 = create(62002, 77, 0, 23);
        DTCLampStatus dtcLampStatus2 = create(dtc2, ON, ON, ON, ON);
        DM31DtcToLampAssociation packet2 = DM31DtcToLampAssociation.create(2,
                                                                           List.of(dtcLampStatus2));


        when(diagnosticMessageModule.requestDM31(any(), eq(0x00)))
                .thenReturn(new RequestResult<>(false, List.of(packet), List.of()));
        when(diagnosticMessageModule.requestDM31(any(), eq(0x01)))
                .thenReturn(new RequestResult<>(false, List.of(packet1), List.of()));
        when(diagnosticMessageModule.requestDM31(any(), eq(0x02)))
                .thenReturn(new RequestResult<>(false, List.of(packet2), List.of()));



        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM31(any(), eq(0x01));
        verify(diagnosticMessageModule).requestDM31(any(), eq(0x02));

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                       "6.2.13.2.a - ECU Engine #1 (0) reported MIL light not off/alt-off");
        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                       "6.2.13.2.a - ECU Engine #2 (1) reported MIL light not off/alt-off");
        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                                       "6.2.13.2.a - ECU Turbocharger (2) reported MIL light not off/alt-off");


        String expectedResults = "FAIL: 6.2.13.2.a - ECU Engine #1 (0) reported MIL light not off/alt-off" + NL;
        expectedResults += "FAIL: 6.2.13.2.a - ECU Engine #2 (1) reported MIL light not off/alt-off" + NL;
        expectedResults += "FAIL: 6.2.13.2.a - ECU Turbocharger (2) reported MIL light not off/alt-off" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for
     * {@link StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link Part02Step13Controller#run()}.
     */
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
        DM31DtcToLampAssociation packet = new DM31DtcToLampAssociation(
                Packet.create(PGN, 0x00, data));

        when(diagnosticMessageModule.requestDM31(any(), eq(0)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet), Collections.emptyList()));

        runTest();

        verify(diagnosticMessageModule).requestDM31(any(), eq(0));

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }
}
