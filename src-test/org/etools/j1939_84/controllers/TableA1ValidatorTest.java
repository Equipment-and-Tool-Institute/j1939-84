/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.WARN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.Spn;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.DM19CalibrationInformationPacket;
import org.etools.j1939tools.j1939.packets.DM24SPNSupportPacket;
import org.etools.j1939tools.j1939.packets.GenericPacket;
import org.etools.j1939tools.j1939.packets.Slot;
import org.etools.j1939tools.j1939.packets.SupportedSPN;
import org.etools.j1939tools.modules.DateTimeModule;
import org.etools.j1939tools.modules.TestDateTimeModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TableA1ValidatorTest {

    private DataRepository dataRepository;
    private TableA1Validator instance;
    @Mock
    private J1939DaRepository j1939DaRepository;
    @Mock
    private ResultsListener mockListener;
    private TestResultsListener listener;
    @Mock
    private TableA1ValueValidator valueValidator;

    private static PgnDefinition mockPgnDef(int... spns) {
        PgnDefinition pgnDef = mock(PgnDefinition.class);
        List<SpnDefinition> spnDefs = new ArrayList<>();
        for (int spn : spns) {
            spnDefs.add(mockSpnDef(spn));
        }
        when(pgnDef.getSpnDefinitions()).thenReturn(spnDefs);
        return pgnDef;
    }

    private static Spn mockSpn(int id, Double value) {
        Spn mock = mock(Spn.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getValue()).thenReturn(value);
        return mock;
    }

    private static SpnDefinition mockSpnDef(int id) {
        SpnDefinition mock = mock(SpnDefinition.class);
        when(mock.getSpnId()).thenReturn(id);
        return mock;
    }

    @Before
    public void setUp() {
        DateTimeModule.setInstance(new TestDateTimeModule());
        dataRepository = DataRepository.newInstance();
        listener = new TestResultsListener(mockListener);
        TableA1Repository tableA1Repository = new TableA1Repository();
        instance = new TableA1Validator(valueValidator, dataRepository, j1939DaRepository, tableA1Repository, 1, 26);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(valueValidator, j1939DaRepository, mockListener);
    }

    @Test
    public void testReportImplausibleSPNValues() {
        List<GenericPacket> packets = new ArrayList<>();
        {
            GenericPacket packet1 = mock(GenericPacket.class);
            when(packet1.toString()).thenReturn("packet1");
            List<Spn> packet1Spns = new ArrayList<>();
            packet1Spns.add(mockSpn(1, 100.0));
            packet1Spns.add(mockSpn(2, 200.0));
            when(packet1.getSpns()).thenReturn(packet1Spns);

            Packet packet1Packet = mock(Packet.class);
            when(packet1Packet.getPgn()).thenReturn(11111);
            when(packet1Packet.toTimeString()).thenReturn("packet1 packet");
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
            when(packet2Packet.toTimeString()).thenReturn("packet2 packet");
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
            packets.add(packet); // Proves we only print it once
        }

        {
            GenericPacket packet = mock(GenericPacket.class);
            List<Spn> spns = new ArrayList<>();
            spns.add(mockSpn(7, 100.0));
            spns.add(mockSpn(8, 200.0));
            when(packet.getSpns()).thenReturn(spns);

            packets.add(packet);
        }

        when(valueValidator.isImplausible(1, 100.0, true)).thenReturn(false);
        when(valueValidator.isImplausible(2, 200.0, true)).thenReturn(true);
        when(valueValidator.isImplausible(3, 100.0, true)).thenReturn(true);
        when(valueValidator.isImplausible(4, 200.0, true)).thenReturn(false);
        when(valueValidator.isImplausible(5, 100.0, true)).thenReturn(false);
        when(valueValidator.isImplausible(6, 200.0, true)).thenReturn(false);
        when(valueValidator.isImplausible(7, 100.0, true)).thenReturn(false);
        when(valueValidator.isImplausible(8, 200.0, true)).thenReturn(false);

        dataRepository.putObdModule(new OBDModuleInformation(0));

        for (GenericPacket packet : packets) {
            instance.reportImplausibleSPNValues(packet, listener, true, "6.1.26");
        }

        verify(valueValidator).isImplausible(1, 100.0, true);
        verify(valueValidator).isImplausible(2, 200.0, true);
        verify(valueValidator).isImplausible(3, 100.0, true);
        verify(valueValidator).isImplausible(4, 200.0, true);
        verify(valueValidator, times(2)).isImplausible(5, 100.0, true);
        verify(valueValidator, times(2)).isImplausible(6, 200.0, true);
        verify(valueValidator).isImplausible(7, 100.0, true);
        verify(valueValidator).isImplausible(8, 200.0, true);

        String expected = "";
        expected += "PGN 11111 with Supported SPNs " + NL;
        expected += "packet1 packet" + NL;
        expected += "Found: packet1" + NL;
        expected += "PGN 22222 with Supported SPNs " + NL;
        expected += "packet2 packet" + NL;
        expected += "Found: packet2" + NL;
        assertEquals(expected, listener.getResults());

        verify(mockListener).addOutcome(1,
                                        26,
                                        WARN,
                                        "6.1.26 - N.8 Engine #1 (0) reported value for SPN 2 (200.0) is implausible");
        verify(mockListener).addOutcome(1,
                                        26,
                                        WARN,
                                        "6.1.26 - N.8 Engine #1 (0) reported value for SPN 3 (100.0) is implausible");
    }

    @Test
    public void testNotAvailableForDM19() {
        OBDModuleInformation obdModuleInformation = new OBDModuleInformation(0);
        obdModuleInformation.set(DM24SPNSupportPacket.create(0,
                                                             SupportedSPN.create(1634, true, true, true, false, 15),
                                                             SupportedSPN.create(1635, true, true, true, false, 15)),
                                 1);
        dataRepository.putObdModule(obdModuleInformation);

        var calInfo = new DM19CalibrationInformationPacket.CalibrationInformation("CALID",
                                                                                  "BADBEEF",
                                                                                  "CALID".getBytes(
                                                                                                   StandardCharsets.UTF_8),
                                                                                  "BADBEEF".getBytes(StandardCharsets.UTF_8));
        var packet = DM19CalibrationInformationPacket.create(0, 0xF9, calInfo);

        instance.reportNotAvailableSPNs(packet, listener, "6.1.26");

        assertEquals(List.of(), listener.getOutcomes());
    }

    static List<GenericPacket> createPackets(int address) {
        return J1939DaRepository.getInstance()
                                .getPgnDefinitions()
                                .values()
                                .stream()
                                .map(pgnDef -> {
                                    int[] data = new int[getLength(pgnDef)];
                                    return new GenericPacket(Packet.create(pgnDef.getId(), address, data));
                                })
                                .collect(Collectors.toList());
    }

    private static int getLength(PgnDefinition pgnDef) {
        return pgnDef.getSpnDefinitions()
                     .stream()
                     .map(TableA1ValidatorTest::findSlot)
                     .mapToInt(Slot::getByteLength)
                     .map(length -> Math.max(length, 1))
                     .sum();
    }

    private static Slot findSlot(SpnDefinition spnDef) {
        return J1939DaRepository.getInstance().findSLOT(spnDef.getSlotNumber(), spnDef.getSpnId());
    }

}
