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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
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
public class Part09Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 9;
    private static final int STEP_NUMBER = 10;

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

        instance = new Part09Step10Controller(executor,
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

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(dm30_123));

        var str456 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456, str456);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.of(dm30_456));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testTid250Nack() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        ScaledTestResult str3 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2, str3));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        var str234 = ScaledTestResult.create(250, 234, 9, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123, str234);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(AcknowledgmentPacket.create(0, Response.NACK)));
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(247),
                                                    eq(123),
                                                    eq(31))).thenReturn(BusResult.of(dm30_123));

        var str456 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456, str456);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.of(dm30_456));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testTid247Nack() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        ScaledTestResult str3 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2, str3));
        dataRepository.putObdModule(obdModuleInformation);

        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(AcknowledgmentPacket.create(0, Response.NACK)));
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(247),
                                                    eq(123),
                                                    eq(31))).thenReturn(BusResult.of(AcknowledgmentPacket.create(0, Response.NACK)));

        var str456 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456, str456);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.of(dm30_456));

        dataRepository.putObdModule(new OBDModuleInformation(1));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(247), eq(123), eq(31));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.c - No response for address 0 SPN 123 TID 250 and TID 247 queries");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.b - Engine #1 (0) reported different SPN+FMI combinations for tests results compared to the combinations in part 1");

    }

    @Test
    public void testFailureForNonInitialized() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(dm30_123));

        var str456 = ScaledTestResult.create(250, 456, 9, 0, 5, 10, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.of(dm30_456));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

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

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 5, 10, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(dm30_123));

        var str456_1 = ScaledTestResult.create(250, 456, 9, 0, 0, 0, 0);
        var str456_2 = ScaledTestResult.create(250, 456, 1, 0, 0, 0, 0);
        var dm30_456 = DM30ScaledTestResultsPacket.create(0, 0, str456_1, str456_2);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.of(dm30_456));

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

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

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 5, 10, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
        var dm30_123 = DM30ScaledTestResultsPacket.create(0, 0, str123);
        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(123),
                                                    eq(14))).thenReturn(BusResult.of(dm30_123));

        when(communicationsModule.requestTestResult(any(),
                                                    eq(0),
                                                    eq(250),
                                                    eq(456),
                                                    eq(9))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(123), eq(14));
        verify(communicationsModule).requestTestResult(any(), eq(0), eq(250), eq(456), eq(9));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.b - Engine #1 (0) reported different SPN+FMI combinations for tests results compared to the combinations in part 1");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.9.10.2.c - No response for address 0 SPN 456 TID 250 and TID 247 queries");
    }

}
