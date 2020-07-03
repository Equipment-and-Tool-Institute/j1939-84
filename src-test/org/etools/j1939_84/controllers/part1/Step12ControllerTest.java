/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.Slot;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
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
    private TableA7Validator tableA7Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private OBDModuleInformation createOBDModuleInformation(
            List<SupportedSPN> testResultSpns,
            List<ScaledTestResult> scaledTestResult) {
        OBDModuleInformation module = mock(OBDModuleInformation.class);
        when(module.getTestResultSpns()).thenReturn(testResultSpns);

        return module;
    }

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
                partResultFactory,
                tableA7Validator);

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
                partResultFactory,
                dataRepository,
                mockListener,
                tableA7Validator);
    }

    @Test
    public void testEmptyTestResults() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                12,
                Outcome.FAIL,
                "Fail if no test result (comprised of a SPN+FMI with a test result and a min and max test limit) for an SPN indicated as supported is actually reported from the ECU/device that indicated support.");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: Fail if no test result (comprised of a SPN+FMI with a test result and a min and max test limit) for an SPN indicated as supported is actually reported from the ECU/device that indicated support.\n";
        assertEquals(expectedResults, listener.getResults());
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

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step12Controller#run()}.
     */
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

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
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

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                12,
                Outcome.FAIL,
                "Fail verification of 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results is only defined for spark ignition or compression engines.");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: Fail verification of 6.1.12 DM7/DM30: Command Non-continuously Monitored Test/Scaled Test Results is only defined for spark ignition or compression engines.\n";
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMaximum() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
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

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        // FIXME this logic should be removed
        // verify(mockListener).addOutcome(1,
        // 12,
        // Outcome.FAIL,
        // "Fail if any test result does not report the test result max test
        // limit initialized one of the following values 0xFB00/0xFFFF/0xFFFF");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        // String expectedResults = "FAIL: Fail if any test result does not
        // report the test result max test limit initialized one of the
        // following values 0xFB00/0xFFFF/0xFFFF\n";
        // assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testInvalidScaledTestMinimum() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
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

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        // this logic should be removed
        // verify(mockListener).addOutcome(1,
        // 12,
        // Outcome.FAIL,
        // "Fail if any test result does not report the test result min test
        // limit initialized one of the following values 0x0000/0x0000/0x0000");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        // String expectedResults = "FAIL: Fail if any test result does not
        // report the test result min test limit initialized one of the
        // following values 0x0000/0x0000/0x0000\n";
        // assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.Step12Controller#run()}.
     */
    @Test
    public void testInvalidSlot() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);
        int slotNumber = 1;
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(slotNumber));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                12,
                Outcome.FAIL,
                "#" + slotNumber + " SLOT identifier is an undefined or invalid");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: #" + String.valueOf(slotNumber)
                + " SLOT identifier is an undefined or invalid\n";
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testNonMatchingSpns() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(159);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
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

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoOBDModules() {
        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_DSL);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        verify(vehicleInformation).getFuelType();

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
    public void testOneModule() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
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

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testReportedDuplicates() {
        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(vehicleInformation.getFuelType()).thenReturn(FuelType.BI_GAS);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        Collection<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        List<SupportedSPN> supportedSPNs = new ArrayList<>();
        SupportedSPN supportedSPN = mock(SupportedSPN.class);
        when(supportedSPN.getSpn()).thenReturn(157);
        supportedSPNs.add(supportedSPN);

        obdModuleInformations.add(createOBDModuleInformation(supportedSPNs,
                new ArrayList<ScaledTestResult>()));
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        List<ScaledTestResult> scaledTestsResults = new ArrayList<>();
        ScaledTestResult scaledTestResult = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult);
        when(scaledTestResult.getSpn()).thenReturn(157);
        when(scaledTestResult.getFmi()).thenReturn(18);
        when(scaledTestResult.getSlot()).thenReturn(Slot.findSlot(242));
        ScaledTestResult scaledTestResult2 = mock(ScaledTestResult.class);
        scaledTestsResults.add(scaledTestResult2);
        when(scaledTestResult2.getSpn()).thenReturn(157);
        when(scaledTestResult2.getSlot()).thenReturn(Slot.findSlot(242));

        // FIXME - I shouldn't have to mock this - .reportDuplicates needs to be
        // corrected.
        when(tableA7Validator.hasDuplicates(scaledTestsResults)).thenReturn(Collections.singletonList(scaledTestResult));

        List<DM30ScaledTestResultsPacket> dm30Packets = new ArrayList<>();
        DM30ScaledTestResultsPacket dm30Packet = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet.getTestResults()).thenReturn(scaledTestsResults);
        dm30Packets.add(dm30Packet);

        when(obdTestsModule.getDM30Packets(any(), eq(0), eq(supportedSPN))).thenReturn(dm30Packets);

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                12,
                Outcome.FAIL,
                "SPN 157 FMI 18 returned duplicates.");

        verify(obdTestsModule).setJ1939(j1939);

        verify(tableA7Validator).hasDuplicates(scaledTestsResults);
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: SPN 157 FMI 18 returned duplicates.\n";
        assertEquals(expectedResults, listener.getResults());
    }

}
