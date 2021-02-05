/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.FuelType.BI_DSL;
import static org.etools.j1939_84.model.FuelType.BI_GAS;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
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

    @Mock
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

    private OBDModuleInformation createOBDModuleInformation(List<SupportedSPN> testResultSpns) {
        OBDModuleInformation module = mock(OBDModuleInformation.class);
        when(module.getTestResultSpns()).thenReturn(testResultSpns);

        return module;
    }

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step12Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              dataRepository,
                                              vehicleInformationModule,
                                              diagnosticMessageModule,
                                              tableA7Validator,
                                              DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 mockListener,
                                 tableA7Validator);
    }

    @Test
    public void testCompressionIgnition() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(159);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();

        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);

        ScaledTestResult scaledTestResult2 = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult2);
        when(scaledTestResult2.getSpn()).thenReturn(159);
        int slotNumber2 = 8;
        when(scaledTestResult2.getSlot()).thenReturn(Slot.findSlot(slotNumber2));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testCompressionIgnitionFailure() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(159);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();

        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);

        ScaledTestResult scaledTestResult2 = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult2);
        when(scaledTestResult2.getSpn()).thenReturn(159);
        int slotNumber2 = 8;
        when(scaledTestResult2.getSlot()).thenReturn(Slot.findSlot(slotNumber2));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(false);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testEmptyTestResults() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(List.of());

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.a - No test result for Supported SPN 0 from Engine #1 (0)");

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = "FAIL: 6.1.12.1.a - No test result for Supported SPN 0 from Engine #1 (0)" + NL;
        assertEquals(expectedResults, listener.getResults());
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
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BATT_ELEC);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));
        when(scaledTestResult.getTestMaximum()).thenReturn(0x0000);
        when(scaledTestResult.getTestMinimum()).thenReturn(0x0000);
        when(scaledTestResult.getTestValue()).thenReturn(0x0000);
        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMaximum() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getTestMaximum()).thenReturn(0x88);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals(
                "FAIL: 6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly" + NL,
                listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMinimum() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult.getTestMinimum()).thenReturn(0x88);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly" + NL,
                listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMinimum2() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getTestValue()).thenReturn(0x25);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(Set.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly" + NL;

        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link Part01Step12Controller#run()}.
     */
    @Test
    public void testInvalidSlot() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        when(scaledTestResult.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult.getTestMinimum()).thenReturn(0xFFFF);
        when(scaledTestResult.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult.getSpn()).thenReturn(157);
        scaledTestsResults.add(scaledTestResult);

        int slotNumber = 1;
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(slotNumber));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);
        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                                        "6.1.12.1.c - #1 SLOT identifier for SPN 157 from Engine #1 (0) is invalid");

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.1.12.1.c - #1 SLOT identifier for SPN 157 from Engine #1 (0) is invalid" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testNoOBDModules() {
        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        verify(vehicleInformation).getFuelType();

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testOneModule() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportedDuplicates() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getTestValue()).thenReturn(0x0000);
        when(scaledTestResult.getTestMinimum()).thenReturn(0xFB00);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getFmi()).thenReturn(18);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));
        ScaledTestResult scaledTestResult2 = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult2);
        when(scaledTestResult2.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult2.getTestMinimum()).thenReturn(0xFFFF);
        when(scaledTestResult2.getTestMaximum()).thenReturn(0xFF00);
        when(scaledTestResult2.getSpn()).thenReturn(157);
        when(scaledTestResult2.getSlot()).thenReturn(Slot.findSlot(242));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(scaledTestsResults)).thenReturn(List.of(scaledTestResult));
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

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

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "";
        expectedResults += "FAIL: 6.1.12.1.b - Test result for SPN 157 FMI 18 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly" + NL;
        expectedResults += "FAIL: 6.1.12.1.b - Test result for SPN 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly" + NL;
        expectedResults += "WARN: 6.1.12.1.d - Engine #1 (0) returned duplicate test results for SPN 157 FMI 18" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testSparkIgnitionFailure() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(159);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();

        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);

        ScaledTestResult scaledTestResult2 = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult2);
        when(scaledTestResult2.getSpn()).thenReturn(159);
        int slotNumber2 = 8;
        when(scaledTestResult2.getSlot()).thenReturn(Slot.findSlot(slotNumber2));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(diagnosticMessageModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(false);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).putObdModule(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }
}
