/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket.create;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.ComponentIdentificationPacket;
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
 * The unit test for {@link Part01Step09Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
public class Part01Step09ControllerTest extends AbstractControllerTest {

    private static final String EXPECTED_WARN_MESSAGE_3_D = "6.1.9.3.d - The model field (SP 587) from Engine #1 (0) is less than one ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_A = "6.1.9.2.a - There are no positive responses";

    private static final String EXPECTED_FAIL_MESSAGE_2_B = "6.1.9.2.b - None of the positive responses were provided by Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_2_C = "6.1.9.2.c - Serial number field (SP 588) from Engine #1 (0) does not end in five numeric characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_MAKE = "6.1.9.2.d - The make (SP 586) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_MODEL = "6.1.9.2.d - The model (SP 587) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_2_D_SN = "6.1.9.2.d - The serial number (SP 588) from Engine #1 (0) contains any unprintable ASCII characters";

    private static final String EXPECTED_FAIL_MESSAGE_5_A = "6.1.9.5.a - There is no positive response from Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_5_B = "6.1.9.5.b - Global response does not match the destination specific response from Engine #1 (0)";

    private static final String EXPECTED_FAIL_MESSAGE_6_A = "6.1.9.6.a - Engine #2 (1) did not supported the Component ID for the global query, but supported it in the destination specific query";

    private static final String EXPECTED_INFO_MESSAGE_3_A = "6.1.9.3.a - Serial number field (SP 588) from Engine #1 (0) is less than eight characters long";

    private static final String EXPECTED_WARN_MESSAGE_3_B = "6.1.9.3.b - The make field (SP 586) from Engine #1 (0) is longer than five ASCII characters";

    private static final String EXPECTED_WARN_MESSAGE_3_C = "6.1.9.3.c - The make field (SP 586) from Engine #1 (0) is less than two ASCII characters";

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 9;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

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
        OBDModuleInformation module = new OBDModuleInformation(sourceAddress, function);
        module.set(create(sourceAddress, make, model, serialNumber, unitNumber), 1);
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

    /**
     * Test module responds with correct ComponentIdentificationPacket to global
     * request: destination specific requests returns a bus result without a packet
     * and retries marked false (not used)
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.2.a", description = "Fail if there are no positive responses (serial number SP 588 not supported by any OBD ECU)."),
            @TestItem(verifies = "6.1.9.2.b", description = "Fail if none of the positive responses are provided by the same SA as the SA that claims to be function 0 (engine).  (SP 588 ESN not supported by the engine function)."),
            @TestItem(verifies = "6.1.9.5.b", description = "Fail if the global response does not match the destination specific response from function 0.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(BusResult.empty());

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN), any(ResultsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(ResultsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_A);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_B);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with correct ComponentIdentificationPacket to global
     * request: destination specific requests returns a packet marked
     * with global support marked false (not used)
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.6.a", description = "Warn if Component ID not supported for the global query in 6.1.9.4, when supported by destination specific query.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(
                                                                                                                              RequestResult.of(packet0));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet1));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_FAIL_MESSAGE_6_A);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module returns empty ComponentIdentificationPacket to global request:
     * destination specific requests returns a good response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.5.b", description = "Fail if the global response does not match the destination specific response from function 0.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet2));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(ResultsListener.class)))
                                                                      .thenReturn(new BusResult<>(false, packet1));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x0),
                                             any(ResultsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_B);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with correct ComponentIdentificationPacket to global
     * request: destination specific requests yields a good response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.1.a", description = "Destination Specific (DS) Component ID request (PGNPG 59904) for PGNPG 65259 (SPs 586, 587, and 588) to each OBD ECU."),
            @TestItem(verifies = "6.1.9.1.b", description = "Display each positive return in the log."),
            @TestItem(verifies = "6.1.9.4.a", description = "Global Component ID request (PGNPG 59904) for PGNPG 65259 (SPs 586, 587, and 588)."),
            @TestItem(verifies = "6.1.9.4.b", description = "Display each positive return in the log.") })
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

        OBDModuleInformation obdModule0 = new OBDModuleInformation(0, 0);
        dataRepository.putObdModule(obdModule0);
        dataRepository.putObdModule(new OBDModuleInformation(1));
        dataRepository.putObdModule(new OBDModuleInformation(2));
        dataRepository.putObdModule(new OBDModuleInformation(3));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any()))
                                                                                          .thenReturn(RequestResult.of(
                                                                                                              packet0x00,
                                                                                                              packet0x01,
                                                                                                              packet0x02,
                                                                                                              packet0x03));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0x00));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), eq(0x01), any(CommunicationsListener.class)))
                                                                                              .thenReturn(new BusResult<>(false,
                                                                                                                        packet0x01));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), eq(0x02), any(CommunicationsListener.class)))
                                                                                              .thenReturn(new BusResult<>(false,
                                                                                                                        packet0x02));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), eq(0x03), any(CommunicationsListener.class)))
                                                                                              .thenReturn(new BusResult<>(false,
                                                                                                                        packet0x03));

        runTest();
        assertEquals(packet0x00.getComponentIdentification(),
                     dataRepository.getObdModule(0)
                                   .get(ComponentIdentificationPacket.class, 1)
                                   .getComponentIdentification());
        assertEquals(packet0x01.getComponentIdentification(),
                     dataRepository.getObdModule(1)
                                   .get(ComponentIdentificationPacket.class, 1)
                                   .getComponentIdentification());
        assertEquals(packet0x02.getComponentIdentification(),
                     dataRepository.getObdModule(2)
                                   .get(ComponentIdentificationPacket.class, 1)
                                   .getComponentIdentification());
        assertEquals(packet0x03.getComponentIdentification(),
                     dataRepository.getObdModule(3)
                                   .get(ComponentIdentificationPacket.class, 1)
                                   .getComponentIdentification());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());

        assertEquals(List.of(), listener.getOutcomes());
        //
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(1),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(2),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(3),
                                             any(CommunicationsListener.class));

    }

    /**
     * Test module responds with correct ComponentIdentificationPacket to global
     * request: destination specific requests yields a good response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.2.c", description = "Destination Specific (DS) Component ID request (PGNPG 59904) for PGNPG 65259 (SPs 586, 587, and 588) to each OBD ECU."),
            @TestItem(verifies = "6.1.9.3.a", description = "Display each positive return in the log.") })
    public void testFailureWithNullSerialNumber() {
        var dataPacket = Packet.create(ComponentIdentificationPacket.PGN,
                                       0,
                                       0x44,
                                       0x54,
                                       0x44,
                                       0x53,
                                       0x43,
                                       0x2A,
                                       0x39,
                                       0x33,
                                       0x36,
                                       0x4E,
                                       0x31,
                                       0x36,
                                       0x2A,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x00,
                                       0x2A,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x20,
                                       0x2A);
        ComponentIdentificationPacket packet = new ComponentIdentificationPacket(dataPacket);

        dataRepository.putObdModule(new OBDModuleInformation(0, 0));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class))).thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet));

        runTest();

        assertEquals(packet.getComponentIdentification(),
                     dataRepository.getObdModule(0)
                                   .get(ComponentIdentificationPacket.class, 1)
                                   .getComponentIdentification());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.9.2.c - Serial number field (SP 588) from Engine #1 (0) does not end in five numeric characters");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        WARN,
                                        "6.1.9.3.a - Serial number field (SP 588) from Engine #1 (0) is less than eight characters long");
    }

    /**
     * Test module responds with ComponentIdentificationPacket with unprintable char in make to global
     * request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.2.d", description = "Fail if the make (SP 586), model (SP 587), or serial number (SP 588) from any OBD ECU contains any unprintable ASCII characters.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));
        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_MAKE);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket which has make field longer than five char to global
     * request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.3.b", description = "Warn if the make field (SP 586) is longer than five(5) ASCII characters.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_B);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket which has make field less than two char to global
     * request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.3.c", description = "Warn if the make field (SP 586) is less than two(2) ASCII characters.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_C);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket with model field which has an unprintable ASCII char
     * in it to global request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.2.d", description = "Fail if the make (SP 586), model (SP 587), or serial number (SP 588) from any OBD ECU contains any unprintable ASCII characters.") })
    public void testModelContainsNonPrintableAsciiCharacterFailure() {
        char unprintableAsciiCarriageReturn = 0xD;
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_MODEL);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket with model field with less than one char
     * in it to global request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.3.d", description = "Warn if the model field (SP 587) is less than one1 character long.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_WARN_MESSAGE_3_D);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with empty bus result to global request:
     * destination specific requests yields a good response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.5.a", description = "Fail if there is no positive response from function 0. (Global request not supported or timed out.)") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                .thenReturn(RequestResult.of());

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_5_A);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket with serial number field with that contains an
     * unprintable ASCII char to global request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.5.a", description = "Fail if there is no positive response from function 0. (Global request not supported or timed out.)") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet0x00,
                                                                                                                                          packet0x01,
                                                                                                                                          packet0x02,
                                                                                                                                          packet0x03));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0x00));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x01),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0x01));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x02),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0x02));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x03),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false,
                                                                                                       packet0x03));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x01),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x02),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x03),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_D_SN);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket with serial number field with less than 8 chars
     * in it to global request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.3.a", description = "Warn if the serial number field (SP 588) from any function 0 device is less than eight(8) characters long.") })
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x0),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, WARN, EXPECTED_INFO_MESSAGE_3_A);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }

    /**
     * Test module responds with ComponentIdentificationPacket with serial number field with non-numeric value in last 5
     * chars
     * in it to global request: destination specific requests yields a same response
     */
    @Test
    @TestDoc(value = {
            @TestItem(verifies = "6.1.9.2.c", description = "Fail if the serial number field (SP 588) from any function 0 device does not end in 5 numeric characters(ASCII 0 through ASCII 9).") })
    public void testSerialNumberEndsWithNonNumericCharacterInLastFiveCharactersFailure() {
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

        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN), any(CommunicationsListener.class)))
                                                                                                                      .thenReturn(RequestResult.of(packet));
        when(communicationsModule.request(eq(ComponentIdentificationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class)))
                                                                             .thenReturn(new BusResult<>(false, packet));

        runTest();

        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             anyInt(),
                                             any(CommunicationsListener.class));
        verify(communicationsModule).request(eq(ComponentIdentificationPacket.PGN),
                                             any(CommunicationsListener.class));

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, EXPECTED_FAIL_MESSAGE_2_C);

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("Function 0 ECU is Engine #1 (0)" + NL, listener.getResults());
    }
}
