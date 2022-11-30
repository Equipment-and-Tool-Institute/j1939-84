/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
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
public class Part11Step13ControllerTest12691 extends AbstractControllerTest {
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
    public void testRunObdPgnSupports12691ActiveFailureThirteenTwelveDAndE() {
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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));

        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();
        verify(communicationsModule).request(eq(64255), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64256), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64257), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |         N/A |         N/A |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
               ;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12691StoredFailureThirteenTwelveD() {
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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
//                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        dataRepository.putObdModule(obdModule0);

        runTest();
        verify(communicationsModule).request(eq(64255), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64256), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64257), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of stored labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Active labels received is not a subset of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Stored labels received is not a subset of lifetime labels"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        String expectedMsg = "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12691StoredFailureThirteenTwelveE() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x44, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Stored labels received is not a subset of lifetime labels"));
        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 44                          |         N/A |         N/A |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |         N/A |         N/A |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                + "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL               
                ;
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
    public void testRunObdPgnSupports12691ActiveFailureThirteenTwelveE() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04, 0x06, 0x7D, 0x60, 0x10, 
                                                                      0x00, 0xC0, 0xBC, 0x05, 0x00, 0x04, 0xCE, 0x31, 0x02, 
                                                                      0x00, 0x02, 0x49, 0x1D, 0x00, 0x00, 0xE0, 0x79, 0x00, 
                                                                      0x00, 0xF9, 0x86, 0xAD, 0x00, 0x00, 0xA8, 0xD2, 0x02, 
                                                                      0x00, 0xF7, 0x4B, 0xC3, 0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x29, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Active labels received is not a subset of lifetime labels"));
        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 29                          |           0 |       8,638 |         N/A |         N/A |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |         N/A |         N/A |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL;
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
    public void testRunObdPgnSupports12691() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                + "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
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
    public void testRunObdPgnSupports12691ThirteenTwelveI() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x04, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.i - Active Tech vehicle distance received is => 0.25km from Engine #1 (0) for SPN 12696, GHG Tracking Active 100 Hour Active Technology Vehicle Distance: 1.000 km"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |         207 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691ThirteenTwelveH() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xF2, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        GenericPacket response64256Part2 = new GenericPacket(Packet.create(0xFB00,
                                                                           0x00,
                                                                           // @formatter:off
                                                                      0xF5, 0x0B, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256Part2, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.h - Active Tech time received is > part 2 value + 600 seconds from Engine #1 (0) for SPN 12695, GHG Tracking Active 100 Hour Active Technology Time: 2420.000 s"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          40 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691WarningThirteenTwelveG() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        GenericPacket response64256Part2 = new GenericPacket(Packet.create(0xFB00,
                                                                           0x00,
                                                                           // @formatter:off
                                                                      0xF5, 0x33, 0x09, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256Part2, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.g - Active Tech time received is greater than part 2 value from Engine #1 (0) for SPN 12695, GHG Tracking Active 100 Hour Active Technology Time: 2190.000 s"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.g - Active Tech time received is greater than part 2 value from Engine #1 (0) for SPN 12696, GHG Tracking Active 100 Hour Active Technology Vehicle Distance: 0.000 km"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691WarningThirteenTwelveF() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        GenericPacket response64256Part2 = new GenericPacket(Packet.create(0xFB00,
                                                                           0x00,
                                                                           // @formatter:off
                                                                      0xF6, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256Part2, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.g - Active Tech time received is greater than part 2 value from Engine #1 (0) for SPN 12694, GHG Tracking Active 100 Hour Active Technology Index: Mfg Defined Active Technology 6"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691WarningThirteenTwelveC() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xFC, 0xFF, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xFC, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.d - Active Technology value received is greater than 0xFAFF(h) and less than 0xFFFF(h) from Engine #1 (0) for SPN 12698, GHG Tracking Stored 100 Hour Active Technology Time: Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          42 |           0 |         N/A |           0 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691FailureThirteenTwelveC() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xFD, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.c - Active Technology value received is greater than 0xFA(h) from Engine #1 (0) for SPN 12694, GHG Tracking Active 100 Hour Active Technology Index: Unknown FD"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Active labels received is not a subset of lifetime labels"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |         N/A |         N/A |          36 |          46 |   1,319,462 |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691FailureThirteenTwelveA() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.a - No response was received from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of stored labels received differs from the number of lifetime labels"));

        assertEquals("", listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12691WarningThirteenTwelveB() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.12.b - No response was received from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of stored labels received differs from the number of lifetime labels"));

        assertEquals("", listener.getResults());

        String expectedMsg = "";
        expectedMsg += "Requesting Green House Gas Lifetime Active Technology Tracking (GHGTTL) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Active 100 Hour Active Technology Tracking (GHGTTA) from Engine #1 (0)"
                + NL;
        expectedMsg += "Requesting Green House Gas Stored 100 Hour Active Technology Tracking (GHGTTS) from Engine #1 (0)";
        assertEquals(expectedMsg, listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12691FailureThirteenTenC() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xFD, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.10.c - Index value received is greater than 0xFA(h) from Engine #1 (0) for SPN 12691, GHG Tracking Lifetime Active Technology Index: Unknown FD"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Active labels received is not a subset of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Stored labels received is not a subset of lifetime labels"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |         N/A |         N/A |" + NL
               ;
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
    public void testRunObdPgnSupports12691FailureThirteenTenB() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xFD, 0xFF, 0xFF, 0xFF,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64257));

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.10.b - Bin value received is greater than 0xFAFFFFFF(h) from Engine #1 (0) for SPN 12692, GHG Tracking Lifetime Active Technology Time: Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |       6,267 |     184,092 |" + NL
                + "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL
                + "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL
                + "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL
                + "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL
                + "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL
                + "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL
                + "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL
                + "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |         N/A |   1,373,795 |" + NL
                ;
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
    public void testRunObdPgnSupports12691WarningThirteenTenA() {
        final int supportedSpnNum = 12691;

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

        GenericPacket response64257 = new GenericPacket(Packet.create(0xFB01,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x04,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00

        ));
        // @formatter:on
        obdModule0.set(response64257, 2);

        when(communicationsModule.request(eq(64257),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64255 = new GenericPacket(Packet.create(0xFAFF,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0xB8, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64255, 2);
        when(communicationsModule.request(eq(64255),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64255));

        GenericPacket response64256 = new GenericPacket(Packet.create(0xFB00,
                                                                      0x00,
                                                                      // @formatter:off
                                                                      0xF5, 0xDB, 0x00, 0x00, 0x00,
                                                                      0x06, 0x7D, 0x60, 0x10, 0x00,
                                                                      0xC0, 0xBC, 0x05, 0x00, 0x04,
                                                                      0xCE, 0x31, 0x02, 0x00, 0x02,
                                                                      0x49, 0x1D, 0x00, 0x00, 0xE0,
                                                                      0x79, 0x00, 0x00, 0xF9, 0x86,
                                                                      0xAD, 0x00, 0x00, 0xA8, 0xD2,
                                                                      0x02, 0x00, 0xF7, 0x4B, 0xC3,
                                                                      0x00, 0xF5, 0xD0, 0xB3, 0x00
        ));
        // @formatter:on
        obdModule0.set(response64256, 2);
        when(communicationsModule.request(eq(64256),
                                          eq(0x00),
                                          any(ResultsListener.class))).thenAnswer(answer -> BusResult.of(response64256));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64256),
                                             eq(0x00),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64255),
                                             eq(0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(64257),
                                             eq(0),
                                             any(ResultsListener.class));

        // verify(mockListener).addOutcome(eq(11), eq(13), eq(FAIL), eq("6.11.13.10.h - "));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.10.a - No response was received from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of active labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.e - Number of stored labels received differs from the number of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Active labels received is not a subset of lifetime labels"));
        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.12.f - Stored labels received is not a subset of lifetime labels"));

        // @formatter:off
        String expected = "10:15:30.0000 GHG Active Technology Arrays from Engine #1 (0)" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "|                                     |    Active   |    Active   |    Stored   |    Stored   |             |             |" + NL;
        expected += "| Index                               |   100 Hour  |   100 Hour  |   100 Hour  |   100 Hour  |   Lifetime  |   Lifetime  |" + NL;
        expected += "| Description                         |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |    Time, m  |   Dist, km  |" + NL;
        expected += "|-------------------------------------+-------------+-------------+-------------+-------------+-------------+-------------|" + NL;
        expected += "| SAE/ISO Reserved                    |       8,916 |          45 |       8,916 |          45 |         N/A |         N/A |" + NL;
        expected += "| Cylinder Deactivation               |      10,539 |      12,499 |      10,539 |      12,499 |         N/A |         N/A |" + NL;
        expected += "| Predictive Cruise Control           |       4,117 |           4 |       4,117 |           4 |         N/A |         N/A |" + NL;
        expected += "| Unknown 49                          |           5 |      14,336 |           5 |      14,336 |         N/A |         N/A |" + NL;
        expected += "| Unknown 79                          |           0 |       8,638 |           0 |       8,638 |         N/A |         N/A |" + NL;
        expected += "| Unknown AD                          |           0 |      13,482 |           0 |      13,482 |         N/A |         N/A |" + NL;
        expected += "| Unknown C0                          |         245 |         256 |         245 |         256 |         N/A |         N/A |" + NL;
        expected += "| Unknown CE                          |          94 |         128 |          94 |         128 |         N/A |         N/A |" + NL;
        expected += "| Mfg Defined Active Technology 6     |          36 |           0 |          36 |          46 |         N/A |         N/A |" + NL;
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
}
