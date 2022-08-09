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
public class Part11Step13ControllerTest12730 extends AbstractControllerTest {
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
    public void testRunObdPgnSupports12730() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
        // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));


        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |           0 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }



    @Test
    public void testRunObdPgnSupports12730WarningThirteenEightG() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(INFO), eq("6.11.13.8.g - Active Tech EOE received is > 1.0 kW-hr from Engine #1 (0) for SPN 12704, GHG Tracking Active 100 Hour Engine Output Energy: 14170.000 kWh"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |      14,170 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730WarningThirteenEightE() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x08, 0x00, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(WARN), eq("6.11.13.8.e - Active 100 hrs engine hours SPN 12700 received is < 600 seconds from Engine #1 (0) for GHG Tracking Active 100 Hour Engine Run Time"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |           1 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730WarningThirteenEightC() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0xFF, 0xFF, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xFD, 0xFF, 0xFF, 0xFF, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.8.c - Bin value received is greater than 0xFAFF(h) and less than 0xFFFF(h) from Engine #1 (0) for SPN 12707, GHG Tracking Active 100 Hour Idle Run Time: Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |         N/A |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |           0 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         N/A |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |         N/A |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenEightA() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.8.a - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenEightB() {
        final int supportedSpnNum = 12730;

        var vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2023);
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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(WARN), eq("6.11.13.8.b - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "";
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenSixD() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x00, 0x00, 0x00, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.6.d - Lifetime engine hours SPN 12730 received is < 600 seconds from Engine #1 (0)"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |           3 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenSixC() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        GenericPacket response64252Part2 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x16, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252Part2, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.6.c - Value received from Engine #1 (0) for SPN 12734, GHG Tracking Lifetime Engine Output Energy: 1049476.000 kWh in part 2 was greater than part 11 value"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenEightD() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        GenericPacket response64253Part2 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1B, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253Part2, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.8.d - Value received from Engine #1 (0) for SPN 12724, GHG Tracking Stored 100 Hour Engine Power Take Off Run Time: 67500.000 s in part 2 was greater than part 11 value"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenSixB() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0xFD, 0xFF, 0xFF, 0xFF, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.6.b - Bin value received is greater than 0xFAFFFFFF(h) and less than 0xFFFFFFFF(h)from Engine #1 (0) for SPN 12734, GHG Tracking Lifetime Engine Output Energy: Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |         N/A |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenSixA() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
                                                                      // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x5A, 0x37, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64253, 2);
        when(communicationsModule.request(eq(64253),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
                                                                      // @formatter:off
                                                                      0x78, 0x69, 0x00, 0x00, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        obdModule0.set(response64254,2);
        when(communicationsModule.request(eq(64254),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.6.a - No response was received from Engine #1 (0) for PG 64252"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |       4,500 |       4,500 |             |" + NL;
        expected += "| Vehicle Dist., km       |           0 |       7,053 |             |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 |             |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |             |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |      14,170 |             |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |             |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |             |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |             |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |             |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |             |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |             |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |             |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |             |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12730FailureThirteenEightF() {
        final int supportedSpnNum = 12730;

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

        GenericPacket response64252 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        GenericPacket response64252Part2 = new GenericPacket(Packet.create(0xFAFC, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64252Part2, 2);
        when(communicationsModule.request(eq(64252),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64252));

        GenericPacket response64253 = new GenericPacket(Packet.create(0xFAFD, 0x00,
        // @formatter:off
                                                                      0x78, 0x69, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on

        obdModule0.set(response64253, 2);

        when(communicationsModule.request(eq(64253),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64253));

        GenericPacket response64254 = new GenericPacket(Packet.create(0xFAFE, 0x00,
        // @formatter:off
                                                                      0x58, 0x00, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on
        GenericPacket response64254Part2 = new GenericPacket(Packet.create(0xFAFE, 0x00,
        // @formatter:off
                                                                      0x08, 0x00, 0x34, 0x6E, 0x12, 0x0B, 0xFE, 0x0A,
                                                                      0x00, 0x00, 0xFF, 0xC1, 0x02, 0x00, 0x8D, 0x27,
                                                                      0xA3, 0x02, 0x0C, 0x00, 0x5E, 0x1A, 0x76, 0x00,
                                                                      0x05, 0x00, 0x46, 0x05));
        // @formatter:on

        obdModule0.set(response64254Part2, 2);

        when(communicationsModule.request(eq(64254),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(response64254));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64252),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64253),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64254),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.8.f - Active Tech vehicle distance received is => 0.25km from Engine #1 (0) for SPN 12701, GHG Tracking Active 100 Hour Vehicle Distance: 7053.000 km"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Tracking Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "|                         |    Active   |    Stored   |             |" + NL;
        expected += "|                         |   100 Hour  |   100 Hour  |   Lifetime  |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += "| Engine Run Time, s      |          15 |       4,500 |  23,112,963 |" + NL;
        expected += "| Vehicle Dist., km       |       7,053 |       7,053 |   1,229,474 |" + NL;
        expected += "| Vehicle Fuel, l         |       1,417 |       1,417 | 145,399,114 |" + NL;
        expected += "| Engine Fuel, l          |       1,407 |       1,407 |  44,236,800 |" + NL;
        expected += "| Eng.Out.Energy, kW-hr   |           0 |           0 |   1,049,476 |" + NL;
        expected += "| PKE Numerator           |     180,735 |     180,735 |  51,163,080 |" + NL;
        expected += "| Urban Speed Run Time, s |       1,688 |       1,688 |   1,966,080 |" + NL;
        expected += "| Idle Run Time, s        |         112 |         112 |           0 |" + NL;
        expected += "| Engine Idle Fuel, l     |           6 |           6 |           0 |" + NL;
        expected += "| PTO Run Time, s         |       1,125 |       1,125 |           0 |" + NL;
        expected += "| PTO Fuel Consumption, l |          59 |          59 |           0 |" + NL;
        expected += "| AES Shutdown Count      |           5 |           5 |           0 |" + NL;
        expected += "| Stop-Start Run Time, s  |         225 |         225 |           0 |" + NL;
        expected += "|-------------------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting GHG Tracking Lifetime Array Data (GHGTL) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Active 100 Hour Array Data (GHGTA) from Engine #1 (0)" + NL;
        expectedMsg += "Requesting GHG Tracking Stored 100 Hour Array Data (GHGTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }
}
