/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part07;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.ACK;
import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import net.soliddesign.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import net.soliddesign.j1939tools.j1939.packets.ScaledTestResult;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

;

@RunWith(MockitoJUnitRunner.class)
public class Part07Step15ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 7;
    private static final int STEP_NUMBER = 15;

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

        instance = new Part07Step15Controller(executor,
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
    public void testHappyPathNoFailuresWithAllResults() {
        // Module responds to all test results request
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        var supportedSPN2 = SupportedSPN.create(456, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        // Not Initialized
        var scaledTestResult1 = ScaledTestResult.create(247, 123, 31, 4, 100, 1000, 0);
        // Initialized
        var scaledTestResult2 = ScaledTestResult.create(247, 456, 31, 4, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(scaledTestResult1, scaledTestResult2));
        dataRepository.putObdModule(obdModuleInformation);

        var dm30 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult1, scaledTestResult2);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(246),
                                                    eq(5846),
                                                    eq(31))).thenReturn(BusResult.of(dm30));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));

        List<ScaledTestResult> nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(1, nonInitializedTests.size());
        assertEquals(123, nonInitializedTests.get(0).getSpn());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testHappyPathNoFailuresWithIndividualResults() {
        // Module will not respond to all test results request, but will respond to individual test requests
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        var supportedSPN2 = SupportedSPN.create(456, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        // Not Initialized
        var scaledTestResult1 = ScaledTestResult.create(247, 123, 31, 4, 100, 1000, 0);
        // Initialized
        var scaledTestResult2 = ScaledTestResult.create(247, 456, 31, 4, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(scaledTestResult1, scaledTestResult2));
        dataRepository.putObdModule(obdModuleInformation);

        when(communicationsModule.requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31)))
                                                                                                .thenReturn(BusResult.empty());

        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult1);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(123), eq(31)))
                                                                                               .thenReturn(BusResult.of(dm30_123));

        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult2);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(456), eq(31)))
                                                                                               .thenReturn(BusResult.of(dm30_456));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(456), eq(31));

        List<ScaledTestResult> nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(1, nonInitializedTests.size());
        assertEquals(123, nonInitializedTests.get(0).getSpn());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testHappyPathNoFailuresWithNoResults() {
        // Module will not respond to all tests results request, and will NACK individual test requests
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        var supportedSPN2 = SupportedSPN.create(456, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        dataRepository.putObdModule(obdModuleInformation);

        when(communicationsModule.requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31)))
                                                                                                .thenReturn(BusResult.empty());

        var ack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(123), eq(31)))
                                                                                               .thenReturn(BusResult.of(ack));
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(456), eq(31)))
                                                                                               .thenReturn(BusResult.of(ack));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(456), eq(31));

        List<ScaledTestResult> nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(0, nonInitializedTests.size());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDifferentTestResultsWithAllResults() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        var supportedSPN2 = SupportedSPN.create(456, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        // Not Initialized
        var scaledTestResult1 = ScaledTestResult.create(247, 123, 31, 4, 100, 1000, 0);
        // Initialized
        var scaledTestResult2 = ScaledTestResult.create(247, 456, 31, 4, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(scaledTestResult1, scaledTestResult2));
        dataRepository.putObdModule(obdModuleInformation);

        var scaledTestResult3 = ScaledTestResult.create(247, 9634, 31, 4, 0, 0, 0);
        var dm30 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult1, scaledTestResult2, scaledTestResult3);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(246),
                                                    eq(5846),
                                                    eq(31))).thenReturn(BusResult.of(dm30));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.15.2.a - Difference in tests results reported from Engine #1 (0) compared to list created in part 1");
    }

    @Test
    public void testFailureForDifferentTestResultsWithIndividualResults() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        var supportedSPN2 = SupportedSPN.create(456, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        // Not Initialized
        var scaledTestResult1 = ScaledTestResult.create(247, 123, 31, 4, 100, 1000, 0);
        // Initialized
        var scaledTestResult2 = ScaledTestResult.create(247, 456, 31, 4, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(scaledTestResult1, scaledTestResult2));
        dataRepository.putObdModule(obdModuleInformation);

        when(communicationsModule.requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31)))
                                                                                                .thenReturn(BusResult.empty());

        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult1);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(123), eq(31)))
                                                                                               .thenReturn(BusResult.of(dm30_123));

        var ack = AcknowledgmentPacket.create(0, ACK);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(456), eq(31)))
                                                                                               .thenReturn(BusResult.of(ack));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(456), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.15.2.a - Difference in tests results reported from Engine #1 (0) compared to list created in part 1");
    }

    @Test
    public void testFailureForNACK() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        var supportedSPN1 = SupportedSPN.create(123, true, true, true, 1);
        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1));

        dataRepository.putObdModule(obdModuleInformation);

        var nack = AcknowledgmentPacket.create(0, NACK);
        when(communicationsModule.requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31)))
                                                                                                .thenReturn(BusResult.of(nack));

        when(communicationsModule.requestTestResult(any(), eq(0), eq(247), eq(123), eq(31)))
                                                                                               .thenReturn(BusResult.of(nack));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.7.15.2.b - NACK received from Engine #1 (0) which did not support an SPN (123) listed in its DM24 response");
    }

}
