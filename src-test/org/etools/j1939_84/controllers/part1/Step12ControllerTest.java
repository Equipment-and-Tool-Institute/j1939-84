/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
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
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step12ControllerTest extends AbstractControllerTest {
    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step12Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDTestsModule obdTestsModule;

    @Mock
    private PartResultFactory partResultFactory;

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
        dateTimeModule = new TestDateTimeModule();

        instance = new Step12Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                dataRepository,
                vehicleInformationModule,
                obdTestsModule,
                partResultFactory);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // verifyNoMoreInteractions(executor,
        // engineSpeedModule,
        // bannerModule,
        // vehicleInformationModule,
        // partResultFactory,
        // dataRepository,
        // mockListener);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step12Controller#getDisplayName()}.
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 12", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step12Controller#getTotalSteps()}.
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testNoOBDModules() {
        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();

        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository, times(2)).getObdModules();

        verify(obdTestsModule).setJ1939(j1939);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step12Controller#run()}.
     */
    @Test
    public void testRun() {
        Map<Integer, Integer> moduleAddresses = new HashMap<>() {
            {
                put(0, 0);
                put(9, 1);
                put(17, 2);
                put(21, 3);

            }
        };

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        when(scaledTestResult.getFmi()).thenReturn(0);
        for (Entry<Integer, Integer> address : moduleAddresses.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(),
                            address.getValue(),
                            (byte) 0,
                            null,
                            null,
                            null,
                            supportedSPNs,
                            null,
                            scaledTestsResults));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository, times(2)).getObdModules();

        verify(obdTestsModule).setJ1939(j1939);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
