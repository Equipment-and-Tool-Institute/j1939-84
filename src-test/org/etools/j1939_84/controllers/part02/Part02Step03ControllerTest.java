/*
 * Copyright 2021 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part02;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.BusResult;
import net.solidDesign.j1939.J1939;
import net.solidDesign.j1939.packets.DM24SPNSupportPacket;
import net.solidDesign.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import net.solidDesign.j1939.modules.CommunicationsModule;
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
 * The unit test for {@link Part02Step03Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@TestDoc(value = @TestItem(verifies = "Part 2 Step 3", description = "DM24: SPN support"))
public class Part02Step03ControllerTest extends AbstractControllerTest {

    private static final int PART_NUMBER = 2;

    private static final int STEP_NUMBER = 3;

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

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(null);

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new Part02Step03Controller(
                                              executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
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
                                 communicationsModule);
    }

    @Test
    public void testEmptyObdModules() {
        runTest();

        assertEquals("", listener.getResults());
        assertEquals("", listener.getMessages());
    }

    @Test
    // Testing the object with all possible errors
    @TestDoc(value = {
            @TestItem(verifies = "6.2.3", dependsOn = "DM24SPNSupportPacketTest"),
            @TestItem(verifies = "6.2.3.2.a")
    }, description = "Using a response that indicates that 6.2.3.2.b failed, verify that the failures are in the report.")
    public void testFailures() {

        SupportedSPN spn1 = SupportedSPN.create(123, true, true, true, 1);

        OBDModuleInformation obdInfo0 = new OBDModuleInformation(0);
        obdInfo0.set(DM24SPNSupportPacket.create(0, spn1), 1);
        dataRepository.putObdModule(obdInfo0);

        SupportedSPN spn2 = SupportedSPN.create(456, true, true, true, 1);
        DM24SPNSupportPacket packet0 = DM24SPNSupportPacket.create(0, spn2);
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        runTest();

        verify(communicationsModule).requestDM24(any(), eq(0));

        assertEquals("", listener.getResults());

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.2.a - Message data received from Engine #1 (0) differs from that provided in part 6.1.4");
    }

    @Test
    @TestDoc(description = "Verify step name is correct.")
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 2 Step 3", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals(STEP_NUMBER, instance.getStepNumber());
    }

    @Test
    @TestDoc(description = "Verify that there is only one step in 6.2.3.")
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.2.3.2.b"), description = "Verify that step completes without errors when none of the fail criteria are met.")
    public void testNoFailures() {
        SupportedSPN spn1 = SupportedSPN.create(123, true, true, true, 1);
        SupportedSPN spn2 = SupportedSPN.create(456, true, true, true, 1);
        OBDModuleInformation obdInfo0 = new OBDModuleInformation(0);
        obdInfo0.set(DM24SPNSupportPacket.create(0, spn1, spn2), 1);
        dataRepository.putObdModule(obdInfo0);

        DM24SPNSupportPacket packet0 = DM24SPNSupportPacket.create(0, spn1, spn2);
        when(communicationsModule.requestDM24(any(), eq(0))).thenReturn(new BusResult<>(false, packet0));

        runTest();

        verify(communicationsModule).requestDM24(any(), eq(0));

        assertEquals("", listener.getResults());
    }
}
