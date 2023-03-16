/*
 * Copyright (c) 2020. Equipment & Tool Institute
 */

package org.etools.j1939_84.controllers.part01;

import static org.etools.j1939_84.J1939_84.NL;
import static org.etools.j1939_84.model.Outcome.FAIL;
import static org.etools.j1939_84.model.Outcome.INFO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.etools.j1939_84.controllers.BroadcastValidator;
import org.etools.j1939_84.controllers.DataRepository;
import org.etools.j1939_84.controllers.ResultsListener;
import org.etools.j1939_84.controllers.TestResultsListener;
import org.etools.j1939_84.model.OBDModuleInformation;
import org.etools.j1939tools.bus.Packet;
import org.etools.j1939tools.j1939.J1939DaRepository;
import org.etools.j1939tools.j1939.model.PgnDefinition;
import org.etools.j1939tools.j1939.model.SpnDefinition;
import org.etools.j1939tools.j1939.packets.GenericPacket;
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
public class BroadcastValidatorTest {

    @Mock
    private DataRepository dataRepository;
    private BroadcastValidator instance;
    @Mock
    private J1939DaRepository j1939DaRepository;

    private static String format(LocalDateTime timestamp) {
        return DateTimeModule.getInstance().getTimeFormatter().format(timestamp);
    }

    private static GenericPacket genericPacket(int pgn, int sourceAddress) {
        return BroadcastValidatorTest.genericPacket(pgn, sourceAddress, null);
    }

    private static GenericPacket genericPacket(int pgn, int sourceAddress, LocalDateTime timestamp) {
        GenericPacket mock = mock(GenericPacket.class);
        when(mock.getSourceAddress()).thenReturn(sourceAddress);

        Packet packet = mock(Packet.class);
        when(packet.getPgn()).thenReturn(pgn);
        if (timestamp != null) {
            when(packet.getTimestamp()).thenReturn(timestamp);
            when(packet.toTimeString()).thenReturn(format(timestamp) + " - " + pgn);
        }

        when(mock.getPacket()).thenReturn(packet);
        return mock;
    }

    private static PgnDefinition pgnDefinition(int broadcastPeriod, Boolean isVariable, int... spns) {
        List<SpnDefinition> spnDefs = new ArrayList<>();
        for (int spn : spns) {
            spnDefs.add(new SpnDefinition(spn, spn + "", 0, 0, 0));

        }
        PgnDefinition mock = mock(PgnDefinition.class);
        when(mock.getBroadcastPeriod()).thenReturn(broadcastPeriod);
        when(mock.getSpnDefinitions()).thenReturn(spnDefs);
        if (isVariable != null) {
            when(mock.isVariableBroadcast()).thenReturn(isVariable);
        }

        return mock;
    }

    private static PgnDefinition pgnDefinition(int broadcastPeriod, int... spns) {
        return pgnDefinition(broadcastPeriod, null, spns);
    }

    private static SupportedSPN supportedSPN(int spn) {
        SupportedSPN mock = mock(SupportedSPN.class);
        when(mock.getSpn()).thenReturn(spn);
        return mock;
    }

    private static List<SupportedSPN> supportedSPNs(int... spns) {
        return Arrays.stream(spns).mapToObj(BroadcastValidatorTest::supportedSPN).collect(Collectors.toList());
    }

    private static LocalDateTime time(int millisOffset) {
        LocalTime time = LocalTime.of(7, 30, 0).plusNanos(TimeUnit.MILLISECONDS.toNanos(millisOffset));
        return LocalDateTime.of(LocalDate.of(2020, 3, 15), time);
    }

