/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.model.FuelType;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939tools.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939tools.j1939.packets.AddressClaimPacket;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The unit test for {@link Part01Step03Controller}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@TestDoc(value = @TestItem(verifies = "Part 1 Step 3", description = "DM5: Diagnostic readiness 1"))
@RunWith(MockitoJUnitRunner.class)
public class Part01Step03ControllerTest {

    @Mock
    private BannerModule bannerModule;

    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Part01Step03Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        DateTimeModule.setInstance(null);
        dataRepository = DataRepository.newInstance();

        instance = new Part01Step03Controller(executor,
                                              engineSpeedModule,
                                              bannerModule,
                                              vehicleInformationModule,
                                              communicationsModule,
                                              dataRepository,
                                              DateTimeModule.getInstance());
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 communicationsModule,
                                 mockListener);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    @TestDoc(value = {
            @TestItem(verifies = "6.1.3.2.b"),
            @TestItem(verifies = "6.1.3.3.a") }, description = "Verify there are fail messages for: <ul><li>Not all responses are identical.</li>"
                    +
                    "<li>The request for DM5 was NACK'ed</li></ul>", dependsOn = {
                            "DM5DiagnosticReadinessPacketTest", "DiagnosticReadinessPacketTest" })
    public void testBadECUValue() {
        List<DM5DiagnosticReadinessPacket> packets = new ArrayList<>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(true, packets, acks);
        when(communicationsModule.requestDM5(any())).thenReturn(requestResult);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        PgnDefinition pgnDefinition = mock(PgnDefinition.class);
        when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet1.getPgnDefinition().getId()).thenReturn(DM5DiagnosticReadinessPacket.PGN);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet2.getPgnDefinition().getId()).thenReturn(AcknowledgmentPacket.PGN);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        acks.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet3.getPgnDefinition().getId()).thenReturn(AcknowledgmentPacket.PGN);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        acks.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet4.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet4.getPgnDefinition().getId()).thenReturn(DM5DiagnosticReadinessPacket.PGN);
        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);

        DM5DiagnosticReadinessPacket packet5 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet5.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet5.getPgnDefinition().getId()).thenReturn(DM5DiagnosticReadinessPacket.PGN);
        when(packet5.isObd()).thenReturn(true);
        when(packet5.getSourceAddress()).thenReturn(17);
        when(packet5.getOBDCompliance()).thenReturn((byte) 5);
        packets.add(packet5);

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0, -1);
        dataRepository.putObdModule(obdInfo1);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(17, -1);
        dataRepository.putObdModule(obdInfo2);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setUsCarb(true);
        dataRepository.setVehicleInformation(vehicleInformation);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(communicationsModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(mockListener).addOutcome(1,
                                        3,
                                        WARN,
                                        "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");
        verify(mockListener).addOutcome(1,
                                        3,
                                        WARN,
                                        "6.1.3.3.b - Response received from a non-OBD ECU provided OBD Compliance values of 0h");
        verify(mockListener).addOutcome(1,
                                        3,
                                        INFO,
                                        "6.1.3.3.c - Response received from a non-OBD ECU provided OBD Compliance values of 0h");
        verify(mockListener).addOutcome(1,
                                        3,
                                        FAIL,
                                        "6.1.3.2.e - US/CARB vehicle does not provide OBD Compliance values of 13h, 14h, 22h, or 23h");
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.3", description = "Verifies part and step name for report."))
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 3", instance.getDisplayName());
    }

    @Test
    @TestDoc(value = @TestItem(verifies = "6.1.3", description = "Verifies that there is a single 6.1.3 step."))
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 0, instance.getTotalSteps());
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    @TestDoc(value = @TestItem(verifies = "6.1.3.2.a"), description = "There needs to be at least one OBD ECU.")
    public void testModulesEmpty() {
        var packets = new ArrayList<DM5DiagnosticReadinessPacket>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(true, packets, acks);
        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.DENIED);
        acks.add(packet2);

        when(communicationsModule.requestDM5(any())).thenReturn(requestResult);
        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setFuelType(FuelType.DSL);
        dataRepository.setVehicleInformation(vehicleInformation);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.a - No ECU reported as an OBD ECU");
        verify(mockListener).addOutcome(1,
                                        3,
                                        WARN,
                                        "6.1.3.3.b - Response received from a non-OBD ECU provided OBD Compliance values of 0h");
        verify(mockListener).addOutcome(1,
                                        3,
                                        INFO,
                                        "6.1.3.3.c - Response received from a non-OBD ECU provided OBD Compliance values of 0h");
        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "The method is called just to get some exception.")
    @TestDoc(value = @TestItem(verifies = "6.1.3.2.b"), description = "The request for DM5 was NACK'ed")
    public void testRun() {
        List<DM5DiagnosticReadinessPacket> packets = new ArrayList<>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(false, packets, acks);
        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getOBDCompliance()).thenReturn((byte) 4);
        PgnDefinition pgnDefinition = mock(PgnDefinition.class);
        when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet1.getPgnDefinition().getId()).thenReturn(DM5DiagnosticReadinessPacket.PGN);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet2.getPgnDefinition().getId()).thenReturn(AcknowledgmentPacket.PGN);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        acks.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet3.getPgnDefinition().getId()).thenReturn(AcknowledgmentPacket.PGN);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        acks.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet4.getPgnDefinition()).thenReturn(pgnDefinition);
        when(packet4.getPgnDefinition().getId()).thenReturn(DM5DiagnosticReadinessPacket.PGN);

        OBDModuleInformation obdInfo = new OBDModuleInformation(0, 0);
        dataRepository.putObdModule(obdInfo);

        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);
        when(communicationsModule.requestDM5(any())).thenReturn(requestResult);

        AddressClaimPacket addressClaimPacket = mock(AddressClaimPacket.class);
        when(addressClaimPacket.getSourceAddress()).thenReturn(0);
        when(addressClaimPacket.getFunctionId()).thenReturn(0);
        RequestResult<AddressClaimPacket> addressClaimResults = new RequestResult<>(false, addressClaimPacket);

        VehicleInformation vehicleInformation = new VehicleInformation();
        vehicleInformation.setAddressClaim(addressClaimResults);
        dataRepository.setVehicleInformation(vehicleInformation);

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(communicationsModule).setJ1939(j1939);
        verify(communicationsModule).requestDM5(any());

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(mockListener).addOutcome(1,
                                        3,
                                        FAIL,
                                        "6.1.3.2.d - Fail if any response from a function 0 device provides OBD Compliance values of 0, 5, FBh, FCh, FDh, FEh, or FFh");
        verify(mockListener).addOutcome(1,
                                        3,
                                        INFO,
                                        "6.1.3.3.c - Response received from a non-OBD ECU provided OBD Compliance values of 4h");
        verify(vehicleInformationModule).setJ1939(j1939);
    }

}
