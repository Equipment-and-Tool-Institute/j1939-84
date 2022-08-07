/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.etools.j1939tools.j1939.model.FuelType.BATT_ELEC;
import static org.etools.j1939tools.j1939.model.FuelType.BI_DSL;
import static org.etools.j1939tools.j1939.model.FuelType.BI_GAS;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_ETH;
import static org.etools.j1939tools.j1939.model.FuelType.HYB_GAS;
import static org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response.NACK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
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
 * The unit test for {@link Part01Step04Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 4", description = "DM24: SPN support"))
public class Part01Step04ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 4;

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step04Controller instance;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SupportedSpnModule supportedSpnModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;
    private TestResultsListener listener;

    private static int[] convertToIntArray(byte[] input) {
        int[] intArray = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            // Range 0 to 255, not -128 to 127
            intArray[i] = Byte.toUnsignedInt(input[i]);
        }
        return intArray;
    }

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step04Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              supportedSpnModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());

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
                                 communicationsModule,
                                 mockListener,
                                 supportedSpnModule);
    }

    // Test handling of no response from the modules
    @Test
    public void testEmptyObdModules() {
        DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
        PgnDefinition pgnDefinition = mock(PgnDefinition.class);
        when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet1.getPgnDefinition().getId()).thenReturn(DM24SPNSupportPacket.PGN);
        when(packet1.getSourceAddress()).thenReturn(0);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        when(communicationsModule.requestDM24(any(), eq(0)))
                                                            .thenReturn(BusResult.of(packet1));

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2013);
        vehicleInfo.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInfo);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM24(any(), eq(0));

        verify(engineSpeedModule).setJ1939(j1939);

        assertEquals("", listener.getResults());

        verify(mockListener)
                            .addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        verify(mockListener)
                            .addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(reportFileModule)
                                .addOutcome(PART_NUMBER,
                                            STEP_NUMBER,
                                            FAIL,
                                            "6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        verify(reportFileModule)
                                .addOutcome(PART_NUMBER,
                                            STEP_NUMBER,
                                            FAIL,
                                            "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any(), anyInt());
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), any());

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    // Testing the object with all possible errors
    @TestDoc(value = {
            @TestItem(verifies = "6.1.4", dependsOn = "DM24SPNSupportPacketTest"),
            @TestItem(verifies = "6.1.4.1.b", dependsOn = "J1939TPTest.testRequestTimeout"),
            @TestItem(verifies = "6.1.4.2.a,b,c")
    }, description = "Using a response that indicates that 6.1.4.2.a, 6.1.4.2.b, 6.1.4.2.c all failed, verify that the failures are in the report.")
    public void testErroredObject() {
        DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
        PgnDefinition pgnDefinition = mock(PgnDefinition.class);
        when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet1.getPgnDefinition().getId()).thenReturn(DM24SPNSupportPacket.PGN);
        when(packet1.getSourceAddress()).thenReturn(0);

        dataRepository.putObdModule(new OBDModuleInformation(0));
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        dataRepository.putObdModule(new OBDModuleInformation(1));
        AcknowledgmentPacket packet4 = AcknowledgmentPacket.create(1, NACK);
        when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet1.getPgnDefinition().getId()).thenReturn(DM24SPNSupportPacket.PGN);
        when(communicationsModule.requestDM24(any(), eq(1)))
                                                            .thenReturn(BusResult.empty())
                                                            .thenReturn(BusResult.of(packet4));

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2013);
        vehicleInfo.setFuelType(BI_GAS);
        dataRepository.setVehicleInformation(vehicleInfo);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM24(any(), eq(0));
        verify(communicationsModule, times(2)).requestDM24(any(), eq(1));

        verify(engineSpeedModule).setJ1939(j1939);

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.4.2.a - Retry was required to obtain DM24 response from Engine #2 (1)");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any(), anyInt());
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), any());

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    @TestDoc(description = "Verify step name is correct.")
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 4", instance.getDisplayName());
    }

    @Test
    @TestDoc(description = "Verify that there is only one step in 6.1.4.")
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    /**
     * Test method for
     * {@link StepController#getStepNumber()}.
     */
    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.a,b,c"), description = "Verify that step completes without errors when none of the fail criteria are met.")
    public void testGoodObjects() {

        dataRepository.putObdModule(new OBDModuleInformation(0));
        dataRepository.putObdModule(new OBDModuleInformation(1));

        DM24SPNSupportPacket packet1 = new DM24SPNSupportPacket(Packet.create(DM24SPNSupportPacket.PGN,
                                                                              0x00,
                                                                              0x5C,
                                                                              0x00,
                                                                              0x1B,
                                                                              0x01,
                                                                              0x00,
                                                                              0x02,
                                                                              0x1B,
                                                                              0x01,
                                                                              0x01,
                                                                              0x02,
                                                                              0x1B,
                                                                              0x01));

        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        //@formatter:off
        DM24SPNSupportPacket packet4 = new DM24SPNSupportPacket(Packet.create(DM24SPNSupportPacket.PGN,
                0x01,
                0x5C, 0x00, 0x1C, 0x01, 0x00, 0x02, 0x1C, 0x01, 0x01, 0x02, 0x1C, 0x01, 0x20, 0x02, 0x1C,
                0x02, 0x1B, 0x02, 0x1C, 0x01, 0x1C, 0x02, 0x1C, 0x01, 0x1D, 0x02, 0x1C, 0x01, 0x1E, 0x02,
                0x1C, 0x01, 0x1F, 0x02, 0x1C, 0x01, 0x6E, 0x00, 0x1C, 0x01, 0xAF, 0x00, 0x1C, 0x02, 0xBE,
                0x00, 0x1C, 0x02, 0x54, 0x00, 0x1C, 0x02, 0x6C, 0x00, 0x1C, 0x01, 0x9E, 0x00, 0x1C, 0x02,
                0x33, 0x00, 0x1C, 0x01, 0x5E, 0x00, 0x1C, 0x01, 0xAC, 0x00, 0x1C, 0x01, 0x69, 0x00, 0x1C,
                0x01, 0x84, 0x00, 0x1C, 0x02, 0xD0, 0x03, 0x1C, 0x01, 0x5B, 0x00, 0x1C, 0x01, 0xB7, 0x00,
                0x1C, 0x02, 0x66, 0x00, 0x18, 0x01, 0xAD, 0x00, 0x1C, 0x02, 0xB3, 0x0C, 0x1C, 0x02, 0x9B,
                0x0D, 0x1C, 0x01, 0xCD, 0x16, 0x1C, 0x01, 0xE5, 0x0C, 0x1C, 0x02, 0x5A, 0x15, 0x1C, 0x02,
                0xCB, 0x14, 0x1C, 0x01, 0x88, 0x0D, 0x1C, 0x02, 0xB9, 0x04, 0x1C, 0x02, 0xA5, 0x15, 0x1C,
                0x01, 0xA4, 0x00, 0x18, 0x02, 0xE7, 0x0A, 0x1C, 0x02, 0x85, 0x05, 0x1C, 0x02, 0x86, 0x05,
                0x1C, 0x02, 0x87, 0x05, 0x1C, 0x02, 0x88, 0x05, 0x1C, 0x02, 0x89, 0x05, 0x1C, 0x02, 0x8A,
                0x05, 0x1C, 0x02, 0xEB, 0x0D, 0x1C, 0x01, 0x1B, 0x00, 0x1C, 0x02, 0xAA, 0x0C, 0x1C, 0x02,
                0xAE, 0x0C, 0x1C, 0x02, 0x90, 0x0C, 0x18, 0x02, 0x39, 0x04, 0x1C, 0x01, 0xA5, 0x04, 0x1C,
                0x01, 0xC2, 0x14, 0x1C, 0x02, 0x19, 0x0E, 0x1E, 0x02, 0x98, 0x0D, 0x1E, 0x02, 0x9A, 0x0C,
                0x1C, 0x02, 0xE1, 0x06, 0x1C, 0x01, 0x9A, 0x0D, 0x1E, 0x01, 0xA2, 0x0D, 0x1E, 0x01, 0x08,
                0x11, 0x1E, 0x02, 0x0B, 0x11, 0x1E, 0x02, 0xD7, 0x0B, 0x1E, 0x01, 0x45, 0x11, 0x1F, 0x01,
                0x95, 0x04, 0x18, 0x02, 0xED, 0x00, 0x1D, 0x11, 0xA1, 0x10, 0x1B, 0x01, 0x00, 0x09, 0x1F,
                0x01, 0x83, 0x03, 0x1D, 0x01, 0x01, 0x09, 0x1F, 0x01, 0xFD, 0x0B, 0x1D, 0x01, 0xDE, 0x0C,
                0x1D, 0x01, 0xDF, 0x0C, 0x1D, 0x01, 0xE0, 0x0C, 0x1D, 0x01, 0xE6, 0x0C, 0x1D, 0x01, 0x87,
                0x0E, 0x1D, 0x01, 0x2B, 0x05, 0x1B, 0x01, 0x2C, 0x05, 0x1B, 0x01, 0x2D, 0x05, 0x1B, 0x01,
                0x2E, 0x05, 0x1B, 0x01, 0x2F, 0x05, 0x1B, 0x01, 0x30, 0x05, 0x1B, 0x01, 0x46, 0x0A, 0x1B,
                0x02, 0x63, 0x0A, 0x1B, 0x02, 0x2A, 0x05, 0x1B, 0x01, 0x90, 0x12, 0x1B, 0x01, 0x9E, 0x12,
                0x1F, 0x02, 0xC7, 0x14, 0x1F, 0x01, 0x8B, 0x02, 0x1B, 0x01, 0x8C, 0x02, 0x1B, 0x01, 0x8D,
                0x02, 0x1B, 0x01, 0x8E, 0x02, 0x1B, 0x01, 0x8F, 0x02, 0x1B, 0x01, 0x90, 0x02, 0x1B, 0x01,
                0x8F, 0x0D, 0x1B, 0x01, 0xE4, 0x0D, 0x1B, 0x01, 0x03, 0x09, 0x1F, 0x01, 0x0A, 0x10, 0x1D,
                0x01, 0xC0, 0x04, 0x1D, 0x01, 0xC4, 0x04, 0x1D, 0x01, 0x1F, 0x10, 0x1D, 0x01, 0x22, 0x10,
                0x1D, 0x01, 0xAB, 0x00, 0x1D, 0x02, 0x9D, 0x00, 0x1F, 0x02, 0x3F, 0x0A, 0x1D, 0x01, 0xF7,
                0x00, 0x1D, 0x04, 0xEB, 0x00, 0x1D, 0x04, 0xBD, 0x04, 0x1D, 0x01, 0xE7, 0x0C, 0x1D, 0x01,
                0xE8, 0x0C, 0x1D, 0x01, 0x4E, 0x15, 0x1D, 0x04, 0x4C, 0x02, 0x1D, 0x11, 0xEF, 0x0B, 0x1F,
                0x01, 0x57, 0x15, 0x1D, 0x01, 0xF8, 0x00, 0x1D, 0x04, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00,
                0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00,
                0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00, 0x00, 0x00, 0x1F, 0x00));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setFuelType(BI_GAS);
        vehicleInfo.setEngineModelYear(2013);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM24(any(), eq(0));
        verify(communicationsModule).requestDM24(any(), eq(1));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(512, 513, 899, 132, 1413, 1414, 1415,
                3719, 3464,
                1416, 1417, 1418, 4106, 3216, 1173, 3226, 539, 3483, 27, 540, 541, 542, 158, 543, 4127, 544, 4130, 164,
                5541, 1189, 3242, 171, 172, 173, 3246, 175, 51, 3251, 183, 1209, 1081, 1213, 190, 2623, 1216, 5314,
                1220, 5323, 588, 5837, 5454, 976, 84, 5463, 5466, 91, 92, 94, 3294, 3295, 3296, 1761, 3301, 102, 3302,
                2791, 3303, 3304, 105, 3563, 235, 108, 237, 110, 247, 248, 3069);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(BI_GAS), anyInt());

        List<Integer> expectedFreezeFrames = Arrays.asList(512,
                                                           513,
                                                           132,
                                                           1413,
                                                           1414,
                                                           1415,
                                                           3464,
                                                           1416,
                                                           4360,
                                                           1417,
                                                           1418,
                                                           4363,
                                                           3216,
                                                           1173,
                                                           3480,
                                                           3609,
                                                           3226,
                                                           3482,
                                                           539,
                                                           3483,
                                                           27,
                                                           540,
                                                           541,
                                                           542,
                                                           158,
                                                           543,
                                                           544,
                                                           3490,
                                                           164,
                                                           5541,
                                                           1189,
                                                           3242,
                                                           172,
                                                           173,
                                                           3246,
                                                           175,
                                                           51,
                                                           3251,
                                                           183,
                                                           1209,
                                                           1081,
                                                           190,
                                                           5314,
                                                           5323,
                                                           5837,
                                                           976,
                                                           84,
                                                           3031,
                                                           5466,
                                                           91,
                                                           92,
                                                           94,
                                                           1761,
                                                           3301,
                                                           102,
                                                           2791,
                                                           105,
                                                           3563,
                                                           108,
                                                           110);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        byte[] spn92Packet1 = { 0x5C, 0x00, 0x1B, 0x01 };
        byte[] spn512Packet1 = { 0x00, 0x02, 0x1B, 0x01 };
        byte[] spn513Packet1 = { 0x01, 0x02, 0x1B, 0x01 };
        List<SupportedSPN> expectedPacket1Spns = List.of(
                                                         new SupportedSPN(convertToIntArray(spn92Packet1)),
                                                         new SupportedSPN(convertToIntArray(spn512Packet1)),
                                                         new SupportedSPN(convertToIntArray(spn513Packet1)));
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        byte[] spn92 = { 0x5C, 0x00, 0x1C, 0x01 };
        byte[] spn512 = { 0x00, 0x02, 0x1C, 0x01 };
        byte[] spn513 = { 0x01, 0x02, 0x1C, 0x01 };
        byte[] spn544 = { 0x20, 0x02, 0x1C, 0x02 };
        byte[] spn539 = { 0x1B, 0x02, 0x1C, 0x01 };
        byte[] spn540 = { 0x1C, 0x02, 0x1C, 0x01 };
        byte[] spn541 = { 0x1D, 0x02, 0x1C, 0x01 };
        byte[] spn542 = { 0x1E, 0x02, 0x1C, 0x01 };
        byte[] spn543 = { 0x1F, 0x02, 0x1C, 0x01 };

        byte[] spn110 = { 0x6E, 0x00, 0x1C, 0x01 };
        byte[] spn175 = { (byte) 0xAF, 0x00, 0x1C, 0x02 };
        byte[] spn190 = { (byte) 0xBE, 0x00, 0x1C, 0x02 };
        byte[] spn84 = { 0x54, 0x00, 0x1C, 0x02 };
        byte[] spn108 = { 0x6C, 0x00, 0x1C, 0x01 };
        byte[] spn158 = { (byte) 0x9E, 0x00, 0x1C, 0x02 };
        byte[] spn51 = { 0x33, 0x00, 0x1C, 0x01 };
        byte[] spn94 = { 0x5E, 0x00, 0x1C, 0x01 };
        byte[] spn172 = { (byte) 0xAC, 0x00, 0x1C, 0x01 };
        byte[] spn105 = { 0x69, 0x00, 0x1C, 0x01 };

        byte[] spn132 = { (byte) 0x84, 0x00, 0x1C, 0x02 };
        byte[] spn976 = { (byte) 0xD0, 0x03, 0x1C, 0x01 };
        byte[] spn91 = { 0x5B, 0x00, 0x1C, 0x01, };
        byte[] spn183 = { (byte) 0xB7, 0x00, 0x1C, 0x02 };
        byte[] spn102 = { 0x66, 0x00, 0x18, 0x01 };
        byte[] spn173 = { (byte) 0xAD, 0x00, 0x1C, 0x02 };
        byte[] spn3251 = { (byte) 0xB3, 0x0C, 0x1C, 0x02 };
        byte[] spn3483 = { (byte) 0x9B, 0x0D, 0x1C, 0x01 };
        byte[] spn5837 = { (byte) 0xCD, 0x16, 0x1C, 0x01 };
        byte[] spn3301 = { (byte) 0xE5, 0x0C, 0x1C, 0x02 };
        byte[] spn5466 = { 0x5A, 0x15, 0x1C, 0x02 };
        byte[] spn5323 = { (byte) 0xCB, 0x14, 0x1C, 0x01 };
        byte[] spn3464 = { (byte) 0x88, 0x0D, 0x1C, 0x02 };
        byte[] spn1209 = { (byte) 0xB9, 0x04, 0x1C, 0x02 };
        byte[] spn5541 = { (byte) 0xA5, 0x15, 0x1C, 0x01 };
        byte[] spn164 = { (byte) 0xA4, 0x00, 0x18, 0x02 };
        byte[] spn2791 = { (byte) 0xE7, 0x0A, 0x1C, 0x02 };
        byte[] spn1413 = { (byte) 0x85, 0x05, 0x1C, 0x02 };
        byte[] spn1414 = { (byte) 0x86, 0x05, 0x1C, 0x02 };
        byte[] spn1415 = { (byte) 0x87, 0x05, 0x1C, 0x02 };
        byte[] spn1416 = { (byte) 0x88, 0x05, 0x1C, 0x02 };
        byte[] spn1417 = { (byte) 0x89, 0x05, 0x1C, 0x02 };
        byte[] spn1418 = { (byte) 0x8A, 0x05, 0x1C, 0x02 };
        byte[] spn3563 = { (byte) 0xEB, 0x0D, 0x1C, 0x01 };
        byte[] spn27 = { 0x1B, 0x00, 0x1C, 0x02 };
        byte[] spn3242 = { (byte) 0xAA, 0x0C, 0x1C, 0x02 };
        byte[] spn3246 = { (byte) 0xAE, 0x0C, 0x1C, 0x02 };
        byte[] spn3216 = { (byte) 0x90, 0x0C, 0x18, 0x02 };
        byte[] spn1081 = { 0x39, 0x04, 0x1C, 0x01 };
        byte[] spn1189 = { (byte) 0xA5, 0x04, 0x1C, 0x01 };
        byte[] spn5314 = { (byte) 0xC2, 0x14, 0x1C, 0x02 };
        byte[] spn3609 = { 0x19, 0x0E, 0x1E, 0x02 };
        byte[] spn3480 = { (byte) 0x98, 0x0D, 0x1E, 0x02 };
        byte[] spn3226 = { (byte) 0x9A, 0x0C, 0x1C, 0x02 };
        byte[] spn1761 = { (byte) 0xE1, 0x06, 0x1C, 0x01 };
        byte[] spn3482 = { (byte) 0x9A, 0x0D, 0x1E, 0x01 };
        byte[] spn3490 = { (byte) 0xA2, 0x0D, 0x1E, 0x01 };
        byte[] spn4360 = { 0x08, 0x11, 0x1E, 0x02 };
        byte[] spn4363 = { 0x0B, 0x11, 0x1E, 0x02 };
        byte[] spn3031 = { (byte) 0xD7, 0x0B, 0x1E, 0x01 };
        byte[] spn4421 = { 0x45, 0x11, 0x1F, 0x01 };
        byte[] spn1173 = { (byte) 0x95, 0x04, 0x18, 0x02 };
        byte[] spn237 = { (byte) 0xED, 0x00, 0x1D, 0x11 };
        byte[] spn4257 = { (byte) 0xA1, 0x10, 0x1B, 0x01 };
        byte[] spn2304 = { 0x00, 0x09, 0x1F, 0x01 };
        byte[] spn899 = { (byte) 0x83, 0x03, 0x1D, 0x01 };
        byte[] spn2305 = { 0x01, 0x09, 0x1F, 0x01 };
        byte[] spn3069 = { (byte) 0xFD, 0x0B, 0x1D, 0x01 };
        byte[] spn3294 = { (byte) 0xDE, 0x0C, 0x1D, 0x01 };
        byte[] spn3295 = { (byte) 0xDF, 0x0C, 0x1D, 0x01 };
        byte[] spn3296 = { (byte) 0xE0, 0x0C, 0x1D, 0x01 };
        byte[] spn3302 = { (byte) 0xE6, 0x0C, 0x1D, 0x01 };
        byte[] spn3719 = { (byte) 0x87, 0x0E, 0x1D, 0x01 };
        byte[] spn1323 = { 0x2B, 0x05, 0x1B, 0x01 };
        byte[] spn1324 = { 0x2C, 0x05, 0x1B, 0x01 };
        byte[] spn1325 = { 0x2D, 0x05, 0x1B, 0x01 };
        byte[] spn1326 = { 0x2E, 0x05, 0x1B, 0x01 };
        byte[] spn1327 = { 0x2F, 0x05, 0x1B, 0x01 };
        byte[] spn1328 = { 0x30, 0x05, 0x1B, 0x01 };
        byte[] spn2630 = { 0x46, 0x0A, 0x1B, 0x02 };
        byte[] spn2659 = { 0x63, 0x0A, 0x1B, 0x02 };
        byte[] spn1322 = { 0x2A, 0x05, 0x1B, 0x01 };
        byte[] spn4752 = { (byte) 0x90, 0x12, 0x1B, 0x01 };
        byte[] spn4766 = { (byte) 0x9E, 0x12, 0x1F, 0x02 };
        byte[] spn5319 = { (byte) 0xC7, 0x14, 0x1F, 0x01 };
        byte[] spn651 = { (byte) 0x8B, 0x02, 0x1B, 0x01 };
        byte[] spn652 = { (byte) 0x8C, 0x02, 0x1B, 0x01 };
        byte[] spn653 = { (byte) 0x8D, 0x02, 0x1B, 0x01 };
        byte[] spn654 = { (byte) 0x8E, 0x02, 0x1B, 0x01 };
        byte[] spn655 = { (byte) 0x8F, 0x02, 0x1B, 0x01 };
        byte[] spn656 = { (byte) 0x90, 0x02, 0x1B, 0x01 };
        byte[] spn3471 = { (byte) 0x8F, 0x0D, 0x1B, 0x01 };
        byte[] spn3556 = { (byte) 0xE4, 0x0D, 0x1B, 0x01 };
        byte[] spn2307 = { 0x03, 0x09, 0x1F, 0x01 };
        byte[] spn4106 = { 0x0A, 0x10, 0x1D, 0x01 };
        byte[] spn1216 = { (byte) 0xC0, 0x04, 0x1D, 0x01 };
        byte[] spn1220 = { (byte) 0xC4, 0x04, 0x1D, 0x01 };
        byte[] spn4127 = { 0x1F, 0x10, 0x1D, 0x01 };
        byte[] spn4130 = { 0x22, 0x10, 0x1D, 0x01 };
        byte[] spn171 = { (byte) 0xAB, 0x00, 0x1D, 0x02 };
        byte[] spn157 = { (byte) 0x9D, 0x00, 0x1F, 0x02 };
        byte[] spn2623 = { 0x3F, 0x0A, 0x1D, 0x01 };
        byte[] spn247 = { (byte) 0xF7, 0x00, 0x1D, 0x04 };
        byte[] spn235 = { (byte) 0xEB, 0x00, 0x1D, 0x04 };
        byte[] spn1213 = { (byte) 0xBD, 0x04, 0x1D, 0x01 };
        byte[] spn3303 = { (byte) 0xE7, 0x0C, 0x1D, 0x01 };
        byte[] spn3304 = { (byte) 0xE8, 0x0C, 0x1D, 0x01 };
        byte[] spn5454 = { 0x4E, 0x15, 0x1D, 0x04 };
        byte[] spn588 = { 0x4C, 0x02, 0x1D, 0x11 };
        byte[] spn3055 = { (byte) 0xEF, 0x0B, 0x1F, 0x01 };
        byte[] spn5463 = { 0x57, 0x15, 0x1D, 0x01 };
        byte[] spn248 = { (byte) 0xF8, 0x00, 0x1D, 0x04 };

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(new SupportedSPN(convertToIntArray(spn92)));
                add(new SupportedSPN(convertToIntArray(spn512)));
                add(new SupportedSPN(convertToIntArray(spn513)));
                add(new SupportedSPN(convertToIntArray(spn544)));
                add(new SupportedSPN(convertToIntArray(spn539)));
                add(new SupportedSPN(convertToIntArray(spn540)));
                add(new SupportedSPN(convertToIntArray(spn541)));
                add(new SupportedSPN(convertToIntArray(spn542)));
                add(new SupportedSPN(convertToIntArray(spn543)));
                add(new SupportedSPN(convertToIntArray(spn110)));
                add(new SupportedSPN(convertToIntArray(spn175)));
                add(new SupportedSPN(convertToIntArray(spn190)));
                add(new SupportedSPN(convertToIntArray(spn84)));
                add(new SupportedSPN(convertToIntArray(spn108)));
                add(new SupportedSPN(convertToIntArray(spn158)));
                add(new SupportedSPN(convertToIntArray(spn51)));
                add(new SupportedSPN(convertToIntArray(spn94)));
                add(new SupportedSPN(convertToIntArray(spn172)));
                add(new SupportedSPN(convertToIntArray(spn105)));
                add(new SupportedSPN(convertToIntArray(spn132)));
                add(new SupportedSPN(convertToIntArray(spn976)));
                add(new SupportedSPN(convertToIntArray(spn91)));
                add(new SupportedSPN(convertToIntArray(spn183)));
                add(new SupportedSPN(convertToIntArray(spn102)));
                add(new SupportedSPN(convertToIntArray(spn173)));
                add(new SupportedSPN(convertToIntArray(spn3251)));
                add(new SupportedSPN(convertToIntArray(spn3483)));
                add(new SupportedSPN(convertToIntArray(spn5837)));
                add(new SupportedSPN(convertToIntArray(spn3301)));
                add(new SupportedSPN(convertToIntArray(spn5466)));
                add(new SupportedSPN(convertToIntArray(spn5323)));
                add(new SupportedSPN(convertToIntArray(spn3464)));
                add(new SupportedSPN(convertToIntArray(spn1209)));
                add(new SupportedSPN(convertToIntArray(spn5541)));
                add(new SupportedSPN(convertToIntArray(spn164)));
                add(new SupportedSPN(convertToIntArray(spn2791)));
                add(new SupportedSPN(convertToIntArray(spn1413)));
                add(new SupportedSPN(convertToIntArray(spn1414)));
                add(new SupportedSPN(convertToIntArray(spn1415)));
                add(new SupportedSPN(convertToIntArray(spn1416)));
                add(new SupportedSPN(convertToIntArray(spn1417)));
                add(new SupportedSPN(convertToIntArray(spn1418)));
                add(new SupportedSPN(convertToIntArray(spn3563)));
                add(new SupportedSPN(convertToIntArray(spn27)));
                add(new SupportedSPN(convertToIntArray(spn3242)));
                add(new SupportedSPN(convertToIntArray(spn3246)));
                add(new SupportedSPN(convertToIntArray(spn3216)));
                add(new SupportedSPN(convertToIntArray(spn1081)));
                add(new SupportedSPN(convertToIntArray(spn1189)));
                add(new SupportedSPN(convertToIntArray(spn5314)));
                add(new SupportedSPN(convertToIntArray(spn3609)));
                add(new SupportedSPN(convertToIntArray(spn3480)));
                add(new SupportedSPN(convertToIntArray(spn3226)));

                add(new SupportedSPN(convertToIntArray(spn1761)));
                add(new SupportedSPN(convertToIntArray(spn3482)));
                add(new SupportedSPN(convertToIntArray(spn3490)));
                add(new SupportedSPN(convertToIntArray(spn4360)));
                add(new SupportedSPN(convertToIntArray(spn4363)));
                add(new SupportedSPN(convertToIntArray(spn3031)));
                add(new SupportedSPN(convertToIntArray(spn4421)));
                add(new SupportedSPN(convertToIntArray(spn1173)));
                add(new SupportedSPN(convertToIntArray(spn237)));
                add(new SupportedSPN(convertToIntArray(spn4257)));
                add(new SupportedSPN(convertToIntArray(spn2304)));
                add(new SupportedSPN(convertToIntArray(spn899)));
                add(new SupportedSPN(convertToIntArray(spn2305)));
                add(new SupportedSPN(convertToIntArray(spn3069)));
                add(new SupportedSPN(convertToIntArray(spn3294)));
                add(new SupportedSPN(convertToIntArray(spn3295)));
                add(new SupportedSPN(convertToIntArray(spn3296)));
                add(new SupportedSPN(convertToIntArray(spn3302)));
                add(new SupportedSPN(convertToIntArray(spn3719)));
                add(new SupportedSPN(convertToIntArray(spn1323)));
                add(new SupportedSPN(convertToIntArray(spn1324)));
                add(new SupportedSPN(convertToIntArray(spn1325)));
                add(new SupportedSPN(convertToIntArray(spn1326)));
                add(new SupportedSPN(convertToIntArray(spn1327)));
                add(new SupportedSPN(convertToIntArray(spn1328)));
                add(new SupportedSPN(convertToIntArray(spn2630)));
                add(new SupportedSPN(convertToIntArray(spn2659)));
                add(new SupportedSPN(convertToIntArray(spn1322)));
                add(new SupportedSPN(convertToIntArray(spn4752)));
                add(new SupportedSPN(convertToIntArray(spn4766)));
                add(new SupportedSPN(convertToIntArray(spn5319)));
                add(new SupportedSPN(convertToIntArray(spn651)));
                add(new SupportedSPN(convertToIntArray(spn652)));
                add(new SupportedSPN(convertToIntArray(spn653)));
                add(new SupportedSPN(convertToIntArray(spn654)));
                add(new SupportedSPN(convertToIntArray(spn655)));
                add(new SupportedSPN(convertToIntArray(spn656)));
                add(new SupportedSPN(convertToIntArray(spn3471)));
                add(new SupportedSPN(convertToIntArray(spn3556)));
                add(new SupportedSPN(convertToIntArray(spn2307)));
                add(new SupportedSPN(convertToIntArray(spn4106)));
                add(new SupportedSPN(convertToIntArray(spn1216)));
                add(new SupportedSPN(convertToIntArray(spn1220)));
                add(new SupportedSPN(convertToIntArray(spn4127)));
                add(new SupportedSPN(convertToIntArray(spn4130)));
                add(new SupportedSPN(convertToIntArray(spn171)));
                add(new SupportedSPN(convertToIntArray(spn157)));
                add(new SupportedSPN(convertToIntArray(spn2623)));
                add(new SupportedSPN(convertToIntArray(spn247)));
                add(new SupportedSPN(convertToIntArray(spn235)));
                add(new SupportedSPN(convertToIntArray(spn1213)));
                add(new SupportedSPN(convertToIntArray(spn3303)));
                add(new SupportedSPN(convertToIntArray(spn3304)));
                add(new SupportedSPN(convertToIntArray(spn5454)));
                add(new SupportedSPN(convertToIntArray(spn588)));
                add(new SupportedSPN(convertToIntArray(spn3055)));
                add(new SupportedSPN(convertToIntArray(spn5463)));
                add(new SupportedSPN(convertToIntArray(spn248)));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.d"), description = "For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) is not included in DM24 response.")
    public void testMy2022ObjectsMissing12675() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1),
                                                                   SupportedSPN.create(12783, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1),
                                                                   SupportedSPN.create(12783, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(BI_DSL);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235, 
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539, 
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978, 
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837, 
                                                                 6895, 7333, 12691, 12730, 12783, 12797);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(BI_DSL), eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12783, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
                add(SupportedSPN.create(12783, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());

        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.d - SP 12675 is not included in DM24 response from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.d - SP 12675 is not included in DM24 response from Engine #2 (1)"));
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2"), description = "Verify that step completes without errors when none of the fail criteria are met using a MY2022+ engine.")
    public void testMy2022Objects() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12675, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1),
                                                                   SupportedSPN.create(12783, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12675, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1),
                                                                   SupportedSPN.create(12783, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(BI_DSL);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235,
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539,
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978,
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837,
                                                                 6895, 7333, 12675, 12691, 12730, 12783, 12797);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(BI_DSL), eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12675, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12783, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12675, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
                add(SupportedSPN.create(12783, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.d"), description = "For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) is not included in DM24 response.")
    public void testMy2022ObjectsMissing12783() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(BATT_ELEC);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235,
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539,
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978,
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837,
                                                                 6895, 7333, 12691, 12730, 12797);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(),
                                                          eq(expectedDataStreamsPacket4),
                                                          eq(BATT_ELEC),
                                                          eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());

        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.h - SP 12783 is not included in DM24 response from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.h - SP 12783 is not included in DM24 response from Engine #2 (1)"));
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.d"), description = "For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) is not included in DM24 response.")
    public void testMy2022ObjectsMissing12691() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(HYB_GAS);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235,
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539,
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978,
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837,
                                                                 6895, 7333, 12730, 12797);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(HYB_GAS), eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());

        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(WARN),
                                        eq("6.1.4.2.f - SP 12691 is not included in DM24 response from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(WARN),
                                        eq("6.1.4.2.f - SP 12691 is not included in DM24 response from Engine #2 (1)"));
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.d"), description = "For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) is not included in DM24 response.")
    public void testMy2022ObjectsMissing12730() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12675, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12675, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12797, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(HYB_GAS);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235,
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539,
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978,
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837,
                                                                 6895, 7333, 12675, 12691, 12797);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(HYB_GAS), eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12675, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12675, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12797, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());

        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.e - SP 12730 is not included in DM24 response from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.e - SP 12730 is not included in DM24 response from Engine #2 (1)"));
    }

    @Test
    // Testing object without any errors.
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.d"), description = "For MY2022+ diesel engines, Fail if SP 12675 (NOx Tracking Engine Activity Lifetime Fuel Consumption Bin 1 - Total) is not included in DM24 response.")
    public void testMy2022ObjectsMissing12797() {

        //@formatter:off
        DM24SPNSupportPacket packet1 = DM24SPNSupportPacket.create(0x00,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1));
        //@formatter:on
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(BusResult.of(packet1));

        OBDModuleInformation obd0x00 = new OBDModuleInformation(0);
        obd0x00.set(packet1, 1);
        dataRepository.putObdModule(obd0x00);

        //@formatter:off
        DM24SPNSupportPacket packet4 = DM24SPNSupportPacket.create(0x01,
                                                                   SupportedSPN.create(27, false, true, false, false, 1),
                                                                   SupportedSPN.create(84, false, true, false, false, 1),
                                                                   SupportedSPN.create(91, false, true, false, false, 1),
                                                                   SupportedSPN.create(92, false, true, true, false, 1),
                                                                   SupportedSPN.create(94, false, true, false, false, 1),
                                                                   SupportedSPN.create(102, true, true, false, false, 1),
                                                                   SupportedSPN.create(108, false, true, false, false, 1),
                                                                   SupportedSPN.create(110, false, true, true, false, 1),
                                                                   SupportedSPN.create(157, true, false, false, false, 1),
                                                                   SupportedSPN.create(158, false, true, false, false, 1),
                                                                   SupportedSPN.create(183, false, true, false, false, 1),
                                                                   SupportedSPN.create(190, false, true, true, true, 2),
                                                                   SupportedSPN.create(235, false, true, false, false, 1),
                                                                   SupportedSPN.create(247, false, true, false, false, 1),
                                                                   SupportedSPN.create(248, false, true, false, false, 1),
                                                                   SupportedSPN.create(512, false, true, true, false, 1),
                                                                   SupportedSPN.create(513, false, true, true, false, 1),
                                                                   SupportedSPN.create(514, false, true, false, false, 1),
                                                                   SupportedSPN.create(528, false, false, false, true, 0),
                                                                   SupportedSPN.create(529, false, false, true, true, 2),
                                                                   SupportedSPN.create(530, false, true, false, true, 0),
                                                                   SupportedSPN.create(531, false, true, true, true, 1),
                                                                   SupportedSPN.create(532, true, false, false, true, 4),
                                                                   SupportedSPN.create(533, true, false, true, true, 2),
                                                                   SupportedSPN.create(534, true, true, false, true, 0),
                                                                   SupportedSPN.create(535, true, true, true, true, 1),
                                                                   SupportedSPN.create(536, false, false, false, true, 0),
                                                                   SupportedSPN.create(537, false, false, true, true, 0),
                                                                   SupportedSPN.create(538, false, true, false, true, 1),
                                                                   SupportedSPN.create(539, false, true, false, true, 1),
                                                                   SupportedSPN.create(540, false, true, false, true, 1),
                                                                   SupportedSPN.create(541, false, true, false, true, 1),
                                                                   SupportedSPN.create(542, false, true, false, true, 1),
                                                                   SupportedSPN.create(543, false, true, false, false, 1),
                                                                   SupportedSPN.create(544, false, true, false, false, 1),
                                                                   SupportedSPN.create(651, true, false, false, false, 1),
                                                                   SupportedSPN.create(1323, true, false, false, false, 1),
                                                                   SupportedSPN.create(1324, true, false, false, false, 1),
                                                                   SupportedSPN.create(1325, true, false, false, false, 1),
                                                                   SupportedSPN.create(1326, true, false, false, false, 1),
                                                                   SupportedSPN.create(1413, false, true, false, false, 1),
                                                                   SupportedSPN.create(1634, false, true, false, false, 15),
                                                                   SupportedSPN.create(1635, false, true, false, false, 4),
                                                                   SupportedSPN.create(2630, true, false, false, false, 1),
                                                                   SupportedSPN.create(2791, false, true, false, false, 1),
                                                                   SupportedSPN.create(2978, false, true, false, false, 1),
                                                                   SupportedSPN.create(3031, false, true, false, false, 1),
                                                                   SupportedSPN.create(3058, true, false, false, false, 1),
                                                                   SupportedSPN.create(3226, true, true, false, false, 1),
                                                                   SupportedSPN.create(3251, true, false, false, false, 1),
                                                                   SupportedSPN.create(3301, false, false, true, true, 2),
                                                                   SupportedSPN.create(3361, true, false, false, false, 1),
                                                                   SupportedSPN.create(3516, false, true, false, false, 1),
                                                                   SupportedSPN.create(3609, false, true, false, false, 1),
                                                                   SupportedSPN.create(3700, false, true, false, false, 1),
                                                                   SupportedSPN.create(3713, true, false, false, false, 1),
                                                                   SupportedSPN.create(4364, true, false, false, false, 1),
                                                                   SupportedSPN.create(4752, true, false, false, false, 1),
                                                                   SupportedSPN.create(5018, true, false, false, false, 1),
                                                                   SupportedSPN.create(5466, false, true, false, false, 1),
                                                                   SupportedSPN.create(5827, false, true, false, false, 1),
                                                                   SupportedSPN.create(5829, false, true, false, false, 1),
                                                                   SupportedSPN.create(5837, false, true, false, false, 1),
                                                                   SupportedSPN.create(6895, false, true, false, false, 1),
                                                                   SupportedSPN.create(7333, false, true, false, false, 1),
                                                                   SupportedSPN.create(12691, false, true, false, false, 1),
                                                                   SupportedSPN.create(12730, false, true, false, false, 1));
        //@formatter:on

        when(communicationsModule.requestDM24(any(), eq(1))).thenReturn(BusResult.of(packet4));
        OBDModuleInformation obd0x01 = new OBDModuleInformation(0x01);
        obd0x01.set(packet4, 1);
        dataRepository.putObdModule(obd0x01);

        VehicleInformation vehicleInfo = new VehicleInformation();
        vehicleInfo.setEngineModelYear(2022);
        vehicleInfo.setFuelType(HYB_ETH);
        dataRepository.setVehicleInformation(vehicleInfo);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any(), anyInt())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(communicationsModule).setJ1939(j1939);

        verify(communicationsModule).requestDM24(any(), eq(0x00));

        verify(communicationsModule).requestDM24(any(), eq(0x01));

        verify(engineSpeedModule).setJ1939(j1939);

        //@formatter:off
        List<Integer> expectedDataStreamsPacket4 = Arrays.asList(27, 84, 91, 92, 94, 102, 108, 110, 158, 183, 190, 235,
                                                                 247, 248, 512, 513, 514, 530, 531, 534, 535, 538, 539,
                                                                 540, 541, 542, 543, 544, 1413, 1634, 1635, 2791, 2978,
                                                                 3031, 3226, 3516, 3609, 3700, 5466, 5827, 5829, 5837,
                                                                 6895, 7333, 12691, 12730);
        //@formatter:on

        Collections.sort(expectedDataStreamsPacket4);
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(HYB_ETH), eq(2022));

        List<Integer> expectedFreezeFrames = Arrays.asList(92, 110, 190, 512, 513, 529, 531, 533, 535, 537, 3301);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        List<SupportedSPN> expectedPacket1Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
            }
        };
        assertEquals(expectedPacket1Spns, dataRepository.getObdModule(0).getSupportedSPNs());

        List<SupportedSPN> expectedPacket4Spns = new ArrayList<>() {
            {
                add(SupportedSPN.create(27, false, true, false, false, 1));
                add(SupportedSPN.create(84, false, true, false, false, 1));
                add(SupportedSPN.create(91, false, true, false, false, 1));
                add(SupportedSPN.create(92, false, true, true, false, 1));
                add(SupportedSPN.create(94, false, true, false, false, 1));
                add(SupportedSPN.create(102, true, true, false, false, 1));
                add(SupportedSPN.create(108, false, true, false, false, 1));
                add(SupportedSPN.create(110, false, true, true, false, 1));
                add(SupportedSPN.create(157, true, false, false, false, 1));
                add(SupportedSPN.create(158, false, true, false, false, 1));
                add(SupportedSPN.create(183, false, true, false, false, 1));
                add(SupportedSPN.create(190, false, true, true, true, 2));
                add(SupportedSPN.create(235, false, true, false, false, 1));
                add(SupportedSPN.create(247, false, true, false, false, 1));
                add(SupportedSPN.create(248, false, true, false, false, 1));
                add(SupportedSPN.create(512, false, true, true, false, 1));
                add(SupportedSPN.create(513, false, true, true, false, 1));
                add(SupportedSPN.create(514, false, true, false, false, 1));
                add(SupportedSPN.create(528, false, false, false, true, 0));
                add(SupportedSPN.create(529, false, false, true, true, 2));
                add(SupportedSPN.create(530, false, true, false, true, 0));
                add(SupportedSPN.create(531, false, true, true, true, 1));
                add(SupportedSPN.create(532, true, false, false, true, 4));
                add(SupportedSPN.create(533, true, false, true, true, 2));
                add(SupportedSPN.create(534, true, true, false, true, 0));
                add(SupportedSPN.create(535, true, true, true, true, 1));
                add(SupportedSPN.create(536, false, false, false, true, 0));
                add(SupportedSPN.create(537, false, false, true, true, 0));
                add(SupportedSPN.create(538, false, true, false, true, 1));
                add(SupportedSPN.create(539, false, true, false, true, 1));
                add(SupportedSPN.create(540, false, true, false, true, 1));
                add(SupportedSPN.create(541, false, true, false, true, 1));
                add(SupportedSPN.create(542, false, true, false, true, 1));
                add(SupportedSPN.create(543, false, true, false, false, 1));
                add(SupportedSPN.create(544, false, true, false, false, 1));
                add(SupportedSPN.create(651, true, false, false, false, 1));
                add(SupportedSPN.create(1323, true, false, false, false, 1));
                add(SupportedSPN.create(1324, true, false, false, false, 1));
                add(SupportedSPN.create(1325, true, false, false, false, 1));
                add(SupportedSPN.create(1326, true, false, false, false, 1));
                add(SupportedSPN.create(1413, false, true, false, false, 1));
                add(SupportedSPN.create(1634, false, true, false, false, 15));
                add(SupportedSPN.create(1635, false, true, false, false, 4));
                add(SupportedSPN.create(2630, true, false, false, false, 1));
                add(SupportedSPN.create(2791, false, true, false, false, 1));
                add(SupportedSPN.create(2978, false, true, false, false, 1));
                add(SupportedSPN.create(3031, false, true, false, false, 1));
                add(SupportedSPN.create(3058, true, false, false, false, 1));
                add(SupportedSPN.create(3226, true, true, false, false, 1));
                add(SupportedSPN.create(3251, true, false, false, false, 1));
                add(SupportedSPN.create(3301, false, false, true, true, 2));
                add(SupportedSPN.create(3361, true, false, false, false, 1));
                add(SupportedSPN.create(3516, false, true, false, false, 1));
                add(SupportedSPN.create(3609, false, true, false, false, 1));
                add(SupportedSPN.create(3700, false, true, false, false, 1));
                add(SupportedSPN.create(3713, true, false, false, false, 1));
                add(SupportedSPN.create(4364, true, false, false, false, 1));
                add(SupportedSPN.create(4752, true, false, false, false, 1));
                add(SupportedSPN.create(5018, true, false, false, false, 1));
                add(SupportedSPN.create(5466, false, true, false, false, 1));
                add(SupportedSPN.create(5827, false, true, false, false, 1));
                add(SupportedSPN.create(5829, false, true, false, false, 1));
                add(SupportedSPN.create(5837, false, true, false, false, 1));
                add(SupportedSPN.create(6895, false, true, false, false, 1));
                add(SupportedSPN.create(7333, false, true, false, false, 1));
                add(SupportedSPN.create(12691, false, true, false, false, 1));
                add(SupportedSPN.create(12730, false, true, false, false, 1));
            }
        };
        expectedPacket4Spns.sort(Comparator.comparingInt(SupportedSPN::getSpn));
        assertEquals(expectedPacket4Spns, dataRepository.getObdModule(1).getSupportedSPNs());

        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.g - SP 12797 is not included in DM24 response from Engine #1 (0)"));
        verify(mockListener).addOutcome(eq(1),
                                        eq(4),
                                        eq(FAIL),
                                        eq("6.1.4.2.g - SP 12797 is not included in DM24 response from Engine #2 (1)"));
    }
}
