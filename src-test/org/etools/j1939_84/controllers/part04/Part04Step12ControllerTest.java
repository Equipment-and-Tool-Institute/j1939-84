/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

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

import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import net.soliddesign.j1939tools.j1939.packets.ScaledTestResult;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part04Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 12;

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

        instance = new Part04Step12Controller(executor,
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
        // Module 0 responses first time
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        ScaledTestResult str0 = ScaledTestResult.create(247, 157, 8, 129, 100, 0, 1000);
        obdModuleInformation0.setScaledTestResults(List.of(str0));
        SupportedSPN supportedSPN0 = SupportedSPN.create(157, true, true, true, false, 1);
        obdModuleInformation0.setSupportedSPNs(List.of(supportedSPN0));
        dataRepository.putObdModule(obdModuleInformation0);
        var dm30_0 = DM30ScaledTestResultsPacket.create(0, 0, str0);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(246), eq(5846), eq(31))).thenReturn(List.of(
                                                                                                                     dm30_0));

        // Module 1 doesn't support TID 246 - requires another request
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        ScaledTestResult str1 = ScaledTestResult.create(247, 159, 8, 129, 0, 0, 0);
        obdModuleInformation1.setScaledTestResults(List.of(str1));
        SupportedSPN supportedSPN1 = SupportedSPN.create(159, true, true, true, false, 1);
        obdModuleInformation1.setSupportedSPNs(List.of(supportedSPN1));
        dataRepository.putObdModule(obdModuleInformation1);

        when(communicationsModule.requestTestResults(any(), eq(1), eq(246), eq(5846), eq(31))).thenReturn(List.of());
        var dm30_1 = DM30ScaledTestResultsPacket.create(1, 0, str1);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(1),
                                                     eq(247),
                                                     eq(supportedSPN1.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30_1));

        // Module 2 doesn't have Scaled Test Results
        dataRepository.putObdModule(new OBDModuleInformation(2));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(1), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(1), eq(247), eq(supportedSPN1.getSpn()), eq(31));

        // Verify non-initialized tests are stored
        List<ScaledTestResult> nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(1, nonInitializedTests.size());
        assertEquals(str0.getSpn(), nonInitializedTests.get(0).getSpn());
        assertEquals(str0.getFmi(), nonInitializedTests.get(0).getFmi());

        assertEquals(List.of(), dataRepository.getObdModule(1).getNonInitializedTests());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForDifference() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        ScaledTestResult str0 = ScaledTestResult.create(247, 157, 8, 129, 0, 0, 0);
        obdModuleInformation0.setScaledTestResults(List.of(str0));
        SupportedSPN supportedSPN0 = SupportedSPN.create(157, true, true, true, false, 1);
        obdModuleInformation0.setSupportedSPNs(List.of(supportedSPN0));
        dataRepository.putObdModule(obdModuleInformation0);

        ScaledTestResult str1 = ScaledTestResult.create(247, 159, 8, 129, 0, 0, 0);
        var dm30_0 = DM30ScaledTestResultsPacket.create(0, 0, str0, str1);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(246), eq(5846), eq(31))).thenReturn(List.of(
                                                                                                                     dm30_0));

        // Module 1 doesn't support TID 246 - requires another request
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        ScaledTestResult str11 = ScaledTestResult.create(247, 159, 8, 129, 0, 0, 0);
        obdModuleInformation1.setScaledTestResults(List.of(str1));
        SupportedSPN supportedSPN1 = SupportedSPN.create(159, true, true, true, false, 1);
        obdModuleInformation1.setSupportedSPNs(List.of(supportedSPN1));
        dataRepository.putObdModule(obdModuleInformation1);

        when(communicationsModule.requestTestResults(any(), eq(1), eq(246), eq(5846), eq(31))).thenReturn(List.of());
        ScaledTestResult str12 = ScaledTestResult.create(247, 200, 8, 129, 0, 0, 0);
        var dm30_1 = DM30ScaledTestResultsPacket.create(1, 0, str11, str12);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(1),
                                                     eq(247),
                                                     eq(supportedSPN1.getSpn()),
                                                     eq(31)))
                                                                .thenReturn(List.of(dm30_1));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(1), eq(246), eq(5846), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(1), eq(247), eq(supportedSPN1.getSpn()), eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.12.2.a - Engine #1 (0) reported a difference in test result labels from the test results received in part 1");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.4.12.2.a - Engine #2 (1) reported a difference in test result labels from the test results received in part 1");
    }

}