    @Test
    public void buildPGNPacketsMap() {
        List<GenericPacket> packets = new ArrayList<>();
        packets.add(genericPacket(11111, 0));
        packets.add(genericPacket(22222, 0));
        packets.add(genericPacket(33333, 0));

        packets.add(genericPacket(11111, 99));
        packets.add(genericPacket(22222, 99));
        packets.add(genericPacket(33333, 99));

        packets.add(genericPacket(22222, 0));
        packets.add(genericPacket(33333, 0));

        packets.add(genericPacket(33333, 0));

        Map<Integer, Map<Integer, List<GenericPacket>>> actual = instance.buildPGNPacketsMap(packets);

        assertEquals(3, actual.size());

        assertEquals(2, actual.get(11111).size());
        long packets1 = actual.get(11111)
                              .get(0)
                              .stream()
                              .filter(p -> p.getPacket().getPgn() == 11111)
                              .filter(p -> p.getSourceAddress() == 0)
                              .count();
        assertEquals(1, packets1);

        assertEquals(2, actual.get(22222).size());
        long packets2 = actual.get(22222)
                              .get(0)
                              .stream()
                              .filter(p -> p.getPacket().getPgn() == 22222)
                              .filter(p -> p.getSourceAddress() == 0)
                              .count();
        assertEquals(2, packets2);

        assertEquals(2, actual.get(33333).size());
        long packets3 = actual.get(33333)
                              .get(0)
                              .stream()
                              .filter(p -> p.getPacket().getPgn() == 33333)
                              .filter(p -> p.getSourceAddress() == 0)
                              .count();
        assertEquals(3, packets3);
    }

    @Test
    public void buildPGNPacketsMapWithNoData() {
        Map<Integer, Map<Integer, List<GenericPacket>>> actual = instance.buildPGNPacketsMap(Collections.emptyList());
        assertTrue(actual.isEmpty());
    }

    @Test
    public void getMaximumBroadcastPeriod() {

        PgnDefinition pgnDefinition1 = pgnDefinition(1000);
        when(j1939DaRepository.findPgnDefinition(11111)).thenReturn(pgnDefinition1);
        PgnDefinition pgnDefinition2 = pgnDefinition(2000);
        when(j1939DaRepository.findPgnDefinition(22222)).thenReturn(pgnDefinition2);
        when(j1939DaRepository.findPgnDefinition(33333)).thenReturn(null);
        PgnDefinition pgnDefinition4 = pgnDefinition(-1);
        when(j1939DaRepository.findPgnDefinition(44444)).thenReturn(pgnDefinition4);

        List<Integer> pgns = Arrays.asList(11111, 22222, 33333, 44444);
        assertEquals(2, instance.getMaximumBroadcastPeriod(pgns));

        verify(j1939DaRepository).findPgnDefinition(11111);
        verify(j1939DaRepository).findPgnDefinition(22222);
        verify(j1939DaRepository).findPgnDefinition(33333);
        verify(j1939DaRepository).findPgnDefinition(44444);
    }

    @Test
    public void getMaximumBroadcastPeriodWithDefault() {
        assertEquals(5, instance.getMaximumBroadcastPeriod(Collections.emptyList()));
    }

