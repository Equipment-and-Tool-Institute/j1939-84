/*
 * Copyright (c) 2024. Equipment & Tool Institute
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CSERSModule;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.etools.j1939tools.modules.NOxBinningModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class Part02Step17ControllerTest22227 extends AbstractControllerTest {
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
        GhgTrackingModule ghgTrackingModule = new GhgTrackingModule(DateTimeModule.getInstance());
        NOxBinningModule nOxBinningModule = new NOxBinningModule((DateTimeModule.getInstance()));
        CSERSModule csersModule = new CSERSModule((DateTimeModule.getInstance()));

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
                                              nOxBinningModule,
                                              csersModule);
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
    public void testRunObdPgnSupports22227() {
        final int supportedSpn = 22227;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSPN = SupportedSPN.create(supportedSpn,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64257, 1);
        when(communicationsModule.request(eq(64257),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64255, 1);
        when(communicationsModule.request(eq(64255),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64256, 1);
        when(communicationsModule.request(eq(64256),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

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
        verify(busService).collectNonOnRequestPGNs(eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(mockListener).addOutcome(eq(2),
                                        eq(17),
                                        eq(WARN),
                                        eq("6.2.17.16.a - No response was received from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(2),
                                        eq(17),
                                        eq(FAIL),
                                        eq("6.2.17.18.e - Number of active labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(2),
                                        eq(17),
                                        eq(FAIL),
                                        eq("6.2.17.18.e - Number of stored labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(2),
                                        eq(17),
                                        eq(FAIL),
                                        eq("6.2.17.18.f - Active labels received is not a subset of lifetime labels"));
        verify(mockListener).addOutcome(eq(2),
                                        eq(17),
                                        eq(FAIL),
                                        eq("6.2.17.18.f - Stored labels received is not a subset of lifetime labels"));

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

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |           0 |           0 |           0 |           0 |         N/A |         N/A |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
        // @formatter:on

    }
}
