/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part04;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.DM30ScaledTestResultsPacket;
import net.solidDesign.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
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
public class Part04Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 4;
    private static final int STEP_NUMBER = 14;

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

        instance = new Part04Step14Controller(executor,
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
        ScaledTestResult str1 = ScaledTestResult.create(247, 123, 12, 6, 100, 0, 250);
        ScaledTestResult str2 = ScaledTestResult.create(247, 456, 9, 4, 9, 12, 3);
        obdModuleInformation.setNonInitializedTests(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var dm30_123_12 = DM30ScaledTestResultsPacket.create(0, 0, str1);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(250), eq(123), eq(12)))
                                                                                                .thenReturn(List.of(dm30_123_12));

        var dm30_456_9 = DM30ScaledTestResultsPacket.create(0, 0, str2);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(250), eq(456), eq(9)))
                                                                                               .thenReturn(List.of(dm30_456_9));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(123), eq(12));
        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailureForInitializedTests() {

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.setNonInitializedTests(List.of(ScaledTestResult.create(247, 123, 12, 6, 100, 0, 250),
                                                            ScaledTestResult.create(247, 456, 9, 4, 9, 12, 3)));
        dataRepository.putObdModule(obdModuleInformation);

        var str_123_12 = ScaledTestResult.create(247, 123, 12, 6, 0, 0, 0);
        var dm30_123_12 = DM30ScaledTestResultsPacket.create(0, 0, str_123_12);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(250), eq(123), eq(12)))
                                                                                                .thenReturn(List.of(dm30_123_12));

        var str_456_9 = ScaledTestResult.create(247, 456, 9, 4, 0xFB00, 0xFFFF, 0xFFFF);
        var dm30_456_9 = DM30ScaledTestResultsPacket.create(0, 0, str_456_9);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(250), eq(456), eq(9)))
                                                                                               .thenReturn(List.of(dm30_456_9));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(123), eq(12));
        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        Outcome.FAIL,
                                        "6.4.14.2.a - Engine #1 (0) is now reporting an initialize test for SPN = 123, FMI = 12");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        Outcome.FAIL,
                                        "6.4.14.2.a - Engine #1 (0) is now reporting an initialize test for SPN = 456, FMI = 9");
    }

}
