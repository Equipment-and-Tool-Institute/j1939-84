/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.ComponentIdentificationPacket;
import org.etools.j1939_84.bus.j1939.packets.DM19CalibrationInformationPacket.CalibrationInformation;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
    private AcknowledgmentPacket acknowledgmentPacket;

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;
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
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private OBDModuleInformation createOBDModuleInformation(Integer sourceAddress,
            Integer function,
            Byte obdCompliance,
            List<CalibrationInformation> calibrationInfoList,
            List<SupportedSPN> dataStreamSpns,
            List<SupportedSPN> freezeFrameSpns,
            List<SupportedSPN> supportedSpns,
            List<SupportedSPN> testResultSpns,
            List<ScaledTestResult> scaledTestResult) {
        OBDModuleInformation module = mock(OBDModuleInformation.class);
        if (sourceAddress != null) {
            when(module.getSourceAddress()).thenReturn(sourceAddress);
        }
        if (function != null) {
            when(module.getFunction()).thenReturn(function);
        }
        if (calibrationInfoList != null) {
            when(module.getCalibrationInformation()).thenReturn(calibrationInfoList);
        }
        if (dataStreamSpns != null) {
            when(module.getDataStreamSpns()).thenReturn(dataStreamSpns);
        }
        if (freezeFrameSpns != null) {
            when(module.getFreezeFrameSpns()).thenReturn(freezeFrameSpns);
        }
        if (supportedSpns != null) {
            when(module.getSupportedSpns()).thenReturn(supportedSpns);
        }
        if (testResultSpns != null) {
            when(module.getTestResultSpns()).thenReturn(testResultSpns);
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
        dateTimeModule = new TestDateTimeModule();

        instance = new Step09Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                partResultFactory,
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
                partResultFactory,
                dataRepository,
                mockListener,
                reportFileModule);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 9", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {

        assertEquals(9, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testGloabalRequestDoesNotMatchDestinationSpecificRequest() {

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

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.6.a Component ID not supported for the global query in 6.1.9.4, when supported by destination specific query");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.6.a Component ID not supported for the global query in 6.1.9.4, when supported by destination specific query");

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
    public void testGlobalPacketMatchFailure() {

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(reportFileModule).onProgress(0, 1, "");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.b Make field (SPN 586) is longer than 5 ASCII characters");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.c Make field (SPN 586) is less than 2 ASCII characters");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.c Make field (SPN 586) is less than 2 ASCII characters");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.d Model field (SPN 587) is less than 1 character long");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.d Model field (SPN 587) is less than 1 character long");

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
    public void testMoreThanOneModuleWithFunctionZeroFailure() {
        ComponentIdentificationPacket packet = createComponentIdPacket(0,
                "Bat",
                "TheBatCave",
                "ST123456");
        ComponentIdentificationPacket packet1 = createComponentIdPacket(1,
                "WW",
                "TheInvisibleJet",
                "IJ345612");
        List<ComponentIdentificationPacket> packetList = new ArrayList<>() {
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
                .thenReturn(packetList);

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(1)))
                .thenReturn(new BusResult<>(false, packet1));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.5.b None of the positive responses are provided by the same SA as the SA that claims to be function 0 (engine). (SPN 588 ESN not supported by the engine function)");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.5.b None of the positive responses are provided by the same SA as the SA that claims to be function 0 (engine). (SPN 588 ESN not supported by the engine function)");

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
    public void testNoModuleWithFunctionZero() {
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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        // Return and empty list of modules
        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");
        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.5.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.5.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");

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
    public void testPacketsEmptyFailure() {
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

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any()))
                .thenReturn(Collections.singletonList(packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.1.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");
        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.1.a There are no positive responses (serial number SPN 588 not supported by any OBD ECU)");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.b None of the positive responses were provided by the same SA as the SA that claims to be function 0 (engine)");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        // OBDModuleInformation obdModuleInfomation = new
        // OBDModuleInformation(0);
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        // verify(dataRepository).getObdModule(eq(0));
        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.d The make (SPN 586), model (SPN 587), or serial number (SPN 588) from any OBD ECU contains any unprintable ASCII characters.");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.a Serial number field (SPN 588) from any function 0 device is less than 8 characters long");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.WARN,
                "6.1.9.3.a Serial number field (SPN 588) from any function 0 device is less than 8 characters long");

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
                .thenReturn(Collections.singletonList(packet));

        when(vehicleInformationModule.reportComponentIdentification(any(), ArgumentMatchers.anyInt()))
                .thenReturn(new BusResult<>(false, Optional.empty()));
        when(vehicleInformationModule.reportComponentIdentification(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet));

        List<OBDModuleInformation> obdModuleInformations = new ArrayList<>();
        for (Entry<Integer, Integer> address : moduleAddressFunction.entrySet()) {
            obdModuleInformations
                    .add(createOBDModuleInformation(address
                            .getKey(), address.getValue(), (byte) 0, null, null, null, null, null, null));
        }
        when(dataRepository.getObdModules()).thenReturn(obdModuleInformations);

        runTest();

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository, times(2)).getObdModules();

        verify(mockListener).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.c Serial number field (SPN 588) from any function 0 device does not end in 6 numeric characters (ASCII 0 through ASCII 9)");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                9,
                Outcome.FAIL,
                "6.1.9.2.c Serial number field (SPN 588) from any function 0 device does not end in 6 numeric characters (ASCII 0 through ASCII 9)");

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
}
