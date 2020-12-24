/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.packets.GenericPacket;
import org.etools.j1939_84.bus.j1939.packets.SupportedSPN;
import org.etools.j1939_84.bus.j1939.packets.model.PgnDefinition;
import org.etools.j1939_84.bus.j1939.packets.model.Spn;
import org.etools.j1939_84.bus.j1939.packets.model.SpnDefinition;
import org.etools.j1939_84.model.FuelType;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939_84.model.Outcome;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA1ValidatorTest {

    @Mock
    private TableA1ValueValidator valueValidator;

    @Mock
    private DataRepository dataRepository;

    private TableA1Validator instance;

    @Mock
    private ResultsListener listener;

    @Before
    public void setUp() {
        instance = new TableA1Validator(valueValidator, dataRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(valueValidator, dataRepository, listener);
    }

    @Test
    public void reportImplausibleSPNValues() {
        List<GenericPacket> packets = new ArrayList<>();

        Collection<Integer> supportedSpns = List.of(1, 2, 3, 4, 5, 6);

        {
            GenericPacket packet1 = mock(GenericPacket.class);
            when(packet1.toString()).thenReturn("packet1");
            List<Spn> packet1Spns = new ArrayList<>();
            packet1Spns.add(mockSpn(1, 100.0));
            packet1Spns.add(mockSpn(2, 200.0));
            when(packet1.getSpns()).thenReturn(packet1Spns);

            Packet packet1Packet = mock(Packet.class);
            when(packet1Packet.getPgn()).thenReturn(11111);
            when(packet1.getPacket()).thenReturn(packet1Packet);
            PgnDefinition pgnDefinition = mockPgnDef(1, 2);
            when(packet1.getPgnDefinition()).thenReturn(pgnDefinition);

            packets.add(packet1);
        }

        {
            GenericPacket packet2 = mock(GenericPacket.class);
            when(packet2.toString()).thenReturn("packet2");
            List<Spn> packet2Spns = new ArrayList<>();
            packet2Spns.add(mockSpn(3, 100.0));
            packet2Spns.add(mockSpn(4, 200.0));
            when(packet2.getSpns()).thenReturn(packet2Spns);

            Packet packet2Packet = mock(Packet.class);
            when(packet2Packet.getPgn()).thenReturn(22222);
            when(packet2.getPacket()).thenReturn(packet2Packet);
            PgnDefinition pgnDefinition = mockPgnDef(3, 4);
            when(packet2.getPgnDefinition()).thenReturn(pgnDefinition);

            packets.add(packet2);
        }

        {
            GenericPacket packet = mock(GenericPacket.class);
            when(packet.toString()).thenReturn("packet3");
            List<Spn> spns = new ArrayList<>();
            spns.add(mockSpn(5, 100.0));
            spns.add(mockSpn(6, 200.0));
            when(packet.getSpns()).thenReturn(spns);

            Packet packetPacket = mock(Packet.class);
            when(packetPacket.getPgn()).thenReturn(33333);
            when(packet.getPacket()).thenReturn(packetPacket);
            PgnDefinition pgnDefinition = mockPgnDef(5, 6);
            when(packet.getPgnDefinition()).thenReturn(pgnDefinition);

            packets.add(packet);
            packets.add(packet); //Proves we only print it once
        }

        {
            GenericPacket packet = mock(GenericPacket.class);
            List<Spn> spns = new ArrayList<>();
            spns.add(mockSpn(7, 100.0));
            spns.add(mockSpn(8, 200.0));
            when(packet.getSpns()).thenReturn(spns);

            Packet packetPacket = mock(Packet.class);
            when(packetPacket.getPgn()).thenReturn(44444);
            when(packet.getPacket()).thenReturn(packetPacket);
            PgnDefinition pgnDefinition = mockPgnDef(7, 8);
            when(packet.getPgnDefinition()).thenReturn(pgnDefinition);

            packets.add(packet);
        }

        when(valueValidator.isImplausible(1, 100.0, true, FuelType.DSL)).thenReturn(false);
        when(valueValidator.isImplausible(2, 200.0, true, FuelType.DSL)).thenReturn(true);
        when(valueValidator.isImplausible(3, 100.0, true, FuelType.DSL)).thenReturn(true);
        when(valueValidator.isImplausible(4, 200.0, true, FuelType.DSL)).thenReturn(false);
        when(valueValidator.isImplausible(5, 100.0, true, FuelType.DSL)).thenReturn(false);
        when(valueValidator.isImplausible(6, 200.0, true, FuelType.DSL)).thenReturn(false);
        when(valueValidator.isImplausible(7, 100.0, true, FuelType.DSL)).thenReturn(false);
        when(valueValidator.isImplausible(8, 200.0, true, FuelType.DSL)).thenReturn(false);

        instance.reportImplausibleSPNValues(packets, supportedSpns, listener, true, FuelType.DSL, 1, 26);

        verify(listener).onResult("Found: packet1");
        verify(listener).onResult("Found: packet2");
        verify(listener).onResult("Found: packet3");

        verify(valueValidator).isImplausible(1, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(2, 200.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(3, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(4, 200.0, true, FuelType.DSL);
        verify(valueValidator, times(2)).isImplausible(5, 100.0, true, FuelType.DSL);
        verify(valueValidator, times(2)).isImplausible(6, 200.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(7, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(8, 200.0, true, FuelType.DSL);

        verify(listener).addOutcome(1, 26, WARN, "Value for SPN 2 (200.0) is implausible");
        verify(listener).onResult("WARN: 6.1.26 - Value for SPN 2 (200.0) is implausible");
        verify(listener).addOutcome(1, 26, WARN, "Value for SPN 3 (100.0) is implausible");
        verify(listener).onResult("WARN: 6.1.26 - Value for SPN 3 (100.0) is implausible");
    }

    @Test
    public void reportNonObdModuleProvidedSPNs() {

        OBDModuleInformation obdModuleInformation = mock(OBDModuleInformation.class);
        List<SupportedSPN> supportedSpns = new ArrayList<>();
        supportedSpns.add(mockSupportedSpn(544));//FAIL
        supportedSpns.add(mockSupportedSpn(190));//WARN
        supportedSpns.add(mockSupportedSpn(158));//PASS
        supportedSpns.add(mockSupportedSpn(96));//INFO
        supportedSpns.add(mockSupportedSpn(92));//Provided
        when(obdModuleInformation.getDataStreamSpns()).thenReturn(supportedSpns);

        when(dataRepository.getObdModule(0)).thenReturn(obdModuleInformation);

        List<GenericPacket> packets = new ArrayList<>();

        {
            GenericPacket packet1 = mock(GenericPacket.class);
            when(packet1.getSourceAddress()).thenReturn(21);

            List<Spn> packet1Spns = new ArrayList<>();
            packet1Spns.add(mockSpn(544, false));
            packet1Spns.add(mockSpn(190, false));
            packet1Spns.add(mockSpn(158, false));
            packet1Spns.add(mockSpn(96, false));

            when(packet1.getSpns()).thenReturn(packet1Spns);
            packets.add(packet1);
        }

        {
            GenericPacket packet2 = mock(GenericPacket.class);
            when(packet2.getSourceAddress()).thenReturn(0);
            packets.add(packet2);
        }

        instance.reportNonObdModuleProvidedSPNs(packets, 0, listener, 1, 26);

        verify(dataRepository, times(2)).getObdModule(0);
        verify(dataRepository).getObdModule(21);

        verify(listener).addOutcome(1, 26, FAIL, "SPN 544 provided by non-OBD Module");
        verify(listener).onResult("FAIL: 6.1.26 - SPN 544 provided by non-OBD Module");
        verify(listener).addOutcome(1, 26, WARN, "SPN 190 provided by non-OBD Module");
        verify(listener).onResult("WARN: 6.1.26 - SPN 190 provided by non-OBD Module");
        verify(listener).addOutcome(1, 26, Outcome.INFO, "SPN 96 provided by non-OBD Module");
        verify(listener).onResult("INFO: 6.1.26 - SPN 96 provided by non-OBD Module");

    }

    @Test
    public void reportDuplicateSPNs() {
        List<GenericPacket> packets = new ArrayList<>();

        {
            GenericPacket packet1 = mock(GenericPacket.class);
            when(packet1.getSourceAddress()).thenReturn(0);
            List<Spn> packet1Spns = new ArrayList<>();
            packet1Spns.add(mockSpn(92, 100.0)); //FAIL
            packet1Spns.add(mockSpn(512, 100.0)); //No Fail
            packet1Spns.add(mockSpn(84, 100.0)); //WARN
            packet1Spns.add(mockSpn(158, 200.0)); //PASS
            packet1Spns.add(mockSpn(3, 300.0));
            when(packet1.getSpns()).thenReturn(packet1Spns);
            packets.add(packet1);
            packets.add(packet1);
        }

        {
            GenericPacket packet2 = mock(GenericPacket.class);
            when(packet2.getSourceAddress()).thenReturn(21);
            List<Spn> packet2Spns = new ArrayList<>();
            packet2Spns.add(mockSpn(92, 100.0));
            packet2Spns.add(mockSpn(84, 100.0));
            packet2Spns.add(mockSpn(158, 100.0));
            packet2Spns.add(mockSpn(4, 400.0));
            when(packet2.getSpns()).thenReturn(packet2Spns);
            packets.add(packet2);
            packets.add(packet2);
        }

        instance.reportDuplicateSPNs(packets, listener, 1, 26);

        verify(listener).addOutcome(1, 26, FAIL, "SPN 92 provided by more than one module");
        verify(listener).onResult("FAIL: 6.1.26 - SPN 92 provided by more than one module");
        verify(listener).addOutcome(1, 26, WARN, "SPN 84 provided by more than one module");
        verify(listener).onResult("WARN: 6.1.26 - SPN 84 provided by more than one module");
    }

    @Test
    public void reportMissingSPNsAllForCI() {
        TestResultsListener testResultsListener = new TestResultsListener(listener);
        instance.reportMissingSPNs(Collections.emptyList(), testResultsListener, FuelType.DSL, 1, 26);
        String expected = "";
        expected += "FAIL: 6.1.26 - Required SPN 27 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 84 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 91 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 92 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 108 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 235 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 247 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 248 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 512 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 513 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 514 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 539 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 540 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 541 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 542 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 543 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 544 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 2791 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 2978 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3226 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3700 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 5466 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 5829 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 5837 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 6895 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 7333 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 96 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 110 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 132 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 157 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 190 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5313 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5466 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5827 is not supported" + NL;
        expected += "WARN: 6.1.26 - Required SPN 158 is not supported" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 110, 1637, 4076, 4193" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 190, 723, 4201, 4202" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 158, 168" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 5454, 5827" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 183, 1413, 1600" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3251, 3609, 3610" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 102, 106, 1127, 3563" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 94, 157, 164, 5313, 5314, 5578" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3516, 3518, 7346" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3031, 3515" + NL;
        assertEquals(expected, testResultsListener.getResults());

        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 27 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 84 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 91 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 92 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 108 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 235 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 247 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 248 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 512 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 513 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 514 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 539 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 540 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 541 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 542 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 543 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 544 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 2791 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 2978 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3226 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3700 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 5466 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 5829 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 5837 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 6895 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 7333 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 96 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 110 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 132 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 157 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 190 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5313 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5466 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5827 is not supported");
        verify(listener).addOutcome(1, 26, WARN, "Required SPN 158 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 110, 1637, 4076, 4193");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 190, 723, 4201, 4202");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 158, 168");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 5454, 5827");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 183, 1413, 1600");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3251, 3609, 3610");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 102, 106, 1127, 3563");
        verify(listener).addOutcome(1,
                26,
                FAIL,
                "At least one of these SPNs is not supported: 94, 157, 164, 5313, 5314, 5578");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3516, 3518, 7346");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3031, 3515");
    }

    @Test
    public void reportMissingSPNsAllForSI() {
        TestResultsListener testResultsListener = new TestResultsListener(listener);
        instance.reportMissingSPNs(Collections.emptyList(), testResultsListener, FuelType.GAS, 1, 26);
        String expected = "";
        expected += "FAIL: 6.1.26 - Required SPN 27 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 51 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 84 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 91 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 92 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 108 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 235 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 247 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 248 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 512 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 513 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 514 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 539 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 540 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 541 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 542 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 543 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 544 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 2791 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 2978 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3217 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3227 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3241 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3249 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 3464 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 4236 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 4237 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 4240 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 5829 is not supported" + NL;
        expected += "FAIL: 6.1.26 - Required SPN 5837 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 96 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 110 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 132 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 157 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 190 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5313 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5466 is not supported" + NL;
        expected += "INFO: 6.1.26 - Required SPN 5827 is not supported" + NL;
        expected += "WARN: 6.1.26 - Required SPN 158 is not supported" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 110, 1637, 4076, 4193" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 190, 723, 4201, 4202" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 158, 168" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 5454, 5827" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 183, 1413, 1600" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3251, 3609, 3610" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 102, 106, 1127, 3563" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 94, 157, 164, 5313, 5314, 5578" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3516, 3518, 7346" + NL;
        expected += "FAIL: 6.1.26 - At least one of these SPNs is not supported: 3031, 3515" + NL;
        assertEquals(expected, testResultsListener.getResults());

        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 27 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 51 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 84 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 91 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 92 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 108 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 235 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 247 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 248 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 512 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 513 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 514 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 539 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 540 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 541 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 542 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 543 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 544 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 2791 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 2978 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3217 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3227 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3241 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3249 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 3464 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 4236 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 4237 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 4240 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 5829 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "Required SPN 5837 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 96 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 110 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 132 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 157 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 190 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5313 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5466 is not supported");
        verify(listener).addOutcome(1, 26, INFO, "Required SPN 5827 is not supported");
        verify(listener).addOutcome(1, 26, WARN, "Required SPN 158 is not supported");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 110, 1637, 4076, 4193");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 190, 723, 4201, 4202");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 158, 168");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 5454, 5827");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 183, 1413, 1600");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3251, 3609, 3610");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 102, 106, 1127, 3563");
        verify(listener).addOutcome(1,
                26,
                FAIL,
                "At least one of these SPNs is not supported: 94, 157, 164, 5313, 5314, 5578");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3516, 3518, 7346");
        verify(listener).addOutcome(1, 26, FAIL, "At least one of these SPNs is not supported: 3031, 3515");
    }

    @Test
    public void reportMissingSPNsNone() {
        List<Integer> supportedSpns = Arrays.asList(27, 84, 91, 92, 108,
                235, 247, 248,
                512, 513, 514, 539, 540, 541, 542, 543, 544,
                2791, 2978, 3226, 3700,
                5466, 5829, 5837, 6895, 7333,
                96, 110, 132, 157, 190,
                5313, 5466, 5827,
                158, 110, 723,
                5454, 183, 3251, 1127, 94, 3516, 3515);
        TestResultsListener testResultsListener = new TestResultsListener(listener);
        instance.reportMissingSPNs(supportedSpns, testResultsListener, FuelType.DSL, 1, 26);
    }

    private static SupportedSPN mockSupportedSpn(int id) {
        SupportedSPN mock = mock(SupportedSPN.class);
        when(mock.getSpn()).thenReturn(id);
        return mock;
    }

    @SuppressWarnings("SameParameterValue")
    private static Spn mockSpn(int id, boolean isNotAvailable) {
        Spn mock = mock(Spn.class);
        when(mock.getId()).thenReturn(id);
        when(mock.isNotAvailable()).thenReturn(isNotAvailable);
        return mock;
    }

    private static Spn mockSpn(int id, Double value) {
        Spn mock = mock(Spn.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getValue()).thenReturn(value);
        return mock;
    }

    private static PgnDefinition mockPgnDef(int... spns) {
        PgnDefinition pgnDef = mock(PgnDefinition.class);
        List<SpnDefinition> spnDefs = new ArrayList<>();
        for (int spn : spns) {
            spnDefs.add(mockSpnDef(spn));
        }
        when(pgnDef.getSpnDefinitions()).thenReturn(spnDefs);
        return pgnDef;
    }

    private static SpnDefinition mockSpnDef(int id) {
        SpnDefinition mock = mock(SpnDefinition.class);
        when(mock.getSpnId()).thenReturn(id);
        return mock;
    }
}