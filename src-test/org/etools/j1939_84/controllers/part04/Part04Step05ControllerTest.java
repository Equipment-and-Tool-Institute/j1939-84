/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.FAST_FLASH;
import static net.soliddesign.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket;
import net.soliddesign.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import net.soliddesign.j1939tools.j1939.packets.DiagnosticTroubleCode;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part04Step05ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
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

        instance = new Part04Step05Controller(executor,
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
    public void testNoFailures() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNoNack() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(obdModule1);

        var dm2 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);

        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm2));
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(true));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testFailureForDTC() {
        var dtc = DiagnosticTroubleCode.create(123, 12, 0, 1);

        var obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 4);
        dataRepository.putObdModule(obdModule0);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.a - OBD ECU Engine #1 (0) reported > 0 previously active DTC");
    }

    @Test
    public void testNackNotReceived() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        dataRepository.putObdModule(obdModule0);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM12MILOnEmissionDTCPacket.create(1,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        dataRepository.putObdModule(obdModule1);

        var dm23 = DM23PreviouslyMILOnEmissionDTCPacket.create(0, FAST_FLASH, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, dm23));

        var dm23Ack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestDM23(any(), eq(1))).thenReturn(new BusResult<>(false, dm23Ack));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));
        verify(communicationsModule).requestDM23(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.b - OBD ECU Engine #1 (0) reported a MIL status different from the DM12 response earlier in this part");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - OBD ECU Engine #2 (1) did not provide a NACK for the DS query");
    }

    @Test
    public void testNoModuleResponse() {
        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM12MILOnEmissionDTCPacket.create(0,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         OFF,
                                                         DiagnosticTroubleCode.create(123, 12, 0, 1)),
                       4);
        dataRepository.putObdModule(obdModule0);
        when(communicationsModule.requestDM23(any(), eq(0))).thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(communicationsModule).requestDM23(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.c - OBD ECU Engine #1 (0) did not provide a NACK for the DS query");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.5.2.d - No OBD ECU provided a DM23");
    }

}
