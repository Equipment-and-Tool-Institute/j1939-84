/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static java.util.Map.entry;
import static org.etools.j1939_84.J1939_84.NL;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
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
 * The unit test for {@link Step09Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step09ControllerTest extends AbstractControllerTest {

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

    /*
     * All values must be checked prior to mocking so that we are not creating
     * unnecessary mocks.
     */
    private static ComponentIdentificationPacket createComponentIdPacket(Integer sourceAddress,
            String make,
            String model,
            String serialNumber) {
        ComponentIdentificationPacket packet = mock(ComponentIdentificationPacket.class);
        if (sourceAddress != null) {
            when(packet.getSourceAddress()).thenReturn(sourceAddress);
        }
        if (make != null) {
            when(packet.getMake()).thenReturn(make);
        }
        if (model != null) {
            when(packet.getModel()).thenReturn(model);
        }
        if (serialNumber != null) {
            when(packet.getSerialNumber()).thenReturn(serialNumber);
        }

        return packet;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step09Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDModuleInformation obdModuleInformation;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

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

        instance = new Step09Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository);

        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);

    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                obdModuleInformation,
                vehicleInformationModule,
                dataRepository,
                mockListener,
                reportFileModule);
    }

    @Test
    public void testDestinationSpecificPacketsEmpty() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 4),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        // Return and empty list of modules
        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_1_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_1_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_1_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_B);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_A);
        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_5_B);

        String functionZeroWarning = "0 module(s) have claimed function 0 - only one module should";
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + functionZeroWarning);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        String expectedResults = FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_1_A + NL + WARN.toString() + COLON_SPACE + functionZeroWarning + NL +
                FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_B + NL +
                WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A + NL +
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
    public void testGlobalRequestDoesNotMatchDestinationSpecificRequest() {

        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = new HashMap<>() {
            {
                put(0, 0);
                put(1, 1);
                put(2, 2);
                put(3, 3);
            }
        };

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        // Global request response
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        Collections.emptyList()));

        // Destination specific responses
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
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
    public void testHappyPath() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
    public void testMakeContainsNonPrintableAsciiCharacterFailure() {
        char unprintableAsciiLineFeed = 0xa;
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat" + unprintableAsciiLineFeed,
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
    public void testMakeFieldMoreThanFiveCharacters() {

        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "BatMan",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()).stream()
                        .sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B + NL, listener.getResults());

    }

    @Test
    public void testMakeFiveCharactersWarning() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "BatMan",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_B + NL, listener.getResults());
    }

    @Test
    public void testMakeLessTwoAsciiCharactersWarning() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "B",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_C + NL, listener.getResults());

    }

    @Test
    public void testModelContainsNonPrintableAsciiCharacterFailure() {
        char unprintableAsciiCarriageReturn = 0xd;
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                ("TheBatCave" + unprintableAsciiCarriageReturn),
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
    public void testModelLessThanOneCharactersWarning() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "",
                "ST123456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_D + NL, listener.getResults());

    }

    @Test
    public void testMoreThanOneModuleWithFunctionZeroFailure() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST123456");
        ComponentIdentificationPacket packet1 = createComponentIdPacket(1,
                "WW",
                "TheInvisibleJet",
                "IJ345612");
        List<ComponentIdentificationPacket> packets = new ArrayList<>() {
            {
                add(packet);
                add(packet1);
            }
        };

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 0),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, packets,
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet1));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        int twice = 2;

        String expected2ModulesWarnMessage = "2 module(s) have claimed function 0 - only one module should";
        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + expected2ModulesWarnMessage);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(twice));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(WARN.toString() + COLON_SPACE + expected2ModulesWarnMessage + NL, listener.getResults());
    }

    @Test
    public void testPacketsEmptyFailureGlobalRequest() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST109823456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
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
    public void testSerialNumberContainsAsciiNonPrintableCharacterFailure() {
        char unprintableAsciiNull = 0x0;
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                ("ST" + unprintableAsciiNull + "109823456"));

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

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
    public void testSerialNumberEightCharactersWarning() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "S123456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_A);

        verify(reportFileModule).onResult(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());

        assertEquals(WARN.toString() + COLON_SPACE + EXPECTED_WARN_MESSAGE_3_A + NL, listener.getResults());

    }

    @Test
    public void testSerialNumberEndsWithNonNumericCharacterInLastSixCharactersFailure() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST109823J456");

        Map<Integer, Integer> moduleAddressFunction = Map.ofEntries(
                entry(0, 0),
                entry(1, 1),
                entry(2, 2),
                entry(3, 3));

        when(dataRepository.getObdModuleAddresses())
                .thenReturn(moduleAddressFunction.keySet().stream().sorted().collect(Collectors.toList()));

        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet),
                        Collections.emptyList()));

        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(2)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(3)))
                .thenReturn(new BusResult<>(false, Optional.empty()));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue()));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModules();

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);
        verify(reportFileModule).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);

        verify(reportFileModule).onResult(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_C);

        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(0));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(1));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(2));
        verify(vehicleInformationModule).reportComponentIdentification(any(), eq(3));
        verify(vehicleInformationModule).reportComponentIdentification(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(FAIL.toString() + COLON_SPACE + EXPECTED_FAIL_MESSAGE_2_C + NL, listener.getResults());
    }
}
