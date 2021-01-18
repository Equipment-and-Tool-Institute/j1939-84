/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part2;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.StepController;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.controllers.part1.Step04Controller;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
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
 * The unit test for {@link Step04Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 2 Step 3", description = "DM24: SPN support"))
public class Part02Step03ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 2;

    private static final int STEP_NUMBER = 3;

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

    private DataRepository dataRepository;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part02Step03Controller instance;

    @Mock
    private J1939 j1939;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDTestsModule obdTestsModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    private TestResultsListener listener;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(null);

        dataRepository = new DataRepository();
        listener = new TestResultsListener(mockListener);

        instance = new Part02Step03Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                obdTestsModule,
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
                                 obdTestsModule,
                                 mockListener);
    }

    @Test
    public void testEmptyObdModules() {

        runTest();

        verify(obdTestsModule).setJ1939(j1939);
    }

    @Test
    // Testing the object with all possible errors
    @TestDoc(value = {
            @TestItem(verifies = "6.2.3", dependsOn = "DM24SPNSupportPacketTest"),
            @TestItem(verifies = "6.2.3.2.a")
    }, description = "Using a response that indicates that 6.2.3.2.b failed, verify that the failures are in the report.")
    public void testFailures() {

        {
            DM24SPNSupportPacket packet0 = mock(DM24SPNSupportPacket.class);
            when(packet0.getSourceAddress()).thenReturn(0);
            when(obdTestsModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));
            List<SupportedSPN> supportedSPNs0 = new ArrayList<>();
            SupportedSPN spn1 = mock(SupportedSPN.class);
            supportedSPNs0.add(spn1);
            SupportedSPN spn2 = mock(SupportedSPN.class);
            supportedSPNs0.add(spn2);
            when(packet0.getSupportedSpns()).thenReturn(supportedSPNs0);

            OBDModuleInformation obdInfo0 = new OBDModuleInformation(0);
            obdInfo0.setObdCompliance((byte) 4);
            obdInfo0.setSupportedSpns(List.of(spn1, spn2, mock(SupportedSPN.class)));
            dataRepository.putObdModule(0, obdInfo0);
        }

        {
            DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
            when(packet1.getSourceAddress()).thenReturn(1);
            when(obdTestsModule.requestDM24(any(), eq(1))).thenReturn(new BusResult<>(false, packet1));
            List<SupportedSPN> supportedSpns = new ArrayList<>();
            SupportedSPN spn1 = mock(SupportedSPN.class);
            supportedSpns.add(spn1);
            SupportedSPN spn2 = mock(SupportedSPN.class);
            supportedSpns.add(spn2);
            when(packet1.getSupportedSpns()).thenReturn(supportedSpns);

            OBDModuleInformation obdInfo1 = new OBDModuleInformation(1);
            obdInfo1.setObdCompliance((byte) 4);
            obdInfo1.setSupportedSpns(List.of(spn1));
            dataRepository.putObdModule(1, obdInfo1);
        }

        runTest();

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM24(any(), eq(0));
        verify(obdTestsModule).requestDM24(any(), eq(1));

        String expected = "";
        expected += "FAIL: 6.2.3.2.a - Message data received from Engine #1 (0) differs from that provided in part 6.1.4" + NL;
        expected += "FAIL: 6.2.3.2.a - Message data received from Engine #2 (1) differs from that provided in part 6.1.4" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.2.a - Message data received from Engine #1 (0) differs from that provided in part 6.1.4");
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.2.a - Message data received from Engine #2 (1) differs from that provided in part 6.1.4");
    }

    @Test
    @TestDoc(description = "Verify step name is correct.")
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Step 3", instance.getDisplayName());
    }

    @Test
    @TestDoc(description = "Verify that there is only one step in 6.2.3.")
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
    @TestDoc(value = @TestItem(verifies = "6.2.3.2.b"),
            description = "Verify that step completes without errors when none of the fail criteria are met.")
    public void testNoFailures() {

        {
            DM24SPNSupportPacket packet0 = mock(DM24SPNSupportPacket.class);
            when(packet0.getSourceAddress()).thenReturn(0);
            when(obdTestsModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));
            List<SupportedSPN> supportedSPNs0 = new ArrayList<>();
            SupportedSPN spn1 = mock(SupportedSPN.class);
            supportedSPNs0.add(spn1);
            SupportedSPN spn2 = mock(SupportedSPN.class);
            supportedSPNs0.add(spn2);
            when(packet0.getSupportedSpns()).thenReturn(supportedSPNs0);

            OBDModuleInformation obdInfo0 = new OBDModuleInformation(0);
            obdInfo0.setObdCompliance((byte) 4);
            obdInfo0.setSupportedSpns(List.of(spn1, spn2));
            dataRepository.putObdModule(0, obdInfo0);
        }

        {
            DM24SPNSupportPacket packet1 = mock(DM24SPNSupportPacket.class);
            when(packet1.getSourceAddress()).thenReturn(1);
            when(obdTestsModule.requestDM24(any(), eq(1))).thenReturn(new BusResult<>(false, packet1));
            List<SupportedSPN> supportedSpns = new ArrayList<>();
            SupportedSPN spn1 = mock(SupportedSPN.class);
            supportedSpns.add(spn1);
            SupportedSPN spn2 = mock(SupportedSPN.class);
            supportedSpns.add(spn2);
            when(packet1.getSupportedSpns()).thenReturn(supportedSpns);

            OBDModuleInformation obdInfo1 = new OBDModuleInformation(1);
            obdInfo1.setObdCompliance((byte) 4);
            obdInfo1.setSupportedSpns(List.of(spn1, spn2));
            dataRepository.putObdModule(1, obdInfo1);
        }

        runTest();

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM24(any(), eq(0));
        verify(obdTestsModule).requestDM24(any(), eq(1));

        assertEquals("", listener.getResults());

    }
}
