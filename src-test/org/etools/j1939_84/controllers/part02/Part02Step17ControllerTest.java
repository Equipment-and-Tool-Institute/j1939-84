/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
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
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.Lookup;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.GhgTrackingModule;
import org.etools.j1939tools.modules.NOxBinningModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class Part02Step17ControllerTest extends AbstractControllerTest {

    @Mock
    private Executor executor;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

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

     private static final int BUS_ADDR = 0xA5;

    @Mock
    private CommunicationsModule communicationsModule;

    private static List<SupportedSPN> spns(int... ids) {
        return Arrays.stream(ids).mapToObj(id -> {
            return SupportedSPN.create(id, false, true, false, false, 1);
        }).collect(Collectors.toList());
    }

    private static GenericPacket packet(int spnId, Boolean isNotAvailable) {
        GenericPacket mock = mock(GenericPacket.class);

        Spn spn = mock(Spn.class);
        when(spn.getId()).thenReturn(spnId);
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
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 tableA1Validator,
                                 broadcastValidator,
                                 busService);
    }

    @Test
    public void testRunObdPgnSupports12783() throws BusException {
        List<Integer> supportedSpns = List.of(12783);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(12783,
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

        GenericPacket packet3 = packet(12783, false, 0);
        packets.add(packet3);
        obdModule0.set(packet3, 1);

        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(12783, false, 0);
        packets.add(packet1);
        obdModule0.set(packet1, 1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 1);

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 1);

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 1);

        when(communicationsModule.request(eq(64244),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64244));
        when(communicationsModule.request(eq(64245),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64245));
        when(communicationsModule.request(eq(64246),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64246));

        dataRepository.putObdModule(obdModule0);
        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(packets);
        verify(broadcastValidator).reportBroadcastPeriod(any(),
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
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.2.a"));
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           eq("6.2.17.2.b"));
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               eq("6.2.17.2.c"));
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  eq("6.2.17.4.a"));

        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,680 |       2,240 |      29,120 |" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,344 |       1,792 |      23,296 |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |         336 |         448 |       5,824 |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |         512 |         683 |       2,223 |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |         128 |         171 |      52,910 |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |       6,105 |       8,140 |      16,926 |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |         977 |       1,302 |     122,746 |" + NL;
        expected += "| Grid: Energy into battery, kWh |       7,082 |       9,442 |           0 |" + NL;
        expected += "| Prod: Prop system active,  s   |             |             |             |" + NL;
        expected += "| Prod: Prop idle active,    s   |             |             |             |" + NL;
        expected += "| Prod: Prop urban active,   s   |             |             |             |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        // assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Lifetime Hours (HCDIOL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Active 100 Hours (HCDIOA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Stored 100 Hours (HCDIOS) from Engine #1 (0)";

        assertEquals(expectedMsg, listener.getMessages());
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

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule0);
        DM24SPNSupportPacket packet0 = DM24SPNSupportPacket.create(0,
                                                                   supportedSpns.stream()
                                                                                .map(spn -> SupportedSPN.create(spn,
                                                                                                                false,
                                                                                                                false,
                                                                                                                false,
                                                                                                                false,
                                                                                                                0))
                                                                                .toArray(SupportedSPN[]::new));
        obdModule0.set(packet0, 1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(0x01);
        dataRepository.putObdModule(obdModule1);
        //@formatter:off
        DM24SPNSupportPacket packet1 = new DM24SPNSupportPacket(Packet.create(DM24SPNSupportPacket.PGN,
                                                                           0x01,
                                                                           0x5C, 0x00, 0x1C, 0x01, 0x00, 0x02, 0x1C, 0x01, 0x01, 0x02, 0x1C, 0x01, 0x20, 0x02, 0x1C,
                                                                           0x02, 0x1B, 0x02, 0x1C, 0x01, 0x1C, 0x02, 0x1C, 0x01, 0x1D, 0x02, 0x1C, 0x01, 0x1E, 0x02,
                                                                           0x1C, 0x01, 0x1F, 0x02, 0x1C, 0x01, 0x6E, 0x00, 0x1C, 0x01, 0xAF, 0x00, 0x1C, 0x02, 0xBE,
                                                                           0x00, 0x1C, 0x02, 0x54, 0x00, 0x1C, 0x02, 0x6C, 0x00, 0x1C, 0x01, 0x9E, 0x00, 0x1C, 0x02,
                                                                           0x33, 0x00, 0x1C, 0x01, 0x5E, 0x00, 0x1C, 0x01, 0xAC, 0x00, 0x1C, 0x01, 0x69, 0x00, 0x1C,
                                                                           0x01, 0x84, 0x00, 0x1C, 0x02, 0xD0, 0x03, 0x1C, 0x01, 0x5B, 0x00, 0x1C, 0x01, 0xB7, 0x00,
                                                                           0x1C, 0x02, 0x66, 0x00, 0x18, 0x01, 0xAD, 0x00, 0x1C, 0x02, 0xB3, 0x0C, 0x1C, 0x02, 0x9B,
                                                                           0x0D, 0x1C, 0x01, 0xCD, 0x16, 0x1C, 0x01, 0xE5, 0x0C, 0x1C, 0x02, 0x5A, 0x15, 0x1C, 0x02,
                                                                           0xCB, 0x14, 0x1C, 0x01, 0x88, 0x0D, 0x1C, 0x02, 0xB9, 0x04, 0x1C, 0x02, 0xA5, 0x15, 0x1C,
                                                                           0x01, 0xA4, 0x00, 0x18, 0x02, 0xE7, 0x0A, 0x1C, 0x02, 0x85, 0x05, 0x1C, 0x02, 0x86, 0x05,
                                                                           0x1C, 0x02, 0x87, 0x05, 0x1C, 0x02, 0x88, 0x05, 0x1C, 0x02, 0x89, 0x05, 0x1C, 0x02, 0x8A,
                                                                           0x05, 0x1C, 0x02, 0xEB, 0x0D, 0x1C, 0x01, 0x1B, 0x00, 0x1C, 0x02, 0xAA, 0x0C, 0x1C, 0x02,
                                                                           0xAE, 0x0C, 0x1C, 0x02, 0x90, 0x0C, 0x18, 0x02, 0x39, 0x04, 0x1C, 0x01, 0xA5, 0x04, 0x1C,
                                                                           0x01, 0xC2, 0x14, 0x1C, 0x02, 0x19, 0x0E, 0x1E, 0x02, 0x98, 0x0D, 0x1E, 0x02, 0x9A, 0x0C,
                                                                           0x1C, 0x02, 0xE1, 0x06, 0x1C, 0x01, 0x9A, 0x0D, 0x1E, 0x01, 0xA2, 0x0D, 0x1E, 0x01, 0x08,
                                                                           0x11, 0x1E, 0x02, 0x0B, 0x11, 0x1E, 0x02, 0xD7, 0x0B, 0x1E, 0x01, 0x45, 0x11, 0x1F, 0x01,
                                                                           0x95, 0x04, 0x18, 0x02, 0xED, 0x00, 0x1D, 0x11, 0xA1, 0x10, 0x1B, 0x01, 0x00, 0x09, 0x1F,
                                                                           0x01, 0x83, 0x03, 0x1D, 0x01, 0x01, 0x09, 0x1F, 0x01, 0xFD, 0x0B, 0x1D, 0x01, 0xDE, 0x0C,
                                                                           0x1D, 0x01, 0xDF, 0x0C, 0x1D, 0x01, 0xE0, 0x0C, 0x1D, 0x01, 0xE6, 0x0C, 0x1D, 0x01, 0x87,
                                                                           0x0E, 0x1D, 0x01, 0x2B, 0x05, 0x1B, 0x01, 0x2C, 0x05, 0x1B, 0x01, 0x2D, 0x05, 0x1B, 0x01,
                                                                           0x2E, 0x05, 0x1B, 0x01, 0x2F, 0x05, 0x1B, 0x01, 0x30, 0x05, 0x1B, 0x01, 0x46, 0x0A, 0x1B,
                                                                           0x02, 0x63, 0x0A, 0x1B, 0x02, 0x2A, 0x05, 0x1B, 0x01, 0x90, 0x12, 0x1B, 0x01, 0x9E, 0x12,
                                                                           0x1F, 0x02, 0xC7, 0x14, 0x1F, 0x01, 0x8B, 0x02, 0x1B, 0x01, 0x8C, 0x02, 0x1B, 0x01, 0x8D,
                                                                           0x02, 0x1B, 0x01, 0x8E, 0x02, 0x1B, 0x01, 0x8F, 0x02, 0x1B, 0x01, 0x90, 0x02, 0x1B, 0x01,
                                                                           0x8F, 0x0D, 0x1B, 0x01, 0xE4, 0x0D, 0x1B, 0x01, 0x03, 0x09, 0x1F, 0x01, 0x0A, 0x10, 0x1D,
                                                                           0x01, 0xC0, 0x04, 0x1D, 0x01, 0xC4, 0x04, 0x1D, 0x01, 0x1F, 0x10, 0x1D, 0x01, 0x22, 0x10,
                                                                           0x1D, 0x01, 0xAB, 0x00, 0x1D, 0x02, 0x9D, 0x00, 0x1F, 0x02, 0x3F, 0x0A, 0x1D, 0x01, 0xF7,
                                                                           0x00, 0x1D, 0x04, 0xEB, 0x00, 0x1D, 0x04, 0xBD, 0x04, 0x1D, 0x01, 0xE7, 0x0C, 0x1D, 0x01,
                                                                           0xE8, 0x0C, 0x1D, 0x01, 0x4E, 0x15, 0x1D, 0x04, 0x4C, 0x02, 0x1D, 0x11, 0xEF, 0x0B, 0x1F,
                                                                           0x01, 0x57, 0x15, 0x1D, 0x01, 0xF8, 0x00, 0x1D, 0x04, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00,
                                                                           0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00,
                                                                           0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00));
        //@formatter:on
        obdModule1.set(packet1, 1);

        OBDModuleInformation obdModule17 = new OBDModuleInformation(17);
        dataRepository.putObdModule(obdModule17);
        DM24SPNSupportPacket packet17 = DM24SPNSupportPacket.create(17,
                                                                    supportedSpns.stream()
                                                                                 .map(spn -> SupportedSPN.create(spn,
                                                                                                                 false,
                                                                                                                 false,
                                                                                                                 false,
                                                                                                                 false,
                                                                                                                 0))
                                                                                 .toArray(SupportedSPN[]::new));
        obdModule17.set(packet17, 1);

        when(busService.collectNonOnRequestPGNs(any())).thenReturn(List.of(11111, 22222, 33333));

        List<Integer> pgns = List.of(22222,
                                     44444,
                                     55555,
                                     66666);
        when(busService.getPGNsForDSRequest(any(), any())).thenReturn(pgns);

        List<GenericPacket> dsPackets = new ArrayList<>();
        GenericPacket packet4 = packet(444, false);
        dsPackets.add(packet4);
        when(busService.dsRequest(eq(44444), eq(0), any())).thenReturn(Stream.of(packet4));

        GenericPacket packet5 = packet(555, false);
        dsPackets.add(packet5);
        when(busService.dsRequest(eq(55555), eq(0), any())).thenReturn(Stream.of(packet5));

        GenericPacket packet6 = packet(666, true);
        dsPackets.add(packet6);
        when(busService.dsRequest(eq(66666), eq(0), any())).thenReturn(Stream.of(packet6));

        runTest();

        verify(busService).setup(eq(j1939), any());
        verify(busService, times(3)).collectNonOnRequestPGNs(any());
        verify(busService, times(3)).getPGNsForDSRequest(any(), any());
        verify(busService).dsRequest(eq(22222), eq(0), any());
        verify(busService).dsRequest(eq(22222), eq(1), any());
        verify(busService).dsRequest(eq(44444), eq(0), any());
        verify(busService).dsRequest(eq(55555), eq(0), any());
        verify(busService).dsRequest(eq(66666), eq(0), any());

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(eq(List.of()));
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    any(),
                                                                    eq(List.of()),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    any(),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(17),
                                                                    eq(List.of()),
                                                                    any(),
                                                                    eq(List.of(11111, 22222, 33333)),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator, times(12)).collectAndReportNotAvailableSPNs(any(),
                                                                               anyInt(),
                                                                               any(),
                                                                               anyInt(),
                                                                               any(ResultsListener.class),
                                                                               eq(2),
                                                                               eq(17),
                                                                               eq("6.2.17.5.b"));

        pgns.forEach(pgn -> {
            // verify(busService).globalRequest(eq(pgn), any());
            verify(busService).dsRequest(eq(pgn), eq(0), any());
            verify(busService).dsRequest(eq(pgn), eq(1), any());
            verify(busService).dsRequest(eq(pgn), eq(17), any());

            // verify(broadcastValidator).collectAndReportNotAvailableSPNs(any(),
            // eq(pgn),
            // any(),
            // eq(0),
            // any(ResultsListener.class),
            // eq(2),
            // eq(17),
            // eq("6.2.17.5.a"));
            // verify(broadcastValidator).collectAndReportNotAvailableSPNs(any(),
            // eq(pgn),
            // any(),
            // eq(1),
            // any(ResultsListener.class),
            // eq(2),
            // eq(17),
            // eq("6.2.17.5.a"));
            // verify(broadcastValidator).collectAndReportNotAvailableSPNs(any(),
            // eq(pgn),
            // any(),
            // eq(17),
            // any(ResultsListener.class),
            // eq(2),
            // eq(17),
            // eq("6.2.17.5.a"));
            // verify(broadcastValidator).collectAndReportNotAvailableSPNs(any(),
            // eq(pgn),
            // any(),
            // isNull(),
            // any(ResultsListener.class),
            // eq(2),
            // eq(17),
            // eq("6.2.17.5.b"));
        });

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(0),
                                   eq("6.2.17.1.c"));
        // verify(busService).globalRequest(eq(22222), any());
        // verify(broadcastValidator).collectAndReportNotAvailableSPNs(any(),
        // eq(22222),
        // any(),
        // eq(0),
        // any(ResultsListener.class),
        // eq(2),
        // eq(17),
        // eq("6.2.17.6.a"));

        // verify(mockListener).addOutcome(eq(2),
        // eq(17),
        // eq(WARN),
        // eq("6.2.17.6.c - Global request was required for PGN 22222 for broadcast SPNs 111"));
        // verify(mockListener).addOutcome(eq(2),
        // eq(17),
        // eq(WARN),
        // eq("6.2.17.6.c - Global request was required for PGN 44444 for broadcast SPNs 111"));
        // verify(mockListener).addOutcome(eq(2),
        // eq(17),
        // eq(WARN),
        // eq("6.2.17.6.c - Global request was required for PGN 55555 for broadcast SPNs 111"));
        // verify(mockListener).addOutcome(eq(2),
        // eq(17),
        // eq(WARN),
        // eq("6.2.17.6.c - Global request was required for PGN 66666 for broadcast SPNs 111"));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule1));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule17));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.2.d"));
        dsPackets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet),
                                                            any(ResultsListener.class),
                                                            eq("6.2.17.6.b"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(true),
                                                                eq("6.2.17.6.e"));
        });
        // verify(tableA1Validator).reportNotAvailableSPNs(eq(packet2),
        // any(ResultsListener.class),
        // eq("6.2.17.6.d"));
        // verify(tableA1Validator).reportImplausibleSPNValues(eq(packet2),
        // any(ResultsListener.class),
        // eq(true),
        // eq("6.2.17.6.e"));

        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.g"));

        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(),
                                                                                  any());

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #1 (0)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 2.17 - Verifying Engine #2 (1)" + NL;
        expectedMsg += "Test 2.17 - Verifying Cruise Control (17)" + NL;
        expectedMsg += "Test 2.17 - Verifying Cruise Control (17)" + NL;
        expectedMsg += "Test 2.17 - Verifying Cruise Control (17)" + NL;
        expectedMsg += "Test 2.17 - Verifying Cruise Control (17)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void runWithUnExpectedToolSaMsg() {
        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);
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
        List<Integer> supportedSpns = List.of(111, 222, 333, 444, 555, 666, 777, 888, 999);
        List<SupportedSPN> supportedSPNList = spns(111, 222, 333, 444, 555, 666, 777, 888, 999);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.set(DM24SPNSupportPacket.create(0,
                                                   SupportedSPN.create(111,
                                                                       false,
                                                                       true,
                                                                       false,
                                                                       false,
                                                                       2)),
                       1);
        dataRepository.putObdModule(obdModule0);
        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM24SPNSupportPacket.create(1, supportedSPNList.toArray(new SupportedSPN[0])),
                       1);
        dataRepository.putObdModule(obdModule1);

        OBDModuleInformation obdModule17 = new OBDModuleInformation(17);
        obdModule17.set(DM24SPNSupportPacket.create(17,
                                                    SupportedSPN.create(111,
                                                                        false,
                                                                        false,
                                                                        false,
                                                                        false,
                                                                        2)),
                        1);
        dataRepository.putObdModule(obdModule17);
        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet1 = packet(111, false);
        packets.add(packet1);
        GenericPacket packet3 = packet(333, true);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenAnswer(a -> {
            instance.stop();
            return packets.stream();
        });

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);
        List<Integer> onRequestPgns = Arrays.asList(11111, 22222, 33333);

        when(busService.collectNonOnRequestPGNs(any())).thenReturn(onRequestPgns);

        runTest();

        verify(busService).setup(eq(j1939), any());
        verify(busService).readBus(eq(12), eq("6.2.17.1.c"));
        verify(busService, times(3)).collectNonOnRequestPGNs(any());
        verify(busService, atLeastOnce()).getPGNsForDSRequest(any(), any());

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(eq(packets));
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         eq(List.of(111, 111, 222, 333, 444, 555, 666, 777, 888, 999)),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    eq(packets),
                                                                    eq(List.of()),
                                                                    eq(onRequestPgns),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    eq(supportedSpns),
                                                                    eq(onRequestPgns),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(17),
                                                                    eq(List.of()),
                                                                    eq(List.of()),
                                                                    eq(onRequestPgns),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService, times(2)).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns));

        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule1));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule17));
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet), any(ResultsListener.class), eq("6.2.17.2.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(true),
                                                                eq("6.2.17.2.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.2.17.2.c"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.4.a"));
        });
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(),
                                                                    any(ResultsListener.class),
                                                                    eq("6.2.17.2.d"));
        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.g"));

        assertEquals(dataRepository.getObdModuleAddresses(), List.of(0, 1, 17));

        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testInterruptedExceptionToForcePacketStreamClose() {
        // SPNs
        // 111 - Broadcast with value
        // 444 - DS with value
        List<SupportedSPN> supportedSPNList = spns(111, 444);

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

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

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM24SPNSupportPacket.create(1,
                                                   supportedSPNList.toArray(new SupportedSPN[0])),
                       1);
        dataRepository.putObdModule(obdModule1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet1 = packet(111, false);
        packets.add(packet1);
        when(busService.readBus(12, "6.2.17.1.c")).thenReturn(packets.stream());

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(eq(packets));
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    any(),
                                                                    eq(List.of()),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    any(),
                                                                    eq(List.of(111, 444)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(12), eq("6.2.17.1.c"));
        verify(busService, times(2))
                                    .collectNonOnRequestPGNs(eq(List.of(111, 111, 444)));

        verify(busService).getPGNsForDSRequest(List.of(), List.of(111, 444));
        verify(busService).getPGNsForDSRequest(List.of(), List.of());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), any());
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet),
                                                            any(ResultsListener.class),
                                                            eq("6.2.17.2.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet),
                                                                any(ResultsListener.class),
                                                                eq(true),
                                                                eq("6.2.17.2.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet),
                                                                    any(ResultsListener.class),
                                                                    eq("6.2.17.2.c"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.4.a"));

            verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                         any(ResultsListener.class),
                                                         eq("6.2.17.2.d"));
        });
        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.g"));
        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void runWithUiInterruptionFailure() {

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        // SPNs
        // 111 - Broadcast with value
        // 444 - DS with value
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

        OBDModuleInformation obdModule1 = new OBDModuleInformation(1);
        obdModule1.set(DM24SPNSupportPacket.create(1,
                                                   supportedSPNList.toArray(new SupportedSPN[0])),
                       1);

        dataRepository.putObdModule(obdModule1);

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();
        GenericPacket packet1 = packet(111, false);
        packets.add(packet1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenAnswer(a -> {
            instance.stop();
            return packets.stream();
        });

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         eq(List.of(111, 111, 444)),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    any(),
                                                                    eq(List.of()),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    eq(List.of(111, 444)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(1),
                                                                    eq(List.of()),
                                                                    eq(List.of(111, 444)),
                                                                    eq(List.of()),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(12), eq("6.2.17.1.c"));
        verify(busService, times(2)).collectNonOnRequestPGNs(eq(List.of(111, 111, 444)));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of(111, 444)));

        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule1));
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet), any(ResultsListener.class), eq("6.2.17.2.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet), any(ResultsListener.class), eq(true), eq("6.2.17.2.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet), any(ResultsListener.class), eq("6.2.17.2.c"));
            verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                         any(ResultsListener.class),
                                                         eq("6.2.17.2.d"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq("6.2.17.4.a"));

        });
        verify(tableA1Validator).reportDuplicateSPNs(any(),
                                                     any(ResultsListener.class),
                                                     eq("6.2.17.6.g"));

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

        when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        obdModule0.set(packet3, 1);

        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        obdModule0.set(response64241, 1);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64241));

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
        obdModule0.set(response64242, 1);
        when(communicationsModule.request(eq(64242),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64242));

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
        obdModule0.set(response64243, 1);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64243));

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
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
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.2.a"));
        verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
                                                                           any(ResultsListener.class),
                                                                           eq(true),
                                                                           eq("6.2.17.2.b"));
        verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
                                                                               any(ResultsListener.class),
                                                                               eq("6.2.17.2.c"));
        verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
                                                                                  any(ResultsListener.class),
                                                                                  eq("6.2.17.4.a"));
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
    public void testRunObdPgnSupports12691WarningTwentrysixEighteenA() {
        final int supportedSpn = 12691;

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
        obdModule0.set(packet3, 1);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        obdModule0.set(packet8, 1);
        packets.add(packet8);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        obdModule0.set(packet1, 1);
        packets.add(packet1);

//        Packet requestPacket64257 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64257, 64257 >> 8, 64257 >> 16);
//        doReturn(requestPacket64257).when(j1939).createRequestPacket(64257, 0x00);

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00, 0xC0, 0xBC, 0x05, 0x00,
                                                                      0x04, 0xCE, 0x31, 0x02, 0x00, 0x02, 0x49, 0x1D, 0x00,
                                                                      0x02, 0x00, 0xE0, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0xF9, 0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 0x00,
                                                                      0xF7, 0x4B, 0xC3, 0x00, 0x00, 0x90, 0xFB, 0x00, 0x00,
                                                                      0xF5, 0xD0, 0xB3, 0x00, 0x00, 0x38, 0xED, 0x02, 0x00));
        // @formatter:on
        obdModule0.set(response64257, 1);
        when(communicationsModule.request(eq(64257),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64257));

