/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
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
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class Part12Step08ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 8;

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

        instance = new Part12Step08Controller(executor,
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

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        ScaledTestResult str3 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2, str3));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 1, 10, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(250),
                                                     eq(123),
                                                     eq(14))).thenReturn(List.of(dm30_123));

        var str456 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456, str456);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(250),
                                                     eq(456),
                                                     eq(9))).thenReturn(List.of(dm30_456));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResults(any(), eq(0), eq(250), eq(456), eq(9));

        Map<ScaledTestResult, Integer> nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(1, nonInitializedTests.size());
        ScaledTestResult scaledTestResult = nonInitializedTests.keySet().iterator().next();
        assertEquals(123, scaledTestResult.getSpn());
        assertEquals(14, scaledTestResult.getFmi());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }
}
