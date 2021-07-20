/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

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
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.VehicleIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.VinDecoder;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Part01Step05Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step05ControllerTest {

    @Mock
    private BannerModule bannerModule;

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

    @Mock
    private VinDecoder vinDecoder;

    private void runTest() {
        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(engineSpeedModule).setJ1939(j1939);
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        dataRepository = DataRepository.newInstance();

        instance = new Part01Step05Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              vinDecoder,
                                              dataRepository,
                                              DateTimeModule.getInstance());
    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 vinDecoder,
                                 mockListener);
    }

    @Test
    @TestDoc(description = "Verify step name.")
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 5", instance.getDisplayName());
    }

    @Test
    @TestDoc(description = "Verify step 5 has 1 step.")
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.5", description = "Happy Path - no errors"))
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testNoError() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(1, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        packets.add(packet);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn(packets);
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);

    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.5.3.b", description = "More than one VIN response from an ECU") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testMoreThanOneVinResponseFromAnEcuFailure() {

        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = mock(VehicleIdentificationPacket.class);
        when(packet1.getVin()).thenReturn(vin);
        when(packet1.getSourceAddress()).thenReturn(1);
        when(packet1.getManufacturerData()).thenReturn("");
        packets.add(packet1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);

        VehicleIdentificationPacket packet2 = mock(VehicleIdentificationPacket.class);
        when(packet2.getSourceAddress()).thenReturn(1);
        packets.add(packet2);
        OBDModuleInformation obdModule2 = new OBDModuleInformation((1));
        obdModule2.set(packet2, 1);
        dataRepository.putObdModule(obdModule2);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn(packets);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.b - More than one OBD ECU responded with VIN");

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.b - More than one VIN response from an ECU");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.5.3.c", description = "VIN provided from more than one non-OBD ECU") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testVinProvidedByMoreThanOneNonObdEcusFailure() {

        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = mock(VehicleIdentificationPacket.class);
        when(packet1.getVin()).thenReturn(vin);
        when(packet1.getSourceAddress()).thenReturn(1);
        when(packet1.getManufacturerData()).thenReturn("");
        packets.add(packet1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet1, 1);

        VehicleIdentificationPacket packet2 = mock(VehicleIdentificationPacket.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        packets.add(packet2);

        OBDModuleInformation obdModule2 = new OBDModuleInformation((0));
        obdModule2.set(packet2, 1);

        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn(packets);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.a - Non-OBD ECU responded with VIN");
        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.c - VIN provided from more than one non-OBD ECU");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.5.3.a", description = "Non-OBD ECU responded with VIN") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testNonObdRespondedWithVin() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(1, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        packets.add(packet);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn(packets);
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.a - Non-OBD ECU responded with VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.d", description = "10th character of VIN does not match model year of vehicle entered by user earlier in this part") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void test10thCharMismatchInVin() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(1, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        packets.add(packet);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn((List.of(packet)));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        vehicleInformation.setVehicleModelYear(2019);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);
        when(vinDecoder.getModelYear(eq(vin))).thenReturn(2016);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.d - 10th character of VIN does not match model year of vehicle entered by user earlier in this part");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.3.d", description = " - Manufacturer defined data follows the VIN") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testManufacturerDataFailures() {

        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = mock(VehicleIdentificationPacket.class);
        when(packet.getVin()).thenReturn(vin);
        when(packet.getSourceAddress()).thenReturn(1);
        when(packet.getManufacturerData()).thenReturn("NightHawk");
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn((List.of(packet)));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        WARN,
                                        "6.1.5.3.d - Manufacturer defined data follows the VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.b", description = "More than one OBD ECU responded with VIN") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testMoreThanOneObdRespondedFailure() {

        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet1 = mock(VehicleIdentificationPacket.class);
        when(packet1.getVin()).thenReturn(vin);
        when(packet1.getSourceAddress()).thenReturn(1);
        when(packet1.getManufacturerData()).thenReturn("");
        packets.add(packet1);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet1, 1);
        dataRepository.putObdModule(obdModule1);

        VehicleIdentificationPacket packet2 = mock(VehicleIdentificationPacket.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        packets.add(packet2);
        OBDModuleInformation obdModule2 = new OBDModuleInformation((0));
        obdModule2.set(packet2, 1);
        dataRepository.putObdModule(obdModule2);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn(packets);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.b - More than one OBD ECU responded with VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.5.2.c", description = "VIN does not match user entered VIN") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testVinAndUserEnteredVinMismatchFailure() {

        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // valid vin
        String vin = "2G1WB5E37E1110567";
        VehicleIdentificationPacket packet = mock(VehicleIdentificationPacket.class);
        when(packet.getVin()).thenReturn(vin);
        when(packet.getSourceAddress()).thenReturn(1);
        when(packet.getManufacturerData()).thenReturn("");
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        packets.add(packet);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn((List.of(packet)));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin("1XPBDK9X1LD708195");
        vehicleInformation.setVehicleModelYear(2019);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(true);
        when(vinDecoder.getModelYear(eq(vin))).thenReturn(2019);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.c - VIN does not match user entered VIN");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).getModelYear(vin);
        verify(vinDecoder).isVinValid(vin);
    }

    /**
     * This test will have an error on the nonObdResponses due to error
     * combination restrictions/requirements
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.5.2.e", description = "Invalid VIN 16 chars long") })
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    public void testNot17CharsError() {
        List<VehicleIdentificationPacket> packets = new ArrayList<>();
        // 16 chars long
        String vin = "G1WB5E37E1110567";
        VehicleIdentificationPacket packet = VehicleIdentificationPacket.create(1, vin);
        OBDModuleInformation obdModule1 = new OBDModuleInformation((1));
        obdModule1.set(packet, 1);
        dataRepository.putObdModule(obdModule1);
        packets.add(packet);
        when(vehicleInformationModule.reportVin(any(ResultsListener.class))).thenReturn((List.of(packet)));
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setVin(vin);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(vinDecoder.isVinValid(vin)).thenReturn(false);

        runTest();

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1,
                                        5,
                                        FAIL,
                                        "6.1.5.2.e - VIN is not valid (not 17 legal chars, incorrect checksum, or non-numeric sequence");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any(ResultsListener.class));

        verify(vinDecoder).isVinValid(vin);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.5.2.a", description = "Verify fail reported with no response."))
    public void testPacketsEmpty() {

        when(vehicleInformationModule.reportVin(any())).thenReturn(List.of());

        runTest();

        verify(mockListener).addOutcome(1, 5, FAIL, "6.1.5.2.a - No VIN was provided by any ECU");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportVin(any());
    }



}
