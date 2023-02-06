/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part01;

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
public class Part01Step26ControllerTest12797 extends AbstractControllerTest {

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

    private Part01Step26Controller instance;

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

        instance = new Part01Step26Controller(executor,
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
                                                                 eq(1),
                                                                 eq(26),
                                                                 eq("6.1.26.6.a")))
                .thenReturn(List.of("111"));

        OBDModuleInformation module1 = new OBDModuleInformation(1);
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
                                                         eq(1),
                                                         eq(26));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(supportedSpns),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(22222),
                                                                    eq(List.of()),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(44444),
                                                                    eq(List.of(packet4)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(55555),
                                                                    eq(List.of(packet5)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(66666),
                                                                    eq(List.of(packet6)),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(22222),
                                                                    eq(List.of()),
                                                                    eq(1),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(44444),
                                                                    eq(List.of()),
                                                                    eq(1),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(55555),
                                                                    eq(List.of()),
                                                                    eq(1),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(66666),
                                                                    eq(List.of()),
                                                                    eq(1),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(supportedSpns),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(List.of(111, 222, 333, 444, 555, 666, 777, 888, 999, 111)),
                                                                    eq(22222),
                                                                    any(),
                                                                    eq(0),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.6.a"));

        verify(busService, times(2)).collectNonOnRequestPGNs(any());
        verify(busService, times(2)).getPGNsForDSRequest(any(), any());
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        pgns.forEach(pgn -> {
            verify(busService).dsRequest(eq(pgn), eq(0), eq(""));
            verify(busService).dsRequest(eq(pgn), eq(1), any());
            verify(busService).globalRequest(eq(pgn), any());
        });
        verify(busService).readBus(eq(0),
                                   eq("6.1.26.2.c"));

        verify(tableA1Validator).reportExpectedMessages(any(ResultsListener.class));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()), any(ResultsListener.class), eq("6.1.26.3.e"));

        verify(tableA1Validator).reportNotAvailableSPNs(eq(packet2),
                                                        any(ResultsListener.class),
                                                        eq("6.1.26.5.b"));
        verify(tableA1Validator).reportImplausibleSPNValues(eq(packet2),
                                                            any(ResultsListener.class),
                                                            eq(false),
                                                            eq("6.1.26.6.b"));
        verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet2),
                                                                any(ResultsListener.class),
                                                                eq("6.1.26.6.c"));
        verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet2),
                                                                   any(ResultsListener.class),
                                                                   eq("6.1.26.6.e"));
        dsPackets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet),
                                                            any(ResultsListener.class),
                                                            eq("6.1.26.5.a"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.1.26.6.e"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(false),
                                                                eq("6.1.26.6.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.1.26.6.f"));
        });

        verify(tableA1Validator).reportDuplicateSPNs(eq(packets),
                                                     any(ResultsListener.class),
                                                     eq("6.1.26.6.d"));

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "Test 1.26 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 1.26 - Verifying Engine #2 (1)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    private List<GenericPacket> getPackets() {
        List<Packet> packets = new ArrayList<>();

        packets.add(Packet.create(0xFB02, 0,
        // @formatter:off
                                  0x40, 0x84, 0x00, 0x10, 0x41, 0x84, 0x00, 0x10,
                                  0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                  0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                  0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                  0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                  0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                  0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                  0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                  0x5F, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB03, 0,
        // @formatter:off
                                  0x60, 0x84, 0x00, 0x10, 0x61, 0x84, 0x00, 0x10,
                                  0x63, 0x84, 0x00, 0x10, 0x65, 0x84, 0x00, 0x10,
                                  0x67, 0x84, 0x00, 0x10, 0x69, 0x84, 0x00, 0x10,
                                  0x6B, 0x84, 0x00, 0x10, 0x6D, 0x84, 0x00, 0x10,
                                  0x6F, 0x84, 0x00, 0x10, 0x71, 0x84, 0x00, 0x10,
                                  0x73, 0x84, 0x00, 0x10, 0x75, 0x84, 0x00, 0x10,
                                  0x77, 0x84, 0x00, 0x10, 0x79, 0x84, 0x00, 0x10,
                                  0x7B, 0x84, 0x00, 0x10, 0x7D, 0x84, 0x00, 0x10,
                                  0x7F, 0x84, 0x08, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB04, 0,
        // @formatter:off
                                  0x80, 0x84, 0x00, 0x10, 0x81, 0x84, 0x00, 0x10,
                                  0x83, 0x84, 0x00, 0x10, 0x85, 0x84, 0x00, 0x10,
                                  0x87, 0x84, 0x00, 0x10, 0x89, 0x84, 0x00, 0x10,
                                  0x8B, 0x84, 0x00, 0x10, 0x8D, 0x84, 0x00, 0x10,
                                  0x8F, 0x84, 0x00, 0x10, 0x91, 0x84, 0x00, 0x10,
                                  0x93, 0x84, 0x00, 0x10, 0x95, 0x84, 0x00, 0x10,
                                  0x97, 0x84, 0x00, 0x10, 0x99, 0x84, 0x00, 0x10,
                                  0x9B, 0x84, 0x00, 0x10, 0x9D, 0x84, 0x00, 0x10,
                                  0x9F, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB05, 0,
        // @formatter:off
                                  0xA0, 0x84, 0x00, 0x10, 0xA1, 0x84, 0x00, 0x10,
                                  0xA3, 0x84, 0x00, 0x10, 0xA5, 0x84, 0x00, 0x10,
                                  0xA7, 0x84, 0x00, 0x10, 0xA9, 0x84, 0x00, 0x10,
                                  0xAB, 0x84, 0x00, 0x10, 0xAD, 0x84, 0x00, 0x10,
                                  0xAF, 0x84, 0x00, 0x10, 0xB1, 0x84, 0x00, 0x10,
                                  0xB3, 0x84, 0x00, 0x10, 0xB5, 0x84, 0x00, 0x10,
                                  0xB7, 0x84, 0x00, 0x10, 0xB9, 0x84, 0x00, 0x10,
                                  0xBB, 0x84, 0x00, 0x10, 0xBD, 0x84, 0x00, 0x10,
                                  0xBF, 0x84, 0x00, 0x10));
        // @formatter:on

        packets.add(Packet.create(0xFB06, 0,
        // @formatter:off
                                  0x00, 0x04, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x0D,
                                  0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                  0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                  0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                  0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                  0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                  0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                  0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                  0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB07, 0,
        // @formatter:off
                                  0x20, 0x04, 0x00, 0x0D, 0x21, 0x04, 0x00, 0x0D,
                                  0x23, 0x04, 0x00, 0x0D, 0x25, 0x04, 0x00, 0x0D,
                                  0x27, 0x04, 0x00, 0x0D, 0x29, 0x04, 0x00, 0x0D,
                                  0x2B, 0x04, 0x00, 0x0D, 0x2D, 0x04, 0x00, 0x0D,
                                  0x2F, 0x04, 0x00, 0x0D, 0x31, 0x04, 0x00, 0x0D,
                                  0x33, 0x04, 0x00, 0x0D, 0x35, 0x04, 0x00, 0x0D,
                                  0x37, 0x04, 0x00, 0x0D, 0x39, 0x04, 0x00, 0x0D,
                                  0x3B, 0x04, 0x00, 0x0D, 0x3D, 0x04, 0x00, 0x0D,
                                  0x3F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB08, 0,
        // @formatter:off
                                  0x40, 0x04, 0x00, 0x0D, 0x41, 0x04, 0x00, 0x0D,
                                  0x43, 0x04, 0x00, 0x0D, 0x45, 0x04, 0x00, 0x0D,
                                  0x47, 0x04, 0x00, 0x0D, 0x49, 0x04, 0x00, 0x0D,
                                  0x4B, 0x04, 0x00, 0x0D, 0x4D, 0x04, 0x00, 0x0D,
                                  0x4F, 0x04, 0x00, 0x0D, 0x51, 0x04, 0x00, 0x0D,
                                  0x53, 0x04, 0x00, 0x0D, 0x55, 0x04, 0x00, 0x0D,
                                  0x57, 0x04, 0x00, 0x0D, 0x59, 0x04, 0x00, 0x0D,
                                  0x5B, 0x04, 0x00, 0x0D, 0x5D, 0x04, 0x00, 0x0D,
                                  0x5F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB09, 0,
        // @formatter:off
                                  0x60, 0x04, 0x00, 0x0D, 0x61, 0x04, 0x00, 0x0D,
                                  0x63, 0x04, 0x00, 0x0D, 0x65, 0x04, 0x00, 0x0D,
                                  0x67, 0x04, 0x00, 0x0D, 0x69, 0x04, 0x00, 0x0D,
                                  0x6B, 0x04, 0x00, 0x0D, 0x6D, 0x04, 0x00, 0x0D,
                                  0x6F, 0x04, 0x00, 0x0D, 0x71, 0x04, 0x00, 0x0D,
                                  0x73, 0x04, 0x00, 0x0D, 0x75, 0x04, 0x00, 0x0D,
                                  0x77, 0x04, 0x00, 0x0D, 0x79, 0x04, 0x00, 0x0D,
                                  0x7B, 0x04, 0x00, 0x0D, 0x7D, 0x04, 0x00, 0x0D,
                                  0x7F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0A, 0,
        // @formatter:off
                                  0x80, 0x04, 0x00, 0x0D, 0x81, 0x04, 0x00, 0x0D,
                                  0x83, 0x04, 0x00, 0x0D, 0x85, 0x04, 0x00, 0x0D,
                                  0x87, 0x04, 0x00, 0x0D, 0x89, 0x04, 0x00, 0x0D,
                                  0x8B, 0x04, 0x00, 0x0D, 0x8D, 0x04, 0x00, 0x0D,
                                  0x8F, 0x04, 0x00, 0x0D, 0x91, 0x04, 0x00, 0x0D,
                                  0x93, 0x04, 0x00, 0x0D, 0x95, 0x04, 0x00, 0x0D,
                                  0x97, 0x04, 0x00, 0x0D, 0x99, 0x04, 0x00, 0x0D,
                                  0x9B, 0x04, 0x00, 0x0D, 0x9D, 0x04, 0x00, 0x0D,
                                  0x9F, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0B, 0,
        // @formatter:off
                                  0xA0, 0x04, 0x00, 0x0D, 0xA1, 0x04, 0x00, 0x0D,
                                  0xA3, 0x04, 0x00, 0x0D, 0xA5, 0x04, 0x00, 0x0D,
                                  0xA7, 0x04, 0x00, 0x0D, 0xA9, 0x04, 0x00, 0x0D,
                                  0xAB, 0x04, 0x00, 0x0D, 0xAD, 0x04, 0x00, 0x0D,
                                  0xAF, 0x04, 0x00, 0x0D, 0xB1, 0x04, 0x00, 0x0D,
                                  0xB3, 0x04, 0x00, 0x0D, 0xB5, 0x04, 0x00, 0x0D,
                                  0xB7, 0x04, 0x00, 0x0D, 0xB9, 0x04, 0x00, 0x0D,
                                  0xBB, 0x04, 0x00, 0x0D, 0xBD, 0x04, 0x00, 0x0D,
                                  0xBF, 0x04, 0x00, 0x0D));
        // @formatter:on

        packets.add(Packet.create(0xFB0C, 0,
        // @formatter:off
                                  0x00, 0x84, 0x01, 0x84, 0x03, 0x84, 0x05, 0x84,
                                  0x07, 0x84, 0x09, 0x84, 0x0B, 0x84, 0x0D, 0x84,
                                  0x0F, 0x84, 0x11, 0x84, 0x13, 0x84, 0x15, 0x84,
                                  0x17, 0x84, 0x19, 0x84, 0x1B, 0x84, 0x1D, 0x84,
                                  0x1F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0D, 0,
        // @formatter:off
                                  0x20, 0x84, 0x21, 0x84, 0x23, 0x84, 0x25, 0x84,
                                  0x27, 0x84, 0x29, 0x84, 0x2B, 0x84, 0x2D, 0x84,
                                  0x2F, 0x84, 0x31, 0x84, 0x33, 0x84, 0x35, 0x84,
                                  0x37, 0x84, 0x39, 0x84, 0x3B, 0x84, 0x3D, 0x84,
                                  0x3F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0E, 0,
        // @formatter:off
                                  0x40, 0x84, 0x41, 0x84, 0x43, 0x84, 0x45, 0x84,
                                  0x47, 0x84, 0x49, 0x84, 0x4B, 0x84, 0x4D, 0x84,
                                  0x4F, 0x84, 0x51, 0x84, 0x53, 0x84, 0x55, 0x84,
                                  0x57, 0x84, 0x59, 0x84, 0x5B, 0x84, 0x5D, 0x84,
                                  0x5F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB0F, 0,
        // @formatter:off
                                  0x60, 0x84, 0x61, 0x84, 0x63, 0x84, 0x65, 0x84,
                                  0x67, 0x84, 0x69, 0x84, 0x6B, 0x84, 0x6D, 0x84,
                                  0x6F, 0x84, 0x71, 0x84, 0x73, 0x84, 0x75, 0x84,
                                  0x77, 0x84, 0x79, 0x84, 0x7B, 0x84, 0x7D, 0x84,
                                  0x7F, 0x84));
        // @formatter:on

        packets.add(Packet.create(0xFB10, 0,
        // @formatter:off
                                  0x80, 0x84, 0x00, 0x00, 0x81, 0x84, 0x00, 0x00,
                                  0x83, 0x84, 0x00, 0x00, 0x85, 0x84, 0x00, 0x00,
                                  0x87, 0x84, 0x00, 0x00, 0x89, 0x84, 0x00, 0x00,
                                  0x8B, 0x84, 0x00, 0x00, 0x8D, 0x84, 0x00, 0x00,
                                  0x8F, 0x84, 0x00, 0x00, 0x91, 0x84, 0x00, 0x00,
                                  0x93, 0x84, 0x00, 0x00, 0x95, 0x84, 0x00, 0x00,
                                  0x97, 0x84, 0x00, 0x00, 0x99, 0x84, 0x00, 0x00,
                                  0x9B, 0x84, 0x00, 0x00, 0x9D, 0x84, 0x00, 0x00,
                                  0x9F, 0x84, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB11, 0,
        // @formatter:off
                                  0xA0, 0x84, 0x00, 0x00, 0xA1, 0x84, 0x00, 0x00,
                                  0xA3, 0x84, 0x00, 0x00, 0xA5, 0x84, 0x00, 0x00,
                                  0xA7, 0x84, 0x00, 0x00, 0xA9, 0x84, 0x00, 0x00,
                                  0xAB, 0x84, 0x00, 0x00, 0xAD, 0x84, 0x00, 0x00,
                                  0xAF, 0x84, 0x00, 0x00, 0xB1, 0x84, 0x00, 0x00,
                                  0xB3, 0x84, 0x00, 0x00, 0xB5, 0x84, 0x00, 0x00,
                                  0xB7, 0x84, 0x00, 0x00, 0xB9, 0x84, 0x00, 0x00,
                                  0xBB, 0x84, 0x00, 0x00, 0xBD, 0x84, 0x00, 0x00,
                                  0xBF, 0x84, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB12, 0,
        // @formatter:off
                                  0x00, 0x04, 0x01, 0x04, 0x03, 0x04, 0x05, 0x04,
                                  0x07, 0x04, 0x09, 0x04, 0x0B, 0x04, 0x0D, 0x04,
                                  0x0F, 0x04, 0x11, 0x04, 0x13, 0x04, 0x15, 0x04,
                                  0x17, 0x04, 0x19, 0x04, 0x1B, 0x04, 0x1D, 0x04,
                                  0x1F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB13, 0,
        // @formatter:off
                                  0x20, 0x04, 0x21, 0x04, 0x23, 0x04, 0x25, 0x04,
                                  0x27, 0x04, 0x29, 0x04, 0x2B, 0x04, 0x2D, 0x04,
                                  0x2F, 0x04, 0x31, 0x04, 0x33, 0x04, 0x35, 0x04,
                                  0x37, 0x04, 0x39, 0x04, 0x3B, 0x04, 0x3D, 0x04,
                                  0x3F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB14, 0,
        // @formatter:off
                                  0x40, 0x04, 0x41, 0x04, 0x43, 0x04, 0x45, 0x04,
                                  0x47, 0x04, 0x49, 0x04, 0x4B, 0x04, 0x4D, 0x04,
                                  0x4F, 0x04, 0x51, 0x04, 0x53, 0x04, 0x55, 0x04,
                                  0x57, 0x04, 0x59, 0x04, 0x5B, 0x04, 0x5D, 0x04,
                                  0x5F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB15, 0,
        // @formatter:off
                                  0x60, 0x04, 0x61, 0x04, 0x63, 0x04, 0x65, 0x04,
                                  0x67, 0x04, 0x69, 0x04, 0x6B, 0x04, 0x6D, 0x04,
                                  0x6F, 0x04, 0x71, 0x04, 0x73, 0x04, 0x75, 0x04,
                                  0x77, 0x04, 0x79, 0x04, 0x7B, 0x04, 0x7D, 0x04,
                                  0x7F, 0x04));
        // @formatter:on

        packets.add(Packet.create(0xFB16, 0,
        // @formatter:off
                                  0x80, 0x04, 0x00, 0x00, 0x81, 0x04, 0x00, 0x00,
                                  0x83, 0x04, 0x00, 0x00, 0x85, 0x04, 0x00, 0x00,
                                  0x87, 0x04, 0x00, 0x00, 0x89, 0x04, 0x00, 0x00,
                                  0x8B, 0x04, 0x00, 0x00, 0x8D, 0x04, 0x00, 0x00,
                                  0x8F, 0x04, 0x00, 0x00, 0x91, 0x04, 0x00, 0x00,
                                  0x93, 0x04, 0x00, 0x00, 0x95, 0x04, 0x00, 0x00,
                                  0x97, 0x04, 0x00, 0x00, 0x99, 0x04, 0x00, 0x00,
                                  0x9B, 0x04, 0x00, 0x00, 0x9D, 0x04, 0x00, 0x00,
                                  0x9F, 0x04, 0x00, 0x00));
        // @formatter:on

        packets.add(Packet.create(0xFB17, 0,
        // @formatter:off
                                  0xA0, 0x04, 0x00, 0x00, 0xA1, 0x04, 0x00, 0x00,
                                  0xA3, 0x04, 0x00, 0x00, 0xA5, 0x04, 0x00, 0x00,
                                  0xA7, 0x04, 0x00, 0x00, 0xA9, 0x04, 0x00, 0x00,
                                  0xAB, 0x04, 0x00, 0x00, 0xAD, 0x04, 0x00, 0x00,
                                  0xAF, 0x04, 0x00, 0x00, 0xB1, 0x04, 0x00, 0x00,
                                  0xB3, 0x04, 0x00, 0x00, 0xB5, 0x04, 0x00, 0x00,
                                  0xB7, 0x04, 0x00, 0x00, 0xB9, 0x04, 0x00, 0x00,
                                  0xBB, 0x04, 0x00, 0x00, 0xBD, 0x04, 0x00, 0x00,
                                  0xBF, 0x04, 0x00, 0x00));
        // @formatter:on

        return packets.stream()
                      .map(GenericPacket::new)
                      .collect(Collectors.toList());
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
        when(busService.readBus(12, "6.1.26.2.c")).thenReturn(packets.stream());

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
                                                         eq(1),
                                                         eq(26));
        verify(broadcastValidator, atLeastOnce()).collectAndReportNotAvailableSPNs(eq(0x00),
                                                                                   eq(packets),
                                                                                   eq(supportedSpns.subList(1,
                                                                                                            supportedSpns.size())),
                                                                                   eq(List.of(11111, 22222, 33333)),
                                                                                   any(ResultsListener.class),
                                                                                   eq(1),
                                                                                   eq(26),
                                                                                   eq("6.1.26.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns);
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(1,
                                        26,
                                        WARN,
                                        "6.1.26 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible");
        verify(mockListener).onUrgentMessage(eq(
                                                     "6.1.26 - Unexpected Service Tool Message from SA 0xF9 observed. Test results uncertain. False failures are possible"),
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
                                                                               eq(false),
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
    public void testRunObdPgnSupports12797() throws BusException {
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
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = newGenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64241));

        GenericPacket response64242 = newGenericPacket(Packet.create(0xFAF2,
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
        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64242));

        GenericPacket response64243 = newGenericPacket(Packet.create(0xFAF3,
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
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));

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
                                                         eq(1),
                                                         eq(26));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(1),
                                                                        eq(26),
                                                                        eq("6.1.26.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(false),
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
        dataRepository.putObdModule(obdModule0);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64242 = newGenericPacket(Packet.create(0xFAF2,
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
        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64242));

        GenericPacket response64243 = newGenericPacket(Packet.create(0xFAF3,
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
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));

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
                                                         eq(1),
                                                         eq(26));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(1),
                                                                        eq(26),
                                                                        eq("6.1.26.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(eq(1), eq(26), eq(WARN), eq("6.1.26.20.a - No response was received from Engine #1 (0)for PG 64241"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(false),
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
    public void testRunObdPgnSupports12797FailureTwentyTwoA() throws BusException {
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
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = newGenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
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
                                                         eq(1),
                                                         eq(26));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(1),
                                                                        eq(26),
                                                                        eq("6.1.26.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());


        verify(mockListener).addOutcome(eq(1), eq(26), eq(FAIL), eq("6.1.26.22.a - No response was received from Engine #1 (0)"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(false),
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
        dataRepository.putObdModule(obdModule0);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = newGenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
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
                                                         eq(1),
                                                         eq(26));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        eq(Collections.emptyList()),
                                                                        eq(Collections.emptyList()),
                                                                        any(ResultsListener.class),
                                                                        eq(1),
                                                                        eq(26),
                                                                        eq("6.1.26.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(eq(1), eq(26), eq(WARN), eq("6.1.26.22.b - No response was received from Engine #1 (0)"));

        verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       any());
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(false),
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

        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenAnswer(a -> {
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
                                                         eq(1),
                                                         eq(26));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(List.of()),
                                                                    eq(List.of(111)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of(packet1)),
                                                                    eq(List.of(444)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(12), eq("6.1.26.2.c"));
        verify(busService, times(2)).collectNonOnRequestPGNs(eq(List.of(111, 111, 444)));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of(111)));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of(444)));

        verify(tableA1Validator).reportExpectedMessages(any(ResultsListener.class));
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet),
                                                            any(ResultsListener.class),
                                                            eq("6.1.26.3.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(false),
                                                                eq("6.1.26.3.c"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.1.26.3.d"));
            verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                         any(ResultsListener.class),
                                                         eq("6.1.26.3.e"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.1.26.3.f"));
            verify(tableA1Validator).reportPacketIfNotReported(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq(false));
        });

        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.1.26.6.d"));

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 26", instance.getDisplayName());
    }

    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", 1, instance.getPartNumber());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(26, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

}
