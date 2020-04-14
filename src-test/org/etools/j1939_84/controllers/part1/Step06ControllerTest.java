/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM56EngineFamilyPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.VehicleInformation;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step06ControllerTest extends AbstractControllerTest {

    private static final String familyName = "YCALIF HD OBD*";

    /*
     * All values must be checked prior to mocking so that we are not
     * creating unnecessary mocks.
     */
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

    private Step06Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {

        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step06Controller(
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
                vehicleInformationModule,
                partResultFactory,
                dataRepository,
                mockListener,
                reportFileModule);
    }

    /**
     * The asterisk temination at a char location of greater than 12
     */
    @Test
    public void testAsteriskTerminationGreaterThanTwelve() {
        String famName = familyName.replace("*", "44*");

        List<DM56EngineFamilyPacket> parsedPackets = listOf(createDM56(null, 2006, "2006E-MY", null, famName));
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

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                6,
                Outcome.FAIL,
                "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");
        verify(reportFileModule).onProgress(0, 1, "");
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
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)\n",
                listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12 characters
     * before first asterisk character (ASCII 0x2A) when asterisk is in a position
     * less than twelve
     */
    @Test
    public void testAstriskPositionLessThanTwelve() {
        String famName = familyName.replace("A", "*");

        List<DM56EngineFamilyPacket> parsedPackets = listOf(
                createDM56(null,
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

        verify(reportFileModule).onProgress(0, 1, "");
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
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)\n",
                listener.getResults());
    }

    /*
     * Test engineModelYearField not matching user input
     */
    @Test
    public void testEngineModelYearDoesntMatch() {
        List<DM56EngineFamilyPacket> parsedPackets = listOf(createDM56(null, 2006, "2006E-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2010);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onResult("FAIL: 6.1.6.2.a - Engine model year does not match user input");
        verify(reportFileModule)
                .addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(vehicleInformationModule).reportEngineFamily(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.a - Engine model year does not match user input\n",
                listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12 characters
     * before first “null” character (ASCII 0x00) correct behavior
     */
    @Test
    public void testFamilyNameWithNullTermination() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace('*', Character.MIN_VALUE);

        List<DM56EngineFamilyPacket> parsedPackets = listOf(
                createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(reportFileModule).onProgress(0, 1, "");

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12 characters
     * before first “null” character (ASCII 0x00) correct behavior
     */
    @Test
    public void testFamilyNameWithNullTerminationGreaterThanTwelve() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace("*", "4");

        List<DM56EngineFamilyPacket> parsedPackets = listOf(
                createDM56(null, 2006, "2006E-MY", null, famName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1,
                6,
                Outcome.FAIL,
                "6.1.6.2.e. - Engine family has <> 12 characters before first “null” character (ASCII 0x00)");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                6,
                Outcome.FAIL,
                "6.1.6.2.e. - Engine family has <> 12 characters before first “null” character (ASCII 0x00)");
        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                6,
                Outcome.FAIL,
                "6.1.6.2.e. - Engine family has <> 12 characters before first “null” character (ASCII 0x00)");
        verify(reportFileModule).onResult(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first “null” character (ASCII 0x00)");

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.e. - Engine family has <> 12 characters before first “null” character (ASCII 0x00)\n",
                listener.getResults());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 6", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    /*
     * Test modelYearField not matching user input and modelYearField with invalid
     * certification type
     */
    @Test
    public void testModelYearField() {
        List<DM56EngineFamilyPacket> parsedPackets = listOf(createDM56(null, 2006, "2006V-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(mockListener).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.b - Indicates “V” instead of “E” for cert type");
        verify(mockListener).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onResult("FAIL: 6.1.6.2.b - Indicates “V” instead of “E” for cert type");
        verify(reportFileModule)
                .addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.b - Indicates “V” instead of “E” for cert type");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onResult("FAIL: 6.1.6.2.c - Not formatted correctly");
        verify(reportFileModule).addOutcome(1, 6, Outcome.FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(vehicleInformationModule).reportEngineFamily(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals(
                "FAIL: 6.1.6.2.b - Indicates “V” instead of “E” for cert type\nFAIL: 6.1.6.2.c - Not formatted correctly\n",
                listener.getResults());
    }

    /*
     * Test the controller with an empty list of DM56EngineFamilyPackets
     */
    @Test
    public void testPacketsEmpty() {
        List<DM56EngineFamilyPacket> packets = new ArrayList<>();
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(packets);

        runTest();

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("DM56 is not supported\n", listener.getResults());

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).onResult("DM56 is not supported");

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("DM56 is not supported\n", listener.getResults());
    }

    /**
     * The happy/no error path of the class
     */
    @Test
    public void testRunHappyPath() {
        List<DM56EngineFamilyPacket> parsedPackets = listOf(createDM56(null, 2006, "2006E-MY", null, familyName));
        when(vehicleInformationModule.reportEngineFamily(any())).thenReturn(parsedPackets);

        VehicleInformation vehicleInformation = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);
        when(dataRepository.getVehicleInformation().getEngineModelYear()).thenReturn(2006);

        runTest();

        verify(dataRepository, times(2)).getVehicleInformation();

        verify(reportFileModule).onProgress(0, 1, "");

        verify(vehicleInformationModule).reportEngineFamily(any());

        // Verify the documentation was recorded correctly
        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }
}
