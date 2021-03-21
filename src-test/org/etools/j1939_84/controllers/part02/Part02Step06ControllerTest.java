/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
@TestDoc(value = @TestItem(verifies = "Part 2 Step 6", description = "DM56: Model year and certification engine family"))
public class Part02Step06ControllerTest extends AbstractControllerTest {

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

    private DataRepository dataRepository;

    @Before
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();

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
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.6.2.a", description = "Engine Family or model year is different from part 1"))
    public void testCompareEngineFamily() {
        OBDModuleInformation obdModuleInformation1 = new OBDModuleInformation(1);
        obdModuleInformation1.set(DM56EngineFamilyPacket.create(1, 2021, true, "Engine Family2"), 1);
        dataRepository.putObdModule(obdModuleInformation1);

        var packet1 = DM56EngineFamilyPacket.create(1, 2021, true, "Engine Family Different");
        when(diagnosticMessageModule.requestDM56(any(), eq(1))).thenReturn(List.of(packet1));

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM56(any(), eq(1));

        verify(mockListener).addOutcome(2,
                                        6,
                                        FAIL,
                                        "6.2.6.2.a - Engine #2 (1) reported difference when compared to data received during part 1");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.6.2.a", description = "Model year and Engine Family are same as part 1"))
    public void testNoFailures() {
        OBDModuleInformation obdModuleInformation0 = new OBDModuleInformation(0);
        obdModuleInformation0.set(DM56EngineFamilyPacket.create(0, 2020, true, "Engine Family"), 1);
        dataRepository.putObdModule(obdModuleInformation0);

        var packet0 = DM56EngineFamilyPacket.create(0, 2020, true, "Engine Family");
        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of(packet0));

        runTest();

        verify(diagnosticMessageModule).requestDM56(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testNoPackets() {
        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(diagnosticMessageModule.requestDM56(any(), eq(0))).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).requestDM56(any(), eq(0));

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

}
