/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part12;

import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.create;
import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.BUSY;
import static net.soliddesign.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.CommunicationsListener;
import net.soliddesign.j1939tools.bus.BusResult;
import net.soliddesign.j1939tools.bus.Packet;
import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM24SPNSupportPacket;
import net.soliddesign.j1939tools.j1939.packets.DM58RationalityFaultSpData;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part12Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 12;
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

        instance = new Part12Step12Controller(executor,
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

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(0x00);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        obd0x00.setSupportedSPNs(dm24Packet.getSupportedSpns());
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
                                                               new int[] { 0xFF, 0xFA, 0xFF, 0xFF });
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

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
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
                                        eq("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for spn 27"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for spn 27");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
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
                                                                                    new int[] { 0xFA, 0xFF, 0xFF, 0xFF });

        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(source),
                                              eq(91))).thenReturn(new BusResult<>(false, dm58Packet91));

        DM58RationalityFaultSpData dm58Packet4145 = DM58RationalityFaultSpData.create(source,
                                                                                     245,
                                                                                     4145,
                                                                                     new int[] { 0xFA, 0xFF, 0xFD, 0x88 });
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
                                         eq("6.12.12.d - Data returned is greater than 0xFB... threshold"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.d - Data returned is greater than 0xFB... threshold");

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
        verify(communicationsModule, atLeastOnce()).requestDM58(any(CommunicationsListener.class), eq(source), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.d - Data returned is greater than 0xFB... threshold"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.d - Data returned is greater than 0xFB... threshold");

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
        verify(communicationsModule, atLeastOnce()).requestDM58(any(CommunicationsListener.class), eq(source), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0)"));
        verify(mockListener,
               atLeastOnce()).addOutcome(eq(PART_NUMBER),
                                         eq(STEP_NUMBER),
                                         eq(FAIL),
                                         eq("6.12.12.d - Data returned is greater than 0xFB... threshold"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0)");
        ActionOutcome expectedActionOutcome2 = new ActionOutcome(FAIL,
                                                                "6.12.12.d - Data returned is greater than 0xFB... threshold");


        assertEquals(List.of(expectedActionOutcome, expectedActionOutcome2), listener.getOutcomes());
    }

    @Test
    public void testObdDataGreaterFbLengthOne() {

        final int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
        DataRepository.getInstance().putObdModule(obd0x00);

        var dm58Packet91 = DM58RationalityFaultSpData.create(source,
                                                             245,
                                                             91,
                                                             new int[] { 0xFD, 0xFF, 0xFF, 0xFF });
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
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(source), eq(27));

         verify(mockListener).addOutcome(eq(PART_NUMBER),
         eq(STEP_NUMBER),
         eq(FAIL),
         eq("6.12.12.d - Data returned is greater than 0xFB... threshold"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.d - Data returned is greater than 0xFB... threshold");

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
        assertEquals("", listener.getResults());

        assertEquals(List.of(), listener.getOutcomes());
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
                                        eq("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) 27 returned a(n) Busy"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) 27 returned a(n) Busy");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testAllBytesAreFfFailure() {

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
                                        eq("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) 27 returned a(n) Busy"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) 27 returned a(n) Busy");

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
                                        eq("6.12.12.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.c - Unused bytes in DM58 are not padded with FFh in the response from Engine #1 (0)");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdDataAllFfLegthFour() {

        int source = 0x00;
        DM24SPNSupportPacket dm24Packet = getDm24SPNSupportPacket(source);
        OBDModuleInformation obd0x00 = new OBDModuleInformation(source);
        obd0x00.set(dm24Packet, 1);
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
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.12.d - DM58 not received (after allowed retries)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.d - DM58 not received (after allowed retries)");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testObdResponseNackRecievedFromObdFailure() {

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
                                              eq(27))).thenReturn(new BusResult<>(false, create(source, NACK)));

        runTest();

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(91));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(4145));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(3301));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), eq(27));

        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.12.a - NACK received for DM7 PG from OBD ECU Engine #1 (0)"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.a - NACK received for DM7 PG from OBD ECU Engine #1 (0)");

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
                                        eq("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for spn 27"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) for spn 27");

        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    @Test
    public void testSpnsEmptyFailure() {

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0x00);
        DataRepository.getInstance().putObdModule(obd0x00);

        DM58RationalityFaultSpData dm58Packet = new DM58RationalityFaultSpData(Packet.create(64587,
                                                                                             0x00,
                                                                                             0xFB,
                                                                                             0,
                                                                                             0,
                                                                                             0,
                                                                                             0xA5,
                                                                                             0xA5,
                                                                                             0,
                                                                                             0));

        runTest();

        verify(mockListener).addOutcome(eq(PART_NUMBER), eq(STEP_NUMBER), eq(WARN), eq("6.12.12.3 - No modules reported supported SPs"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response - using first SP found"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(WARN),
                                        eq("6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response && supports test result - using first SP found"));
        verify(mockListener).addOutcome(eq(PART_NUMBER),
                                        eq(STEP_NUMBER),
                                        eq(FAIL),
                                        eq("6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) has no recorded SPs"));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        ActionOutcome expectedActionOutcome1 = new ActionOutcome(WARN,
                                                                 "6.12.12.3 - No modules reported supported SPs");
        ActionOutcome expectedActionOutcome2 = new ActionOutcome(WARN,
                                                                 "6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response - using first SP found");
        ActionOutcome expectedActionOutcome3 = new ActionOutcome(WARN,
                                                                 "6.12.12.3.a - No SPs found that do NOT indicate support for DM58 in the DM24 response && supports test result - using first SP found");
        ActionOutcome expectedActionOutcome4 = new ActionOutcome(FAIL,
                                                                 "6.12.12.3.a - NACK not received for DM7 PG from OBD ECU Engine #1 (0) has no recorded SPs");

        assertEquals(List.of(expectedActionOutcome1,
                             expectedActionOutcome2,
                             expectedActionOutcome3,
                             expectedActionOutcome4),
                     listener.getOutcomes());
    }

}
