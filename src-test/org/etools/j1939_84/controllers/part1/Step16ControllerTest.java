/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket.Response;
import org.etools.j1939_84.bus.j1939.packets.DM2PreviouslyActiveDTC;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCodePacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.ParsedPacket;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.Outcome;
import org.etools.j1939_84.model.PartResultFactory;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.BannerModule;
import org.etools.j1939_84.modules.DTCModule;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.EngineSpeedModule;
import org.etools.j1939_84.modules.ReportFileModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
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
 *
 * @author Garrison Garland (garrison@soliddesign.net)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class Step16ControllerTest extends AbstractControllerTest {

    @Mock
    private BannerModule bannerModule;

    @Mock
    private DataRepository dataRepository;

    private DateTimeModule dateTimeModule;

    @Mock
    private DiagnosticTroubleCodePacket diagnosticTroubleCodePacket;

    @Mock
    private DTCModule dtcModule;

    @Mock
    private EngineSpeedModule engineSpeedModule;

    @Mock
    private Executor executor;

    private Step16Controller instance;

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

        instance = new Step16Controller(executor,
                engineSpeedModule,
                bannerModule,
                dateTimeModule,
                vehicleInformationModule,
                dtcModule,
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
                mockListener,
                reportFileModule,
                dtcModule);
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testDTCsNotEmpty() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        DiagnosticTroubleCode Dtc1 = mock(DiagnosticTroubleCode.class);
        when(packet1.getDtcs()).thenReturn(listOf(Dtc1));
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getSourceAddress()).thenReturn(3);

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(packet1), Collections.singletonList(packet3)));

        // Set up the destination specific packets we will be returning when
        // requested
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        List<DiagnosticTroubleCode> packet2Dtc = new ArrayList<>();
        DiagnosticTroubleCode Dtc2 = mock(DiagnosticTroubleCode.class);
        packet2Dtc.add(Dtc2);
        when(packet2.getSourceAddress()).thenReturn(0);
        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new RequestResult<>(false, listOf(packet2), Collections.emptyList()));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        when(packet4.getSourceAddress()).thenReturn(3);
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), listOf(packet4)));

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
                add(0);
                add(3);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.a - OBD ECU reported a previously active DTC");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.a - OBD ECU reported a previously active DTC");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

    }

    @Test
    public void testGetDiplayName() {
        assertEquals("Display Name", "Part 1 Step 16", instance.getDisplayName());
    }

    @Test
    public void testGetTotalSteps() {
        assertEquals("Total Steps", 1, instance.getTotalSteps());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILNotSupported() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OTHER);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        // what was this for? globalPackets.add(packet3);

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc(@TestItem(verifies = "6.1.16.2.a"))
    public void testMILOff() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        // because this is a NACK, the SA will not be requested.
        // when(packet3.getSourceAddress()).thenReturn(3);

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(packet1), Collections.singletonList(packet3)));

        // Set up the destination specific packets we will be returning when
        // requested
        List<ParsedPacket> destinationSpecificPackets = new ArrayList<>();
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        destinationSpecificPackets.add(packet2);

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        destinationSpecificPackets.add(packet4);

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.2.a,b") })
    public void testMILStatusNotOFF() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getDtcs()).thenReturn(Collections.emptyList());
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        // FIXME What is this? globalPackets.add(packet3);

        // Set up the destination specific packets we will be returning when
        // requested
        List<ParsedPacket> destinationSpecificPackets = new ArrayList<>();
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        destinationSpecificPackets.add(packet2);
        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new RequestResult<>(false, listOf(packet2), Collections.emptyList()));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        destinationSpecificPackets.add(packet4);
        when(packet4.getSourceAddress()).thenReturn(3);
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), listOf(packet4)));

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
                add(0);
                add(3);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    public void testNoErrors() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getDtcs()).thenReturn(Collections.emptyList());
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getSourceAddress()).thenReturn(3);
        // FIXME what was this? globalPackets.add(packet3);

        // Set up the destination specific packets we will be returning when
        // requested
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new RequestResult<>(false, listOf(packet2), Collections.emptyList()));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        when(packet4.getSourceAddress()).thenReturn(3);

        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), listOf(packet4)));

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
                add(0);
                add(3);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(reportFileModule).onProgress(0, 1, "");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b,c") })
    public void testNonOBDMilOn() {
        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getDtcs()).thenReturn(Collections.emptyList());
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.NACK);
        // FIXME what was this? globalPackets.add(packet3);

        // Set up the destination specific packets we will be returning when
        // requested
        List<ParsedPacket> destinationSpecificPackets = new ArrayList<>();
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        destinationSpecificPackets.add(packet2);
        mock(DiagnosticTroubleCode.class);

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported");
        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.b - OBD ECU does not report MIL off");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.2.c - non-OBD ECU does not report MIL off or not supported");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a,b") })
    public void testResponseNotNACK() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        AcknowledgmentPacket packet3 = mock(AcknowledgmentPacket.class);
        when(packet3.getResponse()).thenReturn(Response.ACK);
        when(packet3.getSourceAddress()).thenReturn(3);

        when(dtcModule.requestDM2(any(), eq(true))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(packet1), Collections.singletonList(packet3)));

        // Set up the destination specific packets we will be returning when
        // requested
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new RequestResult<>(false, listOf(packet2), Collections.emptyList()));

        // add ACK/NACK packets to the listing for complete reality testing
        DM2PreviouslyActiveDTC packet4 = mock(DM2PreviouslyActiveDTC.class);
        when(packet4.getSourceAddress()).thenReturn(3);
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new RequestResult<>(false, listOf(packet4), Collections.emptyList()));

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
                add(0);
                add(3);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");
        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.b Nack not received from OBD ECUs that did not respond to global query");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.b Nack not received from OBD ECUs that did not respond to global query");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

    @Test
    @TestDoc({ @TestItem(verifies = "6.1.16.4.a") })
    public void testResponsesAreDifferent() {

        DM2PreviouslyActiveDTC packet1 = mock(DM2PreviouslyActiveDTC.class);
        when(packet1.getSourceAddress()).thenReturn(0);
        when(packet1.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
        when(dtcModule.requestDM2(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(packet1), Collections.emptyList()));

        DM2PreviouslyActiveDTC packet3 = mock(DM2PreviouslyActiveDTC.class);
        when(packet3.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);
        when(packet3.getSourceAddress()).thenReturn(3);
        // FIXME what was this? globalPackets.add(packet3);

        // Set up the destination specific packets we will be returning when
        // requested
        DM2PreviouslyActiveDTC packet2 = mock(DM2PreviouslyActiveDTC.class);
        when(packet2.getSourceAddress()).thenReturn(0);
        when(dtcModule.requestDM2(any(), eq(true), eq(0)))
                .thenReturn(new RequestResult<>(false, listOf(packet2), Collections.emptyList()));

        // add ACK/NACK packets to the listing for complete reality testing
        AcknowledgmentPacket packet4 = mock(AcknowledgmentPacket.class);
        when(packet4.getSourceAddress()).thenReturn(3);
        when(dtcModule.requestDM2(any(), eq(true), eq(3)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(), listOf(packet4)));

        // Return the modules address so that we can do the destination specific
        // calls
        Set<Integer> obdAddressSet = new HashSet<>() {
            {
                add(0);
                add(3);
            }
        };
        when(dataRepository.getObdModuleAddresses()).thenReturn(obdAddressSet);

        runTest();

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM2(any(), eq(true));
        verify(dtcModule).requestDM2(any(), eq(true), eq(0));
        verify(dtcModule).requestDM2(any(), eq(true), eq(3));

        verify(dataRepository, atLeastOnce()).getObdModuleAddresses();

        verify(mockListener).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        verify(reportFileModule).onProgress(0, 1, "");
        verify(reportFileModule).addOutcome(1,
                16,
                Outcome.FAIL,
                "6.1.16.4.a DS DM2 responses differ from global responses");

        assertEquals("", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());
    }

}
