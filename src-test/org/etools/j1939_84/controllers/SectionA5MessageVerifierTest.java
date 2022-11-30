/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939tools.j1939.model.FuelType.DSL;
import static org.etools.j1939tools.j1939.packets.LampStatus.OFF;
import static org.etools.j1939tools.j1939.packets.LampStatus.ON;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.VehicleInformation;
import org.etools.j1939_84.modules.TestDateTimeModule;
import org.etools.j1939_84.modules.VehicleInformationModule;
import org.etools.j1939tools.CommunicationsListener;
import org.etools.j1939tools.bus.BusResult;
import org.etools.j1939tools.bus.RequestResult;
import org.etools.j1939tools.j1939.J1939;
import org.etools.j1939tools.j1939.packets.CompositeSystem;
import org.etools.j1939tools.j1939.packets.DM12MILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM20MonitorPerformanceRatioPacket;
import org.etools.j1939tools.j1939.packets.DM21DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM23PreviouslyMILOnEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.DM25ExpandedFreezeFrame;
import org.etools.j1939tools.j1939.packets.DM26TripDiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM28PermanentEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DM29DtcCounts;
import org.etools.j1939tools.j1939.packets.DM30ScaledTestResultsPacket;
import org.etools.j1939tools.j1939.packets.DM31DtcToLampAssociation;
import org.etools.j1939tools.j1939.packets.DM33EmissionIncreasingAECDActiveTime;
import org.etools.j1939tools.j1939.packets.DM5DiagnosticReadinessPacket;
import org.etools.j1939tools.j1939.packets.DM6PendingEmissionDTCPacket;
import org.etools.j1939tools.j1939.packets.DTCLampStatus;
import org.etools.j1939tools.j1939.packets.DiagnosticTroubleCode;
import org.etools.j1939tools.j1939.packets.EngineHoursPacket;
import org.etools.j1939tools.j1939.packets.EngineHoursTimer;
import org.etools.j1939tools.j1939.packets.FreezeFrame;
import org.etools.j1939tools.j1939.packets.IdleOperationPacket;
import org.etools.j1939tools.j1939.packets.PerformanceRatio;
import org.etools.j1939tools.j1939.packets.ScaledTestResult;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.CommunicationsModule;
import org.etools.j1939tools.modules.DateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SectionA5MessageVerifierTest {
    private static final int PART_NUMBER = 2;
    private static final int STEP_NUMBER = 3;
    private static final String SECTION = "6.2.3.4.a";
    private DataRepository dataRepository;

    @Mock
    private CommunicationsModule communicationsModule;

    private SectionA5MessageVerifier instance;

    @Mock
    private J1939 j1939;

    private TestResultsListener listener;

    @Mock
    private ResultsListener mockListener;

    @Mock
    private VehicleInformationModule vehicleInformationModule;

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());

        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);

        instance = new SectionA5MessageVerifier(dataRepository,
                                                communicationsModule,
                                                vehicleInformationModule,
                                                PART_NUMBER,
                                                STEP_NUMBER);
    }

    @After
    public void tearDown() {
        DateTimeModule.setInstance(null);
        verifyNoMoreInteractions(communicationsModule,
                                 mockListener,
                                 vehicleInformationModule);
    }

    @Test
    public void testSetJ1939() {
        instance.setJ1939(j1939);
        verify(communicationsModule).setJ1939(j1939);
        verify(vehicleInformationModule).setJ1939(j1939);
    }

    @Test
    public void checkDM6AsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM6(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM6(listener, 0);
    }

    @Test
    public void checkDM6AsErasedFailureWithDTC() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 1);
        dataRepository.putObdModule(moduleInfo);

        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var packet = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM6(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM6(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM6(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM6 data");
    }

    @Test
    public void checkDM6AsErasedFailureWithMIL() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM6(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM6(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM6 data");
    }

    @Test
    public void checkDM6AsNotErasedSuccess() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM6(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM6(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM6(listener, 0);
    }

    @Test
    public void checkDM6AsNotErasedFailure() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM6PendingEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM6PendingEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM6(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM6(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM6(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM6 data");
    }

    @Test
    public void checkDM12AsErasedSuccess() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM12(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM12(listener, 0);
    }

    @Test
    public void checkDM12AsErasedFailureWithDTC() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM12(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM12(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM12(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM12 data");
    }

    @Test
    public void checkDM12AsErasedFailureWithMIL() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM12(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM12(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM12 data");
    }

    @Test
    public void checkDM12AsNotErasedSuccess() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM12(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM12(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM12(listener, 0);
    }

    @Test
    public void checkDM12AsNotErasedFailure() {
        var dtc = DiagnosticTroubleCode.create(123, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM12MILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM12MILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM12(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM12(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM12(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM12 data");
    }

    @Test
    public void checkDM23AsErasedSuccess() {
        var dtc = DiagnosticTroubleCode.create(233, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM23(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM23(listener, 0);
    }

    @Test
    public void checkDM23AsErasedFailureWithDTC() {
        var dtc = DiagnosticTroubleCode.create(233, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM23(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM23(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM23 data");
    }

    @Test
    public void checkDM23AsErasedFailureWithMIL() {
        var dtc = DiagnosticTroubleCode.create(233, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM23(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM23(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM23 data");
    }

    @Test
    public void checkDM23AsNotErasedSuccess() {
        var dtc = DiagnosticTroubleCode.create(233, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM23(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM23(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM23(listener, 0);
    }

    @Test
    public void checkDM23AsNotErasedFailure() {
        var dtc = DiagnosticTroubleCode.create(233, 1, 1, 1);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM23PreviouslyMILOnEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM23PreviouslyMILOnEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM23(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM23(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM23(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM23 data");
    }

    @Test
    public void checkDM29AsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM29(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM29(listener, 0);
    }

    @Test
    public void checkDM29AsErasedFailureWithPending() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 1, 0, 0, 0, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM29(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM29(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM29 data");
    }

    @Test
    public void checkDM29AsErasedFailureWithActive() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 0, 0, 1, 0, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM29(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM29(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM29 data");
    }

    @Test
    public void checkDM29AsErasedFailureWithPrevious() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM29(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM29(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM29 data");
    }

    @Test
    public void checkDM29AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM29(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM29(listener, 0);
    }

    @Test
    public void checkDM29AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM29DtcCounts.create(0, 0, 1, 0, 1, 1, 0), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM29DtcCounts.create(0, 0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM29(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM29(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM29(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM29 data");
    }

    @Test
    public void checkDM5AsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0,
                                                         0,
                                                         0,
                                                         0x22,
                                                         List.of(),
                                                         List.of(CompositeSystem.COMPREHENSIVE_COMPONENT));

        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM5(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM5(listener, 0);
    }

    @Test
    public void checkDM5AsErasedFailureWithActiveCount() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22);
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM5(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM5(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM5 data");
    }

    @Test
    public void checkDM5AsErasedFailureWithPreviouslyActiveCount() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 0, 1, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 0, 1, 0x22);
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM5(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM5(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM5 data");
    }

    @Test
    public void checkDM5AsErasedFailureWithCompleteTest() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22, List.of(), List.of(CompositeSystem.CATALYST)),
                       1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 1, 0, 0x22, List.of(), List.of(CompositeSystem.CATALYST));
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM5(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM5(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM5 data");
    }

    @Test
    public void checkDM5AsNotErasedSuccessWithZeros() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22);
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM5(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM5(listener, 0);
    }

    @Test
    public void checkDM5AsNotErasedSuccessWithNotAvailable() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 0xFF, 0xFF, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 0xFF, 0xFF, 0x22);
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM5(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM5(listener, 0);
    }

    @Test
    public void checkDM5AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM5DiagnosticReadinessPacket.create(0, 1, 1, 0x22), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM5DiagnosticReadinessPacket.create(0, 0, 0, 0x22);
        when(communicationsModule.requestDM5(listener, 0)).thenReturn(BusResult.of(packet));

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        assertFalse(instance.checkDM5(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM5(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM5 data");
    }

    @Test
    public void checkDM25AsErasedSuccess() {
        var freezeFrame = new FreezeFrame(DiagnosticTroubleCode.create(1, 1, 1, 1), new int[0]);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM25ExpandedFreezeFrame.create(0, freezeFrame), 1);
        dataRepository.putObdModule(moduleInfo);

        VehicleInformation vehInfo = new VehicleInformation();
        vehInfo.setEngineModelYear(2018);
        vehInfo.setFuelType(DSL);
        dataRepository.setVehicleInformation(vehInfo);

        var packet = DM25ExpandedFreezeFrame.create(0);
        when(communicationsModule.requestDM25(listener, 0 )).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM25(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM25(listener, 0 );
    }

    @Test
    public void checkDM25AsErasedFailure() {
        var freezeFrame = new FreezeFrame(DiagnosticTroubleCode.create(1, 1, 1, 1), new int[0]);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM25ExpandedFreezeFrame.create(0, freezeFrame), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM25ExpandedFreezeFrame.create(0, freezeFrame);
        when(communicationsModule.requestDM25(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM25(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM25(listener, 0 );
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM25 data");
    }

    @Test
    public void checkDM25AsNotErasedSuccess() {
        var freezeFrame = new FreezeFrame(DiagnosticTroubleCode.create(1, 1, 1, 1), new int[0]);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM25ExpandedFreezeFrame.create(0, freezeFrame), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM25ExpandedFreezeFrame.create(0, freezeFrame);
        when(communicationsModule.requestDM25(listener, 0 )).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM25(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM25(listener, 0 );
    }

    @Test
    public void checkDM25AsNotErasedFailure() {
        var freezeFrame = new FreezeFrame(DiagnosticTroubleCode.create(1, 1, 1, 1), new int[0]);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM25ExpandedFreezeFrame.create(0, freezeFrame), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM25ExpandedFreezeFrame.create(0);
        when(communicationsModule.requestDM25(listener, 0 )).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM25(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM25(listener, 0 );
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM25 data");
    }

    @Test
    public void checkDM31AsErasedSuccess() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1, 1, 1, 1);
        var dtcLampStatus = DTCLampStatus.create(dtc, OFF, ON, OFF, OFF);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM31DtcToLampAssociation.create(0, 0, dtcLampStatus), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM31DtcToLampAssociation.create(0, 0);
        when(communicationsModule.requestDM31(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM31(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM31(listener, 0);
    }

    @Test
    public void checkDM31AsErasedFailure() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1, 1, 1, 1);
        var dtcLampStatus = DTCLampStatus.create(dtc, OFF, ON, OFF, OFF);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM31DtcToLampAssociation.create(0, 0, dtcLampStatus), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM31DtcToLampAssociation.create(0, 0, dtcLampStatus);
        when(communicationsModule.requestDM31(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM31(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM31(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM31 data");
    }

    @Test
    public void checkDM31AsNotErasedSuccess() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1, 1, 1, 1);
        var dtcLampStatus = DTCLampStatus.create(dtc, OFF, ON, OFF, OFF);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM31DtcToLampAssociation.create(0, 0, dtcLampStatus), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM31DtcToLampAssociation.create(0, 0, dtcLampStatus);
        when(communicationsModule.requestDM31(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM31(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM31(listener, 0);
    }

    @Test
    public void checkDM31AsNotErasedFailure() {
        DiagnosticTroubleCode dtc = DiagnosticTroubleCode.create(1, 1, 1, 1);
        var dtcLampStatus = DTCLampStatus.create(dtc, OFF, ON, OFF, OFF);
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM31DtcToLampAssociation.create(0, 0, dtcLampStatus), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM31DtcToLampAssociation.create(0, 0);
        when(communicationsModule.requestDM31(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM31(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM31(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM31 data");
    }

    @Test
    public void checkDM21AsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM21(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM21(listener, 0);
    }

    @Test
    public void checkDM21AsErasedFailureWithDistanceMIL() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 1, 0, 0, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM21(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM21(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM21 data");
    }

    @Test
    public void checkDM21AsErasedFailureTimeMIL() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM21(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM21(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM21 data");
    }

    @Test
    public void checkDM21AsErasedFailureWithDistanceSCC() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 1, 0, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM21(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM21(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM21 data");
    }

    @Test
    public void checkDM21AsErasedFailureTimeSCC() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 1);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM21(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM21(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM21 data");
    }

    @Test
    public void checkDM21AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 1, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM21(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM21(listener, 0);
    }

    @Test
    public void checkDM21AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM21DiagnosticReadinessPacket.create(0, 0, 1, 1, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM21DiagnosticReadinessPacket.create(0, 0, 0, 0, 0, 0);
        when(communicationsModule.requestDM21(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM21(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM21(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM21 data");
    }

    @Test
    public void checkDM26AsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM26TripDiagnosticReadinessPacket.create(0, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM26TripDiagnosticReadinessPacket.create(0, 0, 0);
        when(communicationsModule.requestDM26(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM26(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM26(listener, 0);
    }

    @Test
    public void checkDM26AsErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM26TripDiagnosticReadinessPacket.create(0, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM26TripDiagnosticReadinessPacket.create(0, 0, 1);
        when(communicationsModule.requestDM26(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM26(listener, SECTION, 0, true));

        verify(communicationsModule).requestDM26(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase DM26 data");
    }

    @Test
    public void checkDM26AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM26TripDiagnosticReadinessPacket.create(0, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM26TripDiagnosticReadinessPacket.create(0, 0, 1);
        when(communicationsModule.requestDM26(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM26(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM26(listener, 0);
    }

    @Test
    public void checkDM26AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM26TripDiagnosticReadinessPacket.create(0, 1, 1), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM26TripDiagnosticReadinessPacket.create(0, 0, 0);
        when(communicationsModule.requestDM26(listener, 0)).thenReturn(RequestResult.of(packet));

        assertFalse(instance.checkDM26(listener, SECTION, 0, false));

        verify(communicationsModule).requestDM26(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM26 data");
    }

    @Test
    public void checkDM20AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        var ratio0 = new PerformanceRatio(123, 1, 2, 0);
        moduleInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 13, 9, ratio0), 1);
        dataRepository.putObdModule(moduleInfo);

        var ratio = new PerformanceRatio(123, 1, 2, 0);
        var packet = DM20MonitorPerformanceRatioPacket.create(0, 13, 9, ratio);
        when(communicationsModule.requestDM20(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM20(listener, SECTION, 0));

        verify(communicationsModule).requestDM20(listener, 0);
    }

    @Test
    public void checkDM20AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        var ratio0 = new PerformanceRatio(123, 1, 2, 0);
        moduleInfo.set(DM20MonitorPerformanceRatioPacket.create(0, 13, 9, ratio0), 1);
        dataRepository.putObdModule(moduleInfo);

        var ratio = new PerformanceRatio(123, 0, 0, 0);
        var packet = DM20MonitorPerformanceRatioPacket.create(0, 0, 0, ratio);
        when(communicationsModule.requestDM20(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM20(listener, SECTION, 0));

        verify(communicationsModule).requestDM20(listener, 0);

        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM20 data");
    }

    @Test
    public void checkDM20AsErasedWithoutRepoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkDM20(listener, SECTION, 0));
    }

    @Test
    public void checkDM20AsNotErasedWithoutRepoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkDM20(listener, SECTION, 0));
    }

    @Test
    public void checkDM28AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(11, 1, 1, 1);
        moduleInfo.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM28PermanentEmissionDTCPacket.create(0, ON, OFF, OFF, OFF, dtc);
        when(communicationsModule.requestDM28(listener, 0)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkDM28(listener, SECTION, 0));

        verify(communicationsModule).requestDM28(listener, 0);
    }

    @Test
    public void checkDM28AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        var dtc = DiagnosticTroubleCode.create(11, 1, 1, 1);
        moduleInfo.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF, dtc), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF);
        when(communicationsModule.requestDM28(listener, 0)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkDM28(listener, SECTION, 0));

        verify(communicationsModule).requestDM28(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER, STEP_NUMBER, FAIL, "6.2.3.4.a - Engine #1 (0) erased DM28 data");
    }

    @Test
    public void checkDM28AsErasedWithoutRepoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkDM28(listener, SECTION, 0));
    }

    @Test
    public void checkDM28AsNotErasedWithoutRepoPacket() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM28PermanentEmissionDTCPacket.create(0, OFF, OFF, OFF, OFF), 1);
        dataRepository.putObdModule(moduleInfo);

        assertTrue(instance.checkDM28(listener, SECTION, 0));
    }

    @Test
    public void checkDM33AsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        var timer0 = EngineHoursTimer.create(1, 2, 3);
        moduleInfo.set(DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer0), 1);
        dataRepository.putObdModule(moduleInfo);

        var timer = EngineHoursTimer.create(1, 2, 3);
        var packet = DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer);
        when(communicationsModule.requestDM33(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM33(listener, SECTION, 0));

        verify(communicationsModule).requestDM33(listener, 0);
    }

    @Test
    public void checkDM33AsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        var timer0 = EngineHoursTimer.create(1, 2, 3);
        moduleInfo.set(DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer0), 1);
        dataRepository.putObdModule(moduleInfo);

        var timer = EngineHoursTimer.create(1, 0, 0);
        var packet = DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer);
        when(communicationsModule.requestDM33(listener, 0)).thenReturn(RequestResult.of(packet));

        boolean condition = instance.checkDM33(listener, SECTION, 0);
        assertFalse(condition);

        verify(communicationsModule).requestDM33(listener, 0);
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) erased DM33 Emission Increasing AECD Active Time data");
    }

    @Test
    public void checkDM33AsNotErasedWithoutRepoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkDM33(listener, SECTION, 0));
    }

    @Test
    public void checkDM33AsNotErasedWithoutZeroTimers() {
        var moduleInfo = new OBDModuleInformation(0);
        var timer0 = EngineHoursTimer.create(1, 0, 0);
        moduleInfo.set(DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer0), 1);
        dataRepository.putObdModule(moduleInfo);

        var timer = EngineHoursTimer.create(1, 0, 0);
        var packet = DM33EmissionIncreasingAECDActiveTime.create(0, 0, timer);
        when(communicationsModule.requestDM33(listener, 0)).thenReturn(RequestResult.of(packet));

        assertTrue(instance.checkDM33(listener, SECTION, 0));

        verify(communicationsModule).requestDM33(listener, 0);
    }

    @Test
    public void checkTestResultsAsErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 0)), 1);
        dataRepository.putObdModule(moduleInfo);

        var tr = ScaledTestResult.create(247, 123, 12, 1, 0, 0, 0);
        var packet = DM30ScaledTestResultsPacket.create(0, 0, tr);
        when(communicationsModule.requestTestResult(listener, 0, 247, 123, 31)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkTestResults(listener, SECTION, 0, true));

        verify(communicationsModule).requestTestResult(listener, 0, 247, 123, 31);
    }

    @Test
    public void checkTestResultsAsErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 0)), 1);
        dataRepository.putObdModule(moduleInfo);

        var tr = ScaledTestResult.create(247, 123, 12, 1, 1, 0, 0);
        var packet = DM30ScaledTestResultsPacket.create(0, 0, tr);
        when(communicationsModule.requestTestResult(listener, 0, 247, 123, 31)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkTestResults(listener, SECTION, 0, true));

        verify(communicationsModule).requestTestResult(listener, 0, 247, 123, 31);

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) did not erase Test Results data");
    }

    @Test
    public void checkTestResultsAsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 0)), 1);
        dataRepository.putObdModule(moduleInfo);

        var tr = ScaledTestResult.create(247, 123, 12, 1, 1, 0, 0);
        var packet = DM30ScaledTestResultsPacket.create(0, 0, tr);
        when(communicationsModule.requestTestResult(listener, 0, 247, 123, 31)).thenReturn(BusResult.of(packet));

        assertTrue(instance.checkTestResults(listener, SECTION, 0, false));

        verify(communicationsModule).requestTestResult(listener, 0, 247, 123, 31);
    }

    @Test
    public void checkTestResultsAsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(DM24SPNSupportPacket.create(0, SupportedSPN.create(123, true, true, true, false, 0)), 1);
        dataRepository.putObdModule(moduleInfo);

        var tr = ScaledTestResult.create(247, 123, 12, 1, 0xFB00, 0xFFFF, 0xFFFF);
        var packet = DM30ScaledTestResultsPacket.create(0, 0, tr);
        when(communicationsModule.requestTestResult(listener, 0, 247, 123, 31)).thenReturn(BusResult.of(packet));

        assertFalse(instance.checkTestResults(listener, SECTION, 0, false));

        verify(communicationsModule).requestTestResult(listener, 0, 247, 123, 31);

        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) erased Test Results data");
    }

    @Test
    public void checkIdleOperationAsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(IdleOperationPacket.create(0, 100), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = IdleOperationPacket.create(0, 101);
        when(communicationsModule.request(eq(IdleOperationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet));

        assertTrue(instance.checkEngineIdleTime(listener, SECTION, 0));

        verify(communicationsModule).request(eq(IdleOperationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
    }

    @Test
    public void checkIdleOperationAsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(IdleOperationPacket.create(0, 100), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = IdleOperationPacket.create(0, 0);
        when(communicationsModule.request(eq(IdleOperationPacket.PGN),
                                          eq(0x00),
                                          any(CommunicationsListener.class))).thenReturn(new BusResult<>(false, packet));

        assertFalse(instance.checkEngineIdleTime(listener, SECTION, 0));

        verify(communicationsModule).request(eq(IdleOperationPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) erased Idle Operation data");
    }

    @Test
    public void checkIdleOperationWithNoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkEngineIdleTime(listener, SECTION, 0));
    }

    @Test
    public void checkEngineHourAsNotErasedSuccess() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(EngineHoursPacket.create(0, 100), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = EngineHoursPacket.create(0, 101);
        when(communicationsModule.request(eq(EngineHoursPacket.PGN), eq(0x00), any(CommunicationsListener.class)))
                                                                                                                    .thenReturn(new BusResult<>(false,
                                                                                                                                              packet));

        assertTrue(instance.checkEngineRunTime(listener, SECTION, 0));

        verify(communicationsModule).request(eq(EngineHoursPacket.PGN),
                                             eq(0x00),
                                             any(CommunicationsListener.class));
    }

    @Test
    public void checkEngineHourAsNotErasedFailure() {
        var moduleInfo = new OBDModuleInformation(0);
        moduleInfo.set(EngineHoursPacket.create(0, 100), 1);
        dataRepository.putObdModule(moduleInfo);

        var packet = EngineHoursPacket.create(0, 0);
        when(communicationsModule.request(eq(EngineHoursPacket.PGN), eq(0x00), any(CommunicationsListener.class)))
                                                                                                                    .thenReturn(new BusResult<>(false,
                                                                                                                                              packet));

        assertFalse(instance.checkEngineRunTime(listener, SECTION, 0));

        verify(communicationsModule).request(eq(EngineHoursPacket.PGN), eq(0x00), any(CommunicationsListener.class));
        verify(mockListener).addOutcome(PART_NUMBER,
                                        STEP_NUMBER,
                                        FAIL,
                                        "6.2.3.4.a - Engine #1 (0) erased Engine Hours, Revolutions data");
    }

    @Test
    public void checkEngineHourWithNoPacket() {
        dataRepository.putObdModule(new OBDModuleInformation(0));

        assertTrue(instance.checkEngineRunTime(listener, SECTION, 0));
    }
}
