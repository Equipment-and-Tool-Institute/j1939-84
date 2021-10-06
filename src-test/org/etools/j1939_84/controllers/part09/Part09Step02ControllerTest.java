/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

import static net.solidDesign.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.solidDesign.j1939.packets.LampStatus.OFF;
import static net.solidDesign.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.AcknowledgmentPacket;
import net.solidDesign.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.solidDesign.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
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

@RunWith(MockitoJUnitRunner.class)
public class Part09Step02ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 2;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part09Step02Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
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
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals(PART_NUMBER, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals(0, instance.getTotalSteps());
    }

    @Test
    public void testHappyPathNoFailures() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc3 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, ON, dtc3);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        var nack = AcknowledgmentPacket.create(1, NACK);

        var dm12_1 = DM12MILOnEmissionDTCPacket.create(0x17, ON, ON, ON, ON);
        when(communicationsModule.requestDM12(any())).thenReturn(new RequestResult<>(false,
                                                                                     List.of(dm12_0, dm12_1),
                                                                                     List.of(nack)));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals(dm12_0, dataRepository.getObdModule(0).getLatest(DM12MILOnEmissionDTCPacket.class));
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoResponses() {

        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.2.2.a - No OBD ECU reported one or more active MIL on DTCs");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.2.2.b - No OBD ECUs reported MIL commanded on");
    }

    @Test
    public void testFailureForNoMILOn() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc1), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc2 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.2.2.b - No OBD ECUs reported MIL commanded on");
    }

    @Test
    public void testFailureForNoDTCs() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.2.2.a - No OBD ECU reported one or more active MIL on DTCs");
    }

    @Test
    public void testFailureForDifferentDTCs() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.2.2.c - Engine #1 (0) reported different active MIL on DTC(s) than what it reported in part 8 DM 12 response");
    }

    @Test
    public void testWarningForMoreThanOneDTC() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm12 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.9.2.3.a - Engine #1 (0) reported > 1 active DTC");
    }

    @Test
    public void testWarningForMoreThanOneDTCModule() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        var dtc2 = DiagnosticTroubleCode.create(456, 12, 0, 1);
        obdModuleInformation1.set(DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation1);

        var dm12_0 = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1);
        var dm12_1 = DM12MILOnEmissionDTCPacket.create(1, ON, OFF, OFF, OFF, dtc2);
        when(communicationsModule.requestDM12(any())).thenReturn(RequestResult.of(dm12_0, dm12_1));

        runTest();

        verify(communicationsModule).requestDM12(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.9.2.3.b - More than one ECU reported an active DTC");
    }

}
