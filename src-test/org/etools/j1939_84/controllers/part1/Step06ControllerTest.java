/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
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
 * The unit test for {@link Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 6",
        description = "DM56: Model year and certification engine family"))
public class Step06ControllerTest extends AbstractControllerTest {

    private static final String familyName = "YCALIF HD OBD*";

    /*
     * All values must be checked prior to mocking so that we are not creating
     * unnecessary mocks.
     */
    @SuppressWarnings("SameParameterValue")
    private static DM56EngineFamilyPacket createDM56(Integer sourceAddress,
                                                     Integer engineYear,
                                                     String modelYear,
                                                     Integer vehicleYear,
                                                     String familyName) {
        DM56EngineFamilyPacket packet = mock(DM56EngineFamilyPacket.class);
        if (sourceAddress != null) {
            when(packet.getSourceAddress()).thenReturn(sourceAddress);
        }
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

    @Mock
    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step06Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private DateTimeModule dateTimeModule;

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Step06Controller(
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

    /**
     * The asterisk termination at a char location of greater than 12
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e",
            description = "Engine family has > 12 characters before first asterisk character"))
    public void testAsteriskTerminationGreaterThanTwelve() {
        String famName = familyName.replace("*", "44*");

        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                                        6,
                                        Outcome.FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"
                        + NL,
                listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first asterisk character (ASCII 0x2A) when asterisk is
     * in a position less than twelve
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e",
            description = "Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"))
    public void testAstriskPositionLessThanTwelve() {
        String famName = familyName.replace("A", "*");

        List<DM56EngineFamilyPacket> parsedPackets = Collections.singletonList(createDM56(null,
                                                                                          2006,
                                                                                          "2006E-MY",
                                                                                          null,
                                                                                          famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                                        6,
                                        Outcome.FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(reportFileModule).onResult(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(vehicleInformationModule).reportEngineFamily(any());

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
    public void testEngineModelYearDoesntMatch() {
        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2010);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(reportFileModule).onResult("FAIL: 6.1.6.2.a - Engine model year does not match user input");
        verify(reportFileModule)
                .addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(vehicleInformationModule).reportEngineFamily(any());

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

        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                                        6,
                                        Outcome.FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(vehicleInformationModule).reportEngineFamily(any());

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

        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(vehicleInformationModule).reportEngineFamily(any());

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

        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                                        6,
                                        Outcome.FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
        verify(reportFileModule).addOutcome(1,
                                            6,
                                            Outcome.FAIL,
                                            "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(vehicleInformationModule).reportEngineFamily(any());

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
        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006V-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener)
                .addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
        verify(mockListener).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(reportFileModule).onResult("FAIL: 6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
        verify(reportFileModule)
                .addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");

        verify(reportFileModule).onResult("FAIL: 6.1.6.2.c - Not formatted correctly");
        verify(reportFileModule).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(vehicleInformationModule).reportEngineFamily(any());

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
        List<DM56EngineFamilyPacket> packets = new ArrayList<>();
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(packets);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("DM56 is not supported" + NL, listener.getResults());

        verify(reportFileModule).onResult("DM56 is not supported");

        verify(vehicleInformationModule).reportEngineFamily(any());

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
        List<DM56EngineFamilyPacket> parsedPackets = Collections
                .singletonList(createDM56(null, 2006, "2006E-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }
}
