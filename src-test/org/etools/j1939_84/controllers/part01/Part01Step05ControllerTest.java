/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
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
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939_84.utils.VinDecoder;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.VehicleIdentificationPacket;
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
 * The unit test for {@link Part01Step05Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step05ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private CommunicationsModule communicationsModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step05Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        VinDecoder vinDecoder = new VinDecoder();
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step05Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              vinDecoder,
                                              dataRepository,
                                              DateTimeModule.getInstance(),
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

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 j1939);
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getDisplayName()}.
     */
    @Test
    @TestDoc(description = "Verify step name.")
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 5", instance.getDisplayName());
    }

    /**
     * Test method for
     * {@link Part01Step11Controller#getTotalSteps()}.
     */
    @Test
    @TestDoc(description = "Verify step 5 has 1 step.")
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.5.1.a", description = "Global Request (PG 59904) for PG 65260 Vehicle ID (SP 237) VIN."))
    public void testNoError() {
        // valid vin
        String vin = "SAJWA44B075B90149";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 0x01);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet));
        VehicleInformation vehicleInformation = new VehicleInformation();

        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2037);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).setJ1939(eq(j1939));
        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(CommunicationsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response<br>
     * different VIN stored</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.3.b", description = "Warn if more than one VIN response from any individual ECU.") })
    public void testMoreThanOneVinResponseFromAnEcuWarning() {

        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = VehicleIdentificationPacket.create(0x01, vin);

        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);

        VehicleIdentificationPacket packet2 = VehicleIdentificationPacket.create(0x01, vin);

        OBDModuleInformation obdModule2 = new OBDModuleInformation((1));
        obdModule2.set(packet2, 1);
        dataRepository.putObdModule(obdModule2);
        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet1, packet2));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2014);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.b - More than one OBD ECU responded with VIN");

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.b - More than one VIN response from an ECU");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
     * Test two modules with same VIN responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding:3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">non-OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">non-OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.3.c", description = "Warn if VIN provided from more than one non-OBD ECU") })
    public void testVinProvidedByMoreThanOneNonObdEcusWarning() {

        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet1, 1);

        VehicleIdentificationPacket packet2 = VehicleIdentificationPacket.create(0x00, vin);

        OBDModuleInformation obdModule2 = new OBDModuleInformation((0x00));
        obdModule2.set(packet2, 1);

        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn(RequestResult.of(packet1, packet2));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2014);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.a - Non-OBD ECU responded with VIN");
        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.c - VIN provided from more than one non-OBD ECU");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
     * Test no OBD and one non-OBD module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">non-OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.5.3.a", description = "Warn if VIN response from non-OBD ECU") })
    public void testNonObdRespondedWithVinWarning() {
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 1);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2014);
        dataRepository.setVehicleInformation(vehicleInformation);

        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet));

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN),
                                             any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.a - Non-OBD ECU responded with VIN");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
     * Test no OBD and one non-OBD module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">non-OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.d", description = "Fail if 10th character of VIN does not match model year of vehicle (not engine) entered by user earlier in this part") })
    public void test10thCharInVinFailure() {
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2019);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(CommunicationsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.d - 10th character of VIN does not match model year of vehicle entered by user earlier in this part");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     *
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding:3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid
     * #ddd;padding:3px;word-wrap:break-word">bad VIN response<br>
     * vin contains manufacture data</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.3.d", description = "Warn if manufacturer defined data follows the VIN") })
    public void testManufacturerDataWarning() {

        String vin = "2*G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);

        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);

        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn(RequestResult.of(packet));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);

        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.c - VIN does not match user entered VIN");
        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.d - Manufacturer defined data follows the VIN");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
     * Test two module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;border-bottom:1px solid #ddd;padding: 3px;word-wrap:break-word">0x00</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-bottom:1px solid #ddd;border-left:1px solid #ddd;padding:
     * 3px;word-wrap:break-word">good VIN response</td>
     * </tr>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good VIN
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.b", description = "Fail if more than one OBD ECU responds with VIN") })
    public void testMoreThanOneObdRespondedFailure() {

        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);

        VehicleIdentificationPacket packet2 = VehicleIdentificationPacket.create(0x00, vin);
        OBDModuleInformation obdModule2 = new OBDModuleInformation((0x00));
        obdModule2.set(packet2, 1);
        dataRepository.putObdModule(obdModule2);
        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn(RequestResult.of(packet1, packet2));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2014);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.b - More than one OBD ECU responded with VIN");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">good VIN
     * response</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.c", description = "Fail if VIN does not match user entered VIN from earlier in this section") })
    public void testVinAndUserEnteredVinMismatchFailure() {

        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn(RequestResult.of(packet));

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin("1XPBDK9X1LD708195");
        vehicleInformation.setVehicleModelYear(2014);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.c - VIN does not match user entered VIN");
    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">0x01</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">OBD</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">bad VIN
     * response<br>
     * VIN <17 char</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.e", description = "Fail per Section A.3, Criteria for VIN Validation") })
    public void testNot17CharsFailure() {
        // 16 chars long
        String vin = "G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(0x01, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((0x01));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn((RequestResult.of(packet)));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");

    }

    /**
     * Test method for {@link Part01Step05Controller#run()}.
     * Test no module responding:<br>
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
     * Address</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">Module Type Response</th>
     * <th colspan="1" style="text-align:center;border-left:1px solid #ddd;border-bottom:2px solid #ddd;padding:
     * 4px;word-wrap=break-word">VIN Response</th>
     * </thead>
     * <tbody>
     * <tr>
     * <td style="text-align:center;padding: 3px;word-wrap:break-word">N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">N/A</td>
     * <td style="text-align:center;border-left:1px solid #ddd;padding: 3px;word-wrap:break-word">empty</td>
     * </tr>
     * </tbody>
     * </table>
     * </P>
     */
    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.a", description = "Fail if no VIN is provided by any ECU"))
    public void testPacketsEmptyFailure() {

        when(communicationsModule.request(eq(VehicleIdentificationPacket.PGN),
                                          any(ResultsListener.class))).thenReturn((RequestResult.of()));

        runTest();

        verify(communicationsModule).request(eq(VehicleIdentificationPacket.PGN), any(ResultsListener.class));

        verify(mockListener).addOutcome(1, 5, FAIL, "6.1.5.2.a - No VIN was provided by any ECU");
    }
}
