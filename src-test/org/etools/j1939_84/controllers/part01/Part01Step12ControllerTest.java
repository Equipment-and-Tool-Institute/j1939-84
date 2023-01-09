/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.model.FuelType.BATT_ELEC;
import static org.etools.j1939tools.j1939.model.FuelType.BI_DSL;
import static org.etools.j1939tools.j1939.model.FuelType.BI_GAS;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.ActionOutcome;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
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
    private CommunicationsModule communicationsModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private TableA7Validator tableA7Validator;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private static OBDModuleInformation createOBDModuleInformation(int moduleAddress, SupportedSPN... testResultSpns) {
        OBDModuleInformation module = new OBDModuleInformation(moduleAddress);
        module.set(DM24SPNSupportPacket.create(moduleAddress, testResultSpns), 1);
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
                                              communicationsModule,
                                              tableA7Validator,
                                              DateTimeModule.getInstance());

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
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 tableA7Validator,
                                 communicationsModule);
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: compression ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_DSL</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">PASS ALL</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.1.a", description = "DS DM7 with TID 247 using FMI 31 for each SP identified as providing test results in a DM24 response in step 6.1.4.1 to the SP’s respective OBD ECU. Create list of ECU address+SP+FMI supported test results.") })
    public void testCompressionIgnition() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(159, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 147, 31, 1, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 31, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0x00,
                                                                                    0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(supportedSPN.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 147 FMI 31 (SLOT 1) Result: Test Passed. Min: 0, Value: 0, Max: 0 kPa" + NL;
        expected += "SPN 159 FMI 31 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: compression ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * empty test results</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">Section A.7.1.a failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.a)", description = "Fail if no test result (comprised of a SP+FMI with a test result and a minimum and maximum test limit) for an SP indicated as supported is actually reported from the ECU/device that indicated support.") })
    public void testCompressionIgnitionFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(159, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 31, 1, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 31, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0,
                                                                                    0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(supportedSPN.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForCompressionIgnition(any(), any())).thenReturn(false);

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 31 (SLOT 1) Result: Test Passed. Min: 0, Value: 0, Max: 0 kPa" + NL;
        expected += "SPN 159 FMI 31 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no response;<br>
     * no check</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.2.a)", description = "Fail if no test result is received for any of the SPN+FMI combinations listed in Table A5A.") })
    public void testEmptyTestResults() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(99, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        when(communicationsModule.requestTestResults(any(ResultsListener.class),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(99),
                                                     eq(31))).thenReturn(List.of());

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.validateForSparkIgnition(any(), any(ResultsListener.class))).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.2.a (A.7.2.a) - No test result for Supported SP 99 from Engine #1 (0)");
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(247), eq(99), eq(31));

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any(ResultsListener.class));

        assertEquals("", listener.getMessages());
        assertEquals("" + NL, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#getDisplayName()}.
     *
     */
    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 12", instance.getDisplayName());
    }

    /**
     * Test method for {@link Part01Step12Controller#getStepNumber()}.
     *
     */
    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 12, instance.getStepNumber());
    }

    /**
     * Test method for {@link Part01Step12Controller#getTotalSteps()}.
     * 
     */
    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding:<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BATT_ELEC</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response;<br>
     * DM30 for electric vehicle - unexpected state;<br>
     * verify behavior of tool</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response;<br>
     * DM30 for electric vehicle - unexpected state;<br>
     * verify behavior of tool</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.a)", description = "Fail if no test result (comprised of a SP+FMI with a test result and a minimum and maximum test limit) for an SP indicated as supported is actually reported from the ECU/device that indicated support.") })
    public void testIgnitionUnknown() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BATT_ELEC);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(supportedSPN.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        runTest();

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 0 (SLOT 242) Result: Test Passed. Min: -64, Value: -64, Max: -64 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * test results w/ invalid test maximum</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">Section A.7.1.b failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.b)", description = "Fail if any test result does not report the test result/min test limit/max test limit as initialized (after code clear) values (either 0xFB00(h)/0xFFFF(h)/0xFFFF(h) or 0x0000(h)/0x0000(h)/0x0000(h)).") })
    public void testInvalidScaledTestMaximumFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0, 0x88, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(supportedSPN.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.2.a (A.7.1.b) - Test result for SP 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 0 (SLOT 242) Result: Test Passed. Min: -64, Value: -64, Max: -63.728 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * test results w/ invalid test minimum</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">Section A.7.1.b failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.b)", description = "Fail if any test result does not report the test result/min test limit/max test limit as initialized (after code clear) values (either 0xFB00(h)/0xFFFF(h)/0xFFFF(h) or 0x0000(h)/0x0000(h)/0x0000(h)).") })
    public void testInvalidScaledTestMinimumFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0xFB00, 0, 0x88);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(supportedSPN.getSpn()),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.2.a (A.7.1.b) - Test result for SP 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 0 (SLOT 242) Result: Test Not Complete." + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * invalid test minimum</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">A.7.1.b failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.b)", description = "Fail if the SLOT identifier for any test results is an undefined or a not valid SLOT in Appendix A of J1939-71. See Table A5B for a list of the valid, SLOTs known to be appropriate for use in test results.") })
    public void testInvalidScaledTestValuesFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 242, 0x25, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult);
        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(157),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(Set.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.2.a (A.7.1.b) - Test result for SP 157 FMI 0 from Engine #1 (0) does not report the test result/min test limit/max test limit initialized properly");

        verify(communicationsModule).requestTestResults(any(), eq(0), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 0 (SLOT 242) Result: Test Failed. Min: -64, Value: -63.926, Max: -64 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * invalid slot number</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">A.7.1.c failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.c)", description = "Fail if the SLOT identifier for any test results is an undefined or a not valid SLOT in Appendix A of J1939-71. See Table A5B for a list of the valid, SLOTs known to be appropriate for use in test results.") })
    public void testInvalidSlot() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 157, 0, 1, 0xFB00, 0xFFFF, 0xFFFF);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(157),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);
        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.12.2.a (A.7.1.c) - #1 SLOT identifier for SP 157 FMI 0 from Engine #1 (0) is invalid");

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 157 FMI 0 (SLOT 1) Result: Test Not Complete." + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test no modules responding<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">N/A<br>
     * Fuel Type: N/A</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no request;<br>
     * no resposne</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">no request;<br>
     * no response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    public void testNoOBDModules() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(tableA7Validator).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expectedResults = "6.1.12.5.a - No SPs found that do NOT indicate support for DM58 in the DM24 response"
                + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: compression ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * empty test results</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">Section A.7.1.a failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.1.a)", description = "Fail if no test result (comprised of a SP+FMI with a test result and a minimum and maximum test limit) for an SP indicated as supported is actually reported from the ECU/device that indicated support.") })
    public void testVerifyDM30SupportedFailure() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSPN = SupportedSPN.create(157, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSPN));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(0x00, 0, 31, 0, 0, 0, 0);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0x00,
                                                                                    0,
                                                                                    scaledTestResult);

        when(communicationsModule.requestTestResults(any(ResultsListener.class),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(157),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).requestTestResults(any(ResultsListener.class),
                                                        eq(0x00),
                                                        eq(247),
                                                        eq(157),
                                                        eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(mockListener).addOutcome(1,
                                        12,
                                        FAIL,
                                        "6.1.12.2.a (A.7.1.a) - No test result for supported SP 157 from Engine #1 (0)");

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 0 FMI 31 (SLOT 0) Result: Test Passed. Min: 0, Value: 0, Max: 0" + NL;
        assertEquals(expected, listener.getResults());
        ActionOutcome expectedActionOutcome = new ActionOutcome(FAIL,
                                                                "6.1.12.2.a (A.7.1.a) - No test result for supported SP 157 from Engine #1 (0)");
        assertEquals(List.of(expectedActionOutcome), listener.getOutcomes());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad DM30 response;<br>
     * duplicate SP+FMI test results</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">A.7.2.b failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.2.b)", description = "Fail if any test result does not report the test result/min test limit/max test limit as initialized (after code clear) values (either 0xFB00(h)/0xFFFF(h)/0xFFFF(h) or 0x0000(h)/0x0000(h)/0x0000(h)).") })
    public void testReportedDuplicateTestResultsWarning() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSP = SupportedSPN.create(159, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSP));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);

        DM30ScaledTestResultsPacket dm30Packet = DM30ScaledTestResultsPacket.create(0x00,
                                                                                    0,
                                                                                    scaledTestResult,
                                                                                    scaledTestResult2);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(159),
                                                     eq(31))).thenReturn(List.of(dm30Packet));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of(scaledTestResult), List.of());

        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(true);

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.12.2.a (A.7.1.d) - Engine #1 (0) returned duplicate test results for SP 159 FMI 18");

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSP.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 159 FMI 18 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        expected += "SPN 159 FMI 18 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: compression ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_DSL</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response;<br>
     * duplicate 0x09 SP+FMI test results</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">A.7.1.d failure</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x09;<br>
     * Fuel Type: BI_DSL</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response;<br>
     * duplicate of 0x00 SP+FMI test results</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">A.7.1.d failure</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.2.a (A.7.2.b)", description = "Warn if any ECU reports more than one set of test results for the same SP+FMI.") })
    public void testTwoModulesReportWithSameTestResultsWarning() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_DSL);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);
        SupportedSPN supportedSP0x00 = SupportedSPN.create(159, true, true, true, false, 1);
        SupportedSPN supportedSP0x09 = SupportedSPN.create(159, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSP0x00));
        dataRepository.putObdModule(createOBDModuleInformation(0x09, supportedSP0x09));

        ScaledTestResult scaledTestResult = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);
        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);

        DM30ScaledTestResultsPacket dm30Packet0x00 = DM30ScaledTestResultsPacket.create(0x00,
                                                                                        0,
                                                                                        scaledTestResult);
        DM30ScaledTestResultsPacket dm30Packet0x09 = DM30ScaledTestResultsPacket.create(0x09,
                                                                                        0,
                                                                                        scaledTestResult2);

        when(communicationsModule.requestTestResults(any(ResultsListener.class),
                                                     eq(0x00),
                                                     eq(247),
                                                     eq(159),
                                                     eq(31))).thenReturn(List.of(dm30Packet0x00));
        when(communicationsModule.requestTestResults(any(ResultsListener.class),
                                                     eq(0x09),
                                                     eq(247),
                                                     eq(159),
                                                     eq(31))).thenReturn(List.of(dm30Packet0x09));

        AcknowledgmentPacket ackPacket01 = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket01));

        when(tableA7Validator.findDuplicates(eq(List.of(scaledTestResult2)))).thenReturn(List.of());
        when(tableA7Validator.findDuplicates(eq(List.of(scaledTestResult,
                                                        scaledTestResult2)))).thenReturn(List.of(scaledTestResult,
                                                                                                 scaledTestResult2));

        when(tableA7Validator.validateForCompressionIgnition(any(), any(ResultsListener.class))).thenReturn(true);

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.12.2.a (A.7.2.b) - More than one ECU responded with test results for SP 159 + FMI 18 combination");

        verify(communicationsModule).requestTestResults(any(),
                                                        eq(0x00),
                                                        eq(247),
                                                        eq(supportedSP0x00.getSpn()),
                                                        eq(31));
        verify(communicationsModule).requestTestResults(any(),
                                                        eq(0x09),
                                                        eq(247),
                                                        eq(supportedSP0x09.getSpn()),
                                                        eq(31));

        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(3)).findDuplicates(any());
        verify(tableA7Validator).validateForCompressionIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 159 FMI 18 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        expected += NL + "Axle - Drive #1 (9) Test Results:" + NL;
        expected += "SPN 159 FMI 18 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        assertEquals(expected, listener.getResults());
    }

    /**
     * Test method for {@link Part01Step12Controller#run()}.
     * Test one module responding with fuel type: spark ignition<br>
     * <br>
     * <p>
     * <b style="color:red">Module Responses:</b>
     * <table style="border-collapse: collapse;border-spacing: 0px;border:1px solid #ddd;">
     * <col width="25%";/>
     * <col width="45%";/>
     * <col width="30%";/>
     *
     * <thead>
     * <th colspan="1" style="text-align:center;border-bottom:2px solid #ddd;padding: 4px;word-wrap:break-word">Module
     * Details</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">DM30 Response Detailse</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Section A.7 Details</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x00;<br>
     * Fuel Type: BI_GAS</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good DM30 response</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">PASS ALL</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.12.1.a", description = "DS DM7 with TID 247 using FMI 31 for each SP identified as providing test results in a DM24 response in step 6.1.4.1 to the SP’s respective OBD ECU. Create list of ECU address+SP+FMI supported test results.") })
    public void testSparkIgnition() {
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(BI_GAS);
        vehicleInformation.setEngineModelYear(2022);
        dataRepository.setVehicleInformation(vehicleInformation);

        SupportedSPN supportedSP = SupportedSPN.create(159, true, true, true, false, 1);
        dataRepository.putObdModule(createOBDModuleInformation(0x00, supportedSP));

        ScaledTestResult scaledTestResult2 = ScaledTestResult.create(247, 159, 18, 8, 0, 0, 0);
        DM30ScaledTestResultsPacket dm30Packet2 = DM30ScaledTestResultsPacket.create(0, 0, scaledTestResult2);

        when(communicationsModule.requestTestResults(any(),
                                                     eq(0),
                                                     eq(247),
                                                     eq(159),
                                                     eq(31))).thenReturn(List.of(dm30Packet2));

        AcknowledgmentPacket ackPacket = AcknowledgmentPacket.create(0x00, NACK);
        when(communicationsModule.requestDM58(any(CommunicationsListener.class),
                                              eq(0x00),
                                              anyInt())).thenReturn(BusResult.of(ackPacket));

        when(tableA7Validator.findDuplicates(any())).thenReturn(List.of());
        when(tableA7Validator.validateForSparkIgnition(any(), any())).thenReturn(false);

        runTest();

        assertEquals(List.of(), listener.getOutcomes());

        verify(communicationsModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSP.getSpn()), eq(31));
        verify(communicationsModule).requestDM58(any(CommunicationsListener.class), eq(0x00), anyInt());

        verify(tableA7Validator, times(2)).findDuplicates(any());
        verify(tableA7Validator).validateForSparkIgnition(any(), any());

        assertEquals("", listener.getMessages());
        String expected = "";
        expected += NL + "Engine #1 (0) Test Results:" + NL;
        expected += "SPN 159 FMI 18 (SLOT 8) Result: Test Passed. Min: -200, Value: -200, Max: -200 deg" + NL;
        assertEquals(expected, listener.getResults());
    }
}
