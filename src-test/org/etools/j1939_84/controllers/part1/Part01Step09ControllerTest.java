/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

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

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step09Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step09ControllerTest extends AbstractControllerTest {

    private static final String EXPECTED_WARN_MESSAGE_3_D = "6.1.9.3.d - The model field (SPN 587) from Engine #1 (0) is less than 1 ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_A = "6.1.9.2.a - There are no positive responses";

    private static final String EXPECTED_FAIL_MESSAGE_2_B = "6.1.9.2.b - None of the positive responses were provided by Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_2_C = "6.1.9.2.c - Serial number field (SPN 588) from Engine #1 (0) does not end in 6 numeric characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_MAKE = "6.1.9.2.d - The make (SPN 586) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_MODEL = "6.1.9.2.d - The model (SPN 587) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_SN = "6.1.9.2.d - The serial number (SPN 588) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_5_A = "6.1.9.5.a - There is no positive response from Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_5_B = "6.1.9.5.b - Global response does not match the destination specific response from Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_6_A = "6.1.9.6.a - Engine #2 (1) did not supported the Component ID for the global query, but supported it in the destination specific query";

    private static final String EXPECTED_WARN_MESSAGE_3_A = "6.1.9.3.a - Serial number field (SPN 588) from Engine #1 (0) is less than 8 characters long";

    private static final String EXPECTED_WARN_MESSAGE_3_B = "6.1.9.3.b - The make field (SPN 586) from Engine #1 (0) is longer than 5 ASCII characters";

    private static final String EXPECTED_WARN_MESSAGE_3_C = "6.1.9.3.c - The make field (SPN 586) from Engine #1 (0) is less than 2 ASCII characters";

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 9;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step09Controller instance;

    @Mock
    private J1939 j1939;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private ResultsListener mockListener;

    private TestResultsListener listener;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private static OBDModuleInformation createOBDModuleInformation(int sourceAddress,
                                                                   int function,
                                                                   String make,
                                                                   String model,
                                                                   String serialNumber,
                                                                   String unitNumber) {
        OBDModuleInformation module = new OBDModuleInformation(sourceAddress);
        module.setFunction(function);
        ComponentIdentificationPacket componentIdentificationPacket = create(sourceAddress,
                                                                             make,
                                                                             model,
                                                                             serialNumber,
                                                                             unitNumber);
        module.setComponentInformationIdentification(componentIdentificationPacket.getComponentIdentification());
        return module;
    }

    @Before
    public void setUp() {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step09Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                DateTimeModule.getInstance());

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Part " + PART_NUMBER + " Step " + STEP_NUMBER, instance.getDisplayName());
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
    public void testDestinationSpecificPacketsEmpty() {
        ComponentIdentificationPacket packet = create(0x00,
                                                      "BatMan",
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "Land");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "BatMan",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x00));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = "FAIL: " + EXPECTED_FAIL_MESSAGE_2_A + NL +
                "Function 0 module is Engine #1 (0)" + NL +
                "FAIL: " + EXPECTED_FAIL_MESSAGE_2_B + NL +
                "FAIL: " + EXPECTED_FAIL_MESSAGE_5_B + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testDestinationSpecificSupportWithoutGlobalSupport() {
        ComponentIdentificationPacket packet0 = create(0,
                                                       "BatMa",
                                                       "TheBatCave",
                                                       "ST109823456",
                                                       "Land");
        var packet1 = create(1, "make", "model", "SN", "unit");

        OBDModuleInformation obdModule0 = createOBDModuleInformation(0x00,
                                                                     0,
                                                                     "BatMan",
                                                                     "TheBatCave",
                                                                     "ST109823456",
                                                                     "Land");

        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(new OBDModuleInformation(1));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet0));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet1));
        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_6_A);

        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x00));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x01));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = "" +
                "Function 0 module is Engine #1 (0)" + NL +
                "FAIL: " + EXPECTED_FAIL_MESSAGE_6_A + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testGlobalRequestDoesNotMatchDestinationSpecificRequest() {
        ComponentIdentificationPacket packet1 = create(0,
                                                       "Bat",
                                                       "TheBatCave",
                                                       "ST109823456",
                                                       "");

        ComponentIdentificationPacket packet2 = create(0, "", "", "", "");

        OBDModuleInformation obdModule0x00 = createOBDModuleInformation(0x00,
                                                                        0,
                                                                        "BatMan",
                                                                        "TheBatCave",
                                                                        "ST109823456",
                                                                        "Land");

        dataRepository.putObdModule(obdModule0x00);

        // Global request response
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet2));

        // Destination specific responses
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = "" +
                "Function 0 module is Engine #1 (0)" + NL +
                "FAIL: " + EXPECTED_FAIL_MESSAGE_5_B + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testHappyPath() {
        ComponentIdentificationPacket packet0x00 = create(0,
                                                          "Bat",
                                                          "TheBatCave",
                                                          "ST109823456",
                                                          "");
        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaM",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");
        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "Super",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");
        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "Wonde",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0);
        obdModule0.setFunction(0);
        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet0x00, packet0x01, packet0x02, packet0x03));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x01)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x02)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x03)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();
