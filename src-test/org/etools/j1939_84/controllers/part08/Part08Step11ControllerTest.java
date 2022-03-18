/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part08;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import net.soliddesign.j1939tools.j1939.J1939;
import net.soliddesign.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import net.soliddesign.j1939tools.j1939.packets.ScaledTestResult;
import net.soliddesign.j1939tools.j1939.packets.SupportedSPN;
import net.soliddesign.j1939tools.modules.CommunicationsModule;
import net.soliddesign.j1939tools.modules.DateTimeModule;

@RunWith(MockitoJUnitRunner.class)
public class Part08Step11ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 8;
    private static final int STEP_NUMBER = 11;

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

        instance = new Part08Step11Controller(executor,
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
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);

        int spn1 = 123;
        var supportedSPN1 = SupportedSPN.create(spn1, true, true, true, false, 1);

        int spn2 = 456;
        var supportedSPN2 = SupportedSPN.create(spn2, true, true, true, false, 1);

        obdModuleInformation.setSupportedSPNs(List.of(supportedSPN1, supportedSPN2));

        // Not Initialized
        var str1 = ScaledTestResult.create(247, spn1, 4, 4, 100, 1000, 0);
        // Initialized
        var str2 = ScaledTestResult.create(247, spn2, 7, 4, 0, 0, 0);
        obdModuleInformation.setScaledTestResults(List.of(str1, str2));
        dataRepository.putObdModule(obdModuleInformation);

        var dm30_1 = DM30ScaledTestResultsPacket.create(0, 0, str1);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(spn1),
                                                     eq(31))).thenReturn(List.of(dm30_1));

        var dm30_2 = DM30ScaledTestResultsPacket.create(0, 0, str2);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(spn2),
                                                     eq(31))).thenReturn(List.of(dm30_2));
        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(247), eq(spn1), eq(31));
        verify(communicationsModule).requestTestResults(any(), eq(0), eq(247), eq(spn2), eq(31));

        var nonInitializedTests = dataRepository.getObdModule(0).getNonInitializedTests();
        assertEquals(1, nonInitializedTests.size());
        assertEquals(spn1, nonInitializedTests.get(0).getSpn());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

}
