/**
 * Copyright 2020 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers.part1;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939;
import org.etools.j1939_84.bus.j1939.packets.AcknowledgmentPacket;
import org.etools.j1939_84.bus.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939_84.bus.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939_84.bus.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939_84.bus.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939_84.bus.j1939.packets.DM29DtcCounts;
import org.etools.j1939_84.bus.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939_84.bus.j1939.packets.DM31ScaledTestResults;
import org.etools.j1939_84.bus.j1939.packets.DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime;
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
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
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

/**
 * The unit test for {@link SectionA5Verifier}
 *
 * @author Marianne Schaefer (marianne.m.schaefer@gmail.com)
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SectionA5VerifierTest extends AbstractControllerTest {

    @Mock
    private AcknowledgmentPacket acknowledgmentPacket;

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

    private SectionA5Verifier instance;

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
    private VehicleInformationModule vehicleInformationModule;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        listener = new TestResultsListener(mockListener);
        dateTimeModule = new TestDateTimeModule();

        instance = new SectionA5Verifier(
                dataRepository,
                diagnosticReadinessModule,
                dtcModule,
                obdTestsModule,
                vehicleInformationModule);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                dataRepository,
                diagnosticReadinessModule,
                dtcModule,
                obdTestsModule,
                mockListener,
                vehicleInformationModule);
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.SectionA5Verifier#setJ1939(org.etools.j1939_84.bus.j1939.J1939)}.
     */
    @Test
    public void testSetJ1939() {

        instance.setJ1939(j1939);

        verify(diagnosticReadinessModule).setJ1939(j1939);

        verify(dtcModule).setJ1939(j1939);

        verify(obdTestsModule).setJ1939(j1939);

        verify(vehicleInformationModule).setJ1939(j1939);
    }

    /**
     * Testing errors in method
     * for{@link org.etools.j1939_84.controllers.part1.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyError() {
        Set<Integer> obdModuleAddresses = new HashSet<>() {
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
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(Collections.singletonList(monitoredSystemDM5));
        when(dm5Packet.getActiveCodeCount()).thenReturn((byte) 2);
        when(dm5Packet.toString()).thenReturn(
                "DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22");

        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        DiagnosticTroubleCode diagnosticTroubleCodeDM6 = mock(DiagnosticTroubleCode.class);
        when(dm6Packet.getDtcs()).thenReturn(Collections.singletonList(diagnosticTroubleCodeDM6));
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.FAST_FLASH);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        DiagnosticTroubleCode diagnosticTroubleCodeDM12 = mock(DiagnosticTroubleCode.class);
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.SLOW_FLASH);
        when(dm12Packet.getDtcs()).thenReturn(Collections.singletonList(diagnosticTroubleCodeDM12));

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
        when(dm23Packet.getDtcs()).thenReturn(Collections.singletonList(dm23Dtc));
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
        StringBuilder expectedDM28String = new StringBuilder(
                "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other");
        expectedDM28String.append(NL)
                .append("DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times");
        when(dm28Packet.toString()).thenReturn(expectedDM28String.toString());
        when(dm28Packet.getDtcs()).thenReturn(Collections.singletonList(diagnosticTroubleCode28));

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(-1);

        StringBuilder expectedDM29String = new StringBuilder("DM29 from Engine #1 (0): ");
        expectedDM29String.append(NL)
                .append("Emission-Related Pending DTC Count 9")
                .append(NL)
                .append("All Pending DTC Count 32")
                .append(NL)
                .append("Emission-Related MIL-On DTC Count 71")
                .append(NL)
                .append("Emission-Related Previously MIL-On DTC Count 49")
                .append(NL)
                .append("Emission-Related Permanent DTC Count 1");
        when(dm29Packet.toString()).thenReturn(expectedDM29String.toString());

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet0.getSourceAddress()).thenReturn(0x00);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet17.getSourceAddress()).thenReturn(0x17);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet21.getSourceAddress()).thenReturn(0x21);

        DM31ScaledTestResults dm31Packet = mock(DM31ScaledTestResults.class);
        DTCLampStatus dtcLampStatusDM31 = mock(DTCLampStatus.class);
        when(dm31Packet.getDtcLampStatuses()).thenReturn(Collections.singletonList(dtcLampStatusDM31));

        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);
        when(engineHoursPacket.toString()).thenReturn("Engine Hours from Engine #1 (0): 210,554,060.75 hours");

        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        Integer spn = 157;

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0xFF02);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0xFB00);
        when(dm30Packet0.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFB00);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFF16);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0x0000);
        when(dm30Packet17.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0x0000);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0x0016);
        when(dm30Packet21.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        when(supportedSPN0.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        when(supportedSPN17.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);
        when(supportedSPN21.getSpn()).thenReturn(spn);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticReadinessModule.requestDM5(any(), eq(true))).thenReturn(new RequestResult<>(false,
                Collections.singletonList(dm5Packet),
                Collections.emptyList()));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm20Packet),
                        Collections.emptyList()));

        when(dtcModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm6Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm12Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm21Packet), Collections.emptyList()));
        when(dtcModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm23Packet), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet0), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet17), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet21), Collections.emptyList()));
        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm26Packet), Collections.emptyList()));
        when(dtcModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm28Packet), Collections.emptyList()));
        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm29Packet), Collections.emptyList()));
        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm31Packet), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet0),
                        Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet17),
                        Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet21),
                        Collections.emptyList()));

        when(obdTestsModule.requestDM30Packets(any(), eq(0x00), eq(spn)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet0),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x17), eq(spn)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet17),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x21), eq(spn)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet21),
                        Collections.emptyList()));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(engineHoursPacket),
                        Collections.emptyList()));

        instance.setJ1939(j1939);

        assertFalse(instance.verify(Collections.emptyList(),
                Collections.emptyList(),
                dm33Packets,
                Collections.emptyList(),
                listener));

        StringBuilder expectedMessages = new StringBuilder(
                "Section A.5 verification failed at DM6 check done at table step 1.a");
        expectedMessages.append(NL)
                .append("Modules with source address 0, reported 1 DTCs.")
                .append(NL)
                .append("MIL status is : fast flash.")
                .append(NL)
                .append("Section A.5 verification failed during DM12 check done at table step 1.b")
                .append(NL)
                .append("Modules with source address 0, reported 1 DTCs.")
                .append(NL)
                .append("MIL status is : slow flash.")
                .append(NL)
                .append("Section A.5 verification failed during DM23 check done at table step 1.c")
                .append(NL)
                .append("Module with source address 0, reported 1 DTCs.")
                .append(NL)
                .append("MIL status is : on.")
                .append(NL)
                .append("Section A.5 verification failed during DM29 check done at table step 1.d")
                .append(NL)
                .append("Modules with source address 0, DM29 from Engine #1 (0): ")
                .append(NL)
                .append("Emission-Related Pending DTC Count 9")
                .append(NL)
                .append("All Pending DTC Count 32")
                .append(NL)
                .append("Emission-Related MIL-On DTC Count 71")
                .append(NL)
                .append("Emission-Related Previously MIL-On DTC Count 49")
                .append(NL)
                .append("Emission-Related Permanent DTC Count 1 ")
                .append(NL)
                .append("Section A.5 verification failed during DM5 check done at table step 1.e")
                .append(NL)
                .append("Modules with source address 0, reported 2 active DTCs and 0 previously acitve DTCs")
                .append(NL)
                .append("Section A.5 verification failed during DM25 check done at table step 2.a")
                .append(NL)
                .append("Module with source address 0, has 1 supported SPNs")
                .append(NL)
                .append("Module with source address 33, has 1 supported SPNs")
                .append(NL)
                .append("Module with source address 23, has 1 supported SPNs")
                .append(NL)
                .append("Section A.5 verification failed during DM31 check done at table step 3.a")
                .append(NL)
                .append("Modules with source address 0, is reporting 1 with DTC lamp status(es) causing MIL on.")
                .append(NL)
                .append("Section A.5 verification failed during DM21 check done at table step 3.b & 5.b")
                .append(NL)
                .append("Modules with source address 0, reported :")
                .append(NL)
                .append("0.0 km(s) for distance with the MIL on")
                .append(NL)
                .append("0.0 minute(s) run with the MIL on")
                .append(NL)
                .append("0.0 minute(s) while MIL is activated")
                .append(NL)
                .append("2453.3 km(s) since DTC code clear sent")
                .append(NL)
                .append("0.0 minute(s) since the DTC code clear sent")
                .append(NL)
                .append("Section A.5 verification failed during DM5 check done at table step 4.a")
                .append(NL)
                .append("Module address 0 :")
                .append(NL)
                .append("DM5 from Engine #1 (0): OBD Compliance: HD OBD (20), Active Codes: 11, Previously Active Codes: 22")
                .append(NL)
                .append("Section A.5 verification failed during DM26 check done at table step 5.a")
                .append(NL)
                .append("Modules with source address 0, reported 2 warm-ups since code clear")
                .append(NL)
                .append("Section A.5 verification failed during DM7/DM30 check done at table step 6.a")
                .append(NL)
                .append("DM30 Scaled Test Results for")
                .append(NL)
                .append("source address 0 are : [")
                .append(NL)
                .append("  TestMaximum failed and the value returned was : 65282")
                .append(NL)
                .append("]")
                .append(NL)
                .append("source address 33 are : [")
                .append(NL)
                .append("  TestMinimum failed and the value returned was : 22")
                .append(NL)
                .append("]")
                .append(NL)
                .append("source address 23 are : [")
                .append(NL)
                .append("  TestResult failed and the value returned was : 65302")
                .append(NL)
                .append("]")
                .append(NL)

                .append("Section A.5 verification failed during DM20 check done at table step 7.a")
                .append(NL)
                .append("Previous Monitor Performance Ratio (DM20):")
                .append(NL)
                .append("Post Monitor Performance Ratio (DM20):")
                .append(NL)
                .append("DM20 from Engine #1 (0): [")
                .append(NL)
                .append(" Num'r / Den'r")
                .append(NL)
                .append("Ignition Cycles 42,405")
                .append(NL)
                .append("OBD Monitoring Conditions Encountered 23,130")
                .append(NL)
                .append("SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535")
                .append(NL)
                .append("]")
                .append(NL)
                .append(NL)

                .append("Section A.5 verification failed during DM28 check done at table step 8.a")
                .append(NL)
                .append("Pre DTC all clear code sent retrieved the DM28 packet :")
                .append(NL)
                .append(NL)
                .append("Post DTC all clear code sent retrieved the DM28 packet :")
                .append(NL)
                .append("DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other")
                .append(NL)
                .append("DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times")
                .append(NL)

                .append("PASS: Section A.5 Step 9.a DM33 Verification")
                .append(NL)

                .append("Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))")
                .append(NL)
                .append("and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any")
                .append(NL)
                .append("non-zero values present before code clear check done at table step 9.b")
                .append(NL)
                .append("Previous packet(s) was/were:")
                .append(NL)
                .append("   EMPTY")
                .append(NL)
                .append("Current packet(s) was/were:")
                .append(NL)
                .append("   Engine Hours from Engine #1 (0): 210,554,060.75 hours")
                .append(NL);

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5(any(), eq(true));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true));

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM6(any());
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM21(any());
        verify(dtcModule).requestDM23(any());

        verify(dtcModule).requestDM25(any(), eq(0x00));
        verify(dtcModule).requestDM25(any(), eq(0x17));
        verify(dtcModule).requestDM25(any(), eq(0x21));

        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM28(any());
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM31(any());
        verify(dtcModule).requestDM33(any(), eq(0x00));
        verify(dtcModule).requestDM33(any(), eq(0x17));
        verify(dtcModule).requestDM33(any(), eq(0x21));

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x00), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x17), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x21), eq(spn));

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Testing errors in method
     * for{@link org.etools.j1939_84.controllers.part1.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test

    public void testVerifyMoreError() {
        Set<Integer> obdModuleAddresses = new HashSet<>() {
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
        when(dm5Packet.getContinuouslyMonitoredSystems()).thenReturn(Collections.singletonList(monitoredSystemDM5));
        when(dm5Packet.getPreviouslyActiveCodeCount()).thenReturn((byte) 2);

        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(Collections.emptyList());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.ON);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OTHER);
        when(dm12Packet.getDtcs()).thenReturn(Collections.emptyList());

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
        when(dm23Packet.getDtcs()).thenReturn(new ArrayList<DiagnosticTroubleCode>());
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
        when(dm28Packet.getDtcs()).thenReturn(Collections.singletonList(dm28DTC));
        StringBuilder expectedDM28String = new StringBuilder(
                "DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other");
        expectedDM28String.append(NL)
                .append("DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times");
        when(dm28Packet.toString()).thenReturn(expectedDM28String.toString());

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(-1);

        StringBuilder expectedDM29String = new StringBuilder("DM29 from Engine #1 (0): ");
        expectedDM29String.append(NL)
                .append("Emission-Related Pending DTC Count 9")
                .append(NL)
                .append("All Pending DTC Count 32")
                .append(NL)
                .append("Emission-Related MIL-On DTC Count 71")
                .append(NL)
                .append("Emission-Related Previously MIL-On DTC Count 49")
                .append(NL)
                .append("Emission-Related Permanent DTC Count 1");
        when(dm29Packet.toString()).thenReturn(expectedDM29String.toString());

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet0.getSourceAddress()).thenReturn(0x00);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        when(dm30Packet17.getSourceAddress()).thenReturn(0x17);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        DM31ScaledTestResults dm31Packet = mock(DM31ScaledTestResults.class);

        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet0.getSourceAddress()).thenReturn(0);
        when(dm33Packet0.toString()).thenReturn("DM33 from source address 0");
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        when(dm33Packet17.toString()).thenReturn("DM33 from source address 17");
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);
        when(dm33Packet21.toString()).thenReturn("DM33 from source address 21");

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);
        when(engineHoursPacket.toString()).thenReturn("Engine Hours from Engine #1 (0): 210,554,060.75 hours");

        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet21);
            }
        };

        Integer spn = 157;

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0x0036);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFB02);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0x0019);
        when(dm30Packet0.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0x0000);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFF00);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFB00);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        when(supportedSPN0.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        when(supportedSPN17.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);
        when(supportedSPN21.getSpn()).thenReturn(spn);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticReadinessModule.requestDM5(any(), eq(true)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm5Packet),
                        Collections.emptyList()));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        Collections.emptyList()));

        when(dtcModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm6Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm12Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm21Packet), Collections.emptyList()));
        when(dtcModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm23Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet0), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet17), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet21), Collections.emptyList()));
        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm26Packet), Collections.emptyList()));
        when(dtcModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        Collections.emptyList()));
        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm29Packet), Collections.emptyList()));
        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm31Packet), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet0),
                        Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet17),
                        Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet21),
                        Collections.emptyList()));

        when(obdTestsModule.requestDM30Packets(any(), eq(0x00), eq(spn)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet0),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x17), eq(spn)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet17),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x21), eq(157)))
                .thenReturn(new RequestResult<>(false,
                        Collections.singletonList(dm30Packet21),
                        Collections.emptyList()));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Collections.emptyList(),
                        Collections.emptyList()));

        instance.setJ1939(j1939);

        assertFalse(instance.verify(Collections.singletonList(dm28Packet),
                Collections.singletonList(dm20Packet),
                dm33Packets,
                Collections.singletonList(engineHoursPacket),
                listener));

        StringBuilder expectedMessages = new StringBuilder(
                "Section A.5 verification failed at DM6 check done at table step 1.a");
        expectedMessages.append(NL)
                .append("Modules with source address 0, reported 0 DTCs.")
                .append(NL)
                .append("MIL status is : on.")
                .append(NL)
                .append("Section A.5 verification failed during DM12 check done at table step 1.b")
                .append(NL)
                .append("Modules with source address 0, reported 0 DTCs.")
                .append(NL)
                .append("MIL status is : other.")
                .append(NL)
                .append("Section A.5 verification failed during DM23 check done at table step 1.c")
                .append(NL)
                .append("Module with source address 0, reported 0 DTCs.")
                .append(NL)
                .append("MIL status is : slow flash.")
                .append(NL)
                .append("Section A.5 verification failed during DM29 check done at table step 1.d")
                .append(NL)
                .append("Modules with source address 0, DM29 from Engine #1 (0): ")
                .append(NL)
                .append("Emission-Related Pending DTC Count 9")
                .append(NL)
                .append("All Pending DTC Count 32")
                .append(NL)
                .append("Emission-Related MIL-On DTC Count 71")
                .append(NL)
                .append("Emission-Related Previously MIL-On DTC Count 49")
                .append(NL)
                .append("Emission-Related Permanent DTC Count 1 ")
                .append(NL)
                .append("Section A.5 verification failed during DM5 check done at table step 1.e")
                .append(NL)
                .append("Modules with source address 0, reported 0 active DTCs and 2 previously acitve DTCs")
                .append(NL)
                .append("Section A.5 verification failed during DM25 check done at table step 2.a")
                .append(NL)
                .append("Module with source address 0, has 1 supported SPNs")
                .append(NL)
                .append("Module with source address 33, has 1 supported SPNs")
                .append(NL)
                .append("Module with source address 23, has 1 supported SPNs")
                .append(NL)
                .append("PASS: Section A.5 Step 3.a DM31 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 3.b & 5.b DM21 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 4.a DM5 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 5.a DM26 Verification")
                .append(NL)
                .append("Section A.5 verification failed during DM7/DM30 check done at table step 6.a")
                .append(NL)
                .append("DM30 Scaled Test Results for")
                .append(NL)
                .append("source address 0 are : [")
                .append(NL)
                .append("  TestMaximum failed and the value returned was : 54")
                .append(NL)
                .append("  TestResult failed and the value returned was : 64258")
                .append(NL)
                .append("  TestMinimum failed and the value returned was : 25")
                .append(NL)
                .append("]")
                .append(NL)
                .append("source address 23 are : [")
                .append(NL)
                .append("  TestResult failed and the value returned was : 65280")
                .append(NL)
                .append("]")
                .append(NL)
                .append("Section A.5 verification failed during DM20 check done at table step 7.a")
                .append(NL)
                .append("Previous Monitor Performance Ratio (DM20):")
                .append(NL)
                .append("DM20 from Engine #1 (0): [")
                .append(NL)
                .append(" Num'r / Den'r")
                .append(NL)
                .append("Ignition Cycles 42,405")
                .append(NL)
                .append("OBD Monitoring Conditions Encountered 23,130")
                .append(NL)
                .append("SPN 524287 Manufacturer Assignable SPN 524287 65,279/65,535")
                .append(NL)
                .append("]")
                .append(NL)
                .append("Post Monitor Performance Ratio (DM20):")
                .append(NL)
                .append(NL)
                .append("Section A.5 verification failed during DM28 check done at table step 8.a")
                .append(NL)
                .append("Pre DTC all clear code sent retrieved the DM28 packet :")
                .append(NL)
                .append("DM28 from Engine #1 (0): MIL: on, RSL: off, AWL: off, PL: other")
                .append(NL)
                .append("DTC: Engine Fuel 1 Injector Metering Rail 1 Pressure (157) Mechanical System Not Responding Or Out Of Adjustment (7) 1 times")
                .append(NL)

                .append("Post DTC all clear code sent retrieved the DM28 packet :")
                .append(NL)
                .append(NL)
                .append("Section A.5 verification failed during DM33 check done at table step 9.a")
                .append(NL)
                .append("Pre DTC all clear code sent retrieved the DM33 packet :")
                .append(NL)
                .append("   DM33 from source address 0")
                .append(NL)
                .append("   DM33 from source address 21")
                .append(NL)
                .append("Post DTC all clear code sent retrieved the DM33 packet :")
                .append(NL)
                .append("   DM33 from source address 0")
                .append(NL)
                .append("   DM33 from source address 17")
                .append(NL)
                .append("   DM33 from source address 21")
                .append(NL)

                .append("Section A.5 verification failed Cumulative engine runtime (PGN 65253 (SPN 247))")
                .append(NL)
                .append("and engine idletime (PGN 65244 (SPN 235)) shall not be reset/cleared for any")
                .append(NL)
                .append("non-zero values present before code clear check done at table step 9.b")
                .append(NL)
                .append("Previous packet(s) was/were:")
                .append(NL)
                .append("   Engine Hours from Engine #1 (0): 210,554,060.75 hours")
                .append(NL)
                .append("Current packet(s) was/were:")
                .append(NL)
                .append("   EMPTY")
                .append(NL);

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5(any(), eq(true));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true));

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM6(any());
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM21(any());
        verify(dtcModule).requestDM23(any());
        verify(dtcModule).requestDM25(any(), eq(0x00));
        verify(dtcModule).requestDM25(any(), eq(0x17));
        verify(dtcModule).requestDM25(any(), eq(0x21));
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM28(any());
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM31(any());
        verify(dtcModule).requestDM33(any(), eq(0x00));
        verify(dtcModule).requestDM33(any(), eq(0x17));
        verify(dtcModule).requestDM33(any(), eq(0x21));

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x00), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x17), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x21), eq(spn));

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyNoError() {
        Set<Integer> obdModuleAddresses = new HashSet<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(Collections.emptyList());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(Collections.emptyList());
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
        when(dm28Packet.getDtcs()).thenReturn(Collections.emptyList());

        DM29DtcCounts dm29Packet = mock(DM29DtcCounts.class);
        when(dm29Packet.getAllPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedMILOnDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPendingDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPermanentDTCCount()).thenReturn(0);
        when(dm29Packet.getEmissionRelatedPreviouslyMILOnDTCCount()).thenReturn(0);

        DM30ScaledTestResultsPacket dm30Packet0 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet17 = mock(DM30ScaledTestResultsPacket.class);
        DM30ScaledTestResultsPacket dm30Packet21 = mock(DM30ScaledTestResultsPacket.class);

        DM31ScaledTestResults dm31Packet = mock(DM31ScaledTestResults.class);

        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        int[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x0B, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF, 0x0C, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x0D, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x31, 0x01, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x38, 0x1A, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF };
        Packet.create(0, 0, data);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        Integer spn = 157;

        ScaledTestResult scaledTestResult0 = mock(ScaledTestResult.class);
        when(scaledTestResult0.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult0.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet0.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult0));
        ScaledTestResult scaledTestResult17 = mock(ScaledTestResult.class);
        when(scaledTestResult17.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult17.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet17.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult17));
        ScaledTestResult scaledTestResult21 = mock(ScaledTestResult.class);
        when(scaledTestResult21.getTestMaximum()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestValue()).thenReturn(0xFFFF);
        when(scaledTestResult21.getTestMinimum()).thenReturn(0xFFFF);
        when(dm30Packet21.getTestResults()).thenReturn(Collections.singletonList(scaledTestResult21));

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        when(supportedSPN0.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        when(supportedSPN17.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);
        when(supportedSPN21.getSpn()).thenReturn(spn);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticReadinessModule.requestDM5(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm5Packet),
                        Collections.emptyList()));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm20Packet),
                        Collections.emptyList()));

        when(dtcModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm6Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm12Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm21Packet), Collections.emptyList()));
        when(dtcModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm23Packet), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet0), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet17), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet21), Collections.emptyList()));

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm26Packet), Collections.emptyList()));
        when(dtcModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm28Packet), Collections.emptyList()));
        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm29Packet), Collections.emptyList()));
        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm31Packet), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet0), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet17), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet21), Collections.emptyList()));

        when(obdTestsModule.requestDM30Packets(any(), eq(0x00), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet0),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x17), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet17),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x21), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet21),
                        Collections.emptyList()));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(engineHoursPacket),
                        Collections.emptyList()));

        instance.setJ1939(j1939);

        assertTrue(instance.verify(Collections.singletonList(dm28Packet),
                Collections.singletonList(dm20Packet),
                dm33Packets,
                Collections.singletonList(engineHoursPacket),
                listener));

        StringBuilder expectedMessages = new StringBuilder();
        expectedMessages.append("PASS: Section A.5 Step 1.a DM6 Verfication")
                .append(NL)
                .append("PASS: Section A.5 Step 1.b DM12 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.c DM23 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.d DM29 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.e DM5 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 2.a DM25 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 3.a DM31 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 3.b & 5.b DM21 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 4.a DM5 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 5.a DM26 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 6.a DM7/DM30 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 7.a DM20 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 8.a DM28 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 9.a DM33 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5(any(), eq(true));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true));

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM6(any());
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM21(any());
        verify(dtcModule).requestDM23(any());
        verify(dtcModule).requestDM25(any(), eq(0x00));
        verify(dtcModule).requestDM25(any(), eq(0x17));
        verify(dtcModule).requestDM25(any(), eq(0x21));
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM28(any());
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM31(any());
        verify(dtcModule).requestDM33(any(), eq(0x00));
        verify(dtcModule).requestDM33(any(), eq(0x17));
        verify(dtcModule).requestDM33(any(), eq(0x21));

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x00), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x17), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x21), eq(spn));

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }

    /**
     * Test method for
     * {@link org.etools.j1939_84.controllers.part1.SectionA5Verifier#verify(java.util.List, java.util.List, java.util.List, java.util.List, org.etools.j1939_84.controllers.ResultsListener)}.
     */
    @Test
    public void testVerifyNoErrorAgain() {
        Set<Integer> obdModuleAddresses = new HashSet<>() {
            {
                add(0x00);
                add(0x17);
                add(0x21);
            }
        };

        DM5DiagnosticReadinessPacket dm5Packet = mock(DM5DiagnosticReadinessPacket.class);
        DM6PendingEmissionDTCPacket dm6Packet = mock(DM6PendingEmissionDTCPacket.class);
        when(dm6Packet.getDtcs()).thenReturn(Collections.emptyList());
        when(dm6Packet.getMalfunctionIndicatorLampStatus()).thenReturn(LampStatus.OFF);

        DM12MILOnEmissionDTCPacket dm12Packet = mock(DM12MILOnEmissionDTCPacket.class);
        when(dm12Packet.getDtcs()).thenReturn(Collections.emptyList());
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
        when(dm28Packet.getDtcs()).thenReturn(Collections.emptyList());

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

        DM31ScaledTestResults dm31Packet = mock(DM31ScaledTestResults.class);

        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet0 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        int[] data = { 0x01, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x04, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x06, 0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x0B, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF, 0x0C, 0x00, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x0D, 0x00,
                0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x31, 0x01, 0x00, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x38, 0x1A, 0x00, 0x00, 0x00, 0xFF, 0xFF, 0xFF,
                0xFF };
        Packet.create(0, 0, data);
        when(dm33Packet0.getSourceAddress()).thenReturn(0x00);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet17 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet17.getSourceAddress()).thenReturn(0x17);
        DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime dm33Packet21 = mock(
                DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime.class);
        when(dm33Packet21.getSourceAddress()).thenReturn(0x21);

        EngineHoursPacket engineHoursPacket = mock(EngineHoursPacket.class);

        List<DM33EmissionIncreasingAuxiliaryEmissionControlDeviceActiveTime> dm33Packets = new ArrayList<>() {
            {
                add(dm33Packet0);
                add(dm33Packet17);
                add(dm33Packet21);
            }
        };

        Integer spn = 157;

        SupportedSPN supportedSPN0 = mock(SupportedSPN.class);
        when(supportedSPN0.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN17 = mock(SupportedSPN.class);
        when(supportedSPN17.getSpn()).thenReturn(spn);
        SupportedSPN supportedSPN21 = mock(SupportedSPN.class);
        when(supportedSPN21.getSpn()).thenReturn(spn);

        OBDModuleInformation obdModuleInfo0 = mock(OBDModuleInformation.class);
        when(obdModuleInfo0.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN0));
        OBDModuleInformation obdModuleInfo17 = mock(OBDModuleInformation.class);
        when(obdModuleInfo17.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN17));
        OBDModuleInformation obdModuleInfo21 = mock(OBDModuleInformation.class);
        when(obdModuleInfo21.getTestResultSpns()).thenReturn(Collections.singletonList(supportedSPN21));

        when(dataRepository.getObdModuleAddresses()).thenReturn(obdModuleAddresses);
        when(dataRepository.getObdModule(0x00)).thenReturn(obdModuleInfo0);
        when(dataRepository.getObdModule(0x17)).thenReturn(obdModuleInfo17);
        when(dataRepository.getObdModule(0x21)).thenReturn(obdModuleInfo21);

        when(diagnosticReadinessModule.requestDM5(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm5Packet),
                        Collections.emptyList()));
        when(diagnosticReadinessModule.requestDM20(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm20Packet),
                        Collections.emptyList()));

        when(dtcModule.requestDM6(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm6Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM12(any(), eq(true)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm12Packet),
                        Collections.emptyList()));
        when(dtcModule.requestDM21(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm21Packet), Collections.emptyList()));
        when(dtcModule.requestDM23(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm23Packet), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x00)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet0), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x17)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet17), Collections.emptyList()));
        when(dtcModule.requestDM25(any(), eq(0x21)))
                .thenReturn(
                        new RequestResult<>(false, Collections.singletonList(dm25Packet21), Collections.emptyList()));

        when(dtcModule.requestDM26(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm26Packet), Collections.emptyList()));
        when(dtcModule.requestDM28(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm28Packet), Collections.emptyList()));
        when(dtcModule.requestDM29(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm29Packet), Collections.emptyList()));
        when(dtcModule.requestDM31(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm31Packet), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x00))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet0), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x17))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet17), Collections.emptyList()));
        when(dtcModule.requestDM33(any(), eq(0x21))).thenReturn(
                new RequestResult<>(false, Collections.singletonList(dm33Packet21), Collections.emptyList()));

        when(obdTestsModule.requestDM30Packets(any(), eq(0x00), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet0),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x17), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet17),
                        Collections.emptyList()));
        when(obdTestsModule.requestDM30Packets(any(), eq(0x21), eq(spn)))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(dm30Packet21),
                        Collections.emptyList()));

        when(vehicleInformationModule.requestEngineHours(any()))
                .thenReturn(new RequestResult<>(false, Collections.singletonList(engineHoursPacket),
                        Collections.emptyList()));

        instance.setJ1939(j1939);

        assertTrue(instance.verify(Collections.singletonList(dm28Packet),
                Collections.singletonList(dm20Packet),
                dm33Packets,
                Collections.singletonList(engineHoursPacket),
                listener));

        StringBuilder expectedMessages = new StringBuilder();
        expectedMessages.append("PASS: Section A.5 Step 1.a DM6 Verfication")
                .append(NL)
                .append("PASS: Section A.5 Step 1.b DM12 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.c DM23 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.d DM29 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 1.e DM5 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 2.a DM25 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 3.a DM31 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 3.b & 5.b DM21 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 4.a DM5 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 5.a DM26 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 6.a DM7/DM30 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 7.a DM20 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 8.a DM28 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 9.a DM33 Verification")
                .append(NL)
                .append("PASS: Section A.5 Step 9.b Cumulative engine runtime (PGN 65253 (SPN 247)) and engine idle time (PGN 65244 (SPN 235)) Verification");

        assertEquals(expectedMessages.toString(), listener.getMessages());
        assertEquals("", listener.getMilestones());
        assertEquals("", listener.getResults());

        verify(dataRepository).getObdModuleAddresses();
        verify(dataRepository).getObdModule(eq(0x00));
        verify(dataRepository).getObdModule(eq(0x17));
        verify(dataRepository).getObdModule(eq(0x21));

        verify(diagnosticReadinessModule).setJ1939(j1939);
        verify(diagnosticReadinessModule).requestDM5(any(), eq(true));
        verify(diagnosticReadinessModule).requestDM20(any(), eq(true));

        verify(dtcModule).setJ1939(j1939);
        verify(dtcModule).requestDM6(any());
        verify(dtcModule).requestDM12(any(), eq(true));
        verify(dtcModule).requestDM21(any());
        verify(dtcModule).requestDM23(any());
        verify(dtcModule).requestDM25(any(), eq(0x00));
        verify(dtcModule).requestDM25(any(), eq(0x17));
        verify(dtcModule).requestDM25(any(), eq(0x21));
        verify(dtcModule).requestDM26(any());
        verify(dtcModule).requestDM28(any());
        verify(dtcModule).requestDM29(any());
        verify(dtcModule).requestDM31(any());
        verify(dtcModule).requestDM33(any(), eq(0x00));
        verify(dtcModule).requestDM33(any(), eq(0x17));
        verify(dtcModule).requestDM33(any(), eq(0x21));

        verify(obdTestsModule).setJ1939(j1939);
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x00), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x17), eq(spn));
        verify(obdTestsModule).requestDM30Packets(any(), eq(0x21), eq(spn));

        verify(vehicleInformationModule).setJ1939(j1939);
        verify(vehicleInformationModule).requestEngineHours(any());
    }
}