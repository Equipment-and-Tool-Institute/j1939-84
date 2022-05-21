/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
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
public class Part11Step13ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 13;

    private static final int BUS_ADDR = 0xA5;

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

    private TestDateTimeModule dateTimeModule;

    private StepController instance;

    private TableA1Validator tableA1Validator;
    private GhgTrackingModule ghgTrackingModule;
    private NOxBinningModule nOxBinningModule;
    private DataRepository dataRepository;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(new TestDateTimeModule());
        dataRepository = DataRepository.newInstance();
        ghgTrackingModule = new GhgTrackingModule(DateTimeModule.getInstance());
        nOxBinningModule = new NOxBinningModule((DateTimeModule.getInstance()));

        instance = new Part11Step13Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule,
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
    public void testRunObdPgnSupports12691() throws BusException {
        final int supportedSpn = 12691;
        List<Integer> supportedSpns = Arrays.asList(supportedSpn);

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
        dataRepository.putObdModule(obdModule0);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);

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
        when(communicationsModule.request(eq(64257),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64257));

        Packet requestPacket64255 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64255, 64255 >> 8, 64255 >> 16);
        // doReturn(requestPacket64255).when(j1939).createRequestPacket(64255, 0x00);
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
        responses.add(response64255);
        when(communicationsModule.request(eq(64255),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64255));

        Packet requestPacket64256 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64256, 64256 >> 8, 64256 >> 16);
        // doReturn(requestPacket64256).when(j1939).createRequestPacket(64256, 0x00);
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
        responses.add(response64256);
        when(communicationsModule.request(eq(64256),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64256));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(CommunicationsListener.class));
        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |    Time, s  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |           0 |           0 |           0 |           0 |  48,521,732 |           0 |" + NL;
        expected += "| Predictive Cruise Control           |         N/A |         N/A |         N/A |         N/A |      17,888 |           0 |" + NL;
        expected += "| Unknown 49                          |         N/A |         N/A |         N/A |         N/A |       2,185 |           0 |" + NL;
        expected += "| Unknown C0                          |         N/A |         N/A |         N/A |         N/A |   1,118,506 |           0 |" + NL;
        expected += "| Unknown CE                          |         N/A |         N/A |         N/A |         N/A |     559,250 |           0 |" + NL;
        expected += "| Unknown E0                          |         N/A |         N/A |         N/A |         N/A |           2 |           0 |" + NL;
        expected += "| Mfg Defined Active Technology 6     |         N/A |         N/A |         N/A |         N/A |         767 |           0 |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    // @Test
    public void testRunObdPgnSupports12797() throws BusException {
        final int supportedSpn = 12797;
        List<Integer> supportedSpns = Arrays.asList(supportedSpn);

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

        // when(broadcastValidator.getMaximumBroadcastPeriod()).thenReturn(3);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);
        // when(busService.readBus(eq(12), eq("6.2.17.1.a"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);
        // 64262, 64263, 64264, 64265, 64266, 64267

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        responses.add(response64241);
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
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64243));

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

        // verify(broadcastValidator).getMaximumBroadcastPeriod();
        // verify(broadcastValidator).buildPGNPacketsMap(packets);
        // verify(broadcastValidator).reportBroadcastPeriod(eq(packetMap),
        // any(),
        // any(ResultsListener.class),
        // eq(2),
        // eq(17));
        // packets.forEach(packet -> {
        // verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(packet.getSourceAddress()),
        // any(),
        // eq(Collections.emptyList()),
        // eq(Collections.emptyList()),
        // any(ResultsListener.class),
        // eq(2),
        // eq(17),
        // eq("6.2.17.2.a"));
        // });
        // verify(busService).setup(eq(j1939), any(ResultsListener.class));
        // verify(busService).readBus(12, "6.2.17.1.a");
        // verify(busService).collectNonOnRequestPGNs(supportedSpns.subList(1, supportedSpns.size()));
        // verify(busService).getPGNsForDSRequest(eq(List.of()), eq(supportedSpns.subList(1, supportedSpns.size())));
        // verify(busService).getPGNsForDSRequest(any(), any());

        // verify(tableA1Validator, atLeastOnce()).reportExpectedMessages(any());
        // verify(tableA1Validator, atLeastOnce()).reportNotAvailableSPNs(any(),
        // any(ResultsListener.class),
        // any());
        // verify(tableA1Validator, atLeastOnce()).reportImplausibleSPNValues(any(),
        // any(ResultsListener.class),
        // eq(true),
        // any());
        // verify(tableA1Validator, atLeastOnce()).reportNonObdModuleProvidedSPNs(any(),
        // any(ResultsListener.class),
        // any());
        // verify(tableA1Validator, atLeastOnce()).reportProvidedButNotSupportedSPNs(any(),
        // any(ResultsListener.class),
        // any());
        // verify(tableA1Validator, atLeastOnce()).reportPacketIfNotReported(any(),
        // any(ResultsListener.class),
        // eq(false));
        // verify(tableA1Validator, atLeastOnce()).reportDuplicateSPNs(any(), any(ResultsListener.class), any());

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

    // @Test
    public void testRunObdPgnSupports12675() throws BusException {
        int supportedSpn = 12675;
        List<Integer> supportedSpns = Arrays.asList(supportedSpn);

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

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

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
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64267));

        Packet requestPacket64268 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64268, 64268 >> 8, 64268 >> 16);
        doReturn(requestPacket64268).when(j1939).createRequestPacket(64268, 0x00);
        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64268);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64268));

        Packet requestPacket64269 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64269, 64269 >> 8, 64269 >> 16);
        doReturn(requestPacket64269).when(j1939).createRequestPacket(64269, 0x00);
        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64269);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64269));

        Packet requestPacket64270 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64270, 64270 >> 8, 64270 >> 16);
        doReturn(requestPacket64270).when(j1939).createRequestPacket(64270, 0x00);
        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64270);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64270));

        Packet requestPacket64271 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64271, 64271 >> 8, 64271 >> 16);
        doReturn(requestPacket64271).when(j1939).createRequestPacket(64271, 0x00);
        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64271);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64271));

        Packet requestPacket64272 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64272, 64272 >> 8, 64272 >> 16);
        doReturn(requestPacket64272).when(j1939).createRequestPacket(64272, 0x00);
        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        responses.add(response64272);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64272));

        Packet requestPacket64273 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64273, 64273 >> 8, 64273 >> 16);
        doReturn(requestPacket64273).when(j1939).createRequestPacket(64273, 0x00);
        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        responses.add(response64273);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64273));

        Packet requestPacket64274 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64274, 64274 >> 8, 64274 >> 16);
        doReturn(requestPacket64274).when(j1939).createRequestPacket(64274, 0x00);
        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64274);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64274));

        Packet requestPacket64275 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64275, 64275 >> 8, 64275 >> 16);
        doReturn(requestPacket64275).when(j1939).createRequestPacket(64275, 0x00);
        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64275);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64275));

        Packet requestPacket64276 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64276, 64276 >> 8, 64276 >> 16);
        doReturn(requestPacket64276).when(j1939).createRequestPacket(64276, 0x00);
        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64276);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64276));

        Packet requestPacket64277 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64277, 64277 >> 8, 64277 >> 16);
        doReturn(requestPacket64277).when(j1939).createRequestPacket(64277, 0x00);
        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00));
        // @formatter:on
        responses.add(response64277);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64277));

        Packet requestPacket64278 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64278, 64278 >> 8, 64278 >> 16);
        doReturn(requestPacket64278).when(j1939).createRequestPacket(64278, 0x00);
        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        responses.add(response64278);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64278));

        Packet requestPacket64279 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64279, 64279 >> 8, 64265 >> 16);
        doReturn(requestPacket64279).when(j1939).createRequestPacket(64279, 0x00);
        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        responses.add(response64279);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64279));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));

        Bus busMock = mock(Bus.class);
        when(j1939.getBus()).thenReturn(busMock);
        when(busMock.imposterDetected()).thenReturn(false);

        runTest();