assertEquals(packet0x00.getComponentIdentification(), dataRepository.getObdModule(0).getComponentIdentification());
        assertEquals(packet0x01.getComponentIdentification(), dataRepository.getObdModule(1).getComponentIdentification());
        assertEquals(packet0x02.getComponentIdentification(), dataRepository.getObdModule(2).getComponentIdentification());
        assertEquals(packet0x03.getComponentIdentification(), dataRepository.getObdModule(3).getComponentIdentification());

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL, listener.getResults());
    }

    @Test
    public void testMakeContainsNonPrintableAsciiCharacterFailure() {
        char unprintableAsciiLineFeed = 0x0A;
        ComponentIdentificationPacket packet = create(0,
                                                      "Bat" + unprintableAsciiLineFeed,
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "");

        OBDModuleInformation obdModule0x00 = createOBDModuleInformation(0x00,
                                                                        0,
                                                                        "BatMan",
                                                                        "TheBatCave",
                                                                        "ST109823456",
                                                                        "Land");

        dataRepository.putObdModule(obdModule0x00);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_MAKE);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "FAIL: " + EXPECTED_FAIL_MESSAGE_2_D_MAKE + NL,
                     listener.getResults());
    }

    @Test
    public void testMakeFieldMoreThanFiveCharacters() {

        ComponentIdentificationPacket packet = create(0,
                                                      "BatMan",
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "BatMan",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "WARN: " + EXPECTED_WARN_MESSAGE_3_B + NL,
                     listener.getResults());

    }

    @Test
    public void testMakeFiveCharactersWarning() {
        ComponentIdentificationPacket packet = create(0,
                                                      "BatMan",
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "BatMan",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "WARN: " + EXPECTED_WARN_MESSAGE_3_B + NL,
                     listener.getResults());
    }

    @Test
    public void testMakeLessTwoAsciiCharactersWarning() {
        ComponentIdentificationPacket packet = create(0,
                                                      "B",
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "Land");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "B",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals("Function 0 module is Engine #1 (0)" + NL + "WARN: " + EXPECTED_WARN_MESSAGE_3_C + NL,
                     listener.getResults());

    }

    @Test
    public void testModelContainsNonPrintableAsciiCharacterFailure() {
        //char unprintableAsciiNull = 0x0;
        char unprintableAsciiCarriageReturn = 0xD;//0xD;
        String model = unprintableAsciiCarriageReturn + "TheBatCave";
        ComponentIdentificationPacket packet = create(0x00,
                                                      "Bat",
                                                      model,
                                                      "ST109823456",
                                                      "Land");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "Bat",
                                                                    model,
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_MODEL);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "FAIL: " + EXPECTED_FAIL_MESSAGE_2_D_MODEL + NL,
                     listener.getResults());
    }

    @Test
    public void testModelLessThanOneCharactersWarning() {
        ComponentIdentificationPacket packet = create(0,
                                                      "Bat",
                                                      "",
                                                      "ST123456",
                                                      "");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "Bat",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");

        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "WARN: " + EXPECTED_WARN_MESSAGE_3_D + NL,
                     listener.getResults());

    }

    @Test
    public void testPacketsEmptyFailureGlobalRequest() {
        ComponentIdentificationPacket packet = create(0,
                                                      "Bat",
                                                      "TheBatCave",
                                                      "ST109823456",
                                                      "");

        OBDModuleInformation obdModule = createOBDModuleInformation(0x00,
                                                                    0,
                                                                    "BatMan",
                                                                    "TheBatCave",
                                                                    "ST109823456",
                                                                    "Land");
        dataRepository.putObdModule(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = "Function 0 module is Engine #1 (0)" + NL + "FAIL: " + EXPECTED_FAIL_MESSAGE_5_A + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testSerialNumberContainsAsciiNonPrintableCharacterFailure() {
        char unprintableAsciiNull = 0x0;
        String serialNumber = "ST" + unprintableAsciiNull + "109823456";
        ComponentIdentificationPacket packet0x00 = create(0x00,
                                                          "Bat",
                                                          "TheBatCave",
                                                          serialNumber,
                                                          "");

        ComponentIdentificationPacket packet0x01 = create(0x01,
                                                          "AquaM",
                                                          "TheWater",
                                                          "ST109888765",
                                                          "Ocean");

        ComponentIdentificationPacket packet0x02 = create(0x02,
                                                          "Super",
                                                          "TheCrystalIcePalace",
                                                          "ST109823456",
                                                          "Air");

        ComponentIdentificationPacket packet0x03 = create(0x03,
                                                          "Wonde",
                                                          "TheLair",
                                                          "WW109877654",
                                                          "Lasso");

        OBDModuleInformation obdModule0x00 = createOBDModuleInformation(0x00,
                                                                        0,
                                                                        "BatMan",
                                                                        "TheBatCave",
                                                                        serialNumber,
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

        dataRepository.putObdModule(obdModule0x00);
        dataRepository.putObdModule(obdModule0x01);
        dataRepository.putObdModule(obdModule0x02);
        dataRepository.putObdModule(obdModule0x03);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet0x00, packet0x01, packet0x02, packet0x03));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet0x01));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, packet0x02));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, packet0x03));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_SN);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "FAIL: " + EXPECTED_FAIL_MESSAGE_2_D_SN + NL,
                     listener.getResults());
    }

    @Test
    public void testSerialNumberEightCharactersWarning() {
        ComponentIdentificationPacket packet = create(0,
                                                      "Bat",
                                                      "TheBatCave",
                                                      "S123456",
                                                      "");

        OBDModuleInformation obdModule0x00 = createOBDModuleInformation(0x00,
                                                                        0,
                                                                        "BatMan",
                                                                        "TheBatCave",
                                                                        "ST109823456",
                                                                        "Land");

        dataRepository.putObdModule(obdModule0x00);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x00));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals("Function 0 module is Engine #1 (0)" + NL + "WARN: " + EXPECTED_WARN_MESSAGE_3_A + NL,
                     listener.getResults());
    }

    @Test
    public void testSerialNumberEndsWithNonNumericCharacterInLastSixCharactersFailure() {
        ComponentIdentificationPacket packet = create(0,
                                                      "Bat",
                                                      "TheBatCave",
                                                      "ST109823J456",
                                                      "Land");

        OBDModuleInformation obdModule0x00 = createOBDModuleInformation(0x00,
                                                                        0,
                                                                        "Bat",
                                                                        "TheBatCave",
                                                                        "ST109823J456",
                                                                        "Land");

        dataRepository.putObdModule(obdModule0x00);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("Function 0 module is Engine #1 (0)" + NL + "FAIL: " + EXPECTED_FAIL_MESSAGE_2_C + NL,
                     listener.getResults());
    }
}
