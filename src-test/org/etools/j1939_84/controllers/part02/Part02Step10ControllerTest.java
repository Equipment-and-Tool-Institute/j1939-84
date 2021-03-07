/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part01.Part01Step12Controller;
import org.etools.j1939_84.model.OBDModuleInformation;
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
 * The unit test for {@link Part01Step12Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step10ControllerTest extends AbstractControllerTest {
    private static final int PART = 2;
    private static final int STEP = 10;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step10Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        dataRepository = DataRepository.newInstance();
        DateTimeModule.setInstance(null);

        instance = new Part02Step10Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              dataRepository,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    @Test
    public void testFailureForMissingTestResult() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 12288, 20480, 4096);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 5319, 3, 287, 12288, 20480, 4096);
        obdModule0.setScaledTestResults(List.of(testResult1, testResult2));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1), 1);

        dataRepository.putObdModule(obdModule0);

        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(5319), eq(31)))
                                                                                                 .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                        testResult1)));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(5319), eq(31));
        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.10.2.a - Engine #1 (0) provided different test result labels from the test results received in part 1 test 12");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailureForNewTestResult() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 12288, 20480, 4096);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 5319, 3, 287, 0, 0, 0);
        obdModule0.setScaledTestResults(List.of(testResult1));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1), 1);

        dataRepository.putObdModule(obdModule0);

        DM30ScaledTestResultsPacket dm30_1 = DM30ScaledTestResultsPacket.create(0, testResult1);
        DM30ScaledTestResultsPacket dm30_2 = DM30ScaledTestResultsPacket.create(0, testResult2);
        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(spn1.getSpn()), eq(31)))
                                                                                                          .thenReturn(List.of(dm30_1,
                                                                                                                              dm30_2));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn1.getSpn()), eq(31));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        FAIL,
                                        "6.2.10.2.a - Engine #1 (0) provided different test result labels from the test results received in part 1 test 12");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Step 10", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 10, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testNoFailures() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 12288, 20480, 4096);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 987, 2, 287, 12288, 20480, 4096);
        obdModule0.setScaledTestResults(List.of(testResult1, testResult2));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(987, true, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1, spn2), 1);
        dataRepository.putObdModule(obdModule0);

        OBDModuleInformation obdModule3 = new OBDModuleInformation(3);
        dataRepository.putObdModule(obdModule3);

        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(spn1.getSpn()), eq(31)))
                                                                                                          .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                                 testResult1)));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(spn2.getSpn()), eq(31)))
                                                                                                          .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                                 testResult2)));

        runTest();
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn1.getSpn()), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn2.getSpn()), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoOBDModules() {

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testWarningForInitializedValues() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 0, 0, 0);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 987, 2, 287, 0xFB00, 0xFFFF, 0xFFFF);
        obdModule0.setScaledTestResults(List.of(testResult1, testResult2));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(987, true, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1, spn2), 1);
        dataRepository.putObdModule(obdModule0);

        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(spn1.getSpn()), eq(31)))
                                                                                                          .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                                 testResult1)));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0), eq(247), eq(spn2.getSpn()), eq(31)))
                                                                                                          .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                                 testResult2)));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn1.getSpn()), eq(31));
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn2.getSpn()), eq(31));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        WARN,
                                        "6.2.10.3.a - All test results from Engine #1 (0) are still initialized");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
