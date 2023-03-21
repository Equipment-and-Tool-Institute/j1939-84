/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.create;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part01.Part01Step12Controller;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
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
    private CommunicationsModule communicationsModule;

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
                                              communicationsModule,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
    }

    @Test
    public void testFailureForMissingTestResult() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 12288, 20480, 4096);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 5319, 3, 287, 12288, 20480, 4096);
        obdModule0.setScaledTestResults(List.of(testResult1, testResult2));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1), 1);

        dataRepository.putObdModule(obdModule0);

        when(communicationsModule.requestTestResults(any(), eq(0), eq(247), eq(5319), eq(31)))
                                                                                              .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                     0,
                                                                                                                                                     testResult1)));
        when(communicationsModule.requestDM58(any(),
                                              eq(0x00),
                                              eq(spn1.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(0x00, NACK)));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(5319), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(spn1.getSpn()));

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

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1), 1);

        dataRepository.putObdModule(obdModule0);

        DM30ScaledTestResultsPacket dm30_1 = DM30ScaledTestResultsPacket.create(0, 0, testResult1);
        DM30ScaledTestResultsPacket dm30_2 = DM30ScaledTestResultsPacket.create(0, 0, testResult2);
        when(communicationsModule.requestTestResults(any(), eq(0), eq(247), eq(spn1.getSpn()), eq(31)))
                                                                                                       .thenReturn(List.of(dm30_1,
                                                                                                                           dm30_2));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false,
                                                                                             create(0x00, NACK)));
        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn1.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(spn1.getSpn()));

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
    public void testFailure2ten7a() {

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(getDm24SPNSupportPacket(0x00), 1);
        obd0x00.setScaledTestResults(List.of(testResult4));

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

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
                                              eq(27))).thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(mockListener).addOutcome(eq(2),
                                        eq(10),
                                        eq(FAIL),
                                        eq("6.2.10.7.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27"));

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(27),
                                                        eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(FAIL,
                                                  "6.2.10.7.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for SPN 27");
        assertEquals(List.of(outcome), listener.getOutcomes());
    }

    @Test
    public void testHappyPathFailure2ten5b() {

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.set(getDm24SPNSupportPacket(0x00), 1);
        obd0x00.setScaledTestResults(List.of(testResult4));

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

        DataRepository.getInstance().putObdModule(obd0x00);

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, Optional.empty()));

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
                                              eq(spn4.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        runTest();

        verify(mockListener).addOutcome(eq(2),
                                        eq(10),
                                        eq(FAIL),
                                        eq("6.2.10.5.b. DM58 not received from Engine #1 (0) for SP SPN 91 - Accelerator Pedal 1 Position"));

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(27),
                                                        eq(31));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(FAIL,
                                                  "6.2.10.5.b. DM58 not received from Engine #1 (0) for SP SPN 91 - Accelerator Pedal 1 Position");
        assertEquals(List.of(outcome), listener.getOutcomes());
    }

    @Test
    public void testFailure2ten5c() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn1 = SupportedSPN.create(91,
                                                false,
                                                true,
                                                true,
                                                true,
                                                1);
        SupportedSPN spn2 = SupportedSPN.create(4145,
                                                false,
                                                true,
                                                true,
                                                true,
                                                4);
        SupportedSPN spn3 = SupportedSPN.create(3301,
                                                false,
                                                false,
                                                true,
                                                true,
                                                2);
        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

        obdModule0.setScaledTestResults(List.of(testResult4));
        obdModule0.set(getDm24SPNSupportPacket(0x00), 1);// DM24SPNSupportPacket.create(source, spn1, spn2, spn3), 1);
        dataRepository.putObdModule(obdModule0);

        var dm58PacketSpn1 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn1.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false, dm58PacketSpn1));

        var dm58PacketSpn2 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn2.getSpn(),
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xF0 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn2));

        var dm58PacketSpn3 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn3.getSpn(),
                                                               new int[] { 0xFF, 0xFA, 0x00, 0x00 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn3.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn3));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn4.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        runTest();
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(spn4.getSpn()),
                                                        eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn1.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn2.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn3.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn4.getSpn()));

        verify(mockListener).addOutcome(eq(2),
                                        eq(10),
                                        eq(FAIL),
                                        eq("6.2.10.5.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start"));
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(FAIL,
                                                  "6.2.10.5.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start");
        assertEquals(List.of(outcome), listener.getOutcomes());
    }

    @Test
    public void testNoFailures() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn1 = SupportedSPN.create(91,
                                                false,
                                                true,
                                                true,
                                                true,
                                                1);
        SupportedSPN spn2 = SupportedSPN.create(4145,
                                                false,
                                                true,
                                                true,
                                                true,
                                                4);
        SupportedSPN spn3 = SupportedSPN.create(3301,
                                                false,
                                                false,
                                                true,
                                                true,
                                                2);
        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

        obdModule0.setScaledTestResults(List.of(testResult4));
        obdModule0.set(getDm24SPNSupportPacket(0x00), 1);
        dataRepository.putObdModule(obdModule0);

        var dm58PacketSpn1 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn1.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false, dm58PacketSpn1));

        var dm58PacketSpn2 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn2.getSpn(),
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xF0 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn2));

        var dm58PacketSpn3 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn3.getSpn(),
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn3.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn3));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn4.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        runTest();
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(spn4.getSpn()),
                                                        eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn1.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn2.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn3.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn4.getSpn()));

        // verify(mockListener).addOutcome(eq(2), eq(10), eq(FAIL), eq(""));
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testFailure2ten6a() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        int source = 0x00;

        SupportedSPN spn1 = SupportedSPN.create(91,
                                                false,
                                                true,
                                                true,
                                                true,
                                                1);
        SupportedSPN spn2 = SupportedSPN.create(4145,
                                                false,
                                                true,
                                                true,
                                                true,
                                                4);
        SupportedSPN spn3 = SupportedSPN.create(3301,
                                                false,
                                                false,
                                                true,
                                                true,
                                                2);

        obdModule0.set(DM24SPNSupportPacket.create(source, spn1, spn2, spn3), 1);
        dataRepository.putObdModule(obdModule0);

        var dm58PacketSpn1 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn1.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false, dm58PacketSpn1));

        var dm58PacketSpn2 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn2.getSpn(),
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xF0 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn2));

        var dm58PacketSpn3 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn3.getSpn(),
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn3.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn3));

        runTest();
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn1.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn2.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn3.getSpn()));

        assertEquals("", listener.getMessages());
        String expectedResults = "6.2.10.6.a - No SPs found that do NOT indicate support for DM58 in the DM24 response from Engine #1 (0)"
                + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testFailure2ten5d() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn1 = SupportedSPN.create(91,
                                                false,
                                                true,
                                                true,
                                                true,
                                                1);
        SupportedSPN spn2 = SupportedSPN.create(4145,
                                                false,
                                                true,
                                                true,
                                                true,
                                                4);
        SupportedSPN spn3 = SupportedSPN.create(3301,
                                                false,
                                                false,
                                                true,
                                                true,
                                                2);
        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

        obdModule0.setScaledTestResults(List.of(testResult4));
        obdModule0.set(getDm24SPNSupportPacket(0x00), 1);
        dataRepository.putObdModule(obdModule0);

        var dm58PacketSpn1 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn1.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false, dm58PacketSpn1));

        var dm58PacketSpn2 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn2.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn2));

        var dm58PacketSpn3 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn3.getSpn(),
                                                               new int[] { 0xFF, 0xF0, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn3.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn3));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn4.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        runTest();
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(spn4.getSpn()),
                                                        eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn1.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn2.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn3.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn4.getSpn()));

        verify(mockListener).addOutcome(eq(2),
                                        eq(10),
                                        eq(FAIL),
                                        eq("6.2.10.5.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 4145 - System Cumulative Continuous MI Time"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(FAIL,
                                                  "6.2.10.5.d - Data returned is greater than 0xFB... threshold from Engine #1 (0) for SPN 4145 - System Cumulative Continuous MI Time");
        assertEquals(List.of(outcome), listener.getOutcomes());
    }

    @Test
    public void testFailure2ten5a() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        int source = 0x00;

        final List<ScaledTestResult> dmScaledTestResults = getDmScaledTestResults();

        ScaledTestResult testResult4 = dmScaledTestResults.get(3);

        SupportedSPN spn1 = SupportedSPN.create(91,
                                                false,
                                                true,
                                                true,
                                                true,
                                                1);
        SupportedSPN spn2 = SupportedSPN.create(4145,
                                                false,
                                                true,
                                                true,
                                                true,
                                                4);
        SupportedSPN spn3 = SupportedSPN.create(3301,
                                                false,
                                                false,
                                                true,
                                                true,
                                                2);
        SupportedSPN spn4 = SupportedSPN.create(27,
                                                true,
                                                true,
                                                false,
                                                false,
                                                1);

        when(communicationsModule.requestTestResults(any(), eq(source), eq(247), eq(spn4.getSpn()), eq(31)))
                                                                                                            .thenReturn(List.of(DM30ScaledTestResultsPacket.create(source,
                                                                                                                                                                   0,
                                                                                                                                                                   testResult4)));

        obdModule0.setScaledTestResults(List.of(testResult4));
        obdModule0.set(getDm24SPNSupportPacket(0x00), 1);
        dataRepository.putObdModule(obdModule0);

        var dm58PacketSpn1 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn1.getSpn(),
                                                               new int[] { 0xF0, 0xFF, 0xFF, 0xFF });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn1.getSpn())))
                                                                 .thenReturn(new BusResult<>(false, dm58PacketSpn1));

        var dm58PacketSpn2 = DM58RationalityFaultSpData.create(source,
                                                               245,
                                                               spn2.getSpn(),
                                                               new int[] { 0xFF, 0xFF, 0xFF, 0xF0 });
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false, dm58PacketSpn2));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn3.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(spn4.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(source, NACK)));

        runTest();
        verify(communicationsModule).requestTestResults(any(CommunicationsListener.class),
                                                        eq(source),
                                                        eq(247),
                                                        eq(spn4.getSpn()),
                                                        eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn1.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn2.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn3.getSpn()));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(spn4.getSpn()));

        verify(mockListener).addOutcome(eq(2),
                                        eq(10),
                                        eq(FAIL),
                                        eq("6.2.10.5.a - NACK received for DM7 PG from OBD ECU from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(FAIL,
                                                  "6.2.10.5.a - NACK received for DM7 PG from OBD ECU from Engine #1 (0) for SP SPN 3301 - Time Since Engine Start");
        assertEquals(List.of(outcome), listener.getOutcomes());
    }

    @Test
    public void testNoOBDModules() {

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testWarningForInitializedValues() {

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);

        ScaledTestResult testResult1 = ScaledTestResult.create(247, 5319, 2, 287, 0, 0, 0);
        ScaledTestResult testResult2 = ScaledTestResult.create(247, 987, 2, 287, 0x0000, 0x0000, 0x0000);
        obdModule0.setScaledTestResults(List.of(testResult1, testResult2));

        SupportedSPN spn1 = SupportedSPN.create(5319, true, false, false, false, 1);
        SupportedSPN spn2 = SupportedSPN.create(987, true, false, false, false, 1);
        obdModule0.set(DM24SPNSupportPacket.create(0, spn1, spn2), 1);
        dataRepository.putObdModule(obdModule0);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(spn1.getSpn()),
                                                     eq(31))).thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                    0,
                                                                                                                    testResult1)));
        when(communicationsModule.requestTestResults(any(), eq(0), eq(247), eq(spn2.getSpn()), eq(31)))
                                                                                                       .thenReturn(List.of(DM30ScaledTestResultsPacket.create(0,
                                                                                                                                                              0,
                                                                                                                                                              testResult2)));

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              eq(spn2.getSpn()))).thenReturn(new BusResult<>(false,
                                                                                             create(0x00, NACK)));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn1.getSpn()), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(spn2.getSpn()), eq(31));

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(spn2.getSpn()));

        verify(mockListener).addOutcome(PART,
                                        STEP,
                                        WARN,
                                        "6.2.10.3.a - All test results from Engine #1 (0) are still initialized");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome outcome = new ActionOutcome(WARN,
                                                  "6.2.10.3.a - All test results from Engine #1 (0) are still initialized");
        assertEquals(List.of(outcome), listener.getOutcomes());
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

    private List<ScaledTestResult> getDmScaledTestResults() {

        ScaledTestResult str91 = ScaledTestResult.create(247, 91, 14, 0, 5, 10, 1);
        ScaledTestResult str4145 = ScaledTestResult.create(247, 4145, 3, 0, 5, 10, 1);
        ScaledTestResult str3301 = ScaledTestResult.create(247, 3301, 14, 0, 5, 10, 1);
        ScaledTestResult str27 = ScaledTestResult.create(247, 27, 3, 0, 5, 10, 1);

        return List.of(str91, str4145, str3301, str27);
    }

}
