/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.FuelType.BATT_ELEC;
import static org.etools.j1939_84.model.FuelType.BI_DSL;
import static org.etools.j1939_84.model.FuelType.BI_GAS;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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
 * The unit test for {@link Part01Step12Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step12ControllerTest extends AbstractControllerTest {
    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 12;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step12Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private TableA7Validator tableA7Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private OBDModuleInformation createOBDModuleInformation(SupportedSPN... testResultSpns) {
        OBDModuleInformation module = new OBDModuleInformation(0);
        module.setSupportedSPNs(List.of(testResultSpns));
        return module;
    }

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step12Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              dataRepository,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              tableA7Validator,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 tableA7Validator,
                                 diagnosticMessageModule);
    }

    @Test
    public void testCompressionIgnition() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(159, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 147, 31, 1, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 31, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testCompressionIgnitionFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(159, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 31, 1, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 31, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(false);

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testEmptyTestResults() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(0, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of());

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.a - No test result for Supported SPN 0 from Engine #1 (0)");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 12", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 12, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    public void testIgnitionUnknown() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BATT_ELEC);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMaximum() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0, 0x88, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMinimum() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0xFB00, 0, 0x88);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(supportedSPN.getSpn()),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMinimum2() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0x25, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(157),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(Set.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testInvalidSlot() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 1, 0xFB00, 0xFFFF, 0xFFFF);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(157),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);
        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.c - #1 SLOT identifier for SPN 157 from Engine #1 (0) is invalid");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoOBDModules() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testOneModule() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 8, 0, 0, 0);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, scaledTestResult);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(157),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
        assertEquals(List.of(), listener.getOutcomes());
    }

    @Test
    public void testReportedDuplicates() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 18, 242, 0x0000, 0xFFFF, 0xFB00);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 157, 0, 242, 0xFB00, 0xFF00, 0xFFFF);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(157),
                                                        eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of(scaledTestResult));
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 18 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.12.1.d - Engine #1 (0) returned duplicate test results for SPN 157 FMI 18");

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testSparkIgnitionFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(159, true, true, true, 1);
        dataRepository.putObdModule(createOBDModuleInformation(supportedSPN));

        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet2 = DM30ScaledTestResultsPacket.create(0, scaledTestResult2);

        when(diagnosticMessageModule.requestTestResults(any(),
                                                        eq(0),
                                                        eq(247),
                                                        eq(159),
                                                        eq(31))).thenReturn(List.of(dm30Packet2));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(false);

        runTest();

        assertEquals(List.of(), listener.getOutcomes());

        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }
}
