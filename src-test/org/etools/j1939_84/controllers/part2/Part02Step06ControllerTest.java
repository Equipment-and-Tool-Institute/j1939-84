/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 2 Step 6",
        description = "DM56: Model year and certification engine family"))
public class Part02Step06ControllerTest extends AbstractControllerTest {

    private static DM56EngineFamilyPacket createDM56(String modelYear, String familyName) {
        DM56EngineFamilyPacket packet = mock(DM56EngineFamilyPacket.class);
        when(packet.getModelYearField()).thenReturn(modelYear);
        when(packet.getFamilyName()).thenReturn(familyName);

        return packet;
    }

    @Mock
    private BannerModule bannerModule;

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
    
    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Before
    public void setUp() throws Exception {

        DataRepository dataRepository = DataRepository.newInstance();

        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.setEngineFamilyName("Engine Family");
        obdModuleInformation0.setModelYear("Model Year");
        dataRepository.putObdModule(obdModuleInformation0);

        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.setEngineFamilyName("Engine Family Other");
        obdModuleInformation1.setModelYear("Model Year Other");
        dataRepository.putObdModule(obdModuleInformation1);

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        Part02Step06Controller instance = new Part02Step06Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                DateTimeModule.getInstance(),
                diagnosticMessageModule);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.6.2.a",
            description = "Engine Family is different from part 1"))
    public void testCompareEngineFamily() {

        DM56EngineFamilyPacket packet0 = createDM56("Model Year", "Engine Family");
        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of(packet0));

        DM56EngineFamilyPacket packet1 = createDM56("Model Year Other", "Engine Family Different");
        when(diagnosticMessageModule.requestDM56(any(), eq(1))).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM56(any(), eq(0));
        verify(diagnosticMessageModule).requestDM56(any(), eq(1));

        verify(mockListener).addOutcome(2,
                                        6,
                                        FAIL,
                                        "6.2.6.2.a - Engine #2 (1) reported different Engine Family Name when compared to data received in part 1");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(NL + NL +
                             "FAIL: 6.2.6.2.a - Engine #2 (1) reported different Engine Family Name when compared to data received in part 1"
                             + NL,
                     listener.getResults());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.6.2.a",
            description = "Model year is different from part 1"))
    public void testCompareModelYear() {

        DM56EngineFamilyPacket packet0 = createDM56("Model Year Different", "Engine Family");
        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of(packet0));

        DM56EngineFamilyPacket packet1 = createDM56("Model Year Other", "Engine Family Other");
        when(diagnosticMessageModule.requestDM56(any(), eq(1))).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM56(any(), eq(0));
        verify(diagnosticMessageModule).requestDM56(any(), eq(1));

        verify(mockListener).addOutcome(2,
                                        6,
                                        FAIL,
                                        "6.2.6.2.a - Engine #1 (0) reported different Model Year when compared to data received in part 1");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                NL + "FAIL: 6.2.6.2.a - Engine #1 (0) reported different Model Year when compared to data received in part 1"
                        + NL + NL,
                listener.getResults());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.6.2.a",
            description = "Model year and Engine Family are same as part 1"))
    public void testNoFailures() {

        DM56EngineFamilyPacket packet0 = createDM56("Model Year", "Engine Family");
        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of(packet0));

        DM56EngineFamilyPacket packet1 = createDM56("Model Year Other", "Engine Family Other");
        when(diagnosticMessageModule.requestDM56(any(), eq(1))).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM56(any(), eq(0));
        verify(diagnosticMessageModule).requestDM56(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(NL + NL, listener.getResults());
    }

    @Test
    public void testNoPackets() {

        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of());
        when(diagnosticMessageModule.requestDM56(any(), eq(1))).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM56(any(), eq(0));
        verify(diagnosticMessageModule).requestDM56(any(), eq(1));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(NL + NL, listener.getResults());
    }

}