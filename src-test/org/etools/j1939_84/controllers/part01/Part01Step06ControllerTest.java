/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Part01Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 6",
        description = "DM56: Model year and certification engine family"))
public class Part01Step06ControllerTest extends AbstractControllerTest {

    private static final String familyName = "YCALIF HD OBD*";

    /*
     * All values must be checked prior to mocking so that we are not creating
     * unnecessary mocks.
     */
    @SuppressWarnings("SameParameterValue")
    private static DM56EngineFamilyPacket createDM56(int sourceAddress,
                                                     Integer engineYear,
                                                     String modelYear,
                                                     Integer vehicleYear,
                                                     String familyName) {
        DM56EngineFamilyPacket packet = mock(DM56EngineFamilyPacket.class);
        when(packet.getSourceAddress()).thenReturn(sourceAddress);
        if (engineYear != null) {
            when(packet.getEngineModelYear()).thenReturn(engineYear);
        }
        if (modelYear != null) {
            when(packet.getModelYearField()).thenReturn(modelYear);
        }
        if (vehicleYear != null) {
            when(packet.getVehicleModelYear()).thenReturn(vehicleYear);
        }
        if (familyName != null) {
            when(packet.getFamilyName()).thenReturn(familyName);
        }
        return packet;
    }

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step06Controller instance;

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
    private DiagnosticMessageModule diagnosticMessageModule;

    @Before
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2006);
        dataRepository.setVehicleInformation(vehicleInformation);

        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        dataRepository.putObdModule(obdModuleInformation);

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step06Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dataRepository,
                DateTimeModule.getInstance(),
                diagnosticMessageModule);

        setup(instance,
              listener,
              j1939,
              executor,
              reportFileModule,
              engineSpeedModule,
              vehicleInformationModule,
              diagnosticMessageModule);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 diagnosticMessageModule);
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first asterisk character (ASCII 0x2A) when asterisk is
     * in a position less than twelve
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e",
            description = "Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"))
    public void testAsteriskPositionLessThanTwelve() {
        String famName = familyName.replace("A", "*");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0,
                                                                        2006,
                                                                        "2006E-MY",
                                                                        null,
                                                                        famName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(famName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"
                        + NL,
                listener.getResults());
    }

    /**
     * The asterisk termination at a char location of greater than 12
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e",
            description = "Engine family has > 12 characters before first asterisk character"))
    public void testAsteriskTerminationGreaterThanTwelve() {
        String famName = familyName.replace("*", "44*");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006E-MY", null, famName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(famName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"
                        + NL,
                listener.getResults());
    }

    /**
     * Test engineModelYearField not matching user input
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.a", description = "Engine model year does not match user input"))
    public void testEngineModelYearDoesNotMatch() {
        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2010, "2010E-MY", null, familyName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2010E-MY", obdModule.getModelYear());
        assertEquals(familyName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(diagnosticMessageModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.a - Engine model year does not match user input" + NL,
                listener.getResults());
    }

    /**
     * The asterisk termination at a char location of greater than 12
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e",
            description = "Engine family has > 12 characters before first asterisk character"))
    public void testFamilyNameLessThan13Characters() {
        String famName = familyName.replace(" OBD*", "");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006E-MY", null, famName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(famName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)" + NL,
                listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first 'null' character (ASCII 0x00) correct behavior
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e.",
            description = "Engine family has 12 characters before first 'null' character"))
    public void testFamilyNameWithNullTermination() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace('*', Character.MIN_VALUE);

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006E-MY", null, famName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(famName, obdModule.getEngineFamilyName());

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first 'null' character (ASCII 0x00) correct behavior
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e.",
            description = "Engine family has <> 12 characters before first 'null' character"))
    public void testFamilyNameWithNullTerminationGreaterThanTwelve() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace("*", "4");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006E-MY", null, famName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(famName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)" + NL,
                listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 6", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /*
     * Test modelYearField not matching user input and modelYearField with
     * invalid certification type
     */
    @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.6.2.b"),
            @TestItem(verifies = "6.1.6.2.c") },
            description = "Indicates 'V' instead of 'E' for cert type" + "<br/>" + "&nbsp"
                    + "Not formatted correctly")
    public void testModelYearField() {
        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006V-MY", null, familyName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006V-MY", obdModule.getModelYear());
        assertEquals(familyName, obdModule.getEngineFamilyName());

        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(diagnosticMessageModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.b - Indicates 'V' instead of 'E' for cert type" + NL
                        + "FAIL: 6.1.6.2.c - Not formatted correctly"
                        + NL,
                listener.getResults());
    }

    /*
     * Test the controller with an empty list of DM56EngineFamilyPackets
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6", description = "No packets are returned"))
    public void testPacketsEmpty() {
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(List.of());

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("DM56 is not supported" + NL, listener.getResults());

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("DM56 is not supported" + NL, listener.getResults());
    }

    /**
     * The happy/no error path of the class
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6", description = "Happy Path with no errors and one packet"))
    public void testRunHappyPath() {
        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(0, 2006, "2006E-MY", null, familyName));
        when(diagnosticMessageModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);

        var obdModule = dataRepository.getObdModule(0);
        assertEquals("2006E-MY", obdModule.getModelYear());
        assertEquals(familyName, obdModule.getEngineFamilyName());

        verify(diagnosticMessageModule).requestDM56(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }
}