//        Packet requestPacket64255 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64255, 64255 >> 8, 64255 >> 16);
//        doReturn(requestPacket64255).when(j1939).createRequestPacket(64255, 0x00);
        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x5B, 0x32, 0x34, 0x04,
                                                                      0x04, 0x06, 0x08, 0xB0, 0x06,
                                                                      0x02, 0x6B, 0x00, 0x58, 0x00,
                                                                      0xF9, 0x7A, 0x02, 0x10, 0x02,
                                                                      0xF7, 0xDC, 0x00, 0x74, 0x22,
                                                                      0xF5, 0x91, 0x02, 0x24, 0x02));
        // @formatter:on
        obdModule0.set(response64255, 1);
        when(communicationsModule.request(eq(64255),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of());

        Packet requestPacket64256 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64256, 64256 >> 8, 64256 >> 16);
//        doReturn(requestPacket64256).when(j1939).createRequestPacket(64256, 0x00);
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
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64256));


        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         eq(Collections.emptyList()),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                            any(),
                                                            eq(List.of()),
                                                            any(),
                                                            any(ResultsListener.class),
                                                            eq(2),
                                                            eq(17),
                                                            eq("6.2.17.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(eq(12), eq("6.2.17.1.c"));
        verify(busService).collectNonOnRequestPGNs(any());
        verify(busService).getPGNsForDSRequest(any(), any());

//        verify(mockListener).addOutcome(eq(instance.getPartNumber()),
//                                        eq(instance.getStepNumber()),
//                                        eq(FAIL),
//                                        eq("6.2.17.18.a - No response was received from Engine #1 (0) for PG 64255"));

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(), eq(obdModule0));
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
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |           0 |           0 |         N/A |         N/A |  48,521,732 |           0 |" + NL;
        expected += "| Predictive Cruise Control           |         N/A |         N/A |         N/A |         N/A |      17,888 |           0 |" + NL;
        expected += "| Unknown 49                          |         N/A |         N/A |         N/A |         N/A |       2,185 |           0 |" + NL;
        expected += "| Unknown C0                          |         N/A |         N/A |         N/A |         N/A |   1,118,506 |           0 |" + NL;
        expected += "| Unknown CE                          |         N/A |         N/A |         N/A |         N/A |     559,250 |           0 |" + NL;
        expected += "| Unknown E0                          |         N/A |         N/A |         N/A |         N/A |           2 |           0 |" + NL;
        expected += "| Mfg Defined Active Technology 6     |         N/A |         N/A |         N/A |         N/A |         767 |           0 |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on

