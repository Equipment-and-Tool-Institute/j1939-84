/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