    @Test
    public void reportBroadcastPeriod() {
        List<GenericPacket> packets = new ArrayList<>();
        packets.add(genericPacket(11111, 0, time(0)));

        packets.add(genericPacket(22222, 0, time(100)));
        packets.add(genericPacket(22222, 0, time(200)));

        // Values are ok
        packets.add(genericPacket(33333, 0, time(1000)));
        packets.add(genericPacket(33333, 0, time(2001)));
        packets.add(genericPacket(33333, 0, time(2999)));

        // Variable and too fast (it's ok)
        packets.add(genericPacket(44444, 0, time(5000)));
        packets.add(genericPacket(44444, 0, time(5050)));
        packets.add(genericPacket(44444, 0, time(5100)));

        // Not variable and too fast
        packets.add(genericPacket(55555, 0, time(1000)));
        packets.add(genericPacket(55555, 0, time(2000)));
        packets.add(genericPacket(55555, 0, time(3000)));

        // Too slow
        packets.add(genericPacket(66666, 0, time(2000)));
        packets.add(genericPacket(66666, 0, time(4500)));
        packets.add(genericPacket(66666, 0, time(7000)));

        // On Request
        packets.add(genericPacket(77777, 0, time(8000)));

        PgnDefinition pgnDef1 = pgnDefinition(1000, 111);
        when(j1939DaRepository.findPgnDefinition(11111)).thenReturn(pgnDef1);

        PgnDefinition pgnDef2 = pgnDefinition(1000, 222);
        when(j1939DaRepository.findPgnDefinition(22222)).thenReturn(pgnDef2);

        PgnDefinition pgnDef3 = pgnDefinition(1000, false, 333);
        when(j1939DaRepository.findPgnDefinition(33333)).thenReturn(pgnDef3);

        PgnDefinition pgnDef4 = pgnDefinition(100, true, 444);
        when(j1939DaRepository.findPgnDefinition(44444)).thenReturn(pgnDef4);

        PgnDefinition pgnDef5 = pgnDefinition(5000, false, 555);
        when(j1939DaRepository.findPgnDefinition(55555)).thenReturn(pgnDef5);

        PgnDefinition pgnDef6 = pgnDefinition(2000, false, 666);
        when(j1939DaRepository.findPgnDefinition(66666)).thenReturn(pgnDef6);

        PgnDefinition pgnDef7 = pgnDefinition(-1, 777);
        when(j1939DaRepository.findPgnDefinition(77777)).thenReturn(pgnDef7);

        ResultsListener mockListener = mock(ResultsListener.class);
        TestResultsListener listener = new TestResultsListener(mockListener);

        List<Integer> supportedSPNs = List.of(111, 222, 333, 444, 555, 666, 777);
        // Helper to make the map
        Map<Integer, Map<Integer, List<GenericPacket>>> packetMap = instance.buildPGNPacketsMap(packets);
        instance.reportBroadcastPeriod(packetMap, supportedSPNs, listener, 1, 26);

        verify(mockListener).addOutcome(1,
                                        26,
                                        INFO,
                                        "6.1.26 - Unable to determine period for PGN 11111 from Engine #1 (0)");

        verify(mockListener).addOutcome(1,
                                        26,
                                        INFO,
                                        "6.1.26 - Unable to determine period for PGN 22222 from Engine #1 (0)");

        verify(mockListener).addOutcome(1,
                                        26,
                                        FAIL,
                                        "6.1.26 - Broadcast period of PGN 55555 (1000 ms) by ECU Engine #1 (0) is less than 90% specified broadcast period of 5000 ms.");

        verify(mockListener).addOutcome(1,
                                        26,
                                        FAIL,
                                        "6.1.26 - Broadcast period of PGN 66666 (2500 ms) by ECU Engine #1 (0) is beyond 110% specified broadcast period of 2000 ms.");

        String expected = "" + NL;
        expected += "PGN 11111 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 11111" + NL;
        expected += NL;
        expected += "PGN 22222 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 22222" + NL;
        expected += "10:15:30.0000 - 22222" + NL;
        expected += NL;
        expected += "PGN 33333 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 33333" + NL;
        expected += "10:15:30.0000 - 33333" + NL;
        expected += "10:15:30.0000 - 33333" + NL;
        expected += NL;
        expected += "PGN 44444 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 44444" + NL;
        expected += "10:15:30.0000 - 44444" + NL;
        expected += "10:15:30.0000 - 44444" + NL;
        expected += NL;
        expected += "PGN 55555 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 55555" + NL;
        expected += "10:15:30.0000 - 55555" + NL;
        expected += "10:15:30.0000 - 55555" + NL;
        expected += NL;
        expected += "PGN 66666 from Engine #1 (0)" + NL;
        expected += "10:15:30.0000 - 66666" + NL;
        expected += "10:15:30.0000 - 66666" + NL;
        expected += "10:15:30.0000 - 66666" + NL;

        assertEquals(expected, listener.getResults());

        verify(j1939DaRepository).findPgnDefinition(11111);
        verify(j1939DaRepository).findPgnDefinition(22222);
        verify(j1939DaRepository).findPgnDefinition(33333);
        verify(j1939DaRepository).findPgnDefinition(44444);
        verify(j1939DaRepository).findPgnDefinition(55555);
        verify(j1939DaRepository).findPgnDefinition(66666);
        verify(j1939DaRepository).findPgnDefinition(77777);

        verifyNoMoreInteractions(mockListener);
    }