//        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";

//        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12691FailureTwentrysixEighteenG() throws BusException {
        final int supportedSpn = 12691;
        List<Integer> supportedSpns = List.of(supportedSpn);

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
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        Packet requestPacket64257 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64257, 64257 >> 8, 64257 >> 16);
        // doReturn(requestPacket64257).when(j1939).createRequestPacket(64257, 0x00);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00, 0xC0, 0xBC, 0x05, 0x00,
                                                                      0x04, 0xCE, 0x31, 0x02, 0x00, 0x02, 0x49, 0x1D, 0x00,
                                                                      0x02, 0x00, 0xE0, 0x79, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0xF9, 0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 0x00,
                                                                      0xF7, 0x4B, 0xC3, 0x00, 0x00, 0x90, 0xFB, 0x00, 0x00,
                                                                      0xF5, 0xD0, 0xB3, 0x00, 0x00, 0x38, 0xED, 0x02, 0x00));
        // @formatter:on
        responses.add(response64257);
        obdModule0.set(response64257, 1);
        when(communicationsModule.request(eq(64257),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64257));

        Packet requestPacket64255 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64255, 64255 >> 8, 64255 >> 16);
        // doReturn(requestPacket64255).when(j1939).createRequestPacket(64255, 0x00);
        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x5B, 0x32, 0x34, 0x04,
                                                                      0x04, 0x06, 0x08, 0xB0, 0x06,
                                                                      0x02, 0x6B, 0x00, 0x58, 0x00,
                                                                      0xF9, 0x7A, 0x02, 0x10, 0x02,
                                                                      0xF7, 0xDC, 0x00, 0x74, 0x22,
                                                                      0xF5, 0x91, 0x02, 0x24, 0x02));
        // @formatter:on
        responses.add(response64255);
        obdModule0.set(response64255, 1);
        when(communicationsModule.request(eq(64255),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64255));

        Packet requestPacket64256 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64256, 64256 >> 8, 64256 >> 16);
        // doReturn(requestPacket64256).when(j1939).createRequestPacket(64256, 0x00);
        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xAD, 0x01, 0x68, 0x01,
                                                                      0x04, 0x03, 0x04, 0x58, 0x03,
                                                                      0x02, 0x23, 0x00, 0x1C, 0x00,
                                                                      0xF9, 0x3D, 0x01, 0x08, 0x01,
                                                                      0xF7, 0x49, 0x00, 0x3C, 0x00,
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00));
        // @formatter:on
        responses.add(response64256);
        obdModule0.set(response64256, 1);
        when(communicationsModule.request(eq(64256),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64256));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        dataRepository.putObdModule(obdModule0);
        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
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
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.2.a"));
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
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|"
                + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |"
                + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |"
                + NL;
        expected += "| Description                         |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |"
                + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |          72 |          90 |       2,148 |         269 |  48,521,732 |           0 |" + NL;
        expected += "| Cylinder Deactivation               |           6 |           7 |          18 |          22 |         N/A |         N/A |" + NL;
        expected += "| Intelligent Control                 |         171 |         214 |         342 |         428 |         N/A |         N/A |" + NL;
        expected += "| Predictive Cruise Control           |         N/A |         N/A |         N/A |         N/A |      17,888 |           0 |" + NL;
        expected += "| Unknown 49                          |         N/A |         N/A |         N/A |         N/A |       2,185 |           0 |" + NL;
        expected += "| Unknown C0                          |         N/A |         N/A |         N/A |         N/A |   1,118,506 |           0 |" + NL;
        expected += "| Unknown CE                          |         N/A |         N/A |         N/A |         N/A |     559,250 |           0 |" + NL;
        expected += "| Unknown E0                          |         N/A |         N/A |         N/A |         N/A |           2 |           0 |" + NL;
        expected += "| Mfg Defined Active Technology 6     |          36 |          46 |         110 |         137 |         767 |           0 |" + NL;
        expected += "| Mfg Defined Active Technology 4     |          12 |          15 |          37 |       2,205 |         N/A |         N/A |" + NL;
        expected += "| Mfg Defined Active Technology 2     |          53 |          66 |         106 |         132 |         N/A |         N/A |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on