// @formatter:off
        String expected = "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
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
        assertEquals(expected, listener.getResults());

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
    public void testRunObdPgnSupports12783() throws BusException {
        final int supportedSpn = 12783;
        List<Integer> supportedSpns = Arrays.asList(supportedSpn);

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
        dataRepository.putObdModule(obdModule0);

        List<GenericPacket> packets = new ArrayList<>();

        GenericPacket packet3 = packet(supportedSpn, false, 0);
        packets.add(packet3);
        GenericPacket packet8 = packet(888, true, 0);
        packets.add(packet8);

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        Packet requestPacket64257 = Packet.create(0xEA00 | 0xFF, BUS_ADDR, true, 64257, 64257 >> 8, 64257 >> 16);
        // doReturn(requestPacket64257).when(j1939).createRequestPacket(64257, 0x00);

        List<GenericPacket> responses = new ArrayList<>();
        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        responses.add(response64244);

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
// @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        responses.add(response64245);

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
// @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        responses.add(response64246);

        when(communicationsModule.request(eq(64244),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64244));
        when(communicationsModule.request(eq(64245),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64245));
        when(communicationsModule.request(eq(64246),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> List.of(response64246));

        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = new HashMap<>();
        packetMap.put(11111, Map.of(0, List.of(packet1)));
        packetMap.put(33333, Map.of(0, List.of(packet3)));

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(CommunicationsListener.class));

// @formatter:off
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
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Lifetime Hours (HCDIOL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Active 100 Hours (HCDIOA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Hybrid Charge Depleting or Increasing Operation Stored 100 Hours (HCDIOS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    private static GenericPacket packet(int spnId, Boolean isNotAvailable, int sourceAddress) {
        GenericPacket mock = mock(GenericPacket.class);

        Spn spn = mock(Spn.class);
        // when(spn.getId()).thenReturn(spnId);
        // when(mock.getSourceAddress()).thenReturn(sourceAddress);
        // if (isNotAvailable != null) {
        // when(spn.isNotAvailable()).thenReturn(isNotAvailable);
        // }
        // when(mock.getSpns()).thenReturn(List.of(spn));

        return mock;
    }

}
