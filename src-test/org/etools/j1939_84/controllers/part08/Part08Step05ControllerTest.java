/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part08Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 5;

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

        instance = new Part08Step05Controller(executor,
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
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 8);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 2);
        obdModuleInformation0.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);
        var dm2_0 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF, dtc);

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        dataRepository.putObdModule(obdModuleInformation1);
        var dm2_1 = DM2PreviouslyActiveDTC.create(1, OFF, OFF, OFF, OFF);

        var dtc1 = DiagnosticTroubleCode.create(1289, 3, 0, 1);
        var dm2_2 = DM2PreviouslyActiveDTC.create(2, ON, OFF, OFF, OFF, dtc1);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2_0, dm2_1, dm2_2));

        runTest();

        verify(communicationsModule).requestDM2(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForDifferentDTC() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 8);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 2);
        obdModuleInformation0.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dtc1 = DiagnosticTroubleCode.create(1289, 3, 0, 1);
        var dm2 = DM2PreviouslyActiveDTC.create(0, ON, OFF, OFF, OFF, dtc1);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));

        runTest();

        verify(communicationsModule).requestDM2(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.5.2.a - Engine #1 (0) did not include all DTCs from its DM23 response in its DM2 response");
    }

    @Test
    public void testFailureForDifferentMIL() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 8);
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 2);
        obdModuleInformation0.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 8);
        dataRepository.putObdModule(obdModuleInformation0);

        var dm2 = DM2PreviouslyActiveDTC.create(0, FAST_FLASH, OFF, OFF, OFF, dtc);

        when(communicationsModule.requestDM2(any())).thenReturn(RequestResult.of(dm2));

        runTest();

        verify(communicationsModule).requestDM2(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.8.5.2.b - Engine #1 (0) reported different MIL status than DM12 response earlier in this part");
    }

}