//        assertEquals(expected, listener.getResults());

        String expectedMsg = "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";

//        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12797Two() throws BusException {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = List.of(supportedSpn);
        List<SupportedSPN> supportedSPNList = spns(supportedSpn);

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
        obdModule0.set(packet3, 1);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);
        obdModule0.set(packet1, 1);

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
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64241));

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
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64242));

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
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64243));

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
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
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(), any());
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
        // verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
        // any(ResultsListener.class),
        // eq(false));
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

    private static GenericPacket packet(int spnId, Boolean isNotAvailable, int sourceAddress) {

        GenericPacket mock = mock(GenericPacket.class);

        Slot slot = J1939DaRepository.getInstance().findSLOT(spnId);
        byte[] bytes = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        Spn spn = new Spn(spnId, Lookup.getSpnName(spnId), slot, bytes);

        when(mock.getSourceAddress()).thenReturn(sourceAddress);
        when(mock.getSpns()).thenReturn(List.of(spn));
        PgnDefinition pgnDefinition = mock(PgnDefinition.class);
        when(pgnDefinition.getId()).thenReturn(spnId);
        when(mock.getPgnDefinition()).thenReturn(pgnDefinition);
        return mock;
    }

    // Helper method to get the pg for the class object
    private static int getPg(Class<? extends GenericPacket> clazz) {
        int pg = 0;
        try {
            pg = clazz.getField("PGN").getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pg;
    }

    @Test
    public void testRunObdPgnSupports12730() throws BusException {
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

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2022);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        SupportedSPN supportedSPN = SupportedSPN.create(12730,
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

        GenericPacket packet3 = packet(333, true, 0);
        packets.add(packet3);
        obdModule0.set(packet3, 1);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        // when(busService.readBus(eq(12), eq("6.1.26.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(12730, false, 0);
        obdModule0.set(packet1, 1);
        packets.add(packet1);

        Packet requestPacket64252 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64252, 64252 >> 8, 64252 >> 16);
        // doReturn(requestPacket64252).when(j1939).createRequestPacket(64252, 0x00);
        var response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                            0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                            0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                            0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                            0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64252));
        packets.add(response64252);
        obdModule0.set(response64252, 1);

        Packet requestPacket64253 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64253, 64253 >> 8, 64253 >> 16);
        // doReturn(requestPacket64253).when(j1939).createRequestPacket(64253, 0x00);
        var response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
        // @formatter:off
                                                            0xB0, 0x30, 0x2C, 0x02, 0x58, 0x94, 0x62, 0x06,
                                                            0x7A, 0xD6, 0x05, 0x00, 0x5D, 0x30, 0x1D, 0x00,
                                                            0x27, 0x76, 0x4A, 0x00, 0x4F, 0xD6, 0xF8, 0x0B,
                                                            0x9D, 0xE7, 0x0D, 0x00, 0x2E, 0x06, 0x00, 0x00,
                                                            0x4A, 0x18, 0x61, 0x01, 0xCC, 0x3D, 0x00, 0x00,
                                                            0xD9, 0x02, 0x00, 0x00, 0x3B, 0xCF, 0x1B, 0x00));
        // @formatter:on
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64253));
        packets.add(response64253);
        obdModule0.set(response64253, 1);

        Packet requestPacket64254 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64254, 64254 >> 8, 64254 >> 16);
        // doReturn(requestPacket64254).when(j1939).createRequestPacket(eq(64254), eq(0x00));
        var response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
        // @formatter:off
                                                            0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                            0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                            0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                            0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64254));
        packets.add(response64254);
        obdModule0.set(response64254, 1);

        dataRepository.putObdModule(obdModule0);

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        // when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        // when(busService.collectNonOnRequestPGNs(supportedSpns))
        // .thenReturn(List.of(11111, 22222, 33333));

        Bus busMock = mock(Bus.class);
        // when(j1939.getBus()).thenReturn(busMock);
        // when(busMock.imposterDetected()).thenReturn(false);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         any(),
                                                         any(),
                                                         eq(2),
                                                         eq(17));
        packets.forEach(packet -> {
            verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
                                                                        any(),
                                                                        any(),
                                                                        any(),
                                                                        any(),
                                                                        eq(2),
                                                                        eq(17),
                                                                        eq("6.2.17.5.a"));
        });
        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService, atLeastOnce()).collectNonOnRequestPGNs(any());
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService, atLeastOnce()).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), eq(obdModule0));

        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       2,077 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |       7,053 |         139 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |      18,988 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |         817 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |      14,170 |      54,906 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 | 811,401,221 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |           5 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |       5,041 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |          37 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       9,144 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |       1,532 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |      59,293 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |           2 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        assertEquals(expected, listener.getResults());

        String expectedMsg = "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";

        assertEquals(expectedMsg, listener.getMessages());
    }

    // @Test
    public void testRunObdPgnSupports12675(){
        int supportedSpn = 12675;
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
        obdModule0.set(packet3, 1);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);
        obdModule0.set(packet1, 1);

        List<GenericPacket> responses = new ArrayList<>();
        Packet requestPacket64252 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64252, 64252 >> 8, 64252 >> 16);
        doReturn(requestPacket64252).when(j1939).createRequestPacket(64252, 0x00);
        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        responses.add(response64252);
        obdModule0.set(response64252, 1);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64252));

        Packet requestPacket64258 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64258, 64258 >> 8, 64258 >> 16);
        doReturn(requestPacket64258).when(j1939).createRequestPacket(64258, 0x00);
        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64258);
        obdModule0.set(response64258, 1);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64258));

        Packet requestPacket64259 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64259, 64259 >> 8, 64259 >> 16);
        doReturn(requestPacket64259).when(j1939).createRequestPacket(64259, 0x00);
        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64259);
        obdModule0.set(response64259, 1);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64259));

        Packet requestPacket64260 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64260, 64260 >> 8, 64260 >> 16);
        doReturn(requestPacket64260).when(j1939).createRequestPacket(64260, 0x00);
        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64260);
        obdModule0.set(response64260, 1);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64260));

        Packet requestPacket64261 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64261, 64261 >> 8, 64261 >> 16);
        doReturn(requestPacket64261).when(j1939).createRequestPacket(64261, 0x00);
        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64261);
        obdModule0.set(response64261, 1);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64261));

        Packet requestPacket64262 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64262, 64262 >> 8, 64262 >> 16);
        doReturn(requestPacket64262).when(j1939).createRequestPacket(64262, 0x00);
        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64262);
        obdModule0.set(response64262, 1);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64262));

        Packet requestPacket64263 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64263, 64263 >> 8, 64263 >> 16);
        doReturn(requestPacket64263).when(j1939).createRequestPacket(64263, 0x00);
        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64263);
        obdModule0.set(response64263, 1);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64263));

        Packet requestPacket64264 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64264, 64264 >> 8, 64264 >> 16);
        doReturn(requestPacket64264).when(j1939).createRequestPacket(64264, 0x00);
        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64264);
        obdModule0.set(response64264, 1);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64264));

        Packet requestPacket64265 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64265, 64265 >> 8, 64265 >> 16);
        doReturn(requestPacket64265).when(j1939).createRequestPacket(64265, 0x00);
        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64265);
        obdModule0.set(response64265, 1);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64265));

        Packet requestPacket64266 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64266, 64266 >> 8, 64266 >> 16);
        doReturn(requestPacket64266).when(j1939).createRequestPacket(64266, 0x00);
        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64266);
        obdModule0.set(response64266, 1);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64266));

        Packet requestPacket64267 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64267, 64267 >> 8, 64267 >> 16);
        doReturn(requestPacket64267).when(j1939).createRequestPacket(64267, 0x00);
        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0xBD, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        responses.add(response64267);
        obdModule0.set(response64267, 1);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64267));

        Packet requestPacket64268 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64268, 64268 >> 8, 64268 >> 16);
        doReturn(requestPacket64268).when(j1939).createRequestPacket(64268, 0x00);
        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0,
        // @formatter:off
                                                                      0x00, 0x84, 0x01, 0x84, 0x03, 0x84, 0x05, 0x84,
                                                                      0x07, 0x84, 0x09, 0x84, 0x0B, 0x84, 0x0D, 0x84,
                                                                      0x0F, 0x84, 0x11, 0x84, 0x13, 0x84, 0x15, 0x84,
                                                                      0x17, 0x84, 0x19, 0x84, 0x1B, 0x84, 0x1D, 0x84,
                                                                      0x1F, 0x84));
        // @formatter:on
        responses.add(response64268);
        obdModule0.set(response64268, 1);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64268));

        Packet requestPacket64269 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64269, 64269 >> 8, 64269 >> 16);
        doReturn(requestPacket64269).when(j1939).createRequestPacket(64269, 0x00);
        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x20, 0x84, 0x21, 0x84, 0x23, 0x84, 0x25, 0x84,
                                                                      0x27, 0x84, 0x29, 0x84, 0x2B, 0x84, 0x2D, 0x84,
                                                                      0x2F, 0x84, 0x31, 0x84, 0x33, 0x84, 0x35, 0x84,
                                                                      0x37, 0x84, 0x39, 0x84, 0x3B, 0x84, 0x3D, 0x84,
                                                                      0x3F, 0x8));
        // @formatter:on
        responses.add(response64269);
        obdModule0.set(response64269, 1);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64269));

        Packet requestPacket64270 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64270, 64270 >> 8, 64270 >> 16);
        doReturn(requestPacket64270).when(j1939).createRequestPacket(64270, 0x00);
        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0x40, 0x84, 0x41, 0x84, 0x43, 0x84, 0x45, 0x84,
                                                                      0x47, 0x84, 0x49, 0x84, 0x4B, 0x84, 0x4D, 0x84,
                                                                      0x4F, 0x84, 0x51, 0x84, 0x53, 0x84, 0x55, 0x84,
                                                                      0x57, 0x84, 0x59, 0x84, 0x5B, 0x84, 0x5D, 0x84,
                                                                      0x5F, 0x84));
        // @formatter:on
        responses.add(response64270);
        obdModule0.set(response64270, 1);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64270));

        Packet requestPacket64271 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64271, 64271 >> 8, 64271 >> 16);
        doReturn(requestPacket64271).when(j1939).createRequestPacket(64271, 0x00);
        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0x60, 0x84, 0x61, 0x84, 0x63, 0x84, 0x65, 0x84,
                                                                      0x67, 0x84, 0x69, 0x84, 0x6B, 0x84, 0x6D, 0x84,
                                                                      0x6F, 0x84, 0x71, 0x84, 0x73, 0x84, 0x75, 0x84,
                                                                      0x77, 0x84, 0x79, 0x84, 0x7B, 0x84, 0x7D, 0x84,
                                                                      0x7F, 0x84));
        // @formatter:on
        responses.add(response64271);
        obdModule0.set(response64271, 1);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64271));

        Packet requestPacket64272 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64272, 64272 >> 8, 64272 >> 16);
        doReturn(requestPacket64272).when(j1939).createRequestPacket(64272, 0x00);
        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        responses.add(response64272);
        obdModule0.set(response64272, 1);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64272));

        Packet requestPacket64273 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64273, 64273 >> 8, 64273 >> 16);
        doReturn(requestPacket64273).when(j1939).createRequestPacket(64273, 0x00);
        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        responses.add(response64273);
        obdModule0.set(response64273, 1);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64273));

        Packet requestPacket64274 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64274, 64274 >> 8, 64274 >> 16);
        doReturn(requestPacket64274).when(j1939).createRequestPacket(64274, 0x00);
        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x01, 0x04, 0x03, 0x04, 0x05, 0x04,
                                                                      0x07, 0x04, 0x09, 0x04, 0x0B, 0x04, 0x0D, 0x04,
                                                                      0x0F, 0x04, 0x11, 0x04, 0x13, 0x04, 0x15, 0x04,
                                                                      0x17, 0x04, 0x19, 0x04, 0x1B, 0x04, 0x1D, 0x04,
                                                                      0x1F, 0x04));
        // @formatter:on
        responses.add(response64274);
        obdModule0.set(response64274, 1);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64274));

        Packet requestPacket64275 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64275, 64275 >> 8, 64275 >> 16);
        doReturn(requestPacket64275).when(j1939).createRequestPacket(64275, 0x00);
        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0x20, 0x04, 0x21, 0x04, 0x23, 0x04, 0x25, 0x04,
                                                                      0x27, 0x04, 0x29, 0x04, 0x2B, 0x04, 0x2D, 0x04,
                                                                      0x2F, 0x04, 0x31, 0x04, 0x33, 0x04, 0x35, 0x04,
                                                                      0x37, 0x04, 0x39, 0x04, 0x3B, 0x04, 0x3D, 0x04,
                                                                      0x3F, 0x04));
        // @formatter:on
        responses.add(response64275);
        obdModule0.set(response64275, 1);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64275));

        Packet requestPacket64276 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64276, 64276 >> 8, 64276 >> 16);
        doReturn(requestPacket64276).when(j1939).createRequestPacket(64276, 0x00);
        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x40, 0x04, 0x41, 0x04, 0x43, 0x04, 0x45, 0x04,
                                                                      0x47, 0x04, 0x49, 0x04, 0x4B, 0x04, 0x4D, 0x04,
                                                                      0x4F, 0x04, 0x51, 0x04, 0x53, 0x04, 0x55, 0x04,
                                                                      0x57, 0x04, 0x59, 0x04, 0x5B, 0x04, 0x5D, 0x04,
                                                                      0x5F, 0x04));
        // @formatter:on
        responses.add(response64276);
        obdModule0.set(response64276, 1);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64276));

        Packet requestPacket64277 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64277, 64277 >> 8, 64277 >> 16);
        doReturn(requestPacket64277).when(j1939).createRequestPacket(64277, 0x00);
        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0x60, 0x04, 0x61, 0x04, 0x63, 0x04, 0x65, 0x04,
                                                                      0x67, 0x04, 0x69, 0x04, 0x6B, 0x04, 0x6D, 0x04,
                                                                      0x6F, 0x04, 0x71, 0x04, 0x73, 0x04, 0x75, 0x04,
                                                                      0x77, 0x04, 0x79, 0x04, 0x7B, 0x04, 0x7D, 0x04,
                                                                      0x7F, 0x04));
        // @formatter:on
        responses.add(response64277);
        obdModule0.set(response64277, 1);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64277));

        Packet requestPacket64278 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64278, 64278 >> 8, 64278 >> 16);
        doReturn(requestPacket64278).when(j1939).createRequestPacket(64278, 0x00);
        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        responses.add(response64278);
        obdModule0.set(response64278, 1);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64278));

        Packet requestPacket64279 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64279, 64279 >> 8, 64265 >> 16);
        doReturn(requestPacket64279).when(j1939).createRequestPacket(64279, 0x00);
        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        responses.add(response64279);
        obdModule0.set(response64279, 1);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64279));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).reportBroadcastPeriod(any(),
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
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator, atLeastOnce()).reportMessages(any(ResultsListener.class), eq(obdModule0));
        verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());
        verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
                                                                       any(ResultsListener.class),
                                                                       eq("6.2.17.2.a"));

        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,680 |       2,240 |      29,120 |" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,344 |       1,792 |      23,296 |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |         336 |         448 |       5,824 |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |         512 |         683 |       2,223 |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |         128 |         171 |      52,910 |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |       6,105 |       8,140 |      16,926 |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |         977 |       1,302 |     122,746 |" + NL;
        expected += "| Grid: Energy into battery, kWh |       7,082 |       9,442 |           0 |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;

        // assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Fuel Consumption Bins (NTFCV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Run Time Bins (NTEHV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Vehicle Distance Bins (NTVMV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Output Energy Bins (NTEEV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Out NOx Mass Bins (NTENV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime System Out NOx Mass Bins (NTSNV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Fuel Consumption Bins (NTFCEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Engine Run Time Bins (NTEHEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Vehicle Distance Bins (NTVMEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Engine Output Energy Bins (NTEEEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Fuel Consumption Bins (NTFCA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Run Time Bins (NTEHA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Vehicle Distance Bins (NTVMA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Output Energy Bins (NTEEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Out NOx Mass Bins (NTENA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour System Out NOx Mass Bins (NTSNA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Fuel Consumption Bins (NTFCS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Run Time Bins (NTEHS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Vehicle Distance Bins (NTVMS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Output Energy Bins (NTEES) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Out NOx Mass Bins (NTENS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour System Out NOx Mass Bins (NTSNS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

     @Test
    public void testRunObdPgnSupports12675Part1DiffPart2(){
        int supportedSpn = 12675;
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
        obdModule0.set(packet3, 1);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        obdModule0.set(packet8, 1);
        when(busService.readBus(eq(12), eq("6.2.17.1.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);
        obdModule0.set(packet1, 1);

        Packet requestPacket64252 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64252, 64252 >> 8, 64252 >> 16);
//        doReturn(requestPacket64252).when(j1939).createRequestPacket(64252, 0x00);
        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 1);
//        when(communicationsModule.request(eq(64252),
//                                          eq(0),
//                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64252));

        Packet requestPacket64258 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64258, 64258 >> 8, 64258 >> 16);
//        doReturn(requestPacket64258).when(j1939).createRequestPacket(64258, 0x00);
        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64258, 1);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64258));

        Packet requestPacket64259 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64259, 64259 >> 8, 64259 >> 16);
