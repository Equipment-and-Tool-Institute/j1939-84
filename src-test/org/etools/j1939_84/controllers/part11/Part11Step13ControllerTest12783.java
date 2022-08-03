/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
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
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
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
public class Part11Step13ControllerTest12783 extends AbstractControllerTest {
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

    private StepController instance;

    private DataRepository dataRepository;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(new TestDateTimeModule());
        dataRepository = DataRepository.newInstance();
        GhgTrackingModule ghgTrackingModule = new GhgTrackingModule(DateTimeModule.getInstance());
        NOxBinningModule nOxBinningModule = new NOxBinningModule((DateTimeModule.getInstance()));

        instance = new Part11Step13Controller(executor,
                                              bannerModule,
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
    public void testRunObdPgnSupports12797() {
        final int supportedSpn = 12797;

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

        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
         obdModule0.set(response64241, 2);
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
         obdModule0.set(response64242, 2);
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
         obdModule0.set(response64243, 2);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));
        runTest();

        verify(communicationsModule).request(eq(64241), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64242), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64243), eq(0x00), any(CommunicationsListener.class));

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
    public void testRunObdPgnSupports12797TFailureThirteenSixteenD() {
        final int supportedSpn = 12797;

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

        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        obdModule0.set(response64241, 2);
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

        GenericPacket response64242Part2 = new GenericPacket(Packet.create(0xFAF2,
                                                                      0x00,
                                                                      0xA0,
                                                                      0x8C,
                                                                      0x10,
                                                                      0x1E,
                                                                      0x68,
                                                                      0x5B,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        obdModule0.set(response64242Part2, 2);
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
        obdModule0.set(response64243, 2);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));
        runTest();

        verify(communicationsModule).request(eq(64241), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64242), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64243), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.16.d - Value received from Engine #1 (0) for SPN 12795, Hybrid Stored 100 Hour Idle Propulsion System Active Time: 36000.000 s in part 2 was greater than part 11 value"));

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
    public void testRunObdPgnSupports12797FailureThirteenSixteenC() {
        final int supportedSpn = 12797;

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

        GenericPacket response64241 = new GenericPacket(Packet.create(0xFAF1,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF0, 0x6A, 0x67, 0x01, 0xD8, 0x79, 0x43, 0x01,
                                                                      0x1C, 0x9F, 0xE9, 0x00));
        // @formatter:on
        obdModule0.set(response64241, 2);
        when(communicationsModule.request(eq(64241),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64241));

        GenericPacket response64242 = new GenericPacket(Packet.create(0xFAF2,
                                                                      0x00,
                                                                      0xA0,
                                                                      0x8C,
                                                                      0x10,
                                                                      0x0E,
                                                                      0xFC,
                                                                      0xFF,
                                                                      0xFF,
                                                                      0xFF));
        // @formatter:on
        obdModule0.set(response64242, 2);
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
        obdModule0.set(response64243, 2);
        when(communicationsModule.request(eq(64243),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64243));
        runTest();

        verify(communicationsModule).request(eq(64241), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64242), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64243), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.16.c - Bin value received is greater than 0xFAFF(h) from Engine #1 (0) for SPN 12796, Hybrid Stored 100 Hour Urban Propulsion System Active Time : Not Available"));

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
        expected += "| Prod: Prop urban active,   s   |       2,925 |         N/A |     255,177 |" + NL;
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
    public void testRunObdPgnSupports12783() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                    0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                    0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
                                                                    // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
                                                                    // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenTwentyD() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        GenericPacket response64245Part2 = new GenericPacket(Packet.create(0xFAF5, 0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x22, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245Part2, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
                                                                      // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.20.d - Value received from Engine #1 (0) for SPN 12777, Hybrid Stored 100 Hour Distance Traveled in Driver-Selectable Charge Increasing Operation : 448.000 km in part 2 was greater than part 11 value"));

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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenTwentyC() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
                                                                      // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0xFD, 0xFF, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.8.c - Bin value received is greater than 0xFAFF(h) and less than 0xFFFF(h) from Engine #1 (0) for SPN 12771, Hybrid Active 100 Hour Fuel Consumed in Driver-Selectable Charge Increasing Operation : Not Available"));

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
        expected += "| Fuel Consume: Drv-Sel In,  l   |         N/A |         171 |      52,910 |" + NL;
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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenTwentyA() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.20.a - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |      29,120 |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |      23,296 |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |       5,824 |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |       2,223 |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |      52,910 |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |      16,926 |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |     122,746 |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |           0 |" + NL;
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

    @Test
    public void testRunObdPgnSupports12783WarningThirteenTwentyB() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2022);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
                                                                      // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
                                                                      // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.20.b - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |      29,120 |" + NL;
        expected += "| Chg Depleting engine off,  km  |             |             |      23,296 |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |             |             |       5,824 |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |             |             |       2,223 |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |             |             |      52,910 |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |             |             |      16,926 |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |             |             |     122,746 |" + NL;
        expected += "| Grid: Energy into battery, kWh |             |             |           0 |" + NL;
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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenEighteenA() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.18.a - No response was received from Engine #1 (0) for PG 64244"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,680 |       2,240 |             |" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,344 |       1,792 |             |" + NL;
        expected += "| Drv-Sel Inc Operation,     km  |         336 |         448 |             |" + NL;
        expected += "| Fuel Consume: Chg Dep Op,  l   |         512 |         683 |             |" + NL;
        expected += "| Fuel Consume: Drv-Sel In,  l   |         128 |         171 |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-off,  kWh |       6,105 |       8,140 |             |" + NL;
        expected += "| Grid: Chg Dep Op eng-on,   kWh |         977 |       1,302 |             |" + NL;
        expected += "| Grid: Energy into battery, kWh |       7,082 |       9,442 |             |" + NL;
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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenEighteenB() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xFF, 0xFF, 0xFF, 0xFC, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
                                                                      // @formatter:on
        obdModule0.set(response64244, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.18.b - Bin value received is greater than 0xFAFFFFFF(h) from Engine #1 (0) for SPN 12783, Hybrid Lifetime Distance Traveled in Charge Depleting Operation with Engine off : 21223178235.000 m"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                                |    Active   |    Stored   |             |" + NL;
        expected += "|                                |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|--------------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Chg Depleting engine off,  km  |       1,680 |       2,240 |  21,223,178 |" + NL;
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

    @Test
    public void testRunObdPgnSupports12783FailureThirteenEighteenC() {
        final int supportedSpnNum = 12783;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2025);
        vehInfo.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehInfo);

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0x00);
        SupportedSPN supportedSpn = SupportedSPN.create(supportedSpnNum,
                                                        false,
                                                        true,
                                                        false,
                                                        false,
                                                        1);
        obdModule0.set(DM24SPNSupportPacket.create(0x00,
                                                   supportedSpn),
                       1);

        GenericPacket response64244 = new GenericPacket(Packet.create(0xFAF4,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x00, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on

        GenericPacket response64244Part2 = new GenericPacket(Packet.create(0xFAF4,
                                                                           0x00,
                                                                           // @formatter:off
                                                                      0x00, 0xDE, 0x58, 0x08, 0x00, 0x18, 0x47, 0x00,
                                                                      0x00, 0xC6, 0x11, 0x00, 0x5E, 0x11, 0x00, 0x00,
                                                                      0x5C, 0x9D, 0x01, 0x00, 0x1E, 0x42, 0x00, 0x00,
                                                                      0x7A, 0xDF, 0x01, 0x00));
        // @formatter:on
        obdModule0.set(response64244Part2, 2);

        when(communicationsModule.request(eq(64244),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64244));

        GenericPacket response64245 = new GenericPacket(Packet.create(0xFAF5, 0x00,
        // @formatter:off
                                                                      0x00, 0x23, 0x00, 0x1C, 0x00, 0x07, 0x56, 0x05,
                                                                      0x56, 0x01, 0xCC, 0x1F, 0x16, 0x05, 0xE2, 0x24));
        // @formatter:on
        obdModule0.set(response64245, 2);
        when(communicationsModule.request(eq(64245),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64245));

        GenericPacket response64246 = new GenericPacket(Packet.create(0xFAF6, 0x00,
        // @formatter:off
                                                                      0x40, 0x1A, 0x00, 0x15, 0x40, 0x05, 0x00, 0x04,
                                                                      0x00, 0x01, 0xD9, 0x17, 0xD1, 0x03, 0xAA, 0x1B));
        // @formatter:on
        obdModule0.set(response64246, 2);
        when(communicationsModule.request(eq(64246),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64246));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64244),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64245),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64246),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.18.c - Value received from Engine #1 (0) for SPN 12783, Hybrid Lifetime Distance Traveled in Charge Depleting Operation with Engine off : 29120000.000 m  in part 2 was greater than part 11 value"));

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
}
