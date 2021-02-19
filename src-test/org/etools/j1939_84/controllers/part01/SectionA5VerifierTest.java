/*
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.BusResult;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939_84.bus.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DTCLampStatus;
import org.etools.j1939_84.bus.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939_84.bus.j1939.packets.EngineHoursPacket;
import org.etools.j1939_84.bus.j1939.packets.LampStatus;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystem;
import org.etools.j1939_84.bus.j1939.packets.MonitoredSystemStatus;
import org.etools.j1939_84.bus.j1939.packets.ScaledTestResult;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.RequestResult;
import org.etools.j1939_84.modules.DateTimeModule;
import org.etools.j1939_84.modules.DiagnosticMessageModule;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939_84.utils.AbstractControllerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The unit test for {@link SectionA5Verifier}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("This class isn't currently being used")
public class SectionA5VerifierTest extends AbstractControllerTest {

    @Mock
    private DataRepository dataRepository;

    @Mock
    private DiagnosticMessageModule diagnosticMessageModule;

    private SectionA5Verifier instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        listener = new TestResultsListener(mockListener);

        instance = new SectionA5Verifier(dataRepository, diagnosticMessageModule, vehicleInformationModule);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(dataRepository,
                                 diagnosticMessageModule,
                                 mockListener,
                                 vehicleInformationModule);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#setJ1939(org.etools.j1939_84.bus.j1939.J1939)}.
     */
    @Test
    public void testSetJ1939() {

        instance.setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM12() {

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(List.of());
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm12Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM12(listener));

        assertEquals("PASS: Section A.5 Step 1.b DM12 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).requestDM12(any());

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM12Fail() {

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(List.of());
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.FAST_FLASH);

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm12Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM12(
                listener));

        String expectedMessages = "Section A.5 verification failed during DM12 check done at table step 1.b" + NL +
                "Modules with source address 0, reported 0 DTCs." +
                NL +
                "MIL status is : fast flash.";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM12FailTwo() {

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        DiagnosticTroubleCode dm12dtc = new DiagnosticTroubleCode(new int[] { 0x61, 0x02, 0x13, 0x81 });
        when(dm12Packet.getDtcs()).thenReturn(List.of(dm12dtc));
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.FAST_FLASH);

        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm12Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM12(
                listener));

        String expectedMessages = "Section A.5 verification failed during DM12 check done at table step 1.b" + NL +
                "Modules with source address 0, reported 1 DTCs." +
                NL +
                "MIL status is : fast flash.";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM12(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM20() {
        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm20Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM20(List.of(dm20Packet),
                                       listener));

        assertEquals("PASS: Section A.5 Step 7.a DM20 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM20Fail() {
        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);
        when(dm20Packet.toString()).thenReturn("dm20Packet.toString()");
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm20Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM20(List.of(),
                                        listener));

        String expectedMessages = "Section A.5 verification failed during DM20 check done at table step 7.a" + NL
                + "Previous Monitor Performance Ratio (DM20):" + NL +
                "Post Monitor Performance Ratio (DM20):" + NL + "dm20Packet.toString()" + NL;
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM21() {
        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM21(listener));

        assertEquals("PASS: Section A.5 Step 3.b & 5.b DM21 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM21Fail() {
        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        when(dm21Packet.getMinutesWhileMILIsActivated()).thenReturn(15.0);
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM21(listener));

        String expectedMessages = "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b" + NL
                + "Modules with source address 0, reported :" + NL +
                "0.0 km(s) for distance with the MIL on" + NL +
                "15.0 minute(s) run with the MIL on" + NL + "15.0 minute(s) while MIL is activated" +
                NL + "0.0 km(s) since DTC code clear sent" + NL +
                "0.0 minute(s) since the DTC code clear sent";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM21FailThree() {
        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        when(dm21Packet.getKmWhileMILIsActivated()).thenReturn(15.0);
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM21(listener));

        String expectedMessages = "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b" + NL
                + "Modules with source address 0, reported :" + NL +
                "15.0 km(s) for distance with the MIL on" + NL +
                "0.0 minute(s) run with the MIL on" + NL + "0.0 minute(s) while MIL is activated" +
                NL + "0.0 km(s) since DTC code clear sent" + NL +
                "0.0 minute(s) since the DTC code clear sent";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM21FailTwo() {
        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        when(dm21Packet.getMinutesSinceDTCsCleared()).thenReturn(15.0);
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM21(listener));

        String expectedMessages = "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b" + NL
                + "Modules with source address 0, reported :" + NL +
                "0.0 km(s) for distance with the MIL on" + NL +
                "0.0 minute(s) run with the MIL on" + NL + "0.0 minute(s) while MIL is activated" +
                NL + "0.0 km(s) since DTC code clear sent" + NL +
                "15.0 minute(s) since the DTC code clear sent";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM21(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM23() {

        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm23Packet), List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM23(listener));

        assertEquals("PASS: Section A.5 Step 1.c DM23 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM23(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM23Fail() {

        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);

        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm23Packet), List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM23(listener));

        String expectedMessages = "Section A.5 verification failed during DM23 check done at table step 1.c" + NL
                + "Module with source address 0, reported 0 DTCs." + NL +
                "MIL status is : on.";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM23(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM25() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        byte[] dm25bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM25(listener, obdModuleAddresses));

        assertEquals("PASS: Section A.5 Step 2.a DM25 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM25Fail() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        byte[] dm25bytes = { (byte) 0xFB, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        byte[] dm25bytes17 = { (byte) 0x00, 0x00, 0x00, 0x0A, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes17);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        byte[] dm25bytes21 = { 0x00, (byte) 0xEE, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes21);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM25(listener, obdModuleAddresses));

        String expectedMessages = "Section A.5 verification failed during DM25 check done at table step 2.a" + NL
                + "Module with source address 0, has 1 supported SPNs" + NL +
                "Module with source address 0, has 1 supported SPNs" + NL +
                "Module with source address 33, has 1 supported SPNs";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM25FailTwo() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        byte[] dm25bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x09, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        byte[] dm25bytes17 = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, 0x08, (byte) 0xFF };
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes17);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        byte[] dm25bytes21 = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xAE };
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes21);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM25(listener, obdModuleAddresses));

        String expectedMessages = "Section A.5 verification failed during DM25 check done at table step 2.a" + NL
                + "Module with source address 0, has 1 supported SPNs" + NL +
                "Module with source address 23, has 1 supported SPNs" + NL +
                "Module with source address 33, has 1 supported SPNs";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM26() {
        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);

        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm26Packet), List.of()));

        instance.setJ1939(j1939);
        assertTrue(instance.verifyDM26(listener));

        assertEquals("PASS: Section A.5 Step 5.a DM26 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM26Fail() {
        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);
        when(dm26Packet.getWarmUpsSinceClear()).thenReturn((byte) 0x02);

        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm26Packet), List.of()));

        instance.setJ1939(j1939);
        assertFalse(instance.verifyDM26(listener));

        String expectedMessages = "Section A.5 verification failed during DM26 check done at table step 5.a" +
                NL +
                "Modules with source address 0, reported 2 warm-ups since code clear";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM26(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM28() {
        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);
        when(dm28Packet.getDtcs()).thenReturn(List.of());

        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm28Packet), List.of()));
        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM28(List.of(dm28Packet),
                                       listener));

        assertEquals("PASS: Section A.5 Step 8.a DM28 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM28(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM28Fail() {
        DM28PermanentEmissionDTCPacket dm28Packet = new DM28PermanentEmissionDTCPacket(Packet
                                                                                               .create(DM28PermanentEmissionDTCPacket.PGN,
                                                                                                       0x00,
                                                                                                       0x42,
                                                                                                       0xFD,
                                                                                                       0x9D,
                                                                                                       0x00,
                                                                                                       0x07,
                                                                                                       0x01,
                                                                                                       0xFF,
                                                                                                       0xFF));

        DM28PermanentEmissionDTCPacket previousDM28Packet = new DM28PermanentEmissionDTCPacket(Packet
                                                                                                       .create(DM28PermanentEmissionDTCPacket.PGN,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00,
                                                                                                               0x00));

        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm28Packet), List.of()));
        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM28(List.of(previousDM28Packet),
                                        listener));

        String expectedMessages = "Section A.5 verification failed during DM28 check done at table step 8.a" + NL
                + "Pre DTC all clear code sent retrieved the DM28 packet :" + NL +
                "DM28 from Engine #1 (0): MIL: alternate off, RSL: alternate off, AWL: alternate off, PL: alternate off, No DTCs"
                +
                NL + "Post DTC all clear code sent retrieved the DM28 packet :" + NL +
                "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" + NL +
                "DTC 157:7 - Engine Fuel 1 Injector Metering Rail 1 Pressure, Mechanical System Not Responding Or Out Of Adjustment - 1 times";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM28(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM29() {
        new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(0);

        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));

        instance.setJ1939(j1939);
        assertTrue(instance.verifyDM29(listener));

        assertEquals("PASS: Section A.5 Step 1.d DM29 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM29Fail() {
        new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.toString()).thenReturn("dm29Packet.toString()");
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(1);

        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));

        instance.setJ1939(j1939);
        assertFalse(instance.verifyDM29(listener));

        String expectedMessages = "Section A.5 verification failed during DM29 check done at table step 1.d" + NL +
                "Modules with source address 0, dm29Packet.toString() ";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM29FailThree() {
        new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.toString()).thenReturn("dm29Packet.toString()");
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(1);

        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));

        instance.setJ1939(j1939);
        assertFalse(instance.verifyDM29(listener));

        String expectedMessages = "Section A.5 verification failed during DM29 check done at table step 1.d" + NL +
                "Modules with source address 0, dm29Packet.toString() ";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM29FailTwo() {
        new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.toString()).thenReturn("dm29Packet.toString()");
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(1);

        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));

        instance.setJ1939(j1939);
        assertFalse(instance.verifyDM29(listener));

        String expectedMessages = "Section A.5 verification failed during DM29 check done at table step 1.d" + NL +
                "Modules with source address 0, dm29Packet.toString() ";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM29(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM31() {
        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);
        when(dm31Packet.getDtcLampStatuses()).thenReturn(List.of());

        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm31Packet), List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM31(listener));

        assertEquals("PASS: Section A.5 Step 3.a DM31 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM31(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM31Fail() {
        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);
        DTCLampStatus dm31DtcLampStatus = mock(DTCLampStatus.class);
        when(dm31Packet.getDtcLampStatuses()).thenReturn(List.of(dm31DtcLampStatus));

        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm31Packet), List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM31(listener));

        String expectedMessages = "Section A.5 verification failed during DM31 check done at table step 3.a" + NL +
                "Modules with source address 0, is reporting 1 with DTC lamp status(es) causing MIL on.";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM31(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM33() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        int[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x0B, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF, 0x0C, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x0D, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x31, 0x01, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x38, 0x1A, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF };
        Packet.create(0, 0, data);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        //
        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet21), List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM33(dm33Packets, listener, obdModuleAddresses));

        assertEquals("PASS: Section A.5 Step 9.a DM33 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM33Fail() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = new DM33EmissionIncreasingAECDActiveTime(
                Packet.create(DM33EmissionIncreasingAECDActiveTime.PGN, 0x00,
                              0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        when(dm33Packet17.toString()).thenReturn("dm33Packet17.toString()");
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);
        when(dm33Packet21.toString()).thenReturn("dm33Packet21.toString()");

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet21), List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM33(dm33Packets, listener, obdModuleAddresses));

        String expectedMessages = "Section A.5 verification failed during DM33 check done at table step 9.a" + NL;
        expectedMessages += "Pre DTC all clear code sent retrieved the DM33 packet :" + NL;
        expectedMessages += "   dm33Packet17.toString()" + NL + "   dm33Packet21.toString()" + NL;
        expectedMessages += "Post DTC all clear code sent retrieved the DM33 packet :" + NL;
        expectedMessages += "   DM33 Emission Increasing AECD Active Time from Engine #1 (0): {" + NL;
        expectedMessages += "EI-AECD Number = 1: Timer 1 = 0 minutes; Timer 2 = n/a" + NL;
        expectedMessages += "}" + NL + NL;
        expectedMessages += "   dm33Packet17.toString()" + NL;
        expectedMessages += "   dm33Packet21.toString()";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM33FailTwo() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = new DM33EmissionIncreasingAECDActiveTime(
                Packet.create(DM33EmissionIncreasingAECDActiveTime.PGN, 0x00,
                              0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF));
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.toString()).thenReturn("dm33Packet17.toString()");
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.toString()).thenReturn("dm33Packet21.toString()");
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(), List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM33(dm33Packets, listener, obdModuleAddresses));

        String expectedMessages = "Section A.5 verification failed during DM33 check done at table step 9.a" + NL;
        expectedMessages += "Pre DTC all clear code sent retrieved the DM33 packet :" + NL;
        expectedMessages += "   dm33Packet17.toString()" + NL;
        expectedMessages += "   dm33Packet21.toString()" + NL;
        expectedMessages += "Post DTC all clear code sent retrieved the DM33 packet :" + NL;
        expectedMessages += "   DM33 Emission Increasing AECD Active Time from Engine #1 (0): {" + NL;
        expectedMessages += "EI-AECD Number = 1: Timer 1 = 0 minutes; Timer 2 = n/a" + NL;
        expectedMessages += "}" + NL + NL;
        expectedMessages += "   dm33Packet17.toString()";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * <p>
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verifyDM5(org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM5() {

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm5Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM5(listener));

        String expectedMessages = "PASS: Section A.5 Step 1.e DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 4.a DM5 Verification";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verifyDM5(org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM5Fail() {

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        when(dm5Packet.toString()).thenReturn("dm5Packet.toString()");
        when(dm5Packet.getActiveCodeCount()).thenReturn((byte) 0x02);
        MonitoredSystemStatus dm5Status = mock(MonitoredSystemStatus.class);
        when(dm5Status.isEnabled()).thenReturn(true);
        when(dm5Status.isComplete()).thenReturn(true);
        MonitoredSystem dm5MonitoredSystem = mock(MonitoredSystem.class);
        when(dm5MonitoredSystem.getStatus()).thenReturn(dm5Status);
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(List.of(dm5MonitoredSystem));
        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm5Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM5(listener));

        String expectedMessages = "Section A.5 verification failed during DM5 check done at table step 1.e" +
                NL +
                "Modules with source address 0, reported 2 active DTCs and 0 previously active DTCs" + NL +
                "Section A.5 verification failed during DM5 check done at table step 4.a" + NL +
                "Module address 0 :" + NL + "dm5Packet.toString()";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verifyDM5(org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM5FailTwo() {

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        when(dm5Packet.getActiveCodeCount()).thenReturn((byte) 0x02);
        MonitoredSystemStatus dm5Status = mock(MonitoredSystemStatus.class);
        when(dm5Status.isEnabled()).thenReturn(true);
        when(dm5Status.isComplete()).thenReturn(false);
        MonitoredSystem dm5MonitoredSystem = mock(MonitoredSystem.class);
        when(dm5MonitoredSystem.getStatus()).thenReturn(dm5Status);
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(List.of(dm5MonitoredSystem));
        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm5Packet),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM5(listener));

        String expectedMessages = "Section A.5 verification failed during DM5 check done at table step 1.e" +
                NL +
                "Modules with source address 0, reported 2 active DTCs and 0 previously active DTCs" + NL +
                "PASS: Section A.5 Step 4.a DM5 Verification";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM6() {
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm6Packet),
                                                List.of()));
        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM6(listener));

        assertEquals("PASS: Section A.5 Step 1.a DM6 Verfication", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM6Fail() {
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        DiagnosticTroubleCode dtc = mock(DiagnosticTroubleCode.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of(dtc));
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.SLOW_FLASH);

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm6Packet),
                                                List.of()));
        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM6(listener));

        String expectedMessages = "Section A.5 verification failed at DM6 check done at table step 1.a" +
                NL +
                "Modules with source address 0, reported 1 DTCs." + NL +
                "MIL status is : slow flash.";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyDM7DM30() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet0.getTestResults()).thenReturn(List.of(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(List.of(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(List.of(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyDM7DM30(listener, obdModuleAddresses));

        assertEquals("PASS: Section A.5 Step 6.a DM7/DM30 Verification", listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyDM7DM30Fail() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0x0000);
        when(scaledTestResult0.getTestValue()).thenReturn(0x88);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0x0000);
        when(dm30Packet0.getTestResults()).thenReturn(List.of(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestValue()).thenReturn(0x55);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(List.of(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(List.of(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyDM7DM30(listener, obdModuleAddresses));

        String expectedMessages = "Section A.5 verification failed during DM7/DM30 check done at table step 6.a" + NL +
                "DM30 Scaled Test Results for" +
                NL + "source address 0 are : [" + NL +
                "  TestResult failed and the value returned was : 136" +
                NL + "]" + NL + "source address 0 are : [" + NL +
                "  TestResult failed and the value returned was : 85" + NL + "]";
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void testVerifyEngineHours() {

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, List.of(engineHoursPacket),
                                                List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verifyEngineHours(
                List.of(engineHoursPacket),
                listener));

        assertEquals(
                "PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification",
                listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    @Test
    public void testVerifyEngineHoursEmptyPackets() {

        EngineHoursPacket engineHoursPacket = new EngineHoursPacket(
                Packet.create(EngineHoursPacket.PGN, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        EngineHoursPacket engineHoursPacket1 = new EngineHoursPacket(
                Packet.create(EngineHoursPacket.PGN, 0x03, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Arrays.asList(engineHoursPacket, engineHoursPacket1),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyEngineHours(
                List.of(),
                listener));

        String expected = "";
        expected += "Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))" + NL;
        expected += " and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any" + NL;
        expected += " non-zero values present before code clear check done at table step 9.b" + NL;
        expected += "Previous packet(s) was/were:" + NL;
        expected += "   EMPTY" + NL;
        expected += "Current packet(s) was/were:" + NL;
        expected += "   Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 57210087.250000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;
        expected += "" + NL;
        expected += "   Engine Hours from Transmission #1 (3): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 71638931.600000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 287454020000.000000 r" + NL;
        expected += NL;
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    @Test
    public void testVerifyEngineHoursFail() {

        EngineHoursPacket engineHoursPacket = new EngineHoursPacket(
                Packet.create(EngineHoursPacket.PGN, 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88));
        EngineHoursPacket engineHoursPacket1 = new EngineHoursPacket(
                Packet.create(EngineHoursPacket.PGN, 0x03, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Arrays.asList(engineHoursPacket, engineHoursPacket1),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verifyEngineHours(
                List.of(engineHoursPacket),
                listener));

        String expected = "";
        expected += "Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))" + NL;
        expected += " and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any" + NL;
        expected += " non-zero values present before code clear check done at table step 9.b" + NL;
        expected += "Previous packet(s) was/were:" + NL;
        expected += "   Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 57210087.250000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;
        expected += "" + NL;
        expected += "Current packet(s) was/were:" + NL;
        expected += "   Engine Hours from Engine #1 (0): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 57210087.250000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 2289526357000.000000 r" + NL;
        expected += "" + NL;
        expected += "   Engine Hours from Transmission #1 (3): " + NL;
        expected += "  SPN   247, Engine Total Hours of Operation: 71638931.600000 h" + NL;
        expected += "  SPN   249, Engine Total Revolutions: 287454020000.000000 r" + NL;
        expected += NL;
        assertEquals(expected, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(diagnosticMessageModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Testing errors in method
     * for{@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyError() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        MonitoredSystem monitoredSystemDM5 = mock(MonitoredSystem.class);
        MonitoredSystemStatus monitoredSystemStatusDM5 = mock(MonitoredSystemStatus.class);
        when(monitoredSystemStatusDM5.isEnabled()).thenReturn(true);
        when(monitoredSystemStatusDM5.isComplete()).thenReturn(true);
        when(monitoredSystemDM5.getStatus()).thenReturn(monitoredSystemStatusDM5);
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(List.of(monitoredSystemDM5));
        when(dm5Packet.getActiveCodeCount()).thenReturn((byte) 2);
        when(dm5Packet.toString()).thenReturn(
                "DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22");

        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        DiagnosticTroubleCode diagnosticTroubleCodeDM6 = mock(DiagnosticTroubleCode.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of(diagnosticTroubleCodeDM6));
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.FAST_FLASH);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        DiagnosticTroubleCode diagnosticTroubleCodeDM12 = mock(DiagnosticTroubleCode.class);
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.SLOW_FLASH);
        when(dm12Packet.getDtcs()).thenReturn(List.of(diagnosticTroubleCodeDM12));

        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);
        String expectedDM20 = "";
        expectedDM20 += "DM20 from Engine #1 (0): [" + NL;
        expectedDM20 += " Num'r / Den'r" + NL;
        expectedDM20 += "Ignition Cycles 42,405" + NL;
        expectedDM20 += "OBD Monitoring Conditions Encountered 23,130" + NL;
        expectedDM20 += "SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535" + NL;
        expectedDM20 += "]";
        when(dm20Packet.toString()).thenReturn(expectedDM20);

        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        when(dm21Packet.getKmSinceDTCsCleared()).thenReturn(2453.3);

        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        DiagnosticTroubleCode dm23Dtc = mock(DiagnosticTroubleCode.class);
        when(dm23Packet.getDtcs()).thenReturn(List.of(dm23Dtc));
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);

        byte[] dm25bytes = { 0x00, 0x00, (byte) 0xFF, 0x00, 0x00, (byte) 0xFF, 0x00, (byte) 0x00, 0x00 };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);
        when(dm26Packet.getWarmUpsSinceClear()).thenReturn((byte) 2);

        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);
        DiagnosticTroubleCode diagnosticTroubleCode28 = mock(DiagnosticTroubleCode.class);
        String expectedDM28String = "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" + NL +
                "DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times";
        when(dm28Packet.toString()).thenReturn(
                expectedDM28String);
        when(dm28Packet.getDtcs()).thenReturn(List.of(diagnosticTroubleCode28));

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(-1);

        String expectedDM29String = "DM29 from Engine #1 (0): " + NL +
                "Emission-Related Pending DTC Count 9" +
                NL +
                "All Pending DTC Count 32" +
                NL +
                "Emission-Related MIL-On DTC Count 71" +
                NL +
                "Emission-Related Previously MIL-On DTC Count 49" +
                NL +
                "Emission-Related Permanent DTC Count 1";
        when(dm29Packet.toString()).thenReturn(expectedDM29String);

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet0.getSourceAddress()).thenReturn(0x00);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet17.getSourceAddress()).thenReturn(0x17);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet21.getSourceAddress()).thenReturn(0x21);

        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);
        DTCLampStatus dtcLampStatusDM31 = mock(DTCLampStatus.class);
        when(dm31Packet.getDtcLampStatuses()).thenReturn(List.of(dtcLampStatusDM31));

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);
        when(engineHoursPacket.toString()).thenReturn("Engine Hours from Engine #1 (0): 210,554,060.75 hours");

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0xFF02);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0xFB00);
        when(dm30Packet0.getTestResults()).thenReturn(List.of(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFB00);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFF16);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0x0000);
        when(dm30Packet17.getTestResults()).thenReturn(List.of(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0x0000);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0x0016);
        when(dm30Packet21.getTestResults()).thenReturn(List.of(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestDM5(any())).thenReturn(new RequestResult<>(false,
                                                                                       List.of(dm5Packet),
                                                                                       List.of()));
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm20Packet),
                                                List.of()));

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm6Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm12Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm21Packet), List.of()));
        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm23Packet), List.of()));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));
        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm26Packet), List.of()));
        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm28Packet), List.of()));
        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));
        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm31Packet), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17)))
                .thenReturn(new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21)))
                .thenReturn(new RequestResult<>(false, List.of(dm33Packet21), List.of()));

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(engineHoursPacket),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verify(List.of(),
                                    List.of(),
                                    dm33Packets,
                                    List.of(),
                                    listener));

        String expectedMessages = "Section A.5 verification failed at DM6 check done at table step 1.a" + NL +
                "Modules with source address 0, reported 1 DTCs." +
                NL +
                "MIL status is : fast flash." +
                NL +
                "Section A.5 verification failed during DM12 check done at table step 1.b" +
                NL +
                "Modules with source address 0, reported 1 DTCs." +
                NL +
                "MIL status is : slow flash." +
                NL +
                "Section A.5 verification failed during DM23 check done at table step 1.c" +
                NL +
                "Module with source address 0, reported 1 DTCs." +
                NL +
                "MIL status is : on." +
                NL +
                "Section A.5 verification failed during DM29 check done at table step 1.d" +
                NL +
                "Modules with source address 0, DM29 from Engine #1 (0): " +
                NL +
                "Emission-Related Pending DTC Count 9" +
                NL +
                "All Pending DTC Count 32" +
                NL +
                "Emission-Related MIL-On DTC Count 71" +
                NL +
                "Emission-Related Previously MIL-On DTC Count 49" +
                NL +
                "Emission-Related Permanent DTC Count 1 " +
                NL +
                "Section A.5 verification failed during DM25 check done at table step 2.a" +
                NL +
                "Module with source address 0, has 1 supported SPNs" +
                NL +
                "Module with source address 23, has 1 supported SPNs" +
                NL +
                "Module with source address 33, has 1 supported SPNs" +
                NL +
                "Section A.5 verification failed during DM31 check done at table step 3.a" +
                NL +
                "Modules with source address 0, is reporting 1 with DTC lamp status(es) causing MIL on." +
                NL +
                "Section A.5 verification failed during DM21 check done at table step 3.b & 5.b" +
                NL +
                "Modules with source address 0, reported :" +
                NL +
                "0.0 km(s) for distance with the MIL on" +
                NL +
                "0.0 minute(s) run with the MIL on" +
                NL +
                "0.0 minute(s) while MIL is activated" +
                NL +
                "2453.3 km(s) since DTC code clear sent" +
                NL +
                "0.0 minute(s) since the DTC code clear sent" +
                NL +
                "Section A.5 verification failed during DM5 check done at table step 1.e" +
                NL +
                "Modules with source address 0, reported 2 active DTCs and 0 previously active DTCs" +
                NL +
                "Section A.5 verification failed during DM5 check done at table step 4.a" +
                NL +
                "Module address 0 :" +
                NL +
                "DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22" +
                NL +
                "Section A.5 verification failed during DM26 check done at table step 5.a" +
                NL +
                "Modules with source address 0, reported 2 warm-ups since code clear" +
                NL +
                "Section A.5 verification failed during DM7/DM30 check done at table step 6.a" +
                NL +
                "DM30 Scaled Test Results for" +
                NL +
                "source address 0 are : [" +
                NL +
                "  TestMaximum failed and the value returned was : 65282" +
                NL +
                "]" +
                NL +
                "source address 23 are : [" +
                NL +
                "  TestResult failed and the value returned was : 65302" +
                NL +
                "]" +
                NL +
                "source address 33 are : [" +
                NL +
                "  TestMinimum failed and the value returned was : 22" +
                NL +
                "]" +
                NL +
                "Section A.5 verification failed during DM20 check done at table step 7.a" +
                NL +
                "Previous Monitor Performance Ratio (DM20):" +
                NL +
                "Post Monitor Performance Ratio (DM20):" +
                NL +
                "DM20 from Engine #1 (0): [" +
                NL +
                " Num'r / Den'r" +
                NL +
                "Ignition Cycles 42,405" +
                NL +
                "OBD Monitoring Conditions Encountered 23,130" +
                NL +
                "SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535" +
                NL +
                "]" +
                NL +
                NL +
                "Section A.5 verification failed during DM28 check done at table step 8.a" +
                NL +
                "Pre DTC all clear code sent retrieved the DM28 packet :" +
                NL +
                NL +
                "Post DTC all clear code sent retrieved the DM28 packet :" +
                NL +
                "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" +
                NL +
                "DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times"
                +
                NL +
                "PASS: Section A.5 Step 9.a DM33 Verification" +
                NL +
                "Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))" +
                NL +
                " and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any" +
                NL +
                " non-zero values present before code clear check done at table step 9.b" +
                NL +
                "Previous packet(s) was/were:" +
                NL +
                "   EMPTY" +
                NL +
                "Current packet(s) was/were:" +
                NL +
                "   Engine Hours from Engine #1 (0): 210,554,060.75 hours" +
                NL;
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM23(any());

        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));

        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM28(any());
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM31(any());
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Testing errors in method
     * for{@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test

    public void testVerifyMoreError() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        MonitoredSystem monitoredSystemDM5 = mock(MonitoredSystem.class);
        MonitoredSystemStatus monitoredSystemStatusDM5 = mock(MonitoredSystemStatus.class);
        when(monitoredSystemStatusDM5.isEnabled()).thenReturn(false);
        when(monitoredSystemDM5.getStatus()).thenReturn(monitoredSystemStatusDM5);
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(List.of(monitoredSystemDM5));
        when(dm5Packet.getPreviouslyActiveCodeCount()).thenReturn((byte) 2);

        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OTHER);
        when(dm12Packet.getDtcs()).thenReturn(List.of());

        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);
        String expectedDM20 = "";
        expectedDM20 += "DM20 from Engine #1 (0): [" + NL;
        expectedDM20 += " Num'r / Den'r" + NL;
        expectedDM20 += "Ignition Cycles 42,405" + NL;
        expectedDM20 += "OBD Monitoring Conditions Encountered 23,130" + NL;
        expectedDM20 += "SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535" + NL;
        expectedDM20 += "]";
        when(dm20Packet.toString()).thenReturn(expectedDM20);

        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        when(dm23Packet.getDtcs()).thenReturn(new ArrayList<>());
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.SLOW_FLASH);

        byte[] dm25bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0x1C, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);

        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);
        DiagnosticTroubleCode dm28DTC = mock(DiagnosticTroubleCode.class);
        when(dm28Packet.getDtcs()).thenReturn(List.of(dm28DTC));
        String expectedDM28String = "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" + NL +
                "DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times";
        when(dm28Packet.toString()).thenReturn(
                expectedDM28String);

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(-1);

        String expectedDM29String = "DM29 from Engine #1 (0): " + NL +
                "Emission-Related Pending DTC Count 9" +
                NL +
                "All Pending DTC Count 32" +
                NL +
                "Emission-Related MIL-On DTC Count 71" +
                NL +
                "Emission-Related Previously MIL-On DTC Count 49" +
                NL +
                "Emission-Related Permanent DTC Count 1";
        when(dm29Packet.toString()).thenReturn(expectedDM29String);

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet0.getSourceAddress()).thenReturn(0x00);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet17.getSourceAddress()).thenReturn(0x17);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet0.getSourceAddress()).thenReturn(0);
        when(dm33Packet0.toString()).thenReturn("DM33 from source address 0");
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        when(dm33Packet17.toString()).thenReturn("DM33 from source address 17");
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);
        when(dm33Packet21.toString()).thenReturn("DM33 from source address 21");

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);
        when(engineHoursPacket.toString()).thenReturn("Engine Hours from Engine #1 (0): 210,554,060.75 hours");

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet21);
            }
        };

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0x0036);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFB02);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0x0019);
        when(dm30Packet0.getTestResults()).thenReturn(List.of(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0x0000);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFF00);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(List.of(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(List.of(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm5Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false, List.of(),
                                                List.of()));

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm6Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm12Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm21Packet), List.of()));
        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm23Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));
        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm26Packet), List.of()));
        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, List.of(),
                                                List.of()));
        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm29Packet), List.of()));
        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false,
                                                List.of(dm31Packet), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0),
                                    List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17),
                                    List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet21),
                                    List.of()));

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, List.of(),
                                                List.of()));

        instance.setJ1939(j1939);

        assertFalse(instance.verify(List.of(dm28Packet),
                                    List.of(dm20Packet),
                                    dm33Packets,
                                    List.of(engineHoursPacket),
                                    listener));

        String expectedMessages = "Section A.5 verification failed at DM6 check done at table step 1.a" + NL +
                "Modules with source address 0, reported 0 DTCs." +
                NL +
                "MIL status is : on." +
                NL +
                "Section A.5 verification failed during DM12 check done at table step 1.b" +
                NL +
                "Modules with source address 0, reported 0 DTCs." +
                NL +
                "MIL status is : other." +
                NL +
                "Section A.5 verification failed during DM23 check done at table step 1.c" +
                NL +
                "Module with source address 0, reported 0 DTCs." +
                NL +
                "MIL status is : slow flash." +
                NL +
                "Section A.5 verification failed during DM29 check done at table step 1.d" +
                NL +
                "Modules with source address 0, DM29 from Engine #1 (0): " +
                NL +
                "Emission-Related Pending DTC Count 9" +
                NL +
                "All Pending DTC Count 32" +
                NL +
                "Emission-Related MIL-On DTC Count 71" +
                NL +
                "Emission-Related Previously MIL-On DTC Count 49" +
                NL +
                "Emission-Related Permanent DTC Count 1 " +
                NL +
                "Section A.5 verification failed during DM25 check done at table step 2.a" +
                NL +
                "Module with source address 0, has 1 supported SPNs" +
                NL +
                "Module with source address 23, has 1 supported SPNs" +
                NL +
                "Module with source address 33, has 1 supported SPNs" +
                NL +
                "PASS: Section A.5 Step 3.a DM31 Verification" +
                NL +
                "PASS: Section A.5 Step 3.b & 5.b DM21 Verification" +
                NL +
                "Section A.5 verification failed during DM5 check done at table step 1.e" +
                NL +
                "Modules with source address 0, reported 0 active DTCs and 2 previously active DTCs" +
                NL +
                "PASS: Section A.5 Step 4.a DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 5.a DM26 Verification" +
                NL +
                "Section A.5 verification failed during DM7/DM30 check done at table step 6.a" +
                NL +
                "DM30 Scaled Test Results for" +
                NL +
                "source address 0 are : [" +
                NL +
                "  TestMaximum failed and the value returned was : 54" +
                NL +
                "  TestResult failed and the value returned was : 64258" +
                NL +
                "  TestMinimum failed and the value returned was : 25" +
                NL +
                "]" +
                NL +
                "source address 23 are : [" +
                NL +
                "  TestResult failed and the value returned was : 65280" +
                NL +
                "]" +
                NL +
                "Section A.5 verification failed during DM20 check done at table step 7.a" +
                NL +
                "Previous Monitor Performance Ratio (DM20):" +
                NL +
                "DM20 from Engine #1 (0): [" +
                NL +
                " Num'r / Den'r" +
                NL +
                "Ignition Cycles 42,405" +
                NL +
                "OBD Monitoring Conditions Encountered 23,130" +
                NL +
                "SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535" +
                NL +
                "]" +
                NL +
                "Post Monitor Performance Ratio (DM20):" +
                NL +
                NL +
                "Section A.5 verification failed during DM28 check done at table step 8.a" +
                NL +
                "Pre DTC all clear code sent retrieved the DM28 packet :" +
                NL +
                "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other" +
                NL +
                "DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times"
                +
                NL +
                "Post DTC all clear code sent retrieved the DM28 packet :" +
                NL +
                NL +
                "Section A.5 verification failed during DM33 check done at table step 9.a" +
                NL +
                "Pre DTC all clear code sent retrieved the DM33 packet :" +
                NL +
                "   DM33 from source address 0" +
                NL +
                "   DM33 from source address 21" +
                NL +
                "Post DTC all clear code sent retrieved the DM33 packet :" +
                NL +
                "   DM33 from source address 0" +
                NL +
                "   DM33 from source address 17" +
                NL +
                "   DM33 from source address 21" +
                NL +
                "Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))" +
                NL +
                " and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any" +
                NL +
                " non-zero values present before code clear check done at table step 9.b" +
                NL +
                "Previous packet(s) was/were:" +
                NL +
                "   Engine Hours from Engine #1 (0): 210,554,060.75 hours" +
                NL +
                "Current packet(s) was/were:" +
                NL +
                "   EMPTY" +
                NL;
        assertEquals(expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM28(any());
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM31(any());
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyNoError() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(List.of());
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);

        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        byte[] dm25bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);

        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);
        when(dm28Packet.getDtcs()).thenReturn(List.of());

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(0);

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        int[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x0B, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF, 0x0C, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x0D, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x31, 0x01, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x38, 0x1A, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF };
        Packet.create(0, 0, data);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet0.getTestResults()).thenReturn(List.of(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(List.of(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(List.of(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm5Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm20Packet),
                                                List.of()));

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm6Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm12Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm23Packet), List.of()));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));

        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm26Packet), List.of()));
        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm28Packet), List.of()));
        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));
        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm31Packet), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet21), List.of()));

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, List.of(engineHoursPacket), List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verify(List.of(dm28Packet),
                                   List.of(dm20Packet),
                                   dm33Packets,
                                   List.of(engineHoursPacket),
                                   listener));

        String expectedMessages = "PASS: Section A.5 Step 1.a DM6 Verfication" +
                NL +
                "PASS: Section A.5 Step 1.b DM12 Verification" +
                NL +
                "PASS: Section A.5 Step 1.c DM23 Verification" +
                NL +
                "PASS: Section A.5 Step 1.d DM29 Verification" +
                NL +
                "PASS: Section A.5 Step 2.a DM25 Verification" +
                NL +
                "PASS: Section A.5 Step 3.a DM31 Verification" +
                NL +
                "PASS: Section A.5 Step 3.b & 5.b DM21 Verification" +
                NL +
                "PASS: Section A.5 Step 1.e DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 4.a DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 5.a DM26 Verification" +
                NL +
                "PASS: Section A.5 Step 6.a DM7/DM30 Verification" +
                NL +
                "PASS: Section A.5 Step 7.a DM20 Verification" +
                NL +
                "PASS: Section A.5 Step 8.a DM28 Verification" +
                NL +
                "PASS: Section A.5 Step 9.a DM33 Verification" +
                NL +
                "PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification";
        assertEquals(
                expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM28(any());
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM31(any());
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part01.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyNoErrorTwo() {
        ArrayList<Integer> obdModuleAddresses = new ArrayList<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(List.of());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(List.of());
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM20MonitorPerformanceRatioPacket dm20Packet = mock(DM20MonitorPerformanceRatioPacket.class);

        DM21DiagnosticReadinessPacket dm21Packet = mock(DM21DiagnosticReadinessPacket.class);
        DM23PreviouslyMILOnEmissionDTCPacket dm23Packet = mock(DM23PreviouslyMILOnEmissionDTCPacket.class);
        when(dm23Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        byte[] dm25bytes = { 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        Packet dm25Pack0 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x00, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet0 = new DM25ExpandedFreezeFrame(dm25Pack0);
        Packet dm25Pack17 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x17, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet17 = new DM25ExpandedFreezeFrame(dm25Pack17);
        Packet dm25Pack21 = Packet.create(DM25ExpandedFreezeFrame.PGN, 0x21, dm25bytes);
        DM25ExpandedFreezeFrame dm25Packet21 = new DM25ExpandedFreezeFrame(dm25Pack21);

        DM26TripDiagnosticReadinessPacket dm26Packet = mock(DM26TripDiagnosticReadinessPacket.class);

        DM28PermanentEmissionDTCPacket dm28Packet = mock(DM28PermanentEmissionDTCPacket.class);
        when(dm28Packet.getDtcs()).thenReturn(List.of());

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(0);

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet0.getTestResults()).thenReturn(new ArrayList<>());
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet17.getTestResults()).thenReturn(new ArrayList<>());
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet21.getTestResults()).thenReturn(new ArrayList<>());

        DM31DtcToLampAssociation dm31Packet = mock(DM31DtcToLampAssociation.class);

        DM33EmissionIncreasingAECDActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        int[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x0B, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF, 0x0C, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x0D, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x31, 0x01, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x38, 0x1A, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF };
        Packet.create(0, 0, data);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAECDActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAECDActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAECDActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        List<DM33EmissionIncreasingAECDActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);

        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSPNs()).thenReturn(List.of(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSPNs()).thenReturn(List.of(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSPNs()).thenReturn(List.of(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticMessageModule.requestDM5(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm5Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM20(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm20Packet),
                                                List.of()));

        when(diagnosticMessageModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm6Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM12(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm12Packet),
                                                List.of()));
        when(diagnosticMessageModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm21Packet), List.of()));
        when(diagnosticMessageModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm23Packet), List.of()));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet0));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet17));
        when(diagnosticMessageModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new BusResult<>(false, dm25Packet21));

        when(diagnosticMessageModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm26Packet), List.of()));
        when(diagnosticMessageModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm28Packet), List.of()));
        when(diagnosticMessageModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm29Packet), List.of()));
        when(diagnosticMessageModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, List.of(dm31Packet), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet0), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet17), List.of()));
        when(diagnosticMessageModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, List.of(dm33Packet21), List.of()));

        when(diagnosticMessageModule.requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet0));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet17));
        when(diagnosticMessageModule.requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), eq(31)))
                .thenReturn(List.of(dm30Packet21));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, List.of(engineHoursPacket),
                                                List.of()));

        instance.setJ1939(j1939);

        assertTrue(instance.verify(List.of(dm28Packet),
                                   List.of(dm20Packet),
                                   dm33Packets,
                                   List.of(engineHoursPacket),
                                   listener));

        String expectedMessages = "PASS: Section A.5 Step 1.a DM6 Verfication" +
                NL +
                "PASS: Section A.5 Step 1.b DM12 Verification" +
                NL +
                "PASS: Section A.5 Step 1.c DM23 Verification" +
                NL +
                "PASS: Section A.5 Step 1.d DM29 Verification" +
                NL +
                "PASS: Section A.5 Step 2.a DM25 Verification" +
                NL +
                "PASS: Section A.5 Step 3.a DM31 Verification" +
                NL +
                "PASS: Section A.5 Step 3.b & 5.b DM21 Verification" +
                NL +
                "PASS: Section A.5 Step 1.e DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 4.a DM5 Verification" +
                NL +
                "PASS: Section A.5 Step 5.a DM26 Verification" +
                NL +
                "PASS: Section A.5 Step 6.a DM7/DM30 Verification" +
                NL +
                "PASS: Section A.5 Step 7.a DM20 Verification" +
                NL +
                "PASS: Section A.5 Step 8.a DM28 Verification" +
                NL +
                "PASS: Section A.5 Step 9.a DM33 Verification" +
                NL +
                "PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification";
        assertEquals(
                expectedMessages, listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM5(any());
        verify(diagnosticMessageModule).requestDM20(any());

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestDM6(any());
        verify(diagnosticMessageModule).requestDM12(any());
        verify(diagnosticMessageModule).requestDM21(any());
        verify(diagnosticMessageModule).requestDM23(any());
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM25(any(), eq(0x21));
        verify(diagnosticMessageModule).requestDM26(any());
        verify(diagnosticMessageModule).requestDM28(any());
        verify(diagnosticMessageModule).requestDM29(any());
        verify(diagnosticMessageModule).requestDM31(any());
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x00));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x17));
        verify(diagnosticMessageModule).requestDM33(any(), eq(0x21));

        verify(diagnosticMessageModule).setJ1939(j1939);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x00), eq(247), eq(supportedSPN0).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x17), eq(247), eq(supportedSPN17).getSpn(), 31);
        verify(diagnosticMessageModule).requestTestResults(any(), eq(0x21), eq(247), eq(supportedSPN21).getSpn(), 31);

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }
}