//        doReturn(requestPacket64259).when(j1939).createRequestPacket(64259, 0x00);
        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64259, 1);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64259));

        Packet requestPacket64260 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64260, 64260 >> 8, 64260 >> 16);
//        doReturn(requestPacket64260).when(j1939).createRequestPacket(64260, 0x00);
        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64260, 1);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64260));

        Packet requestPacket64261 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64261, 64261 >> 8, 64261 >> 16);
//        doReturn(requestPacket64261).when(j1939).createRequestPacket(64261, 0x00);
        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64261, 1);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64261));

        Packet requestPacket64262 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64262, 64262 >> 8, 64262 >> 16);
//        doReturn(requestPacket64262).when(j1939).createRequestPacket(64262, 0x00);
        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        GenericPacket response64262PartOne = new GenericPacket(Packet.create(0xFB06, 0,
        // @formatter:off
                                                                             0xAA, 0xC0, 0x0D, 0xB0, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                             0x00, 0x00, 0x00, 0x00));

        obdModule0.set(response64262PartOne, 1);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64262));

        Packet requestPacket64263 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64263, 64263 >> 8, 64263 >> 16);
//        doReturn(requestPacket64263).when(j1939).createRequestPacket(64263, 0x00);
        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64263, 1);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64263));

        Packet requestPacket64264 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64264, 64264 >> 8, 64264 >> 16);
