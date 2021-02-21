/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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
public class Part03Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 12;

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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step12Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
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
        var spn = SupportedSPN.create(123, false, false, false, 0);

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, spn));
        dataRepository.putObdModule(moduleInfo);

        dataRepository.putObdModule(new OBDModuleInformation(1));

        var dm24 = DM24SPNSupportPacket.create(0, spn);
        when(diagnosticMessageModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, dm24));

        var nack = AcknowledgmentPacket.create(1, NACK);
        when(diagnosticMessageModule.requestDM24(any(), eq(1))).thenReturn(new BusResult<>(false, nack));

        runTest();

        verify(diagnosticMessageModule).requestDM24(any(), eq(0));
        verify(diagnosticMessageModule).requestDM24(any(), eq(1));

        assertEquals("", listener.getResults());

    }

    @Test
    public void testFailureForDifferenceSpnContent() {
        var spn1 = SupportedSPN.create(123, false, false, false, 0);
        var spn2 = SupportedSPN.create(123, true, true, true, 1);

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, spn1));
        dataRepository.putObdModule(moduleInfo);

        var dm24 = DM24SPNSupportPacket.create(0, spn2);
        when(diagnosticMessageModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, dm24));

        runTest();

        verify(diagnosticMessageModule).requestDM24(any(), eq(0));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.12.2.a - Message data received from Engine #1 (0) differs from that provided in part 6.1.4");
    }

    @Test
    public void testFailureForDifferentSPNs() {
        var spn1 = SupportedSPN.create(123, false, false, false, 0);
        var spn2 = SupportedSPN.create(456, false, false, false, 0);

        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, spn1));
        dataRepository.putObdModule(moduleInfo);

        var dm24 = DM24SPNSupportPacket.create(0, spn2);
        when(diagnosticMessageModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, dm24));

        runTest();

        verify(diagnosticMessageModule).requestDM24(any(), eq(0));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.12.2.a - Message data received from Engine #1 (0) differs from that provided in part 6.1.4");
    }

    @Test
    public void testFailureForNoNACK() {
        OBDModuleInformation moduleInfo = new OBDModuleInformation(0);
        dataRepository.putObdModule(moduleInfo);

        when(diagnosticMessageModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false));

        runTest();

        verify(diagnosticMessageModule).requestDM24(any(), eq(0));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.3.12.2.b - OBD module Engine #1 (0) did not provide a NACK for the DS query");
    }
}
