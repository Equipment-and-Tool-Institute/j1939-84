/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.FuelType.BI_GAS;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.SupportedSpnModule;
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
 * The unit test for {@link Part01Step04Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 1 Step 4", description = "DM24: SPN support"))
public class Part01Step04ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 1;

    private static final int STEP_NUMBER = 4;

    private static int[] convertToIntArray(byte[] input) {
        int[] intArray = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            // Range 0 to 255, not -128 to 127
            intArray[i] = Byte.toUnsignedInt(input[i]);
        }
        return intArray;
    }

    @Mock
    private BannerModule bannerModule;

    @Mock
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
    private DiagnosticMessageModule diagnosticMessageModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SupportedSpnModule supportedSpnModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);

        instance = new Part01Step04Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticMessageModule,
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
              diagnosticMessageModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 dataRepository,
                                 mockListener,
                                 supportedSpnModule);
    }

    // Test handling of no response from the modules
    @Test
    public void testEmptyObdModules() {
        DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
        when(packet1.getSourceAddress()).thenReturn(0);

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0);
        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfo1.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo1);
        when(dataRepository.getObdModules()).thenReturn(Collections.emptyList());
        when(dataRepository.getObdModule(0)).thenReturn(null);

        when(diagnosticMessageModule.requestDM24(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet1));

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);
        when(dataRepository.getVehicleInformation().getFuelType()).thenReturn(BI_GAS);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM24(any(), eq(0));

        verify(dataRepository).getObdModule(0);
        verify(dataRepository, atLeastOnce()).getObdModules();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();

        verify(engineSpeedModule).setJ1939(j1939);

        String expected = "";
        expected += "FAIL: 6.1.4.2.b - N.2 One or more SPNs for data stream is not supported" + NL;
        expected += "FAIL: 6.1.4.2.c - One or more SPNs for freeze frame are not supported" + NL;
        assertEquals("", listener.getResults());

        verify(mockListener)
                .addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                            "6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        verify(mockListener)
                .addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                            "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(reportFileModule)
                .addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                            "6.1.4.2.b - N.2 One or more SPNs for data stream is not supported");
        verify(reportFileModule)
                .addOutcome(PART_NUMBER, STEP_NUMBER, FAIL,
                            "6.1.4.2.c - One or more SPNs for freeze frame are not supported");

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any());
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
        when(packet1.getSourceAddress()).thenReturn(0);

        List<SupportedSPN> supportedSpns = new ArrayList<>();
        SupportedSPN spn1 = mock(SupportedSPN.class);
        supportedSpns.add(spn1);
        when(packet1.getSupportedSpns()).thenReturn(supportedSpns);

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0);
        OBDModuleInformation obdInfo4 = new OBDModuleInformation(1);
        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfo1.setObdCompliance((byte) 4);
        obdInfo4.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo1);
        obdInfoList.add(obdInfo4);
        when(dataRepository.getObdModules()).thenReturn(obdInfoList);
        when(dataRepository.getObdModule(0)).thenReturn(obdInfo1);

        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);

        when(diagnosticMessageModule.requestDM24(any(), eq(0)))
                .thenReturn(new BusResult<>(false, packet1));
        when(diagnosticMessageModule.requestDM24(any(), eq(1)))
                .thenReturn(new BusResult<>(true, packet4));

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);
        when(dataRepository.getVehicleInformation().getFuelType()).thenReturn(BI_GAS);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM24(any(), eq(0));
        verify(diagnosticMessageModule).requestDM24(any(), eq(1));

        verify(dataRepository).getObdModule(0);

        verify(dataRepository, atLeastOnce()).getObdModules();
        verify(dataRepository, atLeastOnce()).getVehicleInformation();
        verify(dataRepository).putObdModule(obdInfo1);

        verify(engineSpeedModule).setJ1939(j1939);

        String expected = "";
        expected += "FAIL: 6.1.4.2.a - Retry was required to obtain DM24 response from Engine #2 (1)" + NL;
        expected += "FAIL: 6.1.4.2.b - N.2 One or more SPNs for data stream is not supported" + NL;
        expected += "FAIL: 6.1.4.2.c - One or more SPNs for freeze frame are not supported" + NL;
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

        verify(supportedSpnModule).validateDataStreamSpns(any(), any(), any());
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
    @TestDoc(value = @TestItem(verifies = "6.1.4.2.a,b,c"),
            description = "Verify that step completes without errors when none of the fail criteria are met.")
    public void testGoodObjects() {

        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();

        OBDModuleInformation obdInfo0 = new OBDModuleInformation(0);
        obdInfo0.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo0);
        when(dataRepository.getObdModule(eq(0x00))).thenReturn(obdInfo0);

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(1);
        obdInfo1.setObdCompliance((byte) 4);
        obdInfoList.add(obdInfo1);
        when(dataRepository.getObdModule(eq(0x01))).thenReturn(obdInfo1);

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        DM24SPNSupportPacket packet1 = new DM24SPNSupportPacket(
                Packet.create(DM24SPNSupportPacket.PGN, 0x00,
                              0x5C, 0x00, 0x1B, 0x01, 0x00, 0x02, 0x1B, 0x01, 0x01, 0x02, 0x1B, 0x01));

        BusResult<DM24SPNSupportPacket> result1 = new BusResult<>(false, packet1);
        when(diagnosticMessageModule.requestDM24(any(), eq(0))).thenReturn(result1);

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

        BusResult<DM24SPNSupportPacket> result4 = new BusResult<>(false, packet4);
        when(diagnosticMessageModule.requestDM24(any(), eq(1))).thenReturn(result4);

        VehicleInformation vehicleInfo = mock(VehicleInformation.class);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInfo);
        when(dataRepository.getVehicleInformation().getFuelType()).thenReturn(BI_GAS);

        when(supportedSpnModule.validateDataStreamSpns(any(), any(), any())).thenReturn(true);
        when(supportedSpnModule.validateFreezeFrameSpns(any(), any())).thenReturn(true);

        runTest();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM24(any(), eq(0));
        verify(diagnosticMessageModule).requestDM24(any(), eq(1));

        verify(dataRepository).getObdModule(0);
        verify(dataRepository).getObdModule(1);
        verify(dataRepository, times(3)).getObdModules();

        verify(dataRepository, times(2)).getVehicleInformation();
        verify(dataRepository).putObdModule(obdInfo0);
        verify(dataRepository).putObdModule(obdInfo1);

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
        verify(supportedSpnModule).validateDataStreamSpns(any(), eq(expectedDataStreamsPacket4), eq(BI_GAS));

        List<Integer> expectedFreezeFrames = Arrays.asList(512, 513, 132, 1413, 1414, 1415, 3464, 1416,
                4360, 1417, 1418, 4363, 3216, 1173, 3480, 3609, 3226, 3482, 539, 3483, 27, 540, 541, 542, 158, 543, 544,
                3490, 164, 5541, 1189, 3242, 172, 173, 3246, 175, 51, 3251, 183, 1209, 1081, 190, 5314, 5323, 5837, 976,
                84, 3031, 5466, 91, 92, 94, 1761, 3301, 102, 2791, 105, 3563, 108, 110);
        Collections.sort(expectedFreezeFrames);
        verify(supportedSpnModule).validateFreezeFrameSpns(any(), eq(expectedFreezeFrames));

        byte[] spn92Packet1 = { 0x5C, 0x00, 0x1B, 0x01 };
        byte[] spn512Packet1 = { 0x00, 0x02, 0x1B, 0x01 };
        byte[] spn513Packet1 = { 0x01, 0x02, 0x1B, 0x01 };
        List<SupportedSPN> expectedPacket1Spns = List.of(
                new SupportedSPN(convertToIntArray(spn92Packet1)),
                new SupportedSPN(convertToIntArray(spn512Packet1)),
                new SupportedSPN(convertToIntArray(spn513Packet1)));
        assertEquals(expectedPacket1Spns, obdInfo0.getSupportedSpns());

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
        assertEquals(expectedPacket4Spns, obdInfo1.getSupportedSpns());
    }
}
