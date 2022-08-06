/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.create;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part12Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
    private static final int STEP_NUMBER = 11;

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

        instance = new Part12Step11Controller(executor,
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
    public void testNackFromObd() {

        int source = 0x00;

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(getDm24SPNSupportPacket(0x00), 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, create(0x00, NACK)));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(0x00, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.11.3.a - NACK received for DM7 PG from OBD ECU from Engine #1 (0) for SP SPN 91 - Accelerator Pedal Position 1"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedOutcome = new ActionOutcome(FAIL,
                                                          "6.12.11.3.a - NACK received for DM7 PG from OBD ECU from Engine #1 (0) for SP SPN 91 - Accelerator Pedal Position 1");
        assertEquals(List.of(expectedOutcome), listener.getOutcomes());
    }

    @Test
    public void testHappyPathNoFailures2() {

        int source = 0x00;

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(getDm24SPNSupportPacket(0x00), 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(0x00, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNonRationalitySupportFailure() {
        int source = 0x00;
        var dm24 = DM24SPNSupportPacket.create(source,
                                               SupportedSPN.create(91,
                                                                   false,
                                                                   true,
                                                                   true,
                                                                   true,
                                                                   1),
                                               SupportedSPN.create(4145,
                                                                   false,
                                                                   true,
                                                                   true,
                                                                   true,
                                                                   4),
                                               SupportedSPN.create(3301,
                                                                   false,
                                                                   false,
                                                                   true,
                                                                   true,
                                                                   2));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.setSupportedSPNs(dm24.getSupportedSpns());
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(BusResult.empty());

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(BusResult.of(dm58Packet3301));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.11.3.b. DM58 not received from Engine #1 (0) for SP SPN 4145 - System Cumulative Continuous MI Time"));

        assertEquals("", listener.getMessages());
        assertEquals("6.12.11.4.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from Engine #1 (0)" + J1939_84.NL,
                     listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.11.3.b. DM58 not received from Engine #1 (0) for SP SPN 4145 - System Cumulative Continuous MI Time");
        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testHappyPathNoFailures() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);

        ScaledTestResult str1 = ScaledTestResult.create(250, 123, 14, 0, 1, 0, 0);
        ScaledTestResult str2 = ScaledTestResult.create(250, 456, 9, 0, 1, 0, 0);
        ScaledTestResult str3 = ScaledTestResult.create(250, 456, 9, 0, 1, 0, 0);
        obdModuleInformation.setNonInitializedTests(List.of(str1, str2, str3));
        dataRepository.putObdModule(obdModuleInformation);

        var str123 = ScaledTestResult.create(250, 123, 14, 0, 0, 0, 0);
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

        assertEquals("", listener.getMessages());
        String expectedResults = "6.12.11.4.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from Engine #1 (0)" + J1939_84.NL;
        expectedResults += "6.12.11.4.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from Engine #2 (1)"+J1939_84.NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testObdNoResponseFailure() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xBD, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testDataLengthThreeFailure() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = DM24SPNSupportPacket.create(source,
                                                                      SupportedSPN.create(27,
                                                                                          true,
                                                                                          true,
                                                                                          false,
                                                                                          false,
                                                                                          1),
                                                                      SupportedSPN.create(8205,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          true,
                                                                                          3),
                                                                      SupportedSPN.create(4145,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          true,
                                                                                          4),
                                                                      SupportedSPN.create(3301,
                                                                                          false,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          2));
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet8205 = DM58RationalityFaultSpData.create(source,
                                                              245,
                                                              8205,
                                                               new int[] { 0xFB, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(8205))).thenReturn(new BusResult<>(false, dm58Packet8205));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(8205));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthFour() {

        final int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(0x00);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        DM58RationalityFaultSpData dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                                                    245,
                                                                                    91,
                                                                                    new int[] { 0xFA, 0xFF, 0xFF,
                                                                                            0xFF });

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        DM58RationalityFaultSpData dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                                                      245,
                                                                                      4145,
                                                                                      new int[] { 0xFA, 0xFF, 0xFD,
                                                                                              0x88 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFC, 0xFF, 0xFF });

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));
        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthTwo() {

        final int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFC, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule, atLeastOnce()).requestDM58(any(CommunicationsListener.class),
                                                                eq(source),
                                                                eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthTwoEdge() {

        final int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0x00, 0xFC, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule, atLeastOnce()).requestDM58(any(CommunicationsListener.class),
                                                                eq(source),
                                                                eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 3301 - Time Since Engine Start");
        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthOne() {

        final int source = 0x00;
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.setSupportedSPNs(getDm24SPNSupportPacket(0x00).getSupportedSpns());
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xFC, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        var dm58Packet27 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             27,
                                                             new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(0x00, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 91 - Accelerator Pedal Position 1"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 91 - Accelerator Pedal Position 1");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthZero() {

        final int source = 0x00;
        DM24SPNSupportPacket dm24Packet = DM24SPNSupportPacket.create(source,
                                                                      SupportedSPN.create(27,
                                                                                          true,
                                                                                          true,
                                                                                          false,
                                                                                          false,
                                                                                          1),
                                                                      SupportedSPN.create(91,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          true,
                                                                                          1),
                                                                      SupportedSPN.create(271,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          true,
                                                                                          0),
                                                                      SupportedSPN.create(3301,
                                                                                          false,
                                                                                          false,
                                                                                          true,
                                                                                          true,
                                                                                          2));
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xFB, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet271 = DM58RationalityFaultSpData.create(source,
                                                              245,
                                                              271,
                                                              new int[] { 0xFF, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(271))).thenReturn(new BusResult<>(false, dm58Packet271));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(271));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        assertEquals("", listener.getMessages());
        String expectedResults = "Not checking for FF - SP SPN   271, Unknown: Not Available length is 0" + J1939_84.NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    private List<ScaledTestResult> getDmScaledTestResults() {

        ScaledTestResult str91 = ScaledTestResult.create(247, 91, 14, 0, 5, 10, 1);
        ScaledTestResult str4145 = ScaledTestResult.create(247, 4145, 3, 0, 5, 10, 1);
        ScaledTestResult str3301 = ScaledTestResult.create(247, 3301, 14, 0, 5, 10, 1);
        ScaledTestResult str27 = ScaledTestResult.create(247, 27, 3, 0, 5, 10, 1);

        return List.of(str91, str4145, str3301, str27);
    }

    private DM24SPNSupportPacket getDm24SPNSupportPacket(int source) {

        return DM24SPNSupportPacket.create(source,
                                           SupportedSPN.create(27,
                                                               true,
                                                               true,
                                                               false,
                                                               false,
                                                               1),
                                           SupportedSPN.create(91,
                                                               false,
                                                               true,
                                                               true,
                                                               true,
                                                               1),
                                           SupportedSPN.create(4145,
                                                               false,
                                                               true,
                                                               true,
                                                               true,
                                                               4),
                                           SupportedSPN.create(3301,
                                                               false,
                                                               false,
                                                               true,
                                                               true,
                                                               2));
    }

    @Test
    public void testObdResponseIsBusyFailure() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xFA, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, BUSY)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testUnusedBytesNotFF() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xFA, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xDF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.12.3.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdResponseIsEmptyFailure() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0x01, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        var dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               4145,
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xFA });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(4145))).thenReturn(new BusResult<>(false, dm58Packet4145));

        var dm58Packet3301 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               3301,
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(3301))).thenReturn(new BusResult<>(false, dm58Packet3301));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(27))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.11.5.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

}
