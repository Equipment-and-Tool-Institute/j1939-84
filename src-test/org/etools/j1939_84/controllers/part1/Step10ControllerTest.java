package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM11ClearActiveDTCsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticReadinessModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.OBDTestsModule;
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

@RunWith(MockitoJUnitRunner.class)
public class Step10ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticReadinessModule diagnosticReadinessModule;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step10Controller instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private OBDTestsModule obdTestsModule;

    @Mock
    private PartResultFactory partResultFactory;

    @Mock
    private ReportFileModule reportFileModule;

    @Mock
    private SectionA5Verifier sectionA5Verifier;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new Step10Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                dtcModule,
                partResultFactory,
                diagnosticReadinessModule,
                obdTestsModule,
                dataRepository,
                sectionA5Verifier);
        setup(instance, listener, j1939, engineSpeedModule, reportFileModule, executor, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(executor,
                engineSpeedModule,
                bannerModule,
                vehicleInformationModule,
                dtcModule,
                partResultFactory,
                obdTestsModule,
                dataRepository,
                diagnosticReadinessModule,
                mockListener,
                sectionA5Verifier);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Display Name", "Part 1 Step 10", instance.getDisplayName());
    }

    @Test
    public void testGetStepNumber() {
        assertEquals("Step Number", 10, instance.getStepNumber());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    public void testNoError() {

        AcknowledgmentPacket acknowledgmentPacket = mock(AcknowledgmentPacket.class);
        when(acknowledgmentPacket.getResponse()).thenReturn(Response.BUSY);

        DM11ClearActiveDTCsPacket dm11Packet = mock(DM11ClearActiveDTCsPacket.class);

        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);

        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        when(diagnosticReadinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm20Packet), Collections.emptyList()));

        when(dtcModule.requestDM11(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm11Packet),
                        Collections.singletonList(acknowledgmentPacket)));
        when(dtcModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm28Packet), Collections.emptyList()));

        when(sectionA5Verifier.verify(any(), any(), any(), any(), any())).thenReturn(true);

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(engineHoursPacket),
                        Collections.emptyList()));

        runTest();

        verify(dataRepository).getObdModuleAddresses();

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true));

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM11(any());
        verify(dtcModule).requestDM28(any());

        verify(obdTestsModule).setJ1939(j1939);

        verify(sectionA5Verifier).setJ1939(j1939);
        verify(sectionA5Verifier).verify(any(), any(), any(), any(), any());

        verify(vehicleInformationModule).requestEngineHours(any());

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    // @Test
    // public void testVerifyDiagnosticInformation() {
    // List<Integer> obdAddresses = new ArrayList<>() {
    // {
    // add(0);
    // add(3);
    // add(17);
    // }
    // };
    // when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddresses.stream().collect(Collectors.toSet()));
    //
    // List<ParsedPacket> globalPackets = new ArrayList<>();
    // DM11ClearActiveDTCsPacket packet1 =
    // mock(DM11ClearActiveDTCsPacket.class);
    //
    // DM6PendingEmissionDTCPacket packet2 =
    // mock(DM6PendingEmissionDTCPacket.class);
    // when(packet2.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
    //
    // DM12MILOnEmissionDTCPacket packet3 =
    // mock(DM12MILOnEmissionDTCPacket.class);
    // when(packet3.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
    //
    // DM23PreviouslyMILOnEmissionDTCPacket packet4 =
    // mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
    // when(packet4.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
    //
    // DM29DtcCounts packet5 = mock(DM29DtcCounts.class);
    // when(packet5.getAllPendingDTCCount()).thenReturn(-1);
    // when(packet5.getEmissionRelatedMILOnDTCCount()).thenReturn(-1);
    // when(packet5.getEmissionRelatedPendingDTCCount()).thenReturn(-1);
    // when(packet5.getEmissionRelatedPermanentDTCCount()).thenReturn(-1);
    // when(packet5.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(-1);
    //
    // DM5DiagnosticReadinessPacket packet6 =
    // mock(DM5DiagnosticReadinessPacket.class);
    // when(packet6.getActiveCodeCount()).thenReturn((byte) 0);
    // when(packet6.getPreviouslyActiveCodeCount()).thenReturn((byte) 0);
    //
    // DM25ExpandedFreezeFrame packet7 = mock(DM25ExpandedFreezeFrame.class);
    //
    // globalPackets.add(packet1);
    // globalPackets.add(packet2);
    // globalPackets.add(packet3);
    // globalPackets.add(packet4);
    // globalPackets.add(packet5);
    // globalPackets.add(packet6);
    // globalPackets.add(packet7);
    //
    // when(dtcModule.requestDM11(any(), any()))
    // .thenReturn(new RequestResult<>(false, globalPackets));
    // when(dtcModule.requestDM6(any(), any()))
    // .thenReturn(new RequestResult<ParsedPacket>(false, listOf(packet2)));
    // when(dtcModule.requestDM12(any())).thenReturn(new
    // RequestResult<ParsedPacket>(false, listOf(packet3)));
    // when(dtcModule.requestDM23(any())).thenReturn(new
    // RequestResult<ParsedPacket>(false, listOf(packet4)));
    // when(dtcModule.requestDM29(any())).thenReturn(new
    // RequestResult<ParsedPacket>(false, listOf(packet5)));
    // when(dtcModule.requestDM25(any(), eq(obdAddresses)))
    // .thenReturn(new RequestResult<ParsedPacket>(false, listOf(packet7)));
    //
    // when(diagnosticReadinessModule.requestDM5(any(), eq(true)))
    // .thenReturn(new RequestResult<ParsedPacket>(false, listOf(packet6)));
    //
    // runTest();
    //
    // verify(dataRepository).getObdModuleAddresses();
    //
    // verify(diagnosticReadinessModule).requestDM5(any(), eq(true));
    //
    // verify(dtcModule).setJ1939(j1939);
    // verify(dtcModule).requestDM11(any(), any());
    // verify(dtcModule).requestDM6(any(), any());
    // verify(dtcModule).requestDM12(any());
    // verify(dtcModule).requestDM23(any());
    // verify(dtcModule).requestDM29(any());
    // verify(dtcModule).requestDM25(any(), any());
    //
    // verify(mockListener).onMessage("An Error Occurred",
    // "Error",
    // MessageType.ERROR);
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // assertEquals("", listener.getResults());
    //
    // }

    // @Test
    // public void testWithNackFailure() {
    //
    // List<Integer> obdAddresses = new ArrayList<>() {
    // {
    // add(0);
    // add(3);
    // }
    // };
    // when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddresses.stream().collect(Collectors.toSet()));
    //
    // List<ParsedPacket> globalPackets = new ArrayList<>();
    // DM11ClearActiveDTCsPacket packet1 =
    // mock(DM11ClearActiveDTCsPacket.class);
    // AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
    // when(packet2.getResponse()).thenReturn(Response.NACK);
    // globalPackets.add(packet1);
    // globalPackets.add(packet2);
    //
    // when(dtcModule.requestDM11(any(), eq(obdAddresses))).thenReturn(new
    // RequestResult<>(false, globalPackets));
    //
    // runTest();
    //
    // verify(dataRepository).getObdModuleAddresses();
    // // verify(dateTimeModule).pauseFor(eq(300000L));
    // verify(dtcModule).setJ1939(j1939);
    // verify(dtcModule).requestDM11(any(), eq(obdAddresses));
    //
    // verify(mockListener).addOutcome(1,
    // 1,
    // Outcome.FAIL,
    // "6.1.10.2.a - The request for DM11 was NACK'ed");
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // String expectedResults = "FAIL: 6.1.10.2.a - The request for DM11 was
    // NACK'ed\n";
    // assertEquals(expectedResults, listener.getResults());
    //
    // }
    //
    // @Test
    // public void testWithNonNackWarning() {
    //
    // List<Integer> obdAddresses = new ArrayList<>() {
    // {
    // add(0);
    // add(3);
    // }
    // };
    // when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddresses.stream().collect(Collectors.toSet()));
    //
    // List<ParsedPacket> globalPackets = new ArrayList<>();
    // DM11ClearActiveDTCsPacket packet1 =
    // mock(DM11ClearActiveDTCsPacket.class);
    // AcknowledgmentPacket packet2 = mock(AcknowledgmentPacket.class);
    // when(packet2.getResponse()).thenReturn(Response.ACK);
    //
    // // Packet packet = mock(Packet.class);
    // // when(packet.toString()).thenReturn("Howdy");
    // // when(packet1.getPacket().toString()).thenReturn("Howdy");
    // // when(packet1.getPacket()).thenReturn(packet);
    // globalPackets.add(packet1);
    // globalPackets.add(packet2);
    //
    // when(dtcModule.requestDM11(any(), eq(obdAddresses))).thenReturn(new
    // RequestResult<>(false, globalPackets));
    //
    // runTest();
    //
    // verify(dataRepository).getObdModuleAddresses();
    // // verify(dateTimeModule).pauseFor(eq(300000L));
    // verify(dtcModule).setJ1939(j1939);
    // verify(dtcModule).requestDM11(any(), eq(obdAddresses));
    //
    // verify(mockListener).addOutcome(1,
    // 1,
    // Outcome.WARN,
    // "6.1.10.3.a - The request for DM11 was ACK'ed");
    //
    // assertEquals("", listener.getMessages());
    // assertEquals("", listener.getMilestones());
    // String expectedResults = "WARN: 6.1.10.3.a - The request for DM11 was
    // ACK'ed\n";
    // assertEquals(expectedResults, listener.getResults());
    //
    // }
}