    @Before
    public void setUp() throws Exception {
        DateTimeModule.setInstance(new TestDateTimeModule());
        instance = new BroadcastValidator(dataRepository, j1939DaRepository);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(dataRepository);
    }

    @Test
    public void testGetMaximumBroadcastPeriod() {
        List<OBDModuleInformation> modules = new ArrayList<>();

        OBDModuleInformation module1 = mock(OBDModuleInformation.class);
        List<SupportedSPN> supportedSPNs1 = supportedSPNs(111, 222);
        when(module1.getFilteredDataStreamSPNs()).thenReturn(supportedSPNs1);
        modules.add(module1);

        OBDModuleInformation module2 = mock(OBDModuleInformation.class);
        List<SupportedSPN> supportedSPNs2 = supportedSPNs(333, 444);
        when(module2.getFilteredDataStreamSPNs()).thenReturn(supportedSPNs2);
        modules.add(module2);

        when(j1939DaRepository.getPgnForSpn(111)).thenReturn(Set.of(11111));
        when(j1939DaRepository.getPgnForSpn(222)).thenReturn(Set.of(22222));
        when(j1939DaRepository.getPgnForSpn(333)).thenReturn(null);
        when(j1939DaRepository.getPgnForSpn(444)).thenReturn(Set.of(44444));

        PgnDefinition pgnDefinition1 = pgnDefinition(1000);
        when(j1939DaRepository.findPgnDefinition(11111)).thenReturn(pgnDefinition1);
        PgnDefinition pgnDefinition2 = pgnDefinition(2000);
        when(j1939DaRepository.findPgnDefinition(22222)).thenReturn(pgnDefinition2);
        PgnDefinition pgnDefinition4 = pgnDefinition(-1);
        when(j1939DaRepository.findPgnDefinition(44444)).thenReturn(pgnDefinition4);

        when(dataRepository.getObdModules()).thenReturn(modules);

        assertEquals(5, instance.getMaximumBroadcastPeriod());

        verify(dataRepository).getObdModules();

        verify(j1939DaRepository).getPgnForSpn(111);
        verify(j1939DaRepository).getPgnForSpn(222);
        verify(j1939DaRepository).getPgnForSpn(333);
        verify(j1939DaRepository).getPgnForSpn(444);

        verify(j1939DaRepository).findPgnDefinition(11111);
        verify(j1939DaRepository).findPgnDefinition(22222);
        verify(j1939DaRepository).findPgnDefinition(44444);
    }

    @Test
    public void testGetMaximumBroadcastPeriodDefault() {
        when(dataRepository.getObdModules()).thenReturn(Collections.emptyList());

        assertEquals(5, instance.getMaximumBroadcastPeriod());

        verify(dataRepository).getObdModules();
    }

    @Test
    public void testcollectAndReportNotAvailableSPNs() {
        ResultsListener mockListener = mock(ResultsListener.class);
        List<Integer> v = instance.collectAndReportNotAvailableSPNs(0,
                                                                    List.of(new GenericPacket(Packet.parse("18FEC100 [8] FF FF FF FF 00 3B 0B 00"))), // foundPackets
                                                                    List.of(917), // supportedSPNs
                                                                    new ArrayList<>(List.of(0xFEC1)), // requiredPgns,
                                                                    mockListener,
                                                                    5,// partNumber
                                                                    6,// stepNumber
                                                                    "6.5.6"// section
        );
        assertEquals(List.of(), v);
    }
}
