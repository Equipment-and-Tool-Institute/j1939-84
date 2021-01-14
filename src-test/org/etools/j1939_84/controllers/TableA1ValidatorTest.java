/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.etools.j1939_84.bus.Packet;
import org.etools.j1939_84.bus.j1939.J1939DaRepository;
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

    @Mock
    private J1939DaRepository j1939DaRepository;

    private TableA1Validator instance;

    @Mock
    private ResultsListener listener;

    @Before
    public void setUp() {
        instance = new TableA1Validator(valueValidator, dataRepository, j1939DaRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(valueValidator, dataRepository, j1939DaRepository, listener);
    }

    @Test
    public void reportImplausibleSPNValues() {
        List<GenericPacket> packets = new ArrayList<>();
        {
            GenericPacket packet1 = mock(GenericPacket.class);
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
            List<Spn> spns = new ArrayList<>();
            spns.add(mockSpn(5, 100.0));
            spns.add(mockSpn(6, 200.0));
            when(packet.getSpns()).thenReturn(spns);

            packets.add(packet);
            packets.add(packet); //Proves we only print it once
        }

        {
            GenericPacket packet = mock(GenericPacket.class);
            List<Spn> spns = new ArrayList<>();
            spns.add(mockSpn(7, 100.0));
            spns.add(mockSpn(8, 200.0));
            when(packet.getSpns()).thenReturn(spns);

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

        when(dataRepository.getObdModule(0)).thenReturn(new OBDModuleInformation(0));

        for (GenericPacket packet : packets) {
            instance.reportImplausibleSPNValues(packet, listener, true, FuelType.DSL, 1, 26, "6.1.26");
        }

        verify(dataRepository, atLeastOnce()).getObdModule(0);
//        verify(listener).onResult("Found: packet1");
//        verify(listener).onResult("Found: packet2");
//        verify(listener).onResult("Found: packet3");

        verify(valueValidator).isImplausible(1, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(2, 200.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(3, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(4, 200.0, true, FuelType.DSL);
        verify(valueValidator, times(2)).isImplausible(5, 100.0, true, FuelType.DSL);
        verify(valueValidator, times(2)).isImplausible(6, 200.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(7, 100.0, true, FuelType.DSL);
        verify(valueValidator).isImplausible(8, 200.0, true, FuelType.DSL);

        verify(listener).addOutcome(1, 26, WARN, "SA 0 reported value for SPN 2 (200.0) is implausible");
        verify(listener).onResult("WARN: 6.1.26 - SA 0 reported value for SPN 2 (200.0) is implausible");
        verify(listener).addOutcome(1, 26, WARN, "SA 0 reported value for SPN 3 (100.0) is implausible");
        verify(listener).onResult("WARN: 6.1.26 - SA 0 reported value for SPN 3 (100.0) is implausible");
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
        when(dataRepository.getObdModules()).thenReturn(List.of(obdModuleInformation));

        List<GenericPacket> packets = new ArrayList<>();

        {
            GenericPacket packet1 = mock(GenericPacket.class);
            when(packet1.getSourceAddress()).thenReturn(21);
            PgnDefinition pgnDef = mockPgnDef(544, 190, 158, 96, 92);
            when(packet1.getPgnDefinition()).thenReturn(pgnDef);
            Packet packet1Packet = mock(Packet.class);
            when(packet1Packet.getPgn()).thenReturn(61445);
            when(packet1.getPacket()).thenReturn(packet1Packet);
            when(packet1.toString()).thenReturn("packet1");

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

        for (GenericPacket packet : packets) {
            instance.reportNonObdModuleProvidedSPNs(packet, listener, 1, 26, "6.1.26");
        }

        verify(dataRepository, times(3)).getObdModules();
        verify(dataRepository).getObdModule(0);
        verify(dataRepository, times(2)).getObdModule(21);

        verify(listener).onResult("");
        verify(listener).onResult("PGN 61445 with Supported SPNs 92, 96, 158, 190, 544");
        verify(listener).onResult("Found: packet1");

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

        instance.reportDuplicateSPNs(packets, listener, 1, 26, "6.1.26");

        verify(listener).addOutcome(1, 26, FAIL, "SPN 92 provided by more than one module");
        verify(listener).onResult("FAIL: 6.1.26 - SPN 92 provided by more than one module");
        verify(listener).addOutcome(1, 26, WARN, "SPN 84 provided by more than one module");
        verify(listener).onResult("WARN: 6.1.26 - SPN 84 provided by more than one module");
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