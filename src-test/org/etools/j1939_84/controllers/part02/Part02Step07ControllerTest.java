/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket.create;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part02Step07Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part02Step07ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 2;

    private static final int STEP_NUMBER = 7;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step07Controller instance;

    @Mock
    private J1939 j1939;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private ResultsListener mockListener;

    private TestResultsListener listener;
    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private OBDModuleInformation createOBDModuleInformation(Integer sourceAddress, Integer function, String make,
                                                            String model, String serialNumber, String unitNumber) {
        OBDModuleInformation module = new OBDModuleInformation(sourceAddress);
        module.setFunction(function);

        ComponentIdentificationPacket componentIdentificationPacket = create(
                sourceAddress, make, model, serialNumber, unitNumber);
        module.setComponentIdentification(componentIdentificationPacket.getComponentIdentification());
        return module;
    }
    /*
     * 6.1.9.1 ACTIONS:
     *
     * a. Destination Specific (DS) Component ID request (PGN 59904) for PGN
     * 65259 (SPNs 586, 587, and 588) to each OBD ECU. b. Display each positive
     * return in the log.
     */

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part02Step07Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 mockListener);
    }

    @Test
    public void testDestinationSpecificPacketsEmpty() {
        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.a - There are no positive responses to a DS Component ID request from Transmission #1 (3)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.2.7.5.a - Transmission #1 (3) did not provide a positive respond to global query while engine running");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.2.a - There are no positive responses to a DS Component ID request from Transmission #1 (3)" + NL;
        expectedResults += "WARN: 6.2.7.5.a - Transmission #1 (3) did not provide a positive respond to global query while engine running" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        String name = "Part " + PART_NUMBER + " Step " + STEP_NUMBER;
        assertEquals("Display Name", name, instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Rule public TestName name = new TestName();

    @Test
    public void testGlobalRequestDoesNotMatchDestinationSpecificRequest() {

        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBtCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST10983456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocan");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WW",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        // Global request response
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

        // Destination specific responses
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Engine #1 (0) reported component identification as: ComponentIdentification{make='BatMan', model='TheBtCave', serialNumber='ST109823456', unitNumber='Land'}, Part 01 Step 09 reported it as: ComponentIdentification{make='BatMan', model='TheBatCave', serialNumber='ST109823456', unitNumber='Land'}");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Engine #2 (1) reported component identification as: ComponentIdentification{make='AquaMan', model='TheWater', serialNumber='ST109888765', unitNumber='Ocean'}, Part 01 Step 09 reported it as: ComponentIdentification{make='AquaMan', model='TheWater', serialNumber='ST109888765', unitNumber='Ocan'}");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Turbocharger (2) reported component identification as: ComponentIdentification{make='SuperMan', model='TheCrystalIcePalace', serialNumber='ST10983456', unitNumber='Air'}, Part 01 Step 09 reported it as: ComponentIdentification{make='SuperMan', model='TheCrystalIcePalace', serialNumber='ST109823456', unitNumber='Air'}");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Transmission #1 (3) reported component identification as: ComponentIdentification{make='WonderWoman', model='TheLair', serialNumber='WW109877654', unitNumber='Lasso'}, Part 01 Step 09 reported it as: ComponentIdentification{make='WW', model='TheLair', serialNumber='WW109877654', unitNumber='Lasso'}");

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.2.b - Engine #1 (0) reported component identification as: ComponentIdentification{make='BatMan', model='TheBtCave', serialNumber='ST109823456', unitNumber='Land'}, Part 01 Step 09 reported it as: ComponentIdentification{make='BatMan', model='TheBatCave', serialNumber='ST109823456', unitNumber='Land'}" + NL +
                "FAIL: 6.2.7.2.b - Engine #2 (1) reported component identification as: ComponentIdentification{make='AquaMan', model='TheWater', serialNumber='ST109888765', unitNumber='Ocean'}, Part 01 Step 09 reported it as: ComponentIdentification{make='AquaMan', model='TheWater', serialNumber='ST109888765', unitNumber='Ocan'}" + NL +
                "FAIL: 6.2.7.2.b - Turbocharger (2) reported component identification as: ComponentIdentification{make='SuperMan', model='TheCrystalIcePalace', serialNumber='ST10983456', unitNumber='Air'}, Part 01 Step 09 reported it as: ComponentIdentification{make='SuperMan', model='TheCrystalIcePalace', serialNumber='ST109823456', unitNumber='Air'}" + NL +
                "FAIL: 6.2.7.2.b - Transmission #1 (3) reported component identification as: ComponentIdentification{make='WonderWoman', model='TheLair', serialNumber='WW109877654', unitNumber='Lasso'}, Part 01 Step 09 reported it as: ComponentIdentification{make='WW', model='TheLair', serialNumber='WW109877654', unitNumber='Lasso'}" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testHappyPath() {

        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoFunctionZeroObds() {

        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       4,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.7.4.b - No OBD module claimed function 0");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.4.b - No OBD module claimed function 0" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testZeroFunctionPacketGlobalPacketDiffersFromGlobalResponse() {
        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x00ds = create(0x00,
                                                            "CatWoman",
                                                            "TheBatCave",
                                                            "CW019823456",
                                                            "Roofs");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00ds));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.2.b - Engine #1 (0) reported component identification as: ComponentIdentification{make='CatWoman', model='TheBatCave', serialNumber='CW019823456', unitNumber='Roofs'}, Part 01 Step 09 reported it as: ComponentIdentification{make='BatMan', model='TheBatCave', serialNumber='ST109823456', unitNumber='Land'}");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.b - The Component ID Global responses do not contain a match for Engine #1 (0), which claimed function 0 in Part 1 Step 9");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.2.b - Engine #1 (0) reported component identification as: ComponentIdentification{make='CatWoman', model='TheBatCave', serialNumber='CW019823456', unitNumber='Roofs'}, Part 01 Step 09 reported it as: ComponentIdentification{make='BatMan', model='TheBatCave', serialNumber='ST109823456', unitNumber='Land'}" + NL;
        expectedResults += "FAIL: 6.2.7.4.b - The Component ID Global responses do not contain a match for Engine #1 (0), which claimed function 0 in Part 1 Step 9" + NL;
        assertEquals(expectedResults, listener.getResults());

    }

    @Test
    public void testMoreThanOneModuleWithFunctionZeroFailure() {
        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        0,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.b - The Component ID Global responses do not contain a match for Engine #1 (0), which claimed function 0 in Part 1 Step 9");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.4.b - The Component ID Global responses do not contain a match for Engine #1 (0), which claimed function 0 in Part 1 Step 9" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testPacketsEmptyFailureGlobalRequest() {
        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "WonderWoman",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x01, packet0x02, packet0x03),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.7.4.b - No packet was received for Engine #1 (0) which claimed function 0 in Part 1 Step 9");

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "FAIL: 6.2.7.4.b - No packet was received for Engine #1 (0) which claimed function 0 in Part 1 Step 9" + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testAckReturnedToDsRequest() {
        AcknowledgmentPacket packet0x03 = new AcknowledgmentPacket(Packet.create(0xE800 | 0xA5,
                                                                                 0x00,
                                                                                 0x01,
                                                                                 0xFF,
                                                                                 0xFF,
                                                                                 0xFF,
                                                                                 0xA5,
                                                                                 0xD3,
                                                                                 0xFE,
                                                                                 0x00));

        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "BatMan",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "Land");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaMan",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "SuperMan",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        OBDModuleInformation obdModule0x01 = createOBDModuleInformation(0x01,
                                                                        1,
                                                                        "AquaMan",
                                                                        "TheWater",
                                                                        "ST109888765",
                                                                        "Ocean");

        OBDModuleInformation obdModule0x02 = createOBDModuleInformation(0x02,
                                                                        2,
                                                                        "SuperMan",
                                                                        "TheCrystalIcePalace",
                                                                        "ST109823456",
                                                                        "Air");

        OBDModuleInformation obdModule0x03 = createOBDModuleInformation(0x03,
                                                                        3,
                                                                        "WonderWoman",
                                                                        "TheLair",
                                                                        "WW109877654",
                                                                        "Lasso");

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdMoule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);

        // Global request response
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet0x00, packet0x01, packet0x02),
                                                List.of(packet0x03)));

        // Destination specific responses
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.2.7.5.a - Transmission #1 (3) did not provide a positive respond to global query while engine running");

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "WARN: 6.2.7.5.a - Transmission #1 (3) did not provide a positive respond to global query while engine running" + NL;
        assertEquals(expectedResults, listener.getResults());
    }
}
