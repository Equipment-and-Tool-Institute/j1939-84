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
import org.etools.j1939tools.modules.NOxBinningModuleTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step13ControllerTest12675 extends AbstractControllerTest {
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
    public void testRunObdPgnSupports12675() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03, 0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07, 0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675InfoThirteenFourK() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.4.k - Active 100 hr system out Nox 12321 received is = 0 seconds from Engine #1 (0) for NOx Tracking Active 100 Hour System Out NOx Mass Bin 1 (Total)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |            0 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675InfoThirteenFourJ() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.4.k - Active 100 hr system out Nox 12338 received is = 0 seconds from Engine #1 (0) for NOx Tracking Active 100 Hour Engine Out NOx Mass Bin 1 (Total)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |            0 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675InfoThirteenFourI() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.4.i - Active 100 hr EOE 12355 received is <= 0.5kW-hr from Engine #1 (0) for NOx Tracking Active 100 Hour Engine Output Energy Bin 1 (Total)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |            0 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675WarningThirteenFourG() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        GenericPacket response64277Part2 = new GenericPacket(Packet.create(0xFB15,
                                                                           0,
                                                                           // @formatter:off
                                                                           0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                           0xCD, 0x49, 0x54, 0xAD, 0x03, 0x01, 0xBC, 0x34,
                                                                           0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                           0x07, 0x00, 0x08, 0x07));
        // @formatter:on

        obdModule0.set(response64277Part2, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.4.g - Value received from Engine #1 (0) for SPN 12361, NOx Tracking Active 100 Hour Engine Output Energy Bin 7: 3.000 kWh in part 11 was less than part 2 value"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675WarningThirteenFourH() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.4.h - Active 100 hr vehicle distance bins received is => 0.25 km from Engine #1 (0) for SPN 12376, NOx Tracking Active 100 Hour Vehicle Distance Bin 5: 0.250 km"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureThirteenTwoE() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x3C, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
                                                                           // @formatter:off
                                                                           0x0D, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                           0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                           0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                           0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                           0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                           0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                           0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                           0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                           0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.2.e - Lifetime engine activity engine hours bin 1 (total) SP 12659 is < part 2 value + 600 seconds"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |            1 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675ThirteenTwoD() throws BusException {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x41, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
                                                                           // @formatter:off
                                                                           0x0D, 0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x0D,
                                                                           0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                           0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                           0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                           0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                           0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                           0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                           0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                           0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.2.d - Lifetime engine hours bin 1 (total) SP 12593 value is < part 2 value + 60 seconds"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |            1 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675ThirteenTwoC() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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

        GenericPacket response64262Part2 = new GenericPacket(Packet.create(0xFB06, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x1D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        obdModule0.set(response64262Part2, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.2.c - Value received from Engine #1 (0) for SPN 12620, NOx Tracking Valid NOx Lifetime Fuel Consumption Bin 11: 10905242.550 l in part 2 was greater than part 11 value"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureFourA() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.4.a - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureTwoB() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x0D, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0xFC, 0xFF, 0xFF, 0xFF,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.2.b - Bin value received is greater than 0xFAFFFFFF(h) from Engine #1 (0) for SPN 12613, NOx Tracking Valid NOx Lifetime Fuel Consumption Bin 4: Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |Not Available |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureThirteenTwoA() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.2.a - No response was received from Engine #1 (0)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureThirteenFourA() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.4.a - No response was received from Engine #1 (0)"));
        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675WarningThirteenFourB() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.empty());

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(WARN),
                                        eq("6.11.13.4.b - No response was received from Engine #1 (0)"));
        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureThirteenFourC() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0xFC, 0xFF));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.4.c - Bin value received is greater than 0xFAFF(h) from Engine #1 (0) for SPN 12402, NOx Tracking Active 100 Hour Engine Run Time Bin 14 : Not Available"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |Not Available |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675FailureThirteenFourD() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0x00, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        GenericPacket response64274Part2 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274Part2, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(FAIL),
                                        eq("6.11.13.4.d - Value received from Engine #1 (0) for SPN 12406, NOx Tracking Active 100 Hour Fuel Consumption Bin 1 (Total): 17920.000 l in part 2 was greater than part 11 value"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       17,920 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675InfoThirteenFourE() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0x04, 0x00, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.4.e - Active 100 hrs engine hours bin 1 SP 12389 value received is < 60 seconds from Engine #1 (0) for NOx Tracking Active 100 Hour Engine Run Time Bin 1 (Total)"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            1 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |            0 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }

    @Test
    public void testRunObdPgnSupports12675InfoThirteenFourF() {
        int supportedSpn = 12675;

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

        GenericPacket response64258 = new GenericPacket(Packet.create(0xFB02, 0x00,
        // @formatter:off
                                                                      0x40, 0x84, 0x08, 0x10, 0x41, 0x84, 0x00, 0x10,
                                                                      0x43, 0x84, 0x00, 0x10, 0x45, 0x84, 0x00, 0x10,
                                                                      0x47, 0x84, 0x00, 0x10, 0x49, 0x84, 0x00, 0x10,
                                                                      0x4B, 0x84, 0x00, 0x10, 0x4D, 0x84, 0x00, 0x10,
                                                                      0x4F, 0x84, 0x00, 0x10, 0x51, 0x84, 0x00, 0x10,
                                                                      0x53, 0x84, 0x00, 0x10, 0x55, 0x84, 0x00, 0x10,
                                                                      0x57, 0x84, 0x00, 0x10, 0x59, 0x84, 0x00, 0x10,
                                                                      0x5B, 0x84, 0x00, 0x10, 0x5D, 0x84, 0x00, 0x10,
                                                                      0x5F, 0x84, 0x00, 0x10));
        // @formatter:on
        obdModule0.set(response64258, 2);
        when(communicationsModule.request(eq(64258),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64258));

        GenericPacket response64259 = new GenericPacket(Packet.create(0xFB03, 0x00,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on

        GenericPacket response64259Part2 = new GenericPacket(Packet.create(0xFB03,
                                                                           0x00,
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

        obdModule0.set(response64259Part2, 2);
        when(communicationsModule.request(eq(64259),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64259));

        GenericPacket response64260 = new GenericPacket(Packet.create(0xFB04, 0,
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
        obdModule0.set(response64260, 2);
        when(communicationsModule.request(eq(64260),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64260));

        GenericPacket response64261 = new GenericPacket(Packet.create(0xFB05, 0,
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
        obdModule0.set(response64261, 2);
        when(communicationsModule.request(eq(64261),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64261));

        GenericPacket response64262 = new GenericPacket(Packet.create(0xFB06, 0,
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
        obdModule0.set(response64262, 2);
        when(communicationsModule.request(eq(64262),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64262));

        GenericPacket response64263 = new GenericPacket(Packet.create(0xFB07, 0,
        // @formatter:off
                                                                      0x00, 0x04, 0x00, 0x3C, 0x01, 0x04, 0x00, 0x0D,
                                                                      0x03, 0x04, 0x00, 0x0D, 0x05, 0x04, 0x00, 0x0D,
                                                                      0x07, 0x04, 0x00, 0x0D, 0x09, 0x04, 0x00, 0x0D,
                                                                      0x0B, 0x04, 0x00, 0x0D, 0x0D, 0x04, 0x00, 0x0D,
                                                                      0x0F, 0x04, 0x00, 0x0D, 0x11, 0x04, 0x00, 0x0D,
                                                                      0x13, 0x04, 0x00, 0x0D, 0x15, 0x04, 0x00, 0x0D,
                                                                      0x17, 0x04, 0x00, 0x0D, 0x19, 0x04, 0x00, 0x0D,
                                                                      0x1B, 0x04, 0x00, 0x0D, 0x1D, 0x04, 0x00, 0x0D,
                                                                      0x1F, 0x04, 0x00, 0x0D));
        // @formatter:on
        GenericPacket response64263Part2 = new GenericPacket(Packet.create(0xFB07,
                                                                           0,
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
        obdModule0.set(response64263Part2, 2);
        when(communicationsModule.request(eq(64263),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64263));

        GenericPacket response64264 = new GenericPacket(Packet.create(0xFB08, 0x00,
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
        obdModule0.set(response64264, 2);
        when(communicationsModule.request(eq(64264),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64264));

        GenericPacket response64265 = new GenericPacket(Packet.create(0xFB09, 0,
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
        obdModule0.set(response64265, 2);
        when(communicationsModule.request(eq(64265),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64265));

        GenericPacket response64266 = new GenericPacket(Packet.create(0xFB0A, 0,
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
        obdModule0.set(response64266, 2);
        when(communicationsModule.request(eq(64266),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64266));

        GenericPacket response64267 = new GenericPacket(Packet.create(0xFB0B, 0,
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
        obdModule0.set(response64267, 2);
        when(communicationsModule.request(eq(64267),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64267));

        GenericPacket response64268 = new GenericPacket(Packet.create(0xFB0C, 0x00,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64268, 2);
        when(communicationsModule.request(eq(64268),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64268));

        GenericPacket response64269 = new GenericPacket(Packet.create(0xFB0D, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x0D, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64269, 2);
        when(communicationsModule.request(eq(64269),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64269));

        GenericPacket response64270 = new GenericPacket(Packet.create(0xFB0E, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64270, 2);
        when(communicationsModule.request(eq(64270),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64270));

        GenericPacket response64271 = new GenericPacket(Packet.create(0xFB0F, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64271, 2);
        when(communicationsModule.request(eq(64271),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64271));

        GenericPacket response64272 = new GenericPacket(Packet.create(0xFB10, 0,
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
        obdModule0.set(response64272, 2);
        when(communicationsModule.request(eq(64272),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64272));

        GenericPacket response64273 = new GenericPacket(Packet.create(0xFB11, 0,
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
        obdModule0.set(response64273, 2);
        when(communicationsModule.request(eq(64273),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64273));

        GenericPacket response64274 = new GenericPacket(Packet.create(0xFB12, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64274, 2);
        when(communicationsModule.request(eq(64274),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64274));

        GenericPacket response64275 = new GenericPacket(Packet.create(0xFB13, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64275, 2);
        when(communicationsModule.request(eq(64275),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64275));

        GenericPacket response64276 = new GenericPacket(Packet.create(0xFB14, 0,
        // @formatter:off
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                                                      0x00, 0x00, 0x00, 0x00));
        // @formatter:on
        obdModule0.set(response64276, 2);
        when(communicationsModule.request(eq(64276),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64276));

        GenericPacket response64277 = new GenericPacket(Packet.create(0xFB15, 0,
        // @formatter:off
                                                                      0xA0, 0x8C, 0xA8, 0x52, 0xC2, 0x0E, 0xA8, 0x0E,
                                                                      0xCD, 0x49, 0x54, 0xAD, 0x03, 0x00, 0xBC, 0x34,
                                                                      0x84, 0x03, 0x10, 0x00, 0x28, 0x23, 0x9C, 0x00,
                                                                      0x07, 0x00, 0x08, 0x07));
        // @formatter:on
        obdModule0.set(response64277, 2);
        when(communicationsModule.request(eq(64277),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64277));

        GenericPacket response64278 = new GenericPacket(Packet.create(0xFB16, 0,
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
        obdModule0.set(response64278, 2);
        when(communicationsModule.request(eq(64278),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64278));

        GenericPacket response64279 = new GenericPacket(Packet.create(0xFB17, 0,
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
        obdModule0.set(response64279, 2);
        when(communicationsModule.request(eq(64279),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenAnswer(answer -> BusResult.of(
                                                                                                                response64279));

        dataRepository.putObdModule(obdModule0);

        runTest();

        verify(communicationsModule).request(eq(64258), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64259), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64260), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64261), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64262), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64263), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64264), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64265), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64266), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64267), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64268), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64269), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64270), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64271), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64272), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64273), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64274), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64275), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64276), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64277), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64278), eq(0x00), any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(64279), eq(0x00), any(CommunicationsListener.class));

        verify(mockListener).addOutcome(eq(11),
                                        eq(13),
                                        eq(INFO),
                                        eq("6.11.13.4.f - Active Tech stored engine hours received is > 0 seconds from Engine #1 (0) for NOx Tracking Stored 100 Hour Engine Run Time Bin 6"));

        // @formatter:off
        String expected = "10:15:30.0000 NOx Binning Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |  218,104,832 |  218,104,832 |   10,905,242 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |  218,104,833 |  218,104,833 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |  218,104,835 |  218,104,835 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |  218,104,837 |  218,104,837 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |  218,104,839 |  218,104,839 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |  218,104,841 |  218,104,841 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |  218,104,843 |  218,104,843 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |  218,104,845 |  218,104,845 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |  218,104,847 |  218,104,847 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |  218,104,849 |  218,104,849 |   10,905,242 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |  218,104,851 |  218,104,851 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |  218,104,853 |  218,104,853 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |  218,104,855 |  218,104,855 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |  218,104,857 |  218,104,857 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |  218,104,859 |  218,104,859 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |  218,104,861 |  218,104,861 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |  218,104,863 |  218,104,863 |   10,905,243 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Engine Activity Lifetime Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |  218,104,832 |   13,449,680 |   16,777,233 |    1,090,524 |" + NL;
        expected += "| Bin  2 (Idle)             |  218,104,833 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |  218,104,835 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |  218,104,837 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |  218,104,839 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |  218,104,841 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |  218,104,843 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |  218,104,845 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |  218,104,847 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |  218,104,849 |   13,423,466 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |  218,104,851 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |  218,104,853 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |  218,104,855 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |  218,104,857 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 15 (NTE)              |  218,104,859 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 16 (Regen)            |  218,104,861 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "| Bin 17 (MIL On)           |  218,104,863 |   13,423,467 |    3,635,081 |    1,090,524 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Active 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |        6,000 |            0 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |        3,527 |            0 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |          630 |            0 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |          625 |            0 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |        3,149 |            0 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |        7,395 |            0 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            0 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |        2,250 |            0 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |          150 |            0 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            3 |            0 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |        1,500 |            0 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |           26 |            0 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            1 |            0 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |          300 |            0 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += "10:15:30.0000 NOx Binning Stored 100-Hour Array from Engine #1 (0)" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "|                           |  Tail Pipe   |  Eng. Out.   |              |              |   Engine     |   Vehicle    |" + NL;
        expected += "|                           | NOx Mass, g  | NOx Mass, g  |  EOE, kWh    |   Fuel, l    | Hours, min   |  Dist, km    |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += "| Bin  1 (Total)            |       43,621 |      109,052 |       36,000 |       18,000 |            0 |        9,000 |" + NL;
        expected += "| Bin  2 (Idle)             |       43,621 |      109,052 |       21,160 |       10,580 |            0 |        5,290 |" + NL;
        expected += "| Bin  3 (<25%, <16kph)     |       43,621 |      109,052 |        3,778 |        1,889 |            0 |          944 |" + NL;
        expected += "| Bin  4 (<25%, 16-40kph)   |       43,621 |      109,052 |        3,752 |        1,876 |            0 |          938 |" + NL;
        expected += "| Bin  5 (<25%, 40-64kph)   |       43,621 |      109,052 |       18,893 |        9,446 |            0 |        4,723 |" + NL;
        expected += "| Bin  6 (<25%, >64kph)     |       43,621 |      109,052 |       44,372 |       22,186 |          555 |       11,093 |" + NL;
        expected += "| Bin  7 (25-50%, <16kph)   |       43,621 |      109,052 |            3 |            2 |            0 |            1 |" + NL;
        expected += "| Bin  8 (25-50%, 16-40kph) |       43,621 |      109,052 |       13,500 |        6,750 |            0 |        3,375 |" + NL;
        expected += "| Bin  9 (25-50%, 40-64kph) |       43,621 |      109,052 |          900 |          450 |            0 |          225 |" + NL;
        expected += "| Bin 10 (25-50%, >64kph)   |       43,621 |      109,052 |           16 |            8 |            0 |            4 |" + NL;
        expected += "| Bin 11 (>50%, <16kph)     |       43,621 |      109,052 |        9,000 |        4,500 |            0 |        2,250 |" + NL;
        expected += "| Bin 12 (>50%, 16-40kph)   |       43,621 |      109,052 |          156 |           78 |            0 |           39 |" + NL;
        expected += "| Bin 13 (>50%, 40-64kph)   |       43,621 |      109,052 |            7 |            4 |            0 |            2 |" + NL;
        expected += "| Bin 14 (>50%, >64kph)     |       43,621 |      109,052 |        1,800 |          900 |            0 |          450 |" + NL;
        expected += "| Bin 15 (NTE)              |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 16 (Regen)            |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "| Bin 17 (MIL On)           |       43,621 |      109,052 |            0 |            0 |            0 |            0 |" + NL;
        expected += "|---------------------------+--------------+--------------+--------------+--------------+--------------+--------------|" + NL;
        expected += NL;
        expected += NL;
        // @formatter:on
        assertEquals(expected, listener.getResults());

        assertEquals(NOxBinningModuleTest.expectedReqests(), listener.getMessages());
    }
}