//        doReturn(requestPacket64264).when(j1939).createRequestPacket(64264, 0x00);
        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64264, 1);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64264));

        Packet requestPacket64265 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64265, 64265 >> 8, 64265 >> 16);
//        doReturn(requestPacket64265).when(j1939).createRequestPacket(64265, 0x00);
        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0D,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64265, 1);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64265));

        Packet requestPacket64266 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64266, 64266 >> 8, 64266 >> 16);
//        doReturn(requestPacket64266).when(j1939).createRequestPacket(64266, 0x00);
        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64266, 1);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64266));

        Packet requestPacket64267 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64267, 64267 >> 8, 64267 >> 16);
//        doReturn(requestPacket64267).when(j1939).createRequestPacket(64267, 0x00);
        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0xBD, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64267, 1);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64267));

        Packet requestPacket64268 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64268, 64268 >> 8, 64268 >> 16);
//        doReturn(requestPacket64268).when(j1939).createRequestPacket(64268, 0x00);
        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0,
        // @formatter:off
                                                                      0x00, 0x84, 0x01, 0x84, 0x03, 0x84, 0x05, 0x84,
                                                                      0x07, 0x84, 0x09, 0x84, 0x0B, 0x84, 0x0D, 0x84,
                                                                      0x0F, 0x84, 0x11, 0x84, 0x13, 0x84, 0x15, 0x84,
                                                                      0x17, 0x84, 0x19, 0x84, 0x1B, 0x84, 0x1D, 0x84,
                                                                      0x1F, 0x84));
        // @formatter:on
        obdModule0.set(response64268, 1);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64268));

        Packet requestPacket64269 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64269, 64269 >> 8, 64269 >> 16);
