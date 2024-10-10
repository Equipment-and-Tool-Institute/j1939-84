/*
 * Copyright (c) 2024. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.modules.CSERSModule.CSERS_AVERAGE_PG;
import static org.etools.j1939tools.modules.CSERSModule.CSERS_CURRENT_OP_CYCLE_PG;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part01Step26ControllerTest22227 extends AbstractControllerTest {

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
        CSERSModule csersModule = new CSERSModule((DateTimeModule.getInstance()));

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
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        GenericPacket response64019 = new GenericPacket(Packet.create(CSERS_CURRENT_OP_CYCLE_PG,
                                                                      // @formatter:off
                                                                      0x00,
                                                                      0x00, 0x00, 0x00, 0x01, 0x00, 0x02, 0x00, 0x03,
                                                                      0x00, 0x04, 0x00, 0x05, 0x00, 0x06, 0x00, 0x07,
                                                                      0x00, 0x08, 0x00, 0x09));
        // @formatter:on
        obdModule0.set(response64019, 1);
        when(communicationsModule.request(eq(CSERS_CURRENT_OP_CYCLE_PG),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64019));

        GenericPacket response64020 = new GenericPacket(Packet.create(CSERS_AVERAGE_PG,
                                                                      // @formatter:off
                                                                      0x00,
                                                                      0x00, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x06,
                                                                      0x00, 0x08, 0x00, 0x0A, 0x00, 0x0C, 0x00, 0x0E,
                                                                      0x00, 0x10, 0x00, 0x12));
        // @formatter:on
        obdModule0.set(response64020, 1);
        when(communicationsModule.request(eq(CSERS_AVERAGE_PG),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64020));

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
                                                         eq(1),
                                                         eq(26));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0x00),
                                                                    any(),
                                                                    eq(Collections.emptyList()),
                                                                    eq(Collections.emptyList()),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(any(), any());

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
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.1.26.3.f"));
            verify(tableA1Validator).reportPacketIfNotReported(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq(false));
        });
        verify(tableA1Validator).reportDuplicateSPNs(eq(packets), any(ResultsListener.class), eq("6.1.26.3.e"));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()), any(ResultsListener.class), eq("6.1.26.6.d"));

        // @formatter:off
        String expected = "10:15:30.0000 CSERS Average and Current Operating Cycle data from Engine #1 (0)" + NL;
        expected += "|----------------------------------------------------------------------------+-------------+-------------|" + NL;
        expected += "|                                                                            |   Current   |   Average   |" + NL;
        expected += "|                                                                            |  Op. Cycle  |    Data     |" + NL;
        expected += "|                                                                            |    Data     |             |" + NL;
        expected += "|----------------------------------------------------------------------------+-------------+-------------|" + NL;
        expected += "| Heat Energy Until FTP Cold Start Tracking Time,                         kJ |           0 |           0 |" + NL;
        expected += "| Heat Energy Until FTP Engine Output Energy,                             kJ |         256 |         512 |" + NL;
        expected += "| Heat Energy Until Catalyst Cold Start Tracking Temperature Threshold,   kJ |         512 |       1,024 |" + NL;
        expected += "| Output Energy Until FTP Cold Start Tracking Time,                       kJ |       3,840 |       7,680 |" + NL;
        expected += "| Output Energy Until Catalyst Cold Start Tracking Temperature Threshold, kJ |       5,120 |      10,240 |" + NL;
        expected += "| EGR Mass Until FTP Cold Start Tracking Time,                            g  |       6,400 |      12,800 |" + NL;
        expected += "| EGR Mass Until FTP Engine Output Energy,                                g  |       7,680 |      15,360 |" + NL;
        expected += "| EGR Mass Until Cold Start Tracking Catalyst Temperature Threshold,      g  |       8,960 |      17,920 |" + NL;
        expected += "| Time Until FTP Engine Output Energy,                                   min |          34 |          68 |" + NL;
        expected += "| Time Until Catalyst Cold Start Tracking Temperature Threshold,         min |          38 |          77 |" + NL;
        expected += "|----------------------------------------------------------------------------+-------------+-------------+" + NL;

        expected += NL;
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Current Operating Cycle Data (CSERSC) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Average Data (CSERSA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
        // @formatter:on

    }

    @Test
    public void testRunObdPgnSupports22227Failure28a() {
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
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        GenericPacket response64019 = new GenericPacket(Packet.create(CSERS_CURRENT_OP_CYCLE_PG,
                                                                      // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64019, 1);
        when(communicationsModule.request(eq(CSERS_CURRENT_OP_CYCLE_PG),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64019));

        when(communicationsModule.request(eq(CSERS_AVERAGE_PG),
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
                                                         eq(1),
                                                         eq(26));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0x00),
                                                                    any(),
                                                                    eq(Collections.emptyList()),
                                                                    eq(Collections.emptyList()),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(any(), any());

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
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.1.26.3.f"));
            verify(tableA1Validator).reportPacketIfNotReported(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq(false));
        });
        verify(tableA1Validator).reportDuplicateSPNs(eq(packets), any(ResultsListener.class), eq("6.1.26.3.e"));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()), any(ResultsListener.class), eq("6.1.26.6.d"));

        String expectedMsg = "";
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Current Operating Cycle Data (CSERSC) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Average Data (CSERSA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());

        verify(mockListener).addOutcome(eq(1),
                                        eq(26),
                                        eq(FAIL),
                                        eq("6.1.26.28.a - No response was received from Engine #1 (0) for PGN [64020]"));

        // @formatter:on

    }


    @Test
    public void testRunObdPgnSupports22227Failure28aBothMissing() {
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
        when(busService.readBus(eq(12), eq("6.1.26.2.c"))).thenReturn(packets.stream());

        GenericPacket packet1 = packet(supportedSpn, false, 0);
        packets.add(packet1);

        // @formatter:on
        when(communicationsModule.request(eq(CSERS_CURRENT_OP_CYCLE_PG),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        when(communicationsModule.request(eq(CSERS_AVERAGE_PG),
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
                                                         eq(1),
                                                         eq(26));
        verify(broadcastValidator).collectAndReportNotAvailableSPNs(eq(0x00),
                                                                    any(),
                                                                    eq(Collections.emptyList()),
                                                                    eq(Collections.emptyList()),
                                                                    any(ResultsListener.class),
                                                                    eq(1),
                                                                    eq(26),
                                                                    eq("6.1.26.5.a"));

        verify(busService).setup(eq(j1939), any(ResultsListener.class));
        verify(busService).readBus(12, "6.1.26.2.c");
        verify(busService).collectNonOnRequestPGNs(eq(List.of()));
        verify(busService).getPGNsForDSRequest(eq(List.of()), eq(List.of()));
        verify(busService).getPGNsForDSRequest(any(), any());

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
            verify(tableA1Validator).reportProvidedButNotSupportedSPNs(eq(packet),
                                                                       any(ResultsListener.class),
                                                                       eq("6.1.26.3.f"));
            verify(tableA1Validator).reportPacketIfNotReported(eq(packet),
                                                               any(ResultsListener.class),
                                                               eq(false));
        });
        verify(tableA1Validator).reportDuplicateSPNs(eq(packets), any(ResultsListener.class), eq("6.1.26.3.e"));
        verify(tableA1Validator).reportDuplicateSPNs(eq(List.of()), any(ResultsListener.class), eq("6.1.26.6.d"));

        String expectedMsg = "";
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Current Operating Cycle Data (CSERSC) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting Cold Start Emissions Reduction Strategy Average Data (CSERSA) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());

        verify(mockListener).addOutcome(eq(1),
                                        eq(26),
                                        eq(FAIL),
                                        eq("6.1.26.28.a - No response was received from Engine #1 (0) for PGN [64019, 64020]"));

        // @formatter:on

    }

}
