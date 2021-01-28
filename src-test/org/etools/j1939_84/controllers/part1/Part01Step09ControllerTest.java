/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket.createComponentIdPacket;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939_84.utils.CollectionUtils.join;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
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

    private static final String COLON_SPACE = ": ";

    private static final String EXPECTED_WARN_MESSAGE_3_D = "6.1.9.3.d Model field (SPN 587) is less than 1 character long";

    private static final String EXPECTED_FAIL_MESSAGE_1_A = "6.1.9.1.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)";

    private static final String EXPECTED_FAIL_MESSAGE_2_B = "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)";

    private static final String EXPECTED_FAIL_MESSAGE_2_C = "6.1.9.2.c Serial number field (SPN 588) from any function 0 device does not end in 6 numeric characters (ASCII 0 through ASCII 9)";

    private static final String EXPECTED_FAIL_MESSAGE_2_D = "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_5_A = "6.1.9.5.a There is no positive response from function 0";

    private static final String EXPECTED_FAIL_MESSAGE_5_B = "6.1.9.5.b Global response does not match the destination specific response from function 0";

    private static final String EXPECTED_FAIL_MESSAGE_6_A = "6.1.9.6.a Component ID not supported for the global query, when supported by destination specific query";

    private static final String EXPECTED_WARN_MESSAGE_3_A = "6.1.9.3.a Serial number field (SPN 588) from any function 0 device is less than 8 characters long";

    private static final String EXPECTED_WARN_MESSAGE_3_B = "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters";

    private static final String EXPECTED_WARN_MESSAGE_3_C = "6.1.9.3.c Make field (SPN 586) is less than 2 ASCII characters";

    private static final String EXPECTED_WARN_MESSAGE_4_A_4_B = "6.1.9.4.a & 6.1.9.4.b Global Component ID request for PGN 65259 did not receive any packets";

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 9;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step09Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private OBDModuleInformation createOBDModuleInformation(Integer sourceAddress, Integer function, String make,
                                                            String model, String serialNumber, String unitNumber) {
        OBDModuleInformation module = new OBDModuleInformation(sourceAddress);
        module.setFunction(function);

        byte[] bytes = join(make.getBytes(UTF_8),
                            "*".getBytes(UTF_8),
                            model.getBytes(UTF_8),
                            "*".getBytes(UTF_8),
                            serialNumber.getBytes(UTF_8),
                            "*".getBytes(UTF_8),
                            unitNumber.getBytes(UTF_8));

        ComponentIdentificationPacket componentIdentificationPacket = new ComponentIdentificationPacket(
                Packet.create(ComponentIdentificationPacket.PGN, sourceAddress, bytes)
        );
        module.setComponentInformationIdentification(componentIdentificationPacket.getComponentIdentification());
        return module;
    }

    private OBDModuleInformation createOBDModuleInformation(Integer sourceAddress, Integer function) {
        OBDModuleInformation module = mock(OBDModuleInformation.class);
        if (sourceAddress != null) {
            when(module.getSourceAddress()).thenReturn(sourceAddress);
        }
        if (function != null) {
            when(module.getFunction()).thenReturn(function);
        }
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
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 dataRepository,
                                 mockListener,
                                 reportFileModule);
    }

    @Test
    public void testDestinationSpecificPacketsEmpty() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0x00,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        runTest();

        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_1_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_1_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_1_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_B);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B);

        String functionZeroWarning = "0 module(s) have claimed function 0 - only one module should";
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + functionZeroWarning);

        verify(vehicleInformationModule).reportComponentIdentification(any());
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x00));

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_1_A + NL + WARN.toString() + COLON_SPACE + functionZeroWarning + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_B + NL +
                WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A + NL +
                WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B + NL;
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

    @Test
    public void testGlobalRequestDoesNotMatchDestinationSpecificRequest() throws IOException {
        ComponentIdentificationPacket packet = ComponentIdentificationPacket.createComponentIdPacket(0,
                                                                                                     "Bat",
                                                                                                     "TheBatCave",
                                                                                                     "ST109823456",
                                                                                                     "");

        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdMoule0x00);
        when(dataRepository.getObdModules()).thenReturn(List.of(obdMoule0x00));

        // Global request response
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(),
                                                List.of()));

        // Destination specific responses
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdMoule0x00);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_6_A);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_6_A);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_6_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = WARN.toString().trim() + COLON_SPACE + EXPECTED_WARN_MESSAGE_4_A_4_B + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_6_A + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testHappyPath() throws IOException {
        ComponentIdentificationPacket packet0x00 = createComponentIdPacket(0,
                                                                           "Bat",
                                                                           "TheBatCave",
                                                                           "ST109823456",
                                                                           "");
        ComponentIdentificationPacket packet0x01 = createComponentIdPacket(0x01,
                                                                           "AquaMan",
                                                                           "TheWater",
                                                                           "ST109888765",
                                                                           "Ocean");
        ComponentIdentificationPacket packet0x02 = createComponentIdPacket(0x02,
                                                                           "SuperMan",
                                                                           "TheCrystalIcePalace",
                                                                           "ST109823456",
                                                                           "Air");
        ComponentIdentificationPacket packet0x03 = createComponentIdPacket(0x03,
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
        when(dataRepository.getObdModule(0x00)).thenReturn(obdMoule0x00);
        when(dataRepository.getObdModule(0x01)).thenReturn(obdModule0x01);
        when(dataRepository.getObdModule(0x02)).thenReturn(obdModule0x02);
        when(dataRepository.getObdModule(0x03)).thenReturn(obdModule0x03);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                Collections.emptyList()));

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
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x01));
        verify(dataRepository).getObdModule(eq(0x02));
        verify(dataRepository).getObdModule(eq(0x03));
        verify(dataRepository).putObdModule(obdMoule0x00);
        verify(dataRepository).putObdModule(obdModule0x01);
        verify(dataRepository).putObdModule(obdModule0x02);
        verify(dataRepository).putObdModule(obdModule0x03);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testMakeContainsNonPrintableAsciiCharacterFailure() throws IOException {
        char unprintableAsciiLineFeed = 0xa;
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                                                                       "Bat" + unprintableAsciiLineFeed,
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "");

        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdMoule0x00);
        when(dataRepository.getObdModules()).thenReturn(List.of(obdMoule0x00));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdMoule0x00);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D + NL, listener.getResults());
    }

    @Test
    public void testMakeFieldMoreThanFiveCharacters() throws IOException {

        ComponentIdentificationPacket packet = createComponentIdPacket(0,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModule);
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B + NL, listener.getResults());

    }

    @Test
    public void testMakeFiveCharactersWarning() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B + NL, listener.getResults());
    }

    @Test
    public void testMakeLessTwoAsciiCharactersWarning() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_C + NL, listener.getResults());

    }

    @Test
    public void testModelContainsNonPrintableAsciiCharacterFailure() throws IOException {
        //char unprintableAsciiNull = 0x0;
        char unprintableAsciiCarriageReturn = 0xD;//0xD;
        String model = unprintableAsciiCarriageReturn + "TheBatCave";
        ComponentIdentificationPacket packet = createComponentIdPacket(0x00,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet),
                                                Collections.emptyList()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D + NL, listener.getResults());
    }

    @Test
    public void testModelLessThanOneCharactersWarning() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D + NL, listener.getResults());

    }

    @Test
    public void testMoreThanOneModuleWithFunctionZeroFailure() throws IOException {
        ComponentIdentificationPacket packet0x00 = createComponentIdPacket(0,
                                                                           "Bat",
                                                                           "TheBatCave",
                                                                           "ST123456",
                                                                           "");

        ComponentIdentificationPacket packet0x01 = createComponentIdPacket(0x01,
                                                                           "AquaMan",
                                                                           "TheWater",
                                                                           "ST109888765",
                                                                           "Ocean");

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
        when(dataRepository.getObdModules())
                .thenReturn(List.of(obdMoule0x00, obdModule0x01));
        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdMoule0x00);
        when(dataRepository.getObdModule(eq(0x01))).thenReturn(obdModule0x01);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet0x00, packet0x01),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet0x00));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet0x01));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x01));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdMoule0x00);
        verify(dataRepository).putObdModule(obdModule0x01);

        String expected2ModulesWarnMessage = "2 module(s) have claimed function 0 - only one module should";
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + expected2ModulesWarnMessage);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + expected2ModulesWarnMessage + NL, listener.getResults());
    }

    @Test
    public void testPacketsEmptyFailureGlobalRequest() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
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

        when(dataRepository.getObdModules()).thenReturn(List.of(obdModule));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModule);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdModule);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_6_A);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_6_A);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_4_A_4_B);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_6_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        String expectedResults = WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_4_A_4_B + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_6_A + NL;
        assertEquals(expectedResults, listener.getResults());
    }

    @Test
    public void testSerialNumberContainsAsciiNonPrintableCharacterFailure() throws IOException {
        char unprintableAsciiNull = 0x0;
        String serialNumber = "ST" + unprintableAsciiNull + "109823456";
        ComponentIdentificationPacket packet0x00 = createComponentIdPacket(0x00,
                                                                           "Bat",
                                                                           "TheBatCave",
                                                                           serialNumber,
                                                                           "");

        ComponentIdentificationPacket packet0x01 = createComponentIdPacket(0x01,
                                                                           "AquaMan",
                                                                           "TheWater",
                                                                           "ST109888765",
                                                                           "Ocean");

        ComponentIdentificationPacket packet0x02 = createComponentIdPacket(0x02,
                                                                           "SuperMan",
                                                                           "TheCrystalIcePalace",
                                                                           "ST109823456",
                                                                           "Air");

        ComponentIdentificationPacket packet0x03 = createComponentIdPacket(0x03,
                                                                           "WonderWoman",
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

        List<OBDModuleInformation> obdModules = new ArrayList<>();
        obdModules.add(obdModule0x00);
        obdModules.add(obdModule0x01);
        obdModules.add(obdModule0x02);
        obdModules.add(obdModule0x03);

        when(dataRepository.getObdModules()).thenReturn(obdModules);
        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdModule0x00);
        when(dataRepository.getObdModule(eq(0x01))).thenReturn(obdModule0x01);
        when(dataRepository.getObdModule(eq(0x02))).thenReturn(obdModule0x02);
        when(dataRepository.getObdModule(eq(0x03))).thenReturn(obdModule0x03);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(packet0x00, packet0x01, packet0x02, packet0x03),
                                                List.of()));

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
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x01));
        verify(dataRepository).getObdModule(eq(0x02));
        verify(dataRepository).getObdModule(eq(0x03));
        verify(dataRepository).putObdModule(obdModule0x00);
        verify(dataRepository).putObdModule(obdModule0x01);
        verify(dataRepository).putObdModule(obdModule0x02);
        verify(dataRepository).putObdModule(obdModule0x03);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_D + NL, listener.getResults());
    }

    @Test
    public void testSerialNumberEightCharactersWarning() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                                                                       "Bat",
                                                                       "TheBatCave",
                                                                       "S123456",
                                                                       "");

        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "BatMan",
                                                                       "TheBatCave",
                                                                       "ST109823456",
                                                                       "Land");

        when(dataRepository.getObdModules()).thenReturn(List.of(obdMoule0x00));
        when(dataRepository.getObdModule(0x00)).thenReturn(obdMoule0x00);

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet),
                                                List.of()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0x00)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModules();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).putObdModule(eq(obdMoule0x00));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0x00));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A + NL, listener.getResults());

    }

    @Test
    public void testSerialNumberEndsWithNonNumericCharacterInLastSixCharactersFailure() throws IOException {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                                                                       "Bat",
                                                                       "TheBatCave",
                                                                       "ST109823J456",
                                                                       "Land");

        OBDModuleInformation obdMoule0x00 = createOBDModuleInformation(0x00,
                                                                       0,
                                                                       "Bat",
                                                                       "TheBatCave",
                                                                       "ST109823J456",
                                                                       "Land");

        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdMoule0x00);
        when(dataRepository.getObdModules()).thenReturn(List.of(obdMoule0x00));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, List.of(packet),
                                                Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdMoule0x00);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_C + NL, listener.getResults());
    }
}
