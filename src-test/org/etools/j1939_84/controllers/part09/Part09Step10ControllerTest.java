/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part09;

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
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
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
public class Part09Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 10;

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

        instance = new Part09Step10Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        SupportedSPN spn1 = SupportedSPN.create(123, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(456, true, false, false, 1);
        SupportedSPN spn3 = SupportedSPN.create(789, false, true, false, 1);
        obdModuleInformation.setSupportedSPNs(List.of(spn1, spn2, spn3));

        ScaledTestResult str1 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(247, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, str123);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(123),
                                                        eq(31))).thenReturn(List.of(dm30_123));

        var str456 = ScaledTestResult.create(247, 456, 9, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, str456);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(456),
                                                        eq(31))).thenReturn(List.of(dm30_456));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(123), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(456), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForNonInitialized() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        SupportedSPN spn1 = SupportedSPN.create(123, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(456, true, false, false, 1);
        SupportedSPN spn3 = SupportedSPN.create(789, false, true, false, 1);
        obdModuleInformation.setSupportedSPNs(List.of(spn1, spn2, spn3));

        ScaledTestResult str1 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(247, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, str123);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(123),
                                                        eq(31))).thenReturn(List.of(dm30_123));

        var str456 = ScaledTestResult.create(247, 456, 9, 0, 5, 10, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, str456);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(456),
                                                        eq(31))).thenReturn(List.of(dm30_456));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(123), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(456), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.a - Engine #1 (0) reported test result for SPN = 456, FMI = 9 is not initialized");
    }

    @Test
    public void testFailureForDifferentResults1() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        SupportedSPN spn1 = SupportedSPN.create(123, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(456, true, false, false, 1);
        SupportedSPN spn3 = SupportedSPN.create(789, false, true, false, 1);
        obdModuleInformation.setSupportedSPNs(List.of(spn1, spn2, spn3));

        ScaledTestResult str1 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(247, 456, 9, 0, 5, 10, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, str123);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(123),
                                                        eq(31))).thenReturn(List.of(dm30_123));

        var str456_1 = ScaledTestResult.create(247, 456, 9, 0, 0, 0, 0);
        var str456_2 = ScaledTestResult.create(247, 456, 1, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, str456_1, str456_2);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(456),
                                                        eq(31))).thenReturn(List.of(dm30_456));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(123), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(456), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.b - Engine #1 (0) reported different SPN+FMI combinations for tests results compared to the combinations in part 1");
    }

    @Test
    public void testFailureForDifferentResults2() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        SupportedSPN spn1 = SupportedSPN.create(123, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(456, true, false, false, 1);
        SupportedSPN spn3 = SupportedSPN.create(789, false, true, false, 1);
        obdModuleInformation.setSupportedSPNs(List.of(spn1, spn2, spn3));

        ScaledTestResult str1 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(247, 456, 9, 0, 5, 10, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(247, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, str123);
        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(123),
                                                        eq(31))).thenReturn(List.of(dm30_123));

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(456),
                                                        eq(31))).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(123), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(456), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.b - Engine #1 (0) reported different SPN+FMI combinations for tests results compared to the combinations in part 1");
    }

}
