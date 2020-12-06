/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step23Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step23ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;
    private static final int PGN = DM31DtcToLampAssociation.PGN;
    private static final int STEP_NUMBER = 23;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step23Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);

        instance = new Step23Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule,
                mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step23Controller#run()}.
     */
    @Test
    public void testFail() {

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x62, // Lamp Status/Support
                0x1D, // Lamp Status/State
        };
        DM31DtcToLampAssociation packet = new DM31DtcToLampAssociation(
                Packet.create(PGN, 0x00, data));

        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet), Collections.emptyList()));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM31(any());

        verify(mockListener, atLeastOnce()).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                "6.1.23.2 - a. Fail if any received ECU response does not report MIL off");

        verify(reportFileModule).onProgress(0, 1, "");

        String expectedResults = "FAIL: 6.1.23.2 - a. Fail if any received ECU response does not report MIL off" + NL;
        assertEquals(expectedResults, listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getPartNumber()}.
     */
    @Test
    public void testGetPartNumber() {
        assertEquals("Part Number", PART_NUMBER, instance.getPartNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.StepController#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step23Controller#run()}.
     */
    @Test
    public void testRun() {

        int[] data = {
                0x61, // SPN least significant bit
                0x02, // SPN most significant bit
                0x13, // Failure mode indicator
                0x81, // SPN Conversion Occurrence Count
                0x00, // Lamp Status/Support
                0xFF, // Lamp Status/State
        };
        DM31DtcToLampAssociation packet = new DM31DtcToLampAssociation(
                Packet.create(PGN, 0x00, data));

        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet), Collections.emptyList()));

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM31(any());

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
    }
}
