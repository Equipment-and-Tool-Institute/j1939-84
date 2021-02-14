/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part03;

import static org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket.PGN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Part03Step14ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 3;
    private static final int STEP_NUMBER = 14;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DataRepository dataRepository;

    private StepController instance;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        DateTimeModule dateTimeModule = DateTimeModule.getInstance();
        dataRepository = DataRepository.newInstance();

        listener = new TestResultsListener(mockListener);
        instance = new Part03Step14Controller(executor,
                                              bannerModule,
                                              dateTimeModule,
                                              dataRepository,
                                              engineSpeedModule,
                                              vehicleInformationModule,
                                              diagnosticMessageModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
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
    public void testRun() {

        int[] data = {
                0x04, // Ignition Cycle Counter
                0x00, // Ignition Cycle Counter
                0x5A, // OBD Monitoring Conditions Encountered
                0x5A, // OBD Monitoring Conditions Encountered

                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0x11, // SPN of Applicable System Monitor
                0xAA, // Applicable System Monitor Numerator
                0xAA, // Applicable System Monitor Numerator
                0xBB, // Applicable System Monitor Denominator
                0xBB, // Applicable System Monitor Denominator
        };

        DM20MonitorPerformanceRatioPacket dm20Packet = new DM20MonitorPerformanceRatioPacket(Packet.create(PGN, 0x00, data));
        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(new BusResult<>(false,dm20Packet));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule1);

        runTest();

        assertEquals(4, dataRepository.getObdModule(0).getIgnitionCycleCounterValue());
        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));
    }
    @Test
    public void testEmptyPackets() {

        when(diagnosticMessageModule.requestDM20(any(), eq(0))).thenReturn(new BusResult<>(false, Optional.empty()));

        OBDModuleInformation obdModule1 = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModule1);

        runTest();

        assertEquals(0, dataRepository.getObdModule(0).getIgnitionCycleCounterValue());

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        verify(diagnosticMessageModule).requestDM20(any(), eq(0));
    }
}