//        doReturn(requestPacket64269).when(j1939).createRequestPacket(64269, 0x00);
        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x20, 0x84, 0x21, 0x84, 0x23, 0x84, 0x25, 0x84,
                                                                      0x27, 0x84, 0x29, 0x84, 0x2B, 0x84, 0x2D, 0x84,
                                                                      0x2F, 0x84, 0x31, 0x84, 0x33, 0x84, 0x35, 0x84,
                                                                      0x37, 0x84, 0x39, 0x84, 0x3B, 0x84, 0x3D, 0x84,
                                                                      0x3F, 0x8));
        // @formatter:on
        obdModule0.set(response64269, 1);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64269));

        Packet requestPacket64270 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64270, 64270 >> 8, 64270 >> 16);
//        doReturn(requestPacket64270).when(j1939).createRequestPacket(64270, 0x00);
        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0x40, 0x84, 0x41, 0x84, 0x43, 0x84, 0x45, 0x84,
                                                                      0x47, 0x84, 0x49, 0x84, 0x4B, 0x84, 0x4D, 0x84,
                                                                      0x4F, 0x84, 0x51, 0x84, 0x53, 0x84, 0x55, 0x84,
                                                                      0x57, 0x84, 0x59, 0x84, 0x5B, 0x84, 0x5D, 0x84,
                                                                      0x5F, 0x84));
        // @formatter:on
        obdModule0.set(response64270, 1);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64270));

        Packet requestPacket64271 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64271, 64271 >> 8, 64271 >> 16);
