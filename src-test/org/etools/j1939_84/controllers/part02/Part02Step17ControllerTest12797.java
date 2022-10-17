/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.controllers.ResultsListener.MessageType.ERROR;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.model.FuelType.DSL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.etools.j1939_84.controllers.BroadcastValidator;
import org.etools.j1939_84.controllers.BusService;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TableA1Validator;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part01.Part01Step26Controller;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.Bus;
import org.etools.j1939tools.bus.BusException;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.etools.j1939tools.modules.NOxBinningModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part02Step17ControllerTest12797 extends AbstractControllerTest {

    @Mock
    private Executor executor;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Mock
    private CommunicationsModule communicationsModule;

    private DataRepository dataRepository;

    @Mock
    private TableA1Validator tableA1Validator;

    @Mock
    private BroadcastValidator broadcastValidator;

    @Mock
    private BusService busService;

    private Part02Step17Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    private GhgTrackingModule ghgTrackingModule;

    private NOxBinningModule nOxBinningModule;

    private static final int BUS_ADDR = 0xA5;

    private static List<SupportedSPN> spns(int... ids) {
        return Arrays.stream(ids).mapToObj(id -> {
            return SupportedSPN.create(id, false, true, false, false, 1);
        }).collect(Collectors.toList());
    }

    private static GenericPacket packet(int spnId, Boolean isNotAvailable, int sourceAddress) {
        GenericPacket mock = mock(GenericPacket.class);

        Spn spn = mock(Spn.class);
        when(spn.getId()).thenReturn(spnId);
        when(mock.getSourceAddress()).thenReturn(sourceAddress);
        if (isNotAvailable != null) {
            when(spn.isNotAvailable()).thenReturn(isNotAvailable);
        }
        when(mock.getSpns()).thenReturn(List.of(spn));

        return mock;
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(new TestDateTimeModule());
        J1939DaRepository j1939DaRepository = J1939DaRepository.getInstance();
        dataRepository = DataRepository.newInstance();
        ghgTrackingModule = new GhgTrackingModule(DateTimeModule.getInstance());
        nOxBinningModule = new NOxBinningModule((DateTimeModule.getInstance()));

        instance = new Part02Step17Controller(executor,
                                              bannerModule,
                                              DateTimeModule.getInstance(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              tableA1Validator,
                                              j1939DaRepository,
                                              broadcastValidator,
                                              busService,
                                              ghgTrackingModule,
                                              nOxBinningModule);
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
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 tableA1Validator,
                                 broadcastValidator,
                                 busService,
                                 mockListener);
    }

    @Test
    public void testRunWithPgnVerification() {
        // SPNs
        // 111 - Broadcast with value
        // 222 - Not Broadcast not found
        // 333 - Broadcast found with N/A
        // 444 - DS with value
        // 555 - DS No Response
        // 666 - DS found with n/a
        // 222 - Global Request with value
        // 555 - Global Request no response
        // 666 - Global Request with n/a
        List<Integer> supportedSpns = Arrays.asList(111, 222, 333, 444, 555, 666, 777, 888, 999);

        OBDModuleInformation obdModule = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule);
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setVehicleModelYear(2025);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        DM24SPNSupportPacket obdDM24Packet = DM24SPNSupportPacket.create(0,
                                                                         supportedSpns.stream()
                                                                                 .map(num -> SupportedSPN.create(num,
                                                                                                                 true,
                                                                                                                 true,
                                                                                                                 true,
                                                                                                                 false,
                                                                                                                 1))
                                                                                 .toArray(SupportedSPN[]::new));
        obdModule.set(obdDM24Packet, 1);

        when(broadcastValidator.collectAndReportNotAvailableSPNs(any(),
                                                                 anyInt(),
                                                                 any(),
                                                                 eq(0),
                                                                 any(ResultsListener.class),
                                                                 eq(2),
                                                                 eq(17),
                                                                 eq("6.2.17.6.a")))
                .thenReturn(List.of("111"));

        OBDModuleInformation module1 = new OBDModuleInformation(2);
        DM24SPNSupportPacket dm24SPNSupportPacket = DM24SPNSupportPacket.create(1,
                                                                                SupportedSPN.create(111,
                                                                                                    false,
                                                                                                    true,
                                                                                                    false,
                                                                                                    false,
                                                                                                    1));
        module1.set(dm24SPNSupportPacket, 1);
        dataRepository.putObdModule(module1);

        when(busService.collectNonOnRequestPGNs(any())).thenReturn(List.of(11111, 22222, 33333));

        List<Integer> pgns = List.of(22222,
                                     44444,
                                     55555,
                                     66666);
        when(busService.getPGNsForDSRequest(any(), any())).thenReturn(pgns);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet2 = packet(222, false, 0);
        packets.add(packet2);
        when(busService.globalRequest(eq(22222), any())).thenReturn(Stream.of(packet2));

        List<GenericPacket> dsPackets = new ArrayList<>();
        GenericPacket packet4 = packet(444, false, 0);
        packets.add(packet4);
        dsPackets.add(packet4);
        when(busService.dsRequest(eq(44444), eq(0), any())).thenReturn(Stream.of(packet4));

        GenericPacket packet5 = packet(555, false, 0);
        packets.add(packet5);
        dsPackets.add(packet5);
        when(busService.dsRequest(eq(55555), eq(0), any())).thenReturn(Stream.of(packet5));

        GenericPacket packet6 = packet(666, true, 0);
        packets.add(packet6);
        dsPackets.add(packet6);
        when(busService.dsRequest(eq(66666), eq(0), any())).thenReturn(Stream.of(packet6));

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);
        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(List.of());
        //        verify(broadcastValidator).buildPGNPacketsMap(List.of(packet2));

        verify(broadcastValidator).reportBroadcastPeriod(eq(Map.of()),
                                                         eq(List.of(111,
                                                                    222,
                                                                    333,
                                                                    444,
                                                                    555,
                                                                    666,
                                                                    777,
                                                                    888,
                                                                    999,
                                                                    111)),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(supportedSpns),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(22222),
                                                                    eq(List.of()),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(44444),
                                                                    eq(List.of(packet4)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(44444),
                                                                    eq(List.of(packet4)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(55555),
                                                                    eq(List.of(packet5)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(66666),
                                                                    eq(List.of(packet6)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(2),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(22222),
                                                                    eq(List.of()),
                                                                    eq(2),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(44444),
                                                                    eq(List.of()),
                                                                    eq(2),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(55555),
                                                                    eq(List.of()),
                                                                    eq(2),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(66666),
                                                                    eq(List.of()),
                                                                    eq(2),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(2),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(supportedSpns),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111,
                                                                               222,
                                                                               333,
                                                                               444,
                                                                               555,
                                                                               666,
                                                                               777,
                                                                               888,
                                                                               999,
                                                                               111)),
                                                                    eq(22222),
                                                                    any(),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.6.a"));

        verify(busService, times(2)).collectNonOnRequestPGNs(any());
        verify(busService, times(2)).getPGNsForDSRequest(any(), any());
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        pgns.forEach(pgn -> {
            verify(busService).dsRequest(eq(pgn), eq(0), eq(""));
            verify(busService).dsRequest(eq(pgn), eq(2), any());
            verify(busService).globalRequest(eq(pgn), any());
        });
        verify(busService).readBus(eq(0),
                                   eq("6.2.17.2.c"));

        verify(tableA1Validator).reportExpectedMessages(any(ResultsListener.class));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()), any(ResultsListener.class), eq("6.2.17.3.e"));

        verify(tableA1Validator).reportNotAvailableSPNs(eq(packet2),
                                                        any(ResultsListener.class),
                                                        eq("6.2.17.5.b"));
        verify(tableA1Validator).reportImplausibleSPNValues(eq(packet2),
                                                            any(ResultsListener.class),
                                                            eq(true),
                                                            eq("6.2.17.6.b"));
        verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet2),
                                                                   any(ResultsListener.class),
                                                                   eq("6.2.17.6.e"));
        dsPackets.forEach(packet -> {
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(true),
                                                                eq("6.2.17.6.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.2.17.6.c"));
        });

        verify(tableA1Validator).reportDuplicateSPNs(eq(packets),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.d"));

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Turbocharger (2)" + NL;
        expectedMsg += "Test 2.17 - Verifying Turbocharger (2)" + NL;
        expectedMsg += "Test 2.17 - Verifying Turbocharger (2)" + NL;
        expectedMsg += "Test 2.17 - Verifying Turbocharger (2)";
        assertEquals(expectedMsg, listener.getMessages());
    }
    @Test
    public void testRunWithFailuresUnExpectedToolSaMsg() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehicleInformation);
        // SPNs
        // 111 - Broadcast with value
        // 222 - Not Broadcast not found
        // 333 - Broadcast found with N/A
        // 444 - DS with value
        // 555 - DS No Response
        // 666 - DS found with n/a
        // 222 - Global Request with value
        // 555 - Global Request no response
        // 666 - Global Request with n/a
        List<Integer> supportedSpns = Arrays.asList(111, 222, 333, 444, 555, 666, 777, 888, 999);
        List<SupportedSPN> supportedSPNList = spns(111, 222, 333, 444, 555, 666, 777, 888, 999);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   SupportedSPN.create(111,
                                                                       false,
                                                                       true,
                                                                       false,
                                                                       false,
                                                                       1)),
                       1);
        dataRepository.putObdModule(obdModule0);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        obdModule1.set(DM24SPNSupportPacket.create(1, supportedSPNList.toArray(new SupportedSPN[0])),
                       1);
        dataRepository.putObdModule(obdModule1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet1 = packet(111, false, 0);
        packets.add(packet1);
        GenericPacket packet3 = packet(333, true, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(12, "6.2.17.2.c")).thenReturn(packets.stream());

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        when(busService.collectNonOnRequestPGNs(supportedSpns))
                .thenReturn(List.of(11111, 22222, 33333));

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(true);
        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(eq(packets));
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         eq(supportedSpns),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        verify(broadcastValidator, atLeastOnce()).collectAndReportNotAvailableSPNs(eq(0x00),
                                                                                   eq(packets),
                                                                                   eq(supportedSpns.subList(1,
                                                                                                            supportedSpns.size())),
                                                                                   eq(List.of(11111, 22222, 33333)),
                                                                                   any(ResultsListener.class),
                                                                                   eq(2),
                                                                                   eq(17),
                                                                                   eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns);
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(2,
                                        17,
                                        WARN,
                                        "6.2.17 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible");
        verify(mockListener).onUrgentMessage(eq(
                                                     "6.2.17 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible"),
                                             eq("Second device using SA 0xF9"),
                                             eq(ERROR),
                                             any());
        verify(tableA1Validator).reportExpectedMessages(any(ResultsListener.class));
        packets.forEach(packet -> {
            verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                           any(ResultsListener.class),
                                                                           any());
            verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                               any(ResultsListener.class),
                                                                               eq(true),
                                                                               any());
            verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                                   any(ResultsListener.class),
                                                                                   any());
            verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                      any(ResultsListener.class),
                                                                                      any());
            verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
                                                                              any(ResultsListener.class),
                                                                              eq(false));
            verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());
        });
        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12797() {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = List.of(supportedSpn);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(supportedSpn,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   supportedSPN),
                       1);
        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
        obdModule0.set(response64241, 1);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64241));

        GenericPacket response64242 = new GenericPacket(Packet.create(0xFAF2,
                                                                      0x00,
                                                                      0xA0,
                                                                      0x8C,
                                                                      0x10,
                                                                      0x0E,
                                                                      0x68,
                                                                      0x5B,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        responses.add(response64242);
        obdModule0.set(response64242, 1);
        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64242));

        GenericPacket response64243 = new GenericPacket(Packet.create(0xFAF3,
                                                                      0x00,
                                                                      0x78,
                                                                      0x69,
                                                                      0x8C,
                                                                      0x0A,
                                                                      0x8E,
                                                                      0x44,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        responses.add(response64243);
        obdModule0.set(response64243, 1);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(packets);
        verify(broadcastValidator).reportBroadcastPeriod(eq(packetMap),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(2),
                                                                        eq(17),
                                                                        eq("6.2.17.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           any());
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               any());
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  any());
        verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
                                                                          any(ResultsListener.class),
                                                                          eq(false));
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |             |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |             |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |             |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |             |" + NL;
        expected += "| Prod: Prop system active,  s   |       4,500 |       6,000 |     392,580 |" + NL;
        expected += "| Prod: Prop idle active,    s   |         450 |         600 |     353,322 |" + NL;
        expected += "| Prod: Prop urban active,   s   |       2,925 |       3,900 |     255,177 |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting PSA Times Lifetime Hours (PSATL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Stored 100 Hours (PSATS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Active 100 Hours (PSATA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12797WarningTwentyA() throws BusException {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = List.of(supportedSpn);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(supportedSpn,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   supportedSPN),
                       1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64242 = new GenericPacket(Packet.create(0xFAF2,
                                                                      0x00,
                                                                      0xA0,
                                                                      0x8C,
                                                                      0x10,
                                                                      0x0E,
                                                                      0x68,
                                                                      0x5B,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        responses.add(response64242);
        obdModule0.set(response64242, 1);
        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64242));

        GenericPacket response64243 = new GenericPacket(Packet.create(0xFAF3,
                                                                      0x00,
                                                                      0x78,
                                                                      0x69,
                                                                      0x8C,
                                                                      0x0A,
                                                                      0x8E,
                                                                      0x44,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        responses.add(response64243);
        obdModule0.set(response64243, 1);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(packets);
        verify(broadcastValidator).reportBroadcastPeriod(eq(packetMap),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(2),
                                                                        eq(17),
                                                                        eq("6.2.17.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(eq(2), eq(17), eq(WARN), eq("6.2.17.20.a - No response was received from Engine #1 (0)"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           any());
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               any());
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  any());
        verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
                                                                          any(ResultsListener.class),
                                                                          eq(false));
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |             |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |             |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |             |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |             |" + NL;
        expected += "| Prod: Prop system active,  s   |       4,500 |       6,000 |             |" + NL;
        expected += "| Prod: Prop idle active,    s   |         450 |         600 |             |" + NL;
        expected += "| Prod: Prop urban active,   s   |       2,925 |       3,900 |             |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting PSA Times Lifetime Hours (PSATL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Stored 100 Hours (PSATS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Active 100 Hours (PSATA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12797FailureTwentyTwoA() {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = List.of(supportedSpn);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(supportedSpn,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   supportedSPN),
                       1);
        dataRepository.putObdModule(obdModule0);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
        obdModule0.set(response64241, 1);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64241));

        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(packets);
        verify(broadcastValidator).reportBroadcastPeriod(eq(packetMap),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(2),
                                                                        eq(17),
                                                                        eq("6.2.17.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());


        verify(mockListener).addOutcome(eq(2), eq(17), eq(FAIL), eq("6.2.17.22.a - No response was received from Engine #1 (0)"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           any());
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               any());
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  any());
        verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
                                                                          any(ResultsListener.class),
                                                                          eq(false));
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |             |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |             |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |             |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |             |" + NL;
        expected += "| Prod: Prop system active,  s   |             |             |     392,580 |" + NL;
        expected += "| Prod: Prop idle active,    s   |             |             |     353,322 |" + NL;
        expected += "| Prod: Prop urban active,   s   |             |             |     255,177 |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting PSA Times Lifetime Hours (PSATL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Stored 100 Hours (PSATS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Active 100 Hours (PSATA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12797WarningTwentyTwoB() throws BusException {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = List.of(supportedSpn);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2023);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(supportedSpn,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   supportedSPN),
                       1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
        obdModule0.set(response64241, 1);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64241));

        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(packets);
        verify(broadcastValidator).reportBroadcastPeriod(eq(packetMap),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(2),
                                                                        eq(17),
                                                                        eq("6.2.17.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(eq(2), eq(17), eq(WARN), eq("6.2.17.22.b - No response was received from Engine #1 (0)"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           any());
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               any());
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  any());
        verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
                                                                          any(ResultsListener.class),
                                                                          eq(false));
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |             |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |             |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |             |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |             |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |             |" + NL;
        expected += "| Prod: Prop system active,  s   |             |             |     392,580 |" + NL;
        expected += "| Prod: Prop idle active,    s   |             |             |     353,322 |" + NL;
        expected += "| Prod: Prop urban active,   s   |             |             |     255,177 |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting PSA Times Lifetime Hours (PSATL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Stored 100 Hours (PSATS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting PSA Times Active 100 Hours (PSATA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testUiInterruptionFailure() {
        // SPNs
        // 111 - Broadcast with value
        // 444 - DS with value
        List<Integer> supportedSpns = Arrays.asList(111, 444);
        List<SupportedSPN> supportedSPNList = spns(111, 444);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   SupportedSPN.create(111,
                                                                       false,
                                                                       true,
                                                                       false,
                                                                       false,
                                                                       1)),
                       1);
        dataRepository.putObdModule(obdModule0);
        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setVehicleModelYear(2025);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM24SPNSupportPacket.create(1,
                                                   supportedSPNList.toArray(new SupportedSPN[0])),
                       1);

        dataRepository.putObdModule(obdModule1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet1 = packet(111, false, 1);
        packets.add(packet1);

        when(busService.readBus(eq(12), eq("6.2.17.2.c"))).thenAnswer(a -> {
            instance.stop();
            return packets.stream();
        });

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);
        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(List.of(packet1));
        verify(broadcastValidator).reportBroadcastPeriod(eq(Map.of()),
                                                         eq(List.of(111, 111, 444)),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of(packet1)),
                                                                    eq(List.of(444)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(12), eq("6.2.17.2.c"));
        verify(busService, times(2)).collectNonOnRequestPGNs(eq(List.of(111, 111, 444)));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of(111)));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of(444)));

        verify(tableA1Validator).reportExpectedMessages(any(ResultsListener.class));
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet),
                                                            any(ResultsListener.class),
                                                            eq("6.2.17.3.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(true),
                                                                eq("6.2.17.3.c"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.2.17.3.d"));
            verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                         any(ResultsListener.class),
                                                         eq("6.2.17.3.e"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.3.f"));
            verify(tableA1Validator).reportPacketIfNotReported(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq(false));
        });

        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.d"));

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Step 17", instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", 2, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(17, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

}
