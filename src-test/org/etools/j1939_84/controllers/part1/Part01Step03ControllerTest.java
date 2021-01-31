/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.AddressClaimPacket;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.testdoc.TestDoc;
import org.etools.testdoc.TestItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

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

        instance = new Part01Step03Controller(
                executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                diagnosticMessageModule,
                dataRepository,
                DateTimeModule.getInstance());
    }

    @After
    public void tearDown() throws Exception {

        verifyNoMoreInteractions(executor,
                                 engineSpeedModule,
                                 bannerModule,
                                 vehicleInformationModule,
                                 diagnosticMessageModule,
                                 dataRepository,
                                 mockListener);
    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    @TestDoc(value = {
            @TestItem(verifies = "6.1.3.2.b"),
            @TestItem(verifies = "6.1.3.3.a") },
            description = "Verify there are fail messages for: <ul><li>Not all responses are identical.</li>"
                    +
                    "<li>The request for DM5 was NACK'ed</li></ul>",
            dependsOn = {
                    "DM5DiagnosticReadinessPacketTest", "DiagnosticReadinessPacketTest" })
    public void testBadECUValue() {
        List<DM5DiagnosticReadinessPacket> packets = new ArrayList<>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(true, packets, acks);
        when(diagnosticMessageModule.requestDM5(any())).thenReturn(requestResult);

        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        acks.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        acks.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);

        DM5DiagnosticReadinessPacket packet5 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet5.isObd()).thenReturn(true);
        when(packet5.getSourceAddress()).thenReturn(17);
        when(packet5.getOBDCompliance()).thenReturn((byte) 5);
        packets.add(packet5);

        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();

        OBDModuleInformation obdInfo1 = new OBDModuleInformation(0);
        obdInfo1.setObdCompliance((byte) 4);
        obdInfo1.setFunction(-1);
        obdInfoList.add(obdInfo1);

        OBDModuleInformation obdInfo2 = new OBDModuleInformation(17);
        obdInfo2.setObdCompliance((byte) 5);
        obdInfo2.setFunction(-1);
        obdInfoList.add(obdInfo2);

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);

        VehicleInformation vehicleInformation = new VehicleInformation();
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(dataRepository, times(2)).getVehicleInformation();
        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(engineSpeedModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
        verify(dataRepository).getObdModules();
        verify(diagnosticMessageModule).requestDM5(any());
        verify(dataRepository).putObdModule(obdInfo1);
        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(dataRepository).putObdModule(obdInfo2);
        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(mockListener).addOutcome(1,
                                        3,
                                        WARN,
                                        "6.1.3.3.a - An ECU responded with a value for OBD Compliance that was not identical to other ECUs");

        String expectedObd = "OBD Module Information: " + NL;
        expectedObd += "sourceAddress is : 0" + NL;
        expectedObd += "obdCompliance is : 4" + NL;
        expectedObd += "function is : -1" + NL;
        expectedObd += "ignition cycles is : 0" + NL;
        expectedObd += "engine family name is : " + NL;
        expectedObd += "model year is : " + NL;
        expectedObd += "Scaled Test Results: []" + NL;
        expectedObd += "Performance Ratios: []" + NL;
        expectedObd += "Monitored Systems: []" + NL;
        expectedObd += "Supported SPNs: " + NL;
        assertEquals(expectedObd, obdInfo1.toString());
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
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    @TestDoc(value = @TestItem(verifies = "6.1.3.2.a"),
            description = "There needs to be at least one OBD Module.")
    public void testModulesEmpty() {
        var packets = new ArrayList<DM5DiagnosticReadinessPacket>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(true, packets, acks);
        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.DENIED);
        acks.add(packet2);

        Collection<OBDModuleInformation> obdInfoList = List.of();
        when(dataRepository.getObdModules()).thenReturn(obdInfoList);
        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(requestResult);

        instance.execute(listener, j1939, reportFileModule);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(dataRepository).getObdModules();

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.a - There needs to be at least one OBD Module");

        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.a - There needs to be at least one OBD Module");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.a - There needs to be at least one OBD Module");
        verify(reportFileModule).addOutcome(1,
                                            3,
                                            FAIL,
                                            "6.1.3.2.a - There needs to be at least one OBD Module");

        verify(vehicleInformationModule).setJ1939(j1939);

    }

    @Test
    @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT",
            justification = "The method is called just to get some exception.")
    @TestDoc(value = @TestItem(verifies = "6.1.3.2.b"), description = "The request for DM5 was NACK'ed")
    public void testRun() {
        List<DM5DiagnosticReadinessPacket> packets = new ArrayList<>();
        List<AcknowledgmentPacket> acks = new ArrayList<>();
        RequestResult<DM5DiagnosticReadinessPacket> requestResult = new RequestResult<>(false, packets, acks);
        DM5DiagnosticReadinessPacket packet1 = mock(DM5DiagnosticReadinessPacket.class);
        when(packet1.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet1);

        AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
        when(packet2.getResponse()).thenReturn(Response.ACK);
        acks.add(packet2);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        acks.add(packet3);

        DM5DiagnosticReadinessPacket packet4 = mock(DM5DiagnosticReadinessPacket.class);

        OBDModuleInformation obdInfo = new OBDModuleInformation(0);
        obdInfo.setFunction(0);
        obdInfo.setObdCompliance((byte) 4);

        Collection<OBDModuleInformation> obdInfoList = new ArrayList<>();
        obdInfoList.add(obdInfo);

        when(dataRepository.getObdModules()).thenReturn(obdInfoList);
        when(packet4.isObd()).thenReturn(true);
        when(packet4.getSourceAddress()).thenReturn(0);
        when(packet4.getOBDCompliance()).thenReturn((byte) 4);
        packets.add(packet4);
        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(requestResult);

        VehicleInformation vehicleInformation = new VehicleInformation();
        AddressClaimPacket addressClaimPacket = mock(AddressClaimPacket.class);
        when(addressClaimPacket.getSourceAddress()).thenReturn(0);
        when(addressClaimPacket.getFunctionId()).thenReturn(0);
        RequestResult<AddressClaimPacket> addressClaimResults = new RequestResult<>(false, addressClaimPacket);
        vehicleInformation.setAddressClaim(addressClaimResults);
        when(dataRepository.getVehicleInformation()).thenReturn(vehicleInformation);

        instance.execute(listener, j1939, reportFileModule);

        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();

        verify(dataRepository).getVehicleInformation();
        verify(dataRepository).getObdModules();
        verify(dataRepository).putObdModule(obdInfo);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());

        verify(engineSpeedModule).setJ1939(j1939);

        verify(mockListener).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(reportFileModule).addOutcome(1, 3, FAIL, "6.1.3.2.b - The request for DM5 was NACK'ed");
        verify(reportFileModule).onResult("FAIL: 6.1.3.2.b - The request for DM5 was NACK'ed");

        verify(vehicleInformationModule).setJ1939(j1939);
    }

}
