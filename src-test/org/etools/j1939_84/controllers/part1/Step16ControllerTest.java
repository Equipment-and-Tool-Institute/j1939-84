/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
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

/**
 *
 * @author Garrison Garland (garrison@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step16ControllerTest extends AbstractControllerTest {

    // private static DM2PreviouslyActiveDTC createData() {
    //
    // return data;
    // }

    private static DM2PreviouslyActiveDTC createDM20(int[] data) {

        DM2PreviouslyActiveDTC packets = mock(DM2PreviouslyActiveDTC.class);

        if (data != null) {

            List<DiagnosticTroubleCode> diagData = new ArrayList<>(data.length);
            for (int i : data) {
                diagData.add(i, null);
            }
            diagData.stream().map(spn -> new DiagnosticTroubleCode(data)).collect(Collectors.toList());
            when(packets.getDtcs()).thenReturn(diagData);
        }

        return packets;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticTroubleCodePacket diagnosticTroubleCodePacket;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step16Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Test
    public void runHappyPath() {
        List<DM2PreviouslyActiveDTC> globalDM2s = new ArrayList<>();
        Set<Integer> obdModulesAddresses = new HashSet<>();
        obdModulesAddresses.add(0);
        DM2PreviouslyActiveDTC dm2s = mock(DM2PreviouslyActiveDTC.class);
        dm2s.getDtcs();
        globalDM2s.add(dm2s);

        List<DiagnosticTroubleCode> dtcs = new ArrayList<>();
        LampStatus milStatus = LampStatus.OFF;

        when(diagnosticTroubleCodePacket.getDtcs()).thenReturn(dtcs);
        when(diagnosticTroubleCodePacket.getMalfunctionIndicatorLampStatus()).thenReturn(milStatus);

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModulesAddresses);

        // List<DiagnosticTroubleCode> dtcs = new ArrayList<>();
        //
        // when(dtcModule.requestDM2(listener)).thenReturn(globalDM2s);

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticTroubleCodePacket).getDtcs();
        verify(diagnosticTroubleCodePacket).getMalfunctionIndicatorLampStatus();

        verify(dtcModule).setJ1939(j1939);

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step16Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                dtcModule,
                partResultFactory,
                dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                partResultFactory,
                dtcModule);
    }

    @Test
    public void testGetDiplayName() {
        assertEquals("Display Name", "Part 1 Step 16", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

}
