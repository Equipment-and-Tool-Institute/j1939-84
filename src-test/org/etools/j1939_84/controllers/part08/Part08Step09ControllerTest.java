/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.bus.j1939.packets.LampStatus.NOT_SUPPORTED;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.CommunicationsModule;
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
public class Part08Step09ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 9;

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

        instance = new Part08Step09Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(234, 5, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var lampStatus1 = DTCLampStatus.create(dtc1, OFF, ON, OFF, OFF);
        var lampStatus2 = DTCLampStatus.create(dtc2, OFF, OFF, OFF, OFF);
        var dm31_0 = DM31DtcToLampAssociation.create(0, 0, lampStatus1, lampStatus2);

        dataRepository.putObdModule(new OBDModuleInformation(1));
        var dm31_1 = DM31DtcToLampAssociation.create(1, 0);

        when(communicationsModule.requestDM31(any())).thenReturn(RequestResult.of(dm31_0, dm31_1));

        runTest();

        verify(communicationsModule).requestDM31(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoResponses() {
        when(communicationsModule.requestDM31(any())).thenReturn(RequestResult.empty());

        runTest();

        verify(communicationsModule).requestDM31(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForMoreOrLessDTCs() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(234, 5, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var lampStatus1 = DTCLampStatus.create(dtc1, OFF, ON, OFF, OFF);
        var lampStatus2 = DTCLampStatus.create(dtc2, OFF, OFF, OFF, OFF);
        var dtc3 = DiagnosticTroubleCode.create(203, 7, 0, 1);
        var lampStatus3 = DTCLampStatus.create(dtc3, OFF, OFF, OFF, OFF);
        var dm31 = DM31DtcToLampAssociation.create(0, 0, lampStatus1, lampStatus2, lampStatus3);

        when(communicationsModule.requestDM31(any())).thenReturn(RequestResult.of(dm31));

        runTest();

        verify(communicationsModule).requestDM31(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.9.2.b - Engine #1 (0) reported additional or fewer DTCs than those reported in DM12 and DM23 responses earlier in this part");
    }

    @Test
    public void testFailureNoDM12Match() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(234, 5, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var lampStatus1 = DTCLampStatus.create(dtc1, OFF, NOT_SUPPORTED, OFF, OFF);
        var lampStatus2 = DTCLampStatus.create(dtc2, OFF, OFF, OFF, OFF);
        var dm31 = DM31DtcToLampAssociation.create(0, 0, lampStatus1, lampStatus2);

        when(communicationsModule.requestDM31(any())).thenReturn(RequestResult.of(dm31));

        runTest();

        verify(communicationsModule).requestDM31(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.9.2.a - No ECU reported same DTC as MIL on as reported in DM12 earlier in this part");
    }

    @Test
    public void testFailureNoDM23Match() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var dtc1 = DiagnosticTroubleCode.create(123, 12, 0, 1);
        obdModuleInformation.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc1), 8);
        var dtc2 = DiagnosticTroubleCode.create(234, 5, 0, 1);
        obdModuleInformation.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc2), 8);
        dataRepository.putObdModule(obdModuleInformation);

        var lampStatus1 = DTCLampStatus.create(dtc1, OFF, ON, OFF, OFF);
        var lampStatus2 = DTCLampStatus.create(dtc2, OFF, NOT_SUPPORTED, OFF, OFF);
        var dm31 = DM31DtcToLampAssociation.create(0, 0, lampStatus1, lampStatus2);

        when(communicationsModule.requestDM31(any())).thenReturn(RequestResult.of(dm31));

        runTest();

        verify(communicationsModule).requestDM31(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.9.2.c - No ECU reported same DTC as MIL off as reported in DM23 earlier in this part");
    }

}
