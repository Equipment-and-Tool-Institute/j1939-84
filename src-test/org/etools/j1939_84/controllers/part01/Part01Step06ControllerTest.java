/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.DM56EngineFamilyPacket;
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
 * The unit test for {@link Part01Step06Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 6", description = "DM56: Model year and certification engine family"))
public class Part01Step06ControllerTest extends AbstractControllerTest {

    private static final String familyName = "YCALIF HD OBD*";
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
    private CommunicationsModule communicationsModule;

    /*
     * All values must be checked prior to mocking so that we are not creating
     * unnecessary mocks.
     */
    private static DM56EngineFamilyPacket createDM56(Integer engineYear,
                                                     String familyName) {

        return DM56EngineFamilyPacket.create(0x00, engineYear, true, familyName);
    }

    @Before
    public void setUp() throws Exception {

        dataRepository = DataRepository.newInstance();

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2006);

        dataRepository.setVehicleInformation(vehicleInformation);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step06Controller(
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
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 mockListener,
                                 communicationsModule);
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first asterisk character (ASCII 0x2A) when asterisk is
     * in a position less than twelve
     */
    // @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e", description = "Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)"))
    public void testAsteriskPositionLessThanTwelve() {
        String famName = familyName.replace("A", "*");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2006, famName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.f. - Fail if MY2024+ Engine does not support DM56
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.f", description = "Fail if MY2024+ Engine does not support DM56)"))
    public void testDm56NotSupportedFailure() {

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setEngineModelYear(2026);
        dataRepository.setVehicleInformation(vehicleInformation);
        when(communicationsModule.requestDM56(any())).thenReturn(List.of());

        runTest();

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.f - MY2024+ Engine does not support DM56");

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * The asterisk termination at a char location of greater than 12
     */
    // @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e", description = "Engine family has > 12 characters before first asterisk character"))
    public void testAsteriskTerminationGreaterThanTwelve() {
        String famName = familyName.replace("*", "44*");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2006, famName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first asterisk character (ASCII 0x2A)");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Test engineModelYearField not matching user input
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.a", description = "Engine model year does not match user input"))
    public void testEngineModelYearDoesNotMatch() {
        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2010, familyName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.a - Engine model year does not match user input");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * The asterisk termination at a char location of greater than 12
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e", description = "Engine family has > 12 characters before first asterisk character"))
    public void testFamilyNameLessThan13Characters() {
        String famName = familyName.replace(" OBD*", "");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2006, famName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first 'null' character (ASCII 0x00) correct behavior
     */
    // @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e.", description = "Engine family has 12 characters before first 'null' character"))
    public void testFamilyNameWithNullTermination() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace('*', Character.MIN_VALUE);

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2006, famName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /**
     * Verify the error handling for 6.1.6.2.e. - Engine family has <> 12
     * characters before first 'null' character (ASCII 0x00) correct behavior
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6.2.e.", description = "Engine family has <> 12 characters before first 'null' character"))
    public void testFamilyNameWithNullTerminationGreaterThanTwelve() {
        // Remove asterisk from name to test valid null termination
        String famName = familyName.replace("*", "4");

        List<DM56EngineFamilyPacket> parsedPackets = List.of(createDM56(2006, famName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1,
                                        6,
                                        FAIL,
                                        "6.1.6.2.e. - Engine family has <> 12 characters before first 'null' character (ASCII 0x00)");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
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
    // @Test
    @TestDoc(value = { @TestItem(verifies = "6.1.6.2.b"),
            @TestItem(verifies = "6.1.6.2.c") }, description = "Indicates 'V' instead of 'E' for cert type" + "<br/>"
                    + "&nbsp"
                    + "Not formatted correctly")
    public void testModelYearField() {
        List<DM56EngineFamilyPacket> parsedPackets = List.of(DM56EngineFamilyPacket.create(0x00,
                                                                                           2006,
                                                                                           false,
                                                                                           familyName));
        when(communicationsModule.requestDM56(any())).thenReturn(parsedPackets);

        runTest();

        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.b - Indicates 'V' instead of 'E' for cert type");
        verify(mockListener).addOutcome(1, 6, FAIL, "6.1.6.2.c - Not formatted correctly");

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }

    /*
     * Test the controller with an empty list of DM56EngineFamilyPackets
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6", description = "No packets are returned"))
    public void testPacketsEmpty() {
        when(communicationsModule.requestDM56(any())).thenReturn(List.of());

        runTest();

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("6.1.6.1.a - DM56 is not supported" + NL, listener.getResults());
    }

    /**
     * The happy/no error path of the class
     */
    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.6", description = "Happy Path with no errors and one packet"))
    public void testRunHappyPath() {
        DM56EngineFamilyPacket dm56 = DM56EngineFamilyPacket.create(0, 2006, true, familyName);
        when(communicationsModule.requestDM56(any())).thenReturn(List.of(dm56));

        runTest();

        assertSame(dm56, dataRepository.getObdModule(0).getLatest(DM56EngineFamilyPacket.class));

        verify(communicationsModule).requestDM56(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getResults());
    }
}
