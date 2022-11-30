/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part11;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
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
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

;

@RunWith(MockitoJUnitRunner.class)
public class Part11Step10ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 11;
    private static final int STEP_NUMBER = 10;

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

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part11Step10Controller(executor,
                                              bannerModule,
                                              new TestDateTimeModule(),
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              communicationsModule);

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
    public void testHappyPathNoFailures() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(0x17));
        dataRepository.putObdModule(new OBDModuleInformation(0x23));

        var packet0 = new DM5DiagnosticReadinessPacket(Packet.create(PGN,
                                                                     0x00,
                                                                     0x00,
                                                                     0x00,
                                                                     0x14,
                                                                     0x37,
                                                                     0xE0,
                                                                     0x1E,
                                                                     0xE0,
                                                                     0x1E));
        var packet17 = new DM5DiagnosticReadinessPacket(Packet.create(PGN,
                                                                      0x17,
                                                                      0x00,
                                                                      0x00,
                                                                      0x05,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00));
        var packet23 = new DM5DiagnosticReadinessPacket(Packet.create(PGN,
                                                                      0x21,
                                                                      0x00,
                                                                      0x00,
                                                                      0x05,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00,
                                                                      0x00));

        when(communicationsModule.requestDM5(any(), eq(0x00))).thenReturn(BusResult.of(packet0));
        when(communicationsModule.requestDM5(any(), eq(0x17))).thenReturn(BusResult.of(packet17));
        when(communicationsModule.requestDM5(any(), eq(0x23))).thenReturn(BusResult.of(packet23));

        runTest();

        verify(communicationsModule).requestDM5(any(), eq(0x00));
        verify(communicationsModule).requestDM5(any(), eq(0x17));
        verify(communicationsModule).requestDM5(any(), eq(0x23));

        assertSame(packet0, dataRepository.getObdModule(0).getLatest(DM5DiagnosticReadinessPacket.class));

        assertEquals("", listener.getMessages());
        String expectedVehicleComposite = NL + "Vehicle Composite of DM5:" + NL +
                "    Comprehensive component        supported,     complete" + NL
                + "    Fuel System                    supported, not complete" + NL
                + "    Misfire                        supported, not complete" + NL
                + "    EGR/VVT system                 supported, not complete" + NL
                + "    Exhaust Gas Sensor heater      supported, not complete" + NL
                + "    Exhaust Gas Sensor             supported, not complete" + NL
                + "    A/C system refrigerant     not supported,     complete" + NL
                + "    Secondary air system       not supported,     complete" + NL
                + "    Evaporative system         not supported,     complete" + NL
                + "    Heated catalyst            not supported,     complete" + NL
                + "    Catalyst                   not supported,     complete" + NL
                + "    NMHC converting catalyst       supported, not complete" + NL
                + "    NOx catalyst/adsorber          supported, not complete" + NL
                + "    Diesel Particulate Filter      supported, not complete" + NL
                + "    Boost pressure control sys     supported, not complete" + NL
                + "    Cold start aid system      not supported,     complete" + NL;
        assertEquals(expectedVehicleComposite, listener.getResults());
    }

}