//        doReturn(requestPacket64271).when(j1939).createRequestPacket(64271, 0x00);
        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0x60, 0x84, 0x61, 0x84, 0x63, 0x84, 0x65, 0x84,
                                                                      0x67, 0x84, 0x69, 0x84, 0x6B, 0x84, 0x6D, 0x84,
                                                                      0x6F, 0x84, 0x71, 0x84, 0x73, 0x84, 0x75, 0x84,
                                                                      0x77, 0x84, 0x79, 0x84, 0x7B, 0x84, 0x7D, 0x84,
                                                                      0x7F, 0x84));
        // @formatter:on
        obdModule0.set(response64271, 1);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64271));

        Packet requestPacket64272 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64272, 64272 >> 8, 64272 >> 16);
//        doReturn(requestPacket64272).when(j1939).createRequestPacket(64272, 0x00);
        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 1);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64272));

        Packet requestPacket64273 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64273, 64273 >> 8, 64273 >> 16);
//        doReturn(requestPacket64273).when(j1939).createRequestPacket(64273, 0x00);
        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 1);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64273));

        Packet requestPacket64274 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64274, 64274 >> 8, 64274 >> 16);
//        doReturn(requestPacket64274).when(j1939).createRequestPacket(64274, 0x00);
        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x01, 0x04, 0x03, 0x04, 0x05, 0x04,
                                                                      0x07, 0x04, 0x09, 0x04, 0x0B, 0x04, 0x0D, 0x04,
                                                                      0x0F, 0x04, 0x11, 0x04, 0x13, 0x04, 0x15, 0x04,
                                                                      0x17, 0x04, 0x19, 0x04, 0x1B, 0x04, 0x1D, 0x04,
                                                                      0x1F, 0x04));
        // @formatter:on
        obdModule0.set(response64274, 1);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64274));

        Packet requestPacket64275 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64275, 64275 >> 8, 64275 >> 16);
//        doReturn(requestPacket64275).when(j1939).createRequestPacket(64275, 0x00);
        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0x20, 0x04, 0x21, 0x04, 0x23, 0x04, 0x25, 0x04,
                                                                      0x27, 0x04, 0x29, 0x04, 0x2B, 0x04, 0x2D, 0x04,
                                                                      0x2F, 0x04, 0x31, 0x04, 0x33, 0x04, 0x35, 0x04,
                                                                      0x37, 0x04, 0x39, 0x04, 0x3B, 0x04, 0x3D, 0x04,
                                                                      0x3F, 0x04));
        // @formatter:on
        obdModule0.set(response64275, 1);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64275));

        Packet requestPacket64276 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64276, 64276 >> 8, 64276 >> 16);
//        doReturn(requestPacket64276).when(j1939).createRequestPacket(64276, 0x00);
        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x40, 0x04, 0x41, 0x04, 0x43, 0x04, 0x45, 0x04,
                                                                      0x47, 0x04, 0x49, 0x04, 0x4B, 0x04, 0x4D, 0x04,
                                                                      0x4F, 0x04, 0x51, 0x04, 0x53, 0x04, 0x55, 0x04,
                                                                      0x57, 0x04, 0x59, 0x04, 0x5B, 0x04, 0x5D, 0x04,
                                                                      0x5F, 0x04));
        // @formatter:on
        obdModule0.set(response64276, 1);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64276));

        Packet requestPacket64277 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64277, 64277 >> 8, 64277 >> 16);
//        doReturn(requestPacket64277).when(j1939).createRequestPacket(64277, 0x00);
        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0x60, 0x04, 0x61, 0x04, 0x63, 0x04, 0x65, 0x04,
                                                                      0x67, 0x04, 0x69, 0x04, 0x6B, 0x04, 0x6D, 0x04,
                                                                      0x6F, 0x04, 0x71, 0x04, 0x73, 0x04, 0x75, 0x04,
                                                                      0x77, 0x04, 0x79, 0x04, 0x7B, 0x04, 0x7D, 0x04,
                                                                      0x7F, 0x04));
        // @formatter:on
        obdModule0.set(response64277, 1);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64277));

        Packet requestPacket64278 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64278, 64278 >> 8, 64278 >> 16);
//        doReturn(requestPacket64278).when(j1939).createRequestPacket(64278, 0x00);
        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 1);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64278));

        Packet requestPacket64279 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64279, 64279 >> 8, 64265 >> 16);
//        doReturn(requestPacket64279).when(j1939).createRequestPacket(64279, 0x00);
        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 1);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64279));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));
        when(broadcastValidator.buildPGNPacketsMap(packets)).thenReturn(packetMap);

        Bus busMock = mock(Bus.class);
//        when(j1939.getBus()).thenReturn(busMock);
//        when(busMock.imposterDetected()).thenReturn(false);

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(broadcastValidator).getMaximumBroadcastPeriod();
        verify(broadcastValidator).buildPGNPacketsMap(any());
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0),
                                                                    any(),
                                                                    any(),
                                                                    any(),
                                                                    any(ResultsListener.class),
                                                                    eq(2),
                                                                    eq(17),
                                                                    eq("6.2.17.5.a"));

        verify(broadcastValidator).reportBroadcastPeriod(any(),
                                                         any(),
                                                         any(ResultsListener.class),
                                                         eq(2),
                                                         eq(17));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.2.17.1.c");
        verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        verify(busService, atLeastOnce()).getPGNsForDSRequest(eq(List.of()),
                                                              eq(supportedSpns.subList(1, supportedSpns.size())));
        verify(busService).getPGNsForDSRequest(any(), any());

        verify(tableA1Validator).reportMessages(any(ResultsListener.class), eq(obdModule0));
        packets.forEach(packet -> {
            verify(tableA1Validator).reportNotAvailableSPNs(eq(packet), any(ResultsListener.class), eq("6.2.17.2.a"));
            verify(tableA1Validator).reportImplausibleSPNValues(eq(packet), any(ResultsListener.class), eq(true), eq("6.2.17.2.b"));
            verify(tableA1Validator).reportNonObdModuleProvidedSPNs(eq(packet), any(ResultsListener.class), eq("6.2.17.2.c"));
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq("6.2.17.4.a"));
        });
        verify(tableA1Validator).reportDuplicateSPNs(any(), any(ResultsListener.class), eq("6.2.17.2.d"));
         verify(tableA1Validator).reportDuplicateSPNs(any(),
                                              any(ResultsListener.class),
                                              eq("6.2.17.6.g"));

        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        // @formatter:off
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "" + NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            0 |            0 |  218,103,808 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            0 |            0 |        1,024 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |          189 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |            0 |            0 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on

        // assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Fuel Consumption Bins (NTFCV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Run Time Bins (NTEHV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Vehicle Distance Bins (NTVMV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Output Energy Bins (NTEEV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime Engine Out NOx Mass Bins (NTENV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Valid NOx Lifetime System Out NOx Mass Bins (NTSNV) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Fuel Consumption Bins (NTFCEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Engine Run Time Bins (NTEHEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Vehicle Distance Bins (NTVMEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Engine Activity Lifetime Engine Output Energy Bins (NTEEEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Fuel Consumption Bins (NTFCA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Run Time Bins (NTEHA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Vehicle Distance Bins (NTVMA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Output Energy Bins (NTEEA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour Engine Out NOx Mass Bins (NTENA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Active 100 Hour System Out NOx Mass Bins (NTSNA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Fuel Consumption Bins (NTFCS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Run Time Bins (NTEHS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Vehicle Distance Bins (NTVMS) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Output Energy Bins (NTEES) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour Engine Out NOx Mass Bins (NTENS) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting NOx Tracking Stored 100 Hour System Out NOx Mass Bins (NTSNS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

}
