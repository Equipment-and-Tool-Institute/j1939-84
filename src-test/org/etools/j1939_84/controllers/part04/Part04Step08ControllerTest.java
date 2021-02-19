/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.OFF;
import static org.etools.j1939_84.bus.j1939.packets.LampStatus.ON;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
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
public class Part04Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 8;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

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

        instance = new Part04Step08Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule);

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
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(executor,
                                 bannerModule,
                                 engineSpeedModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
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
        //Module 0 provides a response
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(createDM12(0, 231, 12, ON));
        dataRepository.putObdModule(obdModuleInformation0);

        var dm6_0 = createDM6(0, 0, 0, ON);
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6_0));

        //Module 1 provides a NACK
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        dataRepository.putObdModule(obdModuleInformation1);
        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM6(any(), eq(1))).thenReturn(new RequestResult<>(false, nack));

        //Module 2 is not an OBD Module
        var dm6_2 = createDM6(2, 0, 0, ON);

        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6_0, dm6_2));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));
        verify(diagnosticMessageModule).requestDM6(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDTC() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(createDM12(0, 231, 12, ON));
        dataRepository.putObdModule(obdModuleInformation0);

        var dm6 = createDM6(0, 231, 12, ON);
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6));

        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.8.2.a - Engine #1 (0) reported a pending DTC");
    }

    @Test
    public void testFailureDifferentMILStatus() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(createDM12(0, 231, 12, OFF));
        dataRepository.putObdModule(obdModuleInformation0);

        var dm6 = createDM6(0, 0, 0, ON);
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6));

        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.8.2.b - Engine #1 (0) reported a different MIL status that it did for DM12 response earlier in this part");
    }

    @Test
    public void testFailureForNoOBDResponse() {
        var dm6 = createDM6(2, 0, 0, ON);

        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.8.2.c - No OBD ECU provided a DM6 response");
    }

    @Test
    public void testFailureForDifferenceBetweenGlobalAndDS() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(createDM12(0, 231, 12, ON));
        dataRepository.putObdModule(obdModuleInformation0);

        var dm6_0 = createDM6(0, 0, 0, ON);
        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6_0));

        var dm6_1 = createDM6(0, 0, 0, OFF);
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6_1));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.8.4.a - Difference compared to data received during global request from Engine #1 (0)");
    }

    @Test
    public void testFailureForNoNACK() {
        //Module 0 provides a response
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(createDM12(0, 231, 12, ON));
        dataRepository.putObdModule(obdModuleInformation0);

        var dm6_0 = createDM6(0, 0, 0, ON);
        when(diagnosticMessageModule.requestDM6(any(), eq(0))).thenReturn(new RequestResult<>(false, dm6_0));

        //Module 1 doesn't provide a NACK
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        dataRepository.putObdModule(obdModuleInformation1);
        when(diagnosticMessageModule.requestDM6(any(), eq(1))).thenReturn(new RequestResult<>(true));

        when(diagnosticMessageModule.requestDM6(any())).thenReturn(new RequestResult<>(false, dm6_0));

        runTest();

        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM6(any(), eq(0));
        verify(diagnosticMessageModule).requestDM6(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.8.4.b - OBD module Engine #2 (1) did not provide a response to Global query and did not provide a NACK for the DS query");
    }

    @SuppressWarnings("SameParameterValue")
    private static DM6PendingEmissionDTCPacket createDM6(int sourceAddress, int spn, int fmi, LampStatus milStatus) {
        var dtc = DiagnosticTroubleCode.create(spn, fmi, 0, 5);
        return DM6PendingEmissionDTCPacket.create(sourceAddress, milStatus, OFF, OFF, OFF, dtc);
    }

    @SuppressWarnings("SameParameterValue")
    private static DM12MILOnEmissionDTCPacket createDM12(int sourceAddress, int spn, int fmi, LampStatus milStatus) {
        var dtc = DiagnosticTroubleCode.create(spn, fmi, 0, 5);
        return DM12MILOnEmissionDTCPacket.create(sourceAddress, milStatus, OFF, OFF, OFF, dtc);
    }
}
